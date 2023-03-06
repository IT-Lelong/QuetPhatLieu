package com.example.klb_pda;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

public class qr210DB {
    public SQLiteDatabase db = null;
    String DATABASE_NAME = "qr210DB.db";
    String TABLE_NAME = "qr210_table";
    String qr210_00 = "qr210_00"; //類型
    String qr210_01 = "qr210_01"; //品號
    String qr210_02 = "qr210_02"; //規格
    String qr210_03 = "qr210_03"; //應發數量
    String qr210_04 = "qr210_04"; //已掃數量
    String qr210_05 = "qr210_05"; //已驗收量

    String TABLE_NAME2 = "qr210b_table";
    String qr210b_00 = "qr210b_00"; //條碼
    String qr210b_01 = "qr210b_01"; //品號
    String qr210b_02 = "qr210b_02"; //批號
    String qr210b_03 = "qr210b_03"; //數量
    String qr210b_04 = "qr210b_04"; //驗收狀況

    String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + qr210_00 + " TEXT," + qr210_01 + " TEXT," + qr210_02 + " TEXT," + qr210_03 + " INTEGER," + qr210_04 + " INTEGER," + qr210_05 + " INTEGER," + " PRIMARY KEY(qr210_00,qr210_01))";
    String CREATE_TABLE2 = "CREATE TABLE " + TABLE_NAME2 + " (" + qr210b_00 + " TEXT," + qr210b_01 + " TEXT," + qr210b_02 + " TEXT," + qr210b_03 + " INTEGER," + qr210b_04 + " TEXT)";
    private Context mCtx = null;

    public qr210DB(Context ctx) {
        this.mCtx = ctx;
    }

    public void open() throws SQLException {
        db = mCtx.openOrCreateDatabase(DATABASE_NAME, 0, null);
        try {
            db.execSQL(CREATE_TABLE);
            db.execSQL(CREATE_TABLE2);
        } catch (Exception e) {

        }
    }

    public void close() {
        try {
            String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
            String DROP_TABLE2 = "DROP TABLE IF EXISTS " + TABLE_NAME2;
            db.execSQL(DROP_TABLE);
            db.execSQL(DROP_TABLE2);
            db.close();
        } catch (Exception e) {

        }
    }

    public String append(JSONObject result) {
        try {
            JSONArray jarray1 = result.getJSONArray("detail1");
            JSONArray jarray2 = result.getJSONArray("detail2");
            for (int i = 0; i < jarray1.length(); i++) {
                JSONObject data = jarray1.getJSONObject(i);
                String xqr210_00 = data.getString("QR_PTT02");
                String xqr210_01 = data.getString("QR_PTT03");
                String xqr210_02 = data.getString("IMA021");
                Integer xqr210_03 = data.getInt("QR_PTT04");
                Integer xqr210_04 = data.getInt("QR_PTT05");
                Integer xqr210_05 = data.getInt("QR_PTT07");
                ContentValues args = new ContentValues();
                args.put(qr210_00, xqr210_00);
                args.put(qr210_01, xqr210_01);
                args.put(qr210_02, xqr210_02);
                args.put(qr210_03, xqr210_03);
                args.put(qr210_04, xqr210_04);
                args.put(qr210_05, xqr210_05);
                db.insert(TABLE_NAME, null, args);
            }
            for (int i = 0; i < jarray2.length(); i++) {
                JSONObject data = jarray2.getJSONObject(i);
                String xqr210b_00 = data.getString("QR_PTU02");
                String xqr210b_01 = data.getString("QR_PTU03");
                String xqr210b_02 = data.getString("QR_PTU04");
                Integer xqr210b_03 = data.getInt("QR_PTU05");
                String xqr210b_04 = data.getString("QR_PTU07");

                ContentValues args = new ContentValues();
                args.put(qr210b_00, xqr210b_00);
                args.put(qr210b_01, xqr210b_01);
                args.put(qr210b_02, xqr210b_02);
                args.put(qr210b_03, xqr210b_03);
                args.put(qr210b_04, xqr210b_04);
                db.insert(TABLE_NAME2, null, args);
            }
            return "TRUE";
        } catch (Exception e) {
            return "FALSE";
        }
    }

