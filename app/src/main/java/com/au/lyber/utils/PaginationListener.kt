package com.au.lyber.utils

import android.util.Log
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


abstract class PaginationListener(private val layoutManager: LinearLayoutManager) :
    RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        if (!isLoading() && !isLastPage()) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                && firstVisibleItemPosition >= 0
            ) loadMoreItems()
        }

    }



    protected abstract fun loadMoreItems()
    abstract fun isLastPage(): Boolean
    abstract fun isLoading(): Boolean


    abstract class NestedScrollPaginationListener :
        NestedScrollView.OnScrollChangeListener {

        abstract fun isLoading(): Boolean
        abstract fun isLastPage(): Boolean
        abstract fun loadMoreItems()
        override fun onScrollChange(
            v: NestedScrollView,
            scrollX: Int,
            scrollY: Int,
            oldScrollX: Int,
            oldScrollY: Int
        ) {

            val lastChild = v.getChildAt(v.childCount - 1)

            if (lastChild != null) {

                if ((scrollY >= (lastChild.measuredHeight - v.measuredHeight)) && scrollY > oldScrollY && !isLoading() && !isLastPage()) {
                    loadMoreItems()
                    //get more items
                }
            }

        }
    }

}