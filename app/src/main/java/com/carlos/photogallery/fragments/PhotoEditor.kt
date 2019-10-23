package com.carlos.photogallery.fragments

import android.content.Context
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar

import com.carlos.photogallery.clases.Photo
import com.carlos.photogallery.clases.Sticker
import com.carlos.photogallery.listeners.ServiceListenerPhotoEditor
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_options_photo.view.*
import kotlinx.android.synthetic.main.fragment_photo_editor.*
import xdroid.toaster.Toaster
import java.io.File
import android.view.WindowManager
import com.carlos.photogallery.R
import com.carlos.photogallery.clases.Resoluciones


const val SELECTED_NOTHING = 0
const val SELECTED_BRIGHTNESS = 1
const val SELECTED_CONTRAST = 2
const val SELECTED_SATURATION = 3
const val SELECTED_HUE = 4
const val SELECTED_SIZE = 5
const val SELECTED_MARCA_AGUA = 6
const val SELECTED_PROPAGANDA = 7

class PhotoEditor(private val listener: ServiceListenerPhotoEditor,
                  private val photo: Photo,
                  private val position: Int) : Fragment() {

    var primera = true
    var resolucion = Resoluciones.R_ORIGINAL
    var brightness = 0
    var contrast = 0
    var saturation = 0
    var hue = 0
    var size = 1f
    var pos = Pair(0f, 0f)
    var resolution = Pair(0, 0)

    var marca_agua: Sticker? = null
    var propaganda: Sticker? = null

    var ruta_marca_agua = ""
    var ruta_propaganda = ""

    var selected = SELECTED_NOTHING

    init {
        resolucion = photo.resolucion
        brightness = photo.brightness
        contrast = photo.contrast
        saturation = photo.saturation
        hue = photo.hue
        size = photo.size
        pos = photo.pos
        resolution = photo.resolution

        marca_agua = photo.marca_agua
        propaganda = photo.propaganda
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_photo_editor, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onStart() {
        super.onStart()
        loadSettings()
        imageEditPhoto.setImageBitmap(photo.createBitmap())

        val metrics = DisplayMetrics()
        (context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(metrics)
        var location = IntArray(2)
        imageEditPhoto.getLocationOnScreen(location)
        val center_x = (metrics.widthPixels - location[0]) / 2
        val center_y = (metrics.heightPixels - location[1]) / 2
        resolution = Pair((metrics.widthPixels - location[0]), (metrics.heightPixels - location[1]))

        imageEditPhoto.setOnTouchListener { v, event ->
            val aux = Pair(event.x-center_x, event.y-center_y)

//            Log.e("TOUCH", "-------------------------------------------")
//            Log.e("TOUCH", "Center imageView: ${center_x}, ${center_y}")
//            Log.e("TOUCH", "points imageView: ${event.x}, ${event.y}")
//            Log.e("TOUCH", "location: ${location[0]}, ${location[1]}")
//            Log.e("TOUCH", "Pos center: $aux")
//            Log.e("TOUCH", "-------------------------------------------")

            if ( selected == SELECTED_SIZE ) pos = aux
            if ( selected == SELECTED_MARCA_AGUA && marca_agua != null ) marca_agua!!.position = aux
            if ( selected == SELECTED_PROPAGANDA && propaganda != null ) propaganda!!.position = aux

            if (selected == SELECTED_SIZE || selected == SELECTED_MARCA_AGUA || selected == SELECTED_PROPAGANDA) {
                when (event.action) {
                    MotionEvent.ACTION_MOVE -> {
                        photo.editImage(resolucion, brightness, contrast, saturation, hue, size, pos, resolution, marca_agua, propaganda)
                        imageEditPhoto.setImageBitmap(photo.createBitmap())
                    }
                    MotionEvent.ACTION_UP -> {
                        photo.editImage(resolucion, brightness, contrast, saturation, hue, size, pos, resolution, marca_agua, propaganda)
                        imageEditPhoto.setImageBitmap(photo.createBitmap())
                    }
                }
            }
            true
        }

        val dialog = BottomSheetDialog(context!!)
        val bottomSheet = layoutInflater.inflate(R.layout.dialog_options_photo, null)
        dialog.setContentView(bottomSheet)
        dialog.setOnDismissListener {
            primera = false
            if (seek_bar_tool != null){
                if (selected == SELECTED_NOTHING) seek_bar_tool.visibility = View.INVISIBLE
                else seek_bar_tool.visibility = View.VISIBLE
            }
        }

        btn_tools.setOnClickListener {
            selected = SELECTED_NOTHING
            dialog.show()
        }
        seek_bar_tool.visibility = View.INVISIBLE
        seek_bar_tool.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                val aux = if (i == 0) 1 else i
                seek_bar_tool.visibility = View.VISIBLE
                when (selected) {
                    SELECTED_NOTHING -> { seek_bar_tool.visibility = View.INVISIBLE }
                    SELECTED_BRIGHTNESS -> { brightness = aux - 100 }
                    SELECTED_CONTRAST -> { contrast = aux - 100 }
                    SELECTED_SATURATION -> { saturation = aux - 100 }
                    SELECTED_HUE -> { hue = aux - 100 }
                    SELECTED_SIZE -> { size = aux / 10f }
                    SELECTED_MARCA_AGUA -> { marca_agua!!.size = aux / 10f }
                    SELECTED_PROPAGANDA -> { propaganda!!.size = aux / 10f }
                }
                photo.editImage(resolucion, brightness, contrast, saturation, hue, size, pos, resolution, marca_agua, propaganda)
                imageEditPhoto.setImageBitmap(photo.createBitmap())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        bottomSheet.btnCancel.setOnClickListener {
            dialog.dismiss()
            listener.onCancel()
        }
        bottomSheet.btnAccept.setOnClickListener {
            dialog.dismiss()

            if (position == -1){
                listener.onEditAll(resolucion, brightness, contrast, saturation, hue, size, pos, resolution, marca_agua, propaganda)
            } else {
                listener.onAccept(photo, position)
            }
        }

        bottomSheet.btn_brightness.setOnClickListener {
            seek_bar_tool.max = 200
            seek_bar_tool.progress = brightness + 100
            selected = SELECTED_BRIGHTNESS
            dialog.dismiss()
        }
        bottomSheet.btn_contrast.setOnClickListener {
            seek_bar_tool.max = 200
            seek_bar_tool.progress = contrast + 100
            selected = SELECTED_CONTRAST
            dialog.dismiss()
        }
        bottomSheet.btn_saturation.setOnClickListener {
            seek_bar_tool.max = 200
            seek_bar_tool.progress = saturation + 100
            selected = SELECTED_SATURATION
            dialog.dismiss()
        }
        bottomSheet.btn_hue.setOnClickListener {
            seek_bar_tool.max = 200
            seek_bar_tool.progress = hue + 100
            selected = SELECTED_HUE
            dialog.dismiss()
        }
        bottomSheet.btn_size.setOnClickListener {
            seek_bar_tool.max = 20
            seek_bar_tool.progress = (size * 10).toInt()
            selected = SELECTED_SIZE
            dialog.dismiss()
        }


        val adapter_resoluciones = ArrayAdapter<Resoluciones>(requireContext(), android.R.layout.simple_spinner_item, Resoluciones.values())
        adapter_resoluciones.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bottomSheet.sp_resolucion.adapter = adapter_resoluciones
        bottomSheet.sp_resolucion.setSelection(resolucion.ordinal)
        bottomSheet.sp_resolucion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>, v: android.view.View, position: Int, id: Long) {
                resolucion = parent.getItemAtPosition(position) as Resoluciones
                pos = Pair(0f, 0f)
                photo.editImage(resolucion, brightness, contrast, saturation, hue, size, pos, resolution, marca_agua, propaganda)
                imageEditPhoto.setImageBitmap(photo.createBitmap())

                seek_bar_tool.max = 20
                seek_bar_tool.progress = (size * 10).toInt()
                selected = SELECTED_SIZE
                if (!primera) dialog.dismiss()
            }
        }


        val adapter_marca_agua = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, listDirectori(ruta_marca_agua))
        adapter_marca_agua.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bottomSheet.sp_marca_agua.adapter = adapter_marca_agua
        if (marca_agua != null) bottomSheet.sp_marca_agua.setSelection(getPositionSticker(marca_agua!!.name, ruta_marca_agua))
        bottomSheet.sp_marca_agua.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>, v: View, position: Int, id: Long) {
                marca_agua = getSticker(parent.getItemAtPosition(position).toString(), ruta_marca_agua)
                photo.editImage(resolucion, brightness, contrast, saturation, hue, size, pos, resolution, marca_agua, propaganda)
                imageEditPhoto.setImageBitmap(photo.createBitmap())

                if (marca_agua != null) {
                    seek_bar_tool.max = 20
                    seek_bar_tool.progress = (marca_agua!!.size * 10).toInt()
                    selected = SELECTED_MARCA_AGUA
                    if (!primera) dialog.dismiss()
                    primera = false
                } else {
                    selected = SELECTED_NOTHING
                }
            }
        }


        val adapter_propaganda = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, listDirectori(ruta_propaganda))
        adapter_propaganda.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bottomSheet.sp_propaganda.adapter = adapter_propaganda
        if (propaganda != null) bottomSheet.sp_propaganda.setSelection(getPositionSticker(propaganda!!.name, ruta_propaganda))
        bottomSheet.sp_propaganda.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>, v: View, position: Int, id: Long) {
                propaganda = getSticker(parent.getItemAtPosition(position).toString(), ruta_propaganda)
                photo.editImage(resolucion, brightness, contrast, saturation, hue, size, pos, resolution, marca_agua, propaganda)
                imageEditPhoto.setImageBitmap(photo.createBitmap())

                if (propaganda != null) {
                    seek_bar_tool.max = 20
                    seek_bar_tool.progress = (propaganda!!.size * 10).toInt()
                    selected = SELECTED_PROPAGANDA
                    if (!primera) dialog.dismiss()
                    primera = false
                } else {
                    selected = SELECTED_NOTHING
                }
                primera = false
            }
        }
    }

    fun listDirectori(ruta: String): ArrayList<String> {
        val res: ArrayList<String> = ArrayList<String>().apply { add("") }
        try {
            File(ruta).listFiles().forEach { res.add(it.name) }
        } catch (e: Exception) { Toaster.toastLong("ERROR: Directorio inaccesible") }

        return res
    }

    fun getSticker(name: String, ruta: String): Sticker? {
        try {
            File(ruta).listFiles().forEach {
                if (it.name.equals(name)) return Sticker(Uri.fromFile(it), it.name)
            }
        } catch (e: Exception) { Toaster.toastLong("ERROR: Directorio inaccesible") }

        return null
    }

    fun getPositionSticker(name: String, ruta: String): Int {
        try {
            val files = File(ruta).listFiles()
            for (i in 0..files.size-1){
                if (files[i].name.equals(name)) return i+1
            }
        } catch (e: Exception) { Toaster.toastLong("ERROR: Directorio inaccesible") }
        return 0
    }

    fun loadSettings(){
        val prefs = context!!.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)
        ruta_marca_agua = prefs.getString("txtRutaMarcaAgua", "ruta")!!
        ruta_propaganda = prefs.getString("txtRutaPropaganda", "ruta")!!
    }
}
