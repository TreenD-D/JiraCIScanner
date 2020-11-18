package com.example.myapplication;


import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageInfo;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.TorchState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static android.graphics.ImageFormat.YUV_420_888;
import static android.graphics.ImageFormat.YUV_422_888;
import static android.graphics.ImageFormat.YUV_444_888;
import static androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST;



public class BarcodeScanningActivity extends AppCompatActivity implements ImageAnalysis.Analyzer{


    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageAnalysis imageAnalysis;
    private Camera camera;





/*    private int degreesToFirebaseRotation(int degrees) {
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
    }*/




    @Override
    @androidx.camera.core.ExperimentalGetImage
    public void analyze(ImageProxy imageProxy) {
        if (imageProxy == null || imageProxy.getImage() == null) {
            return;
        }
        Image mediaImage = imageProxy.getImage();

//        ImageInfo info = imageProxy.getImageInfo();
        if (mediaImage != null) {
            InputImage image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            //options to limit types of detectable barcodes
            BarcodeScannerOptions options =
                    new BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(
                                    Barcode.FORMAT_QR_CODE,
                                    Barcode.FORMAT_CODE_128)
                            .build();

            BarcodeScanner detector = BarcodeScanning.getClient(options);

            Task<List<Barcode>> result = detector.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            String rawValue = null;
                            for (Barcode barcode : barcodes) {

                                rawValue = barcode.getRawValue();
                                Log.i("Detection suceeded", "Got barcode raw value: " + rawValue);


                            }


                            //    result examples
                            //    1d barcode raw value:: CI-0000279569
                             //   new QR raw value:: https://sd.nexign.com/secure/ShowObject.jspa?id=CI-75886

                            if (rawValue != null) {
                                //Toast.makeText(getApplicationContext(), rawValue, Toast.LENGTH_SHORT).show();
                                //imageProxy.close();
                                imageAnalysis.clearAnalyzer();


                                detector.close();


                                Intent intent = new Intent(BarcodeScanningActivity.this, MainActivity.class);
                                intent.putExtra("BARCODE_RAW_VALUE", rawValue);
                                BarcodeScanningActivity.this.setResult(RESULT_OK, intent);
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

        }
        imageProxy.close();


    }

    void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .setTargetName("Preview")

                .build();

        PreviewView previewView = findViewById(R.id.preview_view);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        CameraSelector cameraSelector =
                new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();


        imageAnalysis = new ImageAnalysis.Builder()
                //.setTargetResolution(new Size (1280, 720))
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                .build();



        imageAnalysis.setAnalyzer(Executors.newFixedThreadPool(10), this::analyze);



        camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);



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

    void toggleTorch(){

        if(camera.getCameraInfo().getTorchState().getValue() == TorchState.OFF){
        camera.getCameraControl().enableTorch(true);
        } else {camera.getCameraControl().enableTorch(false);}


    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_scanning_activity);





        startCamera();




    }




}
