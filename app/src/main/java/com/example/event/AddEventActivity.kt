package com.example.event

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.event.databinding.ActivityAddEventBinding
import com.example.event.model.Event
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEventBinding
    private lateinit var database: DatabaseReference
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Atur toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Hilangkan judul

        // Atur warna icon back jadi putih
        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setTint(android.graphics.Color.WHITE)
        supportActionBar?.setHomeAsUpIndicator(upArrow)

        // 1. Inisialisasi Firebase
        database = FirebaseDatabase.getInstance().reference.child("events")

        // 2. Setup ProgressDialog
        progressDialog = ProgressDialog(this).apply {
            setTitle("Loading")
            setMessage("Menyimpan event...")
            setCancelable(false)
        }

        binding.btnSubmit.setOnClickListener {
            // 3. Validasi dengan feedback jelas
            if (!validateForm()) return@setOnClickListener

            progressDialog.show()
            saveEventToDatabase()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun validateForm(): Boolean {
        val url = binding.etPosterUrl.text.toString()

        return when {
            binding.etTitle.text.isNullOrEmpty() -> {
                binding.etTitle.error = "Judul wajib diisi"
                false
            }
            !isValidImageUrl(url) -> {
                binding.etPosterUrl.error = "URL gambar tidak valid (harus .jpg/.png)"
                false
            }
            else -> true
        }
    }

    private fun isValidImageUrl(url: String): Boolean {
        return Patterns.WEB_URL.matcher(url).matches() &&
                (url.endsWith(".jpg", ignoreCase = true) ||
                        url.endsWith(".jpeg", ignoreCase = true) ||
                        url.endsWith(".png", ignoreCase = true) ||
                        url.endsWith(".webp", ignoreCase = true))
    }

    private fun saveEventToDatabase() {
        val event = HashMap<String, Any>().apply {
            put("title", binding.etTitle.text.toString())
            put("description", binding.etDescription.text.toString())
            put("date", binding.etDate.text.toString())
            put("time", binding.etTime.text.toString())
            put("location", binding.etLocation.text.toString())
            put("posterUrl", binding.etPosterUrl.text.toString())
        }

        database.push().setValue(event)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Event berhasil dibuat!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener { task ->
                android.util.Log.d("FIREBASE_DEBUG", "Status: ${task.isSuccessful}")
                if (!task.isSuccessful) {
                    android.util.Log.e("FIREBASE_ERROR", "Error: ${task.exception?.message}")
                }
            }
    }
}