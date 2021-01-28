package com.billbook.app.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.billbook.app.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class BarGraph extends AppCompatActivity {

    BarChart chart;
    ArrayList<BarEntry> BARENTRY;
    ArrayList<String> BarEntryLabels;
    BarDataSet Bardataset;
    BarData BARDATA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_graph);
//        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
        chart = findViewById(R.id.barchart);

        BARENTRY = new ArrayList<>();

        BarEntryLabels = new ArrayList<String>();
        processData();
//        AddValuesToBARENTRY();

//        AddValuesToBarEntryLabels();

        Bardataset = new BarDataSet(BARENTRY, "Sales Report");
        Bardataset.setColors(ColorTemplate.COLORFUL_COLORS);

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(Bardataset);

        BARDATA = new BarData(dataSets);
        chart.setPinchZoom(true);
        chart.setData(BARDATA);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setDrawGridLines(false);
//        xAxis.setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return BarEntryLabels.get(((int)value-1)>=BarEntryLabels.size()?BarEntryLabels.size():(int)value);
//            }
//        });
        chart.getXAxis()
                .setPosition(XAxis.XAxisPosition.BOTTOM);
//        chart.getXAxis().setLabelCount(BarEntryLabels.size(),true);
        xAxis.setGranularity(1f);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(BarEntryLabels));
        chart.animateY(1000);

        //        convertData();
    }

    private void processData() {
        JSONArray data = null;
        try {
            data = new JSONArray(getIntent().getStringExtra("data"));
            for (int i = 0; i < data.length(); i++) {
                BARENTRY.add(new BarEntry(i + 1, data.getJSONObject(i).getInt("totalPriceAfterGST")));
                BarEntryLabels.add(data.getJSONObject(i).getString("name"));
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
}
