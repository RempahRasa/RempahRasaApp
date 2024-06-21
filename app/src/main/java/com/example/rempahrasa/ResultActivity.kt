package com.example.rempahrasa

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.rempahrasa.databinding.ActivityResultsBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Assume receive the classification result via intent
        val spiceName = intent.getStringExtra("spiceName") ?: "Unknown Spice"
        val spiceImageRes = intent.getIntExtra("spiceImageRes", 0)
        val recipes = intent.getStringArrayListExtra("recipes") ?: arrayListOf()

        binding.tvSpiceName.text = spiceName

        if (spiceImageRes != 0) {
            binding.ivSpiceImage.setImageResource(spiceImageRes)
        }

        // Display recipes
        binding.llRecipes.removeAllViews()
        recipes.forEach { recipe ->
            val recipeTextView = TextView(this).apply {
                text = recipe
                textSize = 16f
                setPadding(8, 8, 8, 8)
            }
            binding.llRecipes.addView(recipeTextView)
        }

        binding.btnClose.setOnClickListener {
            finish()
        }

        // Set favorite button action
        binding.ivFavorite.setOnClickListener {
            // Implement favorite save functionality
        }
    }
}
