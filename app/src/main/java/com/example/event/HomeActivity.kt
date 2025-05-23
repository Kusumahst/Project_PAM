package com.example.event

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.event.adapter.EventAdapter
import com.example.event.databinding.ActivityHomeBinding
import com.example.event.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var eventAdapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupRecyclerView()
        loadEvents()

        binding.fabAddEvent.setOnClickListener {
            startActivity(Intent(this, AddEventActivity::class.java))
        }

        binding.btnProfile.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter { event ->
            val intent = Intent(this, EventDetailActivity::class.java)
            intent.putExtra("EVENT_ID", event.id)
            startActivity(intent)
        }

        binding.rvEvents.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = eventAdapter
        }
    }

    private fun loadEvents() {
        database.getReference("events").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = mutableListOf<Event>()
                for (eventSnapshot in snapshot.children) {
                    val event = eventSnapshot.getValue(Event::class.java)?.copy(id = eventSnapshot.key ?: "")
                    event?.let { events.add(it) }
                }
                eventAdapter.submitList(events)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Failed to load events", Toast.LENGTH_SHORT).show()
            }
        })
    }
}