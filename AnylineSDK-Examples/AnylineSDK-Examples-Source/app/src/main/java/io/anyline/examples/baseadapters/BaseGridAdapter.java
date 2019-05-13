package io.anyline.examples.baseadapters;

import android.content.Context;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.anyline.examples.R;
import io.anyline.examples.scanviewresult.ScanViewResultAdapter;

public class BaseGridAdapter extends RecyclerView.Adapter<BaseGridAdapter.Holder> {

	public static final int TYPE_HEADER = 0;
	public static final int TYPE_ITEM = 1;

	private Context context;
	protected HashMap<String,String> scanResultHashMap;

	public BaseGridAdapter(Context context, HashMap<String, String> scanResultHashMap) {

		this.context = context;
		this.scanResultHashMap = scanResultHashMap;

	}

	@Override
	public Holder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

		View view;

		if(viewType == TYPE_HEADER) {
			view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.header_type_usecase_layout, viewGroup, false);
		} else {
			view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_view_result_screen, viewGroup, false);
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

		Object dataFieldTitleResult = scanResultHashMap.keySet().toArray()[position];
		Object scanDataResult = scanResultHashMap.get(dataFieldTitleResult);

		TextView dataFieldTitleResultTextView = (TextView) container.findViewById(R.id.dataFieldTitleResult);
		TextView resultScanDataTextView = (TextView) container.findViewById(R.id.resultScanData);
		ImageView receivedDataImageView = (ImageView) container.findViewById(R.id.result_ok_image);

		if(String.valueOf(dataFieldTitleResult).equals(context.getResources().getString(R.string.mrz_viz_date_of_expiry))){
			dataFieldTitleResult = context.getResources().getString(R.string.mrz_viz_date_of_expiry).replace("viz ", "");
		}
		if(String.valueOf(dataFieldTitleResult).equals(context.getResources().getString(R.string.mrz_viz_dob))){
			dataFieldTitleResult = context.getResources().getString(R.string.mrz_viz_dob).replace("viz ", "");
		}
		if(String.valueOf(dataFieldTitleResult).equals(context.getResources().getString(R.string.mrz_viz_given_names))){
			dataFieldTitleResult = context.getResources().getString(R.string.mrz_viz_given_names).replace("viz ", "");
		}
		if(String.valueOf(dataFieldTitleResult).equals(context.getResources().getString(R.string.mrz_viz_sur_names))){
			dataFieldTitleResult = context.getResources().getString(R.string.mrz_viz_sur_names).replace("viz ", "");
		}
		if(String.valueOf(dataFieldTitleResult).equals(context.getResources().getString(R.string.mrz_viz_address))){
			dataFieldTitleResult = context.getResources().getString(R.string.mrz_viz_address).replace("viz ", "");
		}
		if(String.valueOf(dataFieldTitleResult).equals(context.getResources().getString(R.string.mrz_viz_issue_date))){
			dataFieldTitleResult = context.getResources().getString(R.string.mrz_viz_issue_date).replace("viz ", "");
		}

		dataFieldTitleResultTextView.setText(String.valueOf(dataFieldTitleResult));
		resultScanDataTextView.setText(String.valueOf(scanDataResult));

		if(scanDataResult != null && (scanDataResult.equals(context.getString(R.string.not_available)) || scanDataResult.equals(context.getString(R.string.not_valid)))){
			receivedDataImageView.setVisibility(View.GONE);
		}else{
			receivedDataImageView.setVisibility(View.VISIBLE);
		}

	}


	/**
	 * This method is used to bind the header with the corresponding item position information
	 *
	 * @param holder
	 * @param position
	 */
	protected void bindHeaderItem(Holder holder, final int position) {

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

		return ("HEADER".equals(indexes.get(position)) || "HEADER_MRZ".equals(indexes.get(position))) ? TYPE_HEADER : TYPE_ITEM;
	}

	@Override
	public int getItemCount() {
		return scanResultHashMap.size();
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