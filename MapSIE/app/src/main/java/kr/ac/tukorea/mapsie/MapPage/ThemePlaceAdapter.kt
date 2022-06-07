package kr.ac.tukorea.mapsie.MapPage


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
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
    val item = filter[position]

    override fun getItemCount(): Int = themePlaceList.size

    // 각 항목에 필요한 기능을 구현
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v
        fun bind(listener: View.OnClickListener, item: ThemePlaceList) {
            view.themeplacename.text = item.placename
            view.themeplaceaddress.text = item.placeaddress
            view.setOnClickListener(listener)
        }
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = themePlaceList[position]
        val listener = View.OnClickListener {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("Sname", item.placename)
            intent.putExtra("Saddress", item.placeaddress)
            intent.putExtra("Stheme", item.placeTheme)
            intent.putExtra("SstoreName", item.placeNum) // 문서 이름


            intent.run {
                context.startActivity(this)
            }
        }
        holder.apply {
            bind(listener, item)
            itemView.tag = item
        }
    }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemePlaceAdapter.ViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.theme_place_item, parent, false)
            return ViewHolder(view)
        }

    //리사이클뷰 필터링 메서드
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    filter = unfilter
                } else {
                    var resultList = ArrayList<ThemePlaceList>()
                    for (item in unfilter) {
                        if (item.title.toLowerCase().contains(charSearch.toLowerCase()))
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



