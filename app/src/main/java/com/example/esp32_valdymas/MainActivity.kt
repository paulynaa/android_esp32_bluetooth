package com.example.esp32_valdymas

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.bt_def.BaseActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, BaseActivity::class.java))
        finish()
    }
}
