package com.example.event

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

        // Atur toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Hilangkan judul

        // Atur warna icon back jadi putih
        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_back)
        upArrow?.setTint(android.graphics.Color.WHITE)
        supportActionBar?.setHomeAsUpIndicator(upArrow)

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
                        binding.tvEventDate.text = it.date
                        binding.tvEventTime.text = it.time
                        binding.tvEventLocation.text = it.location

                        Glide.with(this@EventDetailActivity)
                            .load(it.posterUrl)
                            .into(binding.ivEventPoster)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EventDetailActivity, "Gagal memuat detail event", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun showRegistrationSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Registrasi Berhasil")
            .setMessage("Kamu berhasil mendaftar pada event ini!")
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
                            .setTitle("Unduhan Poster Event")
                            .setDescription("Mengunduh")
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS,
                                "poster_event_$eventId.jpg"
                            )

                        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                        downloadManager.enqueue(request)
                        Toast.makeText(this@EventDetailActivity, "Unduhan dimulai", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EventDetailActivity, "Gagal mendapatkan URL poster", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Menu titik tiga
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_event_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_edit -> {
                // Pindah ke EditEventActivity
                val intent = Intent(this, EditEventActivity::class.java)
                intent.putExtra("EVENT_ID", eventId)
                startActivity(intent)
                true
            }
            R.id.action_delete -> {
                // Konfirmasi sebelum hapus
                AlertDialog.Builder(this)
                    .setTitle("Hapus Event")
                    .setMessage("Yakin ingin menghapus event ini?")
                    .setPositiveButton("Ya") { _, _ -> deleteEvents() }
                    .setNegativeButton("Batal", null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteEvents() {
        database.getReference("events/$eventId").removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Event berhasil dihapus", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menghapus event", Toast.LENGTH_SHORT).show()
            }
    }
}