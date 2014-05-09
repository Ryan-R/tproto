package com.rnr.mapme;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.widget.Button;
import android.widget.Toast;

public class NotificationService extends Service {
	 Handler handler;
	 Runnable runnable;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		handler = new Handler(); 
        runnable = new Runnable() { 

            @Override 
            public void run() { 
                try{
                    Connection connection = new CheckNotifications();
                    connection.execute(getSharedPreferences
                    		("account", MODE_PRIVATE).getString("screenName", ""));
                }
                catch (Exception e) {
                    // TODO: handle exception
                }
                finally{
                    handler.postDelayed(this, 30000); 
                }
            } 
        }; 
        handler.postDelayed(runnable, 30000); 
        return START_NOT_STICKY;
    }
	
	public void onDestroy() {
		handler.removeCallbacks(runnable);
        super.onDestroy();
    }	
	
	
	/*Connects to server to handle checking the user's notifications*/
	class CheckNotifications extends Connection{
		String screenName;
		int notifications;
		
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
				
				toServer.writeInt(11);
				toServer.writeUTF(screenName);
				
				errorDecider = fromServer.readInt();
				notifications = fromServer.readInt();
				
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
				//Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();//receiving notifications?
			    return;
			default:
				break;
			}
			int NOTIFICATION_ID = 19630;
			NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			if(notifications > 0){
				NotificationCompat.Builder builder =
		            new NotificationCompat.Builder(getApplicationContext())
		                    .setSmallIcon(R.drawable.logo)
		                    .setContentTitle("MapMe Notification")
		                    .setContentText("You have unreplied notifications!")
		                    .setNumber(notifications);
			

		    	Intent targetIntent = new Intent(getApplicationContext(), MapMe.class);
		    	PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		    	builder.setContentIntent(contentIntent);		    	
		    	nManager.notify(NOTIFICATION_ID, builder.build());
			}
			else{
				nManager.cancel(NOTIFICATION_ID);
			}
				
		}
	}
}
