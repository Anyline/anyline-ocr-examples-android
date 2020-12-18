package io.anyline.examples.baseadapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.anyline.examples.R;
import io.anyline.examples.util.ShowTooltip;

public class BaseGridListAdapter extends RecyclerView.Adapter<BaseGridListAdapter.Holder> {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_LIST_ITEM = 2;
    public static final int TYPE_FULL_TILE = 3;
    private final int mDefaultSpanCount;

    protected OnItemClickListener listener;
    private Context context;
    protected String[] names;
    protected String[] classes;
    private Boolean isListView;

    public BaseGridListAdapter(Context context, String[] names, String[] classes, GridLayoutManager gridLayoutManager, int defaultSpanCount,
                               OnItemClickListener listener, Boolean isListView) {

        this.context = context;
        this.names = names;
        this.classes = classes;
        this.listener = listener;
        this.isListView = isListView;

        mDefaultSpanCount = defaultSpanCount;
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int spanCount = -1;
                if (classes != null) {
                    // if the class position is a header or a full-tile class show only 1 element in a line:
                    spanCount = TYPE_ITEM;
                    if ("HEADER".equals(classes[position])) {
                        spanCount = TYPE_HEADER;
                    } else if (isFullTileItem(names[position])) {
                        spanCount = TYPE_HEADER;
                    }
                }
                return (spanCount == TYPE_HEADER) ? mDefaultSpanCount : 1;
            }
        });
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view;

        if (viewType == TYPE_HEADER) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.header_type_layout, viewGroup, false);
        } else if (viewType == TYPE_FULL_TILE) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.full_tile_type_layout, viewGroup, false);
        } else if (isListView) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_overview, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_overview, viewGroup, false);
        }

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
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
        ConstraintLayout itemOverView = container.findViewById(R.id.item_overview);

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

    public String getClassName(int position) {
        return classes[position];
    }

    public String getItemName(int position) {
        return names[position];
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
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

        ImageView image = (ImageView) holder.itemView.findViewById(R.id.image_lock);
        title.setText(names[position]);
        if (isHeaderWithHelp(names[position])) {
            image.setVisibility(View.VISIBLE);
            image.setImageResource(R.drawable.ic_help_outline_24px);
            image.setColorFilter(context.getResources().getColor(R.color.black_50));

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) image.getLayoutParams();
            params.width = dip2px(context, 24);
            params.height = dip2px(context, 24);
            // existing height is ok as is, no need to edit it
            image.setLayoutParams(params);
        } else {
            image.setVisibility(View.GONE);
        }


        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getHeaderWithHelpIx(names[position]) >= 0) {
                    String[] headerHelpTextArray = context.getResources().getStringArray(R.array.header_help_text);
                    ShowTooltip.showTooltip(context, image, headerHelpTextArray[getHeaderWithHelpIx(names[position])]);
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (classes == null || classes.length <= position) {
            return -1;
        }
        if ("HEADER".equals(classes[position])) {
            return TYPE_HEADER;
        } else if (isFullTileItem(names[position])) {
            return TYPE_FULL_TILE;
        } else {
            return TYPE_ITEM;
        }
    }

    protected Boolean isFullTileItem(String name) {
        String[] namesArray = context.getResources().getStringArray(R.array.full_tile_document_names);
        for (int i = 0; i < namesArray.length; i++) {
            if (namesArray[i].equals(name)) {
                return true;
            }
        }
        return false;
    }


    protected Boolean isHeaderWithHelp(String headerString) {
        String[] headerWithHelpArray = context.getResources().getStringArray(R.array.header_with_help);
        for (int i = 0; i < headerWithHelpArray.length; i++) {
            if (headerWithHelpArray[i].equals(headerString)) {
                return true;
            }
        }
        return false;
    }

    protected int getHeaderWithHelpIx(String headerString) {
        String[] headerWithHelpArray = context.getResources().getStringArray(R.array.header_with_help);
        for (int i = 0; i < headerWithHelpArray.length; i++) {
            if (headerWithHelpArray[i].equals(headerString)) {
                return i;
            }
        }
        return -1;
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

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

}