package edu.asu.msrs.artceleration;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import edu.asu.msrs.artcelerationlibrary.ArtLib;
import edu.asu.msrs.artcelerationlibrary.TransformHandler;
import edu.asu.msrs.artcelerationlibrary.TransformTest;

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
    android.hardware.camera2.CaptureRequest cr;
    ArrayList<String> testsArray;
    TransformTest[] tests;
    String[] transforms;
    Bitmap src_img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_viewer);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        src_img = BitmapFactory.decodeResource(getResources(), R.drawable.asuhayden, opts);


        spinner = (Spinner) findViewById(R.id.spinner);
        status1 = (TextView) findViewById(R.id.statusText1);
        status2 = (TextView) findViewById(R.id.statusText2);
        artview = (ArtView) findViewById(R.id.artView);

        artlib = new ArtLib();

        artlib.registerHandler(new TransformHandler() {
            @Override
            public void onTransformProcessed(Bitmap img_out) {
                artview.setTransBmp(img_out);
            }
        });

        initSpinner();
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, testsArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TransformTest t = tests[position];
                if (artlib.requestTransform(src_img, t.transformType, t.intArgs, t.floatArgs)){
                    makeToast("Transform requested : "+ transforms[t.transformType]);
                }else{
                    makeToast("Transform request failed"+ transforms[t.transformType]);
                }
            }

            @Override
            public void onNothingSelected (AdapterView < ? > parent){

            }
        });

    }

    private void initSpinner() {
        testsArray = new ArrayList<String>();
        tests = artlib.getTestsArray();
        transforms = artlib.getTransformsArray();

        for (TransformTest t : tests){
            String str = transforms[t.transformType] + " : " + Arrays.toString(t.intArgs)+" : "+ Arrays.toString(t.floatArgs);
            testsArray.add(str);
        }

    }
    private void makeToast(String str){
        Toast.makeText(getBaseContext(), str,
                Toast.LENGTH_LONG).show();
    }
}
