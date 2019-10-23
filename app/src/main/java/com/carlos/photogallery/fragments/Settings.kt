package com.carlos.photogallery.fragments

import android.content.Context
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.files.folderChooser
import com.carlos.photogallery.R
import kotlinx.android.synthetic.main.fragment_settings.*
import java.io.File


class Settings : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onStart() {
        super.onStart()
        loadSettings()

        btnRutaImport.setOnClickListener {
            var ruta = ""
            MaterialDialog(requireContext())
                .show {
                    folderChooser (File("/storage/"),
                        waitForPositiveButton = true
                    ){ dialog, folder ->
                        ruta = folder.absolutePath
                    }
                }
                .setOnDismissListener {
                    txtRutaImport.setText(ruta)
                }
        }

        btnRutaExport.setOnClickListener {
            var ruta = ""
            MaterialDialog(requireContext())
                .show {
                    folderChooser (Environment.getExternalStorageDirectory(),
                        //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        waitForPositiveButton = true,
                        allowFolderCreation = true,
                        folderCreationLabel = R.string.nueva_carpeta
                    ){ dialog, folder ->
                        ruta = folder.absolutePath
                    }
                }
                .setOnDismissListener {
                    txtRutaExport.setText(ruta)
                }
        }

        btnRutaMarcaAgua.setOnClickListener {
            var ruta = ""
            MaterialDialog(requireContext())
                .show {
                    folderChooser (File("/storage/"),
                        waitForPositiveButton = true
                    ){ dialog, folder ->
                        ruta = folder.absolutePath
                    }
                }
                .setOnDismissListener {
                    txtRutaMarcaAgua.setText(ruta)
                }
        }

        btnRutaPropaganda.setOnClickListener {
            var ruta = ""
            MaterialDialog(requireContext())
                .show {
                    folderChooser (File("/storage/"),
                        waitForPositiveButton = true
                    ){ dialog, folder ->
                        ruta = folder.absolutePath
                    }
                }
                .setOnDismissListener {
                    txtRutaPropaganda.setText(ruta)
                }
        }
    }

    override fun onPause() {
        super.onPause()
        saveSettings()

    }

    fun loadSettings(){
        val prefs = context?.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)
        txtRutaImport.setText(prefs?.getString("txtRutaImport", "ruta"))
        txtRutaExport.setText(prefs?.getString("txtRutaExport", "ruta"))
        txtRutaMarcaAgua.setText(prefs?.getString("txtRutaMarcaAgua", "ruta"))
        txtRutaPropaganda.setText(prefs?.getString("txtRutaPropaganda", "ruta"))
    }

    fun saveSettings(){
        val prefs = context?.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)
        val editor = prefs?.edit()

        editor?.putString("txtRutaImport", txtRutaImport.text.toString())
        editor?.putString("txtRutaExport", txtRutaExport.text.toString())
        editor?.putString("txtRutaMarcaAgua", txtRutaMarcaAgua.text.toString())
        editor?.putString("txtRutaPropaganda", txtRutaPropaganda.text.toString())

        editor?.commit()
    }
}
