package io.anyline.examples.baseadapters;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import io.anyline.examples.R;

public class BaseGridListAdapter extends RecyclerView.Adapter<BaseGridListAdapter.Holder> {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;
    private final int mDefaultSpanCount;

    protected OnItemClickListener listener;
    private Context context;
    protected String[] names;
    protected String[] classes;

    public BaseGridListAdapter(Context context, String[] names, String[] classes, GridLayoutManager gridLayoutManager, int defaultSpanCount, OnItemClickListener listener) {

        this.context = context;
        this.names = names;
        this.classes = classes;
        this.listener = listener;

        mDefaultSpanCount = defaultSpanCount;
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (getItemViewType(position) == 0 ) ? mDefaultSpanCount : 1;
            }
        });
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view;

        if(viewType == TYPE_HEADER) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.header_type_layout, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_overview, viewGroup, false);
        }

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        if(getItemViewType(position) == TYPE_HEADER) {
            bindHeaderItem(holder, position);
        } else {
            bindGridItem(holder, position);
        }
    }

    /**
     * This method is used to bind grid item value
     *
     * @param holder
     * @param position
     */
    public void bindGridItem(Holder holder, final int position) {

        View container = holder.itemView;

        TextView itemText = (TextView) container.findViewById(R.id.item_overview_description);
        RelativeLayout itemOverView = (RelativeLayout) container.findViewById(R.id.item_overview);

        itemText.setText(names[position]);
        itemOverView.setVisibility(View.VISIBLE);

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null) {
                    listener.onItemClick(position);
                }
            }
        });
    }
    public String getClassName(int position) {
        return classes[position];
    }

    public String getItemName(int position) {
        return names[position];
    }

    /**
     * This method is used to bind the header with the corresponding item position information
     *
     * @param holder
     * @param position
     */
    protected void bindHeaderItem(Holder holder, final int position) {

        TextView title = (TextView) holder.itemView.findViewById(R.id.headerTitle);
        title.setText(names[position]);

    }

    @Override
    public int getItemViewType(int position) {
        if (classes == null || classes.length <= position) {
            return -1;
        }
        return "HEADER".equals(classes[position]) ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return names.length;
    }

    public class Holder extends RecyclerView.ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
}