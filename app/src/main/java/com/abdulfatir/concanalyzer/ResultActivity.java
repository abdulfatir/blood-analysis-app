package com.abdulfatir.concanalyzer;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import java.util.List;
import java.util.Locale;

/**
 * Displays the results.
 */
public class ResultActivity extends AppCompatActivity {

    private static final String KNOWN_SAMPLES_KEY = "KNOWN_SAMPLES";
    private static final String UNKNOWN_SAMPLES_KEY = "UNKNOWN_SAMPLES";
    private static final String QC_SAMPLES_KEY = "QC_SAMPLES";
    private static final String SLOPE_KEY = "SLOPE";
    private static final String INTERCEPT_KEY = "INTERCEPT";
    private static final String R2SCORE_KEY = "R2SCORE";
    private static final String CHANNEL_KEY = "CHANNEL";
    private CombinedChart mChart;
    private double[] mSlopes;
    private double[] mIntercepts;
    private LinearFunction[] mStandardCurves;
    private double[] mR2Scores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        ActionBar bar = getSupportActionBar();
        bar.setTitle("Results");
        bar.setDisplayHomeAsUpEnabled(false);
        bar.setHomeButtonEnabled(false);
        Bundle bundle = getIntent().getExtras();
        mSlopes = bundle.getDoubleArray(SLOPE_KEY);
        mIntercepts = bundle.getDoubleArray(INTERCEPT_KEY);
        mR2Scores = bundle.getDoubleArray(R2SCORE_KEY);
        ArrayList<SampleModel> knownSamples = bundle.getParcelableArrayList(KNOWN_SAMPLES_KEY);
        ArrayList<SampleModel> unKnownSamples = bundle.getParcelableArrayList(UNKNOWN_SAMPLES_KEY);
        ArrayList<SampleModel> qcSamples = bundle.getParcelableArrayList(QC_SAMPLES_KEY);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(viewPager, knownSamples, unKnownSamples, qcSamples);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);


    }

    public double getSlope(int channel)
    {
        return mSlopes[channel];
    }

    public double getIntercept(int channel)
    {
        return mIntercepts[channel];
    }

    public double getRSquared(int channel)
    {
        return mR2Scores[channel];
    }

    private void setupViewPager(ViewPager viewPager, ArrayList<SampleModel> knownSamples, ArrayList<SampleModel> unKnownSamples, ArrayList<SampleModel> qcSamples) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        Bundle redExtras = new Bundle();
        Bundle greenExtras = new Bundle();
        Bundle blueExtras = new Bundle();

        redExtras.putParcelableArrayList(KNOWN_SAMPLES_KEY, knownSamples);
        greenExtras.putParcelableArrayList(KNOWN_SAMPLES_KEY, knownSamples);
        blueExtras.putParcelableArrayList(KNOWN_SAMPLES_KEY, knownSamples);

        redExtras.putParcelableArrayList(UNKNOWN_SAMPLES_KEY, unKnownSamples);
        greenExtras.putParcelableArrayList(UNKNOWN_SAMPLES_KEY, unKnownSamples);
        blueExtras.putParcelableArrayList(UNKNOWN_SAMPLES_KEY, unKnownSamples);

        redExtras.putParcelableArrayList(QC_SAMPLES_KEY, qcSamples);
        greenExtras.putParcelableArrayList(QC_SAMPLES_KEY, qcSamples);
        blueExtras.putParcelableArrayList(QC_SAMPLES_KEY, qcSamples);

        redExtras.putInt(CHANNEL_KEY, 0);
        greenExtras.putInt(CHANNEL_KEY, 1);
        blueExtras.putInt(CHANNEL_KEY, 2);

        ChannelFragment redResults = new ChannelFragment();
        ChannelFragment greenResults = new ChannelFragment();
        ChannelFragment blueResults = new ChannelFragment();

        redResults.setArguments(redExtras);
        greenResults.setArguments(greenExtras);
        blueResults.setArguments(blueExtras);

        adapter.addFragment(redResults, "Red");
        adapter.addFragment(greenResults, "Green");
        adapter.addFragment(blueResults, "Blue");

        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
