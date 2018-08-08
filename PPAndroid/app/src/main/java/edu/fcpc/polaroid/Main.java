package edu.fcpc.polaroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

public class Main extends Activity implements ServiceListener {
    private WifiManager.MulticastLock multicastLock;
    private JmDNS jmDNS;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup mDNS
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            multicastLock = wifiManager.createMulticastLock("PPMulticastLock");
            multicastLock.setReferenceCounted(true);
            multicastLock.acquire();
            jmDNS = JmDNS.create();
            jmDNS.addServiceListener("_http._tcp.local.", this);
        }catch(IOException ioe){
            try {
                jmDNS.removeServiceListener("_http._tcp.local.", this);
                jmDNS.close();
            }catch(IOException ioe2){
                // Do nothing
            }
        }

        // Setup UI
        setContentView(R.layout.main_frame);
        if (findViewById(R.id.main_frame) != null) {
            if (savedInstanceState != null) {
                return;
            }

            Fragment10 fragment10 = new Fragment10();
            fragment10.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction().add(R.id.main_frame, fragment10).commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0)
            getFragmentManager().popBackStack();
        else {
            new AlertDialog.Builder(this)
                    .setTitle("Quit")
                    .setCancelable(false)
                    .setMessage("Continue closing the app?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                            try {
                                jmDNS.removeServiceListener("_http._tcp.local.", Main.this);
                                jmDNS.close();
                            }catch(IOException ioe2){
                                // Do nothing
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Main.super.onBackPressed();
                        }
                    })
                    .create().show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            jmDNS.removeServiceListener("_http._tcp.local.", this);
            jmDNS.close();
        }catch(IOException ioe2){
            // Do nothing
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Setup mDNS
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            multicastLock = wifiManager.createMulticastLock("HeeereDnssdLock");
            multicastLock.setReferenceCounted(true);
            multicastLock.acquire();
            jmDNS = JmDNS.create();
            jmDNS.addServiceListener("_http._tcp.local.", this);
        }catch(IOException ioe){
            try {
                jmDNS.removeServiceListener("_http._tcp.local.", this);
                jmDNS.close();
            }catch(IOException ioe2){
                // Do nothing
            }
        }
    }

    @Override
    public void serviceAdded(ServiceEvent event) {

    }

    @Override
    public void serviceRemoved(ServiceEvent event) {

    }

    @Override
    public void serviceResolved(ServiceEvent event) {

    }
}
