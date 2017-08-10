package com.backdoor.moove.core.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.backdoor.moove.R;
import com.backdoor.moove.core.data.SpinnerItem;
import com.backdoor.moove.core.helper.Coloring;

import java.util.ArrayList;

public class TitleNavigationAdapter extends BaseAdapter {

    private ImageView imgIcon;
    private TextView txtTitle;
    private ArrayList<SpinnerItem> spinnerNavItem;
    private Context context;
    private Coloring cs;

    public TitleNavigationAdapter(Context context,
                                  ArrayList<SpinnerItem> spinnerNavItem) {
        this.spinnerNavItem = spinnerNavItem;
        this.context = context;
        cs = new Coloring(context);
    }

    @Override
    public int getCount() {
        return spinnerNavItem.size();
    }

    @Override
    public Object getItem(int index) {
        return spinnerNavItem.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.list_item_navigation, null);
        }

        imgIcon = convertView.findViewById(R.id.imgIcon);
        txtTitle = convertView.findViewById(R.id.txtTitle);

        imgIcon.setImageResource(spinnerNavItem.get(position).getIcon());
        imgIcon.setVisibility(View.GONE);
        txtTitle.setText(spinnerNavItem.get(position).getTitle());
        txtTitle.setTextColor(context.getResources().getColor(R.color.whitePrimary));
        return convertView;
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.list_item_navigation, null);
        }

        RelativeLayout itemBg = convertView.findViewById(R.id.itemBg);
        itemBg.setBackgroundColor(cs.getSpinnerStyle());

        imgIcon = convertView.findViewById(R.id.imgIcon);
        txtTitle = convertView.findViewById(R.id.txtTitle);

        imgIcon.setImageResource(spinnerNavItem.get(position).getIcon());
        txtTitle.setTextColor(context.getResources().getColor(R.color.whitePrimary));
        txtTitle.setText(spinnerNavItem.get(position).getTitle());
        return convertView;
    }
}