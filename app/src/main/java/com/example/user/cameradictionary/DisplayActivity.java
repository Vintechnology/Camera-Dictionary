package com.example.user.cameradictionary;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

public class DisplayActivity extends AppCompatActivity {
    public static final String WORD_KEY="translate_key";
    public static final String FILE_PATH_EXTRA="DisplayActivity_filePath";
    private ImageView displayImageView;
    private TextView resultLabel;
    private EditText resultText;
    private CropView cropView;
    private View backButton, translateButton, checkButton;
    private TextRecognizer textRecognizer;
    private String imageFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        //initialize
        textRecognizer=new TextRecognizer.Builder(this).build();

        //assign views
        resultText=(EditText)findViewById(R.id.result);
        resultLabel=(TextView)findViewById(R.id.result_label);
        displayImageView=(ImageView)findViewById(R.id.image_display);
        cropView=(CropView)findViewById(R.id.crop_view);
        backButton=findViewById(R.id.back_button);
        translateButton=findViewById(R.id.translate_button);
        checkButton=findViewById(R.id.check_button);
        final FrameLayout mainContentLayout=(FrameLayout)findViewById(R.id.content);

        //get file path
        imageFilePath=getIntent().getStringExtra(FILE_PATH_EXTRA);
        //resample and show chosen image
        Bitmap imageBitmap=BitmapUtils.resampleImage(this,imageFilePath);

        Log.d(MainActivity.APPLICATION_TAG,"After Bitmap width:"+imageBitmap.getWidth());
        Log.d(MainActivity.APPLICATION_TAG,"After Bitmap height:"+imageBitmap.getHeight());
        displayImageView.setImageBitmap(imageBitmap);
        //set maximum crop zone for CropView
        ViewTreeObserver layoutObserver=mainContentLayout.getViewTreeObserver();
        layoutObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mainContentLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Rect imageOnScreenRect=BitmapUtils.getDrawableOnScreenRect(displayImageView.getImageMatrix()
                        ,displayImageView.getDrawable());
                cropView.setMaxCropZone(imageOnScreenRect);
            }
        });
        //set buttons listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitActivity();
            }
        });
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkResult();
            }
        });
        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translate();
            }
        });
    }


    // Buttons method
    private void exitActivity(){
        BitmapUtils.deleteTempFile(this,imageFilePath);
        finish();
    }
    private void translate(){
        CharSequence result=resultText.getText();
        if(countWhiteSpace(result)==result.length()){
            Toast.makeText(this, R.string.detect_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        if(!networkAvailable()){
            Snackbar.make(displayImageView,R.string.no_connection_available,Snackbar.LENGTH_LONG).show();
            return;
        }
        Bundle args=new Bundle();
        args.putString(WORD_KEY,result.toString());
        TranslateWebFragment webFragment=new TranslateWebFragment();
        webFragment.setArguments(args);
        FragmentManager fm=getSupportFragmentManager();
        fm.beginTransaction().add(R.id.container,webFragment).commit();
    }
    private void checkResult(){
        Bitmap srcBitmap=((BitmapDrawable)displayImageView.getDrawable()).getBitmap();
        Bitmap cropBitmap=BitmapUtils.getCropImageBitmap(srcBitmap,cropView.getMaxCropZone(),cropView.getCropRect());
        CharSequence result=detectText(cropBitmap);
        accessCheckMode(result);
    }

    //supply method
    private CharSequence detectText(Bitmap cropBitmap){
        Frame frame=new Frame.Builder().setBitmap(cropBitmap).build();
        SparseArray<TextBlock> detectResultArray=textRecognizer.detect(frame);
        StringBuilder builder=new StringBuilder();
        for(int i=0;i<detectResultArray.size();++i){
            builder.append(detectResultArray.valueAt(i).getValue());
            builder.append(" ");
        }
        return builder;
    }

    private void accessCheckMode(CharSequence result){
        resultText.setVisibility(View.VISIBLE);
        translateButton.setVisibility(View.VISIBLE);

        resultLabel.setText(R.string.check_result_label);
        Log.d("TAG","result: "+result);
        resultText.setText(result);
    }

    private int countWhiteSpace(CharSequence org){
        int whiteSpace=0;
        for(int i=0; i<org.length();++i){
            if(org.charAt(i)==' ')
                whiteSpace++;
        }
        return whiteSpace;
    }

    public boolean networkAvailable(){
        ConnectivityManager connectManager=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkState=connectManager.getActiveNetworkInfo();
        return networkState!=null && networkState.isConnected();
    }
}
