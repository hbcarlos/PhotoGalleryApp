package com.carlos.photogallery.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.carlos.photogallery.clases.Photo
import com.carlos.photogallery.R
import kotlinx.android.synthetic.main.recycler_view_item_gallery.view.*
import android.util.Log
import com.carlos.photogallery.listeners.ServiceListenerPhotos
import com.facebook.drawee.view.SimpleDraweeView


class RecyclerAdapterGallery (private val listener: ServiceListenerPhotos, private val width:Int, private val height:Int)
    : RecyclerView.Adapter<RecyclerAdapterGallery.PhotoHolder>() {

    private val photos: ArrayList<Photo> = ArrayList<Photo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
        val inflatedView = LayoutInflater.from(parent.context)
        return PhotoHolder(inflatedView.inflate(R.layout.recycler_view_item_gallery, parent, false))
    }

    override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
        val photo = photos[position]
        holder.bindPhoto(photo, listener, width, height)
    }

    override fun getItemCount() = photos.size

    fun addPhoto(photo: Photo){
        photos.add(photo)
        notifyDataSetChanged()
    }

    fun editPhoto(photo: Photo, position: Int){
        photos[position] = photo
    }

    fun deletePhoto(position: Int){
        photos.removeAt(position)
        notifyDataSetChanged()
    }

    fun checkPhoto(position: Int, checked: Boolean){
        photos[position].selected = checked
        notifyDataSetChanged()
    }

    fun refresh() { notifyDataSetChanged() }

    class PhotoHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        private var view: View = v
        private lateinit var photo: Photo
        private lateinit var listener: ServiceListenerPhotos

        init { v.setOnClickListener(this) }

        override fun onClick(v: View) {
            listener.onCheck(adapterPosition)
        }

        fun bindPhoto(photo:Photo, listener: ServiceListenerPhotos, width:Int, height:Int) {
            this.photo = photo
            this.listener = listener

            view.selected.isChecked = photo.selected

            view.imagePhoto.layoutParams.width = width
            view.imagePhoto.layoutParams.height = width

            view.imagePhoto.setImageURI(photo.uri)

            view.btnEdit.setOnClickListener{
                listener.onEdit(adapterPosition)
            }
        }
    }
}