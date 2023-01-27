package com.example.weather;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.example.weather.MainActivity.REQUEST_LOCATION;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.ThumbnailUtils;
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
import android.service.controls.templates.ThumbnailTemplate;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.*;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.view.LayoutInflater;
import android.view.View;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.graphics.Bitmap;

import com.example.weather.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;


public class ClothFragment extends Fragment {
    int imageSize = 224;
    private static final int GALLERY_REQUEST_CODE = 5;
    private View popupView;
    private boolean genderNotSelected = true;
    private List<Bitmap> images;
    private PhotoAdapter adapter;

    public static final int RESULT_OK = Activity.RESULT_OK;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 2;

    private boolean userIsMan=true;

    private static final int Image_Capture_Code = 1;

    public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.image_view);
            }
        }

        public PhotoAdapter() {

        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.imageView.setImageBitmap(images.get(position));
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        public void addImage(Bitmap bitmap) {
            images.add(bitmap);
            classifyImage(bitmap);
            notifyDataSetChanged();
        }
        public void showPopup() {

            // inflate the layout of the popup window
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            popupView = inflater.inflate(R.layout.popup_window, null);


            // create the popup window
            int width = LinearLayout.LayoutParams.MATCH_PARENT;
            int height = LinearLayout.LayoutParams.MATCH_PARENT;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            // show the popup window
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);


            Button closeButton = popupView.findViewById(R.id.close_button);
            //closeButton.setBackgroundResource(R.drawable.baseline_cancel_24);
            //closeButton.setBackgroundColor(Color.TRANSPARENT);
            popupView.setBackgroundColor(Color.argb(150, 0, 0, 0));
            closeButton.setOnClickListener(view -> {
                popupWindow.dismiss();

            }

            );

            RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new PhotoAdapter());
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
                        //images.add(image);
                        adapter.addImage(image);
                        adapter.showPopup();

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


        images = new ArrayList<>();
        adapter = new PhotoAdapter();


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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == GALLERY_REQUEST_CODE) {
            if(data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for(int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    adapter.addImage(bitmap);
                    //process the selected image and add it to the images list
                }
            } else if(data.getData() != null) {
                Uri imageUri = data.getData();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                adapter.addImage(bitmap);
                //process the selected image and add it to the images list
            }
            adapter.showPopup();
        }
    }

    private void showImageSelectionDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Select Image");
            builder.setItems(new CharSequence[]{"From Gallery", "Take Photo"},
                    (dialog, which) -> {
                        switch (which) {
                            case 0:
                                Intent intent = new Intent();
                                intent.setType("image/*");
                                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST_CODE);
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

    private void classifyImage(Bitmap image){
        int dimension = Math.min(image.getWidth(),image.getHeight());
        image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
        image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
        Log.i("AI","ai part started");
        try{
            Model model= Model.newInstance(getActivity().getApplicationContext());



            //creates inputs for reference
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1,224,224,3}, DataType.FLOAT32);


            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4*imageSize*imageSize*3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int [] intValues = new int[imageSize*imageSize];
            image.getPixels(intValues, 0 , image.getWidth(), 0,0,image.getWidth(),image.getHeight());
            int pixel = 0;
            for(int i=0;i<imageSize;i++){
                for(int j=0;j<imageSize;j++){
                    int val= intValues[pixel++]; //RGB
                    byteBuffer.putFloat(((val>>16)&0xFF)*(1.f/255.f));
                    byteBuffer.putFloat(((val>>8)&0xFF)*(1.f/255.f));
                    byteBuffer.putFloat((val&0xFF)*(1.f/255.f));
                }
            }
            //image.copyPixelsToBuffer(byteBuffer);
            //byteBuffer.rewind();


            inputFeature0.loadBuffer(byteBuffer);

            //runs model inference and gets the result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();

            int maxpos=0;
            float maxconfidence=0;
            for(int i=0; i<confidences.length;i++){
                if(confidences[i]>maxconfidence){
                    maxconfidence=confidences[i];
                    maxpos=i;
                }
            }
            String[] classes={"tshirt", "sweater", "jacket", "coat", "jeans", "shorts",
                    "shoes", "boots", "tanktop", "cap", "gloves", "scarf", "beanie", "skirt"};

            Log.i("AI","the uploaded photo:"+classes[maxpos]);

            TextView textView = (TextView) getView().findViewById(R.id.textView2);
            //changes the text to the prediction
            textView.setText("The Last uploaded photo: "+classes[maxpos]);

            //releases model if not used

            model.close();


        } catch (IOException e){
            //handle expection
        }
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

                //ai stuff

                //classifyImage(bitmap);

                if(userIsMan) {

                }
                else{

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
