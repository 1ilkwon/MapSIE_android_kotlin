package kr.ac.tukorea.mapsie.MapPage


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.Toast

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.theme_item.view.*
import kotlinx.android.synthetic.main.theme_place_item.view.*
import kr.ac.tukorea.mapsie.DetailActivity
import kr.ac.tukorea.mapsie.MapActivity
import kr.ac.tukorea.mapsie.R
import kr.ac.tukorea.mapsie.ThemeData


class ThemePlaceAdapter(private val context: Context, private val themePlaceList: ArrayList<ThemePlaceList>):
    RecyclerView.Adapter<ThemePlaceAdapter.ViewHolder>(), Filterable {
    //필터 전 리스트
    var unfilter = themePlaceList
    //필터를 위한 변수
    var filter = themePlaceList

    // 각 항목에 필요한 기능을 구현
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var view: View = itemView

        fun bind(listener: View.OnClickListener, item: ThemePlaceList) {
            view.themeplacename.text = item.placename
            view.themeplaceaddress.text = item.placeaddress
            Glide.with(itemView).load(item.placeimage).into(view.place_image as ImageView)
            view.setOnClickListener(listener)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemePlaceAdapter.ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.theme_place_item, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filter[position]
        val listener = View.OnClickListener {
            var curPos : Int = position
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("Simage", item.placeimage)
            intent.putExtra("Sname", item.placename)
            intent.putExtra("Saddress", item.placeaddress)
            intent.putExtra("Stheme", item.placeTheme)
            intent.putExtra("SstoreName", item.placeNum) // 문서 이름
            intent.putExtra("Position", position.toString())
            intent.putExtra("Introduce", item.introduce)
            //Toast.makeText(context,"this",Toast.LENGTH_SHORT).show()

            intent.run {
                context.startActivity(this)
            }
        }
        holder.apply {
            bind(listener, item)
            itemView.tag = item
        }
    }



    override fun getItemCount(): Int {
        return filter.size
    }

    //리사이클뷰 필터링 메서드 (구현 중)
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    filter = unfilter
                } else {
                    var resultList = ArrayList<ThemePlaceList>()
                    for (item in unfilter) {
                        if (item.placename.toLowerCase().contains(charSearch.toLowerCase()))
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
                filter = results?.values as ArrayList<ThemePlaceList>
                notifyDataSetChanged()
            }

        }
    }

}

