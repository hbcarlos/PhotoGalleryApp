package com.carlos.photogallery.listeners

import com.carlos.photogallery.clases.DriveFile

interface ServiceListenerDrive {
    fun onCheckLoggedInStatus(loggeado: Boolean)
    fun onLoggedIn()
    fun onLoggedOut()

    fun onGetRoot(root: DriveFile)
    fun onGetFolder(folder: DriveFile)
    fun onShareFolder(folder: DriveFile)
    fun onGetLink(folder: DriveFile)

    fun onCreateFolder(folder: DriveFile)
    fun onDeleteFolder(folder: DriveFile)
    fun onUploadImage(image: DriveFile)

    fun onListFolders(folders: ArrayList<DriveFile>)
    fun onListFiles(files: ArrayList<DriveFile>)

    fun onError(e: Exception, request: Int)
}