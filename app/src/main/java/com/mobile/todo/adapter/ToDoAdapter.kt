package com.mobile.todo.adapter

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.mobile.todo.EditTodoHabit
import com.mobile.todo.R
import com.mobile.todo.database.AppDatabase
import com.mobile.todo.database.dataset.Search
import com.mobile.todo.database.dataset.Tag
import com.mobile.todo.database.dataset.ToDo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ToDoAdapter(private var itemList: MutableList<ToDo>, private var searchList: MutableList<Search>) :
    RecyclerView.Adapter<ToDoAdapter.ViewHolder>() {

    private lateinit var context: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent // Store the reference to the parent ViewGroup
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.textView.text = item.title
        if (item.completed) {
            holder.checkbox.isChecked = true
            holder.textView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }

        val result = searchList.any { search ->
            search.toDoId == item.id && search.tag == Tag.FAV
        }

        if (result) {
            holder.starCheckBoxItem.isChecked = true
        }

        holder.delete.setOnClickListener {
            AlertDialog.Builder(context.context)
                .setTitle("Delete to do")
                .setMessage("Do you want to delete this to do?")
                .setPositiveButton("OK") { dialog, _ ->
                    // Remove to do from database
                    GlobalScope.launch {
                        AppDatabase.getDatabase(context.context).toDoDao().deleteToDo(item)
                    }
                    val currentPosition = itemList.indexOf(item)
                    if (currentPosition != -1) {
                        itemList.removeAt(currentPosition)
                        notifyItemRemoved(currentPosition)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        holder.textView.setOnClickListener {
            startActivity(
                context.context,
                Intent(
                    EditTodoHabit.newInstance(
                        context.context, EditTodoHabit.Companion.TYPE.TODO, item.id
                    )
                ), null
            )
        }

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                GlobalScope.launch {
                    AppDatabase.getDatabase(context.context).toDoDao()
                        .setCompleted(item.id, isChecked)
                }
                holder.textView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                GlobalScope.launch {
                    AppDatabase.getDatabase(context.context).toDoDao()
                        .setCompleted(item.id, isChecked)
                }
                holder.textView.paintFlags =
                    holder.textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }

        holder.starCheckBoxItem.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                GlobalScope.launch {
                    AppDatabase.getDatabase(context.context).searchDao()
                        .insertSearch(Search(item.id,  Tag.FAV))
                }
            } else {
                GlobalScope.launch {
                    AppDatabase.getDatabase(context.context).searchDao()
                        .removeTagFromToDoId(item.id, Tag.FAV)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)
        val textView: TextView = itemView.findViewById(R.id.textView)
        val delete: ImageView = itemView.findViewById(R.id.delete)
        val starCheckBoxItem: CheckBox = itemView.findViewById(R.id.starCheckBoxItem)
    }
}
