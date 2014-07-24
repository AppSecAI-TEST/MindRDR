package com.thisplace.mindrdr.view;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.thisplace.mindrdr.R;

/*
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MindReaderFragment.OnCameraFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MindReaderFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class MindReaderFragment extends Fragment {

    private static final int LINE_START_Y = 80;
    private static final int LINE_END_Y = 280;
    private static final int LINE_RANGE = LINE_END_Y - LINE_START_Y;
    private static final int INTENSITY_TRIGGER = 80;
    private static final int LINE_VALUE_MULTIPLIER = LINE_RANGE / INTENSITY_TRIGGER;
    private static final int INTENSITY_CANCEL_TRIGGER = 10;

    public static final String CONFIRM_TEXT = "confirmText";
    public static final String CANCEL_TEXT = "cancelText";


    private ImageView mLine;
    private TextView mConfirmText;
    private TextView mCancelText;
    private ImageView mTakePhoto;

    //private OnCameraFragmentInteractionListener mListener;


    public static MindReaderFragment newInstance(String confirmText, String cancelText) {
        MindReaderFragment fragment = new MindReaderFragment();
        Bundle args = new Bundle();
        args.putString(CONFIRM_TEXT, confirmText);
        args.putString(CANCEL_TEXT, cancelText);
        fragment.setArguments(args);
        return fragment;
    }
    public MindReaderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mind_reader, container, false);

        mLine = (ImageView) view.findViewById(R.id.line);
        mTakePhoto = (ImageView) view.findViewById(R.id.take_photo);
        mConfirmText = (TextView) view.findViewById(R.id.confirm_txt);
        mCancelText = (TextView) view.findViewById(R.id.cancel_txt);

        mCancelText.setText(getArguments().getString(CANCEL_TEXT));
        mConfirmText.setText(getArguments().getString(CONFIRM_TEXT));

        return view;
    }


    public void updateMindReading(int attention) {

        int value = LINE_RANGE - Math.min(LINE_RANGE, Math.round(attention * LINE_VALUE_MULTIPLIER));
        mLine.setTranslationY(LINE_START_Y + value);

    }

    public void hideActionText() {
        mConfirmText.setVisibility(View.INVISIBLE);
        mCancelText.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }



}
