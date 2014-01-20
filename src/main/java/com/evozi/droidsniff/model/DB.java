package com.evozi.droidsniff.model;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Set;

public final class DB {
    public static final String DROIDSNIFF_DBNAME = "droidsniff";

    public static final String CREATE_PREFERENCES = "CREATE TABLE IF NOT EXISTS DROIDSNIFF_PREFERENCES "
            + "(id integer primary key autoincrement, " + "name  varchar(100)," + "value varchar(100));";

    public static final String CREATE_BLACKLIST = "CREATE TABLE IF NOT EXISTS DROIDSNIFF_BLACKLIST "
            + "(id integer primary key autoincrement, " + "domain varchar(100));";

    private static Context ctx;
    private static DB db;

    public static void init(@NonNull Application app) {
        ctx = app;
        SQLiteDatabase sqldb = app.openOrCreateDatabase(DROIDSNIFF_DBNAME, Context.MODE_PRIVATE, null);
        sqldb.execSQL(CREATE_PREFERENCES);
        sqldb.execSQL(CREATE_BLACKLIST);
    }

    public static DB get() {
        if (db == null) {
            db = new DB();
        }

        return db;
    }

    public boolean getGeneric() {
        SQLiteDatabase sqldb = ctx.openOrCreateDatabase(DROIDSNIFF_DBNAME, Context.MODE_PRIVATE, null);
        Cursor cur = sqldb.rawQuery("SELECT * FROM DROIDSNIFF_PREFERENCES WHERE name = 'generic';", new String[] {});
        if (cur.moveToNext()) {
            String s = cur.getString(cur.getColumnIndex("value"));
            cur.close();
            sqldb.close();
            return Boolean.parseBoolean(s);
        } else {
            cur.close();
            sqldb.close();
            return false;
        }
    }

    public Set<String> getBlacklist() {
        Set<String> map = new HashSet<String>();
        SQLiteDatabase sqldb = ctx.openOrCreateDatabase(DROIDSNIFF_DBNAME, Context.MODE_PRIVATE, null);
        Cursor cur = sqldb.rawQuery("SELECT domain FROM DROIDSNIFF_BLACKLIST;", new String[] {});

        while (cur.moveToNext()) {
            String s = cur.getString(cur.getColumnIndex("domain"));
            map.add(s);
        }

        cur.close();
        sqldb.close();
        return map;
    }

    public void addBlacklistEntry(String name) {
        SQLiteDatabase sqldb = ctx.openOrCreateDatabase(DROIDSNIFF_DBNAME, Context.MODE_PRIVATE, null);
        sqldb.execSQL("INSERT INTO DROIDSNIFF_BLACKLIST (domain) VALUES (?);", new Object[] { name });
        sqldb.close();
    }

    public void setGeneric(boolean b) {
        SQLiteDatabase sqldb = ctx.openOrCreateDatabase(DROIDSNIFF_DBNAME, Context.MODE_PRIVATE, null);
        Cursor cur = sqldb.rawQuery("SELECT count(id) as count FROM DROIDSNIFF_PREFERENCES where name = 'generic';",
                new String[] {});
        cur.moveToFirst();
        int count = (int) cur.getLong(cur.getColumnIndex("count"));
        if (count == 0) {
            sqldb.execSQL("INSERT INTO DROIDSNIFF_PREFERENCES (name, value) values ('generic', ?);",
                    new String[] { Boolean.toString(b) });
        } else {
            sqldb.execSQL("UPDATE DROIDSNIFF_PREFERENCES SET value=? WHERE name='generic';",
                    new String[] { Boolean.toString(b) });
        }
        sqldb.close();
    }

    public void clearBlacklist() {
        SQLiteDatabase sqldb = ctx.openOrCreateDatabase(DROIDSNIFF_DBNAME, Context.MODE_PRIVATE, null);
        sqldb.execSQL("DELETE FROM DROIDSNIFF_BLACKLIST;", new Object[] {});
        sqldb.close();
    }
}
