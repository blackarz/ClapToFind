package com.androidkarkhana.claptofind;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class ClapDetector {
    private static final int SAMPLE_RATE = 44100; // Commonly supported sample rate
    private static final String TAG = "ClapDetector";
    private AudioRecord audioRecord;
    private boolean isDetecting = false;
    private OnClapListener onClapListener;

    public interface OnClapListener {
        void onClapDetected();
    }

    public ClapDetector(OnClapListener listener) {
        this.onClapListener = listener;

        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            throw new IllegalStateException("Invalid buffer size for AudioRecord");
        }

        try {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                throw new IllegalStateException("AudioRecord initialization failed");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Permission missing: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error initializing AudioRecord: " + e.getMessage());
        }
    }

    public void startDetection() {
        isDetecting = true;
        new Thread(() -> {
            short[] buffer = new short[1024];
            audioRecord.startRecording();

            while (isDetecting) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (read > 0) {
                    for (short value : buffer) {
                        if (Math.abs(value) > 20000) { // Sensitivity threshold
                            if (onClapListener != null) {
                                onClapListener.onClapDetected();
                            }
                            break;
                        }
                    }
                }
            }
            audioRecord.stop();
        }).start();
    }

    public void stopDetection() {
        isDetecting = false;
    }
}
