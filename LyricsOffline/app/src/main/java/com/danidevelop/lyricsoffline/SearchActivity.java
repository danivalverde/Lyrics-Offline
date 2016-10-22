package com.danidevelop.lyricsoffline;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class SearchActivity extends AppCompatActivity {

    TextInputLayout searchWrapper;
    EditText txtSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_search);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.search_toolbar);
        myToolbar.setTitleTextColor(Color.argb(255, 255, 255, 255));
        setSupportActionBar(myToolbar);
        myToolbar.setNavigationIcon(R.drawable.ic_back);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        this.searchWrapper = (TextInputLayout) findViewById(R.id.searchWrapper);
        this.searchWrapper.setHint(getString(R.string.search_song));
        this.txtSearch = (EditText) findViewById(R.id.txtSearch);

        this.txtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    search();
                    handled = true;
                }
                return handled;
            }
        });

        // Load an ad into the AdMob banner view.
        AdView mAdView = (AdView) findViewById(R.id.adView);
        // AdView Listeners
        mAdView.setAdListener(new AdListener() {
            private void showToast(String message) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAdLoaded() {
                showToast("Ad loaded.");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                showToast(String.format("Ad failed to load with error code %d.", errorCode));
            }

            @Override
            public void onAdOpened() {
                showToast("Ad opened.");
            }

            @Override
            public void onAdClosed() {
                showToast("Ad closed.");
            }

            @Override
            public void onAdLeftApplication() {
                showToast("Ad left application.");
            }
        });
        // Load ads
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("DA739339631C84C0455858D3E8F25F7D").build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void search() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            Toast.makeText(getApplicationContext(), txtSearch.getText(), Toast.LENGTH_SHORT).show();
        }
    }
}
