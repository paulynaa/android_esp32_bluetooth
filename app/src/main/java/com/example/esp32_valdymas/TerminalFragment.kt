package com.example.esp32_valdymas

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.esp32_valdymas.databinding.FragmentTerminalBinding
import java.text.SimpleDateFormat
import java.util.*

class TerminalFragment : Fragment() {

    private var _b: FragmentTerminalBinding? = null
    private val b get() = _b!!
    private val fmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    var onSendCommand: ((String) -> Unit)? = null

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentTerminalBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        b.bSend.setOnClickListener { sendInput() }
        b.bClear.setOnClickListener { b.logContainer.removeAllViews() }
        b.etInput.setOnEditorActionListener { _, id, ev ->
            if (id == EditorInfo.IME_ACTION_SEND ||
                ev?.keyCode == KeyEvent.KEYCODE_ENTER && ev.action == KeyEvent.ACTION_DOWN) {
                sendInput(); true
            } else false
        }
        appendLog("Terminal ready.", LogType.SYSTEM)
        appendLog("LED1:1 | LED1:0 | LED1:BLINK | LED1:BLINK_STOP", LogType.SYSTEM)
        appendLog("BLINK_SPEED:1-10 | RGB:r,g,b | SERVO:0-180", LogType.SYSTEM)
        appendLog("BUZZ:1 | BUZZ:ALARM | BUZZ:STOP", LogType.SYSTEM)
        appendLog("REQ:TEMP_CHIP | REQ:TEMP_LM35 | REQ:UPTIME | REQ:MEM", LogType.SYSTEM)
    }

    private fun sendInput() {
        val txt = b.etInput.text.toString().trim()
        if (txt.isNotEmpty()) {
            appendLog(txt, LogType.SENT)
            onSendCommand?.invoke("$txt\n")
            b.etInput.text.clear()
        }
    }

    fun appendLog(text: String, type: LogType = LogType.RECEIVED) {
        activity?.runOnUiThread {
            if (_b == null) return@runOnUiThread
            val tv = TextView(requireContext())
            val ts = fmt.format(Date())
            val prefix = when (type) {
                LogType.SENT     -> "[$ts] > "
                LogType.RECEIVED -> "[$ts] < "
                LogType.SYSTEM   -> "[$ts] # "
                LogType.ERROR    -> "[$ts] ! "
                LogType.OK       -> "[$ts] ✓ "
            }
            tv.text = "$prefix$text"
            tv.setTextColor(when (type) {
                LogType.SENT     -> Color.parseColor("#C2185B")
                LogType.RECEIVED -> Color.parseColor("#37474F")
                LogType.SYSTEM   -> Color.parseColor("#90A4AE")
                LogType.ERROR    -> Color.parseColor("#F44336")
                LogType.OK       -> Color.parseColor("#388E3C")
            })
            tv.typeface = android.graphics.Typeface.MONOSPACE
            tv.textSize = 12f
            tv.setPadding(0, 2, 0, 2)
            b.logContainer.addView(tv)
            b.scrollView.post { b.scrollView.fullScroll(View.FOCUS_DOWN) }
            if (b.logContainer.childCount > 200) b.logContainer.removeViewAt(0)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
    enum class LogType { SENT, RECEIVED, SYSTEM, ERROR, OK }
}