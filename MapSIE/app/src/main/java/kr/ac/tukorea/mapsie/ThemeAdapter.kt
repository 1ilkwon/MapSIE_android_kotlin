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
            val intent1 = Intent(context, MapActivity::class.java)

            intent1.putExtra("ThemeName", item.num)
            intent1.putExtra("ThemeCollection", item.collect)
            intent1.run { context.startActivity(this) }


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
