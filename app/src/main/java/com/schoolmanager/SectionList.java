package com.schoolmanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.schoolmanager.adapters.SectionRecyclerAdapter;
import com.schoolmanager.common.Common;
import com.schoolmanager.model.ClassItem;
import com.schoolmanager.model.SectionItem;
import com.schoolmanager.utilities.RecyclerViewWithEmptyView;
import com.schoolmanager.utilities.SpaceItemDecoration;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SectionList extends BaseActivity {

    private static final String TAG = "section_list_activity";

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerViewWithEmptyView sectionRecycler;
    private LinearLayout noDataLayout;

    private ClassItem classItem;
    private List<SectionItem> sectionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section_list);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeRefreshLayout = findViewById(R.id.srlSection);
        sectionRecycler = findViewById(R.id.rvSection);
        noDataLayout = findViewById(R.id.llNoData);

        if(getIntent() != null) {
            classItem = getIntent().getParcelableExtra("class_item");
            sectionList = classItem.getSections();
        }

        sectionRecycler.setLayoutManager(new GridLayoutManager(this, 3,
                RecyclerView.VERTICAL, false));
        sectionRecycler.addItemDecoration(new SpaceItemDecoration(this, RecyclerView.VERTICAL,
                3, R.dimen.recycler_vertical_offset, R.dimen.recycler_horizontal_offset,
                false));
        sectionRecycler.setEmptyView(noDataLayout);
        SectionRecyclerAdapter adapter = new SectionRecyclerAdapter(this,
                sectionList, (sectionItem, position) -> {
//            Toast.makeText(SectionList.this, sectionItem.getSectionName(), Toast.LENGTH_SHORT).show();
            Intent nextIntent = new Intent(SectionList.this, StudentsList.class);
            nextIntent.putExtra("section_id", sectionItem.getSectionId());
            nextIntent.putExtra("class_id", classItem.getClassId());
            startActivity(nextIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        sectionRecycler.setAdapter(adapter);

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        swipeRefreshLayout.setColorSchemeResources(typedValue.resourceId);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void getListOfSection(String userId, String appCode){
        AndroidNetworking.post(Common.BASE_URL)
                .addBodyParameter("app_code", appCode) /* App code for app */
                .addBodyParameter("user_id", userId) /* User id of logged in user */
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e(TAG, "onError: " + anError.getLocalizedMessage());
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}