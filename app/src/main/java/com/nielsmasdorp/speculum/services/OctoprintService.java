package com.nielsmasdorp.speculum.services;

import android.app.Application;
import android.content.Context;
import android.location.Address;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.nielsmasdorp.speculum.models.octoprint.Job;
import com.nielsmasdorp.speculum.util.Constants;

import org.json.JSONObject;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.transport.UdpTransportMapping;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vfierce on 11/25/17.
 */

public class OctoprintService {

    public class Octoprinter {
        private OctoprintApi api;
        private CompositeSubscription compositeSubscription;

        public Octoprinter(HttpUrl baseurl) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseurl)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            this.api = retrofit.create(OctoprintApi.class);
            this.compositeSubscription = new CompositeSubscription();
        }

        public Observable<Job> getJob() {
            return Observable.create(new Observable.OnSubscribe<Job>() {
                @Override
                public void call(Subscriber<? super Job> subscriber) {
                    compositeSubscription.add(Observable.interval(5, TimeUnit.SECONDS)
                            .flatMap(ignore -> Octoprinter.this.api.getJob("CC7C827FD75843FD98199FC56654D2D4"))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .unsubscribeOn(Schedulers.io())
                            .subscribe(subscriber));
                }
            });
        }
    }

    private Application application;

    public OctoprintService(Application app) {
        this.application = app;
    }

    public rx.Observable<Octoprinter> getOctoprinter() {
        NsdManager nsd = (NsdManager) this.application.getSystemService(Context.NSD_SERVICE);
        return rx.Observable.create(new rx.Observable.OnSubscribe<Octoprinter>() {
            @Override
            public void call(Subscriber<? super Octoprinter> subscriber) {
                nsd.discoverServices("_octoprint._tcp", NsdManager.PROTOCOL_DNS_SD, new NsdManager.DiscoveryListener() {
                    @Override
                    public void onStartDiscoveryFailed(String s, int i) {
                        subscriber.onError(new Error(s));
                    }

                    @Override
                    public void onStopDiscoveryFailed(String s, int i) {
                        subscriber.onError(new Error(s));
                    }

                    @Override
                    public void onDiscoveryStarted(String s) {
                        Log.d("dnssd", "Service discovery started");
                    }

                    @Override
                    public void onDiscoveryStopped(String s) {
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
                        Log.d("dnssd", "Resolving service..." + nsdServiceInfo.toString());
                        nsd.resolveService(nsdServiceInfo, new NsdManager.ResolveListener() {
                            @Override
                            public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
                                subscriber.onError(new Error("Unable to resolve "+ i));
                            }

                            @Override
                            public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
                                Log.d("dnssd", "Found service " + nsdServiceInfo.getServiceName());
                                HttpUrl url;
                                try {
                                    url = new HttpUrl.Builder()
                                            .scheme("http")
                                            .host(nsdServiceInfo.getHost().getHostAddress())
                                            .port(nsdServiceInfo.getPort())
                                            .addPathSegment("api")
                                            .addPathSegment("")
                                            .build();

                                    subscriber.onNext(new Octoprinter(url));
                                } catch (Throwable e) {
                                    subscriber.onError(e);
                                }

                            }
                        });
                        nsd.stopServiceDiscovery(this);
                    }

                    @Override
                    public void onServiceLost(NsdServiceInfo nsdServiceInfo) {

                    }
                });
            }
        });
    }


    public interface OctoprintApi {
        @GET("job")
        Observable<Job> getJob(@Header("X-Api-Key") String key);
    }
}
