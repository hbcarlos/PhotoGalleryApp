package com.carlos.photogallery.listeners

import com.carlos.photogallery.clases.AsyncTaskPG

interface ServiceListenerAsyncTask {
    fun onCancelTask(task: AsyncTaskPG)
}