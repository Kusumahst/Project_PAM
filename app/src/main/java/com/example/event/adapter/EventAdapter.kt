package com.example.event.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.event.R
import com.example.event.model.Event
import com.example.event.databinding.ItemEventBinding
import java.text.SimpleDateFormat
import java.util.Locale

class EventAdapter(private val onItemClick: (Event) -> Unit) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private val events = mutableListOf<Event>()

    inner class EventViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.tvEventTitle.text = event.title
            binding.tvEventDate.text = "${formatTanggal(event.date)} - ${event.time} WIB"
            binding.tvEventLocation.text = event.location

            Glide.with(binding.root.context)
                .load(event.posterUrl)
                .placeholder(R.drawable.ic_placeholder)
                .into(binding.ivEventPoster)

            binding.root.setOnClickListener { onItemClick(event) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    fun submitList(newEvents: List<Event>) {
        events.clear()
        events.addAll(newEvents)
        notifyDataSetChanged()
    }

    private fun formatTanggal(tanggal: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatter = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            val date = parser.parse(tanggal)
            formatter.format(date!!)
        } catch (e: Exception) {
            tanggal
        }
    }
}
