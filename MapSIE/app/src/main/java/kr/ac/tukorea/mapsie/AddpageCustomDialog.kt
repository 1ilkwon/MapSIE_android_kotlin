package kr.ac.tukorea.mapsie

import android.app.Dialog
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.mapsie.SearchPage.ListAdapter
import kr.ac.tukorea.mapsie.SearchPage.ListLayout

open class AddpageCustomDialog(context: Context) {
    private val dialog = Dialog(context)
    private val listItems = arrayListOf<ListLayout>()   // 리사이클러 뷰 아이템
    private val listAdapter = ListAdapter(listItems)    // 리사이클러 뷰 어댑터

    fun showDia(){
        dialog.setContentView(R.layout.addpage_search)

        var rv_list = dialog.findViewById<RecyclerView>(R.id.rv_list)

        rv_list.adapter = listAdapter

        dialog.show()
    }
    fun getData( item: ListLayout){

    }
}