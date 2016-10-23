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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    boolean success = false;
    String querySong = "";
    ArrayList<Song> listSong;

    TextInputLayout searchWrapper;
    EditText txtSearch;
    ImageButton btnSearch;
    ListView searchListView;

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
        if (this.txtSearch.getText().length() == 0) return;

        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            this.listSong.clear();
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            this.querySong = this.txtSearch.getText().toString();
            SelectSongAsyncTask task = new SelectSongAsyncTask();
            task.execute();
        }
    }

    private void updateUI() {
        if (!this.success) {
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
    private class SelectSongAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            String response = new String();

            HttpURLConnection connection;
            Map<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("song", querySong);

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : parameters.entrySet()) {
                try {
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                postData.append('&');
            }
            String urlParameters = postData.toString();

            try {
                URL url = new URL(Constants.URL_SELECT_SONG + "?" + urlParameters);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setReadTimeout(Constants.MILIS_TIMEOUT);
                connection.setConnectTimeout(Constants.MILIS_TIMEOUT);
                connection.setRequestMethod("POST");

                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                out.write(urlParameters);
                out.flush();

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    response += line;
                }
                in.close();
                out.close();

            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                success = false;
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }

            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            listSong.clear();

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(s);
                success = jsonObject.getBoolean(Constants.PARAMETER_SUCCESS);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (success) {
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObjectSong = (JSONObject) jsonArray.get(i);
                        Song song = new Song();
                        song.setArtist(jsonObjectSong.getString("artist"));
                        song.setTitle(jsonObjectSong.getString("song"));
                        song.setUrl(jsonObjectSong.getString("url"));
                        listSong.add(song);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            updateUI();
        }
    }
}
