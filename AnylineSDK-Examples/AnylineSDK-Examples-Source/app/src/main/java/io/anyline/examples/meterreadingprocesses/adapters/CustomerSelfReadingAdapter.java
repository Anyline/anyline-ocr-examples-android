package io.anyline.examples.meterreadingprocesses.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.anyline.examples.R;
import io.anyline.examples.model.Reading;
import io.anyline.examples.util.NavigationUtils;

public class CustomerSelfReadingAdapter extends RecyclerView.Adapter<CustomerSelfReadingAdapter.ItemHolder> {

    private List<Reading> readingList;
    private Context mContext;

    public CustomerSelfReadingAdapter(Context context, List<Reading> readingList) {
        this.mContext = context;
        this.readingList = readingList;

    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_view_item, null);
        ItemHolder itemHolder = new ItemHolder(v);
        return itemHolder;
    }

    @Override
    public void onBindViewHolder(final ItemHolder itemHolder, int position) {
        Reading readingIrem = readingList.get(position);

        itemHolder.name.setText("Meter Reading #" + (position + 1) );
        itemHolder.meterId.setText(String.valueOf(readingIrem.getLastReadingDate()));
        itemHolder.customerImage.setImageResource(R.drawable.icon_order);
    }

    @Override
    public int getItemCount() {
        return (null != readingList ? readingList.size() : 0);
    }

    public void updateDataSet(List<Reading> readingList) {
        this.readingList = readingList;
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        protected TextView name, meterId;
        protected ImageView customerImage;

        public ItemHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.name);
            this.meterId = (TextView) view.findViewById(R.id.date);
            this.customerImage = (ImageView) view.findViewById(R.id.icon);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
           NavigationUtils.goToCustomerSelfReading(mContext, readingList.get(getAdapterPosition()));
        }

    }

}
