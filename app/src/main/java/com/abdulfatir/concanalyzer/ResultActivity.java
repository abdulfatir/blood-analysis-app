package com.abdulfatir.concanalyzer;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private CombinedChart mChart;
    private double mSlope;
    private double mIntercept;
    private LinearFunction mStandardCurve;
    private static final String KNOWN_SAMPLES_KEY = "KNOWN_SAMPLES";
    private static final String UNKNOWN_SAMPLES_KEY = "UNKNOWN_SAMPLES";
    private static final String QC_SAMPLES_KEY = "QC_SAMPLES";
    private static final String SLOPE_KEY = "SLOPE";
    private static final String INTERCEPT_KEY = "INTERCEPT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);


        ActionBar bar = getSupportActionBar();
        bar.setTitle("Results");
        bar.setDisplayHomeAsUpEnabled(false);
        bar.setHomeButtonEnabled(false);

        Bundle bundle = getIntent().getExtras();
        mSlope = bundle.getDouble(SLOPE_KEY);
        mIntercept = bundle.getDouble(INTERCEPT_KEY);
        ArrayList<SampleModel> knownSamples = bundle.getParcelableArrayList(KNOWN_SAMPLES_KEY);
        ArrayList<SampleModel> unKnownSamples = bundle.getParcelableArrayList(UNKNOWN_SAMPLES_KEY);
        ArrayList<SampleModel> qcSamples = bundle.getParcelableArrayList(QC_SAMPLES_KEY);


        TextView eqTv = (TextView) findViewById(R.id.equation);
        eqTv.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC);
        LinearLayout mainContent = (LinearLayout) findViewById(R.id.content);


        LinearFunction linearFunction = new LinearFunction(mIntercept, mSlope);
        mStandardCurve = linearFunction.swapAxes();
        mStandardCurve.setIntercept(mStandardCurve.getIntercept() + 255);

        eqTv.setText(mStandardCurve.toString());
        for (int i = 0; i < qcSamples.size(); i++)
            mainContent.addView(addSubView(mainContent, qcSamples.get(i), "Quality Control"));
        for (int i = 0; i < unKnownSamples.size(); i++)
            mainContent.addView(addSubView(mainContent, unKnownSamples.get(i), "Unknown"));


        mChart = (CombinedChart) findViewById(R.id.graph);
        mChart.setDescription("");
        mChart.setNoDataTextDescription("No data to display.");

        // enable touch gestures
        mChart.setTouchEnabled(true);

        mChart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        mChart.setHighlightPerDragEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
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
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
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

    }

    private View addSubView(ViewGroup mainContent, SampleModel sampleModel, String s) {

        double intensity = sampleModel.getIntensity();
        double concentration = mSlope * intensity + mIntercept;

        View layout = LayoutInflater.from(this).inflate(R.layout.layout_quality_control, mainContent, false);
        TextView titleTV = (TextView) layout.findViewById(R.id.textView2);
        TextView concTV = (TextView) layout.findViewById(R.id.textView3);
        TextView errorTV = (TextView) layout.findViewById(R.id.textView4);

        titleTV.setText(s);
        concTV.setText(String.format(Locale.getDefault(), "%s%.2f ng/mL", "Concentration: ", concentration));

        if (sampleModel.getDataPointType() == SampleModel.DataPointType.QUALITY_CONTROL) {
            double error = Math.abs(concentration - sampleModel.getConcentration()) / sampleModel.getConcentration();
            error *= 100;
            if (error > 20)
                Toast.makeText(this, "Error is greater than 20%", Toast.LENGTH_SHORT).show();
            errorTV.setText(String.format(Locale.getDefault(), "%s%.1f%%", "Error: ", error));
        } else if (sampleModel.getDataPointType() == SampleModel.DataPointType.UNKNOWN) {
            errorTV.setVisibility(View.GONE);
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
            entries.add(new Entry((float) sampleModel.getConcentration(), 255f - (float) sampleModel.getIntensity()));
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
            double intensity = sampleModel.getIntensity();
            double concentration = mSlope * intensity + mIntercept;
            entries.add(new Entry((float) concentration, 255f - (float) intensity));
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
            //xVals.add(""+i);
            entries.add(new Entry(i, (float) mStandardCurve.eval(i)));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(entries, "Standard Curve");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(Color.RED);
        set1.setDrawCircleHole(false);
        set1.setLineWidth(2f);
        set1.setFillAlpha(65);
        set1.setFillColor(Color.RED);
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setDrawCircles(false);

        // add the datasets

        // create a data object with the datasets
        LineData data = new LineData();
        data.addDataSet(set1);
        data.setDrawValues(false);


        return data;
    }
}
