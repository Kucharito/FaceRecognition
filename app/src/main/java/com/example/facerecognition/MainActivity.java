package com.example.facerecognition;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.widget.Button;
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
        Button detectButton = findViewById(R.id.button4);
        detectButton.setOnClickListener(v -> {
            detectFace();
        });

        Button replaceButton = findViewById(R.id.button5);
        replaceButton.setOnClickListener(v -> {
            replaceFace();
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
}