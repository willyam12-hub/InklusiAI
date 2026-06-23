package com.willyam.inklusiai.features;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.willyam.inklusiai.R;
import java.util.ArrayList;
import java.util.Locale;

public class SpeechToTextActivity extends AppCompatActivity {
    private static final int RECORD_AUDIO_CODE = 201;

    private TextView tvHasilSuara;
    private Button btnMulaiBicara;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private boolean sedangMerekam = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_to_text);

        tvHasilSuara = findViewById(R.id.tvHasilSuara);
        btnMulaiBicara = findViewById(R.id.btnMulaiBicara);

        // Inisialisasi Android Speech Recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                tvHasilSuara.setText("Mulai bicara, sistem sedang mendengarkan...");
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                btnMulaiBicara.setText("Tekan & Bicara");
                sedangMerekam = false;
            }

            @Override
            public void onError(int error) {
                tvHasilSuara.setText("Gagal menangkap suara. Silakan coba lagi.");
                btnMulaiBicara.setText("Tekan & Bicara");
                sedangMerekam = false;
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    tvHasilSuara.setText(matches.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        btnMulaiBicara.setOnClickListener(v -> {
            if (!sedangMerekam) {
                if (ContextCompat.checkSelfPermission(SpeechToTextActivity.this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SpeechToTextActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_CODE);
                } else {
                    speechRecognizer.startListening(speechRecognizerIntent);
                    btnMulaiBicara.setText("Mendengarkan...");
                    sedangMerekam = true;
                }
            } else {
                speechRecognizer.stopListening();
                btnMulaiBicara.setText("Tekan & Bicara");
                sedangMerekam = false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Izin mikrofon diberikan! Silakan tekan tombol lagi.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Izin mikrofon ditolak!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }
}