package com.example.klb_pda;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class CheckDT_Adapter extends ArrayAdapter<List_CheckDT> {

    private final Context context;
    private final int resource;
    private final List<List_CheckDT> objects;

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(this.resource, null);

            //ánh xạ và setTag lưu vào holder
            holder.stt = convertView.findViewById(R.id.stt);
            holder.maphieudk = convertView.findViewById(R.id.maphieudk);
            holder.hm = convertView.findViewById(R.id.hm);
            holder.soluong = convertView.findViewById(R.id.soluong);
            holder.mavl = convertView.findViewById(R.id.mavl);
            holder.tenvl = convertView.findViewById(R.id.tenvl);
            holder.quycach = convertView.findViewById(R.id.quycach);
            holder.manv = convertView.findViewById(R.id.manv);
            holder.gio = convertView.findViewById(R.id.gio);
            holder.matem = convertView.findViewById(R.id.matem);
            holder.solo = convertView.findViewById(R.id.solo);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //Trả về danh bạ hiện tại muốn vẽ
        List_CheckDT list_checkDT = this.objects.get(position);

        holder.stt.setText(list_checkDT.getXqr230b_01().toString());
        holder.maphieudk.setText(list_checkDT.getXqr230b_02());
        holder.hm.setText(list_checkDT.getXqr230b_03().toString());
        holder.soluong.setText(list_checkDT.getXqr230b_10().toString());
        holder.mavl.setText(list_checkDT.getXqr230b_05());
        holder.tenvl.setText(list_checkDT.getXqr230b_06());
        holder.quycach.setText(list_checkDT.getXqr230b_07());
        holder.manv.setText(list_checkDT.getXqr230b_09());
        holder.gio.setText(list_checkDT.getXqr230b_08());
        holder.matem.setText(list_checkDT.getXqr230b_04());
        holder.solo.setText(list_checkDT.getXqr230b_11());

        return convertView;
    }

    public class ViewHolder {
        TextView stt, maphieudk, hm, soluong, mavl, tenvl, quycach, manv, gio, matem, solo;
    }

    public CheckDT_Adapter(@NonNull Context context, int resource, @NonNull List<List_CheckDT> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.objects = objects;
    }

    @Nullable
    @Override
    public List_CheckDT getItem(int position) {
        return super.getItem(position);
    }
}
