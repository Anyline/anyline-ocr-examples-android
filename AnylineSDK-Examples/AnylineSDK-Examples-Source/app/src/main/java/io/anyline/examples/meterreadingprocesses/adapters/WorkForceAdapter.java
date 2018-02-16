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
import io.anyline.examples.model.Order;
import io.anyline.examples.database.DataBaseProcessesAdapter;
import io.anyline.examples.util.NavigationUtils;

public class WorkForceAdapter extends RecyclerView.Adapter<WorkForceAdapter.ItemHolder> {

    private List<Order> orderList;
    private Context mContext;
    private DataBaseProcessesAdapter dbHelper;

    public WorkForceAdapter(Context context, List<Order> orderList) {
        this.mContext = context;
        this.orderList = orderList;

    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_view_item, null);
        ItemHolder ml = new ItemHolder(v);
        try {
            dbHelper = new DataBaseProcessesAdapter(mContext);
        }catch(Exception e){}
        return ml;
    }

    @Override
    public void onBindViewHolder(final ItemHolder itemHolder, int position) {
        Order orderItem = orderList.get(position);

        itemHolder.name.setText("Order #".concat(String.valueOf(position + 1)));
        itemHolder.date.setText(String.valueOf(orderItem.getDate()));
        itemHolder.imageOrder.setImageResource(R.drawable.icon_order);
        itemHolder.count.setVisibility(View.VISIBLE);
        itemHolder.count.setText(dbHelper.completedCustomers(orderItem.getId()) + "/" + dbHelper.countCustomersByOrderId(orderItem.getId()));
    }

    @Override
    public int getItemCount() {
        return (null != orderList ? orderList.size() : 0);
    }

    public void updateDataSet(List<Order> orderList) {
        this.orderList = orderList;
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        protected TextView name, date, count;
        protected ImageView imageOrder;

        public ItemHolder(View view) {
            super(view);
            this.count = (TextView) view.findViewById(R.id.count);
            this.name = (TextView) view.findViewById(R.id.name);
            this.date = (TextView) view.findViewById(R.id.date);
            this.imageOrder = (ImageView) view.findViewById(R.id.icon);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            NavigationUtils.goToOrders(mContext, orderList.get(getAdapterPosition()));
        }

    }

}