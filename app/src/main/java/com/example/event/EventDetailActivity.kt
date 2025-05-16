package com.example.event

import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.event.databinding.ActivityEventDetailBinding
import com.example.event.model.Event
import com.google.firebase.database.*

class EventDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventDetailBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var eventId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        eventId = intent.getStringExtra("EVENT_ID") ?: ""

        loadEventDetails()

        binding.btnRegister.setOnClickListener {
            showRegistrationSuccessDialog()
        }

        binding.btnDownloadPoster.setOnClickListener {
            downloadPoster()
        }
    }

    private fun loadEventDetails() {
        database.getReference("events/$eventId").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val event = snapshot.getValue(Event::class.java)
                    event?.let {
                        binding.tvEventTitle.text = it.title
                        binding.tvEventDescription.text = it.description
                        binding.tvEventDateTime.text = "${it.date} at ${it.time}"
                        binding.tvEventLocation.text = it.location

                        Glide.with(this@EventDetailActivity)
                            .load(it.posterUrl)
                            .into(binding.ivEventPoster)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EventDetailActivity, "Failed to load event details", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun showRegistrationSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Registration Successful")
            .setMessage("You have successfully registered for this event!")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun downloadPoster() {
        database.getReference("events/$eventId/posterUrl").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val posterUrl = snapshot.getValue(String::class.java)
                    posterUrl?.let {
                        val request = DownloadManager.Request(Uri.parse(it))
                            .setTitle("Event Poster Download")
                            .setDescription("Downloading")
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "event_poster_$eventId.jpg")

                        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                        downloadManager.enqueue(request)
                        Toast.makeText(this@EventDetailActivity, "Download started", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EventDetailActivity, "Failed to get poster URL", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}