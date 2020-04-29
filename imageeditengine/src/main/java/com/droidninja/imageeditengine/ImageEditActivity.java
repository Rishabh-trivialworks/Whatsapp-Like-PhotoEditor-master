package com.droidninja.imageeditengine;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import com.droidninja.imageeditengine.utils.FragmentUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static com.droidninja.imageeditengine.ImageEditor.EXTRA_IMAGE_PATH;

public class ImageEditActivity extends BaseImageEditActivity
    implements PhotoEditorFragment.OnFragmentInteractionListener,
    CropFragment.OnFragmentInteractionListener {
  private Rect cropRect;
  String imagePath;
  //private View touchView;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image_edit);

     imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
    if (imagePath != null) {
      FragmentUtil.addFragment(this, R.id.fragment_container,
          PhotoEditorFragment.newInstance(imagePath));
    }
  }

  @Override public void onCropClicked(Bitmap bitmap) {
    FragmentUtil.replaceFragment(this, R.id.fragment_container,
        CropFragment.newInstance(bitmap, cropRect));
  }

  @Override public void onDoneClicked(ArrayList<String> imagePath) {

    Intent intent = new Intent();
    intent.putExtra(ImageEditor.EXTRA_EDITED_PATH, imagePath);
    setResult(Activity.RESULT_OK, intent);
    finish();
  }

  @Override public void onImageCropped(Bitmap bitmap, Rect cropRect) {
    this.cropRect = cropRect;
    PhotoEditorFragment photoEditorFragment =
        (PhotoEditorFragment) FragmentUtil.getFragmentByTag(this,
            PhotoEditorFragment.class.getSimpleName());
    if (photoEditorFragment != null) {
      photoEditorFragment.setImageWithRect(cropRect);
     // photoEditorFragment.setImageBitmap(bitmap);
      photoEditorFragment.reset();
      FragmentUtil.removeFragment(this,
          (BaseFragment) FragmentUtil.getFragmentByTag(this, CropFragment.class.getSimpleName()));
    FragmentUtil.addFragment(this, R.id.fragment_container,
            PhotoEditorFragment.newInstance(storeImage(bitmap).getPath()));
    }
  }

  @Override public void onCancelCrop() {
//    FragmentUtil.replaceFragment(this, R.id.fragment_container,
//            PhotoEditorFragment.newInstance(imagePath));
        FragmentUtil.removeFragment(this,
        (BaseFragment) FragmentUtil.getFragmentByTag(this, CropFragment.class.getSimpleName()));
  }

  @Override public void onBackPressed() {
    super.onBackPressed();
  }

  private File storeImage(Bitmap image) {
    File pictureFile = getOutputMediaFile();
    if (pictureFile == null) {

      return null;
    }
    try {
      FileOutputStream fos = new FileOutputStream(pictureFile);
      image.compress(Bitmap.CompressFormat.PNG, 90, fos);
      fos.close();
      fos.flush();
    } catch (FileNotFoundException e) {
      // Log.d(TAG, "File not found: " + e.getMessage());
    } catch (IOException e) {
      // Log.d(TAG, "Error accessing file: " + e.getMessage());
    }
    return pictureFile;
  }

  private  File getOutputMediaFile(){
    // To be safe, you should check that the SDCard is mounted
    // using Environment.getExternalStorageState() before doing this.
    File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyFiles");

    // This location works best if you want the created images to be shared
    // between applications and persist after your app has been uninstalled.

    // Create the storage directory if it does not exist
    if (! mediaStorageDir.exists()){
      if (! mediaStorageDir.mkdirs()){
        return null;
      }
    }
    // Create a media file name
    File mediaFile;
    String mImageName="MI_"+System.currentTimeMillis()+".jpg";
    mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
    return mediaFile;
  }
}
