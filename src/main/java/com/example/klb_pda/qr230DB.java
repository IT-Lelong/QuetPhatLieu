package com.example.klb_pda;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class qr230DB {
    public SQLiteDatabase db = null;
    String DATABASE_NAME = "qr230DB.db";
    String TABLE_NAME = "qr230_table";
    String qr230_01 = "qr230_01"; //項次 Thứ hạng
    String qr230_02 = "qr230_02"; //料號 Số vật liệu
    String qr230_03 = "qr230_03"; //規格 Thông số kỹ thuật
    String qr230_04 = "qr230_04"; //應掃數量 Số lượng nên được quét
    String qr230_05 = "qr230_05"; //已掃數量 Số lượng đã được quét
    String qr230_06 = "qr230_06"; //已驗收量 Khối lượng đã được chấp nhận
    String qr230_07 = "qr230_07"; //發出倉庫 Phát hành kho
    String qr230_08 = "qr230_08"; //發出儲位 Phát hành vị trí lưu trữ
    String qr230_09 = "qr230_09"; //發出批號 Phát hành số lô
    String qr230_10 = "qr230_10"; //物料名增 Tên mục tăng lên
    String qr230_11 = "qr230_11"; //發出日期 Ngày phát hành
    String qr230_12 = "qr230_12"; //發出時間 Thời gian phát hành

    String TABLE_NAME2 = "qr230b_table";
    String qr230b_01 = "qr230b_01"; //項次
    String qr230b_02 = "qr230b_02"; //QRcode
    String qr230b_03 = "qr230b_03"; //料號
    String qr230b_04 = "qr230b_04"; //批號
    String qr230b_05 = "qr230b_05"; //數量
    String qr230b_06 = "qr230b_06"; //驗收狀況
    String qr230b_07 = "qr230b_07"; //明細項目
    String qr230b_08 = "qr230b_08"; //Ngay quet
    String qr230b_09 = "qr230b_09"; //Nguoi quet
    String qr230b_10 = "qr230b_10"; //Ngay nhan
    String qr230b_11 = "qr230b_11"; //Nguoi nhan

    /*String TABLE_NAME_QR = "qr230qr_table";
    String qr230qr_01 = "qr230qr_01"; //stt
    String qr230qr_02 = "qr230qr_02"; //Mã đơn phát hàng
    String qr230qr_03 = "qr230qr_03"; //hạng mục
    String qr230qr_04 = "qr230qr_04"; //Mã tem
    String qr230qr_05 = "qr230qr_05"; //Mã vật liệu
    String qr230qr_06 = "qr230qr_06"; //Tên quy cách
    String qr230qr_07 = "qr230qr_07"; //Ngay quet
    String qr230qr_08 = "qr230qr_08"; //Nguoi quet
    String qr230qr_09 = "qr230qr_09"; //Số lượng*/

    String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
            qr230_01 + " INTEGER," + qr230_02 + " TEXT," + qr230_03 + " TEXT," +
            qr230_04 + " DOUBLE," + qr230_05 + " DOUBLE," + qr230_06 + " DOUBLE," +
            qr230_07 + " TEXT," + qr230_08 + " TEXT," + qr230_09 + " TEXT," +
            qr230_10 + " TEXT," + qr230_11 + " TEXT," + qr230_12 + " TEXT," + " PRIMARY KEY(qr230_01))";

    String CREATE_TABLE2 = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME2 + " (" + qr230b_01 + " INTEGER," + qr230b_02 + " TEXT," + qr230b_03 + " TEXT," +
            qr230b_04 + " TEXT," + qr230b_05 + " DOUBLE," + qr230b_06 + " TEXT," + qr230b_07 + " INTEGER," + qr230b_08 + " TEXT," + qr230b_09 + " TEXT," + qr230b_10 + " TEXT," + qr230b_11 + " TEXT)";

    private Context mCtx = null;

    public qr230DB(Context ctx) {
        this.mCtx = ctx;
    }

    public void open() throws SQLException {
        db = mCtx.openOrCreateDatabase(DATABASE_NAME, 0, null);
    }

    public void createtable() {
        try {
            db.execSQL(CREATE_TABLE);
            db.execSQL(CREATE_TABLE2);
        } catch (Exception e) {
            Toast.makeText(mCtx, e.toString(), Toast.LENGTH_SHORT).show();
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
                Integer xqr230_01 = data.getInt("IMN02");
                String xqr230_02 = data.getString("IMN03");
                String xqr230_03 = data.getString("TA_IMA021_1");
                Double xqr230_04 = data.getDouble("IMN10");
                Double xqr230_05 = data.getDouble("IMNUD07");
                Double xqr230_06 = data.getDouble("IMNUD08");
                String xqr230_07 = data.getString("IMN04");
                String xqr230_08 = data.getString("IMN05");
                String xqr230_09 = data.getString("IMN06");
                String xqr230_10 = data.getString("TA_IMA02_1");
                ContentValues args = new ContentValues();

                args.put(qr230_01, xqr230_01);
                args.put(qr230_02, xqr230_02);
                args.put(qr230_03, xqr230_03);
                args.put(qr230_04, xqr230_04);
                args.put(qr230_05, xqr230_05);
                args.put(qr230_06, xqr230_06);
                args.put(qr230_07, xqr230_07);
                args.put(qr230_08, xqr230_08);
                args.put(qr230_09, xqr230_09);
                args.put(qr230_10, xqr230_10);
                db.insert(TABLE_NAME, null, args);
            }
            for (int i = 0; i < jarray2.length(); i++) {
                JSONObject data = jarray2.getJSONObject(i);
                Integer xqr230b_01 = data.getInt("QR_IMN02");
                String xqr230b_02 = data.getString("QR_IMN03");
                String xqr230b_03 = data.getString("QR_IMN04");
                String xqr230b_04 = data.getString("QR_IMN05");
                Double xqr230b_05 = data.getDouble("QR_IMN06");
                String xqr230b_06 = data.getString("QR_IMN08");
                Integer xqr230b_07 = data.getInt("QR_IMN09");
                String xqr230b_08 = data.getString("QR_IMN12");
                String xqr230b_09 = data.getString("QR_IMN07");
                String xqr230b_10 = data.getString("QR_IMN13");
                String xqr230b_11 = data.getString("QR_IMN11");
                if (xqr230b_10.equals("NULL")){
                    xqr230b_10="";
                }
                if (xqr230b_11.equals("NULL")){
                    xqr230b_11="";
                }
                ContentValues args = new ContentValues();

                args.put(qr230b_01, xqr230b_01);
                args.put(qr230b_02, xqr230b_02);
                args.put(qr230b_03, xqr230b_03);
                args.put(qr230b_04, xqr230b_04);
                args.put(qr230b_05, xqr230b_05);
                args.put(qr230b_06, xqr230b_06);
                args.put(qr230b_07, xqr230b_07);
                args.put(qr230b_08, xqr230b_08);
                args.put(qr230b_09, xqr230b_09);
                args.put(qr230b_10, xqr230b_10);
                args.put(qr230b_11, xqr230b_11);
                db.insert(TABLE_NAME2, null, args);
            }
            return "TRUE";
        } catch (Exception e) {
            return "FALSE";
        }
    }


    //QR_code,MVL,Số Lô, Số Lượng
    public String scan(String xqr230b_02, String xqr230b_03, String xqr230b_04, Double xqr230b_05, String xqr230b_06, String xqr230b_07) {
        try {
            //確認是否有此品號
            if (xqr230b_04.equals("NULL")) {
                xqr230b_04=" ";
            }
            Cursor c_soloDonDK = db.rawQuery(" SELECT COUNT(qr230_02) count FROM " + TABLE_NAME + " " +
                    " WHERE qr230_02='" + xqr230b_03 + "' " +
                    " AND qr230_09 ='" + xqr230b_04 + "' " +
                    " ORDER BY qr230_01", null);
            c_soloDonDK.moveToFirst();
            Integer solo_count = c_soloDonDK.getInt(0);
            c_soloDonDK.close();
            if (solo_count == 0) {
                return "SAISOLO";
            }
            //}

            Cursor c = db.rawQuery("SELECT COUNT(qr230_02) count FROM " + TABLE_NAME + " WHERE qr230_02='" + xqr230b_03 + "' ORDER BY qr230_01", null);
            c.moveToFirst();
            Integer tcount = c.getInt(0);
            c.close();
            if (tcount > 0) {
                //檢查掃描數量是否超過
                Cursor c1 = db.rawQuery(" SELECT qr230_01,qr230_04,qr230_05 FROM " + TABLE_NAME +
                                " WHERE qr230_02='" + xqr230b_03 + "' AND qr230_04 > qr230_05 AND qr230_04 - qr230_05 >= " + xqr230b_05 + " ORDER BY qr230_01",
                        null);

                if (c1.getCount() == 0) {
                    return "OVERQTY";
                }

                c1.moveToFirst();
                Integer tqr230_01 = c1.getInt(0); //項次
                Double tqr230_04 = c1.getDouble(1); //應掃數量
                Double tqr230_05 = c1.getDouble(2); //已掃數量
                Integer g_qr230b_07 = 0;
                if (tqr230_04 - tqr230_05 - xqr230b_05 >= 0) {
                    //取得最大的明細項目(S)
                    String[] str_sel = new String[]{"MAX(" + qr230b_07 + ") as MAX"};
                    String selection = "qr230b_03 =? and qr230b_01 =?";
                    String[] selectionArgs = new String[]{xqr230b_03, String.valueOf(tqr230_01)};
                    Cursor g_curs = db.query(TABLE_NAME2, str_sel, selection, selectionArgs, null, null, null);
                    g_curs.moveToFirst();

                    if (g_curs.isNull(0)) {
                        g_qr230b_07 = 1;
                    } else {
                        g_qr230b_07 = g_curs.getInt(0) + 1;
                    }
                    //取得最大的明細項目(E)

                    db.execSQL("UPDATE " + TABLE_NAME + " SET qr230_05=qr230_05+" + xqr230b_05 + " WHERE qr230_01=" + tqr230_01);
                    db.execSQL("INSERT INTO " + TABLE_NAME2 + " (qr230b_01,qr230b_02,qr230b_03,qr230b_04,qr230b_05,qr230b_06,qr230b_07,qr230b_08,qr230b_09,qr230b_10,qr230b_11) " +
                            "VALUES(" + tqr230_01 + ",'" + xqr230b_02 + "','" + xqr230b_03 + "','" + xqr230b_04 + "'," + xqr230b_05 + ",'false','" + g_qr230b_07 + "','" + xqr230b_06 + "','" + xqr230b_07 + "',' ',' ')");

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

    public String delscan(String xqr230b_01, String xqr230b_04, Double xqr230b_05, String xqr230b_07) {
        try {
            Cursor c = db.rawQuery("SELECT rowid FROM " + TABLE_NAME2 + " " +
                    "WHERE qr230b_01='" + xqr230b_01 + "' AND qr230b_04='" + xqr230b_04 + "' AND qr230b_05='" + xqr230b_05 + "' AND qr230b_07 = " + xqr230b_07, null);
            c.moveToFirst();
            Integer id = c.getInt(0);
            db.execSQL("DELETE FROM " + TABLE_NAME2 + " WHERE rowid=" + id);
            db.execSQL("UPDATE " + TABLE_NAME + " SET qr230_05 = qr230_05-" + xqr230b_05 + " WHERE qr230_01='" + xqr230b_01 + "'");
            return "TRUE";
        } catch (Exception e) {
            return "FALSE";
        }
    }

    public Cursor getall() {
        try {
            return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY qr230_01", null);
        } catch (Exception e) {
            Cursor c = null;
            return c;
        }
    }

    public Cursor getallb() {
        try {
            return db.rawQuery("SELECT * FROM " + TABLE_NAME2 + " ORDER BY qr230b_01,qr230b_07", null);
        } catch (Exception e) {
            Cursor c = null;
            return c;
        }
    }

    public Cursor getdetail() {
        try {
            return db.rawQuery("SELECT rowid _id,* FROM " + TABLE_NAME + " ORDER BY " + qr230_01 + " ASC", null);
        } catch (Exception e) {
            Cursor c = null;
            return c;
        }

    }

    public Cursor getdialogdetail(String xqr230b_01) {
        try {
            return db.rawQuery("SELECT rowid _id," +
                    "(SELECT count(*) FROM qr230b_table b WHERE a.rowid >=b.rowid AND b.qr230b_01='" + xqr230b_01 + "' ) AS rownum," +
                    "qr230b_04,qr230b_05," +
                    "(case when qr230b_06 = 'true' then 'ok' else '' end) qr230b_06 ," +
                    "qr230b_02,qr230b_07,qr230b_01" +
                    " FROM " + TABLE_NAME2 + " a WHERE qr230b_01='" + xqr230b_01 + "' ORDER BY rownum  ", null);
        } catch (Exception e) {
            Cursor c = null;
            return c;
        }
    }
}