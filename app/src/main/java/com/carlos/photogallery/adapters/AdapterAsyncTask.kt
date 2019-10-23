package com.carlos.photogallery.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.carlos.photogallery.R
import com.carlos.photogallery.clases.AsyncTaskPG
import com.carlos.photogallery.listeners.ServiceListenerAsyncTask
import kotlinx.android.synthetic.main.list_view_item_task.view.*
import kotlin.concurrent.thread


class AdapterAsyncTask (private val windowsContext: Context, private val tasks: ArrayList<AsyncTaskPG>,
                        private val listener: ServiceListenerAsyncTask) : BaseAdapter() {

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val aux = windowsContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val inflatedView = LayoutInflater.from(windowsContext)
        val view = inflatedView.inflate(R.layout.list_view_item_task, parent, false)

        val task = getItem(position)

        view.txt_name_task.setText(task.name)

        view.txt_progress_task.setText("${task.progress}/${task.total}")
        view.pb_task.max = task.total
        view.pb_task.progress = task.progress

        view.btn_cancel.setOnClickListener{ listener.onCancelTask(task) }

        return view
    }

    override fun getItem(position: Int) = tasks[position]
    override fun getItemId(position: Int) = position.toLong()
    override fun getCount() = tasks.size
}