package com.thisplace.mindrdr.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.thisplace.mindrdr.R;

public class UploadingFragment extends Fragment {

    ProgressBar mProgressSpinner;
    FrameLayout mBlueCover;
    LinearLayout mSuccessView;


        public static UploadingFragment newInstance() {
        UploadingFragment fragment = new UploadingFragment();
        return fragment;
    }
    public UploadingFragment() {
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
        View view = inflater.inflate(R.layout.fragment_uploading, container, false);

        mProgressSpinner = (ProgressBar) view.findViewById(R.id.progress_spinner);
        mBlueCover = (FrameLayout) view.findViewById(R.id.blue_cover);
        mSuccessView = (LinearLayout) view.findViewById(R.id.success);

        mBlueCover.setTranslationY(360);

        AnimatorSet set = new AnimatorSet();
        ObjectAnimator animation = ObjectAnimator.ofFloat(mBlueCover, "translationY", 0);
        animation.setDuration(1000);

        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

            }
        });
        set.play(animation);
        set.start();

        return view;
    }


    public void showSuccess() {
        mProgressSpinner.setVisibility(View.INVISIBLE);
        mSuccessView.setVisibility(View.VISIBLE);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


}
