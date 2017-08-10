package com.backdoor.moove.core.utils;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.util.Dictionary;
import java.util.Hashtable;

public class QuickReturnUtils {

    private static Dictionary<Integer, Integer> sRecyclerViewItemHeights = new Hashtable<>();

    public static int dp2px(Context context, int dp) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        DisplayMetrics displaymetrics = new DisplayMetrics();
        display.getMetrics(displaymetrics);

        return (int) (dp * displaymetrics.density + 0.5f);
    }

    public static int getScrollY(RecyclerView rv, int columnCount, boolean mIsGrid) {
        View c = rv.getChildAt(0);
        if (c == null) {
            return 0;
        }

        int firstVisiblePosition;
        if (mIsGrid) {
            try {
                StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) rv.getLayoutManager();
                int[] pos = new int[4];
                layoutManager.findFirstVisibleItemPositions(pos);
                firstVisiblePosition = pos[0];
            } catch (ClassCastException e) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) rv.getLayoutManager();
                firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
            }
        } else {
            try {
                LinearLayoutManager layoutManager = (LinearLayoutManager) rv.getLayoutManager();
                firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
            } catch (ClassCastException e) {
                StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) rv.getLayoutManager();
                int[] pos = new int[4];
                layoutManager.findFirstVisibleItemPositions(pos);
                firstVisiblePosition = pos[0];
            }
        }

        int scrollY = -(c.getTop());

        if (columnCount > 1) {
            sRecyclerViewItemHeights.put(firstVisiblePosition, c.getHeight() + QuickReturnUtils.dp2px(rv.getContext(), 8) / columnCount);
        } else {
            sRecyclerViewItemHeights.put(firstVisiblePosition, c.getHeight());
        }

        if (scrollY < 0)
            scrollY = 0;

        for (int i = 0; i < firstVisiblePosition; ++i) {
            if (sRecyclerViewItemHeights.get(i) != null) // (this is a sanity check)
                scrollY += sRecyclerViewItemHeights.get(i); //add all heights of the views that are gone
        }

        return scrollY;
    }
}
