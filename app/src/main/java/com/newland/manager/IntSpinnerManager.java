package com.newland.manager;

import android.app.Activity;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;

/**
 * Spinner不能直接设置整型数据，且由于Spinner中的值需要存取，为了降低与界面的耦合，因此需要添加一个适配器进行值与索引的转换
 */
public class IntSpinnerManager {

    private Activity activity;
    /** 待管理的下拉列表 */
    private Spinner spinner;

    public IntSpinnerManager(Activity activity, int viewId) {
        this.activity = activity;
        this.spinner = (Spinner) activity.findViewById(viewId);
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        spinner.setOnItemSelectedListener(listener);
    }

    public void setEnabled(boolean enabled) {
        spinner.setEnabled(enabled);
    }

    /**
     * 获取下列框指定位置的整数值
     */
    public int getSpinnerIntValue(int position) {
        if (position > 0 && position < spinner.getAdapter().getCount()) {
            try {
                return Integer.parseInt(String.valueOf(spinner.getItemAtPosition(position)));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 设置下拉框的值大于某数(筛选去小于指定值的项)
     */
    public void setSpinnerIntValueNotLessThan(int valueArrId, int minValue) {
        String[] strValues = activity.getResources().getStringArray(valueArrId);
        ArrayList<String> valueList = new ArrayList<String>();
        for (int i = 0; i < strValues.length; i++) {
            try {
                int value = Integer.parseInt(strValues[i]);
                if (value >= minValue) {
                    valueList.add(String.valueOf(value));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, valueList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
    }

    /**
     * 通过给定的值，通过就近原则来设置下拉框选中的项
     *
     * @return 最终设置的选中项的值
     */
    public int setSelectionByValue(int value) {
        int selection = -1;
        int diff = 0;
        int targetValue = 0;
        SpinnerAdapter adapter = spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            Object obj = adapter.getItem(i);
            if(obj != null) {
                try {
                    int itemValue = Integer.parseInt(String.valueOf(obj));
                    int tmpDiff = Math.abs(value - itemValue);
                    if(selection == -1 || tmpDiff < diff) {
                        selection = i;
                        diff = tmpDiff;
                        targetValue = itemValue;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        if(selection != -1) {
            spinner.setSelection(selection);
            return targetValue;
        }
        return 0;
    }

}
