package com.example.elevenbarbershop.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.elevenbarbershop.R

class SplashAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()


        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashAct, LoginAct::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}