package com.thisplace.mindrdr.view;



import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.thisplace.mindrdr.R;

public class CameraFlashFragment extends Fragment {

    private ImageView mCameraFlash;

    public static CameraFlashFragment newInstance() {
        CameraFlashFragment fragment = new CameraFlashFragment();
        return fragment;
    }
    public CameraFlashFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_camera_flash, container, false);
        mCameraFlash = (ImageView) view.findViewById(R.id.camera_flash);
        mCameraFlash.setImageAlpha(100);
        Animation cameraFlash = AnimationUtils.loadAnimation(getActivity(), R.anim.camera_flash);
        mCameraFlash.startAnimation(cameraFlash);

        return view;
    }


}
