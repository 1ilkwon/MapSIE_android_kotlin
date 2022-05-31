package kr.ac.tukorea.mapsie;

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteMisuseException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kakao.sdk.common.KakaoSdk.init
import kotlinx.android.synthetic.main.theme_item.view.*

class ThemeAdapter(private val context:Context, private val themeList: ArrayList<ThemeData>):
    RecyclerView.Adapter<ThemeAdapter.ItemViewHolder>(){

        inner class ItemViewHolder(itemView:View): RecyclerView.ViewHolder(itemView){
            private var view:View = itemView

            fun bind(listener: View.OnClickListener, item:ThemeData) {
                view.theme_title.text = item.title
                view.theme_img.setImageDrawable(item.img)
                view.setOnClickListener(listener)
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeAdapter.ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.theme_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = themeList[position]
        val listener = View.OnClickListener { it ->
            Toast.makeText(it.context, "title : ${item.title}", Toast.LENGTH_SHORT).show()
        }


        holder.apply {
            bind(listener, item)
            itemView.tag = item
        }
    }

    override fun getItemCount(): Int {
        return themeList.size
    }
}
