package com.danidevelop.lyricsoffline;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.danidevelop.lyricsoffline.SQL.SongsSQLiteHelper;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.lang.reflect.Method;


public class LyricActivity extends AppCompatActivity {

    String title;
    String artist;
    String lyric;

    TextView txtLyric;
    TextView txtTitle;
    TextView txtArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_lyric);

        Intent intent = getIntent();
        this.title = intent.getStringExtra("title");
        this.artist = intent.getStringExtra("artist");
        this.lyric = intent.getStringExtra("lyric");

        Toolbar myToolbar = (Toolbar) findViewById(R.id.lyric_toolbar);
        myToolbar.setTitleTextColor(Color.argb(255, 255, 255, 255));
        setSupportActionBar(myToolbar);
        myToolbar.setNavigationIcon(R.drawable.ic_back);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        this.txtLyric = (TextView) findViewById(R.id.txtLyric);
        this.txtTitle = (TextView) findViewById(R.id.txtTitle);
        this.txtArtist = (TextView) findViewById(R.id.txtArtist);

        this.txtLyric.setText(this.lyric);
        this.txtTitle.setText(this.title);
        this.txtArtist.setText(this.artist);

        // Load an ad into the AdMob banner view.
        AdView mAdView = (AdView) findViewById(R.id.adView);
        // AdView Listeners
        mAdView.setAdListener(new AdListener() {
            private void showToast(String message) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAdLoaded() {
                Log.i("ADMOB", "Ad Loaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.i("ADMOB", String.format("Ad failed to load with error code %d.", errorCode));
            }

            @Override
            public void onAdOpened() {
                Log.i("ADMOB", "Ad Opened");
            }

            @Override
            public void onAdClosed() {
                Log.i("ADMOB", "Ad Closed");
            }

            @Override
            public void onAdLeftApplication() {
                Log.i("ADMOB", "Ad Left Application");
            }
        });
        // Load ads
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lyric, menu);
        String name = menu.getClass().getSimpleName();
        if (name.equals("MenuBuilder")) {
            try {
                Method m = menu.getClass().getDeclaredMethod(
                        "setOptionalIconsVisible", Boolean.TYPE);
                m.setAccessible(true);
                m.invoke(menu, true);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_delete) {

            final SongsSQLiteHelper helper = new SongsSQLiteHelper(this, "DBSongs", null, 1);


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.confirm_delete_title));
            builder.setMessage(getString(R.string.confirm_delete_message));
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    SQLiteDatabase db = helper.getWritableDatabase();
                    db.delete("Songs", "Title='" + title + "' AND Artist='" + artist + "'", null);
                    dialog.dismiss();
                    finish();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

        }

        return super.onOptionsItemSelected(item);
    }
}
