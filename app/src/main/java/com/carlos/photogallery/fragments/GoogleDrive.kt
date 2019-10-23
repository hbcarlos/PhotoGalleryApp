package com.carlos.photogallery.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

import com.carlos.photogallery.clases.*
import com.carlos.photogallery.dialogs.DialogDriveFiles
import com.google.android.gms.common.api.ApiException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException

import kotlinx.android.synthetic.main.fragment_googledrive.*
import xdroid.toaster.Toaster
import java.io.IOException
import android.widget.AdapterView
import com.carlos.photogallery.R
import com.carlos.photogallery.listeners.ServiceListenerDialogDriveFile
import com.carlos.photogallery.listeners.ServiceListenerDrive
import com.carlos.photogallery.listeners.ServiceListenerExport
import com.facebook.drawee.view.SimpleDraweeView


class GoogleDrive(private val drive: GoogleDriveService, private val listener: ServiceListenerExport)
    : Fragment(), ServiceListenerDrive, ServiceListenerDialogDriveFile {

    var root: String = "root"
    lateinit var act: DriveFile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        drive.listener = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_googledrive, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadSettings()

        drive.checkLoggedInStatus(this.context!!)

        driveUser.setOnClickListener {
            if (drive.loggeado) {
                drive.requestSignOut()
            } else {
                startActivityForResult(drive.requestSignIn(this.context!!)!!.signInIntent, REQUEST_SIGN_IN)
            }
        }

        btnRutaDrive.setOnClickListener {
            if (drive.loggeado) drive.listFolders()
            else Toaster.toast("No estas loggeado")
        }

        btnShareDrive.setOnClickListener {
            if (drive.loggeado) drive.shareFolder(act.id)
            else Toaster.toast("No estas loggeado")
        }

        btnDeleteDrive.setOnClickListener {
            if (drive.loggeado) drive.deleteFolder(act)
            else Toaster.toast("No estas loggeado")
        }

        btnEnlaceDrive.setOnClickListener {
            if (drive.loggeado && act.webViewLink != null) {
                val clipboard = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("Link copiado al portapapeles", act.webViewLink)
                clipboard.primaryClip = clip
                Toaster.toast("Link copiado al portapapeles")

            } else if (act.webViewLink == null) Toaster.toast("No estas loggeado")
            else Toaster.toast("La carpeta no está compartida")
        }

        btn_export.setOnClickListener {
            if (drive.loggeado) listener.onExportDrive(drive, act)
            else Toaster.toast("No estas loggeado")
        }
    }

    override fun onPause() {
        super.onPause()
        saveSettings()
    }

    private fun loggedOut(){
        sesionDrive.text = getString(R.string.iniciar_sesion)
        emailDrive.text = getString(R.string.email)
        (imageDrive as SimpleDraweeView).setActualImageResource(R.drawable.googledrive)

        txtRutaDrive.setText(R.string.ruta)
        txtEnlaceDrive.setText(R.string.enlace)

        act = DriveFile("", "", "application/vnd.google-apps.folder")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        drive.onActivityResult(requestCode, resultCode, data, this.context!!)
    }

    override fun onCheckLoggedInStatus(loggeado: Boolean) {
        if (loggeado) {
            sesionDrive.text = getString(R.string.cerrar_sesion)
            emailDrive.text = drive!!.email
            imageDrive.setImageURI(drive!!.photo)

            txtRutaDrive.setText(R.string.ruta)
            txtEnlaceDrive.setText(R.string.enlace)

            drive.getRoot()
            drive.getFolder(act.id)
        } else {
            sesionDrive.text = getString(R.string.iniciar_sesion)
            emailDrive.text = getString(R.string.email)
            (imageDrive as SimpleDraweeView).setActualImageResource(R.drawable.googledrive)

            txtRutaDrive.setText(R.string.ruta)
            txtEnlaceDrive.setText(R.string.enlace)

            act = DriveFile("", "", "application/vnd.google-apps.folder")
        }
    }

    override fun onLoggedIn(){
        sesionDrive.text = getString(R.string.cerrar_sesion)
        emailDrive.text = drive!!.email
        imageDrive.setImageURI(drive!!.photo)

        drive.getRoot()
    }

    override fun onLoggedOut(){
        sesionDrive.text = getString(R.string.iniciar_sesion)
        emailDrive.text = getString(R.string.email)
        (imageDrive as SimpleDraweeView).setActualImageResource(R.drawable.googledrive)

        txtRutaDrive.setText(R.string.ruta)
        txtEnlaceDrive.setText(R.string.enlace)

        act = DriveFile("", "", "application/vnd.google-apps.folder")
    }

    override fun onGetRoot(root: DriveFile) {
        this.root = root.id
    }

    override fun onGetFolder(folder: DriveFile) {
        act = folder

        txtRutaDrive.setText(act.name)
        txtEnlaceDrive.setText(act.webViewLink)
    }

    override fun onShareFolder(folder:DriveFile) {
        act = folder

        txtRutaDrive.setText(act.name)
        txtEnlaceDrive.setText(act.webViewLink)
        Toaster.toast("Compartida")
    }

    override fun onGetLink(folder: DriveFile) {}

    override fun onCreateFolder(folder: DriveFile) {
        act = folder

        txtRutaDrive.setText(act.name)
        txtEnlaceDrive.setText(act.webViewLink)
        Toaster.toast("Carpeta creada")
    }

    override fun onDeleteFolder(folder: DriveFile) {
        act = folder

        txtRutaDrive.setText(act.name)
        txtEnlaceDrive.setText(act.webViewLink)
        Toaster.toast("Carpeta eliminada")
    }

    override fun onUploadImage(image: DriveFile) {}

    override fun onListFolders(folders: ArrayList<DriveFile>){
        Log.e("onListFolders", "dialogo")
        var dialog = DialogDriveFiles(requireContext(), this, folders, root)
            .show(getFragmentManager()!!, "DriveDialog")

    }

    override fun onListFiles(files: ArrayList<DriveFile>) {
        Log.e("LISTFILES", "Size: ${files.size}, Root: ${id}")
        files.forEach { Log.e("LISTFILES", "Folder: id: ${it.id} name: ${it.name}, mimeType: ${it.mimeType}, parent: ${it.parent[0]}, iconLink: ${it.iconLink}") }
        var dialog = DialogDriveFiles(requireContext(), this, files, act.id).show(getFragmentManager()!!, "DriveDialog")
    }

    override fun onError(e: Exception, request: Int) {

        Log.e("ERROR DRIVE", "Codigo: $request \nExcepcion: $e \nMensage: ${e.stackTrace.toString()}")
        when (e) {
            is UserRecoverableAuthIOException -> { startActivityForResult(e.intent, REQUEST_SIGN_IN) }

            //is SecurityException -> { Toaster.toastLong(R.string.EXEPTION_PERMISSIONS) }

            //is IOException -> { Toaster.toastLong(R.string.EXEPTION_NETWORK) }

            //is ApiException -> { Toaster.toastLong(R.string.EXEPTION_NETWORK) }

            else -> {
                when (request) {
                    REQUEST_LOGGING_STATUS -> {
                        loggedOut()
                        Toaster.toastLong(R.string.REQUEST_LOGGING_STATUS)
                    }
                    REQUEST_SIGN_IN -> {
                        loggedOut()
                        Toaster.toastLong(R.string.REQUEST_SIGN_IN)
                    }
                    REQUEST_SIGN_OUT -> { Toaster.toastLong(R.string.REQUEST_SIGN_OUT) }
                    REQUEST_ROOT -> { Toaster.toastLong(R.string.REQUEST_ROOT) }
                    REQUEST_FOLDER -> {
                        txtRutaDrive.setText(R.string.ruta)
                        txtEnlaceDrive.setText(R.string.enlace)

                        act = DriveFile("", "", "application/vnd.google-apps.folder")
                        Toaster.toastLong(R.string.REQUEST_FOLDER)
                    }
                    REQUEST_SHARE_FOLDER -> {
                        txtEnlaceDrive.setText(R.string.enlace)
                        Toaster.toastLong(R.string.REQUEST_SHARE_FOLDER)
                    }
                    REQUEST_GET_LINK -> {
                        txtEnlaceDrive.setText(R.string.enlace)
                        Toaster.toastLong(R.string.REQUEST_GET_LINK)
                    }
                    REQUEST_CREATE_FOLDER -> {
                        txtRutaDrive.setText(R.string.ruta)
                        txtEnlaceDrive.setText(R.string.enlace)

                        act = DriveFile("", "", "application/vnd.google-apps.folder")
                        Toaster.toastLong(R.string.REQUEST_CREATE_FOLDER)
                    }
                    REQUEST_DELETE_FOLDER -> { Toaster.toastLong(R.string.REQUEST_DELETE_FOLDER) }
                    REQUEST_UPLOAD_IMAGE -> { Toaster.toastLong(R.string.REQUEST_UPLOAD_IMAGE) }
                    REQUEST_LIST_FOLDERS -> { Toaster.toastLong(R.string.REQUEST_LIST_FOLDERS) }
                    REQUEST_LIST_FILES -> { Toaster.toastLong(R.string.REQUEST_LIST_FILES) }
                }
            }
        }
    }

    override fun onNewFolder(folder: DriveFile) {
        drive.createFolder( folder )
    }

    override fun onAccept(folder: DriveFile) {
        drive.getFolder(folder.id)
    }

    fun loadSettings(){
        val prefs = context!!.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)
        var id = prefs.getString("driveId", "root").toString()
        var name = prefs.getString("txtNameDrive", "My Drive").toString()
        var webViewLink = prefs.getString("linkDrive", "enlace").toString()

        txtRutaDrive.setText(name)
        txtEnlaceDrive.setText(webViewLink)

        act = DriveFile(id, name, "application/vnd.google-apps.folder")
    }

    fun saveSettings(){
        val prefs = context!!.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("driveId", act.id)
        editor.putString("txtNameDrive", act.name)
        editor.putString("linkDrive", act.webViewLink)
        editor.commit()
    }
}