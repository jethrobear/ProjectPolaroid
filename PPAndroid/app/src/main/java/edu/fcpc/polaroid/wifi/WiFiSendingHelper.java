package edu.fcpc.polaroid.wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;

import edu.fcpc.polaroid.packets.PackageStatus;
import edu.fcpc.polaroid.packets.SentPackage;

public class WiFiSendingHelper extends WiFiHelper {
    private Intent data;

    public WiFiSendingHelper(Activity main, Intent data){
        super(main);
        this.data = data;
    }

    @Override
    protected void onPreExecute()
    {
        dialog.setMessage("Sending image to Digital Frame");
        dialog.show();
    }

    @Override
    public Integer doInBackgroundInner(ObjectOutputStream objOutStream, String... params) throws IOException {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.jpg");
        SentPackage sentPackage = new SentPackage();
        sentPackage.packageStatus = PackageStatus.PICTURE;
        sentPackage.imagebinary = Files.toByteArray(file);
        sentPackage.filename = "A"; // TODO: Determine if this is needed

        objOutStream.writeObject(sentPackage);
        objOutStream.flush();

        return 0;
    }

    @Override
    public void onPostExecuteAfter(SentPackage sentPackage) {
        if(sentPackage.packageStatus == PackageStatus.PICTURE_RESPONSE_OK)
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
        else if(sentPackage.packageStatus == PackageStatus.PICTURE_RESPONSE_FAIL)
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


