package com.carlos.photogallery

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.carlos.photogallery.adapters.AdapterAsyncTask
import com.carlos.photogallery.adapters.RecyclerAdapterGallery
import com.carlos.photogallery.clases.*
import com.carlos.photogallery.fragments.*
import com.carlos.photogallery.listeners.*
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.common.io.CharStreams.copy
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import xdroid.toaster.Toaster
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import kotlinx.android.synthetic.main.dialog_async_task.view.*
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    ServiceListenerPhotos, ServiceListenerPhotoEditor, ServiceListenerAsyncTask, ServiceListenerExport, ServiceListenerDriveSend {

    private val drive: GoogleDriveService = GoogleDriveService(this)

    private val photos: ArrayList<Photo> = ArrayList<Photo>()
    private val new: HashSet<String> = HashSet()
    private var lock = ReentrantLock()
    private var selected: Int = 0

    private lateinit var dialog_async_task: View
    private val tasks: ArrayList<AsyncTaskPG> = ArrayList<AsyncTaskPG>()
    private val positions_tasks: HashMap<AsyncTaskPG, Int> = HashMap()
    private var lock_tasks = ReentrantLock()

    private var ruta_import: String = "./"
    private var ruta_export: String = "./"
    private var ruta_marca_agua: String = "./"
    private var ruta_propaganda: String = "./"

    private var lastSync: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

        Fresco.initialize(this)

        loadSettings()

        val dialog = BottomSheetDialog(this)
        dialog_async_task = layoutInflater.inflate(R.layout.dialog_async_task, null)
        dialog.setContentView(dialog_async_task)

        val adapter_async_task = AdapterAsyncTask(this, tasks, this)
        dialog_async_task.list_view_async_task.adapter = adapter_async_task

        btn_show_tasks.setOnClickListener { dialog.show() }
        btnEditSelected.setOnClickListener {
            if (selected != 0){
                recyclerViewGallery.visibility = View.GONE

                val transaction = supportFragmentManager.beginTransaction()
                supportFragmentManager.fragments.forEach { transaction.remove(it) }
                transaction.add(R.id.main, PhotoEditor(this, photos[0], -1), "PhotoEditor")
                transaction.addToBackStack(null)
                transaction.commit()

                btnCheck.isClickable = false
                btnEditSelected.isClickable = false
                btnDelete.isClickable = false
            } else Toaster.toast("No hay fotos seleccionadas")
        }
        btnDelete.setOnClickListener {
            if (selected != 0){
                deleteSelected()
            } else Toaster.toast("No hay fotos seleccionadas")
        }
        btn_add_photos.setOnClickListener {
            readDirectory()
            btnCheck.isClickable = false
            btnEditSelected.isClickable = false
            btnDelete.isClickable = false
        }
        btnCheck.setOnClickListener { checkAll(btnCheck.isChecked) }

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        recyclerViewGallery.layoutManager = GridLayoutManager(applicationContext, 2)
        recyclerViewGallery.adapter = RecyclerAdapterGallery(this, metrics.widthPixels/2, metrics.heightPixels/2)
    }

    override fun onBackPressed() {
//        loadSettings()
//        btnCheck.isClickable = true
//        btnEditSelected.isClickable = true
//        btnDelete.isClickable = true
//        btnUpload.isClickable = true
//
//        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
//            drawer_layout.closeDrawer(GravityCompat.START)
//        } else {
//            super.onBackPressed()
//        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_gallery -> {
                loadSettings()
                val transaction = supportFragmentManager.beginTransaction()
                supportFragmentManager.fragments.forEach { transaction.remove(it) }
                transaction.commit()

                recyclerViewGallery.visibility = View.VISIBLE
                btnCheck.isClickable = true
                btnEditSelected.isClickable = true
                btnDelete.isClickable = true
            }

            R.id.nav_web -> {
                recyclerViewGallery.visibility = View.GONE

                val transaction = supportFragmentManager.beginTransaction()
                supportFragmentManager.fragments.forEach { transaction.remove(it) }
                transaction.add(R.id.main, Web(), "WEB")
                transaction.addToBackStack(null)
                transaction.commit()

                btnCheck.isClickable = false
                btnEditSelected.isClickable = false
                btnDelete.isClickable = false
            }

            R.id.nav_drive -> {
                recyclerViewGallery.visibility = View.GONE

                val transaction = supportFragmentManager.beginTransaction()
                supportFragmentManager.fragments.forEach { transaction.remove(it) }
                transaction.add(R.id.main, GoogleDrive(drive, this), "DRIVE")
                transaction.addToBackStack(null)
                transaction.commit()

                btnCheck.isClickable = false
                btnEditSelected.isClickable = false
                btnDelete.isClickable = false
            }

            R.id.nav_facebook -> {
                recyclerViewGallery.visibility = View.GONE

                val transaction = supportFragmentManager.beginTransaction()
                supportFragmentManager.fragments.forEach { transaction.remove(it) }
                transaction.add(R.id.main, Facebook())
                transaction.addToBackStack(null)
                transaction.commit()

                btnCheck.isClickable = false
                btnEditSelected.isClickable = false
                btnDelete.isClickable = false
            }

            R.id.nav_instagram -> {
                recyclerViewGallery.visibility = View.GONE

                val transaction = supportFragmentManager.beginTransaction()
                supportFragmentManager.fragments.forEach { transaction.remove(it) }
                transaction.add(R.id.main, Instagram())
                transaction.addToBackStack(null)
                transaction.commit()

                btnCheck.isClickable = false
                btnEditSelected.isClickable = false
                btnDelete.isClickable = false
            }

            R.id.nav_settings -> {
                recyclerViewGallery.visibility = View.GONE

                val transaction = supportFragmentManager.beginTransaction()
                supportFragmentManager.fragments.forEach { transaction.remove(it) }
                transaction.add(R.id.main, Settings())
                transaction.addToBackStack(null)
                transaction.commit()

                btnCheck.isClickable = false
                btnEditSelected.isClickable = false
                btnDelete.isClickable = false
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun deleteSelected(){
        val dialog = MaterialDialog(this)
        dialog.title(R.string.titleDelete)
        dialog.message(R.string.messageDelete)
        dialog.negativeButton(R.string.disagree)
        dialog.positiveButton(R.string.agree) {
            var i = photos.size-1
            while (i >= 0){
                Log.e("deleteSelected", "i: $i")
                if (photos[i].selected){
                    Log.e("deleteSelected", "eliminada")
                    lock.lock()
                    photos.removeAt(i)
                    lock.unlock()
                    selected --
                    (recyclerViewGallery.adapter as RecyclerAdapterGallery).deletePhoto(i)
                }
                i--
            }
        }
        dialog.show()
    }

    fun checkAll(checked:Boolean){
        for (i in 0..photos.size-1) {
            lock.lock()
            photos[i].selected = checked
            lock.unlock()
            (recyclerViewGallery.adapter as RecyclerAdapterGallery).checkPhoto(i, checked)
        }
        selected = if (checked) photos.size else 0
    }

    fun readDirectory(){
        val thread_read = object: Thread() {
            var task: AsyncTaskPG? = null

            override fun run() {
                try {
                    val dir = File(ruta_import)
                    if (checkDirectory(dir)) {
                        var files: Array<out File>? = dir.listFiles()

                        runOnUiThread {
                            if (files != null) {
                                lock_tasks.lock()
                                task = AsyncTaskPG("Cargando fotos", files!!.size - new.size)
                                tasks.add(task!!)
                                positions_tasks[task!!] = tasks.size - 1
                                (dialog_async_task.list_view_async_task.adapter as AdapterAsyncTask).notifyDataSetChanged()
                                lock_tasks.unlock()
                            }
                        }

                        var i = 0
                        for (it in files!!) {
                            if (tasks[positions_tasks[task!!]!!].canceled) break
                            if (it.isFile && (it.lastModified() >= lastSync) && !new.contains(it.name) &&
                                (it.name.endsWith(".jpeg") || it.name.endsWith(".jpg") || it.name.endsWith(".png") || it.name.endsWith(".raw") ||
                                        it.name.endsWith(".JPEG") || it.name.endsWith(".JPG") || it.name.endsWith(".PNG") || it.name.endsWith(".RAW"))
                            ) {

                                var photo = Photo(Uri.fromFile(it), ruta_export, btnCheck.isChecked)
                                if (btnCheck.isChecked) selected++

                                lock.lock()
                                photos.add(photo)
                                lock.unlock()

                                runOnUiThread {
                                    lock_tasks.lock()
                                    if (task != null) tasks[positions_tasks[task!!]!!].progress = i
                                    (dialog_async_task.list_view_async_task.adapter as AdapterAsyncTask).notifyDataSetChanged()
                                    lock_tasks.unlock()
                                    btn_show_tasks.rotation =
                                        if (btn_show_tasks.rotation - 45 <= 0) 360f else btn_show_tasks.rotation - 45
                                    (recyclerViewGallery.adapter as RecyclerAdapterGallery).addPhoto(
                                        photo
                                    )
                                }

                                new.add(it.name)
                                lastSync = it.lastModified()
                                i++
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("EXCEPTION PHOTOS", e.toString())
                } finally {
                    runOnUiThread {
                        lock_tasks.lock()
                        if (task != null) {
                            tasks.removeAt(positions_tasks[task!!]!!)
                            positions_tasks.remove(task!!)
                        }
                        for (i in 0..tasks.size-1) positions_tasks[tasks[i]] = i
                        (dialog_async_task.list_view_async_task.adapter as AdapterAsyncTask).notifyDataSetChanged()
                        lock_tasks.unlock()
                        btnCheck.isClickable = true
                        btnEditSelected.isClickable = true
                        btnDelete.isClickable = true
                    }
                }
            }

        }
        thread_read.start()
    }

    fun checkDirectory(dir: File): Boolean {
        val dir = File(ruta_import)
        val state = Environment.getExternalStorageState(dir)

        if (!ruta_import.equals("./") && (state.equals(Environment.MEDIA_MOUNTED)
                    || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY))) {
            return true

        } else {
            Toaster.toastLong("ERROR: Directorio inaccesible")
            return false
        }
    }

    override fun onEdit(position: Int) {
        recyclerViewGallery.visibility = View.GONE

        val transaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.fragments.forEach { transaction.remove(it) }
        transaction.add(R.id.main, PhotoEditor(this, photos[position], position), "PhotoEditor")
        transaction.addToBackStack(null)
        transaction.commit()

        btnCheck.isClickable = false
        btnEditSelected.isClickable = false
        btnDelete.isClickable = false
    }

    override fun onCheck(position: Int) {
        lock.lock()
        photos[position].selected = !photos[position].selected
        lock.unlock()
        if (photos[position].selected) selected++ else selected--
        (recyclerViewGallery.adapter as RecyclerAdapterGallery).checkPhoto(position, photos[position].selected)
    }

    override fun onAccept(photo: Photo, position: Int) {
        var fOut: FileOutputStream? = null

        try {
            val file = File(photo.folder, photo.name)
            if ( file.exists() ) file.delete()

            fOut = FileOutputStream(file)

            photo.createBitmap().compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.flush()
            fOut.close()

            photo.uri = Uri.fromFile(file)
            lock.lock()
            photos[position] = photo
            lock.unlock()

        } catch (e: Exception) {
            Log.e("EXCEPTION PHOTO", e.toString())
        } finally {
            if (fOut != null) fOut.close()

            (recyclerViewGallery.adapter as RecyclerAdapterGallery).editPhoto(photo, position)
            (recyclerViewGallery.adapter as RecyclerAdapterGallery).refresh()

            val transaction = supportFragmentManager.beginTransaction()
            supportFragmentManager.fragments.forEach { transaction.remove(it) }
            transaction.commit()

            recyclerViewGallery.visibility = View.VISIBLE
            btnCheck.isClickable = true
            btnEditSelected.isClickable = true
            btnDelete.isClickable = true
        }
    }

    override fun onCancel(){
        val transaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.fragments.forEach { transaction.remove(it) }
        transaction.commit()

        recyclerViewGallery.visibility = View.VISIBLE
        btnCheck.isClickable = true
        btnEditSelected.isClickable = true
        btnDelete.isClickable = true
    }

    override fun onEditAll(resolcion: Resoluciones, brightness: Int, contrast: Int, saturation: Int, hue: Int, size: Float, pos: Pair<Float, Float>, resolution: Pair<Int, Int>, marca_agua: Sticker?, propaganda: Sticker?) {
        val transaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.fragments.forEach { transaction.remove(it) }
        transaction.commit()

        recyclerViewGallery.visibility = View.VISIBLE

        val thread_edit = object: Thread() {

            var task = AsyncTaskPG("Editando fotos", selected)

            init {
                lock_tasks.lock()
                tasks.add(task)
                positions_tasks[task] = tasks.size-1
                (dialog_async_task.list_view_async_task.adapter as AdapterAsyncTask).notifyDataSetChanged()
                lock_tasks.unlock()
            }

            override fun run(){
                try {
                    for (i in 0..photos.size - 1) {
                        if (tasks[positions_tasks[task!!]!!].canceled) break
                        if (photos[i].selected) {
                            val file = File(photos[i].folder, photos[i].name+".jpeg")
                            if (file.exists()) file.delete()

                            var fOut = FileOutputStream(file)

                            lock.lock()
                            photos[i].editImage(resolcion, brightness, contrast, saturation, hue, size, pos, resolution, marca_agua, propaganda)
                            photos[i].extension = "jpeg"
                            lock.unlock()
                            photos[i].createBitmap().compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                            fOut.flush()
                            fOut.close()

                            lock.lock()
                            photos[i].uri = Uri.fromFile(file)
                            lock.unlock()

                            runOnUiThread {
                                lock_tasks.lock()
                                tasks[positions_tasks[task]!!].progress = i + 1
                                (dialog_async_task.list_view_async_task.adapter as AdapterAsyncTask).notifyDataSetChanged()
                                lock_tasks.unlock()

                                btn_show_tasks.rotation = if (btn_show_tasks.rotation - 45 <= 0) 360f else btn_show_tasks.rotation - 45

                                (recyclerViewGallery.adapter as RecyclerAdapterGallery).editPhoto(photos[i], i)
                                (recyclerViewGallery.adapter as RecyclerAdapterGallery).refresh()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("EXCEPTION PHOTO", e.toString())
                } finally {
                    runOnUiThread {
                        lock_tasks.lock()
                        tasks.removeAt(positions_tasks[task]!!)
                        positions_tasks.remove(task)
                        for (i in 0..tasks.size-1) positions_tasks[tasks[i]] = i
                        (dialog_async_task.list_view_async_task.adapter as AdapterAsyncTask).notifyDataSetChanged()
                        lock_tasks.unlock()

                        btnCheck.isClickable = true
                        btnEditSelected.isClickable = true
                        btnDelete.isClickable = true
                    }
                }
            }
        }

        thread_edit.start()
    }

    override fun onCancelTask(task: AsyncTaskPG) {
        var pos: Int? = positions_tasks[task]
        if (pos != null) tasks[pos].canceled = true
    }

    override fun onExportDrive(drive: GoogleDriveService, folder: DriveFile) {
        val transaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.fragments.forEach { transaction.remove(it) }
        transaction.commit()

        recyclerViewGallery.visibility = View.VISIBLE
        btnCheck.isClickable = true
        btnEditSelected.isClickable = true
        btnDelete.isClickable = true

        if (selected == 0) Toaster.toast("No hay fotos seleccionadas")

        val thread_edit = object: Thread() {

            val photos_to_send = ArrayList<Photo>()
            var task = AsyncTaskPG("Exportando drive", selected)

            init {
                photos.toList().forEach { if (it.selected) photos_to_send.add(it.copy()) }
                lock_tasks.lock()
                tasks.add(task)
                positions_tasks[task] = tasks.size-1
                (dialog_async_task.list_view_async_task.adapter as AdapterAsyncTask).notifyDataSetChanged()
                lock_tasks.unlock()
            }

            override fun run(){
                try {
                    for (i in 0..photos_to_send.size - 1) {
                        if (tasks[positions_tasks[task!!]!!].canceled) break

                        var cont = 5
                        var res = ""

                        do {
                            cont--
                            res = drive.uploadImage(photos_to_send[i].name+"."+photos_to_send[i].extension, photos_to_send[i].extension, photos_to_send[i].uri, folder.id)
                        } while (res.equals("") && cont > 0)

                        if (res.equals("")) {
                            Toaster.toast("Error al enviar la imagen ${photos_to_send[i].name+"."+photos_to_send[i].extension}")
                            break
                        }

                        runOnUiThread {
                            lock_tasks.lock()
                            tasks[positions_tasks[task]!!].progress = i + 1
                            (dialog_async_task.list_view_async_task.adapter as AdapterAsyncTask).notifyDataSetChanged()
                            lock_tasks.unlock()

                            btn_show_tasks.rotation = if (btn_show_tasks.rotation - 45 <= 0) 360f else btn_show_tasks.rotation - 45
                        }
                    }
                } catch (e: Exception) {
                    Log.e("EXCEPTION export", e.toString())
                } finally {
                    runOnUiThread {
                        lock_tasks.lock()
                        tasks.removeAt(positions_tasks[task]!!)
                        positions_tasks.remove(task)
                        for (i in 0..tasks.size-1) positions_tasks[tasks[i]] = i
                        (dialog_async_task.list_view_async_task.adapter as AdapterAsyncTask).notifyDataSetChanged()
                        lock_tasks.unlock()
                    }
                }
            }
        }

        thread_edit.start()
    }

    fun loadSettings(){
        val prefs = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)
        ruta_import = prefs.getString("txtRutaImport", "ruta")!!
        ruta_export = prefs.getString("txtRutaExport", "ruta")!!
        ruta_marca_agua = prefs.getString("txtRutaMarcaAgua", "ruta")!!
        ruta_propaganda = prefs.getString("txtRutaPropaganda", "ruta")!!
    }
}
