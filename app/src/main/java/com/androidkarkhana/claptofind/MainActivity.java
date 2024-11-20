package com.androidkarkhana.claptofind;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private ClapDetector clapDetector;
    private boolean isClapDetectionEnabled = false;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Switch clapSwitch = findViewById(R.id.switch_clap_detection);
        Button testAlertButton = findViewById(R.id.btn_test_alert);
        Button stopAlertButton = findViewById(R.id.btn_stop_alert);

        // Check and request microphone permission
        if (!checkPermission()) {
            requestPermission();
        }

        clapSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!checkPermission()) {
                requestPermission();
                clapSwitch.setChecked(false);
                return;
            }

            isClapDetectionEnabled = isChecked;
            if (isChecked) {
                startClapDetection();
                Toast.makeText(this, "Clap Detection Enabled", Toast.LENGTH_SHORT).show();
            } else {
                stopClapDetection();
                Toast.makeText(this, "Clap Detection Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        testAlertButton.setOnClickListener(v -> triggerAlert());

        stopAlertButton.setOnClickListener(v -> stopAlert());
    }

    private void startClapDetection() {
        if (clapDetector == null) {
            clapDetector = new ClapDetector(() -> runOnUiThread(this::triggerAlert));
        }
        clapDetector.startDetection();
    }

    private void stopClapDetection() {
        if (clapDetector != null) {
            clapDetector.stopDetection();
        }
    }

    private void triggerAlert() {
        // Play sound
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound);
            mediaPlayer.setOnCompletionListener(mp -> stopAlert());
        }

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }

        // Vibrate
        if (vibrator == null) {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        }

        if (vibrator != null) {
            vibrator.vibrate(1000); // Vibrate for 1 second
        }

        Toast.makeText(this, "Alert Triggered!", Toast.LENGTH_SHORT).show();
    }

    private void stopAlert() {
        // Stop media player
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop(); // Stop playback
                }
                mediaPlayer.reset(); // Reset for reuse
                mediaPlayer.release(); // Release resources
                mediaPlayer = null; // Nullify for safety
            } catch (IllegalStateException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error stopping sound", Toast.LENGTH_SHORT).show();
            }
        }

        // Cancel vibration
        if (vibrator != null) {
            vibrator.cancel(); // Stop ongoing vibration
        }

        Toast.makeText(this, "Alert Stopped!", Toast.LENGTH_SHORT).show();
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Microphone Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
