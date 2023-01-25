package com.example.weather;

import static com.example.weather.MainActivity.REQUEST_LOCATION;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.lifecycle.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ClothFragment extends Fragment {

    private boolean genderNotSelected = true;
    private List<Bitmap> images = new ArrayList<>();
    public static final int RESULT_OK = Activity.RESULT_OK;
    public static final int RESULT_CANCELED = Activity.RESULT_CANCELED;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 2;

    private boolean userIsMan=true;

    private static final int Image_Capture_Code = 1;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Image_Capture_Code) {
            if (resultCode == RESULT_OK) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                images.add(image);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent cInt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cInt,Image_Capture_Code);
    }


    private ActivityResultLauncher<String> mGetContent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityResultRegistry registry = requireActivity().getActivityResultRegistry();
        mGetContent = registry.register("imageSelection", this, new ActivityResultContracts.GetContent(),
                uri -> {
                    // Handle the returned Uri
                    Log.i("image", "image arrived");
                    //there will be returned image
                    try {
                        Bitmap image = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                        images.add(image);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_cloth, container, false);
        Button button = view.findViewById(R.id.upload_image_button);
        button.setOnClickListener(view -> {
            if(getArguments()==null)
                Toast.makeText(getContext(),"Please get weather information first", Toast.LENGTH_SHORT).show();
            else {
                if (genderNotSelected) {
                    showGenderSelection();
                    genderNotSelected = false;
                } else
                    showImageSelectionDialog();
            }
        });

        return view;
    }

    private void showGenderSelection(){

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Select Gender");
            builder.setItems(new CharSequence[]{"Man", "Woman"}, (dialog, which) -> {
                switch (which) {
                    case 0:
                        userIsMan = true;
                        break;
                    case 1:
                        userIsMan = false;
                        break;
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setOnDismissListener(d -> showImageSelectionDialog());
            dialog.show();


    }
    private void showImageSelectionDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Select Image");
            builder.setItems(new CharSequence[]{"From Gallery", "Take Photo"},
                    (dialog, which) -> {
                        switch (which) {
                            case 0:
                                mGetContent.launch("image/*");
                                break;
                            case 1:
                                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                                else
                                    openCamera();
                                break;
                        }
                    });
            builder.create().show();


    }

    private void aiRecommendation(List<Bitmap> images){
        double temperature = getArguments().getDouble("temperature")-273;
        Log.i("temp", "temperature is: "+temperature);
        if(images.isEmpty()){
            //handle empty list
            Toast.makeText(getContext(),"Please upload some images first", Toast.LENGTH_SHORT).show();
        }else{
            for(int i=0;i<images.size();i++){
                Bitmap bitmap = images.get(i);
                ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
                bitmap.copyPixelsToBuffer(byteBuffer);
                byteBuffer.rewind();
                if(userIsMan) {
                    //create man-ai
                }
                else{
                    //create woman-ai
                }
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==CAMERA_PERMISSION_REQUEST_CODE){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                openCamera();
            }else{
                Toast.makeText(getContext(),"cannot recommend without camera permission.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
