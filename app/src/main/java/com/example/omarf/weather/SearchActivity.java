package com.example.omarf.weather;

import android.databinding.DataBindingUtil;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;


import com.example.omarf.weather.ModelWu.ModelCurrentObservation.Search.Result;
import com.example.omarf.weather.ModelWu.ModelCurrentObservation.Search.SearchResult;
import com.example.omarf.weather.databinding.ActivitySearchBinding;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchActivity extends AppCompatActivity {
    private ActivitySearchBinding mSearchBinding;
    private ApiEndPoint mEndPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
/*
        super.onCreate(savedInstanceState);
        mSearchBinding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        setSupportActionBar(mSearchBinding.toolbar);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(WeatherFragment.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

         mEndPoint = retrofit.create(ApiEndPoint.class);*/


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.search_menu_item);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
               /* if (!query.isEmpty())
                    updateListView(query);*/
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty())
                    updateListView(newText);
                return true;
            }
        });
        return true;
    }

    private void updateListView(String query) {


        Call<SearchResult> searchResultCall = mEndPoint.getSearchResult(query);
        searchResultCall.enqueue(new Callback<SearchResult>() {
            @Override
            public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                final ArrayList<Result> results = (ArrayList<Result>) response.body().getResponse().getResults();
                ArrayList<String> countryAndCityNames = new ArrayList<String>();

                if (results == null)
                {
                   // countryAndCityNames.add("Not Found");
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this,
                            android.R.layout.simple_list_item_1, countryAndCityNames);
                    mSearchBinding.listViewSearch.setAdapter(adapter);
                    return;

                }

                for (Result result :
                        results) {
                    String countryName = result.getCountryName();
                    String cityName = result.getCity();
                    countryAndCityNames.add(cityName+ " , " + countryName );
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this,
                        android.R.layout.simple_list_item_1, countryAndCityNames);
                mSearchBinding.listViewSearch.setAdapter(adapter);

                mSearchBinding.listViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                      //  Toast.makeText(SearchActivity.this, , Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<SearchResult> call, Throwable t) {

            }
        });

    }
}
