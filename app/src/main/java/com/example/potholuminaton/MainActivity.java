package com.example.potholuminaton;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    static final int ILLUMINATION = 1;
    static final int POTHOLES = 0;
    static final int REQUEST_TAKE_PHOTO = 1;
    public ImageView imageView;
    public String currentPhotoPath;
    public String imageFileName;

    public LocationManager locationManager;
    public Location location;
    public double longitude;
    public double latitude;

    private FusedLocationProviderClient client;

    TextView textViewLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            requestPermission();
        }

        try {
            saveTextAsFile("potholes", "image,latitude,longitude\n");
            saveTextAsFile("illumination", "image,latitude,longitude\n");

        } catch (IOException e) {
            e.printStackTrace();
        }

        imageView = findViewById(R.id.imageView);
        textViewLocation = findViewById(R.id.textViewLocation);

        client = LocationServices.getFusedLocationProviderClient(this);
    }



    private void getLocation(){
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            // TODO: Consider calling
            Toast.makeText(MainActivity.this, "nahi mila", Toast.LENGTH_SHORT).show();
            return;
        }
        client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    textViewLocation.setText(latitude + ", " + longitude);
                }
            }
        });
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[] {ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE},
                1);
    }

    private void saveTextAsFile(String filename, String content) throws IOException {
        String fileName = filename + ".txt";

        // create file
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName);

        // write to file
        FileOutputStream fos = new FileOutputStream(file, true);
        fos.write(content.getBytes());
        fos.close();
    }

    public void potholeImageCapture(View view) {
        dispatchTakePictureIntent(POTHOLES);
        galleryAddPic();
        getLocation();

        String data = imageFileName + "," + latitude + "," + longitude + "\n";
        try {
            saveTextAsFile("potholes", data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO: Change google.com to the website you want it to redirect to
    public void openWeb1(View view){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
        startActivity(intent);
    }

    public void openWeb2(View view){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
        startActivity(intent);
    }

    public void openWeb3(View view){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
        startActivity(intent);
    }


    public void illuminationImageCapture(View view){
        dispatchTakePictureIntent(ILLUMINATION);
        galleryAddPic();
        getLocation();

        String data = imageFileName + "," + latitude + "," + longitude + "\n";
        try {
            saveTextAsFile("illumination", data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dispatchTakePictureIntent(int type /*POTHOLE = 0, ILLUMINATION = 1*/) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(type);
                System.out.println("created image file");
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "error hogaya", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

            }
        }
    }

    private File createImageFile(int type /*POTHOLE = 0, ILLUMINATION = 1*/) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        switch(type){
            case 0: imageFileName = "Pothole"; break;
            case 1: imageFileName = "Illumination"; break;
            default: imageFileName = "ignore";
        }

        imageFileName += "_JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }




}
