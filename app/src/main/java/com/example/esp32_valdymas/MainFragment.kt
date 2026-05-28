package com.example.esp32_valdymas

import android.animation.ObjectAnimator
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bt_def.BluetoothConstants
import com.example.bt_def.bluetooth.BluetoothController
import com.example.esp32_valdymas.databinding.FragmentMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainFragment : Fragment(), BluetoothController.Listener {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var bluetoothController: BluetoothController
    private var isConnected = false
    private var pulseAnimator: ObjectAnimator? = null

    lateinit var terminalFragment: TerminalFragment
    lateinit var dashboardFragment: DashboardFragment
    lateinit var gameFragment: GameFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBtAdapter()
        setupViewPager()

        val pref = activity?.getSharedPreferences(BluetoothConstants.PREFERENCES, Context.MODE_PRIVATE)
        val mac  = pref?.getString(BluetoothConstants.MAC, "")
        bluetoothController = BluetoothController(btAdapter)

        binding.bList.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_deviceListFragment)
        }
        binding.bConnect.setOnClickListener {
            if (isConnected) {
                bluetoothController.closeConnection()
                setDisconnected()
            } else {
                if (mac.isNullOrEmpty()) {
                    terminalFragment.appendLog("No device selected. Tap BT to choose one.", TerminalFragment.LogType.ERROR)
                } else {
                    terminalFragment.appendLog("Connecting to $mac…", TerminalFragment.LogType.SYSTEM)
                    bluetoothController.connect(mac, this)
                }
            }
        }
    }

    private fun setupViewPager() {
        terminalFragment  = TerminalFragment()
        dashboardFragment = DashboardFragment()
        gameFragment      = GameFragment()

        val send: (String) -> Unit = { cmd ->
            if (isConnected) bluetoothController.sendMessage(cmd)
            else terminalFragment.appendLog("Not connected.", TerminalFragment.LogType.ERROR)
        }
        terminalFragment.onSendCommand  = send
        dashboardFragment.onSendCommand = send

        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 3
            override fun createFragment(pos: Int) = when (pos) {
                0    -> terminalFragment
                1    -> dashboardFragment
                else -> gameFragment
            }
        }
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = true
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            tab.text = when (pos) { 0 -> "TERMINAL"; 1 -> "DASHBOARD"; else -> "GAME" }
        }.attach()
    }

    private fun initBtAdapter() {
        val bm = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = bm.adapter
    }

    private fun setConnected() {
        isConnected = true
        activity?.runOnUiThread {
            binding.bConnect.text = "DISCONNECT"
            binding.bConnect.backgroundTintList = tint("#C2185B")
            binding.tvConnectionStatus.text = "CONNECTED"
            binding.tvConnectionStatus.setTextColor(color("#4CAF50"))
            binding.viewStatusDot.setBackgroundResource(R.drawable.status_dot_connected)
            startPulse()
            gameFragment.startSession()
        }
    }

    private fun setDisconnected() {
        isConnected = false
        activity?.runOnUiThread {
            binding.bConnect.text = "CONNECT"
            binding.bConnect.backgroundTintList = tint("#AD1457")
            binding.tvConnectionStatus.text = "DISCONNECTED"
            binding.tvConnectionStatus.setTextColor(color("#9E9E9E"))
            binding.viewStatusDot.setBackgroundResource(R.drawable.status_dot)
            stopPulse()
            gameFragment.endSession()
        }
    }

    private fun startPulse() {
        pulseAnimator?.cancel()
        pulseAnimator = ObjectAnimator.ofFloat(binding.viewStatusDot, "alpha", 1f, 0.2f, 1f).apply {
            duration = 1500; repeatCount = ObjectAnimator.INFINITE; start()
        }
    }
    private fun stopPulse() { pulseAnimator?.cancel(); binding.viewStatusDot.alpha = 1f }

    private fun tint(hex: String) =
        android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(hex))
    private fun color(hex: String) = android.graphics.Color.parseColor(hex)

    private fun parseAndDispatch(raw: String) {
        raw.split("\n").forEach { line ->
            val msg = line.trim()
            if (msg.isEmpty()) return@forEach
            when {
                msg.startsWith("CHIP_TEMP:") -> {
                    dashboardFragment.onChipTemp(msg.removePrefix("CHIP_TEMP:"))
                }
                msg.startsWith("LM35_TEMP:") -> {
                    dashboardFragment.onLm35Temp(msg.removePrefix("LM35_TEMP:"))
                }
                msg.startsWith("UPTIME:") -> {
                    dashboardFragment.onUptime(msg.removePrefix("UPTIME:"))
                }
                msg.startsWith("MEM:") -> {
                    dashboardFragment.onMem(msg.removePrefix("MEM:"))
                    terminalFragment.appendLog(msg, TerminalFragment.LogType.OK)
                }
                msg == "BTN:PRESSED" -> {
                    dashboardFragment.onButtonPressed()
                    gameFragment.onPhysicalButtonPress()
                    terminalFragment.appendLog("Physical button pressed", TerminalFragment.LogType.OK)
                }
                msg == "OK" -> {
                    dashboardFragment.onOk()
                    terminalFragment.appendLog("OK", TerminalFragment.LogType.OK)
                }
                msg.startsWith("ERR:") -> {
                    dashboardFragment.onError(msg)
                    terminalFragment.appendLog(msg, TerminalFragment.LogType.ERROR)
                }
                else -> terminalFragment.appendLog(msg, TerminalFragment.LogType.RECEIVED)
            }
        }
    }

    override fun onReceive(message: String) {
        when (message) {
            BluetoothController.BLUETOOTH_CONNECTED -> {
                setConnected()
                terminalFragment.appendLog("Connected!", TerminalFragment.LogType.OK)
            }
            BluetoothController.BLUETOOTH_NO_CONNECTED -> {
                setDisconnected()
                terminalFragment.appendLog("Connection lost.", TerminalFragment.LogType.ERROR)
            }
            else -> parseAndDispatch(message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pulseAnimator?.cancel()
        _binding = null
    }
}