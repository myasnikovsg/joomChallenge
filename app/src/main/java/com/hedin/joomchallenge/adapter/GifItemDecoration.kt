package com.hedin.joomchallenge.adapter

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class GifItemDecoration(private val offset : Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        outRect.bottom = offset
        outRect.left = offset
        outRect.right = offset
        outRect.top = offset
    }
}