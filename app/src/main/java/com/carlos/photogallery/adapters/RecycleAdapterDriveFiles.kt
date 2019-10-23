package com.carlos.photogallery.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.carlos.photogallery.R
import com.carlos.photogallery.clases.DriveFile
import com.carlos.photogallery.listeners.ServiceListenerDriveFile
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.recycler_view_item_dialog.view.*

class RecycleAdapterDriveFiles(private val listener: ServiceListenerDriveFile, private val files: ArrayList<DriveFile>)
    : RecyclerView.Adapter<RecycleAdapterDriveFiles.FileHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileHolder {
        val inflatedView = LayoutInflater.from(parent.context)
        return  FileHolder(inflatedView.inflate(R.layout.recycler_view_item_dialog, parent, false), listener)
    }

    override fun getItemCount() = files.size

    override fun onBindViewHolder(holder: FileHolder, position: Int) {
        val file = files[position]
        holder.bindFile(file)
    }

    class FileHolder(private val view: View, private val listener: ServiceListenerDriveFile)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private var file: DriveFile? = null

        init { view.setOnClickListener(this) }

        override fun onClick(v: View) {
            listener.onClick(file!!.id)
        }

        fun bindFile(file: DriveFile) {
            this.file = file

            view.txtName.text = file.name
            if (file.iconLink != null) {
                val uri = Uri.parse(file.iconLink)
                view.itemImage.setImageURI(uri)
            } else {
                (view.itemImage as SimpleDraweeView).setActualImageResource(R.drawable.googledrive)
            }
        }
    }
}