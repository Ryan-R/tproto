//TODO FriendsList, FriendsView in Friendslist, Connect to Map API, Create fake friends with fake location, friend request
package com.example.trackerprototype;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

public class TrackerPrototype extends MapActivity {
	
	private SharedPreferences options;  //holds option information
	//Better way of doing this
	//http://stackoverflow.com/questions/1925486/android-storing-username-and-password
	private SharedPreferences account;  //holds login information
	private SharedPreferences friends;  //holds friend information
	//private BearingFrameLayout bearingFrameLayout; // rotates the MapView
	private MapView mapView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		MapView mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
		
		options = getSharedPreferences("options", MODE_PRIVATE);
		
		//if it not the users first time
		if(!options.getBoolean("firstTime", true)){
			//if the user chooses to use the autoSignin feature
			if(options.getBoolean("autoSignin", false)){
				//Better way of doing this
				//http://stackoverflow.com/questions/1925486/android-storing-username-and-password
				account = getSharedPreferences("account", MODE_PRIVATE);
				String screenName = account.getString("screenName", "");
				String password = account.getString("password", "");
				signIn();
			}
			//if the user chooses to not use the autoSignin feature
			else{
				signInScreen();
			}
		}
		//Users first time
		else{
			registerScreen();			
		}
		
	}

	private void registerScreen() {
		FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
	    View.inflate(this, R.layout.register_screen_view, mainLayout);
	    Button button = (Button) findViewById(R.id.submit);
	    button.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {       		
            	register();
            }
        });	   
	}
	
	private void register() {
		String screenName = ((EditText)findViewById(R.id.screenName)).getText().toString();
		String password = ((EditText)findViewById(R.id.password)).getText().toString();
		String passwordConfirm = ((EditText)findViewById(R.id.passwordConfirm)).getText().toString();
		String birthYear = ((EditText)findViewById(R.id.birthYear)).getText().toString();
		String firstName = ((EditText)findViewById(R.id.firstName)).getText().toString();
		String lastName = ((EditText)findViewById(R.id.lastName)).getText().toString();
		Log.d("l", screenName + password + passwordConfirm + birthYear + firstName + lastName);
		
		//TODO HERE is where we do checks
		//TODO Communicate with server and checks
		//TODO recieve info from server
		//TODO react to server reply
		
		SharedPreferences.Editor optionsEditor = options.edit();
		optionsEditor.putBoolean("firstTime", false);
		optionsEditor.commit();
		
		ScrollView registerScreenView = (ScrollView) findViewById(R.id.registerScreen);
    	FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
    	mainLayout.removeView(registerScreenView);
    	
    	tutorialScreen();
	}
	
	//if we just want to give instructions this can be changed to a dialog
	private void tutorialScreen(){
		FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
	    View.inflate(this, R.layout.tutorial_screen_view, mainLayout);
	    Button button = (Button) findViewById(R.id.next);
	    button.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {  
        		ScrollView tutorialScreenView = (ScrollView) findViewById(R.id.tutorialScreen);
            	FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
            	mainLayout.removeView(tutorialScreenView);
            	addFriendScreen();
            }
        });
	}
	
	//this could just be a dialog as well
	private void addFriendScreen(){
		FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
	    View.inflate(this, R.layout.add_friend_screen_view, mainLayout);
	    Button request = (Button) findViewById(R.id.request);
	    request.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {       		
            	sendRequest();
            }
        });
	    Button done = (Button) findViewById(R.id.done);
	    done.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		ScrollView addFriendScreenView = (ScrollView) findViewById(R.id.addFriendScreen);
            	FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
            	mainLayout.removeView(addFriendScreenView);
            }
        });
	}
	
	private void sendRequest(){
		EditText screenName = (EditText) findViewById(R.id.screenName);
		TestClient test = new TestClient();
		test.execute(screenName.getText().toString());
		//screenName.setText("");
		
		//Toast.makeText(getApplicationContext(), "Request Sent", Toast.LENGTH_SHORT).show();		
	}

	private void signInScreen() {
		FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
	    View.inflate(this, R.layout.sign_in_screen_view, mainLayout);
	    Button button = (Button) findViewById(R.id.submit);
	    button.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {       		
            	signIn();
            }
        });		
	}

	private void signIn() {
		String screenName = ((EditText)findViewById(R.id.screenName)).getText().toString();
		String password = ((EditText)findViewById(R.id.password)).getText().toString();
		Log.d("l", screenName + password);
		// TODO Connect to server and validate screenName and password
		
		if(((CheckBox)findViewById(R.id.checkBox1)).isChecked()){
			SharedPreferences.Editor optionsEditor = options.edit();
			optionsEditor.putBoolean("firstTime", true);
			optionsEditor.commit();
		}
		
		ScrollView registerScreenView = (ScrollView) findViewById(R.id.signInScreen);
    	FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
    	mainLayout.removeView(registerScreenView);
    	
    	//TODO Check friends requests, if there are show them
    	//TODO if not dirent to main app
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	class TestClient extends AsyncTask<String, Void, Void>{
		private Socket socket;
		private DataOutputStream toServer;
		private DataInputStream fromServer;
		private String socketName = "75.134.43.74";
		private int port = 8081;
		private String response;
		
		@Override
		protected Void doInBackground(String... params) {
			try {
				// Tell what address and port to send information to
				Socket socket = new Socket(socketName, port);

				fromServer = new DataInputStream(
						socket.getInputStream());

				toServer = new DataOutputStream(
						socket.getOutputStream());
				
				toServer.writeUTF(params[0]);
				while(true){
					if((response = fromServer.readUTF()) != null)
						break;				
				}
				socket.close();
			}
			catch(IOException ex) {
				System.err.println(ex);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void Result){
			EditText screenName = (EditText) findViewById(R.id.screenName);
			screenName.setText("");		
			Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();	
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}
