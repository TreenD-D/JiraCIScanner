package com.example.myapplication;


import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageInfo;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST;



public class BarcodeScanningActivity extends AppCompatActivity implements ImageAnalysis.Analyzer{


    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ImageAnalysis imageAnalysis;



    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException(
                        "Rotation must be 0, 90, 180, or 270.");
        }
    }

    @Override
    @androidx.camera.core.ExperimentalGetImage
    public void analyze(ImageProxy imageProxy) {
        if (imageProxy == null || imageProxy.getImage() == null) {
            return;
        }
        Image mediaImage = imageProxy.getImage();

        ImageInfo info = imageProxy.getImageInfo();
        int degrees = info.getRotationDegrees();

        int rotation = degreesToFirebaseRotation(degrees);
        FirebaseVisionImage image =
                FirebaseVisionImage.fromMediaImage(mediaImage, rotation);

        //options to limit types of detectable barcodes
        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_QR_CODE,
                                FirebaseVisionBarcode.FORMAT_CODE_128)
                        .build();

        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options);

        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        String rawValue = null;
                        for (FirebaseVisionBarcode barcode: barcodes) {

                            rawValue = barcode.getRawValue();
                            Log.i("Detection suceeded", "Got barcode raw value: " + rawValue);







                        }

                        /*
                                result examples
                                1d barcode raw value:: CI-0000279569
                                new QR raw value:: https://sd.nexign.com/secure/ShowObject.jspa?id=CI-75886
                            */
                        if(rawValue!=null) {
                            //Toast.makeText(getApplicationContext(), rawValue, Toast.LENGTH_SHORT).show();
                            //imageProxy.close();
                            imageAnalysis.clearAnalyzer();

                            try {
                                detector.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            Intent intent = new Intent(BarcodeScanningActivity.this, MainActivity.class);
                            intent.putExtra("BARCODE_RAW_VALUE", rawValue);
                            BarcodeScanningActivity.this.setResult(RESULT_OK,intent);
                            BarcodeScanningActivity.this.finish();


                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception

                    }
                });


        imageProxy.close();


    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .setTargetName("Preview")
                .build();

        PreviewView previewView = findViewById(R.id.preview_view);

        preview.setPreviewSurfaceProvider(previewView.getPreviewSurfaceProvider());

        CameraSelector cameraSelector =
                new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();


        imageAnalysis = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                .build();



        imageAnalysis.setAnalyzer(Executors.newFixedThreadPool(10), this::analyze);



        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);


    }

    void startCamera()
    {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future
                // This should never be reached
            }
        }, ContextCompat.getMainExecutor(this));



    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_scanning_activity);





        startCamera();




    }




}
