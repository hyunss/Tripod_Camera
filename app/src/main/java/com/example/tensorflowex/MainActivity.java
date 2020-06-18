package com.example.tensorflowex;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.yongchun.library.view.ImageSelectorActivity;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int INPUT_SIZE = 299;
    private static final int IMAGE_MEAN = 0;
    private static final float IMAGE_STD = 255.0f;
    private static final String INPUT_NAME = "Mul";
    private static final String OUTPUT_NAME = "final_result";


    private static final String MODEL_FILE = "file:///android_asset/optimized_graph.pb";
    private static final String MODEL_LINE_FILE = "file:///android_asset/optimized_graph2.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/retrained_labels.txt";

    private Classifier classifier;
    private Classifier_line classifier_line;
    private Executor executor = Executors.newSingleThreadExecutor();

    private TextView txtResult;
    private TextView txtResult_line;
    private ImageView imgResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtResult = (TextView)findViewById(R.id.txtResult);
        txtResult_line = (TextView)findViewById(R.id.txtResult_line) ;
        imgResult = (ImageView)findViewById(R.id.imgResult);

        Button btnGallery = (Button) findViewById(R.id.btnGallery);

        //텐서플로우 초기화 및 그래프파일 메모리에 탑재
        initTensorFlowAndLoadModel();

        // 각종 권한체크 (외부라이브러리 이용)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d("TAG", "권한 설정 완료");
            } else {
                Log.d("TAG", "권한 설정 요청");
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            INPUT_SIZE,
                            IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NAME,
                            OUTPUT_NAME);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }


                try {
                    classifier_line = TensorFlowImageClassifier_line.create(
                            getAssets(),
                            MODEL_LINE_FILE,
                            LABEL_FILE,
                            INPUT_SIZE,
                            IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NAME,
                            OUTPUT_NAME);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }

            }
        });
    }


    public void GallaryOnClick(View view) {
        LoadImageFromGallery();
    }

    private void LoadImageFromGallery() {
        // 이미지를 한장만 선택하도록 이미지피커 실행
        ImageSelectorActivity.start(MainActivity.this, 1, ImageSelectorActivity.MODE_SINGLE, false,false,false);
    }

    // 가져온 이미지를 텐서플로우로 넘기기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 이미지피커에서 선택된 이미지를 텐서플로우로 넘깁니다.
        // 이미지피커는 ArrayList 로 값을 리턴합니다.

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ImageSelectorActivity.REQUEST_IMAGE) {
            ArrayList<String> images = (ArrayList<String>) data.getSerializableExtra(ImageSelectorActivity.REQUEST_OUTPUT);

            // 이미지는 안드로이드용 텐서플로우가 인식할 수 있는 포맷인 비트맵으로 변환해서 텐서플로우에 넘깁니다
            Bitmap bitmap = BitmapFactory.decodeFile(images.get(0));
            Bitmap bitmap_line = BitmapFactory.decodeFile(images.get(0));

            // 촬영시 가로, 세로 방향에 따라 사진 rotate
            // 이미지의 EXIF 데이터를 보면 사진을 촬영할 때 카메라의 방향을 알 수 있음.
            try {
                ExifInterface exif = new ExifInterface(images.get(0));
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                Log.d("EXIF", "Exif: " + orientation);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // rotating bitmap
                bitmap_line = Bitmap.createBitmap(bitmap_line, 0, 0, bitmap_line.getWidth(), bitmap_line.getHeight(), matrix, true); // rotating bitmap
            } catch (IOException e) {
                e.printStackTrace();
            }

            //
            OpenCVLoader.initDebug();

            Mat initImg = new Mat(); // initial image
            Mat greyImg = new Mat(); // converted to grey
            Mat lines = new Mat();
            int threshold = 50;
            int minLineSize = 20;
            int lineGap = 10;
            Utils.bitmapToMat(bitmap_line, initImg);
            Imgproc.cvtColor(initImg, greyImg, Imgproc.COLOR_BGR2GRAY);
            //Bitmap bitm = Bitmap.createBitmap(greyImg.cols(), greyImg.rows(),Bitmap.Config.ARGB_8888);
            Imgproc.Canny(greyImg, greyImg, 250, 300, 3, true);
            Imgproc.HoughLinesP(greyImg, lines, 1, Math.PI / 180, threshold,
                    minLineSize, lineGap);

            for (int x = 0; x < lines.rows(); x++) {
                double[] vec = lines.get(x, 0);
                double x1 = vec[0],
                        y1 = vec[1],
                        x2 = vec[2],
                        y2 = vec[3];
                Point start = new Point(x1, y1);
                Point end = new Point(x2, y2);
                Imgproc.line(initImg, start, end, new Scalar(0, 0, 255), 5);// here initimg is the original image.
            }
            Utils.matToBitmap(initImg, bitmap_line);

            recognize_bitmap(bitmap, bitmap_line);
        }
    }
    private void recognize_bitmap(Bitmap bitmap, Bitmap bitmap_line) {

        // 비트맵을 처음에 정의된 INPUT SIZE에 맞춰 스케일링 (상의 왜곡이 일어날수 있는데, 이건 나중에 따로 설명할게요)
        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        bitmap_line = Bitmap.createScaledBitmap(bitmap_line, INPUT_SIZE, INPUT_SIZE, false);
// classifier 의 recognizeImage 부분이 실제 inference 를 호출해서 인식작업을 하는 부분입니다.
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
        final List<Classifier_line.Recognition> results_line = classifier_line.recognizeImage(bitmap_line);

        // 결과값은 Classifier.Recognition 구조로 리턴되는데, 원래는 여기서 결과값을 배열로 추출가능하지만,
        // 여기서는 간단하게 그냥 통째로 txtResult에 뿌려줍니다.
        // imgResult에는 분석에 사용된 비트맵을 뿌려줍니다.

        imgResult.setImageBitmap(bitmap_line);
        txtResult.setText(results.toString());
        txtResult_line.setText(results_line.toString());
    }
}
