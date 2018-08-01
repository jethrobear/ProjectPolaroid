package edu.fcpc.polaroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.fcpc.polaroid.SentPackage;

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
        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        outputStream.flush();
        outputStream.close();
        SentPackage sentPackage = new SentPackage();
        sentPackage.packageStatus = PackageStatus.PICTURE;
        sentPackage.imagebinary = outputStream.toByteArray();
        sentPackage.filename = "A"; // TODO: Determine if this is needed

        objOutStream.writeObject(sentPackage);
        objOutStream.flush();
        objOutStream.close();

        return 0;
    }

    @Override
    protected void onPostExecute(Integer result)
    {
        dialog.dismiss();
        if(result == -1)
            Toast.makeText(main, "Bluetooth send failed", Toast.LENGTH_SHORT).show();
    }
}


