package com.example.weather;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.media.ThumbnailUtils;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.example.weather.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;


public class ClothFragment extends Fragment {


    //TextView predictionTextView;
    int imageSize = 224;
    private static final int GALLERY_REQUEST_CODE = 5;
    private View popupView;
    private boolean genderNotSelected = true;
    //private List<Bitmap> images;
    private PhotoAdapter image_adapter ;

    public static final int RESULT_OK = Activity.RESULT_OK;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 2;

    private boolean userIsMan=true;

    private static final int Image_Capture_Code = 1;
    private HashMap<String, ArrayList<Bitmap>> classesMap = new HashMap<>();


    private final String[] classNames = {"tshirt", "sweater", "jacket", "coat", "jeans", "shorts",
            "shoes", "boots", "tanktop", "cap", "gloves", "scarf", "beanie", "skirt"};


    public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {


        int prediction_index=0;
        private List<String> predictions =new ArrayList<>();
        List<Bitmap> images= new ArrayList<>();
        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView predictionTextView;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.image_view);
                predictionTextView = itemView.findViewById(R.id.prediction_text_view);

            }
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
            holder.predictionTextView.setText(predictions.get(position));
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        public void addImage(Bitmap bitmap) {
            images.add(bitmap);
            classifyImage(bitmap,this);

            //notifyDataSetChanged();
        }

    }

    public void showPopup(PhotoAdapter adapter) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.popup_window, null);


        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        //popupWindow.setAnimationStyle(android.R.anim.fade_in);
        // show the popup window
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);


        Button closeButton = popupView.findViewById(R.id.close_button);
        closeButton.setVisibility(View.VISIBLE);
        //closeButton.setBackgroundResource(R.drawable.baseline_cancel_24);
        //closeButton.setBackgroundColor(Color.TRANSPARENT);
        //popupView.setBackgroundColor(Color.argb(150, 0, 0, 0));
        closeButton.setOnClickListener(view -> {
                    //popupWindow.setAnimationStyle(android.R.anim.fade_out);
                    popupWindow.dismiss();
                    //closeButton.setVisibility(View.GONE);

                }

        );

        RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }




    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_cloth, container, false);
        Button imagebutton = view.findViewById(R.id.images_button);

        image_adapter = new PhotoAdapter();


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


        imagebutton.setOnClickListener(view1 -> {
            if(image_adapter.images.isEmpty())
                Toast.makeText(getContext(),"Please upload some image first", Toast.LENGTH_SHORT).show();
            else
                showPopup(image_adapter);
        });

        Button recommendation_button=view.findViewById(R.id.recommendation_button);
        recommendation_button.setOnClickListener(view1 -> {
            if(image_adapter.images.isEmpty())
                Toast.makeText(getContext(),"Please upload some image first", Toast.LENGTH_SHORT).show();
            else
                aiRecommendation(image_adapter);
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

    private Uri imageUri;

    private void openCamera() {
        Intent cInt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String fileName = "Pic.jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        imageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        cInt.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cInt,Image_Capture_Code);
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
                    image_adapter.addImage(bitmap);
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
                image_adapter.addImage(bitmap);
                //process the selected image and add it to the images list
            }
            showPopup(image_adapter);
        }
        if (resultCode == RESULT_OK && requestCode == Image_Capture_Code) {
            Uri selectedImage = imageUri;
            getActivity().getContentResolver().notifyChange(selectedImage, null);
            ContentResolver cr = getActivity().getContentResolver();
            Bitmap bitmap;
            try {
                bitmap = android.provider.MediaStore.Images.Media
                        .getBitmap(cr, selectedImage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            image_adapter.addImage(bitmap);
            showPopup(image_adapter);
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

    private void classifyImage(Bitmap image, PhotoAdapter adapter){
        Bitmap original_image=image;
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

            Log.i("AI","the uploaded photo:"+classNames[maxpos]);

            adapter.predictions.add(classNames[maxpos]);
            adapter.prediction_index++;

            addToList(original_image,classNames[maxpos]);

            //TextView textView = getView().findViewById(R.id.textView2);




            //releases model if not used

            model.close();


        } catch (IOException e){
            //handle expection
        }
    }


    public void addToList(Bitmap bitmap, String className) {
        if (!classesMap.containsKey(className)) {
            classesMap.put(className, new ArrayList<Bitmap>());
        }
        classesMap.get(className).add(bitmap);
    }

    public ArrayList<Bitmap> getArray(String className) {
        return classesMap.get(className);
    }

    private void getrandomcloth(String str, PhotoAdapter adapter){
        List<Bitmap> imagelist = getArray(str);
        if(imagelist !=null){
            Random random = new Random();
            Bitmap image = imagelist.get(random.nextInt(imagelist.size()));
            adapter.images.add(image);
            adapter.predictions.add(str);
            adapter.prediction_index++;
        }
    }
    private void aiRecommendation(PhotoAdapter adapter){
        double temperature = getArguments().getDouble("temperature")-273;
        Log.i("temp", "temperature is: "+temperature);
        if(adapter.images.isEmpty()){
            //handle empty list
            Toast.makeText(getContext(),"Please upload some images first", Toast.LENGTH_SHORT).show();
        }else{
            //ai stuff

            PhotoAdapter recommendedAdapter = new PhotoAdapter();


                if(temperature<10){
                    getrandomcloth("gloves",recommendedAdapter);
                    getrandomcloth("scarf",recommendedAdapter);
                    getrandomcloth("boots",recommendedAdapter);
                    getrandomcloth("coat",recommendedAdapter);
                    getrandomcloth("beanie",recommendedAdapter);
                    getrandomcloth("jeans",recommendedAdapter);

                }else if(temperature<20){
                    getrandomcloth("sweater",recommendedAdapter);
                    getrandomcloth("beanie",recommendedAdapter);
                    getrandomcloth("shoes",recommendedAdapter);
                    getrandomcloth("jacket",recommendedAdapter);
                    getrandomcloth("jeans",recommendedAdapter);

                } else if(temperature<30){
                    getrandomcloth("tshirt",recommendedAdapter);
                    getrandomcloth("shoes",recommendedAdapter);
                    getrandomcloth("jeans",recommendedAdapter);
                } else {
                    if(userIsMan) {
                        getrandomcloth("tanktop",recommendedAdapter);
                        getrandomcloth("shoes",recommendedAdapter);
                        getrandomcloth("shorts",recommendedAdapter);
                    }
                     else{
                        getrandomcloth("tshirt",recommendedAdapter);
                        getrandomcloth("tanktop",recommendedAdapter);
                        getrandomcloth("shoes",recommendedAdapter);
                        getrandomcloth("skirt",recommendedAdapter);
                    }

                }


            showPopup(recommendedAdapter);
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
