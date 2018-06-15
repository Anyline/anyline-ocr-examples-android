package io.anyline.examples.baseadapters;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import io.anyline.examples.R;
import io.anyline.examples.baseadapters.BaseGridListAdapter;

/**
 * Created by lorena on .12.17.
 */

public class EnergyAdapter extends BaseGridListAdapter {
    Context ctx;
    public EnergyAdapter(Context context, String[] names, String[] classes, GridLayoutManager gridLayoutManager,
                         int defaultSpanCount, OnItemClickListener listener) {
        super(context, names, classes, gridLayoutManager, defaultSpanCount, listener);
        this.ctx = context;
    }

    @Override
    public void bindGridItem(Holder holder, final int position) {

        View container = holder.itemView;
        TextView itemText = (TextView) container.findViewById(R.id.item_overview_description);
        RelativeLayout itemOverView = (RelativeLayout) container.findViewById(R.id.item_overview);

        itemText.setText(names[position]);
        itemOverView.setVisibility(View.VISIBLE);
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onItemClick(position);
                }
            }
        });
    }

    @Override
    protected void bindHeaderItem(Holder holder, final int position) {

        View container = holder.itemView;
        TextView title = (TextView) holder.itemView.findViewById(R.id.headerTitle);
        title.setText(names[position]);


        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null) {
                    listener.onItemClick(position);
                }
            }
        });
    }
}
