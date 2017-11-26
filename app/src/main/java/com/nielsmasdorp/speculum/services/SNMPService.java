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
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vfierce on 11/25/17.
 */

public class SNMPService {

    public class NetActivity {
        public long upload;
        public long download;

        public NetActivity(long upload, long download) {
            this.upload = upload;
            this.download = download;
        }

        public String toString() {
            return "Upload " + upload + ", download " + download;
        }
    }

    private PDU requestPDU;
    private CommunityTarget target;
    private Snmp snmp = null;

    private void setupNetwork() throws IOException {
        if (snmp == null) {
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
    }

    private void runQuery(Subscriber<? super NetActivity> subscriber) throws IOException {
        setupNetwork();
        ResponseEvent evt = snmp.send(requestPDU, target, null);
        if (evt == null) {
            subscriber.onError(evt.getError());
        } else {
            NetActivity activity = new NetActivity(evt.getResponse().get(0).getVariable().toLong(),evt.getResponse().get(1).getVariable().toLong());
            subscriber.onNext(activity);
        }
    }

    public Observable<NetActivity> getActivity() {
        return Observable.create(new Observable.OnSubscribe<NetActivity>() {
            @Override
            public void call(Subscriber<? super NetActivity> subscriber) {
                try {
                    runQuery(subscriber);
                } catch (IOException e) {
                    Log.e("snmp", "query failure", e);
                    subscriber.onError(e);
                }
            }
        });
    }
}
