package com.idontneedaname.ondevicetextrecognize;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class MainActivity extends Base {

    private Button mButton;
    private Button button_cam;
    private CameraView cameraView;
   // private TextToSpeech textToSpeech;
    private ImageView imageView;
    private EditText textView;
    private int x=0;
    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        imageView=findViewById(R.id.image_id);
        button_cam = findViewById(R.id.button_cam);
        mButton = findViewById(R.id.button_text);
        cameraView=findViewById(R.id.camera_view);
        textView=findViewById(R.id.text_detect);
        cameraView.addCameraListener(new CameraListener() {
    @Override
    public void onPictureTaken(byte[] jpeg) {
        super.onPictureTaken(jpeg);
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(jpeg);
        myBitmap = BitmapFactory.decodeStream(arrayInputStream);
        runTextRecognition();
    }

});

//        Observable.interval(3000,TimeUnit.MILLISECONDS).subscribe(new Consumer<Long>() {
//            @SuppressLint("CheckResult")
//            @Override
//            public void accept(Long aLong) throws Exception {
//                cameraView.captureSnapshot();
//            }
//        });

//        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
//            @Override
//            public void onInit(int status) {
//                if (status == TextToSpeech.SUCCESS) {
//                    int ttsLang = textToSpeech.setLanguage(Locale.getDefault());
//
//                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
//                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
//                        Log.e("TTS", "The Language is not supported!");
//                    } else {
//                        Log.i("TTS", "Language Supported.");
//                    }
//                    Log.i("TTS", "Initialization success.");
//                } else {
//                    Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_cam.setVisibility(View.GONE);

                cameraView.captureSnapshot();
//                if (myBitmap != null) {
//                    runTextRecognition();
//                } else {
//                    showToast("Choose a proper image");
//                }

            }
        });
        button_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mButton.setVisibility(View.VISIBLE);
                cameraView.captureSnapshot();
              //  startActivityForResult(getPickImageChooserIntent(), 200);
            }
        });

    }

    private void runTextRecognition() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(myBitmap);
//        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
//                .getCloudTextRecognizer();
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        mButton.setEnabled(false);
        Task<FirebaseVisionText> result = detector.processImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText texts) {
                                mButton.setEnabled(true);
                                processTextRecognitionResult(texts);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                mButton.setEnabled(true);
                                e.printStackTrace();
                            }
                        });
        if(myBitmap!=null)
        {
            imageView.setImageBitmap(myBitmap);
        }
    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {

        StringBuilder t = new StringBuilder();

        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            showToast("No text found");
            return;
        }

        for (int i = 0; i < blocks.size(); i++) {
            t.append(" ").append(blocks.get(i).getText());
        }
        String numberOnly= t.toString().replaceAll("[^0-9]", "");

        showToast(numberOnly);
        textView.setText(numberOnly);
        textView.setVisibility(View.VISIBLE);
       // textToSpeech.speak(t.toString(), TextToSpeech.QUEUE_FLUSH, null);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
    }
}
