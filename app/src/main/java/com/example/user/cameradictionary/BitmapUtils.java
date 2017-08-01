package com.example.user.cameradictionary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by user on 7/16/2017.
 */

 class BitmapUtils {
    static File createTempFile(Context context) throws IOException{
        String timeStamp= new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName="JPEG_"+timeStamp+"_";
        File cacheDir=context.getExternalCacheDir();
        return File.createTempFile(imageFileName,".jpg",cacheDir);
    }

    static boolean deleteTempFile(Context context, String filePath){
        File fileToDelete= new File(filePath);
        boolean deleteSuccess=fileToDelete.delete();
        if(!deleteSuccess){
            Toast.makeText(context,R.string.failed_to_delete,Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, R.string.success_delete, Toast.LENGTH_SHORT).show();
        }
        return deleteSuccess;
    }

    static Bitmap resampleImage(Context context,String imagePath){
        DisplayMetrics displayMetrics=context.getResources().getDisplayMetrics();
        int targetWidth=displayMetrics.widthPixels;
        int targetHeight=displayMetrics.heightPixels;
        Log.d(MainActivity.APPLICATION_TAG,"screenWidth:"+targetWidth);
        Log.d(MainActivity.APPLICATION_TAG,"screenHeight"+targetHeight);

        BitmapFactory.Options displayOptions=new BitmapFactory.Options();
        displayOptions.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(imagePath,displayOptions);
        int orgWidth=displayOptions.outWidth;
        int orgHeight=displayOptions.outHeight;
        Log.d(MainActivity.APPLICATION_TAG,"Bitmap width:"+orgWidth);
        Log.d(MainActivity.APPLICATION_TAG,"Bitmap height:"+orgHeight);

        int scaleFactor=Math.min(orgWidth/targetWidth,orgHeight/targetHeight);
        displayOptions.inJustDecodeBounds=false;
        displayOptions.inSampleSize=scaleFactor;
        displayOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(imagePath,displayOptions);
    }

    public static Rect getDrawableOnScreenRect(Matrix imageMatrix, Drawable drawable){
        float[] values =new float[9];
        int bitmapWidth=drawable.getIntrinsicWidth();
        int bitmapHeight=drawable.getIntrinsicHeight();
        imageMatrix.getValues(values);
        int transX=(int)(values[Matrix.MTRANS_X]+0.5f);
        int transY=(int)(values[Matrix.MTRANS_Y]+0.5f);
        float scale=values[Matrix.MSCALE_X];
        int bitmapScaledWidth=(int)(scale*bitmapWidth+0.5f);
        int bitmapScaledHeight=(int)(scale*bitmapHeight+0.5f);
        Rect returnRect =new Rect();
        returnRect.set(transX,transY,transX+bitmapScaledWidth,transY+bitmapScaledHeight);

        return returnRect;
    }

    public static Bitmap getCropImageBitmap(Bitmap src, Rect onScreenBitmapBounds,Rect onScreenCropBounds){

        int onScreenBitmapWidth=onScreenBitmapBounds.width();
        int onScreenBitmapHeight=onScreenBitmapBounds.height();

        int onScreenCropWidth=onScreenCropBounds.width();
        int onScreenCropHeight=onScreenCropBounds.height();

        int offsetX=onScreenCropBounds.left-onScreenBitmapBounds.left;
        int offsetY=onScreenCropBounds.top-onScreenBitmapBounds.top;

        float scaleX=(float)src.getWidth()/(float)onScreenBitmapWidth;
        float scaleY=(float)src.getHeight()/(float)onScreenBitmapHeight;

        return Bitmap.createBitmap(src,(int)(offsetX*scaleX+0.5f),(int)(offsetY*scaleY+0.5f)
                                    ,(int)(onScreenCropWidth*scaleX+0.5f),(int)(onScreenCropHeight*scaleY+0.5f));
    }
}
