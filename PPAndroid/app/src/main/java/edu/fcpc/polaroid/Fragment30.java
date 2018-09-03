package edu.fcpc.polaroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.fcpc.polaroid.wifi.WiFiSendingHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment30 extends Fragment {
    private final int CAMERA_INTENT_REQUEST_CODE = 0;
    public static File imgFile;

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
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_INTENT_REQUEST_CODE);
            }
        });

        return rootView;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        try {
            imgFile = File.createTempFile("ppandroid", ".png", getActivity().getCacheDir());

            // Get the file's URI
            Uri photoUri = Uri.fromFile(imgFile);
            if (Build.VERSION.SDK_INT >= 24)
                photoUri = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".provider", imgFile);

            // Add intent data
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

            // Start the intent
            if (intent.resolveActivity(getActivity().getPackageManager()) != null)
                super.startActivityForResult(intent, requestCode);
        } catch (IOException ioe) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Image file")
                    .setCancelable(false)
                    .setMessage(ioe.getMessage())
                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_INTENT_REQUEST_CODE) {
            // Attempt to rotate the image
            AsyncTask<String, Void, Void> imageTransform = new AsyncTask<String, Void, Void>() {
                private ProgressDialog dialog;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    dialog = new ProgressDialog(Fragment30.this.getActivity());
                    dialog.setTitle("Transforming image");
                    dialog.setCancelable(false);
                    dialog.setMessage("Please wait while the image is being rotated");
                    dialog.show();
                }

                @Override
                protected Void doInBackground(String... strings) {
                    try {
                        ExifInterface exifInterface = new ExifInterface(strings[0]);
                        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                        int rotation = 0;
                        switch (orientation) {
                            case ExifInterface.ORIENTATION_ROTATE_90:
                                rotation = 90;
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_180:
                                rotation = 180;
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_270:
                                rotation = 270;
                                break;
                            default:
                        }
                        Matrix matrix = new Matrix();
                        matrix.postRotate(rotation);
                        Bitmap normalImage = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        Bitmap rotatedImage = Bitmap.createBitmap(normalImage, 0, 0, normalImage.getWidth(), normalImage.getHeight(), matrix, true);

                        // Replace the image
                        if (imgFile.exists())
                            imgFile.delete();
                        imgFile.createNewFile();
                        FileOutputStream fileOutStream = new FileOutputStream(imgFile);
                        rotatedImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutStream);
                        fileOutStream.flush();
                        fileOutStream.close();
                    } catch (IOException ioe) {
                        Log.e("ProjectPolaroid", ioe.getMessage());
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    dialog.dismiss();

                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle("Alert Box")
                            .setMessage("Do you want to send this captured photo to the digital frame?")
                            .setPositiveButton("Yes", new Fragment30.AcceptSend(Fragment30.this.getActivity()))
                            .setNeutralButton("No", new Fragment30.AcceptSave())
                            .setNegativeButton("Later", new Fragment30.DeclineOption(Fragment30.this.getActivity())).show();
                }
            };
            imageTransform.execute(imgFile.getAbsolutePath());
        }
    }

    class AcceptSend implements DialogInterface.OnClickListener {
        private Activity main;

        public AcceptSend(Activity main) {
            this.main = main;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            new WiFiSendingHelper(main).execute();
        }
    }

    class AcceptSave implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // Permission has already been granted
            try {
                // Prepare source file
                File file = new File(imgFile.getAbsolutePath());

                // Prepare destination file
                File savePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath(), "PPAndroid");
                if (!savePath.exists())
                    savePath.mkdir();
                File nfile = new File(savePath, new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".png");
                if (nfile.exists())
                    nfile.delete();
                nfile.createNewFile();

                // Copy files
                Files.copy(file, nfile);

                new AlertDialog.Builder(Fragment30.this.getActivity())
                        .setTitle("Save")
                        .setCancelable(false)
                        .setMessage(String.format("Image saved as %s", nfile.getAbsoluteFile()))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            } catch (IOException ioe) {
                Log.e("PPAndroid", ioe.getMessage());
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
