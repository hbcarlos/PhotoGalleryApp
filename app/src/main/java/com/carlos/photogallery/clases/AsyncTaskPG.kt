package com.carlos.photogallery.clases

class AsyncTaskPG(name: String, total: Int) {
    var name: String
    var total: Int
    var progress: Int
    var canceled: Boolean

    init {
        this.name = name
        this.total = total
        this.progress = 0
        this.canceled = false
    }
}