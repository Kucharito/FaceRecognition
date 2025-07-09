package com.example.facerecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceLandmark;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private FaceDetector detector;
    private FrameLayout snowContainer;
    private static final int REQUEST_IMAGE_PICK = 1;

    private final Handler handler = new Handler();
    private boolean snowRunning = false;

    private final Runnable snowTask = new Runnable() {
        @Override
        public void run() {
            addSnowFlake();
            if (snowRunning) {
                // Add snow animation logic here
                handler.postDelayed(this, 300);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageView = findViewById(R.id.imageView);
        snowContainer = findViewById(R.id.snow_container);

        Button detectButton = findViewById(R.id.button4);
        detectButton.setOnClickListener(v -> {
            detectFace();
        });

        Button replaceButton = findViewById(R.id.button5);
        replaceButton.setOnClickListener(v -> {
            replaceFace();
        });

        Button hatButton = findViewById(R.id.button);
        hatButton.setOnClickListener(v -> {
            putHatOnFace();
        });

        Button animationButton = findViewById(R.id.button2);
        animationButton.setOnClickListener(v->{
            snowRunning = !snowRunning;
            if (snowRunning) {
                handler.post(snowTask);
                animationButton.setText("Stop Snow");
            } else {
                handler.removeCallbacks(snowTask);
                snowContainer.removeAllViews();
                animationButton.setText("Start Snow");
            }
        });


        Button galleryButton = findViewById(R.id.button3);
        galleryButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });

        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();

        detector= FaceDetection.getClient(highAccuracyOpts);

    }
    public void detectFace(){

        imageView.setImageResource(R.drawable.adam);
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        InputImage image= InputImage.fromBitmap(bitmap, 0);


        // Handle the error
        detector.process(image)
                .addOnSuccessListener(faces->{
                    Bitmap resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas canvas = new Canvas(resultBitmap);
                    Paint paint = new Paint();
                    paint.setColor(Color.YELLOW);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(15);

                    for (Face face : faces){
                        //tvar
                        canvas.drawRect(face.getBoundingBox(),paint);

                        //oci
                        FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
                        FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);
                        FaceLandmark mouth = face.getLandmark(FaceLandmark.MOUTH_BOTTOM);

                        if (leftEye!=null){
                            canvas.drawCircle(
                                    leftEye.getPosition().x,
                                    leftEye.getPosition().y,
                                    50,
                                    paint
                            );
                        }

                        if (rightEye!=null){
                            canvas.drawCircle(
                                    rightEye.getPosition().x,
                                    rightEye.getPosition().y,
                                    50,
                                    paint
                            );
                        }
                        if (mouth!=null){
                            canvas.drawLine(
                                    mouth.getPosition().x - 200,
                                    mouth.getPosition().y-50,
                                    mouth.getPosition().x + 200,
                                    mouth.getPosition().y-50,
                                    paint
                            );
                        }
                    }
                    imageView.setImageBitmap(resultBitmap);

                }).addOnFailureListener(Throwable::printStackTrace);
    }
    public void replaceFace(){
        imageView.setImageResource(R.drawable.adam);
        Bitmap original = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        InputImage image = InputImage.fromBitmap(original, 0);

        detector.process(image)
                .addOnSuccessListener(faces -> {
                    Bitmap resultBitmap = original.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas canvas = new Canvas(resultBitmap);

                    Bitmap emoji = BitmapFactory.decodeResource(getResources(), R.drawable.img);

                    for (Face face : faces) {
                        // Replace face with a red rectangle
                        float scale = 2.5f;

                        int width = Math.round(face.getBoundingBox().width() * scale);
                        int height = Math.round(face.getBoundingBox().height() * scale);

                        int left= face.getBoundingBox().left;
                        int top= face.getBoundingBox().top;

                        Bitmap replacementFace = Bitmap.createScaledBitmap(emoji,
                                width, height, false);

                        canvas.drawBitmap(replacementFace,
                                left, top, null);
                    }
                    imageView.setImageBitmap(resultBitmap);
                }).addOnFailureListener(Throwable::printStackTrace);
    }
    private void putHatOnFace() {
        Bitmap src = BitmapFactory.decodeResource(getResources(), R.drawable.adam);
        if (src.getWidth() > 1024 || src.getHeight() > 1024) {
            float k = 1024f / Math.max(src.getWidth(), src.getHeight());
            src = Bitmap.createScaledBitmap(src,
                    Math.round(src.getWidth() * k),
                    Math.round(src.getHeight() * k), true);
        }
        Bitmap bmp = src.copy(Bitmap.Config.ARGB_8888, true);

        detector.process(InputImage.fromBitmap(bmp, 0))
                .addOnSuccessListener(faces -> {
                    if (faces.isEmpty()){
                        return;
                    }

                    Canvas canvas = new Canvas(bmp);
                    Bitmap hat = BitmapFactory.decodeResource(getResources(),
                            R.drawable.img_3);

                    for (Face face : faces) {
                        Rect box = face.getBoundingBox();


                        float scale = 1.4f;
                        int hatWidth = Math.round(box.width() * scale);
                        int hatHeight = Math.round(hatWidth * hat.getHeight() / (float) hat.getWidth());

                        Bitmap hatScaled = Bitmap.createScaledBitmap(hat, hatWidth, hatHeight, true);


                        float x = box.centerX() - hatScaled.getWidth() / 3f;
                        float y = box.top - hatScaled.getHeight() * 0.75f;
                        canvas.drawBitmap(hatScaled, x, y, null);
                    }
                    imageView.setImageBitmap(bmp);
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            try {
                Uri imageUri = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addSnowFlake() {
        ImageView snowFlake = new ImageView(this);
        snowFlake.setImageResource(R.drawable.snowflake);

        int size= (int) (getResources().getDisplayMetrics().density * (30 + Math.random() * 20));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        snowContainer.addView(snowFlake, params);

        float startX = (float) (Math.random() * (snowContainer.getWidth() - size));
        snowFlake.setX(startX);
        snowFlake.setY(-size);
        snowFlake.animate()
                .translationY(snowContainer.getHeight() + size)
                .setDuration((long) (Math.random() * 3000 + 2000))
                .withEndAction(() -> {
                    snowContainer.removeView(snowFlake);
                });
    }
}