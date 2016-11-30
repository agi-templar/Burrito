package edu.asu.msrs.artceleration;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.jph.takephoto.model.TImage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import edu.asu.msrs.artcelerationlibrary.ArtLib;
import edu.asu.msrs.artcelerationlibrary.TransformHandler;
import edu.asu.msrs.artcelerationlibrary.TransformTest;
import edu.asu.msrs.artcelerationlibrary.artcelerationService.ArtTransformThreadPool;

import static com.bumptech.glide.request.target.Target.SIZE_ORIGINAL;

public class MainViewer extends AppCompatActivity {
    static {
        System.loadLibrary("native-lib");
    }

    private Spinner spinner;
    private TextView status1;
    private TextView status2;
    private ArtView artview;
    private ArtLib artlib;
    private CaptureRequest cm;
    private CameraDevice cd;

    public static final String KEY_TRANSFORM_OPTION = "TransformType";

    android.hardware.camera2.CaptureRequest cr;
    ArrayList<String> testsArray;
    TransformTest[] tests;
    String[] transforms;
    static Bitmap src_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_viewer);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        src_img = BitmapFactory.decodeResource(getResources(), R.drawable.asuhayden, opts);
        artview = (ArtView) findViewById(R.id.artView);

        artlib = new ArtLib(MainViewer.this);
        artlib.registerHandler(new TransformHandler() {
            @Override
            public void onTransformProcessed(Bitmap img_out) {
                artview.setTransBmp(img_out);
            }
        });
        initSpinner();
        initMenu();


//                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, testsArray);
//                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        spinner.setAdapter(spinnerArrayAdapter);





        spinner = (Spinner) findViewById(R.id.spinner);
        status1 = (TextView) findViewById(R.id.statusText1);
        status2 = (TextView) findViewById(R.id.statusText2);

//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                TransformTest t = tests[position];
//                if (artlib.requestTransform(src_img, t.transformType, t.intArgs, t.floatArgs)) {
//
//                    makeToast("Transform requested : " + transforms[t.transformType]);
//
//
//                } else {
//                    makeToast("Transform request failed" + transforms[t.transformType]);
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

    }

    private void initMenu() {

        AHBottomNavigation bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);

        AHBottomNavigationItem item1 = new AHBottomNavigationItem(getString(R.string.tab_1),
                ContextCompat.getDrawable(this, R.drawable.ic_blur_circular_white_24dp),
                ContextCompat.getColor(this, R.color.color_tab_1));
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(getString(R.string.tab_2),
                ContextCompat.getDrawable(this, R.drawable.ic_palette_white_24dp),
                ContextCompat.getColor(this, R.color.color_tab_2));
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(getString(R.string.tab_3),
                ContextCompat.getDrawable(this, R.drawable.ic_brightness_4_white_24dp),
                ContextCompat.getColor(this, R.color.color_tab_3));
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(getString(R.string.tab_4),
                ContextCompat.getDrawable(this, R.drawable.ic_filter_vintage_white_24dp),
                ContextCompat.getColor(this, R.color.color_tab_4));
        AHBottomNavigationItem item5 = new AHBottomNavigationItem(getString(R.string.tab_5),
                ContextCompat.getDrawable(this, R.drawable.ic_looks_white_24dp),
                ContextCompat.getColor(this, R.color.color_tab_5));

        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);
        bottomNavigation.addItem(item4);
        bottomNavigation.addItem(item5);

        //bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#FEFEFE"));

        bottomNavigation.setTranslucentNavigationEnabled(true);

//        bottomNavigation.setAccentColor(Color.parseColor("#F63D2B"));
//        bottomNavigation.setInactiveColor(Color.parseColor("#747474"));

        //  Enables Reveal effect
        bottomNavigation.setColored(true);

        bottomNavigation.setCurrentItem(0);

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                // Do something cool here...

                TransformTest t = tests[position];

                if (artlib.requestTransform(src_img, t.transformType, t.intArgs, t.floatArgs)) {
                    makeToast("Transform requested : " + transforms[t.transformType]);
                } else {
                    makeToast("Transform request failed" + transforms[t.transformType]);
                }

                return true;
            }
        });
    }


    private void initSpinner() {

        testsArray = new ArrayList<String>();
        tests = artlib.getTestsArray();
        transforms = artlib.getTransformsArray();

        for (TransformTest t : tests) {
            String str = transforms[t.transformType] + " : " + Arrays.toString(t.intArgs) + " : " + Arrays.toString(t.floatArgs);
            testsArray.add(str);
        }

    }

    private void makeToast(String str) {
        Toast.makeText(getBaseContext(), str,
                Toast.LENGTH_SHORT).show();
    }


}
