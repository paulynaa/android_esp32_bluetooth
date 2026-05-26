package com.example.bt_def

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bt_def.databinding.FragmentListBinding
import com.google.android.material.snackbar.Snackbar

//
class DeviceListFragment : Fragment(), ItemAdapter.Listener {
    private lateinit var itemAdapter: ItemAdapter // paired
    private var bAdapter: BluetoothAdapter? = null // valdo bluetooth
    private lateinit var binding: FragmentListBinding // pasiekia ui xml
    private lateinit var discoveryAdapter: ItemAdapter // paieskai
    private lateinit var btLauncher: ActivityResultLauncher<Intent> // paleidzia sistemini langa
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>> // praso permissions
    private var preferences: SharedPreferences? = null // issaugo pasirinkta mac adresa

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = activity?.getSharedPreferences(BluetoothConstants.PREFERENCES, Context.MODE_PRIVATE)
        binding.imBluetoothOn.setOnClickListener {
            btLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) // jeigu bluetooth isjungtas, praso ijungti
        }
        binding.imBluetoothSearch.setOnClickListener { // paieskos mygtuko
           try {
               if(bAdapter?.isEnabled == true) {
                   bAdapter?.startDiscovery()
                   binding.imBluetoothSearch.visibility = View.GONE
                   binding.pbSearch.visibility = View.VISIBLE
               }
           }
           catch (e: SecurityException) {

           }
        }
        intentFilters()
        checkPermissions()
        initRcViews()
        registerBtLauncher()
        initBtAdapter()
        bluetoothState()
    }

    private fun initRcViews() = with(binding) { // sudaro du sarasus
        rcViewPaired.layoutManager = LinearLayoutManager(requireContext())
        rcViewSearch.layoutManager = LinearLayoutManager(requireContext())
        itemAdapter = ItemAdapter(this@DeviceListFragment, adapterType = false)
        discoveryAdapter = ItemAdapter(this@DeviceListFragment, adapterType = true)
        rcViewSearch.adapter = discoveryAdapter
        rcViewPaired.adapter = itemAdapter
    }

    private fun getPairedDevices() {
        val list = ArrayList<ListItem>()
        val deviceList = bAdapter?.bondedDevices as Set<BluetoothDevice>
        deviceList.forEach {
            list.add(
                ListItem(
                    it,
                    preferences?.getString(BluetoothConstants.MAC, "") == it.address))
        }
        binding.tvEmptyPaired.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        itemAdapter.submitList(list)
    }
    private fun initBtAdapter() {
        val bManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bAdapter = bManager.adapter
    }
    private fun bluetoothState() {
        if (bAdapter?.isEnabled == true){
            changeButtonColor(binding.imBluetoothOn, ContextCompat.getColor(requireContext(),R.color.white))
            getPairedDevices()

        }
    }
    private fun registerBtLauncher(){
        btLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                changeButtonColor(binding.imBluetoothOn, ContextCompat.getColor(requireContext(),R.color.white))
                getPairedDevices()
                Snackbar.make(binding.root, "Bluetooth is turned on", Snackbar.LENGTH_LONG).show()

            } else {
                Snackbar.make(binding.root, "Bluetooth is turned off", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun checkPermissions() {
        if (!checkBtPermissions()) {
            registerPermissionListener()
            launchBtPermissions()
        }
    }
    private fun launchBtPermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pLauncher.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION)

            )
        } else {
            pLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    private fun registerPermissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {

        }
    }

    private fun saveMac(mac: String) {
        val editor = preferences?.edit()
        editor?.putString(BluetoothConstants.MAC, mac)
        editor?.apply()
    }

    override fun onClick(item: ListItem) {
        saveMac(item.device.address)

    }
    private val bReceiver = object : BroadcastReceiver() { // laukia kol kazka ivyks su bluetooth
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) { // randa nauja irengini, idedu i sarasa, atnaujina ui
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val list = mutableSetOf<ListItem>()
                list.addAll(discoveryAdapter.currentList)
                if (device != null)  list.add(ListItem(device, false))
                discoveryAdapter.submitList(list.toList())
                binding.tvEmptySearch.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE // pagal tuscia sarasa pasikeicia ui
                try {Log.d("MyLog", "Device: ${device?.name}")}
                catch (e: SecurityException) {

                }

            } else if (intent?.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) { // cia poruotu irenginiu sarasas keiciasi
                getPairedDevices()

            } else if (intent?.action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED) { // kai baigesi paieska, keiciasi ikonele
                binding.imBluetoothSearch.visibility = View.VISIBLE
                binding.pbSearch.visibility = View.GONE

            }

        }

    }

    private fun intentFilters(){ // kai atsitiks f1, f2 ar f3, kviesk breceiver
        val f1 = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val f2 = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        val f3 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        activity?.registerReceiver(bReceiver, f1)
        activity?.registerReceiver(bReceiver, f2)
        activity?.registerReceiver(bReceiver, f3)
    }
}