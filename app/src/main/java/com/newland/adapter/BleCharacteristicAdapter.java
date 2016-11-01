package com.newland.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.newland.bletesttool.R;
import com.newland.model.BleCharacteristicModel;

/**
 * 连接时显示的特征列表适配器
 */
public class BleCharacteristicAdapter extends BaseAdapter {

    private Context context;
    private BleCharacteristicModel[] bleCharacteristics;
    private int readIndex = -1;
    private int writeIndex = -1;

    public BleCharacteristicAdapter(Context context, BleCharacteristicModel[] bleCharacteristics) {
        this.context = context;
        this.bleCharacteristics = bleCharacteristics;
    }

    public String getReadUuid() {
        if(readIndex == -1) {
            return null;
        }
        return bleCharacteristics[readIndex].getUuid();
    }

    public String getWriteUuid() {
        if(writeIndex == -1) {
            return null;
        }
        return bleCharacteristics[writeIndex].getUuid();
    }

    private void setReadIndex(int index) {
        this.readIndex = index;
        notifyDataSetChanged();
    }

    private void setWriteIndex(int index) {
        this.writeIndex = index;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (bleCharacteristics == null) {
            return 0;
        }
        return bleCharacteristics.length;
    }

    @Override
    public Object getItem(int position) {
        return bleCharacteristics[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_setting_characteristics, null);
            holder = new ViewHolder();
            holder.tv = (TextView) view.findViewById(R.id.characteristic_description_tv);
            holder.readRb = (RadioButton) view.findViewById(R.id.read_characteristic_rb);
            holder.writeRb = (RadioButton) view.findViewById(R.id.write_characteristic_rb);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.tv.setText(bleCharacteristics[position].toString(context));
        holder.readRb.setChecked(position == readIndex);
        holder.writeRb.setChecked(position == writeIndex);
        holder.readRb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((RadioButton)view).setChecked(true);
                setReadIndex(position);
            }
        });
        holder.writeRb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((RadioButton)view).setChecked(true);
                setWriteIndex(position);
            }
        });
        return view;
    }

    class ViewHolder {
        public TextView tv;
        public RadioButton readRb;
        public RadioButton writeRb;
    }
}
