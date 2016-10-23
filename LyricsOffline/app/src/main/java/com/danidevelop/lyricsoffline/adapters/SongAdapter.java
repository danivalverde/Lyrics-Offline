package com.danidevelop.lyricsoffline.adapters;

import android.app.Activity;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.danidevelop.lyricsoffline.Constants.Constants;
import com.danidevelop.lyricsoffline.LyricActivity;
import com.danidevelop.lyricsoffline.R;
import com.danidevelop.lyricsoffline.SearchActivity;
import com.danidevelop.lyricsoffline.objects.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

/**
 * Created by Dani on 22/10/2016.
 */

public class SongAdapter extends ArrayAdapter<Song> {

    boolean success = false;
    private String url;

    private int layoutID;
    private ArrayList<Song> data;
    private Song selectedSong;
    private Activity activity;
    private Context context;

    public SongAdapter(Context context, ArrayList<Song> data) {
        super(context, R.layout.item_list_song, data);
        this.data = data;
        this.layoutID = R.layout.item_list_song;
        this.context = context;
    }

    public SongAdapter(Context context, ArrayList<Song> data, int layoutID, Activity activity) {
        super(context, layoutID, data);
        this.data = data;
        this.layoutID = layoutID;
        this.context = context;
        this.activity = activity;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(this.layoutID, null);

        this.selectedSong = this.data.get(position);

        TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        TextView txtArtist = (TextView) view.findViewById(R.id.txtArtist);
        txtTitle.setText(this.selectedSong.getTitle());
        txtArtist.setText(this.selectedSong.getArtist());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedSong = data.get(position);
                if (layoutID == R.layout.item_list_song) {
                    Intent intent = new Intent(context, LyricActivity.class);
                    intent.putExtra("title", selectedSong.getTitle());
                    intent.putExtra("artist", selectedSong.getArtist());
                    intent.putExtra("lyric", selectedSong.getLyric());
                    context.startActivity(intent);
                }
                else if (layoutID == R.layout.item_list_search) {
                    url = selectedSong.getUrl();
                    GetLyricsAsyncTask task = new GetLyricsAsyncTask();
                    task.execute();
                }
            }
        });

        return view;
    }

    public void saveSong() {
        if (success) {
            SearchActivity act = (SearchActivity) this.activity;
            act.saveSong(this.selectedSong);
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.error_saving_song), Toast.LENGTH_SHORT).show();
        }
    }

    // Get Lyrics (HTTP GET)
    private class GetLyricsAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            String response = new String();

            HttpURLConnection connection;
            Map<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("url", url);

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
                URL url = new URL(Constants.URL_GET_LYRICS + "?" + urlParameters);
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

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(s);
                success = jsonObject.getBoolean(Constants.PARAMETER_SUCCESS);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (success) {
                try {
                    String lyric = jsonObject.getString("data");
                    selectedSong.setLyric(lyric);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            saveSong();
        }
    }
}
