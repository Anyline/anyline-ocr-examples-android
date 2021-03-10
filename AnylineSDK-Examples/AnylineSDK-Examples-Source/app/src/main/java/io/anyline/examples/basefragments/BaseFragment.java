package io.anyline.examples.basefragments;

/**
 * Base fragment with basics methods
 */


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.anyline.camera.CameraPermissionHelper;
import io.anyline.examples.baseadapters.BaseGridListAdapter;
import io.anyline.examples.baseadapters.EnergyAdapter;
import io.anyline.examples.R;


public class BaseFragment extends Fragment{

    private static final int DEFAULT_SPAN_COUNT = 2;
    protected RecyclerView mRecyclerView;
    protected CameraPermissionHelper cameraPermissionHelper;
    public BaseGridListAdapter mAdapter;
    private Intent targetIntent;

    private final static int PERMISSIONS_REQUEST_OPEN_CAMERA = 1;


    public BaseFragment() {
    }

    protected View inflateFragment(int resId, LayoutInflater inflater, ViewGroup container, String[] names, String[] classes, BaseGridListAdapter.OnItemClickListener onClickListener) {

        View view = inflater.inflate(resId, container, false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), DEFAULT_SPAN_COUNT);
        setupToolbar(view);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        mAdapter = new BaseGridListAdapter(this.getContext(), names, classes, gridLayoutManager, DEFAULT_SPAN_COUNT, onClickListener, false);
        mRecyclerView.setAdapter(mAdapter);
        cameraPermissionHelper = new CameraPermissionHelper(getActivity());
        return view;
    }

    protected View inflateEnergyFragment(int resId, LayoutInflater inflater, ViewGroup container, String[] names, String[] classes, BaseGridListAdapter.OnItemClickListener onClickListener) {
        View view = inflater.inflate(resId, container, false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), DEFAULT_SPAN_COUNT);
        setupToolbar(view);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        mAdapter = new EnergyAdapter(this.getContext(), names, classes, gridLayoutManager, DEFAULT_SPAN_COUNT, onClickListener);
        mRecyclerView.setAdapter(mAdapter);

        cameraPermissionHelper = new CameraPermissionHelper(getActivity());

        return view;
    }


    private void setupToolbar(View view){
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if(toolbar!=null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
    protected void replaceFragment(Fragment someFragment, String flag) {
        FragmentTransaction transaction =  getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.activity_open_translate, R.anim.fade_out,R.anim.fade_in, R.anim.activity_close_translate);
        transaction.replace(R.id.fragment_container, someFragment, flag);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    public void checkedStartActivity(Intent intent) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                               PERMISSIONS_REQUEST_OPEN_CAMERA);
            this.targetIntent = intent;
        } else {
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.activity_open_translate, R.anim.fade_out);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_OPEN_CAMERA
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(targetIntent);
            } else{
                // Displays a message to the user, asking to grant the permissions for the camera in order for Anyline to
                // work
                cameraPermissionHelper.showPermissionMessage(null);
            }
        }
}