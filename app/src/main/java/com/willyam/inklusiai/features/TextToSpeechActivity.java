package com.willyam.inklusiai.features;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.willyam.inklusiai.R;
import java.util.Locale;

public class TextToSpeechActivity extends AppCompatActivity {
    private EditText etInputTeks;
    private Button btnKonversiSuara;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_speech);

        etInputTeks = findViewById(R.id.etInputTeksTTS);
        btnKonversiSuara = findViewById(R.id.btnKonversiSuara);

        // Inisialisasi Android TTS Engine
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("id", "ID")); // Set Bahasa Indonesia
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(TextToSpeechActivity.this, "Bahasa Indonesia tidak didukung di perangkat ini!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(TextToSpeechActivity.this, "Inisialisasi TextToSpeech Gagal!", Toast.LENGTH_SHORT).show();
            }
        });

        btnKonversiSuara.setOnClickListener(v -> {
            String teks = etInputTeks.getText().toString().trim();
            if (TextUtils.isEmpty(teks)) {
                Toast.makeText(TextToSpeechActivity.this, "Silakan ketik sesuatu terlebih dahulu!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Konversi teks tertulis menjadi output audio
            textToSpeech.speak(teks, TextToSpeech.QUEUE_FLUSH, null, null);
        });
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}