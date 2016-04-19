package com.shzlabs.app.keeptrack;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by personal on 25-12-2015.
 */
public class FontView extends TextView {

    private static final String TAG = FontView.class.getSimpleName();
    private static Typeface font;

    public FontView(Context context){
        super(context);
        setFont(context);
    }

    public FontView(Context context, AttributeSet attrs){
        super(context, attrs);
        setFont(context);
    }

    public FontView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        setFont(context);
    }

    private void setFont(Context context) {

        if(this.isInEditMode()){
            return;
        }

        if(font == null){
            try {
                font = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");
                Log.d(TAG, "Font Awesome loaded!");
            }catch (RuntimeException e){
                Log.e(TAG, "Font Awesome not loaded!");
            }
        }

        setTypeface(font);
    }
}
