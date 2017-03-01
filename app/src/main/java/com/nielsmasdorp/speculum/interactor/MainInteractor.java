package com.nielsmasdorp.speculum.interactor;

import com.nielsmasdorp.speculum.models.Weather;
import com.nielsmasdorp.speculum.models.RedditPost;

import java.io.File;

import rx.Subscriber;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public interface MainInteractor {

    void loadCalendarEvents(int updateDelay, Subscriber<String> subscriber);

    void loadTopRedditPost(String subreddit, int updateDelay, Subscriber<RedditPost> subscriber);

    void loadWeather(String location, boolean celsius, int updateDelay, String apiKey, Subscriber<Weather> subscriber);

    void getAssetsDirForSpeechRecognizer(Subscriber<File> subscriber);

    void unSubscribe();
}
