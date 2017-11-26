package com.nielsmasdorp.speculum.presenters;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.afollestad.assent.Assent;
import com.afollestad.assent.AssentCallback;
import com.nielsmasdorp.speculum.R;
import com.nielsmasdorp.speculum.activity.MainActivity;
import com.nielsmasdorp.speculum.interactor.MainInteractor;
import com.nielsmasdorp.speculum.models.Configuration;
import com.nielsmasdorp.speculum.models.RedditPost;
import com.nielsmasdorp.speculum.models.Weather;
import com.nielsmasdorp.speculum.models.octoprint.Job;
import com.nielsmasdorp.speculum.services.OctoprintService;
import com.nielsmasdorp.speculum.services.SNMPService;
import com.nielsmasdorp.speculum.util.Constants;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class MainPresenter implements RecognitionListener, TextToSpeech.OnInitListener {

    private MainActivity view;
    private MainInteractor interactor;
    private Application application;
    private Configuration configuration;
    private SpeechRecognizer recognizer;
    private TextToSpeech textToSpeech;

    public MainPresenter(MainActivity view, MainInteractor interactor, Application application) {

        this.view = view;
        this.interactor = interactor;
        this.application = application;
    }

    /*
    Begin presenter methods
     */
    public void setConfiguration(Configuration configuration) {

        this.configuration = configuration;
    }

    public void start(boolean hasAccessToCalendar) {
        if (null != configuration) {
            startWeather();
            if (configuration.isVoiceCommands()) {
                initSpeechRecognitionService();
                setupTts();
            }
            if (!configuration.isSimpleLayout()) {
                startReddit();
                if (hasAccessToCalendar) {
                    startCalendar();
                }
            }
            startPrinter();
        }
    }

    private void updateData() {
        interactor.unSubscribe();
        if (null != configuration) {
            startWeather();
            if (!configuration.isSimpleLayout()) {
                startReddit();
                startCalendar();
            }
        }
    }

    public void showError(String error) {
        view.showError(error);
    }

    public void finish() {
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        view.hideListening();
        interactor.unSubscribe();
    }

    /*
    End presenter methods
     */

    /*
    Begin start background data methods
     */
    private void startWeather() {

        if (!Assent.isPermissionGranted(Assent.ACCESS_FINE_LOCATION)) {
            Assent.requestPermissions((AssentCallback) permissionResultSet -> onLocationGranted(), 69, Assent.ACCESS_FINE_LOCATION);
        } else {
            onLocationGranted();
        }
    }

    @SuppressLint("MissingPermission")
    private void onLocationGranted() {
        LocationManager manager = (LocationManager) this.application.getSystemService(Context.LOCATION_SERVICE);
        android.location.Location location = manager.getLastKnownLocation(manager.getProviders(true).get(0));

        interactor.loadWeather(location, configuration.isCelsius(), ((MainActivity)
            view).getString(R.string.forecast_api_key), new WeatherSubscriber());
    }

    private void startReddit() {
        interactor.loadTopRedditPost(configuration.getSubreddit(), new RedditSubscriber());
    }

    private void startPrinter() {
        interactor.loadPrinter(new PrinterSubscriber());
    }

    private void startCalendar() {
        interactor.loadCalendarEvents(new CalendarEventSubscriber());
        interactor.loadSNMP(new SNMPSubscriber());
    }
    /*
    End start background data methods
     */

    /*
    Begin speech recognition related initialisation
     */
    private void initSpeechRecognitionService() {
        interactor.getAssetsDirForSpeechRecognizer(new AssetSubscriber());
    }

    private void setupTts() {
        initTts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showError(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }
                });
    }

    public void setupRecognizer(File assetDir) {
        initRecognizer(assetDir)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        setListeningMode(Constants.KWS_SEARCH);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showError(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(Void aVoid) {
                    }
                });
    }

    private Observable<Void> initTts() {
        return Observable.defer(() -> {
            textToSpeech = new TextToSpeech(application, this);
            return Observable.empty();
        });
    }

    private Observable<Void> initRecognizer(File assetDir) {
        return Observable.defer(() -> {
            try {
                recognizer = defaultSetup()
                        .setAcousticModel(new File(assetDir, "en-us-ptm"))
                        .setDictionary(new File(assetDir, "cmudict-en-us.dict"))
                        .setKeywordThreshold(1e-45f)
                        .getRecognizer();
                recognizer.addListener(this);
                recognizer.addKeyphraseSearch(Constants.KWS_SEARCH, Constants.KEYPHRASE);
                recognizer.addKeywordSearch(Constants.COMMANDS_SEARCH, new File(assetDir, "commands.gram"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Observable.empty();
        });
    }
    /*
     End speech recognition related initialisation
     */


    /*
    Begin speech recognition logic methods
     */
    public void setListeningMode(String mode) {
        recognizer.stop();
        if (mode.equals(Constants.KWS_SEARCH)) {
            recognizer.startListening(mode);
        } else {
            recognizer.startListening(mode, (int) TimeUnit.MINUTES.toMillis(1));
        }
    }

    private void processVoiceCommand(String command) {
        switch (command) {
            case Constants.KEYPHRASE:
                speak(Constants.WAKE_NOTIFICATION);
                setListeningMode(Constants.COMMANDS_SEARCH);
                view.showListening();
                break;
            case Constants.SLEEP_PHRASE:
                speak(Constants.SLEEP_NOTIFICATION);
                setListeningMode(Constants.KWS_SEARCH);
                view.hideListening();
                break;
            case Constants.UPDATE_PHRASE:
                speak(Constants.UPDATE_NOTIFICATION);
                setListeningMode(Constants.COMMANDS_SEARCH);
                updateData();
                break;
            case Constants.MAP_PHRASE:
                speak(Constants.MAP_NOTIFICATION);
                setListeningMode(Constants.COMMANDS_SEARCH);
                showMap();
                break;
        }
    }

    private void showMap() {
    }

    private Observable<Void> timeOut(Integer seconds) {
        return Observable.defer(() -> {
            SystemClock.sleep(TimeUnit.SECONDS.toMillis(seconds));
            return Observable.empty();
        });
    }

    /*
    End speech recognition logic methods
     */

    /*
    Begin text to speech methods
     */
    @SuppressWarnings("deprecation")
    public void speak(String sentence) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String utteranceId = this.hashCode() + "";
            textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
            textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, map);
        }
    }
    /*
    End text to speech methods
     */


    /*
    Begin speech recognition logic methods
     */
    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null) return;
        String command = hypothesis.getHypstr();
        processVoiceCommand(command);
    }

    @Override
    public void onResult(Hypothesis hypothesis) {

    }

    @Override
    public void onError(Exception e) {
        showError(e.getLocalizedMessage());
        Log.e(MainActivity.class.getSimpleName(), e.toString());
    }

    @Override
    public void onTimeout() {
        speak(Constants.SLEEP_NOTIFICATION);
        setListeningMode(Constants.KWS_SEARCH);
        view.hideListening();
    }
     /*
    End speech recognition logic methods
     */

    /*
   Begin tts lifecycle methods
    */
    @Override
    public void onInit(int status) {
    }
    /*
   End tts lifecycle methods
    */

    private abstract class DataSubscriber<T> extends Subscriber<T> {
        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) { view.showError(e.getMessage());}
    }

    private final class WeatherSubscriber extends DataSubscriber<Weather> {
        @Override
        public void onNext(Weather weather) {
            view.displayCurrentWeather(weather, configuration.isSimpleLayout());
        }
    }

    private final class PrinterSubscriber extends DataSubscriber<Job> {
        @Override
        public void onNext(Job job) {
            Log.d("presenter", "Printer status: "+job.getState());
            view.displayPrinterJob(job);
        }
    }

    private final class SNMPSubscriber extends DataSubscriber<SNMPService.NetActivity> {
        @Override
        public void onNext(SNMPService.NetActivity act) {
            view.displayNetActivity(act);
        }
    }

    private final class RedditSubscriber extends DataSubscriber<RedditPost> {
        @Override
        public void onNext(RedditPost redditPost) {
            view.displayTopRedditPost(redditPost);
        }
    }

    private final class CalendarEventSubscriber extends DataSubscriber<String> {
        @Override
        public void onNext(String events) {
            view.displayCalendarEvents(events);
        }
    }

    private final class AssetSubscriber extends DataSubscriber<File> {
        @Override
        public void onNext(File assetDir) {
            setupRecognizer(assetDir);
        }
    }
}
