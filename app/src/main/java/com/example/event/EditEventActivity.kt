package com.example.event

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.event.databinding.ActivityEditEventBinding
import com.google.firebase.database.FirebaseDatabase

class EditEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditEventBinding
    private lateinit var eventId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Atur toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Hilangkan judul

        // Atur warna icon back jadi putih
        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setTint(android.graphics.Color.WHITE)
        supportActionBar?.setHomeAsUpIndicator(upArrow)

        eventId = intent.getStringExtra("EVENT_ID") ?: ""

        // Memuat data event yang sudah ada
        loadEventDetails()

        // Tombol untuk menyimpan perubahan
        binding.btnUpdateEvent.setOnClickListener {
            updateEvent()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadEventDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("events").child(eventId)
        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val title = snapshot.child("title").getValue(String::class.java) ?: ""
                val description = snapshot.child("description").getValue(String::class.java) ?: ""
                val date = snapshot.child("date").getValue(String::class.java) ?: ""
                val time = snapshot.child("time").getValue(String::class.java) ?: ""
                val location = snapshot.child("location").getValue(String::class.java) ?: ""
                val posterUrl = snapshot.child("posterUrl").getValue(String::class.java) ?: ""

                binding.etTitle.setText(title)
                binding.etDescription.setText(description)
                binding.etDate.setText(date)
                binding.etTime.setText(time)
                binding.etLocation.setText(location)
                binding.etPosterUrl.setText(posterUrl)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal memuat data event", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEvent() {
        val updatedData = mapOf(
            "title" to binding.etTitle.text.toString(),
            "description" to binding.etDescription.text.toString(),
            "date" to binding.etDate.text.toString(),
            "time" to binding.etTime.text.toString(),
            "location" to binding.etLocation.text.toString(),
            "posterUrl" to binding.etPosterUrl.text.toString()
        )

        val ref = FirebaseDatabase.getInstance().getReference("events").child(eventId)
        ref.updateChildren(updatedData).addOnSuccessListener {
            Toast.makeText(this, "Event berhasil diperbarui", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal memperbarui event", Toast.LENGTH_SHORT).show()
        }
    }
}
