package com.au.lyber.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class ScrollListener(private val linearLayoutManager: LinearLayoutManager) :
    RecyclerView.OnScrollListener() {



    abstract fun shouldLoad(): Boolean
    abstract fun loadMore()

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

        if (dy > 0) {

            val visibleItemCount = linearLayoutManager.childCount
            val pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition()
            val totalItems = linearLayoutManager.itemCount

            if (shouldLoad()) {
                if ((visibleItemCount + pastVisibleItems) >= totalItems) {
                    loadMore()
                }
            }
        }
        super.onScrolled(recyclerView, dx, dy)
    }

    open fun upAction(){

    }

    open fun downAction(){

    }
}