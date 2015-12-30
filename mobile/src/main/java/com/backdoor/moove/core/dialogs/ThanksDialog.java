
package com.backdoor.moove.core.dialogs;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;

import com.backdoor.moove.R;
import com.backdoor.moove.core.helper.Coloring;
import com.backdoor.moove.core.views.FloatingEditText;

/**
 * Show all open source libraries used in project.
 */
public final class ThanksDialog extends AppCompatActivity {

    /**
     * Helper method initialization.
     */
    private Coloring cSetter = new Coloring(ThanksDialog.this);

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorPrimaryDark());
        }
        setContentView(R.layout.help_layout);

        int code = getIntent().getIntExtra("int", 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.ic_security_white_24dp);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowTitleEnabled(true);
            bar.setTitle(getString(R.string.open_source_licenses));
            if (code == 1) {
                bar.setTitle(getString(R.string.permissions));
            }
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
        }

        WebView helpView = (WebView) findViewById(R.id.helpView);
        String url = "file:///android_asset/LICENSE.html";
        if (code == 1) {
            url = "file:///android_asset/permissions.html";
        }
        helpView.loadUrl(url);

        FloatingEditText searchEdit = (FloatingEditText) findViewById(R.id.searchEdit);
        searchEdit.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}