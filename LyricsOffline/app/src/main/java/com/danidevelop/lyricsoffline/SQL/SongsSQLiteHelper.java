package com.danidevelop.lyricsoffline.SQL;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by Dani on 23/10/2016.
 */

public class SongsSQLiteHelper extends SQLiteOpenHelper {

    String sqlCreate = "CREATE TABLE Songs (Title TEXT, Artist TEXT, Url TEXT, Lyric TEXT)";

    public SongsSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(sqlCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Songs");
        db.execSQL(sqlCreate);
    }

}
