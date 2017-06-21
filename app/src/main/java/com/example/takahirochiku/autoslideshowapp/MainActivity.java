package com.example.takahirochiku.autoslideshowapp;

import android.os.Handler;
import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.takahirochiku.autoslideshowapp.R.id.back_button;
import static com.example.takahirochiku.autoslideshowapp.R.id.forward_button;
import static com.example.takahirochiku.autoslideshowapp.R.id.switch_button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Timer mTimer;
    ImageView imageView;
    Button mForwardButton;
    Button mBackButton;
    Button mSwitchButton;
    Uri imageUri;
    int num = 0;
    Uri photo1;
    Uri photo2;

    ArrayList<Uri> imageList = new ArrayList<Uri>();

    Handler mHandler = new Handler();
    double mTimerSec = 0.0;

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);

        mForwardButton = (Button) findViewById(forward_button);
        mForwardButton.setOnClickListener(this);

        mBackButton = (Button) findViewById(back_button);
        mBackButton.setOnClickListener(this);

        mSwitchButton = (Button) findViewById(switch_button);
        mSwitchButton.setOnClickListener(this);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
        } else {
            // Android 5系以下の場合
            getContentsInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (cursor.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                //先頭レコードの_IDの値を取得
                Long id = cursor.getLong(fieldIndex);
                //先頭レコードの_IDからURIを作成
                imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                imageList.add(imageUri);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.forward_button) {
            photo1 = imageList.get(num++);
                imageView.setImageURI(photo1);
            } else if (v.getId() == R.id.back_button) {
                photo2 = imageList.get(num--);
                imageView.setImageURI(photo2);
            } else if (v.getId() == R.id.switch_button) {
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                } else {
                    mTimer = new Timer();
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mTimerSec += 2.0;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    imageView.setImageURI(imageUri);
                                }
                            });
                        }
                    }, 200, 200);
                }
            }
        }
    }
