//TODO Heart Beat to Server
/*
 * This is the Main Activity of the application designed for 
 * UCM's CS4910-Software Engineering
 * by: Nicholas Lockhart NSL24980, Russell Michal RKM87480, Ryan Rickard RLR19630
 */
package com.example.trackerprototype;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;

import org.w3c.dom.Document;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trackerprototype.GoogleDirection.OnDirectionResponseListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

/* Main activity that allows fragments(for the GoogleMap API v2 map)
 * Implements google play services in order to create a connection
 */
public class TrackerPrototype extends FragmentActivity
    implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener
{
	
	private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000; //failure code for connection
	private SharedPreferences options;  //holds option information
	private SharedPreferences account;  //holds login information
	private SharedPreferences acceptedLocation; //holds accepted location information
	private String userScreenName = ""; //Current user's screenname
	private boolean menuCheck = true; //if menu is allowed
	private boolean routeDisplayed = false; //if route is displayed
	private GoogleMap map; 
	private GoogleDirection direction;
	private LocationClient locationClient;
	private Location userLocation;
	
	/**
	 * Called when application is started on the user's device
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		//Set map from API
		if((map = ((SupportMapFragment) getSupportFragmentManager().
				findFragmentById(R.id.mapview)).getMap()) == null)
		{
			
			//TODO handle lack of map
		}
		else{
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			map.getUiSettings().setZoomControlsEnabled(false);
			map.getUiSettings().setMyLocationButtonEnabled(true);
			map.setMyLocationEnabled(true);			
		}
		
		if(!userScreenName.equals("")){
			return;
		}
		
		options = getSharedPreferences("options", MODE_PRIVATE);
		account = getSharedPreferences("account", MODE_PRIVATE);
		acceptedLocation = getSharedPreferences("location", MODE_PRIVATE);
		//if it not the users first time
		if(!options.getBoolean("firstTime", true)){
			//if the user chooses to use the autoSignin feature
			if(options.getBoolean("autoSignin", false)){
				String screenName = account.getString("screenName", "");
				String password = account.getString("password", "");
				SignIn connection = new SignIn();
        		connection.execute(screenName, password);
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
		locationClient = new LocationClient(this, this, this); //last to params
		
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        locationClient.connect();        
    }
	
	@Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        locationClient.disconnect();
        super.onStop();
    }

	/**
	 * Show register screen
	 */
	private void registerScreen() {
		FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
	    View.inflate(this, R.layout.register_screen_view, mainLayout);
	    ((EditText)findViewById(R.id.passwordConfirm)).addTextChangedListener(new TextWatcher() {
	    	public void onTextChanged(CharSequence s, int start, int before, int count) {
	    		if (((EditText) findViewById(R.id.passwordConfirm)).getText().toString().compareTo(((EditText) findViewById(R.id.password)).getText().toString()) != 0) {
	    			((EditText) findViewById(R.id.passwordConfirm)).setTextColor(Color.RED);
	    		} else {
	    			((EditText) findViewById(R.id.passwordConfirm)).setTextColor(Color.BLACK);
	    		}
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }	    
	    });
	    Button button = (Button) findViewById(R.id.submit);
	    button.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {     
        		v.setEnabled(false);
        		String screenName = ((EditText)findViewById(R.id.screenName)).getText().toString();
        		String password = ((EditText)findViewById(R.id.password)).getText().toString();
        		String passwordConfirm = ((EditText)findViewById(R.id.passwordConfirm)).getText().toString();
        		String birthYear = ((EditText)findViewById(R.id.birthYear)).getText().toString();
        		String firstName = ((EditText)findViewById(R.id.firstName)).getText().toString();
        		String lastName = ((EditText)findViewById(R.id.lastName)).getText().toString();
        		Log.d("l", screenName + password + passwordConfirm + birthYear + firstName + lastName);
        		
        		//check screenName for length
        		if(screenName.length() == 0 || screenName.length() > 25){
        			Toast.makeText(getApplicationContext(), "Bad Screen Name", Toast.LENGTH_SHORT).show();
        			v.setEnabled(true);
        			return;
        		}
        		
        		//check screenName for a-z, A-Z, or 0-9
        		if(!screenName.matches("(\\w+)")) {
        			Toast.makeText(getApplicationContext(), "Bad Screen Name", Toast.LENGTH_SHORT).show();
        			v.setEnabled(true);
        			return;
        		}
        		
        		//check password
        		if(password.length() == 0 || password.length() > 30 || !password.equals(passwordConfirm)){
        			Toast.makeText(getApplicationContext(), "Bad Password", Toast.LENGTH_SHORT).show();
        			v.setEnabled(true);
        			return;
        		}
        		
        		if(!password.matches("(\\w+)")) {
        			Toast.makeText(getApplicationContext(), "Bad Password", Toast.LENGTH_SHORT).show();
        			v.setEnabled(true);
        			return;
        		}
        		
        		//check birthyear
        		if (birthYear.length() < 4 || Integer.parseInt(birthYear) > 2014 || Integer.parseInt(birthYear) < 1900){
        			Toast.makeText(getApplicationContext(), "Bad Birth Year", Toast.LENGTH_SHORT).show();
        			v.setEnabled(true);
        			return;
        		}
        		
        		//check firstname
        		if(firstName.length() == 0 || firstName.length() > 20){
        			Toast.makeText(getApplicationContext(), "Bad First Name", Toast.LENGTH_SHORT).show();
        			v.setEnabled(true);
        			return;
        		}
        		
        		//check lastname
        		if(lastName.length() == 0 || lastName.length() > 20){
        			Toast.makeText(getApplicationContext(), "Bad Last Name", Toast.LENGTH_SHORT).show();
        			v.setEnabled(true);
        			return;
        		}
        		
        		Register connection = new Register();
        		connection.execute(screenName, password, birthYear.toString(), firstName, lastName);       		
        		
            }
        });		
	    
	    Button login = (Button) findViewById(R.id.login);
	    login.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {     
        		ScrollView registerScreenView = (ScrollView) findViewById(R.id.registerScreen);
            	FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
            	mainLayout.removeView(registerScreenView);
            	signInScreen();
        	}
	    });
	}	
	
	//Screen after tutorial to add friends when user's first join
	private void addFriendScreen(){
		FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
	    View.inflate(this, R.layout.add_friend_screen_view, mainLayout);
	    Button request = (Button) findViewById(R.id.request);
	    request.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {   
        		v.setEnabled(false);
        		String requestee = ((EditText) findViewById(R.id.newFriend)).getText().toString();
        		if (requestee.length() == 0 || requestee.length() > 25){
        			Toast.makeText(getApplicationContext(), "Bad Screen Name", Toast.LENGTH_SHORT).show();
        			v.setEnabled(true);
        			return;
        		}
        		RequestFriend connection = new RequestFriend();
        		connection.execute(userScreenName, requestee);
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

	/**
	 * Shows signinScreen
	 */
	private void signInScreen() {
		FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
	    View.inflate(this, R.layout.sign_in_screen_view, mainLayout);
	    Button button = (Button) findViewById(R.id.submit);
	    button.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {  
        		v.setEnabled(false);
        		String screenName = ((EditText)findViewById(R.id.screenName)).getText().toString();
        		String password = ((EditText)findViewById(R.id.password)).getText().toString();
        		
        		//check screenName
        		if(screenName.length() == 0 || screenName.length() > 25){
        			Toast.makeText(getApplicationContext(), "Bad Screen Name", Toast.LENGTH_SHORT).show();
        			v.setEnabled(true);
        			return;
        		}
        		
        		//check password
        		if(password.length() == 0 || password.length() > 30){
        			Toast.makeText(getApplicationContext(), "Bad Password", Toast.LENGTH_SHORT).show();
        			v.setEnabled(true);
        			return;
        		}       		
            	
        		SignIn connection = new SignIn();
        		connection.execute(screenName, password);
            }
        });	
	    
	    Button register = (Button) findViewById(R.id.register);
	    register.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {     
        		ScrollView signInScreenView = (ScrollView) findViewById(R.id.signInScreen);
            	FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
            	mainLayout.removeView(signInScreenView);
            	registerScreen();
        	}
	    });
	}
	
	/**
	 * Shows notifications screen
	 */
	private void notificationScreen(){
		menuCheck = false;
		FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
	    View.inflate(this, R.layout.notification_screen_view, mainLayout);
	    
	    Button exit = (Button) findViewById(R.id.exit);
	    exit.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) { 
        		ScrollView notificationScreenView = (ScrollView) findViewById(R.id.notificationScreen);
        		FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
        		mainLayout.removeView(notificationScreenView);
        		menuCheck = true;
            }
        });
	    exit.setEnabled(false);
	    GetNotifications connection = new GetNotifications();
	    connection.execute(userScreenName);
	}
	
	
    /**
     * Inflate notifications into the notification screen
     */
	private void inflateNotifications(String notifications){
		int i = 0;
		boolean alflag = false, lflag = false, fflag = false; 
		final Context context = this;
		TableLayout acceptedLocationTableLayout = (TableLayout) findViewById(R.id.acceptedLocationRequestTable);
		TableLayout locationTableLayout = (TableLayout) findViewById(R.id.locationRequestTable);
		TableLayout friendTableLayout = (TableLayout) findViewById(R.id.friendRequestTable);
		TextView screenNameView;
		View notificationView;
		StringTokenizer st = new StringTokenizer(notifications, "/");
		String screenName = "";
		//accepted location requests
		st.nextToken();
		    while (st.hasMoreTokens()) {
		        screenName = st.nextToken();
		        if(screenName.equals("location")) break;
		        final int newId = Integer.MAX_VALUE - i++; //creates new id so screenName id can be referenced
		        notificationView = View.inflate(this, R.layout.notification, acceptedLocationTableLayout);
		        screenNameView = (TextView)notificationView.findViewById(R.id.screenName);
			    screenNameView.setText(screenName);
			    screenNameView.setId(newId);
				    
			    Button respond = (Button) notificationView.findViewById(R.id.respond);
			    respond.setText("Locate");
			    respond.setId(Integer.MAX_VALUE - i++);
			    respond.setOnClickListener(new OnClickListener() {
			    	@Override
			    	public void onClick(View v) { 
			    		final String  name = ((TextView)((View) v.getParent()).findViewById(newId)).getText().toString();
			    		
				    	GetLocation connection = new GetLocation();
				    	connection.execute(userScreenName, name);				    			
			    	}	
				 });
		    alflag = true;
	    }
		    
		//location requests
	    while (st.hasMoreTokens()) {
	        screenName = st.nextToken();
	        if(screenName.equals("friend")) break;
	        final int newId = Integer.MAX_VALUE - i++; //creates new id so screenName id can be referenced
	        notificationView = View.inflate(this, R.layout.notification, locationTableLayout);
	        screenNameView = (TextView)notificationView.findViewById(R.id.screenName);
		    screenNameView.setText(screenName);
		    screenNameView.setId(newId);
		    
		    Button respond = (Button) notificationView.findViewById(R.id.respond);
		    respond.setId(Integer.MAX_VALUE - i++);
		    respond.setOnClickListener(new OnClickListener() {
		    	@Override
		    	public void onClick(View v) { 
		    		final String  name = ((TextView)((View) v.getParent()).findViewById(newId)).getText().toString();
		    		new AlertDialog.Builder(context)
		    	    .setTitle("Location Request")
		    	    .setMessage("Do you wish to accept " + name+"'s request?")
		    	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		    	        public void onClick(DialogInterface dialog, int which) { 
		    	        	userLocation = locationClient.getLastLocation();
		    	        	String latString = "" + userLocation.getLatitude();
		    	        	String longString = "" + userLocation.getLongitude();
		    	        	LocationRequestResponse connection = new LocationRequestResponse();
		    	            connection.execute(name ,userScreenName, 
		    	            		latString, longString, "true");
		    	        }
		    	     })
		    	    .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
		    	        public void onClick(DialogInterface dialog, int which) { 
		    	        	LocationRequestResponse connection = new LocationRequestResponse();
		    	            connection.execute(name ,userScreenName, "0", "0","false");
		    	        }
		    	     })
		    	    .setIcon(android.R.drawable.ic_dialog_alert)
		    	    .show();
		    	}
		    });
		    lflag = true;
	    }
	    
	    //friend requests
	    while (st.hasMoreTokens()) {
	    	screenName = st.nextToken();
	    	final int newId = Integer.MAX_VALUE - i++; //creates new id so screenName id can be referenced
	        notificationView = View.inflate(this, R.layout.notification, friendTableLayout);
		    screenNameView = (TextView)notificationView.findViewById(R.id.screenName);
		    screenNameView.setText(screenName);
		    screenNameView.setId(newId);
		    
		    Button respond = (Button) notificationView.findViewById(R.id.respond);
		    respond.setId(Integer.MAX_VALUE - i++);
		    respond.setOnClickListener(new OnClickListener() {
		    	@Override
		    	public void onClick(View v) { 
		    		final String name = ((TextView)((View) v.getParent()).findViewById(newId)).getText().toString();
		    		new AlertDialog.Builder(context)
		    	    .setTitle("Friend Request")
		    	    .setMessage("Do you wish to accept " + name+"'s request?")
		    	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		    	        public void onClick(DialogInterface dialog, int which) { 
		    	        	FriendRequestResponse connection = new FriendRequestResponse();
		    	            connection.execute(name ,userScreenName, "true");
		    	        }
		    	     })
		    	    .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
		    	        public void onClick(DialogInterface dialog, int which) { 
		    	        	FriendRequestResponse connection = new FriendRequestResponse();
		    	            connection.execute(name ,userScreenName, "false");
		    	        }
		    	     })
		    	    .setIcon(android.R.drawable.ic_dialog_alert)
		    	    .show();
		    	}
		    });
		    fflag = true;
	    }
	    
	    if(alflag) acceptedLocationTableLayout.findViewById(R.id.acceptedLocationRequests).setVisibility(View.GONE);
	    
	    if(lflag) locationTableLayout.findViewById(R.id.locationRequests).setVisibility(View.GONE);
	    
	    if(fflag) friendTableLayout.findViewById(R.id.friendRequests).setVisibility(View.GONE);
	    
	    Button exit = (Button) findViewById(R.id.exit);
	    exit.setEnabled(true);
	}
	
	
	/**
	 * Show friend list screen
	 */
	private void friendListScreen(){
		menuCheck = false;
		FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
		View.inflate(this, R.layout.friends_screen_view, mainLayout);
		
		 Button request = (Button) findViewById(R.id.request);
		 request.setOnClickListener(new OnClickListener() {
	       	@Override
	        public void onClick(View v) {   
	       		v.setEnabled(false);
	       		String requestee = ((EditText) findViewById(R.id.newFriend)).getText().toString();
	       		if (requestee.length() == 0 || requestee.length() > 25){
	       			Toast.makeText(getApplicationContext(), "Bad Screen Name", Toast.LENGTH_SHORT).show();
	       			v.setEnabled(true);
	       			return;
	      		}
	       		RequestFriend connection = new RequestFriend();
	      		connection.execute(userScreenName, requestee);
	        }
	    });
		
		Button exit = (Button) findViewById(R.id.exit);
		exit.setOnClickListener(new OnClickListener() {
	      	@Override
	        public void onClick(View v) { 
	      		RelativeLayout friendsScreenView = (RelativeLayout) findViewById(R.id.friendsScreen);
	       		FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
	       		mainLayout.removeView(friendsScreenView);
	       		menuCheck = true;
	        }
	    });
		exit.setEnabled(false);
		GetFriends connection = new GetFriends();
		connection.execute(userScreenName);
	}
	
	/**
	 * Inflate friend List into the friend list screen
	 */
	private void inflateFriendList(String friends){
		int i = 0;
		boolean fflag = false;
		final Context context = this;
		TableLayout friendListTableLayout = (TableLayout) findViewById(R.id.friendListTable);
		TextView screenNameView;
		View notificationView;
		StringTokenizer st = new StringTokenizer(friends, "/");
		String screenName = "";
		//friends
		while (st.hasMoreTokens()) {
		    screenName = st.nextToken();
		    
		    final int newId = Integer.MAX_VALUE - i++; //creates new id so screenName id can be referenced
		    notificationView = View.inflate(this, R.layout.friend, friendListTableLayout);
		    screenNameView = (TextView)notificationView.findViewById(R.id.screenName);
		    screenNameView.setText(screenName);
		    screenNameView.setId(newId);
			    
		    Button options = (Button) notificationView.findViewById(R.id.options);
		    options.setId(Integer.MAX_VALUE - i++);
		    options.setOnClickListener(new OnClickListener() {
		    	@Override
		    	public void onClick(View v) { 
		    		final String  name = ((TextView)((View) v.getParent()).findViewById(newId)).getText().toString();
		    		CharSequence[] options = {"Locate", "Delete"};
		    		new AlertDialog.Builder(context)
		    	    .setTitle("Friend Options")
		    	    .setItems(options, new DialogInterface.OnClickListener() {
		    	    	public void onClick(DialogInterface dialog, int which) {
		    	    		Connection connection;
		    	    		switch(which){
		    	    		case 0:
		    	    			connection = new LocationRequest();
		    	    			connection.execute(userScreenName, name);
		    	    			break;
		    	    		case 1:
		    	    			connection = new DeleteFriend();
		    	    			connection.execute(userScreenName, name);
		    	    			break;
		    	    		}
		    	    	}
		    	    })		    	    
		    	    .setIcon(android.R.drawable.ic_dialog_alert)
		    	    .show();
		    	}
		    });
		    fflag = true;
		}		 
		   
		if(fflag) friendListTableLayout.findViewById(R.id.noFriends).setVisibility(View.GONE);
	    
		Button exit = (Button) findViewById(R.id.exit);
		exit.setEnabled(true);
	}
	
	/**
	 * Generate screen for directions
	 */
	private void directionsScreen(String screenname){
		menuCheck = false;
		FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
		View directionScreenView = View.inflate(this, R.layout.directions_screen_view, mainLayout);
		TextView titleView = (TextView)directionScreenView.findViewById(R.id.directionTitle);  
		titleView.setText("Directions to " + screenname);
	    Button exit = (Button) findViewById(R.id.exit);
	    exit.setOnClickListener(new OnClickListener() {
	      	@Override
	        public void onClick(View v) { 
	       		RelativeLayout directionsScreenView = (RelativeLayout) findViewById(R.id.directionsScreen);
	       		FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
	       		mainLayout.removeView(directionsScreenView);
	      		menuCheck = true;
	        }
	    });
	    exit.setEnabled(false);
	    
	    inflateDirections(screenname);
	}
	
	/**
	 * Inflates directions into the directions screen based upon the route currently displayed
	 */
	private void inflateDirections(String screenname){
		int i = 0;
		TableLayout directionsTableLayout = (TableLayout) findViewById(R.id.directionsTable);
		TextView instructionView;
		TextView distanceView;
		View directionView;
		String instruction, distance;		
		String tokenizedSteps = acceptedLocation.getString("instructions", "Failed to load/?/");
		StringTokenizer st = new StringTokenizer(tokenizedSteps, "+");
		
		//Line by Line of instruction/distance pairs
		while (st.hasMoreTokens()) {
		    instruction = st.nextToken();
		    directionView = View.inflate(this, R.layout.direction, directionsTableLayout);
		    instructionView = (TextView)directionView.findViewById(R.id.instruction);
		    instructionView.setText(Html.fromHtml(instruction));
		    instructionView.setId(Integer.MAX_VALUE - i++);
		    
		    distance = st.nextToken();
		    distanceView = (TextView)directionView.findViewById(R.id.distance);
		    distanceView.setText(distance);	
		    distanceView.setId(Integer.MAX_VALUE - i++);
		}		 
		   
		Button exit = (Button) findViewById(R.id.exit);
		exit.setEnabled(true);
	}
	
	
	/**
	 * Generate a route using the {@link GoogleDirection} class.
	 * Save instructions and friends name to be used in directions screen	 
	 */
	public void getRoute(final String name, final double lat, final double lng){

        userLocation = locationClient.getLastLocation();
                
        direction = new GoogleDirection(this);
        //create response listener for when the direction API responds to request
        direction.setOnDirectionResponseListener(new OnDirectionResponseListener() {
			public void onResponse(String status, Document doc, GoogleDirection gd) {
				map.clear();
				map.addPolyline(gd.getPolyline(doc, 3, Color.GREEN));	
				LatLng userLatLng = new LatLng(userLocation.getLatitude(),userLocation.getLongitude());
				LatLng friendLatLng = new LatLng(lat,lng);
		        map.addMarker(new MarkerOptions().position(userLatLng)
		        	    .icon(BitmapDescriptorFactory.defaultMarker(
		        	    BitmapDescriptorFactory.HUE_CYAN)));
        
		        map.addMarker(new MarkerOptions().position(new LatLng(lat,lng))
		        	    .icon(BitmapDescriptorFactory.defaultMarker(
		        	    BitmapDescriptorFactory.HUE_CYAN)));
		        
		        //Zoom to route
		        LatLngBounds.Builder builder = new LatLngBounds.Builder();
		        builder.include(userLatLng);
		        builder.include(friendLatLng);
		        map.animateCamera(CameraUpdateFactory.newLatLngBounds
		        		(builder.build(), 
		        		getApplicationContext().getResources().getDisplayMetrics().widthPixels, 
		        		getApplicationContext().getResources().getDisplayMetrics().heightPixels, 
		                100));
		        
		        //set key/value pair as instructions, and name
		        Editor edit = acceptedLocation.edit();
		        edit.putString("instructions", direction.getStepInstruction(doc));
		        edit.putString("name", name);
		        edit.commit();
		        routeDisplayed = true;
			}
		});
        
        //request to google directions API
        direction.request(new LatLng(userLocation.getLatitude(),userLocation.getLongitude()),
        		new LatLng(lat,lng), GoogleDirection.MODE_DRIVING);
	}
	
	//TODO add options screen
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	//Dynamically create context Menu
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear(); //Clear view of previous menu
        MenuInflater inflater = getMenuInflater();
        if(!userScreenName.equals("") &&  menuCheck)
            inflater.inflate(R.menu.activity_main, menu);
        return super.onPrepareOptionsMenu(menu);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    	case R.id.friendlist:
	    		friendListScreen();
	    		return true;
	        case R.id.notifications:
	            notificationScreen();
	            return true;
	        case R.id.directions:
	        	//show directions for the current route
	        	if(routeDisplayed)
	        		directionsScreen(acceptedLocation.getString("name", "?"));
	        	//No route to get directions
	        	else
	        		Toast.makeText(getApplicationContext(), "No Route", Toast.LENGTH_SHORT).show();
	            return true;	       
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
	/*CONNECTIONS TO THE SERVER*/	
	/*Connect to server to handle registration*/
	class Register extends Connection{
		String screenName;
		String password;
		int birthYear;
		String firstName;
		String lastName;
		@Override
		protected Void doInBackground(String... params) {
			screenName = params[0];
			password = params[1];
			birthYear = Integer.parseInt(params[2]);
			firstName = params[3];
			lastName = params[4];
					
			try {
				// Tell what address and port to send information to
				socket = new Socket(socketName, port);
				fromServer = new DataInputStream(
						socket.getInputStream());

				toServer = new DataOutputStream(
						socket.getOutputStream());
				toServer.writeInt(1);
				toServer.writeUTF(screenName);
				toServer.writeUTF(password);
				toServer.writeInt(birthYear);
				toServer.writeUTF(firstName);
				toServer.writeUTF(lastName);
				
				errorDecider = fromServer.readInt();
				
				socket.close();
			}
			catch(IOException ex) {
				System.err.println(ex);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void Result){			
			Button button = (Button) findViewById(R.id.submit);
			button.setEnabled(true);
			
			switch (errorDecider){
			case -1: 
				Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();				
				return;
			case -2:
				Toast.makeText(getApplicationContext(), "Screen name already exists", Toast.LENGTH_SHORT).show();				
				return;
			default:
				break;
		}
			userScreenName = screenName;
			SharedPreferences.Editor optionsEditor = options.edit();
    		optionsEditor.putBoolean("firstTime", false);
    		optionsEditor.commit();
    		
    		ScrollView registerScreenView = (ScrollView) findViewById(R.id.registerScreen);
        	FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
        	mainLayout.removeView(registerScreenView);
        	
        	addFriendScreen();
		}
	}
	
	/*Connect to server to handle attempt to signin*/
	class SignIn extends Connection{
		String screenName;
		String password;
		
		@Override
		protected Void doInBackground(String... params) {
			screenName = params[0];
			password = params[1];
			try {
				// Tell what address and port to send information to
				socket = new Socket(socketName, port);

				fromServer = new DataInputStream(
						socket.getInputStream());

				toServer = new DataOutputStream(
						socket.getOutputStream());
				
				toServer.writeInt(2);
				toServer.writeUTF(screenName);
				toServer.writeUTF(password);
				
				errorDecider = fromServer.readInt();
				
				socket.close();
			}
			catch(IOException ex) {
				System.err.println(ex);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void Result){
			switch (errorDecider){
			case -1: 
				if(!options.getBoolean("autoSignin", false)){
					Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
					Button button = (Button) findViewById(R.id.submit);
					button.setEnabled(true);
				}
				else{
					Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
					signInScreen();
				}
				return;
			case -3: 
				if(!options.getBoolean("autoSignin", false)){
					Toast.makeText(getApplicationContext(), "Incorrect screen name or password", Toast.LENGTH_SHORT).show();
					Button button = (Button) findViewById(R.id.submit);
					button.setEnabled(true);
				}
				else{
					Toast.makeText(getApplicationContext(), "Error in signing in", Toast.LENGTH_SHORT).show();
					signInScreen();
				}
				return;
			default:
				break;
		}
			
			userScreenName = screenName;			
			if(!options.getBoolean("autoSignin", false)){
				SharedPreferences.Editor optionsEditor = options.edit();
				if(((CheckBox)findViewById(R.id.checkBox1)).isChecked()){					
					optionsEditor.putBoolean("autoSignin", true);					
					SharedPreferences.Editor accountEditor = account.edit();
					accountEditor.putString("screenName", screenName);
					accountEditor.putString("password", password);    
					accountEditor.commit();					
    			}
				optionsEditor.putBoolean("firstTime", false);
	    		optionsEditor.commit();
				
				ScrollView signInScreenView = (ScrollView) findViewById(R.id.signInScreen);
	        	FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
	        	mainLayout.removeView(signInScreenView);
			}			
		}
	}

	/*Connects to server to handle Friend Requests*/
	class RequestFriend extends Connection{
		String requester;
		String requestee;
		
		@Override
		protected Void doInBackground(String... params) {
			requester = params[0];
			requestee = params[1];
			try {
				// Tell what address and port to send information to
				socket = new Socket(socketName, port);

				fromServer = new DataInputStream(
						socket.getInputStream());

				toServer = new DataOutputStream(
						socket.getOutputStream());
				
				toServer.writeInt(3);
				toServer.writeUTF(requester);
				toServer.writeUTF(requestee);
				
				errorDecider = fromServer.readInt();
				
				socket.close();
			}
			catch(IOException ex) {
				System.err.println(ex);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void Result){
			EditText screenName = (EditText) findViewById(R.id.newFriend);
			screenName.setText("");		
			Button request = (Button) findViewById(R.id.request);
			request.setEnabled(true);
			
			switch (errorDecider){
				case -1: 
					Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
					return;
				case -4: 
					Toast.makeText(getApplicationContext(), "Friend Request already exists", Toast.LENGTH_SHORT).show();	
					return;
				case -5: 
					Toast.makeText(getApplicationContext(), "Friend does not exist right now", Toast.LENGTH_SHORT).show();
					return;
				default:
					break;
			}
			
			Toast.makeText(getApplicationContext(), "Friend Request Sent", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	/*Connects to server to handle Location Requests*/
	class LocationRequest extends Connection{
		String requester;
		String requestee;
		
		@Override
		protected Void doInBackground(String... params) {
			requester = params[0];
			requestee = params[1];
			try {
				// Tell what address and port to send information to
				socket = new Socket(socketName, port);

				fromServer = new DataInputStream(
						socket.getInputStream());

				toServer = new DataOutputStream(
						socket.getOutputStream());
				
				toServer.writeInt(4);
				toServer.writeUTF(requester);
				toServer.writeUTF(requestee);
				
				errorDecider = fromServer.readInt();
				
				socket.close();
			}
			catch(IOException ex) {
				System.err.println(ex);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void Result){
			
			switch (errorDecider){
				case -1: 
					Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
					return;
				case -6: 
					Toast.makeText(getApplicationContext(), "Friends does not exist", Toast.LENGTH_SHORT).show();	
					return;
				default:
					break;
			}
			
			Toast.makeText(getApplicationContext(), "Location Request Sent", Toast.LENGTH_SHORT).show();
		}
	}
	
	/*Connects to server to handle Friend Deletion*/
	class DeleteFriend extends Connection{
		String requester;
		String requestee;
		
		@Override
		protected Void doInBackground(String... params) {
			requester = params[0];
			requestee = params[1];
			try {
				// Tell what address and port to send information to
				socket = new Socket(socketName, port);

				fromServer = new DataInputStream(
						socket.getInputStream());

				toServer = new DataOutputStream(
						socket.getOutputStream());
				
				toServer.writeInt(5);
				toServer.writeUTF(requester);
				toServer.writeUTF(requestee);
				
				errorDecider = fromServer.readInt();
				
				socket.close();
			}
			catch(IOException ex) {
				System.err.println(ex);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void Result){	
			//Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();	
			switch (errorDecider){
				case -1: 				
					Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
				    return;
				default:
					break;
			}
			Toast.makeText(getApplicationContext(), "Friend Deleted", Toast.LENGTH_SHORT).show();
			RelativeLayout friendsScreenView = (RelativeLayout) findViewById(R.id.friendsScreen);
       		FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
    		mainLayout.removeView(friendsScreenView);
    		friendListScreen();
		}
	}
		
	/*Connects to server to handle Response of friend request*/
	class LocationRequestResponse extends Connection{
		String requester;
		String requestee;
		boolean response;
		double lastKnownLat;
		double lastKnownLong;
		
		@Override
		protected Void doInBackground(String... params) {
			requester = params[0];
			requestee = params[1];			
			lastKnownLat = Double.parseDouble(params[2]);
			lastKnownLong  = Double.parseDouble(params[3]);
			response = Boolean.valueOf(params[4]);
			try {
				// Tell what address and port to send information to
				socket = new Socket(socketName, port);

				fromServer = new DataInputStream(
						socket.getInputStream());

				toServer = new DataOutputStream(
						socket.getOutputStream());
				
				toServer.writeInt(6);
				toServer.writeUTF(requester);
				toServer.writeUTF(requestee);				
				toServer.writeDouble(lastKnownLat);
				toServer.writeDouble(lastKnownLong);
				toServer.writeBoolean(response);
				
				errorDecider = fromServer.readInt();
				
				socket.close();
			}
			catch(IOException ex) {
				System.err.println(ex);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void Result){	
			switch (errorDecider){
				case -1: 				
					Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
				    return;
				default:
					break;
			}
			Toast.makeText(getApplicationContext(), "Response Sent", Toast.LENGTH_SHORT).show();
			ScrollView notificationScreenView = (ScrollView) findViewById(R.id.notificationScreen);
    		FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
    		mainLayout.removeView(notificationScreenView);
    		notificationScreen();			
		}
	}
	
	/*Connects to server to handle response of friend request*/
	class FriendRequestResponse extends Connection{
		String requester;
		String requestee;
		boolean response;
		
		@Override
		protected Void doInBackground(String... params) {
			requester = params[0];
			requestee = params[1];
			response = Boolean.valueOf(params[2]);
			try {
				// Tell what address and port to send information to
				socket = new Socket(socketName, port);

				fromServer = new DataInputStream(
						socket.getInputStream());

				toServer = new DataOutputStream(
						socket.getOutputStream());
				
				toServer.writeInt(7);
				toServer.writeUTF(requester);
				toServer.writeUTF(requestee);
				toServer.writeBoolean(response);
				
				errorDecider = fromServer.readInt();
				
				socket.close();
			}
			catch(IOException ex) {
				System.err.println(ex);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void Result){	
			switch (errorDecider){
				case -1: 				
					Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
				    return;
				default:
					break;
			}
			Toast.makeText(getApplicationContext(), "Response Sent", Toast.LENGTH_SHORT).show();
			ScrollView notificationScreenView = (ScrollView) findViewById(R.id.notificationScreen);
    		FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
    		mainLayout.removeView(notificationScreenView);
    		notificationScreen();
		}
	}
	
	/*Connects to server to handle getting the user's friends*/
	class GetFriends extends Connection{
		String screenName;
		String friends;
		
		@Override
		protected Void doInBackground(String... params) {
			screenName = params[0];
			try {
				// Tell what address and port to send information to
				socket = new Socket(socketName, port);

				fromServer = new DataInputStream(
						socket.getInputStream());

				toServer = new DataOutputStream(
						socket.getOutputStream());
				
				toServer.writeInt(8);
				toServer.writeUTF(screenName);
				
				errorDecider = fromServer.readInt();
				friends = fromServer.readUTF();
				
				socket.close();
			}
			catch(IOException ex) {
				System.err.println(ex);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void Result){
			switch (errorDecider){
				case -1: 		
					Button exit = (Button) findViewById(R.id.exit);
				    exit.setEnabled(true);
					Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();//reseving friends list?
				    return;
				default:
					break;
			}
			inflateFriendList(friends);
		}
	}
	
	/*Connects to server to handle getting the user's notifications*/
	class GetNotifications extends Connection{
		String screenName;
		String notifications;
		
		@Override
		protected Void doInBackground(String... params) {
			screenName = params[0];
			try {
				// Tell what address and port to send information to
				socket = new Socket(socketName, port);

				fromServer = new DataInputStream(
						socket.getInputStream());

				toServer = new DataOutputStream(
						socket.getOutputStream());
				
				toServer.writeInt(9);
				toServer.writeUTF(screenName);
				
				errorDecider = fromServer.readInt();
				notifications = fromServer.readUTF();
				
				socket.close();
			}
			catch(IOException ex) {
				System.err.println(ex);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void Result){	
			
			switch (errorDecider){
			case -1: 	
				Button exit = (Button) findViewById(R.id.exit);
			    exit.setEnabled(true);
				Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();//receiving notifications?
			    return;
			default:
				break;
			}
			inflateNotifications(notifications);
				
		}
	}
	
	/*Connects to server to handle getting the user's requested location*/
	class GetLocation extends Connection{
		String requester;
		String requestee;
		double lastKnownLat;
		double lastKnownLong;
		
		@Override
		protected Void doInBackground(String... params) {
			requester = params[0];
			requestee = params[1];
			try {
				// Tell what address and port to send information to
				socket = new Socket(socketName, port);

				fromServer = new DataInputStream(
						socket.getInputStream());

				toServer = new DataOutputStream(
						socket.getOutputStream());
				
				toServer.writeInt(10);
				toServer.writeUTF(requester);
				toServer.writeUTF(requestee);
				
				lastKnownLat = fromServer.readDouble();
				lastKnownLong = fromServer.readDouble();
				errorDecider = fromServer.readInt();				
				
				socket.close();
			}
			catch(IOException ex) {
				System.err.println(ex);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void Result){	
			switch (errorDecider){
				case -1: 				
					Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
				    return;
				case -2:
					Toast.makeText(getApplicationContext(), "Error getting latitude and longitude", Toast.LENGTH_SHORT).show();
				    return;
				default:
					break;
			}			
			ScrollView notificationScreenView = (ScrollView) findViewById(R.id.notificationScreen);
    		FrameLayout mainLayout = (FrameLayout) findViewById(R.id.mainLayout);
    		mainLayout.removeView(notificationScreenView);
    		menuCheck = true;
			getRoute(requestee, lastKnownLat, lastKnownLong);
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		 /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (result.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                result.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showDialog(result.getErrorCode());
        }
		
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// Display the connection status
        //Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		userLocation = locationClient.getLastLocation();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom
        		(new LatLng(userLocation.getLatitude(),userLocation.getLongitude()), 16));
		
	}

	@Override
	public void onDisconnected() {
		// Display the connection status
        //Toast.makeText(this, "Disconnected. Please re-connect.",
        //       Toast.LENGTH_SHORT).show();
		
	}
	
}
