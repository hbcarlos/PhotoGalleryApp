package com.carlos.photogallery.clases

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.carlos.photogallery.R
import com.carlos.photogallery.listeners.ServiceListenerDrive
import com.carlos.photogallery.listeners.ServiceListenerDriveSend

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.Permission
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import xdroid.toaster.Toaster
import java.io.IOException
import kotlin.collections.ArrayList
import com.google.api.client.http.FileContent



const val REQUEST_LOGGING_STATUS = 1
const val REQUEST_SIGN_IN = 2
const val REQUEST_SIGN_OUT = 3
const val REQUEST_ROOT = 4
const val REQUEST_FOLDER = 5
const val REQUEST_SHARE_FOLDER = 6
const val REQUEST_GET_LINK = 7
const val REQUEST_CREATE_FOLDER = 8
const val REQUEST_DELETE_FOLDER = 9
const val REQUEST_UPLOAD_IMAGE = 10
const val REQUEST_LIST_FOLDERS = 11
const val REQUEST_LIST_FILES = 12

class GoogleDriveService(private val listenerSender: ServiceListenerDriveSend) {
    var listener: ServiceListenerDrive? = null

    private var account: GoogleSignInAccount? = null
    private var client: GoogleSignInClient? = null
    private var drive: Drive? = null

