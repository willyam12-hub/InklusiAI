package com.willyam.inklusiai.features;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.willyam.inklusiai.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ChatAIActivity extends AppCompatActivity {

    private static final String GEMINI_API_KEY = "";

    private EditText etPertanyaan;
    private Button btnKirimChat;
    private TextView tvRiwayatChat;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String uidUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_ai);

        etPertanyaan = findViewById(R.id.etPertanyaan);
        btnKirimChat = findViewById(R.id.btnKirimChat);
        tvRiwayatChat = findViewById(R.id.tvRiwayatChat);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            uidUser = mAuth.getCurrentUser().getUid();
        }

        btnKirimChat.setOnClickListener(v -> {

            String pertanyaan = etPertanyaan.getText().toString().trim();

            if (TextUtils.isEmpty(pertanyaan)) {
                return;
            }

            tvRiwayatChat.append("\n\nUser : " + pertanyaan);

            etPertanyaan.setText("");

            new AmbilResponGeminiTask().execute(pertanyaan);
        });
    }

    private class AmbilResponGeminiTask extends AsyncTask<String, Void, String> {

        private String teksPertanyaan;

        @Override
        protected String doInBackground(String... params) {

            teksPertanyaan = params[0];

            String urlString =
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="AQ.Ab8RN6Lv-DmTYBVS2SlfG8geQwJMQ2TBVdVjREfg6WDSAi3IHQ;

            try {

                URL url = new URL(urlString);

                HttpURLConnection conn =
                        (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);

                JSONObject jsonBody = new JSONObject();

                JSONArray contents = new JSONArray();

                JSONObject content = new JSONObject();

                JSONArray parts = new JSONArray();

                JSONObject part = new JSONObject();

                part.put("text", teksPertanyaan);

                parts.put(part);

                content.put("parts", parts);

                contents.put(content);

                jsonBody.put("contents", contents);

                OutputStream os = conn.getOutputStream();

                os.write(jsonBody.toString().getBytes(StandardCharsets.UTF_8));

                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                Log.d("GEMINI", "Response Code = " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    BufferedReader br =
                            new BufferedReader(
                                    new InputStreamReader(
                                            conn.getInputStream(),
                                            StandardCharsets.UTF_8));

                    StringBuilder response = new StringBuilder();

                    String line;

                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }

                    br.close();

                    JSONObject jsonResponse =
                            new JSONObject(response.toString());

                    return jsonResponse
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                } else {

                    BufferedReader br =
                            new BufferedReader(
                                    new InputStreamReader(
                                            conn.getErrorStream(),
                                            StandardCharsets.UTF_8));

                    StringBuilder error = new StringBuilder();

                    String line;

                    while ((line = br.readLine()) != null) {
                        error.append(line);
                    }

                    br.close();

                    Log.e("GeminiApiError", error.toString());

                    return "Error API : " + responseCode +
                            "\n\n" + error;
                }

            } catch (Exception e) {

                Log.e("GeminiException", e.getMessage(), e);

                return "Error : " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String jawabanAI) {

            tvRiwayatChat.append("\nAI : " + jawabanAI + "\n");

            if (uidUser != null) {

                Map<String, Object> chatLog = new HashMap<>();

                chatLog.put("id",
                        String.valueOf(System.currentTimeMillis()));

                chatLog.put("uid", uidUser);
                chatLog.put("pertanyaan", teksPertanyaan);
                chatLog.put("jawaban", jawabanAI);
                chatLog.put("timestamp",
                        System.currentTimeMillis());

                db.collection("ChatHistory")
                        .add(chatLog);
            }
        }
    }
}
