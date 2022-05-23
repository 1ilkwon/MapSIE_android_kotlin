package kr.ac.tukorea.mapsie;

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ThemeAdapter(private val context:Context, private val themeList: ArrayList<ThemeData>):
    RecyclerView.Adapter<ThemeAdapter.ItemViewHolder>(){

        inner class ItemViewHolder(itemView:View): RecyclerView.ViewHolder(itemView){
            private val themeImg = itemView.findViewById<ImageView>(R.id.theme_img)
            private val themeTitle = itemView.findViewById<TextView>(R.id.theme_title)

            fun bind(themeData: ThemeData, context: Context) {
                //사진 처리
                if(themeData.img != ""){
                    val resouceId = context.resources.getIdentifier(themeData.img, "drawable", context.packageName)

                    if(resouceId > 0) {
                        themeImg.setImageResource(resouceId)
                    }else {
                        themeImg.setImageResource(R.drawable.ic_baseline_sports_esports_24)
                    }
                } else{
                    themeImg.setImageResource(R.drawable.ic_baseline_sports_esports_24)
                }

                themeTitle.text = themeData.title
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.theme_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(themeList[position], context)
    }

    override fun getItemCount(): Int {
        return themeList.size
    }
}
