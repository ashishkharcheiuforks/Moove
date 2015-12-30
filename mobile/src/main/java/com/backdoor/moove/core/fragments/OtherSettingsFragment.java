package com.backdoor.moove.core.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.backdoor.moove.R;
import com.backdoor.moove.core.dialogs.AboutDialog;
import com.backdoor.moove.core.dialogs.ChangeDialog;
import com.backdoor.moove.core.dialogs.Help;
import com.backdoor.moove.core.dialogs.PermissionsList;
import com.backdoor.moove.core.dialogs.RateDialog;
import com.backdoor.moove.core.dialogs.ThanksDialog;
import com.backdoor.moove.core.helper.Module;

public class OtherSettingsFragment extends Fragment {

    private ActionBar ab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.settings_other, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.other);
        }

        TextView about = (TextView) rootView.findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                AboutDialog.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        TextView changes = (TextView) rootView.findViewById(R.id.changes);
        changes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                ChangeDialog.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        TextView rateApp = (TextView) rootView.findViewById(R.id.rateApp);
        rateApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                RateDialog.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        TextView thanks = (TextView) rootView.findViewById(R.id.thanks);
        thanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                ThanksDialog.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        TextView help = (TextView) rootView.findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                Help.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        TextView menuFeedback = (TextView) rootView.findViewById(R.id.menuFeedback);
        menuFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"feedback.cray@gmail.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Moove");
                getActivity().startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            }
        });

        TextView menuShare = (TextView) rootView.findViewById(R.id.menuShare);
        menuShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.cray.software.justreminderpro");
                getActivity().startActivity(Intent.createChooser(shareIntent, "Share..."));
            }
        });

        LinearLayout permissionBlock = (LinearLayout) rootView.findViewById(R.id.permissionBlock);
        if (Module.isMarshmallow()) permissionBlock.setVisibility(View.VISIBLE);
        else permissionBlock.setVisibility(View.GONE);

        TextView permissionExplain = (TextView) rootView.findViewById(R.id.permissionExplain);
        permissionExplain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(), ThanksDialog.class)
                                .putExtra("int", 1)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        TextView permissionEnable = (TextView) rootView.findViewById(R.id.permissionEnable);
        permissionEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(), PermissionsList.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.settings);
        }
    }
}