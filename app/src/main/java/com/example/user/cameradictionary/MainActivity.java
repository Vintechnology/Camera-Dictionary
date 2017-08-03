package com.example.user.cameradictionary;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE=1
                                ,REQUEST_WRITE_PERMISSION=2
                                ,REQUEST_PICK_IMAGE=3;
    private static final String FILE_SHARING_AUTHORITY="com.example.user.fileprovider";
    public static  final String APPLICATION_TAG="CAMERA_DICTIONARY";
    public static final String WORD_KEY="translate_key";
    private boolean remainCachedPhoto;
    private ImageView displayImageView;
    private CropView cropView;
    private View takePictureButton,loadPreviousImage, welcomeView, backButton, translateButton, pickImageButton;
    private String mTempImagePath;
    private TextRecognizer textDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textDetector=new TextRecognizer.Builder(this).build();

        displayImageView=(ImageView)findViewById(R.id.image_view);
        cropView=(CropView)findViewById(R.id.crop_view);
        takePictureButton=findViewById(R.id.take_picture);
        backButton=findViewById(R.id.back_button);
        welcomeView=findViewById(R.id.welcome_text);
        loadPreviousImage=findViewById(R.id.get_previous_image);
        translateButton=findViewById(R.id.translate_button);

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(remainCachedPhoto){
                    BitmapUtils.deleteTempFile(MainActivity.this,mTempImagePath);
                    remainCachedPhoto=false;
                }
                startTakingPicture();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToHome();
            }
        });
        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageAndDisplayText();
            }
        });
        checkForRemainCachedImage();
    }

    private void checkForRemainCachedImage(){
        File cacheDir=getExternalCacheDir();
        if(cacheDir!=null){
            File[] tempCachedFiles=cacheDir.listFiles();
            if(tempCachedFiles.length>0) {
                remainCachedPhoto=true;
                loadPreviousImage.setVisibility(View.VISIBLE);
                mTempImagePath = tempCachedFiles[0].getAbsolutePath();
                loadPreviousImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        processAndDisplayImage();
                    }
                });
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
                mTempImagePath=photoFile.getAbsolutePath();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_IMAGE_CAPTURE){
            if(resultCode==RESULT_OK){
                processAndDisplayImage();
            }else{
                BitmapUtils.deleteTempFile(this,mTempImagePath);// !!!
            }
        }else if (requestCode== REQUEST_PICK_IMAGE && resultCode==RESULT_OK){
            Uri selectedImage=data.getData();
            String[] filePathColumn={MediaStore.Images.Media.DATA};
            Cursor c=getContentResolver().query(selectedImage,filePathColumn,null,null,null);
            if(c!=null) {
                int columnIndex = c.getColumnIndex(filePathColumn[0]);
                String selectedFilePath = c.getString(columnIndex);
                c.close();
            }
        }
        else{
            super.onActivityResult(requestCode,resultCode,data);
        }
    }

    private void processAndDisplayImage(){
        takePictureButton.setVisibility(View.GONE);
        welcomeView.setVisibility(View.GONE);
        loadPreviousImage.setVisibility(View.GONE);

        translateButton.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
        cropView.setVisibility(View.VISIBLE);
        Bitmap returnImage=BitmapUtils.resampleImage(this,mTempImagePath);
        displayImageView.setImageBitmap(returnImage);
        Rect maxZone=BitmapUtils.getDrawableOnScreenRect(displayImageView.getImageMatrix()
                                                        ,displayImageView.getDrawable());
        cropView.setMaxCropZone(maxZone);
    }

    private void backToHome(){
        displayImageView.setImageResource(android.R.color.transparent);

        backButton.setVisibility(View.GONE);
        cropView.setVisibility(View.GONE);
        translateButton.setVisibility(View.GONE);

        welcomeView.setVisibility(View.VISIBLE);
        takePictureButton.setVisibility(View.VISIBLE);
        BitmapUtils.deleteTempFile(this,mTempImagePath);
    }

    private void cropImageAndDisplayText(){
        if(!networkAvailable()){
            Snackbar.make(displayImageView,R.string.no_connection_available,Snackbar.LENGTH_LONG).show();
            return;
        }
        Bitmap srcBitmap=((BitmapDrawable)displayImageView.getDrawable()).getBitmap();
        Bitmap croppedBitmap=BitmapUtils.getCropImageBitmap(srcBitmap,cropView.getMaxCropZone(),cropView.getCropRect());

        CharSequence detectedText=detectText(croppedBitmap);
        if(detectedText.length()>0) {
            FragmentManager fragManager=getSupportFragmentManager();
            TranslateWebFragment webFragment=new TranslateWebFragment();
            Bundle bundle = new Bundle();
            bundle.putString(WORD_KEY, detectedText.toString());
            webFragment.setArguments(bundle);
            fragManager.beginTransaction().add(R.id.top_container,webFragment).commit();
        }else{
            Toast.makeText(this, R.string.detect_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private CharSequence detectText (Bitmap imageBitmap){
        Frame frame=new Frame.Builder().setBitmap(imageBitmap).build();
        SparseArray<TextBlock> textSparseArray=textDetector.detect(frame);
        StringBuilder builder=new StringBuilder();
        for(int i=0;i<textSparseArray.size();++i){
            TextBlock text=textSparseArray.valueAt(i);
            builder.append(text.getValue());
            builder.append(" ");
        }
        return builder;
    }

    public boolean networkAvailable(){
        ConnectivityManager connectManager=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkState=connectManager.getActiveNetworkInfo();
        return networkState!=null && networkState.isConnected();
    }


}
