package com.nielsmasdorp.speculum.di.module;

import android.app.Application;

import com.nielsmasdorp.speculum.services.ForecastIOService;
import com.nielsmasdorp.speculum.services.GoogleCalendarService;
import com.nielsmasdorp.speculum.services.OctoprintService;
import com.nielsmasdorp.speculum.services.RedditService;
import com.nielsmasdorp.speculum.services.SNMPService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Module
public class ServiceModule {

    @Provides
    @Singleton
    public ForecastIOService provideForecastIOService() {

        return new ForecastIOService();
    }

    @Provides
    @Singleton
    public GoogleCalendarService provideGoogleCalendarService(Application application) {

        return new GoogleCalendarService(application);
    }

    @Provides
    @Singleton
    public RedditService redditService() {

        return new RedditService();
    }

    @Provides
    @Singleton
    public OctoprintService octoprintService(Application application) {
        return new OctoprintService(application);
    }

    @Provides
    @Singleton
    public SNMPService snmpService() {
        return new SNMPService();
    }
}
