package com.danidevelop.lyricsoffline;

import com.danidevelop.lyricsoffline.SQL.SongsSQLiteHelper;
import com.danidevelop.lyricsoffline.adapters.SongAdapter;
import com.danidevelop.lyricsoffline.objects.Song;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.ActivityOptions;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listViewSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        myToolbar.setTitleTextColor(Color.argb(255, 255, 255, 255));
        setSupportActionBar(myToolbar);

        this.listViewSongs = (ListView) findViewById(R.id.main_list_view);

        this.loadSavedLyrics();

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
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("DA739339631C84C0455858D3E8F25F7D").build();
        //AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        this.loadSavedLyrics();
    }

    private void loadSavedLyrics() {
        SongsSQLiteHelper helper = new SongsSQLiteHelper(this, "DBSongs", null, 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Songs", null);
        ArrayList<Song> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            Song song = new Song();
            song.setTitle(cursor.getString(0));
            song.setArtist(cursor.getString(1));
            song.setUrl(cursor.getString(2));
            song.setLyric(cursor.getString(3));
            list.add(song);
        }
        SongAdapter adapter = new SongAdapter(this, list);
        this.listViewSongs.setEmptyView(findViewById(R.id.layout_empty_message));
        this.listViewSongs.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        if (id == R.id.action_search) {
            //Toast.makeText(getApplicationContext(), "Search", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}
