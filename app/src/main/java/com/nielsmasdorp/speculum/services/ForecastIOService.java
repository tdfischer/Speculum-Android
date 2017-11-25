package com.nielsmasdorp.speculum.services;

import android.app.Application;
import android.text.format.DateFormat;
import android.util.Log;

import com.nielsmasdorp.speculum.R;
import com.nielsmasdorp.speculum.models.ForecastDayWeather;
import com.nielsmasdorp.speculum.models.Weather;
import com.nielsmasdorp.speculum.models.forecast.Currently;
import com.nielsmasdorp.speculum.models.forecast.DayForecast;
import com.nielsmasdorp.speculum.models.forecast.ForecastResponse;
import com.nielsmasdorp.speculum.util.Constants;
import com.nielsmasdorp.speculum.util.WeatherIconGenerator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class ForecastIOService {

    private ForecastIOApi forecastIOApi;

    public ForecastIOService() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.FORECAST_BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        forecastIOApi = retrofit.create(ForecastIOApi.class);
    }

    public Observable<Weather> getCurrentWeather(ForecastResponse response,
                                                 WeatherIconGenerator iconGenerator,
                                                 Application application) {

        boolean metric = true;
        boolean is24HourFormat = DateFormat.is24HourFormat(application);
        Currently currently = response.getCurrently();

        String distanceUnit = metric ? Constants.DISTANCE_METRIC : Constants.DISTANCE_IMPERIAL;
        String pressureUnit = metric ? Constants.PRESSURE_METRIC : Constants.PRESSURE_IMPERIAL;
        String speedUnit = metric ? Constants.SPEED_METRIC : Constants.SPEED_IMPERIAL;
        String temperatureUnit = metric ? Constants.TEMPERATURE_METRIC : Constants.TEMPERATURE_IMPERIAL;

        SimpleDateFormat dateFormatter = new SimpleDateFormat(metric ? "d MMM" : "MMM d", Locale.getDefault());

        // Convert degrees to cardinal directions for wind
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
        String direction = directions[(int) Math.round((currently.getWindBearing() % 360) / 45)];

        List<ForecastDayWeather> forecast = new ArrayList<>();

        int AMOUNT_OF_DAYS_IN_FORECAST = 4;
        for (int i = 0; i < AMOUNT_OF_DAYS_IN_FORECAST; i++) {
            DayForecast f = response.getForecast().getData().get(i+1);
            String date = dateFormatter.format(new Date((long) f.getTime() * 1000));
            int intTemp = (f.getTemperatureMin().intValue() + f.getTemperatureMax().intValue()) / 2;
            String temp = intTemp + "º" + temperatureUnit;
            int iconId = iconGenerator.getIcon(f.getIcon());
            forecast.add(new ForecastDayWeather(iconId, temp, i == 0 ? application.getString(R.string.tomorrow) : date));
        }

        return Observable.just(new Weather.Builder()
                .iconId(iconGenerator.getIcon(response.getCurrently().getIcon()))
                .summary(currently.getSummary())
                .temperature(currently.getTemperature().intValue() + "º" + temperatureUnit)
                .lastUpdated(new SimpleDateFormat(!is24HourFormat ? "h:mm" : "H:mm", Locale.getDefault()).format(new Date((long) currently.getTime() * 1000)))
                .windInfo(currently.getWindSpeed().intValue() + speedUnit + " " + direction + " | " + currently.getApparentTemperature().intValue() + "º" + temperatureUnit)
                .humidityInfo((int) (currently.getHumidity() * 100) + "%")
                .pressureInfo(currently.getPressure().intValue() + pressureUnit)
                .visibilityInfo(currently.getVisibility().intValue() + distanceUnit)
                .forecast(forecast)
                .build());
    }

    public ForecastIOApi getApi() {

        return forecastIOApi;
    }

    public interface ForecastIOApi {

        @GET("{apiKey}/{latLong}")
        Observable<ForecastResponse> getCurrentWeatherConditions(@Path("apiKey") String apiKey, @Path("latLong") String latLong, @Query("units") String units);
    }
}
