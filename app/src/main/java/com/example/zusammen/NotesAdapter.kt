package com.example.zusammen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotesAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onNoteLongClick: (Note) -> Unit = { _ -> }, // Добавил значение по умолчанию
    private val onEditClick: (Note) -> Unit,
    private val onDeleteClick: (Note) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    private var notes = emptyList<Note>()

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tvNoteTitle)
        private val contentTextView: TextView = itemView.findViewById(R.id.tvNoteContent)
        private val dateTextView: TextView = itemView.findViewById(R.id.tvNoteDate)
        private val editButton: ImageView = itemView.findViewById(R.id.btnEditProfile)
        private val deleteButton: ImageView = itemView.findViewById(R.id.ibDeleteNote)

        fun bind(note: Note) {
            titleTextView.text = note.title
            contentTextView.text = note.content
            dateTextView.text = formatDate(note.createdAt)

            itemView.setOnClickListener { onNoteClick(note) }
            itemView.setOnLongClickListener {
                onNoteLongClick(note)
                true
            }
            editButton.setOnClickListener { onEditClick(note) }
            deleteButton.setOnClickListener { onDeleteClick(note) }
        }

        private fun formatDate(timestamp: Long): String {
            return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notes, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount() = notes.size

    fun submitList(newNotes: List<Note>) {
        val diffCallback = NotesDiffCallback(notes, newNotes)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        notes = newNotes
        diffResult.dispatchUpdatesTo(this)
    }
}

class NotesDiffCallback(
    private val oldList: List<Note>,
    private val newList: List<Note>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size
    override fun areItemsTheSame(oldPos: Int, newPos: Int) =
        oldList[oldPos].id == newList[newPos].id
    override fun areContentsTheSame(oldPos: Int, newPos: Int) =
        oldList[oldPos] == newList[newPos]
}