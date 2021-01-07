package com.schoolmanager.utilities;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.schoolmanager.R;

import java.util.Objects;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    //    private Drawable mDivider;
    private final int horizontalDecorationWidth;
    private final int verticalDecorationHeight;
    private final int lastDecorationHeight;
    private final boolean lastSpace;
    private int orientationOfLayout;
    private int noOfColumn;

//    @orientationOfLayout is 0 for HORIZONTAL, 1 for VERTICAL

    public SpaceItemDecoration(Context context, int orientationOfLayout, int noOfColumn,
                               int verticalOffset, int horizontalOffset, boolean lastSpace) {
//        mDivider = ContextCompat.getDrawable(context,R.drawable.recycler_divider);
        this.orientationOfLayout = orientationOfLayout;
        this.noOfColumn = noOfColumn;
        verticalDecorationHeight = context.getResources().getDimensionPixelSize(verticalOffset);
        horizontalDecorationWidth = context.getResources().getDimensionPixelSize(horizontalOffset);
        lastDecorationHeight = context.getResources().getDimensionPixelSize(R.dimen.recycler_end_offset);
        this.lastSpace = lastSpace;
    }

//    @Override
//    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
//        int left = parent.getPaddingLeft();
//        int right = parent.getWidth() - parent.getPaddingRight();
//
//        int childCount = parent.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            View child = parent.getChildAt(i);
//
//            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
//
//            int top = child.getBottom() + params.bottomMargin;
//            int bottom = top + mDivider.getIntrinsicHeight();
//
//            mDivider.setBounds(left, top, right, bottom);
//            mDivider.draw(c);
//        }
//    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
//        if (parent != null && view != null) {
        int itemPosition = parent.getChildAdapterPosition(view);
        int totalCount = Objects.requireNonNull(parent.getAdapter()).getItemCount();

        if (noOfColumn > 0) {
            if (noOfColumn == 1) {
                arrangeInList(itemPosition, outRect, totalCount);
            } else {
                arrangeInGrid(itemPosition, outRect, totalCount, noOfColumn);
            }
        }
    }

    private void arrangeInList(int position, Rect outRect, int totalCount) {
        if (position >= 0) {
            outRect.left = horizontalDecorationWidth;
            outRect.right = horizontalDecorationWidth;

            if (position == 0) {
                outRect.top = verticalDecorationHeight * 2;
                outRect.bottom = verticalDecorationHeight;
            }

            if (position > 0 && position < totalCount - 1) {
                outRect.top = verticalDecorationHeight;
                outRect.bottom = verticalDecorationHeight;
            }

            if (position == (totalCount - 1)) {
                outRect.top = verticalDecorationHeight;
                if (lastSpace) {
                    outRect.bottom = lastDecorationHeight;
                } else {
                    outRect.bottom = verticalDecorationHeight * 2;
                }
            }
        }
    }

    private void arrangeInGrid(int position, Rect outRect, int totalCount, int noOfColumns) {
        if (position >= 0) {
//            int x = position % noOfColumns;
            int y = position / noOfColumns;

//            if(x == 0){
//                outRect.left = horizontalDecorationWidth*2;
//                outRect.right = horizontalDecorationWidth;
//            } else if (x == noOfColumns-1) {
//                outRect.left = horizontalDecorationWidth;
//                outRect.right = horizontalDecorationWidth*2;
//            }else {
                outRect.left = horizontalDecorationWidth;
                outRect.right = horizontalDecorationWidth;
//            }

            if(y == 0){
                outRect.top = verticalDecorationHeight * 2;
                outRect.bottom = verticalDecorationHeight;
            } else if (y == (totalCount-1)/noOfColumns){
                outRect.top = verticalDecorationHeight;
                if (lastSpace) {
                    outRect.bottom = lastDecorationHeight;
                } else {
                    outRect.bottom = verticalDecorationHeight * 2;
                }
            } else {
                outRect.top = verticalDecorationHeight;
                outRect.bottom = verticalDecorationHeight;
            }
        }
    }
}
