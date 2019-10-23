package com.carlos.photogallery.clases

class DriveFile(id:String, name:String, mimeType:String, parent:List<String> = ArrayList<String>(), iconLink:String? = null, webViewLink:String? = null) {
    var id:String = id
    var name:String = name
    var mimeType:String = mimeType
    var parent:List<String> = parent
    var iconLink:String? = iconLink
    var webViewLink:String? = webViewLink

    override fun toString(): String {
        super.toString()
        return "Id: $id, name: $name, mimeType: $mimeType, parent: $parent, iconLink: $iconLink, webViewLink: $webViewLink"
    }
}