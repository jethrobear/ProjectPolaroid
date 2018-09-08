package edu.fcpc.polaroid.wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.google.common.io.Files;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.HashMap;

import edu.fcpc.polaroid.Fragment30;
import edu.fcpc.polaroid.packets.PackageStatus;
import edu.fcpc.polaroid.packets.SentPackage;

public class WiFiSendingHelper extends WiFiHelper {

    public WiFiSendingHelper(Activity main) {
        super(main);
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Sending image to Digital Frame");
        dialog.show();
    }

    @Override
    public Integer doInBackgroundInner(ObjectOutputStream objOutStream, String... params) throws IOException {

        File file = new File(Fragment30.imgFile.getAbsolutePath());
        SentPackage sentPackage = new SentPackage();
        sentPackage.packageStatus = PackageStatus.PICTURE;
        sentPackage.imagebinary = Files.toByteArray(file);
        sentPackage.filename = file.getName();
        objOutStream.writeObject(sentPackage);
        objOutStream.flush();

        return 0;
    }

    @Override
    public void onPostExecuteAfter(HashMap<ImmutablePair<InetAddress, Integer>, SentPackage> results) {
        for (SentPackage sentPackage : results.values()) {
            if (sentPackage.packageStatus == PackageStatus.PICTURE_RESPONSE_OK)
                new AlertDialog.Builder(main)
                        .setTitle("Picture")
                        .setCancelable(false)
                        .setMessage("Picture successfully sent to the server")
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            else if (sentPackage.packageStatus == PackageStatus.PICTURE_RESPONSE_FAIL)
                new AlertDialog.Builder(main)
                        .setTitle("Picture")
                        .setCancelable(false)
                        .setMessage(sentPackage.retMessage)
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
        }
    }
}


