package com.rrmotor.hargapart;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);
        textView.setText("🏍️ RR MOTOR CEK HARGA PART");
        textView.setTextSize(22);
        textView.setPadding(30, 50, 30, 30);

        setContentView(textView);
    }
}
