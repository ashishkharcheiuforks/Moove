package com.backdoor.moove.core.dialogs;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import com.backdoor.moove.R;
import com.backdoor.moove.core.consts.Configs;
import com.backdoor.moove.core.helper.ColorSetter;
import com.backdoor.moove.core.utils.AssetsUtil;

/**
 * About application dialog.
 */
public class AboutDialog extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ColorSetter cs = new ColorSetter(AboutDialog.this);

        setTheme(cs.getDialogStyle());
        setContentView(R.layout.about_dialog_layout);

        TextView appName = (TextView) findViewById(R.id.appName);
        appName.setTypeface(AssetsUtil.getMediumTypeface(this));
        String name = getString(R.string.app_name);
        appName.setText(name.toUpperCase());

        TextView appVersion = (TextView) findViewById(R.id.appVersion);
        appVersion.setTypeface(AssetsUtil.getThinTypeface(this));
        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            appVersion.setText(getString(R.string.version) + " " + version + " (" + Configs.CODENAME + ")");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView rights = (TextView) findViewById(R.id.rights);
        rights.setTypeface(AssetsUtil.getThinTypeface(this));
    }
}
