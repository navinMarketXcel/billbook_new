package com.billbook.app.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.billbook.app.R;
import com.billbook.app.activities.BusinessDetailsActivity;
import com.billbook.app.activities.ContactDetailsActivity;
import com.billbook.app.activities.SplashActivity;
import com.billbook.app.activities.loginPick_activity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    RelativeLayout rlContact,rlBusiness;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        rlContact = (RelativeLayout) view.findViewById(R.id.rlContact);
        rlBusiness = (RelativeLayout) view.findViewById(R.id.rlBusiness);
        setonClick();
        return view;
    }

    public void setonClick(){
        rlContact.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ContactDetailsActivity.class);
            startActivity(intent);
        });
        rlBusiness.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BusinessDetailsActivity.class);
            startActivity(intent);
        });
    }
}