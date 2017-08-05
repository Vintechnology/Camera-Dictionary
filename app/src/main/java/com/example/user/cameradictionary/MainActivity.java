package com.example.user.cameradictionary;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE=1
                                ,REQUEST_WRITE_PERMISSION=2
                                ,REQUEST_PICK_IMAGE=3;
    private static final String FILE_SHARING_AUTHORITY="com.example.user.fileprovider";
    public static  final String APPLICATION_TAG="CAMERA_DICTIONARY";
    private boolean remainCachedPhoto;
    private View takePictureButton,loadPreviousImage, pickImageButton;
    private String mImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        takePictureButton=findViewById(R.id.take_picture);
        loadPreviousImage=findViewById(R.id.get_previous_image);
        pickImageButton=findViewById(R.id.pick_image_button);

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(remainCachedPhoto){
                    BitmapUtils.deleteTempFile(MainActivity.this, mImagePath);
                    remainCachedPhoto=false;
                }
                startTakingPicture();
            }
        });
        pickImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchPickImageIntent();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkForRemainCachedImage();
    }

    private void checkForRemainCachedImage(){
        File cacheDir=getExternalCacheDir();
        if(cacheDir!=null){
            File[] tempCachedFiles=cacheDir.listFiles();
            if(tempCachedFiles.length>0) {
                remainCachedPhoto=true;
                loadPreviousImage.setVisibility(View.VISIBLE);
                mImagePath = tempCachedFiles[0].getAbsolutePath();
                loadPreviousImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        processAndDisplayImage();
                    }
                });
                Log.d(APPLICATION_TAG,"file name: "+ mImagePath);
                Log.d(APPLICATION_TAG,"parent: "+getExternalCacheDir().getAbsolutePath());
            }else {
                loadPreviousImage.setVisibility(View.GONE);
                remainCachedPhoto=false;
            }
        }
    }


    public void startTakingPicture(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_WRITE_PERMISSION);
        }else{
            dispatchCameraCaptureIntent();
        }
    }

    private void dispatchCameraCaptureIntent(){
        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraIntent.resolveActivity(getPackageManager())!=null){
            File photoFile=null;
            try{
                photoFile=BitmapUtils.createTempFile(this);
            }catch(IOException ex){
                ex.printStackTrace();
            }
            if(photoFile!=null) {
                mImagePath =photoFile.getAbsolutePath();
                Uri photoUri= FileProvider.getUriForFile(this,FILE_SHARING_AUTHORITY,photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void dispatchPickImageIntent(){
        Intent getPicIntent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(getPicIntent,REQUEST_PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_WRITE_PERMISSION){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                dispatchCameraCaptureIntent();
            else
                Toast.makeText(this,R.string.permission_denied,Toast.LENGTH_SHORT).show();
        }else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {//
        if(requestCode==REQUEST_IMAGE_CAPTURE){
            if(resultCode==RESULT_OK){
                processAndDisplayImage();
            }else{
                BitmapUtils.deleteTempFile(this, mImagePath);
            }
        }else if (requestCode== REQUEST_PICK_IMAGE && resultCode==RESULT_OK){
            Uri selectedImage=data.getData();
            String[] filePathColumn={MediaStore.Images.Media.DATA};
            Cursor c=getContentResolver().query(selectedImage,filePathColumn,null,null,null);
            if(c!=null && c.moveToFirst()) {
                int columnIndex = c.getColumnIndex(filePathColumn[0]);
                mImagePath = c.getString(columnIndex);
                c.close();
                processAndDisplayImage();
            }
        }else{
            super.onActivityResult(requestCode,resultCode,data);
        }
    }

    private void processAndDisplayImage(){
        if(mImagePath ==null) {
            Toast.makeText(this, R.string.directory_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent displayIntent=new Intent(this,DisplayActivity.class);
        displayIntent.putExtra(DisplayActivity.FILE_PATH_EXTRA, mImagePath);
        startActivity(displayIntent);
    }


}
