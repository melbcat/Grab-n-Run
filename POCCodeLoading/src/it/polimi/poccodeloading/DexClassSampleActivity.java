package it.polimi.poccodeloading;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity shows a small example in which a customizer element is loaded
 * dynamically at run time through a DexClassLoader API call.
 * Depending on the provided jar container this object will customize in a 
 * different way the layout of the components in the GUI even if the same 
 * method calls are executed.
 * 
 * @author Luca Falsina
 * 
 */
public class DexClassSampleActivity extends Activity {

	// Unique identifier used for Log entries
	private static final String TAG_DEX_SAMPLE = DexClassSampleActivity.class.getSimpleName();
	
	private boolean isSecureModeChosen;
	
	// This is the interface used to mask the object
	// retrieved from the external jar..
	private ComponentModifier mComponentModifier;
	
	//private String assetSuffix = "/exampleJar/componentModifier.jar";
	
	// Used to pick different classes dynamically at run time
	private final String firstClassName = "it.polimi.componentmodifier.FirstComponentModifierImpl";
	private final String secondClassName = "it.polimi.componentmodifier.SecondComponentModifierImpl";
	
	// Used to visualize helper toast messages..
	private Handler toastHandler;
	
	// Components of the layout
	private TextView textView;
	private Button firstBtn, secondBtn, thirdBtn;
	private Switch switchSlider;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dex_class_sample);
		setTitle(getString(R.string.title_activity_dex_class_sample));
		
		toastHandler = new Handler();
		
		// Retrieve the intent of the launching activity
		Intent intent = getIntent();
		
		// Enable/Disable secure loading;
		isSecureModeChosen = intent.getBooleanExtra(MainActivity.IS_SECURE_LOADING_CHOSEN, false);
		
		// Retrieve all the components, which are going to be modified
		// by the instance of ComponentModifier
		textView = (TextView) findViewById(R.id.exp_text_dex_load_jar);
		firstBtn = (Button) findViewById(R.id.button1_dex_load_jar);
		secondBtn = (Button) findViewById(R.id.button2_dex_load_jar);
		thirdBtn = (Button) findViewById(R.id.button3_dex_load_jar);
		switchSlider = (Switch) findViewById(R.id.switch_dex_load_jar);
		switchSlider.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			   @Override
			   public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				   if(isChecked){
					   onBtnClickExit(buttonView);
				   }
			   }
		});
		
	}
	
	private ComponentModifier retrieveComponentModifier(String className) {

		Log.i(TAG_DEX_SAMPLE, "Setting up Dex Class Loader..");
		
		ComponentModifier retComponentModifier = null;
		
		// final String jarContainerPath = getAssets() + assetSuffix;
		final String jarContainerPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/componentModifier.jar";
		// final String jarContainerPath = "https://github.com/lukeFalsina/test/blob/master/componentModifier.jar";
		File dexOutputDir = getDir("dex", MODE_PRIVATE);
		
		DexClassLoader mDexClassLoader = new DexClassLoader(	jarContainerPath, 
																dexOutputDir.getAbsolutePath(), 
																null, 
																getClass().getClassLoader());		
		
		try {
			
			Class<?> loadedClass = mDexClassLoader.loadClass(className);
			
			retComponentModifier = (ComponentModifier) loadedClass.newInstance();
			
		} catch (ClassNotFoundException e) {
			Log.e(TAG_DEX_SAMPLE, "Error: Class not found!");
			e.printStackTrace();
		} catch (InstantiationException e) {
			Log.e(TAG_DEX_SAMPLE, "Error: Instantiation issues!");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			Log.e(TAG_DEX_SAMPLE, "Error: Illegal access!");
			e.printStackTrace();
		}
		
		if (retComponentModifier != null) {
			
			final String shortClassName = retComponentModifier.getClass().getSimpleName();
			
			Log.i(TAG_DEX_SAMPLE, "DexClassLoader was successful!\nLoaded class name:" + shortClassName + "\nPath: " + jarContainerPath);
			
			toastHandler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(DexClassSampleActivity.this,
							"DexClassLoader was successful!\nLoaded class name: " + shortClassName + "\nPath: " + jarContainerPath,
							Toast.LENGTH_LONG).show();
				}
				
			});
		}
		else {
			
			toastHandler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(DexClassSampleActivity.this,
							"DexClassLoader failed!\nLeaving this activity..",
							Toast.LENGTH_SHORT).show();
				}
				
			});
			
			// Exit this activity..
			finish();
		}
		
		return retComponentModifier;
	}
	
	/**
	 * When one of the two initial buttons in this activity is clicked 
	 * a different component is dynamically loaded and used to customize
	 * the rest of the layout.
	 * 
	 * @param view
	 */
	public void onBtnClick(View view) {
		
		if (isSecureModeChosen) {
			
			// TODO Fill with test ClassLoader..
			
		} else {
		
			if (view.getId() == firstBtn.getId()) {
				
				mComponentModifier = retrieveComponentModifier(firstClassName);
				Log.i(TAG_DEX_SAMPLE, "First button was pressed..");
			}
			else {
			
				mComponentModifier = retrieveComponentModifier(secondClassName);
				Log.i(TAG_DEX_SAMPLE, "Second button was pressed..");
			}
		}
		
		List<Button> buttonList = new ArrayList<Button>();
		buttonList.add(firstBtn);
		buttonList.add(secondBtn);
		buttonList.add(thirdBtn);
		
		// The dynamic loaded class customizes all the components..
		mComponentModifier.customizeButtons(buttonList);
		mComponentModifier.customizeSwitch(switchSlider);
		mComponentModifier.customizeTextView(textView);
		
		Log.i(TAG_DEX_SAMPLE, "Customization process successfully completed.");
	}
	
	/**
	 * This effect is used to end the activity.
	 * 
	 * @param view
	 */
	public void onBtnClickExit(View view) {
		
		toastHandler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(DexClassSampleActivity.this,
						"Activity completed..",
						Toast.LENGTH_SHORT).show();
			}
			
		});
		
		Log.i(TAG_DEX_SAMPLE, "End of " + R.string.title_activity_dex_class_sample + " Activity.");
		
		finish();
	}

}
