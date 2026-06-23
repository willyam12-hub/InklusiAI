package com.willyam.inklusiai;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.willyam.inklusiai.auth.LoginActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ProgressBar progressBar = findViewById(R.id.splashProgressBar);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // SOLUSI: Cek status login Firebase agar tidak terjebak di Login terus menerus
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    // Jika sudah login, langsung ke Home (Menu Utama dengan 5 fitur)
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                } else {
                    // Jika belum login, ke halaman Login
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                finish();
            }
        }, 3000); // Delay 3 Detik
    }
}
