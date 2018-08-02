package edu.fcpc.polaroid;

import android.app.Activity;

import java.io.IOException;
import java.io.ObjectOutputStream;

import edu.fcpc.polaroid.packets.PackageStatus;
import edu.fcpc.polaroid.packets.SentPackage;

/**
 * Created by PAULM915 on 11/08/2017.
 */

public class WiFiRegisterHelper extends WiFiHelper {

    public WiFiRegisterHelper(Activity main){
        super(main);
    }

    @Override
    protected void onPreExecute()
    {
        dialog.setMessage("Sending image to Digital Frame");
        dialog.show();
    }

    @Override
    public Integer doInBackgroundInner(ObjectOutputStream objOutStream, String... params) throws IOException{

        SentPackage sentPackage = new SentPackage();
        sentPackage.packageStatus = PackageStatus.REGISTER;
        sentPackage.lastname = params[0];
        sentPackage.firstname = params[1];
        sentPackage.birthmonth = params[2];
        sentPackage.birthday = params[3];
        sentPackage.birthyear = params[4];
        sentPackage.username = params[5];
        sentPackage.password = params[6];

        objOutStream.writeObject(sentPackage);
        objOutStream.flush();
        objOutStream.close();

        return 0;
    }

    @Override
    public Integer doInBackgroundPostSend(SentPackage sentPackage, String... params) throws IOException {
        if(sentPackage.packageStatus == PackageStatus.REGISTER_RESPONSE_OK){
            // TODO: Affirm that the member had registered
        }else if(sentPackage.packageStatus == PackageStatus.REGISTER_RESPONSE_FAIL){
            // TODO: Affirm that the member had not been registered
        }

        return 0;
    }
}
