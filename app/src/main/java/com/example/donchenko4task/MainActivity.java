package com.example.donchenko4task;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private Handler handler = new Handler();
    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> songList;
    SQLiteDatabase db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);
        db = dbHelper.getReadableDatabase();
        listView = findViewById(R.id.listView);

        songList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songList);
        listView.setAdapter(adapter);


        loadSongsFromDatabase();
        startFetchingSongs();

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Запуск в автономном режиме", Toast.LENGTH_LONG).show();
            loadSongsFromDatabase();
        } else {
            startFetchingSongs();
        }
    }

    // Метод проверки подключения к интернету
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    // Регулярный опрос API каждые 60 секунд
    private void startFetchingSongs() {
        final Runnable fetchTask = new Runnable() {
            @Override
            public void run() {
                new FetchSongTask(MainActivity.this).execute();
                handler.postDelayed(this, 60000);
            }
        };
        handler.post(fetchTask);
    }


    // Загрузка данных из базы данных и отображение
    private void loadSongsFromDatabase() {
        songList.clear(); // Очистите список перед загрузкой
        Cursor cursor = dbHelper.getAllSongs();
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String track = cursor.getString(cursor.getColumnIndex("TrackTitle"));
                @SuppressLint("Range") String timestamp = cursor.getString(cursor.getColumnIndex("Timestamp"));
                songList.add(track + " " + timestamp);
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    public void updateSongList() {
        loadSongsFromDatabase();
    }
}