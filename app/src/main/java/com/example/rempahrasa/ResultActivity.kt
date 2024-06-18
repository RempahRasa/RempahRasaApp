package com.example.rempahrasa

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        val ivSpiceImage = findViewById<ImageView>(R.id.ivSpiceImage)
        val tvSpiceName = findViewById<TextView>(R.id.tvSpiceName)
        val tvSpiceDescription = findViewById<TextView>(R.id.tvSpiceDescription)
        val ivFavorite = findViewById<ImageView>(R.id.ivFavorite)
        val llRecipes = findViewById<LinearLayout>(R.id.llRecipes)
        val btnClose = findViewById<Button>(R.id.btnClose)


        val spiceName = intent.getStringExtra("spiceName")
        val recipes = intent.getStringArrayListExtra("recipes")
        val spiceImageRes = intent.getIntExtra("spiceImageRes", R.drawable.ic_launcher_background) // Replace with actual default image resource


        tvSpiceName.text = spiceName
        tvSpiceDescription.text = "Lorem ipsum" // Placeholder text, replace with actual description if available
        ivSpiceImage.setImageResource(spiceImageRes)

        recipes?.forEach { recipe ->
            val textView = TextView(this)
            textView.text = recipe
            textView.textSize = 16f
            llRecipes.addView(textView)
        }

        ivFavorite.setOnClickListener {
            // Handle favorite button click
        }

        btnClose.setOnClickListener {
            finish()
        }
    }
}
