package io.anyline.examples.scanviewresult;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import io.anyline.examples.R;

public class ScanViewResultAdapter extends RecyclerView.Adapter<ScanViewResultAdapter.ItemHolder> {

    private HashMap<String, String> scanResultHashMap;
    private Context mContext;

    public ScanViewResultAdapter(Context context, HashMap<String, String> scanResultHashMap) {
        this.mContext = context;
        this.scanResultHashMap = scanResultHashMap;

    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_view_result_screen, null);
        ItemHolder itemHolder = new ItemHolder(view);
        return itemHolder;
    }

    @Override
    public void onBindViewHolder(final ItemHolder itemHolder, int position) {
        Object dataFieldTitleResult = scanResultHashMap.keySet().toArray()[position];
        Object scanDataResult = scanResultHashMap.get(dataFieldTitleResult);

        itemHolder.dataFieldTitleResultTextView.setText(String.valueOf(dataFieldTitleResult));
        itemHolder.resultScanDataTextView.setText(String.valueOf(scanDataResult));

        if(scanDataResult.equals(mContext.getString(R.string.not_available)) || scanDataResult.equals(mContext.getString(R.string.not_valid))){
            itemHolder.receivedDataImageView.setVisibility(View.GONE);
        }else{
            itemHolder.receivedDataImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return (null != scanResultHashMap ? scanResultHashMap.size() : 0);
    }

    public void updateDataSet(HashMap<String, String> scanResultHashMap) {
        this.scanResultHashMap = scanResultHashMap;
    }

    public class ItemHolder extends RecyclerView.ViewHolder {


        protected TextView dataFieldTitleResultTextView, resultScanDataTextView ;
        protected ImageView receivedDataImageView;

        public ItemHolder(View view) {

            super(view);
            this.dataFieldTitleResultTextView = (TextView) view.findViewById(R.id.dataFieldTitleResult);
            this.resultScanDataTextView = (TextView) view.findViewById(R.id.resultScanData);
            this.receivedDataImageView = (ImageView) view.findViewById(R.id.result_ok_image);

        }

    }

}