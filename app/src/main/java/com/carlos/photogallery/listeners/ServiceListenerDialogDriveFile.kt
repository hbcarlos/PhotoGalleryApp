package com.carlos.photogallery.listeners

import com.carlos.photogallery.clases.DriveFile

interface ServiceListenerDialogDriveFile {
    fun onNewFolder(folder: DriveFile)
    fun onAccept(folder: DriveFile)
}