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
import io.anyline.examples.model.Customer;
import io.anyline.examples.util.NavigationUtils;

public class WorkOrderAdapter extends RecyclerView.Adapter<WorkOrderAdapter.ItemHolder> {

    private final static int IS_COMPLETED = 1;

    private List<Customer> customerList;
    private Context mContext;

    public WorkOrderAdapter(Context context, List<Customer> customerList) {
        this.mContext = context;
        this.customerList = customerList;

    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_view_item, null);
        ItemHolder itemHolder = new ItemHolder(v);
        return itemHolder;
    }

    @Override
    public void onBindViewHolder(final ItemHolder itemHolder, int position) {
        Customer customerItem = customerList.get(position);

        itemHolder.name.setText(customerItem.getName());
        itemHolder.meterId.setText("#".concat(String.valueOf(customerItem.getMeterId())));
        itemHolder.customerImage.setImageResource(R.drawable.icon_hollow_person);
        if(customerItem.getIsCompleted() == IS_COMPLETED){
            itemHolder.syncImage.setVisibility(View.VISIBLE);
            itemHolder.syncImage.setImageResource(R.drawable.blue_round_checkmark);
        }else{
            itemHolder.syncImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return (null != customerList ? customerList.size() : 0);
    }

    public void updateDataSet(List<Customer> customerList) {
        this.customerList = customerList;
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        protected TextView name, meterId;
        protected ImageView customerImage;
        protected ImageView syncImage;

        public ItemHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.name);
            this.meterId = (TextView) view.findViewById(R.id.date);
            this.customerImage = (ImageView) view.findViewById(R.id.icon);
            this.syncImage = (ImageView) view.findViewById(R.id.sync_image);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            NavigationUtils.goToEditCustomer(mContext, customerList.get(getAdapterPosition()));
        }

    }

}