package com.example.omarf.weather;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.example.omarf.weather.Adapter.ForeCastDayAdapterRecycler;
import com.example.omarf.weather.Adapter.ForecastHourRecyclerAdapter;
import com.example.omarf.weather.ModelWu.ModelCurrentObservation.CurrentObservation;
import com.example.omarf.weather.ModelWu.ModelCurrentObservation.HourlyForecast.Hourly;
import com.example.omarf.weather.ModelWu.ModelCurrentObservation.HourlyForecast.HourlyForecast;
import com.example.omarf.weather.ModelWu.ModelCurrentObservation.ModelForecast.Forecast;
import com.example.omarf.weather.ModelWu.ModelCurrentObservation.ModelForecast.Forecastday;
import com.example.omarf.weather.ModelWu.ModelCurrentObservation.ModelForecast.Forecastday_;
import com.example.omarf.weather.ModelWu.ModelCurrentObservation.WeatherUnder;
import com.example.omarf.weather.databinding.FragmentWeatherBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherFragment extends Fragment {
    /*1bdb6580e4c12e06*/


    private String mCountyName;
    private String mCityName;

    public WeatherFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final FragmentWeatherBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_weather, container, false);
        String key= KeyPreferences.getKey(getActivity());
        String base_url=MainActivity.BASE_URL+key+"/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(base_url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        ApiEndPoint endPoint = retrofit.create(ApiEndPoint.class);


        /************************************Current Condition and some parts of details*************************************************/
        Call<WeatherUnder> res = endPoint.getCurrentCondition(mCountyName, mCityName);
        res.enqueue(new Callback<WeatherUnder>() {
            @Override
            public void onResponse(Call<WeatherUnder> call, Response<WeatherUnder> response) {

                CurrentObservation observation = response.body().getCurrentObservation();
                if (observation == null) {
                    getActivity().onBackPressed();
                    Toast.makeText(getActivity(), "Server error, please enter new query ", Toast.LENGTH_SHORT).show();
                } else {
                    String currentTemp = String.valueOf(observation.getTempC()) + "°";
                    String feelsLike = observation.getFeelslikeC() + "°";
                    String humidity = observation.getRelativeHumidity();
                    String visibility = observation.getVisibilityKm() + "Km";
                    String uv = observation.getUV();
                    String wind=observation.getWindMph()+" mph";
                    String windString=observation.getWindString();



                    dataBinding.currentTextView.setText(currentTemp);
                    dataBinding.locationName.setText(mCityName + " " + mCountyName);

                    dataBinding.feelsLikeTextView.setText(feelsLike);
                    dataBinding.humidityTextView.setText(humidity);
                    dataBinding.visibilityTextView.setText(visibility);
                    dataBinding.uvTextView.setText(uv);
                    dataBinding.windTextView.setText(wind);
                    dataBinding.windStringTextView.setText(windString);
                }

            }

            @Override
            public void onFailure(Call<WeatherUnder> call, Throwable t) {

            }
        });

        /************************************Forecast*************************************************/

        Call<Forecast> forecastCall = endPoint.getForeCast(mCountyName, mCityName);
        forecastCall.enqueue(new Callback<Forecast>() {
            @Override
            public void onResponse(Call<Forecast> call, Response<Forecast> response) {
                if(response.body().getForecast()==null){
                    return;
                }
                final ArrayList<Forecastday_> forecastday_s = (ArrayList<Forecastday_>) response.body().getForecast().getSimpleforecast().getForecastday();
                ArrayList<Forecastday> forecastday_text = (ArrayList<Forecastday>) response.body().getForecast().getTxtForecast().getForecastday();
              /*  if (forecastday_s == null || forecastday_text == null) {
                    return;
                }*/

                String todaySummary="Today- "+forecastday_text.get(0).getFcttextMetric();
                String tonightSummary="Tonight- "+forecastday_text.get(1).getFcttextMetric();
                dataBinding.todayForecastTextTextView.setText(todaySummary);
                dataBinding.tonightForecastTextTextView.setText(tonightSummary);

                String highCelcius = forecastday_s.get(0).getHigh().getCelsius()+"°";
                String lowestCelcius = forecastday_s.get(0).getLow().getCelsius()+"°";





               /* ForecastDayAdapter adapter = new ForecastDayAdapter(getActivity(), forecastday_s);*/
                dataBinding.highestTextView.setText(highCelcius);
                dataBinding.lowestTextView.setText(lowestCelcius);
               /* dataBinding.dayForecastListView.setAdapter(adapter);*/


                final ArrayList<Forecastday_> forecastFivedays = new ArrayList<Forecastday_>();
                final ArrayList<Forecastday_> forecastTendays = new ArrayList<Forecastday_>();

                for (int i = 0; i < 5; i++) {
                    forecastFivedays.add(forecastday_s.get(i));
                }
                forecastTendays.addAll(forecastday_s);



                /*forecastday_s.clear();
                forecastday_s.addAll(forecastFivedays);*/
                listSwaping(forecastFivedays, forecastday_s);
                final ForeCastDayAdapterRecycler adapterRecycler = new ForeCastDayAdapterRecycler(getActivity(), forecastday_s);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());

                dataBinding.dayForecastRecyclerView.setAdapter(adapterRecycler);
                dataBinding.dayForecastRecyclerView.setLayoutManager(layoutManager);


                dataBinding.fiveDayTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listSwaping(forecastFivedays, forecastday_s);
                        /*forecastday_s.clear();
                        forecastday_s.addAll(forecastFivedays);*/

                        adapterRecycler.notifyDataSetChanged();
                    }
                });

                dataBinding.tenDayTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*forecastday_s.clear();
                        forecastday_s.addAll(forecastTendays);*/
                        listSwaping(forecastTendays, forecastday_s);

                        adapterRecycler.notifyDataSetChanged();
                    }
                });

            }

            @Override
            public void onFailure(Call<Forecast> call, Throwable t) {

            }
        });


        /***********************************Hourly Forecast*************************************************/

        Call<Hourly> hourlyCall = endPoint.getHourlyForeCast(mCountyName, mCityName);
        hourlyCall.enqueue(new Callback<Hourly>() {
            @Override
            public void onResponse(Call<Hourly> call, Response<Hourly> response) {
                ArrayList<HourlyForecast> hourlyForecasts = (ArrayList<HourlyForecast>) response.body().getHourlyForecast();
                if (hourlyForecasts==null){
                    return;
                }
                ForecastHourRecyclerAdapter adapter = new ForecastHourRecyclerAdapter(getActivity(), hourlyForecasts);
                dataBinding.hourForecastRecyclerView.setAdapter(adapter);

                String condition = hourlyForecasts.get(0).getCondition();
                String iconUrl = hourlyForecasts.get(0).getIconUrl();
                dataBinding.weatherTextView.setText(condition);
                Picasso.with(getActivity()).load(iconUrl).into(dataBinding.weatherImageView);
                Picasso.with(getActivity()).load(iconUrl).into(dataBinding.weatherTypeDetailImageView);

            }

            @Override
            public void onFailure(Call<Hourly> call, Throwable t) {

            }
        });


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        dataBinding.hourForecastRecyclerView.setLayoutManager(layoutManager);


        return dataBinding.getRoot();
    }


    private void listSwaping(ArrayList<Forecastday_> sourceList, ArrayList<Forecastday_> destinationList) {
        destinationList.clear();
        destinationList.addAll(sourceList);
    }

    public void setQuery(String countryName, String cityName) {
        mCountyName = countryName;
        mCityName = cityName;

    }
}
