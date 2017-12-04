package com.nielsmasdorp.speculum.services;

import android.util.Log;

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

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by vfierce on 11/25/17.
 */

public class SNMPService {

    public static class NetActivity {
        public long upload;
        public long download;

        public NetActivity(long upload, long download) {
            this.upload = upload;
            this.download = download;
        }

        public String toString() {
            return "Upload " + upload + ", download " + download;
        }

        public static NetActivity fromResponse(ResponseEvent resp) {
            return new NetActivity(resp.getResponse().get(0).getVariable().toLong(),resp.getResponse().get(1).getVariable().toLong());
        }
    }

    private static class Requester {
        private PDU requestPDU;
        private CommunityTarget target;
        private Snmp snmp = null;

        Requester() {

        }

        void bind() throws IOException {
            TransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();

            org.snmp4j.smi.Address targetAddress = GenericAddress.parse("udp:10.0.0.1/161");
            target = new CommunityTarget();
            target.setCommunity(new OctetString("public"));
            target.setAddress(targetAddress);
            target.setVersion(SnmpConstants.version2c);

            requestPDU = new PDU();
            requestPDU.add(new VariableBinding(new OID(".1.3.6.1.2.1.2.2.1.10.2"))); // WAN download
            requestPDU.add(new VariableBinding(new OID(".1.3.6.1.2.1.2.2.1.16.1"))); // WAN upload
            requestPDU.setType(PDU.GET);
        }

        ResponseEvent send() throws IOException {
            return snmp.send(requestPDU, target, null);
        }
    }

    Requester req = null;

    public SNMPService() {
    }

    public Observable<NetActivity> getActivity() {
        Random rnd = new Random();
        return Observable.just(new NetActivity(rnd.nextLong(), rnd.nextLong()));
        /*if (req == null) {
            req = new Requester();
            try {
                req.bind();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Observable.create(new Observable.OnSubscribe<NetActivity>() {
            @Override
            public void call(Subscriber<? super NetActivity> subscriber) {
                try {
                    ResponseEvent resp = req.send();
                    if (resp.getError() != null) {
                        subscriber.onError(resp.getError());
                    } else {
                        subscriber.onNext(NetActivity.fromResponse(resp));
                        subscriber.onCompleted();
                    }
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        });*/
    }
}
