package com.example.brickkotlin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GameOver : AppCompatActivity() {
    lateinit var tvPoints: TextView
    lateinit var ivNewHighest: ImageView
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_over)
        ivNewHighest = findViewById(R.id.ivNewHeighest)
        tvPoints = findViewById(R.id.tvPoints)
        val points = intent.extras!!.getInt("points")
        if (points == 240) {
            ivNewHighest.setVisibility(View.VISIBLE)
        }
        tvPoints.text = "" + points
    }

    fun restart(view: View?) {
        val intent = Intent(this@GameOver, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun exit(view: View?) {
        finish()
    }
}
