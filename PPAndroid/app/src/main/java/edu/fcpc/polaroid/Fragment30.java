package edu.fcpc.polaroid;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.fcpc.polaroid.wifi.WiFiSendingHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment30 extends Fragment {
    private Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

    public Fragment30() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_30, container, false);

        Button btnCamera = (Button) rootView.findViewById(R.id.btnCamera);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Check if we have the permission to start CAMERA intent
                if (Build.VERSION.SDK_INT >= 23)
                    if (getContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, PermissionResults.CAMERA_INTENT_AFTER_LOGIN.ordinal());
                    } else {
                        // Permission has already been granted
                        startActivityForResult(cameraIntent, 0);
                    }
            }
        });

        return rootView;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionResults.CAMERA_INTENT_AFTER_LOGIN.ordinal()) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                new AlertDialog.Builder(getActivity())
                        .setTitle("Permissions")
                        .setCancelable(false)
                        .setMessage("Allow the app to use the camera to proceed")
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();
            else {
                startActivityForResult(cameraIntent, 0);
            }
        } else if (requestCode == PermissionResults.WRITE_EXT_STORAGE_ON_DEMAND.ordinal()) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                new AlertDialog.Builder(getActivity())
                        .setTitle("Permissions")
                        .setCancelable(false)
                        .setMessage("Allow the app to use the device storage to proceed")
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();
            else
                new AlertDialog.Builder(Fragment30.this.getActivity())
                        .setTitle("Create image file")
                        .setCancelable(false)
                        .setMessage("The app just received the permission to save and was not able to save the previous file")
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create().show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle("Alert Box")
                    .setMessage("Do you want to send this captured photo to the digital frame?")
                    .setPositiveButton("Yes", new Fragment30.AcceptSend(this.getActivity(), data))
                    .setNeutralButton("No", new Fragment30.AcceptSave(data))
                    .setNegativeButton("Later", new Fragment30.DeclineOption(this.getActivity())).show();
        }
    }

    class AcceptSend implements DialogInterface.OnClickListener {
        private Activity main;
        private Intent data;

        public AcceptSend(Activity main, Intent data) {
            this.main = main;
            this.data = data;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (data != null)
                new WiFiSendingHelper(main, data).execute();
        }
    }

    class AcceptSave implements DialogInterface.OnClickListener {
        private Intent data;

        public AcceptSave(Intent data) {
            this.data = data;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (data != null) {
                // Check if we have the permission to start WRITE_EXTERNAL intent
                if (Build.VERSION.SDK_INT >= 23)
                    if (getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionResults.WRITE_EXT_STORAGE_ON_DEMAND.ordinal());
                    }

                // Permission has already been granted
                String destPath = Environment.getExternalStorageDirectory().getPath() + File.separatorChar + "IMG_" +
                        new SimpleDateFormat("ddMMyyyy").format(new Date()) + ".jpg";
                try {
                    File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.jpg");
                    File nfile = new File(destPath);
                    Files.copy(file, nfile);
                    MediaStore.Images.Media.insertImage(Fragment30.this.getActivity().getContentResolver(), nfile.getAbsolutePath(), nfile.getName(), nfile.getName());
                } catch (IOException ioe) {

                }
            }
        }
    }

    class DeclineOption implements DialogInterface.OnClickListener {
        private Activity main;

        public DeclineOption(Activity main) {
            this.main = main;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (main != null) {
                Toast.makeText(main, "Photo discarded", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
