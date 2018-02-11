package com.example.lucifer.qr_code_sample;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final int CAMERA_RC = 101;
    private SurfaceView camView;
    private Button button;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSrc;
    private Detector.Processor<Barcode> processor;


    public boolean checkPermission()
    {
        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_RC);
                return false;
            }
            else
                return true;
        } else return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_RC:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startSurface();
                } else {
                    Toast.makeText(getApplicationContext(), "Camera Access is Required to Take Picture.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @SuppressLint("MissingPermission")
    private void startSurface(){
        try {
            barcodeDetector.setProcessor(processor);
            cameraSrc.start(camView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        camView = findViewById(R.id.surface_view);
        camView.getHolder().addCallback(this);

        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();

        processor=  new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0){
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), barcodes.valueAt(0).displayValue, Toast.LENGTH_LONG).show();
                            barcodeDetector.setProcessor(null);
                            cameraSrc.stop();
                        }
                    };
                    runOnUiThread(r);
                }
            }
        };

        cameraSrc = new CameraSource.Builder(getApplicationContext(), barcodeDetector).
                setRequestedPreviewSize(640, 480).
                setAutoFocusEnabled(true).
                build();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camView.setVisibility(View.VISIBLE);
                if(checkPermission())
                    startSurface();
            }
        });


        findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Main2Activity.class));
            }
        });

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if(checkPermission())
            startSurface();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        cameraSrc.stop();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
