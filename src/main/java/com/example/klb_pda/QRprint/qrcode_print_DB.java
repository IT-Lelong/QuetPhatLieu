package com.example.klb_pda.QRprint;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class qrcode_print_DB {
    public SQLiteDatabase db = null;
    String DATABASE_NAME = "qrcode_print_DB.db";
    String TABLE_NAME = "qrb_table";
    String qrb00 = "qrb00"; //項次
    String qrb01 = "qrb01"; //料號
    String qrb02 = "qrb02"; //批號
    String qrb03 = "qrb03"; //數量
    String qrb04 = "qrb04"; //qrcode
    String qrb05 = "qrb05"; //有變更數量

    String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + qrb00 + " INTEGER," + qrb01 + " TEXT," + qrb02 + " TEXT," + qrb03 + " DOUBLE," + qrb04 + " TEXT," + qrb05 + " TEXT," + " PRIMARY KEY(qrb00))";
    private Context mCtx = null;

    public qrcode_print_DB(Context ctx) {
        this.mCtx = ctx;
    }

    public void open() throws SQLException {
        db = mCtx.openOrCreateDatabase(DATABASE_NAME, 0, null);
        try {
            db.execSQL(CREATE_TABLE);
        } catch (Exception e) {

        }
    }

    public void close() {
        try {
            String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
            db.execSQL(DROP_TABLE);
            //db.close();
        } catch (Exception e) {

        }
    }

    public void append(int xqrb00, String xqrb01, String xqrb02, Double xqrb03, String xqrb04) {
        try {
            ContentValues argsA = new ContentValues();
            argsA.put(qrb00, xqrb00);
            argsA.put(qrb01, xqrb01);
            argsA.put(qrb02, xqrb02);
            argsA.put(qrb03, xqrb03);
            argsA.put(qrb04, xqrb04);
            argsA.put(qrb05, "N");
            db.insert(TABLE_NAME, null, argsA);
        } catch (Exception e) {

        }
    }

    public Cursor getAll() {
        try {
            return db.query(TABLE_NAME, new String[]{"rowid _id", qrb00, qrb01, qrb02, qrb03, qrb04},
                    null, null, null, null, qrb00, null);
        } catch (Exception e) {
            return null;
        }

    }

    public void update(int xqrb00, Double xqrb03, String xqrb04) {
        try {
            db.execSQL("UPDATE " + TABLE_NAME +
                    " SET qrb03= " + xqrb03 + " ," +
                    " qrb04= '" + xqrb04 + "'," +
                    " qrb05= 'Y'" +
                    " WHERE qrb00=" + xqrb00);
        } catch (Exception e) {
        }
    }

    public Cursor sum_qrb03(String xqrb05) {
        //xqrb05 : ALL = 全部
        Cursor cur_sum = null;
        int res = 0;

        String[] sum_qrb03 = new String[]{"sum(" + qrb03 + ") as SUMQRB03"};
        if (!xqrb05.equals("ALL")) {
            String selection = "qrb05 =? ";
            String[] selectionArgs = new String[]{String.valueOf(xqrb05)};
            cur_sum = db.query(TABLE_NAME, sum_qrb03, selection, selectionArgs, null, null, null);
        } else {
            cur_sum = db.query(TABLE_NAME, sum_qrb03, null, null, null, null, null);
        }

        return cur_sum;
    }

    public Cursor count_qrb05(String xqrb05) {
        //xqrb05 : ALL = 全部
        Cursor cur_sum = null;
        int res = 0;

        String[] sum_qrb03 = new String[]{"COUNT(" + qrb03 + ") as COUNTQRB05"};
        if (!xqrb05.equals("ALL")) {
            String selection = "qrb05 =? ";
            String[] selectionArgs = new String[]{String.valueOf(xqrb05)};
            cur_sum = db.query(TABLE_NAME, sum_qrb03, selection, selectionArgs, null, null, null);
        } else {
            cur_sum = db.query(TABLE_NAME, sum_qrb03, null, null, null, null, null);
        }

        return cur_sum;
    }

    public void recal(Integer g_cnt, Double tt_slpb, Double slpb) {
        String code ;
        Integer g_cnt_t = 0 ;
        Double tt_slpb_t = 0.0;
        String[] str_sel = new String[]{"rowid _id", qrb00, qrb01, qrb02, qrb03, qrb04, qrb05};
        String selection = "qrb05 =? ";
        String[] selectionArgs = new String[]{"N"};
        Cursor g_curs = db.query(TABLE_NAME, str_sel, selection, selectionArgs, null, null, qrb00);


        if (g_curs.moveToFirst()) {
            do {
                try {
                    @SuppressLint("Range") String xqrb00 = g_curs.getString(g_curs.getColumnIndex("qrb00"));
                    @SuppressLint("Range") String xqrb01 = g_curs.getString(g_curs.getColumnIndex("qrb01"));
                    @SuppressLint("Range") String xqrb02 = g_curs.getString(g_curs.getColumnIndex("qrb02"));
                    g_cnt_t = g_cnt_t + 1;

                    if (g_cnt_t == g_cnt) {
                        slpb =  tt_slpb  - tt_slpb_t;
                        if(xqrb02.equals("")){xqrb02= "NULL";}
                        code = "new_" + xqrb01 + "_" + xqrb02 + "_" + slpb;
                        db.execSQL("UPDATE " + TABLE_NAME +
                                " SET qrb03= " + slpb + " ," +
                                " qrb04= '" + code + "'" +
                                " WHERE qrb00=" + xqrb00);
                    } else {
                        if(xqrb02.equals("")){xqrb02= "NULL";}
                        code = "new_" + xqrb01 + "_" + xqrb02 + "_" + slpb;
                        db.execSQL("UPDATE " + TABLE_NAME +
                                " SET qrb03= " + slpb + " ," +
                                " qrb04= '" + code + "'" +
                                " WHERE qrb00=" + xqrb00);
                    }
                    tt_slpb_t = tt_slpb_t + slpb;
                } catch (Exception e) {
                }
            } while (g_curs.moveToNext());
        }
    }
}
