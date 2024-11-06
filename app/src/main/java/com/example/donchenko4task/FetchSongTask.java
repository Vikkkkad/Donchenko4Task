package com.example.donchenko4task;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchSongTask extends AsyncTask<Void, Void, String> {
    private DBHelper dbHelper;
    private Context context; // Добавляем поле для контекста

    public FetchSongTask(Context context) {
        this.dbHelper = new DBHelper(context);
        this.context = context; // Инициализируем контекст
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            // URL запроса
            URL url = new URL("https://www.loveradio.ru/backend/api/v1/love-radio/player/online?filter[musicStreamIds][]=28");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // Чтение ответа
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            try {
                // Парсим JSON-ответ
                JSONObject jsonObject = new JSONObject(result);
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray historyArray = data.getJSONArray("playerHistory");
                if (historyArray.length() > 0) {
                    JSONObject firstSong = historyArray.getJSONObject(0);
                    String track = firstSong.getString("title");

                    // Сравниваем с последней записью в базе данных
                    if (!isLastSongInDatabase(track)) {
                        addSongToDatabase(track);
                        ((MainActivity) context).updateSongList(); // Обновляем список песен
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void addSongToDatabase(String track) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "INSERT INTO songs (TrackTitle, Timestamp) VALUES (?, ?)";
        db.execSQL(query, new Object[]{track, System.currentTimeMillis()});
        db.close();
    }

    private boolean isLastSongInDatabase(String track) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT TrackTitle FROM songs ORDER BY Timestamp DESC LIMIT 1", null);

        boolean exists = false;
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex("TrackTitle"); // Получаем индекс столбца
            if (columnIndex != -1) { // Проверяем, что столбец существует
                String lastTrack = cursor.getString(columnIndex);
                exists = lastTrack.equals(track);
            }
        }

        cursor.close();
        db.close();
        return exists; // Возвращаем true, если такая песня уже есть
    }
}


