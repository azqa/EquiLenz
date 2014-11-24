package com.example.equilenz;

import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


public class MainActivity extends Activity {

	private static final int ACTION_TAKE_PHOTO_S = 1;
	//keep track of cropping intent
	final int PIC_CROP = 2;

	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
	private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
	private ImageView mImageView;
	private Bitmap mImageBitmap;
	//captured picture uri
	private Uri picUri;

	private void dispatchTakePictureIntent(int actionCode) {

		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		

		startActivityForResult(takePictureIntent, actionCode);
	}

	

	private void handleSmallCameraPhoto(Intent intent) {
		Bundle extras = intent.getExtras();
		mImageBitmap = (Bitmap) extras.get("data");
		mImageView.setImageBitmap(mImageBitmap);
		mImageView.setVisibility(View.VISIBLE);
	}



	Button.OnClickListener mTakePicSOnClickListener = 
		new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			dispatchTakePictureIntent(ACTION_TAKE_PHOTO_S);
		}
	};

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mImageView = (ImageView) findViewById(R.id.imgPreview);
		mImageBitmap = null;

		Button picSBtn = (Button) findViewById(R.id.btnCapturePicture);
		setBtnListenerOrDisable( 
				picSBtn, 
				mTakePicSOnClickListener,
				MediaStore.ACTION_IMAGE_CAPTURE
		);

		
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			new FroyoAlbumDirFactory();
		} else {
			new BaseAlbumDirFactory();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		
		case ACTION_TAKE_PHOTO_S: {
			if (resultCode == RESULT_OK) {
				//handleSmallCameraPhoto(data);
				picUri = data.getData();
    			//carry out the crop operation
    			performCrop();
			}
			break;
		} // ACTION_TAKE_PHOTO_S
		case PIC_CROP: {
			//get the returned data
			
			
			Bundle extras = data.getExtras();
			//get the cropped bitmap
			//Bitmap thePic = extras.getParcelable("data");
			mImageBitmap = (Bitmap) extras.get("data");

			mImageView.setImageBitmap(mImageBitmap);
			mImageView.setVisibility(View.VISIBLE);
			//retrieve a reference to the ImageView
			//ImageView picView = (ImageView)findViewById(R.id.imgPreview);
			//display the returned cropped image
			//picView.setImageBitmap(thePic);
		}
		
		} // switch
	}
	 private void performCrop(){
	    	//take care of exceptions
	    	try {
	    		//call the standard crop action intent (the user device may not support it)
		    	Intent cropIntent = new Intent("com.android.camera.action.CROP"); 
		    	//indicate image type and Uri
		    	cropIntent.setDataAndType(picUri, "image/*");
		    	//set crop properties
		    	cropIntent.putExtra("crop", "true");
		    	//indicate output X and Y
		    	cropIntent.putExtra("outputX", 256);
		    	cropIntent.putExtra("outputY", 256);
		    	//retrieve data on return
		    	cropIntent.putExtra("return-data", true);
		    	//start the activity - we handle returning in onActivityResult
		        startActivityForResult(cropIntent, PIC_CROP);  
	    	}
	    	//respond to users whose devices do not support the crop action
	    	catch(ActivityNotFoundException anfe){
	    		//display an error message
	    		String errorMessage = "Whoops - your device doesn't support the crop action!";
	    		Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
	    		toast.show();
	    	}
	    }

	// Some lifecycle callbacks so that the image can survive orientation change
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
		outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
		mImageView.setImageBitmap(mImageBitmap);
		mImageView.setVisibility(
				savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ? 
						ImageView.VISIBLE : ImageView.INVISIBLE
		);
		
	}

	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
	 *
	 * @param context The application's environment.
	 * @param action The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list =
			packageManager.queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	private void setBtnListenerOrDisable( 
			Button btn, 
			Button.OnClickListener onClickListener,
			String intentName
	) {
		if (isIntentAvailable(this, intentName)) {
			btn.setOnClickListener(onClickListener);        	
		} else {
			btn.setText( 
				getText(R.string.cannot).toString() + " " + btn.getText());
			btn.setClickable(false);
		}
	}

}