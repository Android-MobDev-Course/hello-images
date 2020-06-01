package com.mobdev.helloimages;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Marco Picone (picone.m@gmail.com) 01/06/2020
 * Simple Activity and application to show how to work with Images, Gallery and Camera
 */
public class MainActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 1;

    private static final int CAMERA_REQUEST_CODE = 2;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_EXTERNAL_STORAGE = 54;

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 55;

    private String cameraFilePath;

    private Button imageGalleryButton = null;

    private Button captureImageButton = null;

    private ImageView imageView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
    }

    private void initUI() {

        imageGalleryButton = (Button)findViewById(R.id.imageGalleryButton);
        captureImageButton = (Button)findViewById(R.id.captureImageButton);
        imageView = (ImageView)findViewById(R.id.imageView);

        imageGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromGallery();
            }
        });

        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureFromCamera();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK)

            switch (requestCode) {
                case GALLERY_REQUEST_CODE:
                    Uri selectedImage = data.getData();
                    //data.getData returns the content URI for the selected Image
                    //loadImageWithUri(selectedImage);
                    loadImageBitmap(selectedImage);
                    break;
                case CAMERA_REQUEST_CODE:
                    imageView.setImageURI(Uri.parse(cameraFilePath));
                    addNewImageToGallery();
                    break;
            }
    }

    /**
     * When you create a photo through an intent, you should know where your image is located,
     * because you said where to save it in the first place. For everyone else,
     * perhaps the easiest way to make your photo accessible
     * is to make it accessible from the system's Media Provider.
     *
     * This method shows how to invoke the system's media scanner
     * to add your photo to the Media Provider's database, making it available
     * in the Android Gallery application and to other apps.
     */
    private void addNewImageToGallery() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(cameraFilePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    /**
     * Once you decide the directory for the file,
     * you need to create a collision-resistant file name.
     * You may wish also to save the path in a member variable for later use.
     *
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        //This is the directory in which the file will be created. This is the default location of Camera photos
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for using again
        cameraFilePath = image.getAbsolutePath();

        return image;
    }

    /**
     * This method start the camera in order to start capturing a new Image
     * Handle External Storage Permission
     */
    private void captureFromCamera() {

        try {

            String myPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

            int permissionCheck = ContextCompat.checkSelfPermission(this, myPermission);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, myPermission)) {
                    Toast.makeText(this,"The Application needs the access to your external storage to properly work ! Check System Setting to grant access !",Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this, new String[]{myPermission}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat.requestPermissions(this,new String[]{myPermission}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            }
            else {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(
                                this,
                                BuildConfig.APPLICATION_ID + ".provider",
                                createImageFile()
                        ));

                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Example method to show how to load an Imaged from an URI
     * @param selectedImageUri
     */
    private void loadImageWithUri(Uri selectedImageUri) {
        imageView.setImageURI(selectedImageUri);
    }

    /**
     * Method to load a Bitmap from an URI
     * @param selectedImageUri
     */
    private void loadImageBitmap(Uri selectedImageUri){

        try{
             imageView.setImageBitmap(BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri)));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Method to load an existing picture from the Gallery
     * Handle External Storage Permission
     */
    private void pickImageFromGallery(){

        String myPermission = Manifest.permission.READ_EXTERNAL_STORAGE;

        int permissionCheck = ContextCompat.checkSelfPermission(this,myPermission);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,myPermission)) {
                Toast.makeText(this,"The Application needs the access to your external storage to properly work ! Check System Setting to grant access !",Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{myPermission}, MY_PERMISSIONS_REQUEST_ACCESS_EXTERNAL_STORAGE);

            } else {
                ActivityCompat.requestPermissions(this,new String[]{myPermission}, MY_PERMISSIONS_REQUEST_ACCESS_EXTERNAL_STORAGE);
            }
        }
        else {

            //Create an Intent with action as ACTION_PICK
            Intent intent=new Intent(Intent.ACTION_PICK);
            // Sets the type as image/*. This ensures only components of type image are selected
            intent.setType("image/*");

            //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
            String[] mimeTypes = {"image/jpeg", "image/png"};

            intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);

            // Launching the Intent
            startActivityForResult(intent,GALLERY_REQUEST_CODE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_EXTERNAL_STORAGE: {
                if (grantResults.length > 0	&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(this,"The Application needs the access to your external storage to properly work ! Check System Setting to grant access !",Toast.LENGTH_LONG).show();
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0	&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    captureFromCamera();
                } else {
                    Toast.makeText(this,"The Application needs the access to your external storage to properly work ! Check System Setting to grant access !",Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
}
