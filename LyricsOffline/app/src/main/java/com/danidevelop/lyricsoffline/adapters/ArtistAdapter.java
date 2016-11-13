package com.danidevelop.lyricsoffline.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.danidevelop.lyricsoffline.Constants.Constants;
import com.danidevelop.lyricsoffline.R;
import com.danidevelop.lyricsoffline.objects.Artist;
import com.danidevelop.lyricsoffline.objects.Song;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by dani on 13/11/16.
 */

public class ArtistAdapter extends ArrayAdapter<Artist> {

    private ArrayList<Artist> data;
    private Context context;
    private Artist selectedArtist;
    private Document document = null;
    private boolean success = false;
    private ArrayList<Song> songList;

    public ArtistAdapter(Context context, ArrayList<Artist> data) {
        super(context, R.layout.item_list_search_artist, data);
        this.data = data;
        this.context = context;
    }

    @NonNull
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.item_list_search_artist, null);

        this.selectedArtist = this.data.get(position);

        TextView txtArtistName = (TextView) view.findViewById(R.id.txtArtistName);
        txtArtistName.setText(this.selectedArtist.getName());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedArtist = data.get(position);
                Toast.makeText(view.getContext(), "Selected: "+selectedArtist.getName(), Toast.LENGTH_SHORT).show();
                Log.i("DANI_DEBUG", "Name: "+selectedArtist.getName()+" URL: "+selectedArtist.getUrl());
            }
        });

        return view;
    }

    private void showListSongs() {
        if(success) {

        }
        else {
            Toast.makeText(getContext(), getContext().getString(R.string.error_getting_songs), Toast.LENGTH_SHORT).show();
        }
    }

    private class GetArtistSongsAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                String url = selectedArtist.getUrl();
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

            success = false;
            if (document != null) {
                try {
                    Elements elementsTr = document.select("div.maincont div.colortable table tbody tr");
                    if (elementsTr != null) {
                        for (int iTr = 0; iTr < elementsTr.size(); iTr++) {
                            Song song = new Song();
                            song.setArtist(selectedArtist.getName());

                            Elements elementsTd = elementsTr.get(iTr).select("td");
                            String title = elementsTd.get(0).select("a").first().html();
                            String url = Constants.URL_BASE + elementsTd.get(0).select("a").get(0).attr("href");

                            title = title.replace("&middot;", "");
                            title = title.replace("·", "");
                            title = title.replace("&nbsp;", "");
                            title = title.replace("&amp;", "&");
                            if (title.toLowerCase().endsWith(" lyrics")) {
                                title = title.replace(" Lyrics", "");
                                title = title.replace(" lyrics", "");
                            }

                            song.setUrl(url);
                            song.setTitle(title);
                            songList.add(song);
                        }
                    }

                    success = true;
                } catch (Exception e) {
                    success = false;
                    e.printStackTrace();
                }
            }

            showListSongs();
        }
    }
}
