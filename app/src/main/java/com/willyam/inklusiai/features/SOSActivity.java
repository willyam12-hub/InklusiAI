package com.willyam.inklusiai.features;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.willyam.inklusiai.R;
import java.util.HashMap;
import java.util.Map;

public class SOSActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 301;

    private EditText etNomorDarurat;
    private Button btnSimpanNomor, btnKirimSOS;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private String uidUser, linkLokasiMaps = "http://maps.google.com/?q=0,0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        etNomorDarurat = findViewById(R.id.etNomorDarurat);
        btnSimpanNomor = findViewById(R.id.btnSimpanNomor);
        btnKirimSOS = findViewById(R.id.btnKirimSOS);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (mAuth.getCurrentUser() != null) {
            uidUser = mAuth.getCurrentUser().getUid();
            db.collection("Users").document(uidUser).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && documentSnapshot.contains("nomorDarurat")) {
                            etNomorDarurat.setText(documentSnapshot.getString("nomorDarurat"));
                        }
                    });
        }

        btnSimpanNomor.setOnClickListener(v -> {
            String nomor = etNomorDarurat.getText().toString().trim();
            if (TextUtils.isEmpty(nomor)) {
                Toast.makeText(SOSActivity.this, "Isi nomor terlebih dahulu!", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("nomorDarurat", nomor);

            db.collection("Users").document(uidUser).update(updateData)
                    .addOnSuccessListener(aVoid -> Toast.makeText(SOSActivity.this, "Nomor Darurat Berhasil Disimpan!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(SOSActivity.this, "Gagal menyimpan data.", Toast.LENGTH_SHORT).show());
        });

        btnKirimSOS.setOnClickListener(v -> {
            if (periksaIzinAkses()) {
                ambilLokasiDanKirimSMS();
            } else {
                ActivityCompat.requestPermissions(SOSActivity.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.SEND_SMS
                }, PERMISSION_REQUEST_CODE);
            }
        });
    }

    private boolean periksaIzinAkses() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void ambilLokasiDanKirimSMS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                linkLokasiMaps = "http://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
            }
            eksekusiKirimSMS();
        }).addOnFailureListener(e -> eksekusiKirimSMS());
    }

    private void eksekusiKirimSMS() {
        String nomorTujuan = etNomorDarurat.getText().toString().trim();
        if (TextUtils.isEmpty(nomorTujuan)) {
            Toast.makeText(this, "Nomor kontak darurat belum diatur!", Toast.LENGTH_SHORT).show();
            return;
        }

        String pesanSMS = "Pesan SOS Darurat! Saya membutuhkan bantuan.\n\nLokasi saya:\n" + linkLokasiMaps;

        try {
            // Gunakan API modern untuk mengirim SMS
            SmsManager smsManager = this.getSystemService(SmsManager.class);
            smsManager.sendTextMessage(nomorTujuan, null, pesanSMS, null, null);
            Toast.makeText(this, "Pesan SOS Darurat Berhasil Dikirim!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("SOSActivity", "SMS Error: " + e.getMessage());
            Toast.makeText(this, "SMS Gagal Dikirim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ambilLokasiDanKirimSMS();
        } else {
            Toast.makeText(this, "Aplikasi membutuhkan izin GPS dan SMS untuk fitur SOS!", Toast.LENGTH_SHORT).show();
        }
    }
}