# Лабораторная работа №4. Взаимодействие с сервером.
## Выполнила: Донченко Вика, ИСП-211о
## Язык программирования: Java

### 1. Функции приложения
Приложение позволяет пользователям получать и отображать информацию о текущих музыкальных треках на радио Мегабайт. Оно включает в себя один экран:

- "Главный экран" (MainActivity)

Приложение создает базу данных и таблицу "songs", если они не существуют, и автоматически обновляет информацию о текущих треках каждые 60 секунд.

### 2. Главный экран
Главный экран приложения отображает список текущих треков. Пользователь может видеть как последние треки, так и те, которые были сохранены в базе данных.

- Обновление списка треков происходит автоматически, если доступно интернет-соединение.
  `java
  private void startFetchingSongs() {
  final Runnable fetchTask = new Runnable() {
  @Override
  public void run() {
  new FetchSongTask(MainActivity.this).execute();
  handler.postDelayed(this, 60000);
  }
  };
  handler.post(fetchTask);
  }`
- Загрузка данных из базы данных осуществляется при запуске приложения:
  `private void loadSongsFromDatabase() {
  songList.clear(); // Очистите список перед загрузкой
  Cursor cursor = dbHelper.getAllSongs();
  if (cursor.moveToFirst()) {
  do {
  @SuppressLint("Range") String track = cursor.getString(cursor.getColumnIndex("TrackTitle"));
  songList.add(track);
  } while (cursor.moveToNext());
  }
  cursor.close();
  adapter.notifyDataSetChanged();
  }`

![sk3.png](..%2F..%2F..%2F..%2Fsk3.png)

### 3. Работа с базой данных
Приложение создает таблицу "songs" с полями:
- ID;
- TrackTitle;
- Timestamp.
`@Override
public void onCreate(SQLiteDatabase db) {
db.execSQL("CREATE TABLE songs (" +
"ID INTEGER PRIMARY KEY AUTOINCREMENT," +
"TrackTitle TEXT," +
"Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
");");
}`

### 4. Обработка треков
- Добавление новой песни осуществляется при получении информации о текущем треке:
`private void addSongToDatabase(String track) {
SQLiteDatabase db = dbHelper.getWritableDatabase();
ContentValues values = new ContentValues();
values.put("TrackTitle", track);
db.insert("songs", null, values);
db.close();
}`
- Проверка на дубликаты перед добавлением новой записи в базу данных:
`private boolean isLastSongInDatabase(String track) {
Cursor cursor = db.rawQuery("SELECT TrackTitle FROM songs ORDER BY Timestamp DESC LIMIT 1", null);
boolean exists = false;
if (cursor.moveToFirst()) {
String lastTrack = cursor.getString(cursor.getColumnIndex("TrackTitle"));
exists = lastTrack.equals(track);
}
cursor.close();
return exists;
}`

