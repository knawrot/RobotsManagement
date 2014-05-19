package com.example.robotsmanagement;

import android.app.Activity;
import android.os.Bundle;

/* 
 * 1. Skalowanie mapy to nie jest najlepsze wyjscie w naszej sytuacji (powinien byc z gory znany
 * obszar ktory sie pokazuje w widoku mapy)
 * 2. Mniejsze przyciski
 * 3. Pochowac kolory/rozmiary tekstow itd. do *.xml jakas zmiana
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        return;
    }
}
