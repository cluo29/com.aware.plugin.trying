package com.aware.plugin.trying;

/**
 * Created by CLUO29 on 2/27/2015.
 */


import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aware.Aware;
import com.aware.plugin.trying.Provider.AmbientNoise_Data;
import com.aware.utils.IContextCard;


public class ContextCard implements IContextCard {
    private static TextView frequency;
    private static LinearLayout ambient_plot;
    public ContextCard(){}
    public View getContextCard(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View card = inflater.inflate(R.layout.activity_settings, null);
        frequency = (TextView) card.findViewById(R.id.frequency);

        Cursor latest = context.getContentResolver().query(AmbientNoise_Data.CONTENT_URI, null, null, null, AmbientNoise_Data.TIMESTAMP + " DESC LIMIT 1");
        if( latest != null && latest.moveToFirst() ) {
            frequency.setText("There was data");
            //frequency.setText(String.format("%.1f", latest.getDouble(latest.getColumnIndex(AmbientNoise_Data.FREQUENCY))) + " Hz");
        }
        else
        {
            //frequency.setText("COMET is super cleverrrrrr");
            //always in this branch==
        }
        if( latest != null && ! latest.isClosed() ) latest.close();

        ambient_plot = (LinearLayout) card.findViewById(com.aware.plugin.trying.R.id.ambient_plot);
        ambient_plot.removeAllViews();
        ambient_plot.addView(drawGraph(context));

        return card;
    }

    private static GraphicalView drawGraph( Context context ) {
        GraphicalView mChart;

        long delta_time = System.currentTimeMillis()-(1 * 60 * 1000);

        XYSeries frequency_series = new XYSeries("Frequency (Hz)");
        Cursor audio_frequency = context.getContentResolver().query(AmbientNoise_Data.CONTENT_URI, null, AmbientNoise_Data.TIMESTAMP + " > " + delta_time, null, AmbientNoise_Data.TIMESTAMP + " ASC");
        if( audio_frequency != null && audio_frequency.moveToFirst() ) {
            do {
                frequency_series.add(audio_frequency.getPosition(), audio_frequency.getDouble(audio_frequency.getColumnIndex(AmbientNoise_Data.FREQUENCY)));
            } while( audio_frequency.moveToNext() );
        }
        if( audio_frequency != null && ! audio_frequency.isClosed()) audio_frequency.close();

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(frequency_series);


        //setup frequency
        XYSeriesRenderer frequency_renderer = new XYSeriesRenderer();
        frequency_renderer.setColor(Color.BLUE);
        frequency_renderer.setPointStyle(PointStyle.POINT);
        frequency_renderer.setDisplayChartValues(false);
        frequency_renderer.setLineWidth(2);
        frequency_renderer.setFillPoints(true);


        //Setup graph
        XYMultipleSeriesRenderer dataset_renderer = new XYMultipleSeriesRenderer();
        dataset_renderer.setChartTitle("Ambient noise");
        dataset_renderer.setLabelsColor(Color.BLACK);
        dataset_renderer.setDisplayValues(true);
        dataset_renderer.setFitLegend(false);
        dataset_renderer.setXLabelsColor(Color.BLACK);
        dataset_renderer.setYLabelsColor(0, Color.BLACK);
        dataset_renderer.setLegendHeight(0);
        dataset_renderer.setYLabels(0);
        dataset_renderer.setYTitle("Hz & dB");
        dataset_renderer.setXTitle("Time");
        dataset_renderer.setZoomButtonsVisible(false);
        dataset_renderer.setXLabels(0);
        dataset_renderer.setPanEnabled(false);
        dataset_renderer.setShowGridY(false);
        dataset_renderer.setClickEnabled(false);
        dataset_renderer.setAntialiasing(true);
        dataset_renderer.setAxesColor(Color.BLACK);
        dataset_renderer.setApplyBackgroundColor(true);
        dataset_renderer.setBackgroundColor(Color.WHITE);
        dataset_renderer.setMarginsColor(Color.WHITE);

        //add plot renderers to main renderer
        dataset_renderer.addSeriesRenderer(frequency_renderer);


        //put everything together
        mChart = ChartFactory.getLineChartView(context, dataset, dataset_renderer);

        return mChart;
    }
}
