package com.quizapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class SplashActivity extends AppCompatActivity {
    private TextView appName;
    private Animation animation;
    public static List<String> catList = new ArrayList<>();
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        appName = findViewById(R.id.textView);
        //Typeface typeface = ResourcesCompat.getFont(this, R.font.blacklist);
        //appName.setTypeface(typeface);
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.myanim);
        appName.setAnimation(animation);

        firestore = FirebaseFirestore.getInstance();

        new Thread(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        }).start();

    }

    private void loadData() {
        catList.clear();
        firestore.collection("QUIZ").document("Categories")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        long count = (long) doc.get("COUNT");
                        for (int i = 1; i <= count; i++) {
                            String catName = doc.getString("CAT" + String.valueOf(i));
                            catList.add(catName);
                        }

                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        SplashActivity.this.finish();
                        
                    } else {
                        Toast.makeText(SplashActivity.this, "No category document exist", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(SplashActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
