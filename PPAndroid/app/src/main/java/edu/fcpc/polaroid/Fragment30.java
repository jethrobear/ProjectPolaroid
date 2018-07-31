package edu.fcpc.polaroid;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment30 extends Fragment {


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
                Intent a = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(a, 0);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == Activity.RESULT_OK){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this.getContext());
            dialog.setTitle("Alert Box")
                    .setMessage("Do you want to send this captured photo to the digital frame?")
                    .setPositiveButton("Yes", new Fragment30.AcceptSend(this.getActivity(), data))
                    .setNeutralButton("No", new Fragment30.AcceptSave(data))
                    .setNegativeButton("Later", new Fragment30.DeclineOption(this.getActivity())).show();
        }
    }

    class AcceptSend implements DialogInterface.OnClickListener{
        private Activity main;
        private Intent data;

        public AcceptSend(Activity main, Intent data){
            this.main = main;
            this.data = data;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(data != null)
                new WiFiSendingHelper(main, data).execute();
        }
    }

    class AcceptSave implements DialogInterface.OnClickListener{
        private Intent data;

        public AcceptSave(Intent data){
            this.data = data;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(data != null) {
                String destPath = Environment.getExternalStorageDirectory().getPath() + File.separatorChar + "IMG_" +
                        new SimpleDateFormat("ddMMyyyy").format(new Date()) + ".jpg";
                try {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    File file = new File(destPath);
                    FileOutputStream fileOutStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutStream);
                    fileOutStream.flush();
                    fileOutStream.close();
                    MediaStore.Images.Media.insertImage(Fragment30.this.getContext().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
                } catch (IOException ioe) {

                }
            }
        }
    }

    class DeclineOption implements DialogInterface.OnClickListener{
        private Activity main;
        public DeclineOption(Activity main){
            this.main = main;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(main != null) {
                Toast.makeText(main, "Photo discarded", Toast.LENGTH_SHORT).show();
            }
        }
    }
}