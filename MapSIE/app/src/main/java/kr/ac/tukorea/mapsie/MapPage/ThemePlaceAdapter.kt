package kr.ac.tukorea.mapsie.MapPage


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.theme_item.view.*
import kotlinx.android.synthetic.main.theme_place_item.view.*
import kr.ac.tukorea.mapsie.DetailActivity
import kr.ac.tukorea.mapsie.MapActivity
import kr.ac.tukorea.mapsie.R


class ThemePlaceAdapter(private val context: Context, private val themePlaceList: ArrayList<ThemePlaceList>):
    RecyclerView.Adapter<ThemePlaceAdapter.ViewHolder>() {

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

    }



