package com.carlos.photogallery.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.input.input
import com.carlos.photogallery.R
import com.carlos.photogallery.adapters.RecycleAdapterDriveFiles
import com.carlos.photogallery.clases.DriveFile
import com.carlos.photogallery.listeners.ServiceListenerDriveFile
import com.carlos.photogallery.listeners.ServiceListenerDialogDriveFile
import kotlinx.android.synthetic.main.dialog_drive_files.view.*


class DialogDriveFiles(private val windowContext: Context, private val listenerDriveFile: ServiceListenerDialogDriveFile,
                       private val files: ArrayList<DriveFile>, private val parentId: String)
                        : DialogFragment(), ServiceListenerDriveFile {

    lateinit var v: View
    lateinit var id:String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val builder = AlertDialog.Builder(windowContext)

        val inflater = this.activity!!.layoutInflater
        v = inflater.inflate(R.layout.dialog_drive_files, null)
        builder.setView(v)

        id = parentId
        v.txtTitle.setText(searchFile(id).name)

        val layoutManager = LinearLayoutManager(this.context)
        v.recyclerViewDialog.layoutManager = layoutManager
        val adapter = RecycleAdapterDriveFiles(this, filesToShow(files, id))
        v.recyclerViewDialog.adapter = adapter

        v.btnBack.setOnClickListener {
            id = searchParent(files, id)
            v.txtTitle.setText(searchFile(id).name)

            val layoutManager = LinearLayoutManager(this.context)
            v.recyclerViewDialog.layoutManager = layoutManager
            val adapter = RecycleAdapterDriveFiles(this, filesToShow(files, id))
            v.recyclerViewDialog.adapter = adapter
        }

        v.btnNewFolder.setOnClickListener{
            MaterialDialog(requireContext()).show {
                var name:String? = null

                input(waitForPositiveButton = true, allowEmpty = false) { dialog, text ->
                    name = text.toString()
                }

                positiveButton(R.string.accept)

                onDismiss {
                    if (name != null){
                        listenerDriveFile.onNewFolder( DriveFile("", name!!, "", List<String>(1) {id}) )
                    }
                }
            }
            dismiss()
        }

        v.btnCancel.setOnClickListener { dismiss() }

        v.btnAccept.setOnClickListener {
            listenerDriveFile.onAccept(searchFile(id))
            dismiss()
        }

        return builder.create()
    }

    override fun onClick(parentId: String) {
        id = parentId
        val adapter = RecycleAdapterDriveFiles(this, filesToShow(files, id))
        v.recyclerViewDialog.adapter = adapter
    }

    private fun filesToShow(files:ArrayList<DriveFile>, id:String): ArrayList<DriveFile> {
        val res:ArrayList<DriveFile> = ArrayList<DriveFile>()
        files.forEach(){
            if ( id.equals(it.parent[0]) ) res.add(it)
        }
        return res
    }

    private fun searchParent(files:ArrayList<DriveFile>, id:String): String {
        files.forEach() {
            if (id.equals(it.id)) return it.parent[0]
        }
        return parentId
    }

    private fun searchFile(id:String): DriveFile {
        files.forEach() {
            if (id.equals(it.id)) return it
        }
        return DriveFile(parentId, "My Drive", "application/vnd.google-apps.folder", ArrayList<String>())
    }
}