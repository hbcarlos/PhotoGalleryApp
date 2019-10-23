package com.carlos.photogallery.clases

import android.net.Uri


class Sticker(uri: Uri, name: String) {
    var uri: Uri
    var name: String
    var size: Float
    var position: Pair<Float, Float>

    init {
        this.uri = uri
        this.name = name
        this.size = 1f
        this.position = Pair(0f, 0f)
    }
}