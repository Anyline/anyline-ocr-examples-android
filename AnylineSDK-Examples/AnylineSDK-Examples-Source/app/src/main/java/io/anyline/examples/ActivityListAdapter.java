package io.anyline.examples;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import at.nineyards.anyline.util.DimensUtil;

class ActivityListAdapter extends BaseAdapter {

    public static final int TYPE_ITEM = 0;
    public static final int TYPE_HEADER = 1;
    private final String[] names;
    private final String[] classes;
    private final Context context;

    public ActivityListAdapter(Context context) {
        this.context = context;

        Resources res = context.getResources();
        names = res.getStringArray(R.array.example_activity_names);
        classes = res.getStringArray(R.array.example_activity_classes);
    }

    @Override
    public int getItemViewType(int position) {
        return "HEADER".equals(classes[position]) ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public String getItem(int position) {
        return names[position];
    }

    public String getClassName(int position) {
        return classes[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        int rowType = getItemViewType(position);

        if (convertView == null) {
            convertView = new TextView(context);
            int padding = DimensUtil.getPixFromDp(context, 16);
            switch (rowType) {
                case TYPE_ITEM:
                    ((TextView) convertView).setTextAppearance(context, android.R.style.TextAppearance_Medium);
                    convertView.setPadding(DimensUtil.getPixFromDp(context, 32), padding, padding, padding);
                    break;
                case TYPE_HEADER:
                    ((TextView) convertView).setTextAppearance(context, android.R.style.TextAppearance_Small);
                    int padding4 = DimensUtil.getPixFromDp(context, 4);
                    convertView.setPadding(padding, padding4, padding, padding4);
                    break;
            }
        }
        ((TextView) convertView).setText(getItem(position));

        return convertView;
    }


}