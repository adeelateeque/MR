package com.codurs.meetroulette.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.codurs.meetroulette.R;

/**
 * Created by Adeel on 6/7/14.
 */
public class ChoiceFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choice, container, false);
    }
}