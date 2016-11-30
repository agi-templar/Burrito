package edu.asu.msrs.artceleration;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.ParcelFileDescriptor;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class ArtView extends View {
    private static final String TAG = "ArtView";

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;
    private Bitmap imgbmp, transbmp, transbmp_trunc;
    private int offset_x = 200;
    public ArtView(Context context) {
        super(context);
        init();
    }


    public ArtView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArtView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
        init();
    }

    public void init(){
        imgbmp = BitmapFactory.decodeResource(getResources(), R.drawable.asuhayden);
        transbmp = BitmapFactory.decodeResource(getResources(), R.drawable.asuhayden2);
        transbmp_trunc =Bitmap.createBitmap(transbmp, offset_x,0,transbmp.getWidth()-offset_x, transbmp.getHeight());
    }
    @Override
    public boolean onTouchEvent (MotionEvent event){
        offset_x = (int)(event.getX());
        if (offset_x>transbmp.getWidth()-1){
            offset_x = transbmp.getWidth()-1;
        }

        if (offset_x <1){
            offset_x = 1;
        }
        transbmp_trunc = Bitmap.createBitmap(transbmp, offset_x,0,transbmp.getWidth()-offset_x, transbmp.getHeight());
        invalidate();
        return true;
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        canvas.drawBitmap(imgbmp,0,0,null); // 改过
        canvas.drawBitmap(transbmp_trunc,offset_x,0,null);
    }
    public void setImgBmp(Bitmap b){
        imgbmp = b;
    }

    public void setTransBmp(Bitmap b){
        transbmp = b;
        transbmp_trunc =Bitmap.createBitmap(transbmp, offset_x,0,transbmp.getWidth()-offset_x, transbmp.getHeight());
        invalidate();
        requestLayout();
    }
    public Bitmap getImgBmp(){
        return imgbmp;
    }
    public Bitmap getTransBmp(){
        return transbmp;
    }
}
