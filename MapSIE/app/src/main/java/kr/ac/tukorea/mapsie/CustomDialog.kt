package kr.ac.tukorea.mapsie

import android.app.Dialog
import android.content.Context

open class CustomDialog(context: Context) {
    private val dialog = Dialog(context)
    fun showDia(){
        dialog.setContentView(R.layout.activity_theme_dialog)
        dialog.show()
    }
}