    public Cursor getdetail(String type) {
        try {
            return db.rawQuery("SELECT rowid _id,(SELECT count(*) FROM qr210_table b WHERE a.rowid >=b.rowid AND b.qr210_00=" + type + " ) AS rownum,* FROM " + TABLE_NAME + " a WHERE qr210_00=" + type + " ORDER BY " + qr210_01 + " ASC", null);
        } catch (Exception e) {
            Cursor c = null;
            return c;
        }

    }

    public Cursor getdialogdetail(String xqr210b_01) {
        try {
            return db.rawQuery("SELECT rowid _id,(SELECT count(*) FROM qr210b_table b WHERE a.rowid >=b.rowid AND b.qr210b_01='" + xqr210b_01 + "' ) AS rownum,qr210b_02,qr210b_03,(case when qr210b_04 = 'true' then 'ok' else '' end) qr210b_04  FROM "
                    + TABLE_NAME2 + " a WHERE qr210b_01='" + xqr210b_01 + "' ORDER BY rowid," + qr210b_02 + " ASC", null);
        } catch (Exception e) {
            Cursor c = null;
            return c;
        }
    }

    public Cursor getall() {
        try {
            return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY qr210_00,qr210_01", null);
        } catch (Exception e) {
            Cursor c = null;
            return c;
        }
    }

    public Cursor getallb() {
        try {
            return db.rawQuery("SELECT * FROM " + TABLE_NAME2 + " ORDER BY qr210b_01 ASC,qr210b_02 ASC", null);
        } catch (Exception e) {
            Cursor c = null;
            return c;
        }
    }

    public String delscan(String xqr210b_01, String xqr210b_02, Integer xqr210b_03) {
        try {
            Cursor c = db.rawQuery("SELECT rowid FROM " + TABLE_NAME2 + " WHERE qr210b_01='" + xqr210b_01 + "' AND qr210b_02='" + xqr210b_02 + "' AND qr210b_03=" + xqr210b_03, null);
            c.moveToFirst();
            Integer id = c.getInt(0);
            db.execSQL("DELETE FROM " + TABLE_NAME2 + " WHERE rowid=" + id);
            db.execSQL("UPDATE " + TABLE_NAME + " SET qr210_04=qr210_04-" + xqr210b_03 + " WHERE qr210_01='" + xqr210b_01 + "'");
            return "TRUE";
        } catch (Exception e) {
            return "FALSE";
        }
    }

    public String scan(String xqr210b_00, String xqr210b_01, String xqr210b_02, Integer xqr210b_03) {
        try {
            //確認是否有此品號
            Cursor c = db.rawQuery("SELECT qr210_03,qr210_04 FROM " + TABLE_NAME + " WHERE qr210_01='" + xqr210b_01 + "'", null);
            c.moveToFirst();
            Integer tqr210_03 = c.getInt(0);
            Integer tqr210_04 = c.getInt(1);
            if (tqr210_03 > 0) {
                //檢查掃描數量是否超過
                if (tqr210_03 - tqr210_04 - xqr210b_03 >= 0) {
                    db.execSQL("UPDATE " + TABLE_NAME + " SET qr210_04=qr210_04+" + xqr210b_03 + " WHERE qr210_01='" + xqr210b_01 + "'");
                    db.execSQL("INSERT INTO " + TABLE_NAME2 + " (qr210b_00,qr210b_01,qr210b_02,qr210b_03,qr210b_04) " +
                            "VALUES('" + xqr210b_00 + "','" + xqr210b_01 + "','" + xqr210b_02 + "'," + xqr210b_03 + ",'false')   ");
                    return "TRUE";
                } else {
                    return "OVERQTY";
                }
            } else {
                return "NORECORD";
            }

        } catch (Exception e) {
            return "FALSE";
        }
    }
}
