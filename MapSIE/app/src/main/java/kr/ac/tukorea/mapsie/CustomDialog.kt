package kr.ac.tukorea.mapsie

import android.app.Dialog
import android.content.Context
import kr.ac.tukorea.mapsie.R

open class CustomDialog(context: Context) {
    private val dialog = Dialog(context)
    fun showDia(){
        dialog.setContentView(R.layout.theme_detail_list_n_search)
        dialog.show()
    }
}