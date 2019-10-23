package com.carlos.photogallery.clases

enum class Resoluciones(val w:Int, val h:Int) {
    R_ORIGINAL(1, 1),
    R_2048x2048( 2048, 2048),
    R_1920x1080( 1920, 1080),
    R_1280x720( 1280, 720),
    R_1280x1200( 1280, 1200),
    R_1200x630( 1200, 630),
    R_1080x1080( 1080, 1080),
    R_1080x566( 1080, 566),
    R_854x480( 854, 480);
}