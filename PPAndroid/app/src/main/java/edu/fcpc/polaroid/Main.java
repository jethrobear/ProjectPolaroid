package edu.fcpc.polaroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import java.net.InetAddress;

public class Main extends Activity {
    private NsdManager.ResolveListener mResolveListener;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager mNsdManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup mDNS
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e("ZZ", "onResolveFailed Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.v("ZZ", "onServiceResolved Resolve Succeeded. " + serviceInfo);
                int port = serviceInfo.getPort();
                InetAddress host = serviceInfo.getHost();

                Log.i("ZZ", String.format("%s:%d", host.getHostAddress(), port));
            }
        };
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.v("ZZ", "onDiscoveryStarted Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                if (!service.getServiceType().equals("_http._tcp.")) {
                    Log.v("ZZ", "onServiceFound Unknown Service Type: " + service.getServiceType());
                } else {
                    Log.v("ZZ", "onServiceFound Known Service Type: " + service.getServiceType());
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e("ZZ", "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i("ZZ", "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e("ZZ", "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e("ZZ", "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
        mNsdManager.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);


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
}
