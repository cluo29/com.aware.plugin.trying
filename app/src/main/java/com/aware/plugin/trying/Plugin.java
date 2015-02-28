package com.aware.plugin.trying;

/**
 * Created by CLUO29 on 2/27/2015.
 */

import android.content.ContentValues;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.trying.Provider.AmbientNoise_Data;
import com.aware.utils.Aware_Plugin;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class Plugin extends Aware_Plugin {

    /**
     * Broadcasted with sound frequency value <br/>
     * Extra: sound_frequency, in Hz
     */
    public static final String ACTION_AWARE_PLUGIN_AMBIENT_NOISE = "ACTION_AWARE_PLUGIN_AMBIENT_NOISE";

    /**
     * Extra for ACTION_AWARE_PLUGIN_AMBIENT_NOISE
     * (double) sound frequency in Hz
     */
    public static final String EXTRA_SOUND_FREQUENCY = "sound_frequency";
    private static double[] magnitudes;
    private double sound_frequency;
    //AWARE context producer
    public static ContextProducer context_producer;
    public Thread audio_thread = new Thread() {
        public void run() {
            int buffer_size = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 3;
            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer_size);

            //Audio data buffer
            short[] audio_data = new short[buffer_size];

            while (true) {
                if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                    if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
                        recorder.startRecording();
                    }


                    recorder.read(audio_data, 0, buffer_size);
                    //AudioAnalysis audio_analysis = new AudioAnalysis(getApplicationContext(), audio_data, buffer_size);
                    //Create an FFT buffer
                    double[] fft_buffer = new double[buffer_size * 2];
                    for (int i = 0; i < audio_data.length; i++) {
                        fft_buffer[2 * i] = (double) audio_data[i];
                        fft_buffer[2 * i + 1] = 0;
                    }

                    //apply FFT to fill imaginary buffers
                    DoubleFFT_1D fft = new DoubleFFT_1D(buffer_size);
                    fft.realForward(fft_buffer);

                    //Fetch power spectrum (magnitudes)
                    //and normalize them
                    magnitudes = new double[buffer_size / 2];
                    for (int i = 1; i < buffer_size / 2 - 1; i++) {
                        double real = fft_buffer[2 * i];
                        double imaginary = fft_buffer[2 * i + 1];
                        magnitudes[i] = Math.sqrt((real * real) + (imaginary * imaginary));
                    }

                    //find largest peak in power spectrum (magnitudes)
                    double max = -1;
                    int max_index = -1;
                    for (int i = 0; i < buffer_size / 2 - 1; i++) {
                        if (magnitudes[i] > max) {
                            max = magnitudes[i];
                            max_index = i;
                        }
                    }
                    sound_frequency = max_index * 8000 / buffer_size; //Hz here!
                    ContentValues data = new ContentValues();
                    //require Provider.java here
                    data.put(AmbientNoise_Data.TIMESTAMP, System.currentTimeMillis());
                    data.put(AmbientNoise_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                    data.put(AmbientNoise_Data.FREQUENCY, sound_frequency);

                    getContentResolver().insert(AmbientNoise_Data.CONTENT_URI, data);

                    //Share context
                    context_producer.onContext();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else { //recorder is busy right now, let's wait 30 seconds before we try again
                    try {
                        Thread.sleep(30 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }


        }
    };
    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::Trying";
        DEBUG = true;

        Intent aware = new Intent(this, Aware.class);
        startService(aware);
        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {
                Intent context_ambient_noise = new Intent();
                context_ambient_noise.setAction(ACTION_AWARE_PLUGIN_AMBIENT_NOISE);
                context_ambient_noise.putExtra(EXTRA_SOUND_FREQUENCY, sound_frequency);
                sendBroadcast(context_ambient_noise);
            }
        };
        context_producer = CONTEXT_PRODUCER;
        audio_thread.start();
    }
    public void onDestroy() {
        super.onDestroy();
    }
}

