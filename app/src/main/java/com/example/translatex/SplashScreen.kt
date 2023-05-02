package com.example.translatex

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.WindowCompat

class SplashScreen : AppCompatActivity() {
    lateinit var topAnim : Animation
    lateinit var bottomAnim : Animation
    lateinit var splashImage : ImageView
    lateinit var splashText : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        splashImage = findViewById(R.id.splashImage)
        splashText = findViewById(R.id.splashText)

        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_animation)
        topAnim.duration = 2500
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_animation)
        bottomAnim.duration = 2500

        splashImage.startAnimation(topAnim)
        splashText.startAnimation(bottomAnim)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        Handler(Looper.getMainLooper()).postDelayed({
            val i = Intent(this@SplashScreen, MainActivity::class.java)
            startActivity(i)
            finish()
        }, 5000)

    }
}