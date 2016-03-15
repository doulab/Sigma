package com.sensoft.sigma.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sensoft.sigma.R;

public class PointageFragmentActivity extends Fragment {

    public PointageFragmentActivity(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_pointage_fragment, container, false);

        return rootView;
    }
}
