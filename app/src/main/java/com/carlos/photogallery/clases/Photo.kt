package com.carlos.photogallery.clases

import android.graphics.*
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log

class Photo(u:Uri, f:String, c:Boolean) {
    var uri: Uri
    var original: Uri
    var selected: Boolean

    var name: String
    var extension: String
    var folder: String

    var resolucion = Resoluciones.R_ORIGINAL
    var brightness = 0
    var contrast = 0
    var saturation = 0
    var hue = 0
    var size = 1f
    var pos = Pair(0f, 0f)
    var resolution = Pair(1, 1)

    var marca_agua: Sticker? = null
    var propaganda: Sticker? = null

    init {
        this.uri = u
        this.original = u
        this.selected = c

        var aux = u.path.split("/", ignoreCase=true)
        aux = aux[aux.size -1].split(".", ignoreCase=true)
        this.name = aux[0]
        this.extension = aux[aux.size-1]
        this.folder = f
    }

    fun copy(): Photo{
        var res = Photo(uri, folder, selected)
        res.editImage(resolucion, brightness, contrast, saturation, hue, size, pos, resolution, marca_agua, propaganda)
        return res
    }

    fun editImage(resolucion: Resoluciones, brightness: Int, contrast: Int, saturation: Int, hue: Int, size: Float,
                  pos: Pair<Float, Float>, resolution: Pair<Int, Int>, marca_agua: Sticker?, propaganda: Sticker?) {
        this.resolucion = resolucion
        this.brightness = brightness
        this.contrast = contrast
        this.saturation = saturation
        this.hue = hue
        this.size = size
        this.pos = pos
        this.resolution = resolution

        this.marca_agua = marca_agua
        this.propaganda = propaganda
    }

    fun createBitmap(): Bitmap {
        var foto = BitmapFactory.decodeFile(original.path).copy(Bitmap.Config.ARGB_8888, true)

        lateinit var bpc: Bitmap
        if (resolucion == Resoluciones.R_ORIGINAL) {
            bpc = Bitmap.createBitmap(foto.width, foto.height, Bitmap.Config.ARGB_8888)
        } else {
            bpc = Bitmap.createBitmap(resolucion.w, resolucion.h, Bitmap.Config.ARGB_8888)
        }

        bpc.eraseColor(Color.WHITE)

        val canvas = Canvas(bpc)
        val center_x = bpc.width / 2
        val center_y = bpc.height / 2

        val paint = Paint()
        paint.colorFilter = ColorFilterGenerator().adjustColor(brightness, contrast, saturation, hue)
        foto = Bitmap.createScaledBitmap(foto, (foto.width*size).toInt(), (foto.height*size).toInt(), true)

        var ancho = (resolution.second * bpc.width).toFloat() / bpc.height
        var alto = (resolution.first * bpc.height).toFloat() / bpc.width

        var dif_x = 0f
        var dif_y = 0f

        if (ancho > resolution.first) {
            ancho = resolution.first.toFloat()
            dif_x = 1f
            dif_y = resolution.second / alto
        } else {
            alto = resolution.second.toFloat()
            dif_x = resolution.first / ancho
            dif_y = 1f
        }

//        Log.e("createBitmap", "---------------------------------------------------")
//        Log.e("createBitmap", "Ancho: ${ancho}, Alto: ${alto}")
//        Log.e("createBitmap", "Ancho: ${resolution.first}, Alto: ${resolution.second}")
//        Log.e("createBitmap", "Dif_x: ${dif_x}, Dif_y: ${dif_y}")
//        Log.e("createBitmap", "---------------------------------------------------")

        val position_x = ((pos.first*bpc.width) / resolution.first) * dif_x + center_x - (foto.width / 2)
        val position_y = ((pos.second*bpc.height) / resolution.second) * dif_y + center_y - (foto.height / 2)
        canvas.drawBitmap(foto, position_x, position_y, paint)

        if (marca_agua != null) {
            var ma = BitmapFactory.decodeFile(marca_agua!!.uri.path).copy(Bitmap.Config.ARGB_8888, true)
            ma = Bitmap.createScaledBitmap(ma, (ma.width*marca_agua!!.size).toInt(), (ma.height*marca_agua!!.size).toInt(), true)
            val position_x = ((marca_agua!!.position.first*bpc.width) / resolution.first) * dif_x + center_x - (ma.width / 2)
            val position_y = ((marca_agua!!.position.second*bpc.height) / resolution.second) * dif_y + center_y - (ma.height / 2)
            canvas.drawBitmap(ma, position_x, position_y, null)
        }

        if (propaganda != null) {
            var p = BitmapFactory.decodeFile(propaganda!!.uri.path).copy(Bitmap.Config.ARGB_8888, true)
            p = Bitmap.createScaledBitmap(p, (p.width*propaganda!!.size).toInt(), (p.height*propaganda!!.size).toInt(), true)
            val position_x = ((propaganda!!.position.first*bpc.width) / resolution.first) * dif_x + center_x - (p.width / 2)
            val position_y = ((propaganda!!.position.second*bpc.height) / resolution.second) * dif_y + center_y - (p.height / 2)
            canvas.drawBitmap(p, position_x, position_y, null)
        }

        return bpc
    }
}