package kr.ac.tukorea.mapsie;

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.kakao.sdk.common.KakaoSdk.init
import kotlinx.android.synthetic.main.theme_item.view.*
import java.util.*
import kotlin.collections.ArrayList

class ThemeAdapter(private val context:Context, private val themeList: ArrayList<ThemeData>):
    RecyclerView.Adapter<ThemeAdapter.ItemViewHolder>(), Filterable{
        //필터 전 리스트
        var unfilter = themeList
        //필터를 위한 변수
        var filter = themeList


        inner class ItemViewHolder(itemView:View): RecyclerView.ViewHolder(itemView){
            private var view:View = itemView


            fun bind(listener: View.OnClickListener, item:ThemeData) {
                view.theme_title.text = item.title
                Glide.with(itemView).load(item.img).into(view.theme_img)
                view.setOnClickListener(listener)
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeAdapter.ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.theme_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        //val item = themeList[position]
        val item = filter[position]
        val listener = View.OnClickListener { it ->
            //Toast.makeText(it.context, "title : ${item.title}", Toast.LENGTH_SHORT).show()
            val intent = Intent(context, MapActivity::class.java)
            intent.run { context.startActivity(this) }
        }
        holder.apply {
            bind(listener, item)
            itemView.tag = item
        }
    }

    override fun getItemCount(): Int {
        //return themeList.size
        return filter.size
    }


    //필러를 위한 메서드
//    override fun getFilter(): Filter {
//        return itemFilter
//    }
//    inner class ItemFilter:Filter(){
//            override fun performFiltering(charSequence: CharSequence): FilterResults {
//                val filterString = charSequence.toString()
//                val results = FilterResults()
//
//                //검색이 필요없을 경우를 위해 원본 배열을 복제
//                val filteredList: ArrayList<ThemeData> = ArrayList<ThemeData>()
//                //공백제외 아무런 값이 없을 경우 -> 원본 배열
//                if (filterString.trim { it <= ' ' }.isEmpty()) {
//                    results.values = themeList
//                    results.count = themeList.size
//
//                    return results
//                    //공백제외 2글자 이하인 경우 -> 이름으로만 검색
//                } else if (filterString.trim { it <= ' ' }.length <= 2) {
//                    for (theme in themeList) {
//                        if (theme.title.contains(filterString)) {
//                            filteredList.add(theme)
//                        }
//                    }
//                    //그 외의 경우(공백제외 2글자 초과) -> 이름/전화번호로 검색
//                } else {
//                    for (theme in themeList) {
//                        if (theme.title.contains(filterString)) {
//                            filteredList.add(theme)
//                        }
//                    }
//                }
//                results.values = filteredList
//                results.count = filteredList.size
//
//                return results
//            }
//            @SuppressLint("NotifyDataSetChanged")
//            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
//                filteredTheme.clear()
//                filteredTheme.addAll(filterResults.values as ArrayList<ThemeData>)
//                notifyDataSetChanged()
//            }
//    }
    //리사이클뷰 필터링 메서드
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    filter = unfilter
                } else {
                    var resultList = ArrayList<ThemeData>()
                    for (item in unfilter) {
                        if(item.title.toLowerCase().contains(charSearch.toLowerCase()))
                            resultList.add(item)
                    }
                    filter = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filter
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filter = results?.values as ArrayList<ThemeData>
                notifyDataSetChanged()
            }

        }
    }
}
