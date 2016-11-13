package com.danidevelop.lyricsoffline.objects;

/**
 * Created by dani on 13/11/16.
 */

public class Artist {
    private String name;
    private String url;

    public Artist() {}

    public Artist(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
