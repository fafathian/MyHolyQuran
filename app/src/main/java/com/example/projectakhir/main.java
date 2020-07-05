package com.example.projectakhir;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static java.lang.String.valueOf;

public class main extends AppCompatActivity {

    //general
    private SQLiteDatabase db;
    private String[] surahName = {"Al-Fatihah", "Al-Baqarah", "AliImran", "An-Nisaa", "Al-Maidah", "Al-An'am", "Al-A'raf", "Al-Anfaal", "At-Taubah", "Yunus", "Huud",
            "Yusuf", "Ar-Ra'd", "Ibrahim", "Al-Hijr", "An-Nahl", "Al-Israa'", "Al-Kahfi", "Maryam", "Thaahaa", "Al-Anbiyaa", "Al-Hajj", "Al-Mu'minuun", "An-Nuur",
            "Al-Furqaan", "Asy-Syu'araa", "An-Naml", "Al-Qashash", "Al-'Ankabuut", "Ar-Ruum", "Luqman", "As-Sajdah", "Al-Ahzab", "Saba'", "Faathir", "YaaSiin",
            "Ash-Shaaffat", "Shaad", "Az-Zumar", "Al-Mu'min", "Fushshilat", "Asy-Syuura", "Az-Zukhruf", "Ad-Dukhaan", "Al-Jaatsiyah", "Al-Ahqaaf", "Muhammad",
            "Al-Fat-h", "Al-Hujuraat", "Qaaf", "Adz-Dzaariyat", "Ath-Thuur", "An-Najm", "Al-Qamar", "Ar-Rahmaan", "Al-Waaqi'ah", "Al-Hadiid", "Al-Mujaadilah",
            "Al-Hasyr", "Al-Mumtahanah", "Ash-Shaff", "Al-Jumuah", "Al-Munaafiqun", "At-Taghaabun", "Ath-Thalaaq", "At-Tahriim", "Al-Mulk", "Al-Qalam", "Al-Haaqqah",
            "Al-Ma'aarij", "Nuh", "Al-Jin", "Al-Muzzammil", "Al-Muddatstsir", "Al-Qiyaamah", "Al-Insaan", "Al-Mursalaat", "An-Naba'", "An-Naazi'aat", "'Abasa,42",
            "At-Takwiir", "Al-Infithaar", "Al-Muthaffif", "Al-Insyiqaaq", "Al-Buruuj", "Ath-Thaariq", "Al-A'laa", "Al-Ghaasyiyah", "Al-Fajr", "Al-Balad", "Asy-Syams",
            "Al-Lail", "Adh-Dhuhaa", "Al-Insyirah", "At-Tiin", "Al-'Alaq", "Al-Qadr", "Al-Bayyinah", "Az-Zalzalah", "Al-'Aadiyaat", "1Al-Qaari'ah", "At-Takaatsur",
            "Al-'Ashr", "Al-Humazah", "Al-Fiil", "Quraisy", "Al-Maa'uun", "Al-Kautsar", "Al-Kaafiruun", "An-Nashr", "Al-Lahab", "Al-Ikhlash", "Al-Falaq", "An-Naas"};
    private SharedPreferences settings;

    //component
    private ListView listview;
    private AutoCompleteTextView autoCompleteTextView;
    private ProgressBar pBar;
    private TextView txtSilahkanCari;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        listview = findViewById(R.id.listview);
        pBar = findViewById(R.id.pBar);
        txtSilahkanCari = findViewById(R.id.txtSilahkanCari);
        autoCompleteTextView = findViewById(R.id.autocomplete);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onLoadingCari();
                String selection = (String) adapterView.getItemAtPosition(i);
                int pos = -1;

                for (int j = 0; j < surahName.length; j++) {
                    if (surahName[j].equals(selection)) {
                        pos = j;
                        break;
                    }
                }


                List<Item> listData = new QuranDao(main.this).getListDataWhereCriteria("SuratID", pos + 1);
                String[] surah = new String[listData.size()];
                for (int j = 0; j < listData.size(); j++) {
                    surah[j] = valueOf(listData.get(j).getAyatText());
                }

                Adapter adapter = new Adapter(main.this, R.layout.item, listData);
                listview.setAdapter(adapter);

                onAfterLoadingCari();
            }
        });

        //init data pas pertama kali buka android saja
        settings = getSharedPreferences("myalqruan", 0);
        if (settings.getBoolean("firsttime", true)) {
            initData();
        } else {
            onAfterLoadingFirst();
        }
        setToListView();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //menu item selected
        switch (item.getItemId()) {
            case R.id.action_end:
                Intent intent = new Intent(main.this, MainActivity.class);
                main.this.startActivity(intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initData() {
        DBHelper dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();
        new doBackground().execute();
    }

    class doBackground extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            onLoadingFirst();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            insertFromFile(main.this, R.raw.quran);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            onAfterLoadingFirst();
        }
    }

    public void insertFromFile(Context context, int resourceId) {
        try {
            InputStream insertsStream = context.getResources().openRawResource(resourceId);
            BufferedReader insertReader = new BufferedReader(new InputStreamReader(insertsStream));

            db.beginTransaction();

            while (insertReader.ready()) {
                String insertStmt = insertReader.readLine();
                db.execSQL(insertStmt);
            }

            db.setTransactionSuccessful();
            db.endTransaction();

            insertReader.close();

            settings.edit().putBoolean("firsttime", false).apply();
        } catch (IOException e) {
            onAfterLoadingFirst();
            txtSilahkanCari.setText("Gagal Memasukkan Quran");
            e.printStackTrace();
        }
    }

    private void setToListView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, surahName);
        autoCompleteTextView.setAdapter(adapter);
    }

    private void onLoadingFirst() {
        txtSilahkanCari.setVisibility(View.GONE);
        listview.setVisibility(View.GONE);
        pBar.setVisibility(View.VISIBLE);
    }

    private void onAfterLoadingFirst() {
        pBar.setVisibility(View.GONE);
        listview.setVisibility(View.GONE);
        txtSilahkanCari.setVisibility(View.VISIBLE);
    }

    private void onLoadingCari() {
        txtSilahkanCari.setVisibility(View.GONE);
        listview.setVisibility(View.GONE);
        pBar.setVisibility(View.VISIBLE);
    }

    private void onAfterLoadingCari() {
        pBar.setVisibility(View.GONE);
        txtSilahkanCari.setVisibility(View.GONE);
        listview.setVisibility(View.VISIBLE);
    }

    public static Intent getActIntent(MainActivity mainActivity) {
        return new Intent(mainActivity, main.class);
    }

}