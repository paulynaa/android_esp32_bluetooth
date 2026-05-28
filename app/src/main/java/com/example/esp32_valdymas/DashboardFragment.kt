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

        // LED
        binding.bLed1On.setOnClickListener { onSendCommand?.invoke("LED1:1\n") }
        binding.bLed1Off.setOnClickListener { onSendCommand?.invoke("LED1:0\n") }
        binding.bLed1Blink.setOnClickListener { onSendCommand?.invoke("LED1:BLINK\n") }

        // Sensor requests
        binding.bReqTemp.setOnClickListener  { onSendCommand?.invoke("REQ:TEMP\n") }
        binding.bReqHumid.setOnClickListener { onSendCommand?.invoke("REQ:UPTIME\n") }
        binding.bReqAll.setOnClickListener   { onSendCommand?.invoke("REQ:ALL\n") }

        // Buzzer
        binding.bBuzz.setOnClickListener      { onSendCommand?.invoke("BUZZ:1\n") }
        binding.bBuzzAlarm.setOnClickListener { onSendCommand?.invoke("BUZZ:ALARM\n") }

        // RGB seekbars
        val rgbListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val r = binding.seekR.progress
                val g = binding.seekG.progress
                val b = binding.seekB.progress
                binding.tvRgbValue.text = "R:$r G:$g B:$b"
                binding.viewRgbPreview.setBackgroundColor(
                    android.graphics.Color.rgb(r, g, b)
                )
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val r = binding.seekR.progress
                val g = binding.seekG.progress
                val b = binding.seekB.progress
                onSendCommand?.invoke("RGB:$r,$g,$b\n")
            }
        }
        binding.seekR.setOnSeekBarChangeListener(rgbListener)
        binding.seekG.setOnSeekBarChangeListener(rgbListener)
        binding.seekB.setOnSeekBarChangeListener(rgbListener)

        // Servo
        binding.seekServo.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvServoValue.text = "${progress}°"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                onSendCommand?.invoke("SERVO:${binding.seekServo.progress}\n")
            }
        })
    }

    fun updateSensorData(key: String, value: String) {
        activity?.runOnUiThread {
            when (key.uppercase()) {
                "TEMP" -> binding.tvTemp.text = value
                "HUMID" -> binding.tvHumid.text = value
                "POT"  -> binding.tvPot.text = "$value%"
                "VALUE" -> binding.tvValue.text = value
            }
            if (key.uppercase() != "POT") {  // don't flash last response for every pot tick
                binding.tvLastResponse.text = "$key: $value"
                binding.tvLastResponse.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            }
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