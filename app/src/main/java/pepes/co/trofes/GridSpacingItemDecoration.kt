package pepes.co.trofes

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

/**
 * Spacing sederhana untuk GridLayoutManager.
 * - includeEdge=false supaya padding luar tetap mengikuti padding RecyclerView (lebih gampang kontrolnya).
 */
class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacingDp: Int,
    private val includeEdge: Boolean,
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val density = parent.resources.displayMetrics.density
        val spacing = (spacingDp * density).roundToInt()

        val column = position % spanCount

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            if (position < spanCount) {
                outRect.top = spacing
            }
            outRect.bottom = spacing
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount

            if (position >= spanCount) {
                outRect.top = spacing
            }
        }
    }
}
