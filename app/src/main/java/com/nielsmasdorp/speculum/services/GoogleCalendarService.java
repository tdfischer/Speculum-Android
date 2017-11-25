package com.nielsmasdorp.speculum.services;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;

import com.nielsmasdorp.speculum.R;
import com.nielsmasdorp.speculum.util.Constants;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.util.ICalDate;
import retrofit2.Retrofit;
import rx.Observable;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class GoogleCalendarService {

    private Application application;

    public GoogleCalendarService(Application application) {

        this.application = application;
    }

    @SuppressWarnings("all")
    public Observable<String> getCalendarEvents() {
        URL calendarUrl = null;
        try {
            calendarUrl = new URL("https", "calendar.google.com", "/calendar/ical/t2ciqseie8d6177ei9qi9q2lvo%40group.calendar.google.com/public/basic.ics");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        ICalendar ical = null;
        try {
            ical = Biweekly.parse(calendarUrl.openStream()).first();
        } catch (IOException e) {
            e.printStackTrace();
        }
        VEvent nextEvent = null;
        Date now = new Date();

        for( VEvent evt : ical.getEvents()) {
            ICalDate start = evt.getDateStart().getValue();
            if (nextEvent == null) {
                nextEvent = evt;
                continue;
            }
            if (start.after(now) && start.before(nextEvent.getDateStart().getValue())) {
                nextEvent = evt;
            }
        }

        return Observable.just(nextEvent.getDateStart().getValue().toLocaleString() + " - " + nextEvent.getSummary().getValue());
    }
}
