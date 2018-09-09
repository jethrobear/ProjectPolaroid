package edu.fcpc.polaroid.wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.LinkedHashMap;

import edu.fcpc.polaroid.data.SocketCache;
import edu.fcpc.polaroid.packets.PackageStatus;
import edu.fcpc.polaroid.packets.SentPackage;

/**
 * Created by PAULM915 on 11/08/2017.
 */

public class WiFiRegisterHelper extends WiFiHelper {

    public WiFiRegisterHelper(Activity main) {
        super(main);
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Sending image to Digital Frame");
        dialog.show();
        workingServerSet = SocketCache.workingAddresses.values();
    }

    @Override
    public Integer doInBackgroundInner(ObjectOutputStream objOutStream, String... params) throws IOException {

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

        return 0;
    }

    @Override
    public void onPostExecuteAfter(LinkedHashMap<ImmutablePair<InetAddress, Integer>, SentPackage> results) {
        String regFirstName = "", regLastName = "";
        StringBuilder retMessage = new StringBuilder();
        boolean hasSuccessfulRegistration = false;
        for (SentPackage sentPackage : results.values()) {
            if (sentPackage.packageStatus == PackageStatus.REGISTER_RESPONSE_OK) {
                regFirstName = sentPackage.firstname;
                regLastName = sentPackage.lastname;
                hasSuccessfulRegistration = true;
                break;
            } else {
                retMessage.append(sentPackage.retMessage + "\n");
            }
        }

        if (hasSuccessfulRegistration) {
            new AlertDialog.Builder(main)
                    .setTitle("Register")
                    .setCancelable(false)
                    .setMessage(String.format("%s %s has been registered", regFirstName, regLastName))
                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            main.getFragmentManager().popBackStackImmediate();
                        }
                    }).create().show();
        } else {
            new AlertDialog.Builder(main)
                    .setTitle("Register")
                    .setCancelable(false)
                    .setMessage(retMessage)
                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // INFO: Do nothing
                        }
                    }).create().show();
        }
    }
}
