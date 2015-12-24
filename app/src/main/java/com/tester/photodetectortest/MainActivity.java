package com.tester.photodetectortest;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.detector.Detector;
import com.affectiva.android.affdex.sdk.detector.Face;
import com.affectiva.android.affdex.sdk.detector.PhotoDetector;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Detector.ImageListener{
    public static final String TAG = MainActivity.class.getSimpleName();
    private RelativeLayout mainContainer;
    private PhotoDetector pd;
    private Camera mCamera;
    private Preview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCamera = Camera.open(1);
        mainContainer = (RelativeLayout) findViewById(R.id.main_container);

        mPreview = new Preview(this, mCamera);
        mainContainer.addView(mPreview, 0);

        pd = new PhotoDetector(this);
        pd.setLicensePath("sdk.license");
        pd.setImageListener(this);
        pd.setDetectAllEmotions(true);
        pd.setDetectAllExpressions(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.takePicture(null, null, mPictureCallback);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT>=23) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 28);
            }
        }
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        startDetector();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopDetector();
    }

    void startDetector() {
        if(pd.isRunning()==false)
            pd.start();

    }

    void stopDetector() {
        if(pd.isRunning()==true)
            pd.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImageResults(List<Face> list, Frame frame, float v) {
        if (list == null)
            return;

        if (list.size() == 0) {
            Log.d(TAG, "NO FACE");
        } else {
            Face face = list.get(0);

            Log.d(TAG, String.format("ANGER:%.2f\n", face.emotions.getAnger()));
            Log.d(TAG, String.format("DISGUST:%.2f\n", face.emotions.getDisgust()));
            Log.d(TAG, String.format("FEAR:%.2f\n", face.emotions.getFear()));
            Log.d(TAG, String.format("JOY:%.2f\n", face.emotions.getJoy()));
            Log.d(TAG, String.format("SADNESS:%.2f\n", face.emotions.getSadness()));
            Log.d(TAG, String.format("SURPRISE:%.2f\n", face.emotions.getSurprise()));
        }
    }
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            matrix.postRotate(270f);
            bm =  Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            try {
                Frame f = new Frame.BitmapFrame(bm, Frame.COLOR_FORMAT.RGBA);
                //f.setTargetRotation(Frame.ROTATE.BY_90_CCW);
                pd.process(f);
            } catch(Exception e) {
                e.printStackTrace();
            }

//            ByteArrayOutputStream bmpStream = new ByteArrayOutputStream();
//            bm.compress(Bitmap.CompressFormat.JPEG, 50, bmpStream);
//            bm = BitmapFactory.decodeByteArray(bmpStream.toByteArray(), 0, bmpStream.size());

            File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                    + "/Android/data/"
                    + getApplicationContext().getPackageName()
                    + "/Files");
            File mediaFile;
            String mImageName="photo.jpg";
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);

            if (! mediaStorageDir.exists()){
                mediaStorageDir.mkdirs();
            }
            try {
                FileOutputStream fos = new FileOutputStream(mediaFile);
                bm.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.close();
            } catch(Exception e) {
                Log.e(this.getClass().getSimpleName(), "onPictureTaken(): exception caught", e);
            }
//            photo.setImageBitmap(bm);
//            moment.setPhoto(bm);
//            mPreview.setVisibility(View.GONE);
//            resultContainer.setVisibility(View.VISIBLE);
        }
    };
}
