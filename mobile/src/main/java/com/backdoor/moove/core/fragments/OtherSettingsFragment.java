package com.backdoor.moove.core.fragments;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backdoor.moove.R;
import com.backdoor.moove.core.consts.Configs;
import com.backdoor.moove.core.dialogs.ChangeDialog;
import com.backdoor.moove.core.dialogs.Help;
import com.backdoor.moove.core.dialogs.ThanksDialog;
import com.backdoor.moove.core.helper.Module;
import com.backdoor.moove.core.helper.Permissions;
import com.backdoor.moove.databinding.DialogAboutLayoutBinding;

import java.util.ArrayList;
import java.util.List;

public class OtherSettingsFragment extends Fragment {

    private ActionBar ab;
    private List<Item> mDataList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.settings_other, container, false);
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.other);
        }
        if (Module.isLollipop()) {
            rootView.findViewById(R.id.otherCard).setElevation(Configs.CARD_ELEVATION);
        }
        TextView about = rootView.findViewById(R.id.about);
        about.setOnClickListener(v -> showAboutDialog());

        TextView changes = rootView.findViewById(R.id.changes);
        changes.setOnClickListener(v -> getActivity().getApplicationContext()
                .startActivity(new Intent(getActivity().getApplicationContext(),
                        ChangeDialog.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));

        TextView rateApp = rootView.findViewById(R.id.rateApp);
        rateApp.setOnClickListener(v -> launchMarket(getActivity()));

        TextView thanks = rootView.findViewById(R.id.thanks);
        thanks.setOnClickListener(v -> getActivity().getApplicationContext()
                .startActivity(new Intent(getActivity().getApplicationContext(),
                        ThanksDialog.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));

        TextView help = rootView.findViewById(R.id.help);
        help.setOnClickListener(v -> getActivity().getApplicationContext()
                .startActivity(new Intent(getActivity().getApplicationContext(),
                        Help.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));

        TextView menuFeedback = rootView.findViewById(R.id.menuFeedback);
        menuFeedback.setOnClickListener(v -> {
            final Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(Intent.EXTRA_EMAIL,
                    new String[]{"feedback.cray@gmail.com"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Moove");
            getActivity().startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        });

        TextView menuShare = rootView.findViewById(R.id.menuShare);
        menuShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "https://play.google.com/store/apps/details?id=" +
                            getActivity().getPackageName());
            getActivity().startActivity(Intent.createChooser(shareIntent, "Share..."));
        });

        LinearLayout permissionBlock = rootView.findViewById(R.id.permissionBlock);
        if (Module.isMarshmallow()) {
            permissionBlock.setVisibility(View.VISIBLE);
        } else {
            permissionBlock.setVisibility(View.GONE);
        }

        TextView permissionExplain = rootView.findViewById(R.id.permissionExplain);
        permissionExplain.setOnClickListener(v -> getActivity().getApplicationContext()
                .startActivity(new Intent(getActivity().getApplicationContext(), ThanksDialog.class)
                        .putExtra("int", 1)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));

        TextView permissionEnable = rootView.findViewById(R.id.permissionEnable);
        permissionEnable.setOnClickListener(v -> showPermissionDialog());

        return rootView;
    }

    private void launchMarket(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, context.getString(R.string.could_not_launch_market), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.settings);
        }
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        DialogAboutLayoutBinding binding = DialogAboutLayoutBinding.inflate(LayoutInflater.from(getContext()));
        PackageInfo pInfo;
        try {
            pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            binding.appVersion.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        builder.setView(binding.getRoot());
        builder.create().show();
    }

    private void showPermissionDialog() {
        if (!loadDataToList()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.allow_permission);
        builder.setSingleChoiceItems(new ArrayAdapter<Item>(getContext(), android.R.layout.simple_list_item_1) {
            @Override
            public int getCount() {
                return mDataList.size();
            }

            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
                }
                TextView tvName = convertView.findViewById(android.R.id.text1);
                tvName.setText(mDataList.get(position).getTitle());
                return convertView;
            }
        }, -1, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            requestPermission(i);
        });
        builder.create().show();
    }

    private boolean loadDataToList() {
        mDataList.clear();
        if (!Permissions.checkPermission(getActivity(), Permissions.ACCESS_COARSE_LOCATION)) {
            mDataList.add(new Item(getString(R.string.course_location), Permissions.ACCESS_COARSE_LOCATION));
        }
        if (!Permissions.checkPermission(getActivity(), Permissions.ACCESS_FINE_LOCATION)) {
            mDataList.add(new Item(getString(R.string.fine_location), Permissions.ACCESS_FINE_LOCATION));
        }
        if (!Permissions.checkPermission(getActivity(), Permissions.CALL_PHONE)) {
            mDataList.add(new Item(getString(R.string.call_phone), Permissions.CALL_PHONE));
        }
        if (!Permissions.checkPermission(getActivity(), Permissions.GET_ACCOUNTS)) {
            mDataList.add(new Item(getString(R.string.get_accounts), Permissions.GET_ACCOUNTS));
        }
        if (!Permissions.checkPermission(getActivity(), Permissions.READ_PHONE_STATE)) {
            mDataList.add(new Item(getString(R.string.read_phone_state), Permissions.READ_PHONE_STATE));
        }
        if (!Permissions.checkPermission(getActivity(), Permissions.READ_CONTACTS)) {
            mDataList.add(new Item(getString(R.string.read_contacts), Permissions.READ_CONTACTS));
        }
        if (!Permissions.checkPermission(getActivity(), Permissions.READ_EXTERNAL)) {
            mDataList.add(new Item(getString(R.string.read_external_storage), Permissions.READ_EXTERNAL));
        }
        if (!Permissions.checkPermission(getActivity(), Permissions.WRITE_EXTERNAL)) {
            mDataList.add(new Item(getString(R.string.write_external_storage), Permissions.WRITE_EXTERNAL));
        }
        if (!Permissions.checkPermission(getActivity(), Permissions.SEND_SMS)) {
            mDataList.add(new Item(getString(R.string.send_sms), Permissions.SEND_SMS));
        }
        if (mDataList.size() == 0) {
            Toast.makeText(getContext(), R.string.all_permissions_are_enabled, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private void requestPermission(int position) {
        requestPermissions(new String[]{mDataList.get(position).getPermission()}, 155);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 0) return;
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showPermissionDialog();
        }
    }

    private class Item {
        private String title, permission;

        Item(String title, String permission) {
            this.permission = permission;
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public String getPermission() {
            return permission;
        }
    }
}
