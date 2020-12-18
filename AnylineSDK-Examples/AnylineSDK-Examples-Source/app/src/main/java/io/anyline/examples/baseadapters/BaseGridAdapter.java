package io.anyline.examples.baseadapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.anyline.examples.R;

public class BaseGridAdapter extends RecyclerView.Adapter<BaseGridAdapter.Holder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private Context context;
    private LinkedHashMap<String, String> scanResultHashMap;

    public BaseGridAdapter(Context context, LinkedHashMap<String, String> scanResultHashMap) {

        this.context = context;
        this.scanResultHashMap = scanResultHashMap;

    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;
        if (viewType == TYPE_HEADER) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.header_type_usecase_layout, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_view_result_screen, viewGroup, false);
        }

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, final int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            bindHeaderItem(holder, position);
        } else {
            bindGridItem(holder, position);
        }
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                String result = scanResultHashMap.values().toArray()[position].toString();
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", result);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Copied: " + result, Toast.LENGTH_LONG).show();
                return true;
            }
        });
    }

    /**
     * This method is used to bind grid item value
     *
     * @param holder
     * @param position
     */
    private void bindGridItem(Holder holder, final int position) {

        View container = holder.itemView;
        String regex = "(.)*(\\d)(.)*";
        Pattern pattern = Pattern.compile(regex);

        Object dataFieldTitleResult = scanResultHashMap.keySet().toArray()[position];
        Object scanDataResult = scanResultHashMap.get(dataFieldTitleResult);

        Log.i("BGA", "sorted Data: key, value: " + dataFieldTitleResult + " " + scanDataResult);

        TextView dataFieldTitleResultTextView = container.findViewById(R.id.dataFieldTitleResult);
        TextView resultScanDataTextView = container.findViewById(R.id.resultScanData);
        ImageView receivedDataImageView = container.findViewById(R.id.result_ok_image);

        if (String.valueOf(dataFieldTitleResult).contains(context.getResources().getString(R.string.barcode_format))) {
            if (pattern.matcher(String.valueOf(dataFieldTitleResult)).matches()) {
                dataFieldTitleResult = context.getResources().getString(R.string.barcode_format);
            }
        }

        if (String.valueOf(dataFieldTitleResult).contains(context.getResources().getString(R.string.barcode_result))) {
            if (pattern.matcher(String.valueOf(dataFieldTitleResult)).matches()) {

                dataFieldTitleResult = context.getResources().getString(R.string.barcode_result);
            }

        }

        if (String.valueOf(dataFieldTitleResult).equals(context.getResources().getString(R.string.mrz_viz_date_of_expiry))) {
            dataFieldTitleResult = context.getResources().getString(R.string.mrz_viz_date_of_expiry).replace("viz ", "");
        }
        if (String.valueOf(dataFieldTitleResult).equals(context.getResources().getString(R.string.mrz_viz_dob))) {
            dataFieldTitleResult = context.getResources().getString(R.string.mrz_viz_dob).replace("viz ", "");
        }
        if (String.valueOf(dataFieldTitleResult).equals(context.getResources().getString(R.string.mrz_viz_given_names))) {
            dataFieldTitleResult = context.getResources().getString(R.string.mrz_viz_given_names).replace("viz ", "");
        }
        if (String.valueOf(dataFieldTitleResult).equals(context.getResources().getString(R.string.mrz_viz_sur_names))) {
            dataFieldTitleResult = context.getResources().getString(R.string.mrz_viz_sur_names).replace("viz ", "");
        }
        if (String.valueOf(dataFieldTitleResult).equals(context.getResources().getString(R.string.mrz_viz_address))) {
            dataFieldTitleResult = context.getResources().getString(R.string.mrz_viz_address).replace("viz ", "");
        }
        if (String.valueOf(dataFieldTitleResult).equals(context.getResources().getString(R.string.mrz_viz_issue_date))) {
            dataFieldTitleResult = context.getResources().getString(R.string.mrz_viz_issue_date).replace("viz ", "");
        }

        // for front-/backside scanning the second side will have appended " Back" to the label to avoid duplicates.
		// remove the appendix before displaying it:
        String s = String.valueOf(dataFieldTitleResult);
            if (s.startsWith("side2 ")) {
                dataFieldTitleResult = s.replace("side2 ", "");
        }
        dataFieldTitleResultTextView.setText(String.valueOf(dataFieldTitleResult));
        resultScanDataTextView.setText(String.valueOf(scanDataResult));

        if (scanDataResult != null && (scanDataResult.equals(context.getString(R.string.not_available)) || scanDataResult.equals(
                context.getString(R.string.not_valid)))) {
            receivedDataImageView.setVisibility(View.GONE);
        } else {
            receivedDataImageView.setVisibility(View.VISIBLE);
        }

    }


    /**
     * This method is used to bind the header with the corresponding item position information
     *
     * @param holder
     * @param position
     */
    private void bindHeaderItem(Holder holder, final int position) {

        List<String> indexes = new ArrayList<String>(scanResultHashMap.values());

        TextView title = (TextView) holder.itemView.findViewById(R.id.headerTitle);
        title.setText(indexes.get(position));

    }

    @Override
    public int getItemViewType(int position) {
        if (scanResultHashMap == null || scanResultHashMap.size() <= position) {
            return -1;
        }
        //String x = scanResultHashMap.get("HEADER");
        List<String> indexes = new ArrayList<String>(scanResultHashMap.keySet());

        //return ("HEADER".equals(indexes.get(position)) || "HEADER_MRZ".equals(indexes.get(position)) || ("HEADER" + ((position/3))).equals(indexes.get(position))) ? TYPE_HEADER : TYPE_ITEM;
        return indexes.get(position).startsWith("HEADER") ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return scanResultHashMap.size();
    }


    class Holder extends RecyclerView.ViewHolder {

        Holder(View itemView) {
            super(itemView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}