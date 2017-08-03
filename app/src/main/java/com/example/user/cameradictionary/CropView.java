package com.example.user.cameradictionary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by user on 7/17/2017.
 */

public class CropView extends View {
    public static final int PAN_FLAG=1
                ,ZOOM_FLAG=2
                ,NONE_FLAG=3;
    private static final int CLICK_DISTANCE=3;
    public static final int MIN_GAP_IN_DP=15;
    private int modeFlag;
    private int cornerID;
    private int minGapInPixel;
    private Paint rectPaint,cornerSquarePaint;
    private Rect cropRect;
    private Rect maxCropZone;
    private CornerSquare[] mCornerSquareArray;
    private Point startPoint, lastPoint;
    public CropView(Context context) {
        super(context);
        init();
    }

    public CropView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        rectPaint=new Paint();
        rectPaint.setColor(Color.GRAY);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(3f);

        cornerSquarePaint=new Paint();
        cornerSquarePaint.setColor(Color.BLUE);
        cornerSquarePaint.setStyle(Paint.Style.FILL);

        final float scaleFactor=getResources().getDisplayMetrics().density;
        minGapInPixel=(int)(MIN_GAP_IN_DP*scaleFactor+0.5f);
        mCornerSquareArray=CornerSquare.createSquares(scaleFactor,4);
        cropRect=new Rect();
        maxCropZone=new Rect();
        startPoint=new Point();
        lastPoint=new Point();
        modeFlag=NONE_FLAG;
        cornerID=-1;
    }

    public void setMaxCropZone(Rect maxZone){
            maxCropZone.set(maxZone);
    }

    public Rect getCropRect(){
        return cropRect;
    }

    public Rect getMaxCropZone(){
        return maxCropZone;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int canvasWidth=canvas.getWidth();
        int canvasHeight=canvas.getHeight();
        if(cropRect.isEmpty()){
            cropRect.top=4*canvasHeight/10;
            cropRect.bottom=6*canvasHeight/10;
            cropRect.left=3*canvasWidth/10;
            cropRect.right=7*canvasWidth/10;
        }
        canvas.drawRect(cropRect,rectPaint);

        mCornerSquareArray[0].draw(canvas,cropRect.left,cropRect.top,cornerSquarePaint);
        mCornerSquareArray[2].draw(canvas,cropRect.left,cropRect.bottom,cornerSquarePaint);
        mCornerSquareArray[1].draw(canvas,cropRect.right,cropRect.top,cornerSquarePaint);
        mCornerSquareArray[3].draw(canvas,cropRect.right,cropRect.bottom,cornerSquarePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int currentX=(int)(event.getX()+0.5f);
        int currentY=(int)(event.getY()+0.5f);
        switch(event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                startPoint.set(currentX,currentY);
                lastPoint.set(currentX,currentY);
                modeFlag=NONE_FLAG;
                for(int i=0;i<mCornerSquareArray.length;++i){
                    if(mCornerSquareArray[i].contains(currentX,currentY)){
                        cornerID=i;
                        modeFlag=ZOOM_FLAG;
                        break;
                    }
                }
                /* may not need
                if(!maxCropZone.contains(currentX,currentY) && modeFlag==NONE_FLAG)
                    return false;
                    */
                if(modeFlag==NONE_FLAG && cropRect.contains(currentX,currentY)){
                    modeFlag=PAN_FLAG;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX=currentX-lastPoint.x;
                int deltaY=currentY-lastPoint.y;
                if(modeFlag==PAN_FLAG){
                    fixedCropPan(deltaX,deltaY);
                }else if(modeFlag==ZOOM_FLAG){
                    if(cornerID>=2)//corners at the bottom
                        cropRect.bottom=fixedTranslate(deltaY+cropRect.bottom,maxCropZone.bottom,cropRect.top+minGapInPixel);
                    else
                        cropRect.top=fixedTranslate(deltaY+cropRect.top,cropRect.bottom-minGapInPixel,maxCropZone.top);
                    if((cornerID%2)!=0)//corners on the right
                        cropRect.right=fixedTranslate(deltaX+cropRect.right,maxCropZone.right,cropRect.left+minGapInPixel);
                    else
                        cropRect.left=fixedTranslate(deltaX+cropRect.left,cropRect.right-minGapInPixel,maxCropZone.left);
                }else{
                    for(int i=0;i<mCornerSquareArray.length;++i){
                        if(mCornerSquareArray[i].contains(currentX,currentY)){
                            cornerID=i;
                            modeFlag=ZOOM_FLAG;
                            break;
                        }
                    }
                }

                lastPoint.x=currentX;
                lastPoint.y=currentY;
                break;
            case MotionEvent.ACTION_CANCEL:
                modeFlag=NONE_FLAG;
                cornerID=-1;
                break;
            case MotionEvent.ACTION_UP:
                if(modeFlag==NONE_FLAG){
                    int distance=(int)Math.sqrt(Math.pow(startPoint.x-currentX,2)+Math.pow(startPoint.y-currentY,2));
                    if(distance<=CLICK_DISTANCE)
                        performClick();
                }
                modeFlag=NONE_FLAG;
                cornerID=-1;
                break;
        }
        invalidate();
        return true;
    }

    private void fixedCropPan(int deltaX, int deltaY){
        cropRect.offset(deltaX,deltaY);
        float regain;
        if(cropRect.top<maxCropZone.top) {
            regain=cropRect.top-maxCropZone.top;
            cropRect.top = maxCropZone.top;
            cropRect.bottom-=regain;
        }
        else if(cropRect.bottom>maxCropZone.bottom) {
            regain=cropRect.bottom-maxCropZone.bottom;
            cropRect.bottom = maxCropZone.bottom;
            cropRect.top-=regain;
        }
        if(cropRect.left<maxCropZone.left) {
            regain=cropRect.left-maxCropZone.left;
            cropRect.left = maxCropZone.left;
            cropRect.right-=regain;
        }
        else if(cropRect.right>maxCropZone.right) {
            regain=cropRect.right-maxCropZone.right;
            cropRect.right = maxCropZone.right;
            cropRect.left-=regain;
        }
    }

    private int fixedTranslate(int newOne,int max, int min){
        return Math.max(min,Math.min(newOne,max));
    }

    private static class CornerSquare{
        private static final int SQUARE_HALF_WIDTH_IN_DP=8;
        private static int halfWidthInPixel;
        private Point centerPoint;
        public static CornerSquare[] createSquares(float density,int numberOfSquare){
            halfWidthInPixel=(int)(density*SQUARE_HALF_WIDTH_IN_DP+0.5f);
            CornerSquare[] array=new CornerSquare[numberOfSquare];
            for(int i=0;i<numberOfSquare;++i){
                array[i]=new CornerSquare();
            }
            return array;
        }
        private CornerSquare(){
            centerPoint=new Point();
        }
        public void draw(Canvas canvas,int x, int y,Paint squarePaint){
            centerPoint.set(x,y);
            canvas.drawRect(x-halfWidthInPixel,y-halfWidthInPixel,x+halfWidthInPixel,y+halfWidthInPixel,squarePaint);
        }
        public boolean contains (int x,int y){
            return Math.abs(x-centerPoint.x)<=halfWidthInPixel && Math.abs(y-centerPoint.y)<=halfWidthInPixel;
        }
    }
}
