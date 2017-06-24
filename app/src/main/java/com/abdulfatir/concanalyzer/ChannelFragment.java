package com.abdulfatir.concanalyzer;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abdulfatir.concanalyzer.models.SampleModel;
import com.abdulfatir.concanalyzer.util.LinearFunction;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by abdulfatir on 24/06/17.
 */
public class ChannelFragment extends Fragment {


    private static final String CHANNEL_KEY = "CHANNEL";
    private static final String KNOWN_SAMPLES_KEY = "KNOWN_SAMPLES";
    private static final String UNKNOWN_SAMPLES_KEY = "UNKNOWN_SAMPLES";
    private static final String QC_SAMPLES_KEY = "QC_SAMPLES";

    private int mChannel;

    private double mSlope;
    private double mIntercept;
    private double mR2Score;
    private LinearFunction mStandardCurve;
    private CombinedChart mChart;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_channel, container, false);
        Bundle bundle = getArguments();
        mChannel = bundle.getInt(CHANNEL_KEY);

        mSlope = ((ResultActivity)getActivity()).getSlope(mChannel);
        mIntercept = ((ResultActivity)getActivity()).getIntercept(mChannel);
        mR2Score = ((ResultActivity)getActivity()).getRSquared(mChannel);

        ArrayList<SampleModel> knownSamples = bundle.getParcelableArrayList(KNOWN_SAMPLES_KEY);
        ArrayList<SampleModel> unKnownSamples = bundle.getParcelableArrayList(UNKNOWN_SAMPLES_KEY);
        ArrayList<SampleModel> qcSamples = bundle.getParcelableArrayList(QC_SAMPLES_KEY);


        TextView eqTv = (TextView) rootView.findViewById(R.id.equation);
        eqTv.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC);
        LinearLayout mainContent = (LinearLayout) rootView.findViewById(R.id.content);
        mStandardCurve = new LinearFunction(mIntercept, mSlope);
        eqTv.setText(mStandardCurve.toString());
        for (int i = 0; i < qcSamples.size(); i++)
            mainContent.addView(addSubView(mainContent, qcSamples.get(i), "Quality Control"));
        for (int i = 0; i < unKnownSamples.size(); i++)
            mainContent.addView(addSubView(mainContent, unKnownSamples.get(i), "Unknown"));
        mChart = (CombinedChart) rootView.findViewById(R.id.graph);
        mChart.setDescription("");
        mChart.setNoDataTextDescription("No data to display.");
        mChart.setTouchEnabled(true);
        mChart.setDragDecelerationFrictionCoef(0.9f);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setHighlightPerDragEnabled(true);
        mChart.setPinchZoom(true);
        mChart.setBackgroundColor(Color.WHITE);
        mChart.getAxisRight().setEnabled(false);
        XAxis mXaxis = mChart.getXAxis();
        mXaxis.setDrawGridLines(false);
        mXaxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        mXaxis.setAxisMinValue(0);
        mXaxis.setGranularity(1f);
        Legend l = mChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_INSIDE);
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaxValue(255f);
        leftAxis.setAxisMinValue(0f);
        leftAxis.setDrawGridLines(false);
        CombinedData cData = new CombinedData();
        ScatterData scatterData = new ScatterData();
        scatterData.addDataSet(generateKnownScatterData(knownSamples));
        scatterData.addDataSet(generateUnKnownScatterData(unKnownSamples));
        scatterData.addDataSet(generateQCScatterData(qcSamples));
        cData.setData(generateStandardCurveData());
        cData.setData(scatterData);
        mChart.setData(cData);
        mChart.invalidate();

        return rootView;
    }


    private View addSubView(ViewGroup mainContent, SampleModel sampleModel, String s) {
        double intensity = sampleModel.getIntensities()[mChannel];
        double concentration = (intensity - mIntercept)/ mSlope;
        View layout = LayoutInflater.from(getActivity()).inflate(R.layout.layout_quality_control, mainContent, false);
        TextView titleTV = (TextView) layout.findViewById(R.id.textView2);
        TextView concTV = (TextView) layout.findViewById(R.id.textView3);
        TextView errorTV = (TextView) layout.findViewById(R.id.textView4);
        titleTV.setText(s);
        concTV.setText(String.format(Locale.getDefault(), "%s%.2f ng/mL", "Concentration: ", concentration));
        if (sampleModel.getDataPointType() == SampleModel.DataPointType.QUALITY_CONTROL) {
            double error = Math.abs(concentration - sampleModel.getConcentration()) / sampleModel.getConcentration();
            error *= 100;
            if (error > 20)
                Toast.makeText(getActivity(), "Error is greater than 20%", Toast.LENGTH_SHORT).show();
            errorTV.setText(String.format(Locale.getDefault(), "%s%.1f%%", "Error: ", error));
        } else if (sampleModel.getDataPointType() == SampleModel.DataPointType.UNKNOWN) {
            errorTV.setText(String.format(Locale.getDefault(), "%s%.4f", "R2 Score: ", mR2Score));
        }
        return layout;

    }

    private ScatterDataSet generateQCScatterData(ArrayList<SampleModel> dataPoints) {
        return generateCalculatedData(dataPoints, "Quality Control", 0xff00a000);
    }

    private ScatterDataSet generateKnownScatterData(ArrayList<SampleModel> dataPoints) {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < dataPoints.size(); i++) {
            SampleModel sampleModel = dataPoints.get(i);
            entries.add(new Entry((float) sampleModel.getConcentration(), (float) sampleModel.getIntensities()[mChannel]));
        }
        ScatterDataSet set = new ScatterDataSet(entries, "Known");
        set.setColor(Color.GRAY);
        set.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        set.setScatterShapeSize(7.5f);
        set.setDrawValues(false);
        set.setValueTextSize(10f);
        return set;
    }

    private ScatterDataSet generateUnKnownScatterData(ArrayList<SampleModel> dataPoints) {
        return generateCalculatedData(dataPoints, "Unknown", Color.BLUE);
    }

    private ScatterDataSet generateCalculatedData(ArrayList<SampleModel> dataPoints, String label, int color) {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < dataPoints.size(); i++) {
            SampleModel sampleModel = dataPoints.get(i);
            double intensity = sampleModel.getIntensities()[mChannel];
            double concentration =  (intensity - mIntercept)/ mSlope;
            entries.add(new Entry((float) concentration,  (float) intensity));
        }
        ScatterDataSet set = new ScatterDataSet(entries, label);
        set.setColor(color);
        set.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        set.setScatterShapeSize(7.5f);
        set.setDrawValues(false);
        set.setValueTextSize(10f);
        return set;
    }

    private LineData generateStandardCurveData() {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        int k = 0;
        for (int i = 0; i <= 1000; i += 5) {
            entries.add(new Entry(i, (float) mStandardCurve.eval(i)));
        }
        LineDataSet set1 = new LineDataSet(entries, "Standard Curve");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(Color.RED);
        set1.setDrawCircleHole(false);
        set1.setLineWidth(2f);
        set1.setFillAlpha(65);
        set1.setFillColor(Color.RED);
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setDrawCircles(false);
        LineData data = new LineData();
        data.addDataSet(set1);
        data.setDrawValues(false);
        return data;
    }
}
