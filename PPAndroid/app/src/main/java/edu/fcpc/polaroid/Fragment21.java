package edu.fcpc.polaroid;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.io.DataOutputStream;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment21 extends Fragment {
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothServerSocket btServerSocket;
    private BluetoothSocket btSocket;
    private DataOutputStream dataOutputStream;
    private ProgressDialog dialog;
    private String errMessage;

    public Fragment21() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Activity main = Fragment21.this.getActivity();
        View rootView = inflater.inflate(R.layout.fragment_21, container, false);

        dialog = new ProgressDialog(main);
        dialog.setCancelable(false);

        final EditText txtLastname = (EditText) rootView.findViewById(R.id.txtLastname);
		final EditText txtFirstname = (EditText) rootView.findViewById(R.id.txtFirstname);
		final EditText txtRegisterMonth = (EditText) rootView.findViewById(R.id.txtRegisterMonth);
		final EditText txtRegisterDay = (EditText) rootView.findViewById(R.id.txtRegisterDay);
		final EditText txtRegisterYear = (EditText) rootView.findViewById(R.id.txtRegisterYear);
		final EditText txtUsername2 = (EditText) rootView.findViewById(R.id.txtUsername2);
		final EditText txtPassword2 = (EditText) rootView.findViewById(R.id.txtPassword2);
		Button btnRegister = (Button) rootView.findViewById(R.id.btnRegister);
		
		btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(txtLastname.getText().toString().equals("") ||
                   txtFirstname.getText().toString().equals("") ||
                   txtRegisterMonth.getText().toString().equals("") ||
                   txtRegisterDay.getText().toString().equals("") ||
                   txtRegisterYear.getText().toString().equals("") ||
                   txtUsername2.getText().toString().equals("") ||
                   txtPassword2.getText().toString().equals(""))
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Alert Box")
                            .setMessage("Please input every required information on the register form")
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                else {
                    WiFiRegisterHelper btRegisterSendHlpr = new WiFiRegisterHelper(Fragment21.this.getActivity());
                    btRegisterSendHlpr.execute(txtLastname.getText().toString(),
                                               txtFirstname.getText().toString(),
                                               txtRegisterMonth.getText().toString(),
                                               txtRegisterDay.getText().toString(),
                                               txtRegisterYear.getText().toString(),
                                               txtUsername2.getText().toString(),
                                               txtPassword2.getText().toString());
                }
            }
        });

        return rootView;
    }
}