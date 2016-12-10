package com.abdulfatir.concanalyzer.models;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.abdulfatir.concanalyzer.R;

import java.util.ArrayList;

/**
 * Created by abdulfatir on 10/12/16.
 */
public class SamplesListViewAdapter extends BaseAdapter {
    private final Activity ctx;
    private ArrayList<DefaultsModel> samples;


    public SamplesListViewAdapter(Activity ctx, ArrayList<DefaultsModel> samples) {
        this.samples = samples;
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return samples.size();
    }

    @Override
    public DefaultsModel getItem(int i) {
        return samples.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = ctx.getLayoutInflater();
        View row = inflater.inflate(R.layout.row_sample, viewGroup, false);
        TextView sampleNo = (TextView) row.findViewById(R.id.sampleNo);
        EditText conc = (EditText) row.findViewById(R.id.conc);
        Spinner sampleType = (Spinner) row.findViewById(R.id.sampleType);
        sampleNo.setText(String.valueOf(samples.get(i).getIndex()));
        conc.setText(String.valueOf(samples.get(i).getConcentration()));
        switch (samples.get(i).getSampleType())
        {
            case KNOWN:
                sampleType.setSelection(0);
                break;
            case QUALITY_CONTROL:
                sampleType.setSelection(1);
                break;
        }
        return row;
    }
}
