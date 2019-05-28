package com.hedin.joomchallenge.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.hedin.joomchallenge.R
import com.hedin.joomchallenge.inflate
import com.hedin.joomchallenge.model.GifItem
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.gif_item_view.view.*

class GifGridRecyclerViewAdapter(
        private val items: MutableList<GifItem>,
        private val onItemClick: (GifItem, ImageView) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var progressVisible = false
        set(value) {
            val dataChanged = field != value
            field = value
            if (dataChanged) {
                notifyDataSetChanged()
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        VIEW_TYPE_ITEM ->
            GifViewHolder(parent.inflate(R.layout.gif_item_view))
        VIEW_TYPE_PROGRESS ->
            ProgressViewHolder(parent.inflate(R.layout.progress_footer))
        else ->
            throw IllegalArgumentException("Invalid ViewType: $viewType")
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (getItemViewType(position)) {
        VIEW_TYPE_ITEM ->
            (holder as GifViewHolder).bind(items[position], onItemClick)
        VIEW_TYPE_PROGRESS -> {
            (holder as ProgressViewHolder).bind(progressVisible)
        }
        else ->
            throw IllegalArgumentException("Invalid ViewType: ${getItemViewType(position)}")
    }

    override fun getItemCount() = if (items.isEmpty()) 0 else items.size + 1

    override fun getItemViewType(position: Int) = if (position != 0 && position == itemCount - 1)
        VIEW_TYPE_PROGRESS else VIEW_TYPE_ITEM

    fun addItems(newItems: List<GifItem>?) {
        newItems?.let {
            val count = itemCount
            items.addAll(newItems)
            notifyItemRangeInserted(count, newItems.size)
        }
    }

    fun replaceItems(newItems: List<GifItem>?) {
        items.clear()
        newItems?.let {
            items.addAll(newItems)
        }
        notifyDataSetChanged()
    }

    class GifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: GifItem, listener: (GifItem, ImageView) -> Unit) = with(itemView) {
            Picasso.get()
                    .load(item.url)
                    .placeholder(R.drawable.ic_gif_placeholder)
                    .into(itemView.gifImage)
            setOnClickListener { listener(item, gifImage) }
        }
    }

    class ProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(visible: Boolean) {
            itemView.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    companion object {
        const val VIEW_TYPE_ITEM = 0
        const val VIEW_TYPE_PROGRESS = 1
    }

}