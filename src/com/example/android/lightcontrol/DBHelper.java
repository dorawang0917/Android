package com.example.android.lightcontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Eric on 2015/1/14.
 */
public class DBHelper extends SQLiteOpenHelper {
    public final static String DATABASE_NAME = "listview_database";
    public final static int DATABASE_VERSION = 1;
    public final static String TABLE_NAME_GROUP = "GROUP_table";
    public final static String TABLE_NAME_SHORT_ADDRESS = "SHORT_ADDRESS_table";
    public final static String TABLE_NAME_DALISLAVEADDRESS = "DALISLAVEADDRESS_table";
    public final static String TABLE_NAME_DALICOMMAND = "DALICOMMAND_table";
    public final static String FEILD_ID = "_id";
    public final static String FEILD_Group = "_group";
    public final static String FEILD_Checked = "_checked";
    public final static String FEILD_SHORTADDRESS = "_short_address";
    public final static String FEILD_SHORTADDRESS_DALI = "_short_address_dali";
    public final static String FEILD_DALISLAVEADDRESS = "_dali_address";
    public final static String FEILD_NAME_DALI = "_dali_name";
    public final static String FEILD_DALICOMMAND = "_dali_command";
    public String sql_group =
            "CREATE TABLE " + TABLE_NAME_GROUP + "(" +
                    FEILD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FEILD_Group + " TEXT," +
                    FEILD_Checked + " TEXT" +
                    ")";
    public String sql_short_address =
            "CREATE TABLE " + TABLE_NAME_SHORT_ADDRESS + "(" +
                    FEILD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FEILD_SHORTADDRESS + " TEXT"+
                    ")";
    public String sql_dali_address =
            "CREATE TABLE " + TABLE_NAME_DALISLAVEADDRESS + "(" +
                    FEILD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FEILD_DALISLAVEADDRESS + " TEXT,"+
                    FEILD_SHORTADDRESS_DALI + " TEXT,"+
                    FEILD_NAME_DALI + " TEXT"+
                    ")";
    public String sql_dali_command =
            "CREATE TABLE " + TABLE_NAME_DALICOMMAND + "(" +
                    FEILD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FEILD_DALICOMMAND + " TEXT" +
                    ")";
    public SQLiteDatabase database;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(sql_group);
        db.execSQL(sql_short_address);
        db.execSQL(sql_dali_address);
        db.execSQL(sql_dali_command);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        onCreate(db);
    }

    public Cursor select(String table_name) {
        return database.query(table_name, null, null, null, null, null, null);
    }


    public void update(int id, String table_name, String field_name, String text) {
        ContentValues values = new ContentValues();
        values.put(field_name, text);
        database.update(table_name, values, FEILD_ID + "=" + Integer.toString(id), null);
    }
    public void delete(int id, String table_name, String field_name, String text) {
        ContentValues values = new ContentValues();
        values.put(field_name, text);
        database.delete(table_name, FEILD_ID + "=" + Integer.toString(id), null);
    }

    public void insert(String table_name, String field_name, String text) {
        ContentValues values = new ContentValues();
        values.put(field_name, text);
        database.insert(table_name, null, values);
    }

    public void close() {
        database.close();
    }
}



























