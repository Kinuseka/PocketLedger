package com.macarambon.pocketledger.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View.MeasureSpec
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView that expands to show all items when placed inside a ScrollView.
 */
class FullyExpandedRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RecyclerView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(
            widthSpec,
            MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST),
        )
    }
}