    var root: DriveFile? = null
    var folder: DriveFile? = null
    var email: String? = null
    var photo: Uri? = null
    var loggeado = false


    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, context: Context) {
        when (requestCode) {
            REQUEST_SIGN_IN -> {
                GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener { googleAccount ->
                        account = googleAccount
                        var credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE_FILE))
                        credential.selectedAccount = account!!.account

                        drive = Drive.Builder(AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), credential)
                            .setApplicationName("PhotoGallery")
                            .build()

                        root = DriveFile("root", "Mi unidad", "application/vnd.google-apps.folder", ArrayList<String>())
                        folder = root

                        email = account!!.email
                        photo = account!!.photoUrl
                        loggeado = true

                        listener!!.onLoggedIn()
                    }
                    .addOnFailureListener { e ->
                        listener!!.onError(e, REQUEST_SIGN_IN)
                    }
            }
        }
    }

    fun checkLoggedInStatus(context: Context) {
        try {
            val requiredScopes = HashSet<Scope>(2)
            requiredScopes.add(Scope(DriveScopes.DRIVE_APPDATA))
            requiredScopes.add(Scope(DriveScopes.DRIVE))

            account = GoogleSignIn.getLastSignedInAccount(context)
            val containsScope = account?.grantedScopes?.containsAll(requiredScopes)

            if (account != null && containsScope == true) {
                val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
                    .requestScopes(Scope(DriveScopes.DRIVE))
                    .build()

                client = GoogleSignIn.getClient(context, signInOptions)

                var credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE_FILE))
                credential.selectedAccount = account!!.account

                drive = Drive.Builder(AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), credential)
                    .setApplicationName("PhotoGallery")
                    .build()

                root = DriveFile("root", "Mi unidad", "application/vnd.google-apps.folder", ArrayList<String>())
                folder = root

                email = account!!.email
                photo = account!!.photoUrl
                loggeado = true

            } else {
                account = null
                client = null
                drive = null

                root = null
                folder = null
                email = null
                photo = null
                loggeado = false
            }

            listener!!.onCheckLoggedInStatus(loggeado)

        } catch (e: Exception) {
            listener!!.onError(e, REQUEST_LOGGING_STATUS)
        }
    }

    fun requestSignIn(context: Context): GoogleSignInClient? {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .requestScopes(Scope(DriveScopes.DRIVE))
            .build()

        client = GoogleSignIn.getClient(context, signInOptions)
        return client
    }

    fun requestSignOut(){
        try {
            client!!.signOut()

            account = null
            client = null
            drive = null

            root = null
            folder = null
            email = null
            photo = null
            loggeado = false
            listener!!.onLoggedOut()

        } catch (e: Exception) {
            listener!!.onError(e, REQUEST_SIGN_OUT)
        }
    }

    fun getRoot(){
        doAsync{
            try {
                val result = drive!!.files().get("root").execute()

                root = DriveFile(result.id, result.name, "application/vnd.google-apps.folder")
                folder = root

                uiThread { listener!!.onGetRoot(root!!) }

            } catch (e: Exception) { listener!!.onError(e, REQUEST_ROOT) }
        }
    }

    fun getFolder(id: String){
        doAsync {
            try {
                val result = drive!!.files().get(id)
                    .setFields("id, name, mimeType, parents, iconLink, webViewLink, shared, trashed")
                    .execute()

                if (result.trashed) uiThread { listener!!.onError(Exception(), REQUEST_FOLDER) }
                if (result.shared && result.parents != null) folder = DriveFile(result.id, result.name, result.mimeType, result.parents, result.iconLink, result.webViewLink)
                else if (result.parents != null) folder = DriveFile(result.id, result.name, result.mimeType, result.parents, result.iconLink)
                else folder = DriveFile(result.id, result.name, result.mimeType)

                uiThread { listener!!.onGetFolder(folder!!) }

            } catch (e: Exception) { listener!!.onError(e, REQUEST_FOLDER) }
        }
    }

    fun shareFolder(id: String){
        doAsync {
            try {
                var domainPermission = Permission()
                    .setType("anyone")
                    .setRole("reader")

                drive!!.permissions().create(id, domainPermission).execute()

                val result = drive!!.files().get(id)
                    .setFields("id, name, mimeType, parents, iconLink, webViewLink")
                    .execute()

                folder = DriveFile(result.id, result.name, result.mimeType, result.parents, result.iconLink, result.webViewLink)

                uiThread { listener!!.onShareFolder(folder!!) }

            } catch (e: Exception) { listener!!.onError(e, REQUEST_SHARE_FOLDER) }
        }

    }

    fun getLink(id: String) {
        doAsync {
            try {
                val result = drive!!.files().get(id)
                    .setFields("id, name, mimeType, parents, iconLink, webViewLink")
                    .execute()

                folder = DriveFile(result.id, result.name, result.mimeType, result.parents, result.iconLink, result.webViewLink)

                uiThread { listener!!.onGetLink(folder!!) }

            } catch (e: Exception) { listener!!.onError(e, REQUEST_GET_LINK) }
        }
    }

    fun createFolder(folder: DriveFile){
        doAsync {
            try {
                var aux: DriveFile? = null

                var file: File = File()
                    .setName(folder.name)
                    .setParents(folder.parent)
                    .setMimeType("application/vnd.google-apps.folder")

                var res = drive!!.Files().create(file)
                    .setFields("id")
                    .execute()

                val result = drive!!.files().get(res.id)
                    .setFields("id, name, mimeType, parents, iconLink, webViewLink, shared")
                    .execute()

                if (result.shared && result.parents != null) aux = DriveFile(result.id, result.name, result.mimeType, result.parents, result.iconLink, result.webViewLink)
                else if (result.parents != null) aux = DriveFile(result.id, result.name, result.mimeType, result.parents, result.iconLink)
                else aux = DriveFile(result.id, result.name, result.mimeType)

                uiThread { listener!!.onCreateFolder(aux!!) }

            } catch (e: Exception) { listener!!.onError(e, REQUEST_CREATE_FOLDER) }
        }
    }

    fun deleteFolder(folder: DriveFile){
        doAsync {
            try {
                var res: DriveFile? = null
                drive!!.Files().delete(folder.id).execute()

                val result = drive!!.files().get(folder.parent[0])
                    .setFields("id, name, mimeType, parents, iconLink, webViewLink, shared")
                    .execute()

                if (result.shared && result.parents != null) res = DriveFile(result.id, result.name, result.mimeType, result.parents, result.iconLink, result.webViewLink)
                else if (result.parents != null) res = DriveFile(result.id, result.name, result.mimeType, result.parents, result.iconLink)
                else res = DriveFile(result.id, result.name, result.mimeType)

                uiThread { listener!!.onDeleteFolder(res!!) }

            } catch (e: Exception) { listener!!.onError(e, REQUEST_DELETE_FOLDER) }
        }
    }

    fun uploadImage(name:String, ext:String, uri: Uri, parentId:String): String {
        try {
            var file: File = File()
                .setName(name)
                .setParents(List(1){parentId})
                .setMimeType("image/$ext")

            val mediaContent = FileContent("image/$ext", java.io.File(uri.path))

            var result = drive!!.Files().create(file, mediaContent)
                .setFields("id")
                .execute()

            return result.id

        } catch (e: UserRecoverableAuthIOException) {
            Toaster.toastLong(R.string.EXEPTION_SESSION)
            return ""
        } catch (e: SecurityException) {
            Toaster.toastLong(R.string.EXEPTION_PERMISSIONS)
            return ""
        } catch (e: IOException) {
            Toaster.toastLong(R.string.EXEPTION_NETWORK)
            return ""
        } catch (e: ApiException) {
            Toaster.toastLong(R.string.EXEPTION_NETWORK)
            return ""
        } catch (e: Exception) {
            Toaster.toastLong(R.string.EXEPTION)
            return ""
        }
    }

    fun listFolders() {
        doAsync {
            try {
                var res = ArrayList<DriveFile>()
                var pageToken: String? = null
                do {
                    val result = drive!!.files().list()
                        .setQ("mimeType='application/vnd.google-apps.folder' and trashed=false")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, mimeType, parents, iconLink, webViewLink, shared)")
                        .setPageToken(pageToken)
                        .execute()

                    result.files.forEach {
                        if (it.shared) res.add(DriveFile(it.id, it.name, it.mimeType, it.parents, it.iconLink, it.webViewLink))
                        else res.add(DriveFile(it.id, it.name, it.mimeType, it.parents, it.iconLink))
                    }
                    pageToken = result.getNextPageToken()
                } while (pageToken != null)

                uiThread { listener!!.onListFolders(res) }

            } catch (e: Exception) { listener!!.onError(e, REQUEST_LIST_FOLDERS) }
        }
    }

    fun listFiles(id:String){
        doAsync {
            try {
                var res = ArrayList<DriveFile>()
                var pageToken: String? = null
                do {
                    val result = drive!!.files().list()
                        .setQ("mimeType!='application/vnd.google-apps.folder' and trashed=false and '$id' in parents")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, mimeType, parents, iconLink)")
                        .setPageToken(pageToken)
                        .execute()

                    result.files.forEach {
                        res.add(DriveFile(it.id, it.name, it.mimeType, it.parents, it.iconLink))
                    }
                    pageToken = result.getNextPageToken()
                } while (pageToken != null)

                uiThread { listener!!.onListFiles(res) }

            } catch (e: Exception) { listener!!.onError(e, REQUEST_LIST_FILES) }
        }
    }
}