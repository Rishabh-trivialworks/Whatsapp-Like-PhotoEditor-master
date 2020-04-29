package com.droidninja.imageeditengine;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.droidninja.imageeditengine.utils.FragmentUtil;
import com.droidninja.imageeditengine.utils.Utility;
import com.droidninja.imageeditengine.views.VerticalSlideColorPicker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MediaGalleryActivity extends AppCompatActivity implements PhotoEditorFragment.OnFragmentInteractionListener,
        CropFragment.OnFragmentInteractionListener, View.OnClickListener {

    private LockViewpager mPager;
    private GalleryMediaSlider mPagerAdapter;
    public static List<String> postMediaFiles = new ArrayList<>();
    public static final int MY_PERMISSIONS_REQUEST = 101;
    private Activity activity;
    private Rect cropRect;
    ImageView cropButton;
    ImageView stickerButton;
    ImageView addTextButton;
    ImageView paintButton;
    View toolbarLayout;
    FloatingActionButton doneBtn;
    protected int currentMode;
    VerticalSlideColorPicker colorPickerView;

    public static final int MODE_NONE = 0;
    public static final int MODE_PAINT = 1;
    public static final int MODE_ADD_TEXT = 2;
    public static final int MODE_STICKER = 3;
    private PhotoEditorFragment.OnFragmentInteractionListener mListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_pager);

        postMediaFiles = getIntent().getExtras().getStringArrayList(ImageEditor.EXTRA_IMAGE_PATH);

        Log.e("origionl", postMediaFiles.toString());

        activity = this;
        mPager = (LockViewpager) findViewById(R.id.pager);
        mPagerAdapter = new GalleryMediaSlider(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOffscreenPageLimit((postMediaFiles.size()));

        mPager.setCurrentItem(0);
        onPageSelected(0);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                MediaGalleryActivity.this.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        if (activity instanceof PhotoEditorFragment.OnFragmentInteractionListener) {
            mListener = (PhotoEditorFragment.OnFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(
                    activity.toString() + " must implement OnFragmentInteractionListener");
        }
        cropButton = findViewById(R.id.crop_btn);
        stickerButton = findViewById(R.id.stickers_btn);
        addTextButton = findViewById(R.id.add_text_btn);
        doneBtn = findViewById(R.id.done_btn);
        paintButton = findViewById(R.id.paint_btn);
        colorPickerView = findViewById(R.id.color_picker_view);

        toolbarLayout = findViewById(R.id.toolbar_layout);

        findViewById(R.id.back_iv).setOnClickListener(this);
        cropButton.setOnClickListener(this);
        stickerButton.setOnClickListener(this);
        addTextButton.setOnClickListener(this);
        paintButton.setOnClickListener(this);
        doneBtn.setOnClickListener(this);
        colorPickerView.setOnColorChangeListener(
                new VerticalSlideColorPicker.OnColorChangeListener() {
                    @Override
                    public void onColorChange(int selectedColor) {
                        PhotoEditorFragment selectedMediaGalleryFragment = mPagerAdapter.mediaGalleryFragments.get(mPager.getCurrentItem());
                        if (currentMode == MODE_PAINT) {
                            paintButton.setBackground(
                                    Utility.tintDrawable(activity, R.drawable.circle, selectedColor));
                            selectedMediaGalleryFragment.setColor(selectedColor);
                        } else if (currentMode == MODE_ADD_TEXT) {
                            addTextButton.setBackground(
                                    Utility.tintDrawable(activity, R.drawable.circle, selectedColor));
                            selectedMediaGalleryFragment.setTextColor(selectedColor);
                        }
                    }
                });
//        PhotoEditorFragment selectedMediaGalleryFragment = mPagerAdapter.mediaGalleryFragments.get(mPager.getCurrentItem());
//        if (selectedMediaGalleryFragment != null) {
//            selectedMediaGalleryFragment.setColor(colorPickerView.getDefaultColor());
//            selectedMediaGalleryFragment.setTextColor(colorPickerView.getDefaultColor());
//        }
        if (!checkPermissions(true, false)) {
            return;
        }

        Intent intent = activity.getIntent();
        setVisibility(addTextButton, intent.getBooleanExtra(ImageEditor.EXTRA_IS_TEXT_MODE, false));
        setVisibility(cropButton, intent.getBooleanExtra(ImageEditor.EXTRA_IS_CROP_MODE, false));
        setVisibility(stickerButton, intent.getBooleanExtra(ImageEditor.EXTRA_IS_STICKER_MODE, false));
        setVisibility(paintButton, intent.getBooleanExtra(ImageEditor.EXTRA_IS_PAINT_MODE, false));
    }

    protected void setVisibility(View view, boolean visible) {
        if (visible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

        }

    }

    private boolean checkPermissions(boolean checkStorage, boolean checkAudio) {
        ArrayList<String> arrPerm = new ArrayList<>();
        if (checkAudio && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(android.Manifest.permission.RECORD_AUDIO);
        }

        if (checkStorage && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!arrPerm.isEmpty()) {
            String[] permissions = new String[arrPerm.size()];
            permissions = arrPerm.toArray(permissions);


            ActivityCompat.requestPermissions(activity, permissions, MY_PERMISSIONS_REQUEST);
            return false;
        }
        return true;
    }


    private void onPageSelected(final int position) {

        try {
            PhotoEditorFragment selectedMediaGalleryFragment = mPagerAdapter.mediaGalleryFragments.get(position);
            for (PhotoEditorFragment mediaGalleryFragment : mPagerAdapter.mediaGalleryFragments) {
                if (mediaGalleryFragment != null) {
                    if (selectedMediaGalleryFragment.equals(mediaGalleryFragment)) {
                        //selectedMediaGalleryFragment.onSelected(position);
                    } else {
                        //ediaGalleryFragment.onPageChange(position);
                    }
                }
            }
            mPagerAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCropClicked(Bitmap bitmap) {
        doneBtn.setVisibility(View.GONE);
        FragmentUtil.replaceFragment(this, R.id.fragment_container,
                CropFragment.newInstance(bitmap, cropRect));
    }

    @Override
    public void onDoneClicked(ArrayList<String> imagePath) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(ImageEditor.EXTRA_IMAGE_PATH, imagePath);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onImageCropped(Bitmap bitmap, Rect cropRect) {
        doneBtn.setVisibility(View.VISIBLE);
        this.cropRect = cropRect;
        PhotoEditorFragment selectedMediaGalleryFragment = mPagerAdapter.mediaGalleryFragments.get(mPager.getCurrentItem());
        if (selectedMediaGalleryFragment != null) {
            selectedMediaGalleryFragment.setImageWithRect(cropRect);
            // photoEditorFragment.setImageBitmap(bitmap);
            //selectedMediaGalleryFragment.reset();

            FragmentUtil.removeFragment(this,
                    (BaseFragment) FragmentUtil.getFragmentByTag(this, CropFragment.class.getSimpleName()));
        }
    }

    @Override
    public void onCancelCrop() {
        doneBtn.setVisibility(View.VISIBLE);
        FragmentUtil.removeFragment(this,
                (BaseFragment) FragmentUtil.getFragmentByTag(this, CropFragment.class.getSimpleName()));
    }

    @Override
    public void onBackPressed() {
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

    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyFiles");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        // Create a media file name
        File mediaFile;
        String mImageName = "MI_" + System.currentTimeMillis() + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }


    private class GalleryMediaSlider extends FragmentStatePagerAdapter {
        public List<PhotoEditorFragment> mediaGalleryFragments;

        public GalleryMediaSlider(FragmentManager fm) {
            super(fm);
            mediaGalleryFragments = new ArrayList<>(postMediaFiles.size());
            for (int size = 0; size < postMediaFiles.size(); size++) {
                mediaGalleryFragments.add(size, null);
            }
        }

        @Override
        public Fragment getItem(int position) {
            PhotoEditorFragment galleryMediaFragment = new PhotoEditorFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(ImageEditor.EXTRA_IMAGE_PATH, postMediaFiles.get(position));
            galleryMediaFragment.setArguments(bundle);
            mediaGalleryFragments.set(position, galleryMediaFragment);
            return galleryMediaFragment;
        }

        @Override
        public int getCount() {
            return postMediaFiles.size();
        }


    }

    public interface OnPageChangeListener {
        // Called when ViewPager's selected position is not yours
        void onPageChange(int position);

        // Called when your position is selected in ViewPager
        void onSelected(int position);
    }


    @Override
    public void onClick(final View view) {
        int id = view.getId();
        if (id == R.id.crop_btn) {
            PhotoEditorFragment selectedMediaGalleryFragment = mPagerAdapter.mediaGalleryFragments.get(mPager.getCurrentItem());
            mListener.onCropClicked(selectedMediaGalleryFragment.getBitmapCache(selectedMediaGalleryFragment.getOriginalBitmap()));
            selectedMediaGalleryFragment.hidePaintView();
        } else if (id == R.id.stickers_btn) {
            setMode(MODE_STICKER);
        } else if (id == R.id.add_text_btn) {
            setMode(MODE_ADD_TEXT);
        } else if (id == R.id.paint_btn) {
            setMode(MODE_PAINT);
        } else if (id == R.id.back_iv) {
            onBackPressed();
        } else if (id == R.id.done_btn) {
            final ArrayList<String> finalString = new ArrayList<>();
            for (int i = 0; i < postMediaFiles.size(); i++) {
                PhotoEditorFragment selectedMediaGalleryFragment = mPagerAdapter.mediaGalleryFragments.get(i);
                finalString.add(storeImage(selectedMediaGalleryFragment.getBitmapCache(selectedMediaGalleryFragment.getChangedBitmap())).getAbsolutePath());
//                new ProcessingImage(selectedMediaGalleryFragment.getBitmapCache(selectedMediaGalleryFragment.getChangedBitmap()), Utility.getCacheFilePath(view.getContext()),
//                        new TaskCallback<String>() {
//                            @Override
//                            public void onTaskDone(String data) {
//                                //mListener.onDoneClicked(data);
//                                finalString.add(data);
//                                Log.d("Strins",finalString.toString());
//                            }
//                        }).execute();

            }
            Log.d("Strins", finalString.toString());

            mListener.onDoneClicked(finalString);

        }

    }


    protected void setMode(int mode) {
        if (currentMode != mode) {
            onModeChanged(mode);
        } else {
            mode = MODE_NONE;
            onModeChanged(mode);
        }
        this.currentMode = mode;
    }

    private void onAddTextMode(boolean status) {
        PhotoEditorFragment selectedMediaGalleryFragment = mPagerAdapter.mediaGalleryFragments.get(mPager.getCurrentItem());
        if (status) {
            mPager.setSwipeable(false);
            selectedMediaGalleryFragment.eraseLine(false);
            //selectedMediaGalleryFragment.setColor(ContextCompat.getColor(activity, R.color.checkbox_color));
            selectedMediaGalleryFragment.setTextColor(ContextCompat.getColor(activity, R.color.checkbox_color));
            addTextButton.setBackground(
                    Utility.tintDrawable(activity, R.drawable.circle, selectedMediaGalleryFragment.getTextColor()));
            //photoEditorView.setTextColor(photoEditorView.getColor());
            selectedMediaGalleryFragment.addtext();
        } else {
            mPager.setSwipeable(true);
            addTextButton.setBackground(null);
            //selectedMediaGalleryFragment.setTextColor(0);
            selectedMediaGalleryFragment.hideTextMode();
        }
    }

    private void onPaintMode(boolean status) {
        PhotoEditorFragment selectedMediaGalleryFragment = mPagerAdapter.mediaGalleryFragments.get(mPager.getCurrentItem());
        if (status) {
            mPager.setSwipeable(false);
            selectedMediaGalleryFragment.eraseLine(false);
            selectedMediaGalleryFragment.setColor(ContextCompat.getColor(activity, R.color.checkbox_color));
            paintButton.setBackground(
                    Utility.tintDrawable(activity, R.drawable.circle, selectedMediaGalleryFragment.getColor()));
            selectedMediaGalleryFragment.showPaintView();
            //paintEditView.setVisibility(View.VISIBLE);
        } else {
            mPager.setSwipeable(true);
            paintButton.setBackground(null);
            selectedMediaGalleryFragment.hidePaintView();
            selectedMediaGalleryFragment.setColor(0);

            //photoEditorView.enableTouch(true);
            //paintEditView.setVisibility(View.GONE);
        }
    }

    private void onStickerMode(boolean status) {
        PhotoEditorFragment selectedMediaGalleryFragment = mPagerAdapter.mediaGalleryFragments.get(mPager.getCurrentItem());
        if (status) {
            stickerButton.setBackground(
                    Utility.tintDrawable(activity, R.drawable.circle, selectedMediaGalleryFragment.getColor()));
            if (activity != null && activity.getIntent() != null) {
                String folderName = activity.getIntent().getStringExtra(ImageEditor.EXTRA_STICKER_FOLDER_NAME);
                // photoEditorView.showStickers(folderName);
            }
            selectedMediaGalleryFragment.eraseLine(true);
        } else {
            selectedMediaGalleryFragment.eraseLine(false);
            stickerButton.setBackground(null);
            // photoEditorView.hideStickers();
        }
    }

    protected void onModeChanged(int currentMode) {
        Log.i(ImageEditActivity.class.getSimpleName(), "CM: " + currentMode);
        if (currentMode == MODE_STICKER) {
            onAddTextMode(currentMode == MODE_ADD_TEXT);
            onPaintMode(currentMode == MODE_PAINT);
            onStickerMode(currentMode == MODE_STICKER);

        } else if (currentMode == MODE_ADD_TEXT) {
            onPaintMode(currentMode == MODE_PAINT);
            onStickerMode(currentMode == MODE_STICKER);
            onAddTextMode(currentMode == MODE_ADD_TEXT);
        } else if (currentMode == MODE_PAINT) {
            onStickerMode(currentMode == MODE_STICKER);
            onAddTextMode(currentMode == MODE_ADD_TEXT);
            onPaintMode(currentMode == MODE_PAINT);

        } else {
            onStickerMode(currentMode == MODE_STICKER);
            onAddTextMode(currentMode == MODE_ADD_TEXT);
            onPaintMode(currentMode == MODE_PAINT);
        }


        if (currentMode == MODE_PAINT || currentMode == MODE_ADD_TEXT) {
            AnimationHelper.animate(activity, colorPickerView, R.anim.slide_in_right, View.VISIBLE,
                    null);
        } else {
            AnimationHelper.animate(activity, colorPickerView, R.anim.slide_out_right, View.INVISIBLE,
                    null);
        }
    }

    public interface OnFragmentInteractionListener {
        void onCropClicked(Bitmap bitmap);

        void onDoneClicked(ArrayList<String> imagePath);
    }
}
