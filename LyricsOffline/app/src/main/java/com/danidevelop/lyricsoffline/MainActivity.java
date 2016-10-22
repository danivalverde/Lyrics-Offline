package com.danidevelop.lyricsoffline;

import com.danidevelop.lyricsoffline.adapters.SongAdapter;
import com.danidevelop.lyricsoffline.objects.Song;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        myToolbar.setTitleTextColor(Color.argb(255, 255, 255, 255));
        setSupportActionBar(myToolbar);

        // Layout elements
        this.listViewSongs = (ListView) findViewById(R.id.main_list_view);

        //  Test data
        ArrayList<Song> list = new ArrayList<>();
        list.add(new Song("Harder Better Faster Stronger", "Daft Punk"));
        list.add(new Song("To the master of darkness", "Ancient Bards"));
        list.add(new Song("Message in a bottle", "The Police"));
        list.add(new Song("Satisfaction", "The Rolling Stones"));
        list.add(new Song("Nothing else matters", "Metallica"));
        list.add(new Song("Harder Better Faster Stronger", "Daft Punk"));
        list.add(new Song("To the master of darkness", "Ancient Bards"));
        list.add(new Song("Message in a bottle", "The Police"));
        list.add(new Song("Satisfaction", "The Rolling Stones"));
        list.add(new Song("Nothing else matters", "Metallica"));
        SongAdapter adapter = new SongAdapter(this, list);
        this.listViewSongs.setEmptyView(findViewById(R.id.layout_empty_message));
        this.listViewSongs.setAdapter(adapter);

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
        /*// Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;*/

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
            Toast.makeText(getApplicationContext(), "Search", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

}
