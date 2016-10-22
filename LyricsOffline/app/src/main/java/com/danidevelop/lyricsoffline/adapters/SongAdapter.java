package com.danidevelop.lyricsoffline.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.danidevelop.lyricsoffline.R;
import com.danidevelop.lyricsoffline.objects.Song;

import java.util.ArrayList;

/**
 * Created by Dani on 22/10/2016.
 */

public class SongAdapter extends ArrayAdapter<Song> {

    private ArrayList<Song> data;
    private Song selectedSong;

    public SongAdapter(Context context, ArrayList<Song> data) {
        super(context, R.layout.item_list_song, data);
        this.data = data;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.item_list_song, null);

        this.selectedSong = this.data.get(position);

        TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        TextView txtArtist = (TextView) view.findViewById(R.id.txtArtist);
        txtTitle.setText(this.selectedSong.getTitle());
        txtArtist.setText(this.selectedSong.getArtist());

        return view;
    }
}
