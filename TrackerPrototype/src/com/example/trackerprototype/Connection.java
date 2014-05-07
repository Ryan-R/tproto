package com.example.trackerprototype;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import android.os.AsyncTask;

/**
 * Connection to server
 * Creates an asynchronous task to connect to Server
 * @author Ryan
 *
 */
public class Connection extends AsyncTask<String, Void, Void>{

	protected Socket socket;
	protected DataOutputStream toServer;
	protected DataInputStream fromServer;
	protected String socketName = "75.134.43.74";
	protected int port = 8081;
	protected int errorDecider;
	
	@Override
	protected Void doInBackground(String... params) {
		// TODO Auto-generated method stub
		return null;
	}

}
