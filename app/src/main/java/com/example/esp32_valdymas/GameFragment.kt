package com.example.esp32_valdymas

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.esp32_valdymas.databinding.FragmentGameBinding

class GameFragment : Fragment() {

    private var _b: FragmentGameBinding? = null
    private val b get() = _b!!

    private var score = 0
    private var sessionActive = false

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentGameBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateDisplay()
        b.bRestart.setOnClickListener {
            score = 0
            updateDisplay()
        }
    }

    // Called from MainFragment whenever BTN:PRESSED arrives
    fun onPhysicalButtonPress() {
        if (!sessionActive) return
        score++
        activity?.runOnUiThread { updateDisplay() }
    }

    fun startSession() {
        sessionActive = true
        score = 0
        activity?.runOnUiThread { updateDisplay() }
    }

    fun endSession() {
        sessionActive = false
        activity?.runOnUiThread { updateDisplay() }
    }

    private fun updateDisplay() {
        if (_b == null) return
        b.tvScore.text = "SCORE: $score"
        b.tvBest.text  = if (sessionActive) "CONNECTED" else "DISCONNECTED"
        b.layoutGameOver.visibility = View.GONE
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}