package com.example.tensorflowex;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.yongchun.library.view.ImageSelectorActivity;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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

    private TextView txtResult0;
    private ImageView imgResult0;
    private TextView txtResult1;
    private ImageView imgResult1;
    private TextView txtResult2;
    private ImageView imgResult2;
    private TextView txtResult3;
    private ImageView imgResult3;
    private TextView txtResult4;
    private ImageView imgResult4;
    private TextView txtResult5;
    private ImageView imgResult5;

    private CheckBox checkBox0;
    private CheckBox checkBox1;
    private CheckBox checkBox2;
    private CheckBox checkBox3;
    private CheckBox checkBox4;
    private CheckBox checkBox5;
    private ImageButton savebtn;

    Bitmap bitmap0;
    Bitmap bitmap1;
    Bitmap bitmap2;
    Bitmap bitmap3;
    Bitmap bitmap4;
    Bitmap bitmap5;

    Bitmap bitmap_line0;
    Bitmap bitmap_line1;
    Bitmap bitmap_line2;
    Bitmap bitmap_line3;
    Bitmap bitmap_line4;
    Bitmap bitmap_line5;

    Bitmap[] bitmaps_result = new Bitmap[6];
    double[] percent_result = new double[6];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        imgResult0 = (ImageView)findViewById(R.id.imgResult0);
        txtResult0 = (TextView) findViewById(R.id.txtResult0);

        imgResult1 = (ImageView)findViewById(R.id.imgResult1);
        txtResult1 = (TextView) findViewById(R.id.txtResult1);

        imgResult2 = (ImageView)findViewById(R.id.imgResult2);
        txtResult2 = (TextView) findViewById(R.id.txtResult2);

        imgResult3 = (ImageView)findViewById(R.id.imgResult3);
        txtResult3 = (TextView) findViewById(R.id.txtResult3);

        imgResult4 = (ImageView)findViewById(R.id.imgResult4);
        txtResult4 = (TextView) findViewById(R.id.txtResult4);

        imgResult5 = (ImageView)findViewById(R.id.imgResult5);
        txtResult5 = (TextView) findViewById(R.id.txtResult5);

        savebtn = (ImageButton) findViewById(R.id.saveBtn);


        checkBox0 = (CheckBox) findViewById(R.id.check0);
        checkBox1 = (CheckBox) findViewById(R.id.check1);
        checkBox2 = (CheckBox) findViewById(R.id.check2);
        checkBox3 = (CheckBox) findViewById(R.id.check3);
        checkBox4 = (CheckBox) findViewById(R.id.check4);
        checkBox5 = (CheckBox) findViewById(R.id.check5);


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

