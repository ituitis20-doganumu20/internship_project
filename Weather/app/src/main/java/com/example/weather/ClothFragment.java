package com.example.weather;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import androidx.lifecycle.*;


public class ClothFragment extends Fragment {

    class MyLifecycleObserver implements DefaultLifecycleObserver {
        private final ActivityResultRegistry mRegistry;
        private ActivityResultLauncher<String> mGetContent;

        MyLifecycleObserver(@NonNull ActivityResultRegistry registry) {
            mRegistry = registry;
        }

        public void onCreate(@NonNull LifecycleOwner owner) {
            // ...
            String umut="umut";
            mGetContent = mRegistry.register(umut, owner, new ActivityResultContracts.GetContent(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri uri) {
                            // Handle the returned Uri
                            Log.i("image", "image arrived");
                            //there will be returned image
                        }
                    });
        }

        public void selectImage() {
            // Open the activity to select an image
            mGetContent.launch("image/*");
        }
    }
    private MyLifecycleObserver mObserver;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mObserver = new MyLifecycleObserver(requireActivity().getActivityResultRegistry());
        getLifecycle().addObserver(mObserver);
    }


    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_cloth, container, false);
        Button button = view.findViewById(R.id.upload_image_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageSelectionDialog();
            }
        });

        return view;
    }
    private void showImageSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Image");
        builder.setItems(new CharSequence[]{"From Gallery", "Take Photo"},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                mObserver.selectImage();
                                break;
                            case 1:
                                //openCamera();
                                break;
                        }
                    }
                });
        builder.create().show();
    }



}