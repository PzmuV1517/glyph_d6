package com.example.glyph_d6;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);
        textView.setText("D6 Dice Glyph Toy\n\nThis app creates a virtual dice toy for the Nothing Phone(3) Glyph Matrix.\n\nTo use:\n1. Go to Settings > Glyph Interface > Glyph Toys\n2. Enable 'D6 Dice Toy'\n3. Use the Glyph Button to cycle to this toy\n4. Long press the Glyph Button to roll the dice!");
        textView.setPadding(40, 40, 40, 40);
        textView.setTextSize(16);

        setContentView(textView);
    }
}