//        try {
//            Thread.sleep(5000);
//            bit();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

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
        ImageButton btnGallery = (ImageButton)findViewById(R.id.btnGallery);
        btnGallery.setVisibility(View.GONE);
        bit();
       // LoadImageFromGallery();
        //LinearLayout chl = (LinearLayout)findViewById(R.id.chLinear);
        CheckBox ch0 = (CheckBox)findViewById(R.id.check0);
        CheckBox ch1 = (CheckBox)findViewById(R.id.check1);
        CheckBox ch2 = (CheckBox)findViewById(R.id.check2);
        CheckBox ch3 = (CheckBox)findViewById(R.id.check3);
        CheckBox ch4 = (CheckBox)findViewById(R.id.check4);
        CheckBox ch5 = (CheckBox)findViewById(R.id.check5);
        ch0.setVisibility(View.VISIBLE);
        ch1.setVisibility(View.VISIBLE);
        ch2.setVisibility(View.VISIBLE);
        ch3.setVisibility(View.VISIBLE);
        ch4.setVisibility(View.VISIBLE);
        ch5.setVisibility(View.VISIBLE);

    }

    public void bit(){

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;

        bitmap0 = BitmapFactory.decodeFile("storage/emulated/0/0.jpg", options);
        bitmap1 = BitmapFactory.decodeFile("storage/emulated/0/1.jpg", options);
        bitmap2 = BitmapFactory.decodeFile("storage/emulated/0/2.jpg", options);
        bitmap3 = BitmapFactory.decodeFile("storage/emulated/0/3.jpg", options);
        bitmap4 = BitmapFactory.decodeFile("storage/emulated/0/4.jpg", options);
        bitmap5 = BitmapFactory.decodeFile("storage/emulated/0/5.jpg", options);

        bitmap_line0 = BitmapFactory.decodeFile("storage/emulated/0/0.jpg", options);
        bitmap_line1 = BitmapFactory.decodeFile("storage/emulated/0/1.jpg", options);
        bitmap_line2 = BitmapFactory.decodeFile("storage/emulated/0/2.jpg", options);
        bitmap_line3 = BitmapFactory.decodeFile("storage/emulated/0/3.jpg", options);
        bitmap_line4 = BitmapFactory.decodeFile("storage/emulated/0/4.jpg", options);
        bitmap_line5 = BitmapFactory.decodeFile("storage/emulated/0/5.jpg", options);

        // 이미지는 안드로이드용 텐서플로우가 인식할 수 있는 포맷인 비트맵으로 변환해서 텐서플로우에 넘깁니다

        OpenCVLoader.initDebug();

        Mat initImg = new Mat(); // initial image
        Mat greyImg = new Mat(); // converted to grey
        Mat lines = new Mat();
        int threshold = 50;
        int minLineSize = 20;
        int lineGap = 10;
        Utils.bitmapToMat(bitmap_line0, initImg);
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
        Utils.matToBitmap(initImg, bitmap_line0);

        Utils.bitmapToMat(bitmap_line1, initImg);
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
        Utils.matToBitmap(initImg, bitmap_line1);

        Utils.bitmapToMat(bitmap_line2, initImg);
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
        Utils.matToBitmap(initImg, bitmap_line2);

        Utils.bitmapToMat(bitmap_line3, initImg);
        Imgproc.cvtColor(initImg, greyImg, Imgproc.COLOR_BGR2GRAY);

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
        Utils.matToBitmap(initImg, bitmap_line3);

        Utils.bitmapToMat(bitmap_line4, initImg);
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
        Utils.matToBitmap(initImg, bitmap_line4);
        Utils.bitmapToMat(bitmap_line5, initImg);
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
        Utils.matToBitmap(initImg, bitmap_line5);

        recognize_bitmap0(bitmap0, bitmap_line0);
        recognize_bitmap1(bitmap1, bitmap_line1);
        recognize_bitmap2(bitmap2, bitmap_line2);
        recognize_bitmap3(bitmap3, bitmap_line3);
        recognize_bitmap4(bitmap4, bitmap_line4);
        recognize_bitmap5(bitmap5, bitmap_line5);

        //good의 percent비교하여 오름차순 정렬
        Bitmap tmp_b;
        double tmp;
        for(int i=0; i<6; i++){
            for(int j=i+1; j<6; j++){
                if(percent_result[i] < percent_result[j]){
                    tmp = percent_result[i];
                    percent_result[i] = percent_result[j];
                    percent_result[j] = tmp;

                    tmp_b = bitmaps_result[i];
                    bitmaps_result[i] = bitmaps_result[j];
                    bitmaps_result[j] = tmp_b;

                }
            }
        }

        imgResult0.setImageBitmap(bitmaps_result[0]);
        imgResult1.setImageBitmap(bitmaps_result[1]);
        imgResult2.setImageBitmap(bitmaps_result[2]);
        imgResult3.setImageBitmap(bitmaps_result[3]);
        imgResult4.setImageBitmap(bitmaps_result[4]);
        imgResult5.setImageBitmap(bitmaps_result[5]);

        txtResult0.setText(""+Math.round(percent_result[0] * 100) / 100.0);
        txtResult1.setText(""+Math.round(percent_result[1] * 100) / 100.0);
        txtResult2.setText(""+Math.round(percent_result[2] * 100) / 100.0);
        txtResult3.setText(""+Math.round(percent_result[3] * 100) / 100.0);
        txtResult4.setText(""+Math.round(percent_result[4] * 100) / 100.0);
        txtResult5.setText(""+Math.round(percent_result[5] * 100) / 100.0);




        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(getApplicationContext(), "갤러리 저장 완료", Toast.LENGTH_LONG);
                toast.show();
                saveImage();
            }
        });



    }
    // 가져온 이미지를 텐서플로우로 넘기기

    int count=0;
    public void saveImage(){
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();

        String foler_name = "/seoul/";
        String file_name;
        String string_path = ex_storage+foler_name;
        File file_path;
        if(checkBox0.isChecked()){

            try{
                file_name = count++ +".jpg";
                file_path = new File(string_path);
                if(!file_path.isDirectory()){
                    file_path.mkdirs();
                }
                FileOutputStream out = new FileOutputStream(string_path+file_name);
                bitmaps_result[0].compress(Bitmap.CompressFormat.JPEG, 50, out);
                out.close();
                sendBroadcast(new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://"+string_path+file_name)) );

            }catch(FileNotFoundException exception){
                Log.e("FileNotFoundException", exception.getMessage());
            }catch(IOException exception){
                Log.e("IOException", exception.getMessage());
            }

        }
        if(checkBox1.isChecked()){

            try{
                file_name = count++ +".jpg";
                file_path = new File(string_path);
                if(!file_path.isDirectory()){
                    file_path.mkdirs();
                }
                FileOutputStream out = new FileOutputStream(string_path+file_name);
                bitmaps_result[1].compress(Bitmap.CompressFormat.JPEG, 50, out);
                out.close();
                sendBroadcast(new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://"+string_path+file_name)) );

            }catch(FileNotFoundException exception){
                Log.e("FileNotFoundException", exception.getMessage());
            }catch(IOException exception){
                Log.e("IOException", exception.getMessage());
            }
        }
        if(checkBox2.isChecked()){

            try{
                file_name = count++ +".jpg";
                file_path = new File(string_path);
                if(!file_path.isDirectory()){
                    file_path.mkdirs();
                }
                FileOutputStream out = new FileOutputStream(string_path+file_name);
                bitmaps_result[2].compress(Bitmap.CompressFormat.JPEG, 50, out);
                out.close();
                sendBroadcast(new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://"+string_path+file_name)) );

            }catch(FileNotFoundException exception){
                Log.e("FileNotFoundException", exception.getMessage());
            }catch(IOException exception){
                Log.e("IOException", exception.getMessage());
            }
        }
        if(checkBox3.isChecked()){

            try{
                file_name = count++ +".jpg";
                file_path = new File(string_path);
                if(!file_path.isDirectory()){
                    file_path.mkdirs();
                }
                FileOutputStream out = new FileOutputStream(string_path+file_name);
                bitmaps_result[3].compress(Bitmap.CompressFormat.JPEG, 50, out);
                out.close();
                sendBroadcast(new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://"+string_path+file_name)) );

            }catch(FileNotFoundException exception){
                Log.e("FileNotFoundException", exception.getMessage());
            }catch(IOException exception){
                Log.e("IOException", exception.getMessage());
            }
        }
        if(checkBox4.isChecked()){

            try{
                file_name = count++ +".jpg";
                file_path = new File(string_path);
                if(!file_path.isDirectory()){
                    file_path.mkdirs();
                }
                FileOutputStream out = new FileOutputStream(string_path+file_name);
                bitmaps_result[4].compress(Bitmap.CompressFormat.JPEG, 50, out);
                out.close();
                sendBroadcast(new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://"+string_path+file_name)) );

            }catch(FileNotFoundException exception){
                Log.e("FileNotFoundException", exception.getMessage());
            }catch(IOException exception){
                Log.e("IOException", exception.getMessage());
            }
        }
        if(checkBox5.isChecked()){

            try{
                file_name = count++ +".jpg";
                file_path = new File(string_path);
                if(!file_path.isDirectory()){
                    file_path.mkdirs();
                }
                FileOutputStream out = new FileOutputStream(string_path+file_name);
                bitmaps_result[5].compress(Bitmap.CompressFormat.JPEG, 50, out);
                out.close();
                sendBroadcast(new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://"+string_path+file_name)) );

            }catch(FileNotFoundException exception){
                Log.e("FileNotFoundException", exception.getMessage());
            }catch(IOException exception){
                Log.e("IOException", exception.getMessage());
            }
        }
    }
    private void recognize_bitmap0(Bitmap bitmap, Bitmap bitmap_line) {
        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        bitmap_line = Bitmap.createScaledBitmap(bitmap_line, INPUT_SIZE, INPUT_SIZE, false);
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
        final List<Classifier_line.Recognition> results_line = classifier_line.recognizeImage(bitmap_line);
        String[] Results = results.toString().split(",");
        String[] Results_line = results_line.toString().split(",");

        String a = "0";
        String b = "0";

        Results[0] = Results[0].substring(1);
        Results_line[0] = Results_line[0].substring(1);

        if(Results.length > 2){
            Results[3] = Results[3].substring(0, Results[3].length()-1);
        }
        if(Results_line.length > 2){
            Results_line[3] = Results_line[3].substring(0, Results_line[3].length()-1);
        }

        if(Results[0].equals("good")){
            a = Results[1];
        }
        if(Results_line[0].equals("good")){
            b = Results_line[1];
        }

        if(Results.length > 2){
            if(Results[0].equals("bad")){
                a = Results[3];
            }
        }
        if(Results_line.length > 2){
            if(Results_line[0].equals("bad")){
                b = Results_line[3];
            }
        }

        bitmaps_result[0] = bitmap;
        percent_result[0] = 8*Double.parseDouble(a) + 2*Double.parseDouble(b);
    }
    private void recognize_bitmap1(Bitmap bitmap, Bitmap bitmap_line) {
        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        bitmap_line = Bitmap.createScaledBitmap(bitmap_line, INPUT_SIZE, INPUT_SIZE, false);
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
        final List<Classifier_line.Recognition> results_line = classifier_line.recognizeImage(bitmap_line);
        String[] Results = results.toString().split(",");
        String[] Results_line = results_line.toString().split(",");

        String a = "0";
        String b = "0";

        Results[0] = Results[0].substring(1);
        Results_line[0] = Results_line[0].substring(1);

        if(Results.length > 2){
            Results[3] = Results[3].substring(0, Results[3].length()-1);
        }
        if(Results_line.length > 2){
            Results_line[3] = Results_line[3].substring(0, Results_line[3].length()-1);
        }

        if(Results[0].equals("good")){
            a = Results[1];
        }
        if(Results_line[0].equals("good")){
            b = Results_line[1];
        }

        if(Results.length > 2){
            if(Results[0].equals("bad")){
                a = Results[3];
            }
        }
        if(Results_line.length > 2){
            if(Results_line[0].equals("bad")){
                b = Results_line[3];
            }
        }

        bitmaps_result[1] = bitmap;
        percent_result[1] = 8*Double.parseDouble(a) + 2*Double.parseDouble(b);
    }
    private void recognize_bitmap2(Bitmap bitmap, Bitmap bitmap_line) {
        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        bitmap_line = Bitmap.createScaledBitmap(bitmap_line, INPUT_SIZE, INPUT_SIZE, false);
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
        final List<Classifier_line.Recognition> results_line = classifier_line.recognizeImage(bitmap_line);
        String[] Results = results.toString().split(",");
        String[] Results_line = results_line.toString().split(",");

        String a = "0";
        String b = "0";

        Results[0] = Results[0].substring(1);
        Results_line[0] = Results_line[0].substring(1);

        if(Results.length > 2){
            Results[3] = Results[3].substring(0, Results[3].length()-1);
        }
        if(Results_line.length > 2){
            Results_line[3] = Results_line[3].substring(0, Results_line[3].length()-1);
        }

        if(Results[0].equals("good")){
            a = Results[1];
        }
        if(Results_line[0].equals("good")){
            b = Results_line[1];
        }

        if(Results.length > 2){
            if(Results[0].equals("bad")){
                a = Results[3];
            }
        }
        if(Results_line.length > 2){
            if(Results_line[0].equals("bad")){
                b = Results_line[3];
            }
        }

        bitmaps_result[2] = bitmap;
        percent_result[2] = 8*Double.parseDouble(a) + 2*Double.parseDouble(b);
    }
    private void recognize_bitmap3(Bitmap bitmap, Bitmap bitmap_line) {
        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        bitmap_line = Bitmap.createScaledBitmap(bitmap_line, INPUT_SIZE, INPUT_SIZE, false);
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
        final List<Classifier_line.Recognition> results_line = classifier_line.recognizeImage(bitmap_line);
        String[] Results = results.toString().split(",");
        String[] Results_line = results_line.toString().split(",");

        String a = "0";
        String b = "0";

        Results[0] = Results[0].substring(1);
        Results_line[0] = Results_line[0].substring(1);

        if(Results.length > 2){
            Results[3] = Results[3].substring(0, Results[3].length()-1);
        }
        if(Results_line.length > 2){
            Results_line[3] = Results_line[3].substring(0, Results_line[3].length()-1);
        }

        if(Results[0].equals("good")){
            a = Results[1];
        }
        if(Results_line[0].equals("good")){
            b = Results_line[1];
        }

        if(Results.length > 2){
            if(Results[0].equals("bad")){
                a = Results[3];
            }
        }
        if(Results_line.length > 2){
            if(Results_line[0].equals("bad")){
                b = Results_line[3];
            }
        }

        bitmaps_result[3] = bitmap;
        percent_result[3] = 8*Double.parseDouble(a) + 2*Double.parseDouble(b);
    }
    private void recognize_bitmap4(Bitmap bitmap, Bitmap bitmap_line) {
        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        bitmap_line = Bitmap.createScaledBitmap(bitmap_line, INPUT_SIZE, INPUT_SIZE, false);
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
        final List<Classifier_line.Recognition> results_line = classifier_line.recognizeImage(bitmap_line);
        String[] Results = results.toString().split(",");
        String[] Results_line = results_line.toString().split(",");

        String a = "0";
        String b = "0";

        Results[0] = Results[0].substring(1);
        Results_line[0] = Results_line[0].substring(1);

        if(Results.length > 2){
            Results[3] = Results[3].substring(0, Results[3].length()-1);
        }
        if(Results_line.length > 2){
            Results_line[3] = Results_line[3].substring(0, Results_line[3].length()-1);
        }

        if(Results[0].equals("good")){
            a = Results[1];
        }
        if(Results_line[0].equals("good")){
            b = Results_line[1];
        }

        if(Results.length > 2){
            if(Results[0].equals("bad")){
                a = Results[3];
            }
        }
        if(Results_line.length > 2){
            if(Results_line[0].equals("bad")){
                b = Results_line[3];
            }
        }

        bitmaps_result[4] = bitmap;
        percent_result[4] = 8*Double.parseDouble(a) + 2*Double.parseDouble(b);
    }
    private void recognize_bitmap5(Bitmap bitmap, Bitmap bitmap_line) {
        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        bitmap_line = Bitmap.createScaledBitmap(bitmap_line, INPUT_SIZE, INPUT_SIZE, false);
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
        final List<Classifier_line.Recognition> results_line = classifier_line.recognizeImage(bitmap_line);
        String[] Results = results.toString().split(",");
        String[] Results_line = results_line.toString().split(",");

        String a = "0";
        String b = "0";

        Results[0] = Results[0].substring(1);
        Results_line[0] = Results_line[0].substring(1);

        if(Results.length > 2){
            Results[3] = Results[3].substring(0, Results[3].length()-1);
        }
        if(Results_line.length > 2){
            Results_line[3] = Results_line[3].substring(0, Results_line[3].length()-1);
        }

        if(Results[0].equals("good")){
            a = Results[1];
        }
        if(Results_line[0].equals("good")){
            b = Results_line[1];
        }

        if(Results.length > 2){
            if(Results[0].equals("bad")){
                a = Results[3];
            }
        }
        if(Results_line.length > 2){
            if(Results_line[0].equals("bad")){
                b = Results_line[3];
            }
        }

        bitmaps_result[5] = bitmap;
        percent_result[5] = 8*Double.parseDouble(a) + 2*Double.parseDouble(b);
    }
}
