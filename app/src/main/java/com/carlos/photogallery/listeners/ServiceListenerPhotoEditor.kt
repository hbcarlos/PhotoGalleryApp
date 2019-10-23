package com.carlos.photogallery.listeners

import com.carlos.photogallery.clases.Photo
import com.carlos.photogallery.clases.Resoluciones
import com.carlos.photogallery.clases.Sticker

interface ServiceListenerPhotoEditor {
    fun onAccept(photo: Photo, position: Int)
    fun onCancel()
    fun onEditAll(resolucion: Resoluciones, brightness: Int, contrast: Int, saturation: Int, hue: Int, size: Float, pos: Pair<Float, Float>, center: Pair<Int, Int>, marca_agua: Sticker?, propaganda: Sticker?)
}