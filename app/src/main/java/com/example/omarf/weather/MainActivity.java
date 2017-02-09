package com.example.omarf.weather;

import android.databinding.DataBindingUtil;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.example.omarf.weather.databinding.ActivityMainBinding;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    /*"https://api.wunderground.com/api/1bdb6580e4c12e06/"*/
/*"https://api.darksky.net/forecast/2117c1a0960a5a90a9ee02546013ffb8/"*/

    public static final String BASE_URL = "http://api.wunderground.com/api/";

    private static final String TAG = "MainActivityTag";

    private ApiEndPoint mEndPoint;
    private ActivityMainBinding mMainBinding;
    private FragmentManager mFragmentManager;
    private SearchView mSearchView;
    private boolean isFastSearch;
    private String[] mKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

      /*  mKeys = new String[]{
                "1bdb6580e4c12e06",
                "08ded16a63230cfd",
                "f511f9ee1428e329"
        };*/

        String key= KeyPreferences.getKey(this);
        String base_url=BASE_URL+key+"/";


        mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(base_url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mEndPoint = retrofit.create(ApiEndPoint.class);
        setSupportActionBar(mMainBinding.toolbar);

        mFragmentManager = getSupportFragmentManager();

        loadWeatherFragment("Bangladesh", "dhaka");

      /*  Fragment fragment= mFragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment==null){
            fragment=new WeatherFragment();
            mFragmentManager.beginTransaction()
                    .add(R.id.fragment_container,fragment)
                    .commit();
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.search_menu_item);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!isFastSearch){

                    if (!query.isEmpty()) {
                        updateListView(query);
                        mMainBinding.listViewSearch.setVisibility(View.VISIBLE);
                        mMainBinding.fragmentContainer.setVisibility(View.GONE);
                    } else {
                        mMainBinding.listViewSearch.setVisibility(View.GONE);
                        mMainBinding.fragmentContainer.setVisibility(View.VISIBLE);
                    }
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (isFastSearch){

                    if (!newText.isEmpty())
                    {
                        updateListView(newText);
                        mMainBinding.listViewSearch.setVisibility(View.VISIBLE);
                        mMainBinding.fragmentContainer.setVisibility(View.GONE);
                    }
                    else {
                        mMainBinding.listViewSearch.setVisibility(View.GONE);
                        mMainBinding.fragmentContainer.setVisibility(View.VISIBLE);
                    }
                }

                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       /* switch (item.getItemId()){
            case R.id.search_menu_item:
                startActivity(new Intent(MainActivity.this,SearchActivity.class));
                return true;
        }*/
        return super.onOptionsItemSelected(item);
    }


    private void updateListView(String query) {


        Call<SearchResult> searchResultCall = mEndPoint.getSearchResult(query);
        searchResultCall.enqueue(new Callback<SearchResult>() {
            @Override
            public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                final ArrayList<Result> results = (ArrayList<Result>) response.body().getResponse().getResults();
                ArrayList<String> countryAndCityNames = new ArrayList<String>();
                final ArrayList<String> cityNames = new ArrayList<String>();
                final ArrayList<String> countryNames = new ArrayList<String>();

                if (results == null) {
                    // countryAndCityNames.add("Not Found");
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                            android.R.layout.simple_list_item_1, countryAndCityNames);
                    mMainBinding.listViewSearch.setAdapter(adapter);
                    return;

                }


                for (Result result :
                        results) {
                    String countryName = result.getCountryName();
                    String cityName = result.getCity();
                    countryNames.add(countryName);
                    cityNames.add(cityName);
                    countryAndCityNames.add(cityName + " , " + countryName);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_list_item_1, countryAndCityNames);
                mMainBinding.listViewSearch.setAdapter(adapter);

                mMainBinding.listViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        loadWeatherFragment(countryNames.get(i), cityNames.get(i));
                        mSearchView.setIconified(true);
                        mSearchView.setIconified(true);


                        //Toast.makeText(MainActivity.this,countryNames.get(i)+" "+cityNames.get(i) , Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<SearchResult> call, Throwable t) {

            }
        });


    }

    private void loadWeatherFragment(String countryName, String cityName) {
        Fragment fragment = mFragmentManager.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            WeatherFragment weatherFragment = new WeatherFragment();
            weatherFragment.setQuery(countryName, cityName);

            mFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, weatherFragment)
                    .commit();

        } else {
            WeatherFragment weatherFragment = new WeatherFragment();
            weatherFragment.setQuery(countryName, cityName);
            mFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, weatherFragment)
                    .addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();

            mMainBinding.listViewSearch.setVisibility(View.GONE);
            mMainBinding.fragmentContainer.setVisibility(View.VISIBLE);
        }


    }

    public void onClickKey(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.key_one:
                KeyPreferences.setKey(this,   "1bdb6580e4c12e06");
                break;
            case R.id.key_two:
                KeyPreferences.setKey(this, "08ded16a63230cfd");
                break;
            case R.id.key_three:
                KeyPreferences.setKey(this, "f511f9ee1428e329");
                break;

        }
    }

    public void onClickSearch(MenuItem item) {
        switch (item.getItemId()){
            case R.id.normal_search:
                isFastSearch=false;
                break;
            case R.id.fast_search:
                isFastSearch=true;
                break;
        }
    }
}
