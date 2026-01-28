package pepes.co.trofes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CameraActivity extends AppCompatActivity {

    public static final String EXTRA_RESULT_TEXT = "extra_result_text";

    private PreviewView previewView;
    private ImageButton btnCapture;
    private View btnUpload;
    private View btnBack;
    private View btnFlash;
    private ExecutorService cameraExecutor;
    private Camera camera;
    private ImageCapture imageCapture;
    private boolean isFlashOn = false;
    private ImageView ivFrame;

    private final ActivityResultLauncher<String> pickImageLauncher =
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                handleSelectedImage(uri);
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);
        ivFrame = findViewById(R.id.ivFrame);
        btnCapture = findViewById(R.id.btnCapture);
        btnUpload = findViewById(R.id.btnUpload);
        btnBack = findViewById(R.id.btnBack);
        btnFlash = findViewById(R.id.btnFlash);

        // default: not focused (white)
        setFrameStateNeutral();

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            );
        }

        btnCapture.setOnClickListener(v -> takePhoto());
        btnFlash.setOnClickListener(v -> toggleFlash());
        btnUpload.setOnClickListener(v -> openGallery());
        btnBack.setOnClickListener(v -> finish());
    }

    private void setFrameStateNeutral() {
        if (ivFrame != null) {
            ivFrame.setSelected(false);   // green off
            ivFrame.setActivated(false);  // red off
        }
    }

    private void setFrameStateFocused() {
        if (ivFrame != null) {
            ivFrame.setActivated(false);  // red off
            ivFrame.setSelected(true);    // green
        }
    }

    private void setFrameStateError() {
        if (ivFrame != null) {
            ivFrame.setSelected(false);   // green off
            ivFrame.setActivated(true);   // red
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                );

                // camera ready -> trigger one autofocus metering at center; frame becomes green if success
                triggerCenterAutoFocus();

                // enable/disable flash button depending on device
                boolean hasFlash = camera.getCameraInfo().hasFlashUnit();
                btnFlash.setEnabled(hasFlash);

            } catch (Exception e) {
                Log.e(TAG, "Use case binding failed", e);
                setFrameStateError();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void triggerCenterAutoFocus() {
        try {
            if (camera == null) return;
            if (previewView == null) return;

            setFrameStateNeutral();

            MeteringPointFactory factory = new SurfaceOrientedMeteringPointFactory(
                previewView.getWidth(),
                previewView.getHeight()
            );
            MeteringPoint point = factory.createPoint(previewView.getWidth() / 2f, previewView.getHeight() / 2f);

            FocusMeteringAction action = new FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                .setAutoCancelDuration(2, TimeUnit.SECONDS)
                .build();

            camera.getCameraControl().startFocusAndMetering(action)
                .addListener(() -> {
                    // If we got here without exception, assume focus attempt succeeded.
                    // (CameraX doesn't always expose AF result consistently across devices)
                    runOnUiThread(this::setFrameStateFocused);
                }, ContextCompat.getMainExecutor(this));

        } catch (Exception e) {
            Log.e(TAG, "Autofocus trigger failed", e);
            setFrameStateError();
        }
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(
            getOutputDirectory(),
            System.currentTimeMillis() + ".jpg"
        );

        ImageCapture.OutputFileOptions outputOptions =
            new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onError(@NonNull ImageCaptureException exc) {
                    Log.e(TAG, "Photo capture failed: " + exc.getMessage(), exc);
                    Toast.makeText(CameraActivity.this, "Capture failed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                    Log.d(TAG, "Photo capture succeeded: " + photoFile.getAbsolutePath());

                    Uri uri = Uri.fromFile(photoFile);
                    handleSelectedImage(uri);
                }
            }
        );
    }

    private void toggleFlash() {
        if (camera != null) {
            camera.getCameraControl().enableTorch(!isFlashOn);
            isFlashOn = !isFlashOn;
        }
    }

    private void openGallery() {
        pickImageLauncher.launch("image/*");
    }

    private void handleSelectedImage(@NonNull Uri uri) {
        // TODO: Panggil ML/OCR kamu di sini.
        // Untuk sekarang: stub kosong supaya wiring & flow jalan dulu.
        String recognizedText = runOcrStub(uri);

        Intent result = new Intent();
        result.putExtra(EXTRA_RESULT_TEXT, recognizedText);
        setResult(RESULT_OK, result);
        finish();
    }

    private String runOcrStub(@NonNull Uri uri) {
        // Kalau sudah ada model ML kamu, ganti isi fungsi ini.
        // Misalnya: return IngredientsOcrEngine.recognize(this, uri);
        return "";
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode,
        @NonNull String[] permissions,
        @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private File getOutputDirectory() {
        File[] mediaDirs = getExternalMediaDirs();
        File baseDir = (mediaDirs != null && mediaDirs.length > 0 && mediaDirs[0] != null)
            ? mediaDirs[0]
            : getFilesDir();

        File outputDir = new File(baseDir, getResources().getString(R.string.app_name));
        //noinspection ResultOfMethodCallIgnored
        outputDir.mkdirs();
        return outputDir;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    private static final String TAG = "CameraActivity";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
        Manifest.permission.CAMERA
    };
}
