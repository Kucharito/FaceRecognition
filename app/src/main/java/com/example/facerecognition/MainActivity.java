package com.example.facerecognition;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
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

        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();

        detector= FaceDetection.getClient(highAccuracyOpts);
        detectButton.setOnClickListener(v -> {
            detectFace();
        });
    }
    public void detectFace(){
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
}