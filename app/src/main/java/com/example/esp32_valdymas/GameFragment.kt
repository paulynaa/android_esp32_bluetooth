package com.example.esp32_valdymas

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.example.esp32_valdymas.databinding.FragmentGameBinding

class GameFragment : Fragment() {

    private var _b: FragmentGameBinding? = null
    private val b get() = _b!!
    private var gameView: GameView? = null

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentGameBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gameView = GameView(requireContext()) { score, best ->
            activity?.runOnUiThread {
                b.tvScore.text = "SCORE: $score"
                b.tvBest.text  = "BEST: $best"
            }
        }
        b.gameContainer.addView(gameView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

        b.gameContainer.setOnClickListener { gameView?.onTap() }
        b.bRestart.setOnClickListener { gameView?.restart(); b.layoutGameOver.visibility = View.GONE }

        gameView?.onGameOver = {
            activity?.runOnUiThread { b.layoutGameOver.visibility = View.VISIBLE }
        }
    }

    override fun onResume()  { super.onResume();  gameView?.resume() }
    override fun onPause()   { super.onPause();   gameView?.pause()  }
    override fun onDestroyView() { super.onDestroyView(); gameView?.pause(); _b = null }
}

class GameView(ctx: Context, private val onScore: (Int, Int) -> Unit) : View(ctx) {

    // ── Physics ──────────────────────────────────
    private var playerY   = 0f
    private var velY      = 0f
    private var groundY   = 0f
    private var playerX   = 0f
    private var playerSize = 0f

    // Obstacle
    private var obsX      = 0f
    private var obsW      = 0f
    private var obsH      = 0f

    // Game state
    private var score     = 0
    private var bestScore = 0
    private var running   = false
    private var started   = false

    var onGameOver: (() -> Unit)? = null

    // Constants
    private val GRAVITY   = 1800f   // px/s²
    private val JUMP_VEL  = -700f
    private var obsSpeed  = 400f    // px/s

    // Timing
    private var lastTime  = 0L

    // Paint
    private val pPlayer = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#C2185B") }
    private val pObs    = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#AD1457") }
    private val pGround = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F8BBD0") }
    private val pText   = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#37474F"); textSize = 48f; typeface = Typeface.MONOSPACE
    }
    private val pHint   = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#90A4AE"); textSize = 36f; typeface = Typeface.MONOSPACE
    }

    private val thread = Thread {
        while (true) {
            if (running) {
                val now = System.nanoTime()
                if (lastTime != 0L) step((now - lastTime) / 1_000_000_000f)
                lastTime = now
                postInvalidate()
            }
            try { Thread.sleep(16) } catch (_: InterruptedException) { break }
        }
    }.also { it.isDaemon = true; it.start() }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        groundY    = h * 0.80f
        playerSize = h * 0.06f
        playerX    = w * 0.15f
        playerY    = groundY - playerSize
        obsW       = w * 0.04f
        obsH       = h * 0.12f
        obsX       = w.toFloat()
    }

    private fun step(dt: Float) {
        if (!started) return
        velY  += GRAVITY * dt
        playerY += velY * dt
        if (playerY >= groundY - playerSize) {
            playerY = groundY - playerSize; velY = 0f
        }
        obsX -= obsSpeed * dt
        if (obsX + obsW < 0) {
            obsX = width.toFloat()
            score++
            obsSpeed = 400f + score * 15f
            onScore(score, bestScore)
        }
        // Collision
        val px = playerX; val py = playerY
        val ox = obsX;    val oy = groundY - obsH
        if (px + playerSize > ox && px < ox + obsW && py + playerSize > oy) {
            if (score > bestScore) bestScore = score
            started = false
            running = false
            onScore(score, bestScore)
            onGameOver?.invoke()
        }
    }

    fun onTap() {
        if (!started) { started = true; running = true; lastTime = 0L; return }
        if (playerY >= groundY - playerSize - 2f) velY = JUMP_VEL
    }

    fun restart() {
        score = 0; obsSpeed = 400f
        playerY = groundY - playerSize; velY = 0f
        obsX = width.toFloat()
        started = false; running = true
        onScore(0, bestScore)
        postInvalidate()
    }

    fun resume() { running = started; lastTime = 0L }
    fun pause()  { running = false }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#FFF0F4"))
        // Ground
        canvas.drawRect(0f, groundY, width.toFloat(), groundY + 6f, pGround)
        // Player (square)
        canvas.drawRoundRect(playerX, playerY, playerX + playerSize, playerY + playerSize, 8f, 8f, pPlayer)
        // Obstacle
        canvas.drawRect(obsX, groundY - obsH, obsX + obsW, groundY, pObs)
        // Hint
        if (!started) {
            val hint = "TAP TO JUMP"
            val tw = pHint.measureText(hint)
            canvas.drawText(hint, (width - tw) / 2, height * 0.45f, pHint)
        }
    }
}