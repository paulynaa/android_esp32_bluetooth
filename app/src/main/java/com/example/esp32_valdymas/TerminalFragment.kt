package com.example.esp32_valdymas

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.esp32_valdymas.databinding.FragmentTerminalBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TerminalFragment : Fragment() {

    private var _binding: FragmentTerminalBinding? = null
    private val binding get() = _binding!!
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    var onSendCommand: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTerminalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bSend.setOnClickListener { sendInput() }

        binding.etInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                sendInput()
                true
            } else false
        }

        appendLog("Terminal ready. Type a command and press send.", LogType.SYSTEM)
        appendLog("Commands: LED1:1, LED1:0, LED2:1, LED2:0, PWM:128, REQ:TEMP, REQ:HUMID, REQ:ALL", LogType.SYSTEM)
    }

    private fun sendInput() {
        val text = binding.etInput.text.toString().trim()
        if (text.isNotEmpty()) {
            appendLog(text, LogType.SENT)
            onSendCommand?.invoke(text)
            binding.etInput.text.clear()
        }
    }

    fun appendLog(text: String, type: LogType = LogType.RECEIVED) {
        activity?.runOnUiThread {
            val tv = TextView(requireContext())
            val timestamp = timeFormat.format(Date())
            val prefix = when (type) {
                LogType.SENT -> "[$timestamp] > "
                LogType.RECEIVED -> "[$timestamp] < "
                LogType.SYSTEM -> "[$timestamp] # "
                LogType.ERROR -> "[$timestamp] ! "
                LogType.OK -> "[$timestamp] ✓ "
            }
            tv.text = "$prefix$text"
            tv.setTextColor(
                when (type) {
                    LogType.SENT -> Color.parseColor("#FF6B00")
                    LogType.RECEIVED -> Color.parseColor("#E8E8E8")
                    LogType.SYSTEM -> Color.parseColor("#555555")
                    LogType.ERROR -> Color.parseColor("#F44336")
                    LogType.OK -> Color.parseColor("#4CAF50")
                }
            )
            tv.typeface = android.graphics.Typeface.MONOSPACE
            tv.textSize = 12f
            tv.setPadding(0, 2, 0, 2)
            binding.logContainer.addView(tv)

            // Auto-scroll to bottom
            binding.scrollView.post {
                binding.scrollView.fullScroll(View.FOCUS_DOWN)
            }

            // Keep max 200 lines
            if (binding.logContainer.childCount > 200) {
                binding.logContainer.removeViewAt(0)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class LogType { SENT, RECEIVED, SYSTEM, ERROR, OK }
}