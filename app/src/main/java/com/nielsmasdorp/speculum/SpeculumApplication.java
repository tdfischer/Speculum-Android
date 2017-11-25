package com.nielsmasdorp.speculum;

import android.app.Application;

import com.nielsmasdorp.speculum.activity.MainActivity;
import com.nielsmasdorp.speculum.di.component.ApplicationComponent;
import com.nielsmasdorp.speculum.di.component.DaggerApplicationComponent;
import com.nielsmasdorp.speculum.di.component.MainComponent;
import com.nielsmasdorp.speculum.di.module.AppModule;
import com.nielsmasdorp.speculum.di.module.MainModule;
import com.nielsmasdorp.speculum.di.module.ServiceModule;
import com.nielsmasdorp.speculum.di.module.UtilModule;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class SpeculumApplication extends Application {

    private ApplicationComponent applicationComponent;
    private MainComponent mainComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationComponent = DaggerApplicationComponent.builder()
                .appModule(new AppModule(this))
                .serviceModule(new ServiceModule())
                .utilModule(new UtilModule())
                .build();
    }

    public MainComponent createMainComponent(MainActivity view) {
        mainComponent = applicationComponent.plus(new MainModule(view));
        return mainComponent;
    }

    public void releaseMainComponent() {
        mainComponent = null;
    }
}
