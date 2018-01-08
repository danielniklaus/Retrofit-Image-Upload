package com.steemit.daniel.retrofitimageupload.activity;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.steemit.daniel.retrofitimageupload.R;
import com.steemit.daniel.retrofitimageupload.response.UploadResponse;
import com.steemit.daniel.retrofitimageupload.utils.RequestInterface;
import com.steemit.daniel.retrofitimageupload.utils.RetrofitClientUtil;

import java.io.File;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 100;

    @BindView(R.id.parent_layout)
    protected RelativeLayout mParent;

    @BindView(R.id.img_thumb)
    protected AppCompatImageView mImageThumb;


    @BindView(R.id.rl_tambah_gambar)
    protected RelativeLayout mAddImage;

    @BindView(R.id.btn_upload)
    protected AppCompatButton mBtnUpload;


    private Unbinder mUnbinder;
    private String selectImagePath;
    private Snackbar mSnackbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUnbinder = ButterKnife.bind(this);
        mAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
            }
        });

        mBtnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });


    }

    private void uploadImage() {

        File file = new File(selectImagePath);
        RequestBody reqFile = RequestBody.create(MediaType.parse("image"), file);
        MultipartBody.Part imageBody = MultipartBody.Part.createFormData("image", file.getName(), reqFile);
        RequestBody ImageName = RequestBody.create(MediaType.parse("text/plain"), file.getName());
        RequestInterface request = RetrofitClientUtil.getRequestInterface();
        Call<UploadResponse> responseCall = request.postImage(imageBody, ImageName);
        responseCall.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                if (response.isSuccessful()){
                    UploadResponse resp = response.body();
                    if (resp.getCode() == 200){
                        mSnackbar = Snackbar.make(mParent, resp.getMessage(), Snackbar.LENGTH_LONG);
                        View views = mSnackbar.getView();
                        views.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
                        mSnackbar.show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                mSnackbar = Snackbar.make(mParent, t.getLocalizedMessage(), Snackbar.LENGTH_LONG);
                View views = mSnackbar.getView();
                views.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorWarmGrey));
                mSnackbar.show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri selectImageUri = data.getData();
            selectImagePath = getRealPathFromURI(selectImageUri);
            decodeImage(selectImagePath);
        }
    }

    private String getRealPathFromURI(Uri selectImageUri) {
        Cursor cursor = getContentResolver().query(selectImageUri, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            return selectImageUri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    private void decodeImage(String selectImagePath) {
        int targetW = mImageThumb.getWidth();
        int targetH = mImageThumb.getHeight();

        final BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectImagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        Bitmap bitmap = BitmapFactory.decodeFile(selectImagePath, bmOptions);
        if (bitmap != null) {
            mImageThumb.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }
}
