package com.carlos.photogallery.listeners

import com.carlos.photogallery.clases.DriveFile
import com.carlos.photogallery.clases.GoogleDriveService

interface ServiceListenerExport {
    fun onExportDrive(drive: GoogleDriveService, folder: DriveFile)
}