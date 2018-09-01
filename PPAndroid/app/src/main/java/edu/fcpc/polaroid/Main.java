package edu.fcpc.polaroid;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;

import edu.fcpc.polaroid.wifi.SocketCache;

public class Main extends Activity implements NsdManager.DiscoveryListener {
    private NsdManager mNsdManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Before running check for the app's permission sets
        // Check if we have the permission to start WRITE_EXTERNAL intent
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> permissionSets = new ArrayList<String>();
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissionSets.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                permissionSets.add(Manifest.permission.CAMERA);
            if (permissionSets.size() > 0)
                requestPermissions(permissionSets.toArray(new String[]{}), PermissionResults.ROOT_PERMISSION_SET.ordinal());
        }

        // Setup mDNS
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        try {
            mNsdManager.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, this);
        } catch (NullPointerException npe) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.app_name))
                    .setCancelable(false)
                    .setMessage("No server found")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Main.this.finish();
                        }
                    });
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
    public void onDiscoveryStarted(String regType) {
        Log.v("ZZ", "onDiscoveryStarted Service discovery started");
    }

    @Override
    public void onServiceFound(NsdServiceInfo service) {
        if (!service.getServiceType().equals("_http._tcp.") || !service.getServiceName().equals("example")) {
            Log.v("ZZ", "onServiceFound Unknown Service Type: " + service.getServiceType());
        } else {
            Log.v("ZZ", "onServiceFound Known Service Type: " + service.getServiceType());
            mNsdManager.resolveService(service, new NsdManager.ResolveListener() {
                @Override
                public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                    Log.e("ZZ", "onResolveFailed Resolve failed" + errorCode);
                }

                @Override
                public void onServiceResolved(NsdServiceInfo serviceInfo) {
                    Log.v("ZZ", "onServiceResolved Resolve Succeeded. " + serviceInfo);
                    SocketCache.workingAddresses.put(serviceInfo.getHost(), serviceInfo.getPort());
                    Log.i("ZZ", String.format("%s:%d", serviceInfo.getHost(), serviceInfo.getPort()));
                }
            });
        }
    }

    @Override
    public void onServiceLost(NsdServiceInfo service) {
        SocketCache.workingAddresses.remove(service.getHost());
        Log.e("ZZ", "service lost" + service);
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
        Log.i("ZZ", "Discovery stopped: " + serviceType);
        // TODO: Close gracefully, notify issues found on NSD
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionResults.ROOT_PERMISSION_SET.ordinal()) {
            for (int i = 0, j = 0; i < permissions.length && j < grantResults.length; i++, j++) {
                if (permissions[i].equals(Manifest.permission.CAMERA) && grantResults[j] != PackageManager.PERMISSION_GRANTED)
                    new AlertDialog.Builder(this)
                            .setTitle("Permissions")
                            .setCancelable(false)
                            .setMessage("Allow the app to use the camera to proceed")
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).create().show();

                if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[j] != PackageManager.PERMISSION_GRANTED)
                    new AlertDialog.Builder(this)
                            .setTitle("Permissions")
                            .setCancelable(false)
                            .setMessage("Allow the app to use the device storage to proceed")
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).create().show();

            }
        }
    }
}
