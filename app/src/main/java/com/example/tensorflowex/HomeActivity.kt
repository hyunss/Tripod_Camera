package com.example.tensorflowex

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget


class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val splash :ImageView = findViewById(R.id.imageView)
        val gifImage = GlideDrawableImageViewTarget(splash)
        Glide.with(this).load(R.drawable.splash).into(gifImage)

        startLoading()
    }

    private fun startLoading() {
        val handler = Handler()
        handler.postDelayed(Runnable {
            val intent = Intent(baseContext, CameraActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}