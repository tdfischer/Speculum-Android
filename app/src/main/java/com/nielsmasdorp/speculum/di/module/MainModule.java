package com.nielsmasdorp.speculum.di.module;

import android.app.Application;

import com.nielsmasdorp.speculum.activity.MainActivity;
import com.nielsmasdorp.speculum.di.PerActivity;
import com.nielsmasdorp.speculum.interactor.MainInteractor;
import com.nielsmasdorp.speculum.presenters.MainPresenter;
import com.nielsmasdorp.speculum.services.ForecastIOService;
import com.nielsmasdorp.speculum.services.GoogleCalendarService;
import com.nielsmasdorp.speculum.services.OctoprintService;
import com.nielsmasdorp.speculum.services.RedditService;
import com.nielsmasdorp.speculum.services.SNMPService;
import com.nielsmasdorp.speculum.util.WeatherIconGenerator;

import dagger.Module;
import dagger.Provides;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Module
public class MainModule {

    private MainActivity mainView;

    public MainModule(MainActivity mainView) {

        this.mainView = mainView;
    }

    @Provides
    @PerActivity
    public MainPresenter provideMainPresenter(MainInteractor interactor, Application application) {

        return new MainPresenter(mainView, interactor, application);
    }

    @Provides
    @PerActivity
    public MainInteractor provideMainInteractor(Application application,
                                                ForecastIOService forecastIOService,
                                                GoogleCalendarService googleMapService,
                                                RedditService redditService,
                                                WeatherIconGenerator iconGenerator,
                                                OctoprintService octoprintService,
                                                SNMPService snmpService) {

        return new MainInteractor(application, forecastIOService, googleMapService, redditService, iconGenerator, octoprintService, snmpService);
    }
}
