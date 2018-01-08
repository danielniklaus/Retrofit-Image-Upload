package com.steemit.daniel.retrofitimageupload.utils;

import com.steemit.daniel.retrofitimageupload.response.UploadResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by daniel on 1/6/2018.
 */

public interface RequestInterface {

    @Multipart
    @POST("upload")
    Call<UploadResponse> postImage(@Part MultipartBody.Part image, @Part("name") RequestBody name);
}
