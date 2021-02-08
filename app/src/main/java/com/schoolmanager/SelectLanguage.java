package com.schoolmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.schoolmanager.adapters.DriversRecyclerAdapter;
import com.schoolmanager.adapters.LanguageRecyclerAdapter;
import com.schoolmanager.common.Common;
import com.schoolmanager.model.DriverItem;
import com.schoolmanager.model.LanguageItem;
import com.schoolmanager.utilities.ConnectionDetector;
import com.schoolmanager.utilities.SpaceItemDecoration;
import com.schoolmanager.utilities.UserSessionManager;
import com.zeugmasolutions.localehelper.LocaleHelper;
import com.zeugmasolutions.localehelper.Locales;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SelectLanguage extends BaseActivity {

    private static final String TAG = "language_activity";

    private RecyclerView languageRecycler;

    private final List<LanguageItem> languageList = new ArrayList<>();
    private LanguageRecyclerAdapter adapter;

    private String langCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_language);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        UserSessionManager sessionManager = new UserSessionManager(this);
        langCode = sessionManager.getLanguage();

        languageRecycler = findViewById(R.id.rvDriverList);

        languageRecycler.setLayoutManager(new GridLayoutManager(this, 1,
                RecyclerView.VERTICAL, false));
        languageRecycler.addItemDecoration(new SpaceItemDecoration(this, RecyclerView.VERTICAL,
                1, R.dimen.recycler_vertical_offset, R.dimen.recycler_horizontal_offset,
                false));
        adapter = new LanguageRecyclerAdapter(this, languageList, (item, position) -> {
            sessionManager.setLanguage(item.getLangCode());
            updateLocale(new Locale(item.getLangCode(), item.getLangCountry()));
        });
        languageRecycler.setAdapter(adapter);

        getLanguageList();

    }

    private void getLanguageList() {
        languageList.clear();
        LanguageItem languageItem = new LanguageItem();
        languageItem.setLangName(getString(R.string.english));
        languageItem.setLangCode("en");
        languageItem.setLangCountry("IN");
        languageItem.setSelected(langCode.equals("en"));
        languageList.add(languageItem);

        languageItem = new LanguageItem();
        languageItem.setLangName(getString(R.string.telugu));
        languageItem.setLangCode("te");
        languageItem.setLangCountry("IN");
        languageItem.setSelected(langCode.equals("te"));
        languageList.add(languageItem);

        adapter.notifyDataSetChanged();
    }

    @Override
    public void updateLocale(@NotNull Locale locale) {
        super.updateLocale(locale);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

}