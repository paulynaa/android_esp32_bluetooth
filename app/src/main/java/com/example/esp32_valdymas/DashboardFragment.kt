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

    // blink state
    private var blinkOn = false

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentDashboardBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLed()
        setupTemperature()
        setupUptime()
        setupMemory()
        setupRgb()
        setupBuzzer()
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

    // ── Temperature ──────────────────────────────
    private fun setupTemperature() {
        b.bReqChipTemp.setOnClickListener { onSendCommand?.invoke("REQ:TEMP_CHIP\n") }
        b.bReqLm35.setOnClickListener    { onSendCommand?.invoke("REQ:TEMP_LM35\n") }
    }

    fun onChipTemp(v: String) = activity?.runOnUiThread { b.tvChipTemp.text = "$v°C" }
    fun onLm35Temp(v: String) = activity?.runOnUiThread { b.tvLm35Temp.text = "$v°C" }

    // ── Uptime ───────────────────────────────────
    private fun setupUptime() {
        // auto-updated from telemetry push
    }

    fun onUptime(raw: String) {
        // format: "0d,0h,5m,32s"
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

    // ── Memory ───────────────────────────────────
    private fun setupMemory() {
        b.bReqMem.setOnClickListener { onSendCommand?.invoke("REQ:MEM\n") }
    }

    fun onMem(raw: String) {
        // format: "free,used,total"
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

    // ── RGB ──────────────────────────────────────
    private fun setupRgb() {
        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, user: Boolean) {
                val r = b.seekR.progress; val g = b.seekG.progress; val bv = b.seekB.progress
                b.tvRgbVal.text = "R:$r  G:$g  B:$bv"
                b.viewRgbPreview.setBackgroundColor(android.graphics.Color.rgb(r, g, bv))
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {
                val r = b.seekR.progress; val g = b.seekG.progress; val bv = b.seekB.progress
                onSendCommand?.invoke("RGB:$r,$g,$bv\n")
            }
        }
        b.seekR.setOnSeekBarChangeListener(listener)
        b.seekG.setOnSeekBarChangeListener(listener)
        b.seekB.setOnSeekBarChangeListener(listener)
    }

    // ── Buzzer ───────────────────────────────────
    private fun setupBuzzer() {
        b.bBeep.setOnClickListener  { onSendCommand?.invoke("BUZZ:1\n") }
        b.bAlarm.setOnClickListener { onSendCommand?.invoke("BUZZ:ALARM\n") }
        b.bMute.setOnClickListener  { onSendCommand?.invoke("BUZZ:STOP\n") }
    }

    // ── Status helpers ────────────────────────────
    fun onOk()                   = activity?.runOnUiThread { setStatus("OK", "#4CAF50") }
    fun onError(msg: String)     = activity?.runOnUiThread { setStatus(msg, "#F44336") }
    fun onButtonPressed()        = activity?.runOnUiThread { setStatus("Physical button pressed!", "#FF9800") }

    private fun setStatus(msg: String, hex: String) {
        b.tvLastResponse.text = msg
        b.tvLastResponse.setTextColor(android.graphics.Color.parseColor(hex))
    }

    private fun tint(hex: String) =
        android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(hex))

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}