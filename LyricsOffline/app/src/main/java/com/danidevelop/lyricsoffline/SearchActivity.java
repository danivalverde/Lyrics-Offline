package com.danidevelop.lyricsoffline;

import com.danidevelop.lyricsoffline.Constants.Constants;
import com.danidevelop.lyricsoffline.SQL.SongsSQLiteHelper;
import com.danidevelop.lyricsoffline.adapters.SongAdapter;
import com.danidevelop.lyricsoffline.objects.Song;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    String querySong = "";
    ArrayList<Song> listSong;

    TextInputLayout searchWrapper;
    EditText txtSearch;
    ImageButton btnSearch;
    ListView searchListView;

    Document document = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_search);

        this.listSong = new ArrayList<>();

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
        this.btnSearch = (ImageButton) findViewById(R.id.btnSearch);
        this.searchListView = (ListView) findViewById(R.id.search_list_view);

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
        this.btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
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
        //AdRequest adRequest = new AdRequest.Builder().addTestDevice("DA739339631C84C0455858D3E8F25F7D").build();
        AdRequest adRequest = new AdRequest.Builder().build();
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
        if (this.txtSearch.getText().length() == 0) return;

        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            this.listSong.clear();
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            this.querySong = this.txtSearch.getText().toString();
            /*SelectSongAsyncTask task = new SelectSongAsyncTask();
            task.execute();*/

            SearchSongTask task = new SearchSongTask();
            task.execute();
        }
    }

    private void updateUI() {
        if (this.listSong.size() == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.http_call_error), Toast.LENGTH_SHORT).show();
        } else {
            SongAdapter adapter = new SongAdapter(getApplicationContext(), this.listSong, R.layout.item_list_search, this);
            this.searchListView.setAdapter(adapter);
        }

        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }

    public void saveSong(Song song) {
        SongsSQLiteHelper helper = new SongsSQLiteHelper(this, "DBSongs", null, 1);
        SQLiteDatabase db = helper.getWritableDatabase();

        if (db != null) {
            String title = song.getTitle();
            String artist = song.getArtist();
            String url = song.getUrl();
            String lyric = song.getLyric();

            ContentValues values = new ContentValues();
            values.put("Title", title);
            values.put("Artist", artist);
            values.put("Url", url);
            values.put("Lyric", lyric);

            db.insert("Songs", null, values);

            db.close();
            this.finish();
        }
    }

    // Select song (HTTP GET)
    private class SearchSongTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            try {
                String url = Constants.URL_SELECT_SONG + querySong;
                document = Jsoup.connect(url).get();
            } catch (IOException e) {
                document = null;
                e.printStackTrace();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (document != null) {
                try {
                    Elements elementsTr = document.select("div.colortable table tbody tr");
                    if (elementsTr != null) {
                        for (int iTr = 0; iTr < elementsTr.size(); iTr++) {
                            Song song = new Song();
                            Element eTr = elementsTr.get(iTr);
                            Elements elementsTd = eTr.select("td");
                            String artist = elementsTd.get(0).select("a").first().html();
                            String title = elementsTd.get(1).text();
                            String url = Constants.URL_BASE + elementsTd.get(1).select("a").get(0).attr("href");

                            artist = artist.replace("&middot;", "");
                            artist = artist.replace("·", "");
                            artist = artist.replace("&nbsp;", "");
                            if (title.toLowerCase().endsWith(" lyrics")) {
                                title = title.replace(" Lyrics", "");
                                title = title.replace(" lyrics", "");
                            }

                            song.setTitle(title);
                            song.setArtist(artist);
                            song.setUrl(url);
                            listSong.add(song);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            updateUI();
        }
    }

}
