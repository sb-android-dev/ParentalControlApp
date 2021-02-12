package com.schoolmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.UiModeManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.schoolmanager.adapters.LanguageRecyclerAdapter;
import com.schoolmanager.adapters.ThemeRecyclerAdapter;
import com.schoolmanager.model.LanguageItem;
import com.schoolmanager.model.ThemeItem;
import com.schoolmanager.utilities.SpaceItemDecoration;
import com.schoolmanager.utilities.UserSessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChangeAppTheme extends BaseActivity {

    private static final String TAG = "change_theme_activity";

    private RecyclerView themeRecycler;

    private final List<ThemeItem> themeList = new ArrayList<>();
    private ThemeRecyclerAdapter adapter;

    private int themeValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_app_theme);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        UserSessionManager sessionManager = new UserSessionManager(this);

        themeRecycler = findViewById(R.id.rvThemeList);

        themeRecycler.setLayoutManager(new GridLayoutManager(this, 1,
                RecyclerView.VERTICAL, false));
        themeRecycler.addItemDecoration(new SpaceItemDecoration(this, RecyclerView.VERTICAL,
                1, R.dimen.recycler_vertical_offset, R.dimen.recycler_horizontal_offset,
                false));
        adapter = new ThemeRecyclerAdapter(this, themeList, (item, position) -> {
            sessionManager.setTheme(item.getThemeValue());
            MyApplication.initTheme(item.getThemeValue());
        });
        themeRecycler.setAdapter(adapter);

        refreshData(sessionManager);
    }

    private void refreshData(UserSessionManager sessionManager){
        themeValue = sessionManager.getTheme();
        getThemeList();
    }

    private void getThemeList() {
        themeList.clear();
        ThemeItem themeItem = new ThemeItem();
//        themeItem.setThemeName("System Default");
//        themeItem.setThemeValue(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
//        themeItem.setSelected(themeValue == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
//        themeList.add(themeItem);
//
//        themeItem = new ThemeItem();
        themeItem.setThemeName("Light Mode");
        themeItem.setThemeValue(AppCompatDelegate.MODE_NIGHT_NO);
        themeItem.setSelected(themeValue == AppCompatDelegate.MODE_NIGHT_NO);
        themeList.add(themeItem);

        themeItem = new ThemeItem();
        themeItem.setThemeName("Dark Mode");
        themeItem.setThemeValue(AppCompatDelegate.MODE_NIGHT_YES);
        themeItem.setSelected(themeValue == AppCompatDelegate.MODE_NIGHT_YES);
        themeList.add(themeItem);

        adapter.notifyDataSetChanged();
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