package com.nielsmasdorp.speculum.interactor;

import android.app.Application;
import android.util.Log;

import com.nielsmasdorp.speculum.models.RedditPost;
import com.nielsmasdorp.speculum.models.Weather;
import com.nielsmasdorp.speculum.models.octoprint.Job;
import com.nielsmasdorp.speculum.services.OctoprintService;
import com.nielsmasdorp.speculum.services.SNMPService;
import com.nielsmasdorp.speculum.util.Observables;
import com.nielsmasdorp.speculum.services.ForecastIOService;
import com.nielsmasdorp.speculum.services.GoogleCalendarService;
import com.nielsmasdorp.speculum.services.RedditService;
import com.nielsmasdorp.speculum.util.Constants;
import com.nielsmasdorp.speculum.util.WeatherIconGenerator;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import edu.cmu.pocketsphinx.Assets;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.schedulers.TimeInterval;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class MainInteractor {

    private static int AMOUNT_OF_RETRIES = 3;
    private static int DELAY_IN_SECONDS = 4;

    private Application application;
    private ForecastIOService forecastIOService;
    private GoogleCalendarService googleCalendarService;
    private RedditService redditService;
    private WeatherIconGenerator weatherIconGenerator;
    private CompositeSubscription compositeSubscription;
    private OctoprintService octoprintService;
    private SNMPService snmpService;

    public MainInteractor(Application application, ForecastIOService forecastIOService,
                          GoogleCalendarService googleCalendarService, RedditService redditService,
                          WeatherIconGenerator weatherIconGenerator, OctoprintService octoprintService, SNMPService snmpService) {

        this.application = application;
        this.forecastIOService = forecastIOService;
        this.googleCalendarService = googleCalendarService;
        this.redditService = redditService;
        this.weatherIconGenerator = weatherIconGenerator;
        this.octoprintService = octoprintService;
        this.snmpService = snmpService;
        this.compositeSubscription = new CompositeSubscription();
    }

    public void loadCalendarEvents(Subscriber<String> subscriber) {

        compositeSubscription.add(Observable.interval(0, 60, TimeUnit.MINUTES)
                .flatMap(ignore -> googleCalendarService.getCalendarEvents())
                .retryWhen(Observables.exponentialBackoff(DELAY_IN_SECONDS, TimeUnit.SECONDS))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribe(subscriber));
    }

    public void loadSNMP(Subscriber<SNMPService.NetActivity> subscriber) {
        compositeSubscription.add(Observable.interval(0, 80, TimeUnit.MILLISECONDS)
                .flatMap(ignore -> snmpService.getActivity())
                .retry()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribe(subscriber));
    }

    public void loadTopRedditPost(String subreddit, Subscriber<RedditPost> subscriber) {

        compositeSubscription.add(Observable.interval(0, 30, TimeUnit.MINUTES)
                .flatMap(ignore -> redditService.getApi().getTopRedditPostForSubreddit(subreddit, Constants.REDDIT_LIMIT))
                .flatMap(redditService::getRedditPost)
                .retryWhen(Observables.exponentialBackoff(DELAY_IN_SECONDS, TimeUnit.SECONDS))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribe(subscriber));
    }

    public void loadPrinter(Subscriber<Job> subscriber) {
        compositeSubscription.add(Observable.interval(0, 30, TimeUnit.MINUTES)
                .flatMap(ignore -> octoprintService.getOctoprinter())
                .flatMap(printer -> printer.getJob())
                .retry()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribe(subscriber));
    }

    public void loadWeather(android.location.Location location, boolean celsius, String apiKey, Subscriber<Weather> subscriber) {

        final String query = celsius ? Constants.WEATHER_QUERY_SECOND_CELSIUS : Constants.WEATHER_QUERY_SECOND_FAHRENHEIT;
        String latlng = location.getLatitude() + "," + location.getLongitude();

        compositeSubscription.add(Observable.interval(0, 30, TimeUnit.MINUTES)
                .flatMap(ignore -> forecastIOService.getApi().getCurrentWeatherConditions(apiKey, latlng, query))
                .flatMap(response -> forecastIOService.getCurrentWeather(response, weatherIconGenerator, application))
                .retryWhen(Observables.exponentialBackoff(DELAY_IN_SECONDS, TimeUnit.SECONDS))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribe(subscriber));
    }

    public void getAssetsDirForSpeechRecognizer(Subscriber<File> subscriber) {

        Observable.defer(() -> {
            try {
                Assets assets = new Assets(application);
                File assetDir = assets.syncAssets();
                return Observable.just(assetDir);
            } catch (IOException e) {
                throw new RuntimeException("IOException: " + e.getLocalizedMessage());
            }
        })
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void unSubscribe() {
        compositeSubscription.clear();
    }
}
