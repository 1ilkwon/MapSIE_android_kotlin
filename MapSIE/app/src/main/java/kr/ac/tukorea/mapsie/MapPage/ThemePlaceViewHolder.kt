package kr.ac.tukorea.mapsie.MapPage

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.detail_body.view.*
import kotlinx.android.synthetic.main.theme_place_item.view.*
import kr.ac.tukorea.mapsie.R

class ThemePlaceViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {

    private val themePlaceNameTextView = itemView.nameText
    private val themePlaceAddressTextView = itemView.addressText
    private val themePlaceImageView = itemView.themeimageView

    fun bind(themePlaceList: ThemePlaceList){
        themePlaceNameTextView.text = themePlaceList.placename
        themePlaceAddressTextView.text = themePlaceList.placeaddress

        Glide
            .with(ThemePlaceApp.instance)
            .load(themePlaceList.placeimage)
            .centerCrop()
            .placeholder(R.drawable.flower)
            .into(themePlaceImageView)


    }

}
