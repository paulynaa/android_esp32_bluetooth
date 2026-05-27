package com.example.esp32_valdymas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.example.esp32_valdymas.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    var onSendCommand: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bLed1On.setOnClickListener { onSendCommand?.invoke("LED1:1\n") }
        binding.bLed1Off.setOnClickListener { onSendCommand?.invoke("LED1:0\n") }
        binding.bLed2On.setOnClickListener { onSendCommand?.invoke("LED2:1\n") }
        binding.bLed2Off.setOnClickListener { onSendCommand?.invoke("LED2:0\n") }
        binding.bReqTemp.setOnClickListener  { onSendCommand?.invoke("REQ:TEMP\n") }
        binding.bReqHumid.setOnClickListener { onSendCommand?.invoke("REQ:UPTIME\n") }
        binding.bReqAll.setOnClickListener   { onSendCommand?.invoke("REQ:MEM\n") }

        binding.seekPwm.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvPwmValue.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.bSendPwm.setOnClickListener {
            val value = binding.seekPwm.progress
            onSendCommand?.invoke("PWM:$value\n")
        }
    }

    fun updateSensorData(key: String, value: String) {
        activity?.runOnUiThread {
            when (key.uppercase()) {
                "TEMP" -> binding.tvTemp.text = value
                "HUMID" -> binding.tvHumid.text = value
                "VALUE" -> binding.tvValue.text = value
            }
            binding.tvLastResponse.text = "$key: $value"
            binding.tvLastResponse.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        }
    }

    fun showResponse(message: String, isError: Boolean = false) {
        activity?.runOnUiThread {
            binding.tvLastResponse.text = message
            binding.tvLastResponse.setTextColor(
                android.graphics.Color.parseColor(if (isError) "#F44336" else "#4CAF50")
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}