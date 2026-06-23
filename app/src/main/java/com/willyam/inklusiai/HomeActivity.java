package com.willyam.inklusiai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.willyam.inklusiai.features.ChatAIActivity;
import com.willyam.inklusiai.features.OCRActivity;
import com.willyam.inklusiai.features.SOSActivity;
import com.willyam.inklusiai.features.SpeechToTextActivity;
import com.willyam.inklusiai.features.TextToSpeechActivity;

public class HomeActivity extends AppCompatActivity {
    private TextView tvNamaUser;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvNamaUser = findViewById(R.id.tvNamaUser);

        // Ambil data nama dari Firestore
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("Users").document(uid).get()
                    .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists() && document.contains("nama")) {
                        tvNamaUser.setText(document.getString("nama"));
                    }
                }
            });
        }

        // Inisialisasi Menu CardView
        MaterialCardView menuOCR = findViewById(R.id.menuOCR);
        MaterialCardView menuSTT = findViewById(R.id.menuSTT);
        MaterialCardView menuTTS = findViewById(R.id.menuTTS);
        MaterialCardView menuSOS = findViewById(R.id.menuSOS);
        MaterialCardView menuChat = findViewById(R.id.menuChat);

        // Intent Navigation
        menuOCR.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, OCRActivity.class)));
        menuSTT.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SpeechToTextActivity.class)));
        menuTTS.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, TextToSpeechActivity.class)));
        menuSOS.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SOSActivity.class)));
        menuChat.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ChatAIActivity.class)));
    }
}