package com.example.esp32_valdymas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.example.esp32_valdymas.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _b: FragmentDashboardBinding? = null
    private val b get() = _b!!
    var onSendCommand: ((String) -> Unit)? = null

    private var blinkOn = false

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentDashboardBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLed()
        setupRgb()
        setupBuzzer()
        setupMemory()
    }

    // ── LED ──────────────────────────────────────
    private fun setupLed() {
        b.bLedOn.setOnClickListener {
            blinkOn = false
            updateBlinkButton()
            onSendCommand?.invoke("LED1:1\n")
        }
        b.bLedOff.setOnClickListener {
            blinkOn = false
            updateBlinkButton()
            onSendCommand?.invoke("LED1:0\n")
        }
        b.bLedBlink.setOnClickListener {
            blinkOn = !blinkOn
            updateBlinkButton()
            onSendCommand?.invoke(if (blinkOn) "LED1:BLINK\n" else "LED1:BLINK_STOP\n")
        }
        b.seekBlinkSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, user: Boolean) {
                b.tvBlinkSpeed.text = "${p + 1}"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {
                onSendCommand?.invoke("BLINK_SPEED:${b.seekBlinkSpeed.progress + 1}\n")
            }
        })
    }

    private fun updateBlinkButton() {
        if (blinkOn) {
            b.bLedBlink.text = "STOP BLINK"
            b.bLedBlink.backgroundTintList = tint("#C2185B")
        } else {
            b.bLedBlink.text = "BLINK"
            b.bLedBlink.backgroundTintList = tint("#4A148C")
        }
    }

    // ── RGB
    private fun setupRgb() {
        b.bRgbRed.setOnClickListener {
            onSendCommand?.invoke("RGB:RED\n")
            highlightRgb(b.bRgbRed)
        }
        b.bRgbGreen.setOnClickListener {
            onSendCommand?.invoke("RGB:GREEN\n")
            highlightRgb(b.bRgbGreen)
        }
        b.bRgbBlue.setOnClickListener {
            onSendCommand?.invoke("RGB:BLUE\n")
            highlightRgb(b.bRgbBlue)
        }
        b.bRgbOff.setOnClickListener {
            onSendCommand?.invoke("RGB:OFF\n")
            highlightRgb(null)
        }
    }

    private fun highlightRgb(active: android.widget.Button?) {
        val buttons = listOf(b.bRgbRed, b.bRgbGreen, b.bRgbBlue, b.bRgbOff)
        val colors  = mapOf(
            b.bRgbRed   to Pair("#FFEBEE", "#C62828"),
            b.bRgbGreen to Pair("#E8F5E9", "#388E3C"),
            b.bRgbBlue  to Pair("#E3F2FD", "#1565C0"),
            b.bRgbOff   to Pair("#EFEBE9", "#4E342E")
        )
        buttons.forEach { btn ->
            val (bg, fg) = colors[btn]!!
            if (btn == active) {
                btn.backgroundTintList = tint(fg)
                btn.setTextColor(android.graphics.Color.WHITE)
            } else {
                btn.backgroundTintList = tint(bg)
                btn.setTextColor(android.graphics.Color.parseColor(fg))
            }
        }
    }

    // ── Buzzer ───────────────────────────────────
    private fun setupBuzzer() {
        b.bBeep.setOnClickListener  { onSendCommand?.invoke("BUZZ:1\n") }
        b.bAlarm.setOnClickListener { onSendCommand?.invoke("BUZZ:ALARM\n") }
        b.bMute.setOnClickListener  { onSendCommand?.invoke("BUZZ:STOP\n") }
    }

    // ── Memory ───────────────────────────────────
    private fun setupMemory() {
        b.bReqMem.setOnClickListener { onSendCommand?.invoke("REQ:MEM\n") }
    }

    fun onChipTemp(v: String) = activity?.runOnUiThread { b.tvChipTemp.text = "$v°C" }
    fun onLm35Temp(v: String) = activity?.runOnUiThread { b.tvLm35Temp.text = "$v°C" }

    fun onUptime(raw: String) {
        activity?.runOnUiThread {
            val parts = raw.split(",")
            if (parts.size == 4) {
                b.tvUptimeDays.text    = parts[0].removeSuffix("d")
                b.tvUptimeHours.text   = parts[1].removeSuffix("h")
                b.tvUptimeMinutes.text = parts[2].removeSuffix("m")
                b.tvUptimeSeconds.text = parts[3].removeSuffix("s")
            }
        }
    }

    fun onMem(raw: String) {
        activity?.runOnUiThread {
            val p = raw.split(",")
            if (p.size == 3) {
                val free  = p[0].toLongOrNull() ?: 0
                val used  = p[1].toLongOrNull() ?: 0
                val total = p[2].toLongOrNull() ?: 1
                b.tvMemFree.text = "${free / 1024} KB free"
                b.tvMemUsed.text = "${used / 1024} KB used"
                val pct = ((used.toFloat() / total) * 100).toInt()
                b.progressMem.progress = pct
                b.tvMemPercent.text = "$pct%"
            }
        }
    }

    fun onOk()               = activity?.runOnUiThread { setStatus("OK", "#4CAF50") }
    fun onError(msg: String) = activity?.runOnUiThread { setStatus(msg, "#F44336") }
    fun onButtonPressed()    = activity?.runOnUiThread { setStatus("Physical button pressed!", "#FF9800") }

    private fun setStatus(msg: String, hex: String) {
        b.tvLastResponse.text = msg
        b.tvLastResponse.setTextColor(android.graphics.Color.parseColor(hex))
    }

    private fun tint(hex: String) =
        android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(hex))

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}