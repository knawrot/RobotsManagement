package com.robotsmanagement.core.stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;

import com.example.robotsmanagement.R;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.robotsmanagement.core.MainActivity;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class StreamRequestListener implements OnClickListener {
	
	private final static String tag = StreamRequestListener.class.getName();
	private final static String login = "lorens";
	private final static String password = "konik001";

	private FFmpegFrameGrabber grabber;
	private MainActivity activity;
	private String lastRobotIp;
	private String ip;
	private Integer sessionNo;
	
	public StreamRequestListener(MainActivity activity) {
		this.activity = activity;
		this.sessionNo = 0;

		try {
			WifiInfo wi = ((WifiManager) activity.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
			byte[] bIp = BigInteger.valueOf(wi.getIpAddress()).toByteArray();
			byte[] bIp2 = new byte[bIp.length];
			for(int i = 0; i < bIp.length; i++)
				bIp2[bIp2.length - 1 - i] = bIp[i];
			InetAddress ia = InetAddress.getByAddress(bIp2);
			ip = ia.getHostAddress();
			grabber = FFmpegFrameGrabber.createDefault(createTempFile(ip));
			grabber.start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) { 
		if(activity.getSelectedItem() == null)
				return;
		
		new AsyncTask<String, Void, FFmpegFrameGrabber>() {

			@Override
			protected FFmpegFrameGrabber doInBackground(String... param) {
				Log.i(tag, "Uruchamianie strumieniowania wideo...");
		        
	        	if(grabber != null) {
	        		activity.getRenderThread().setStreamRunning(false);
	        	}
				
		        try {
					execSSHCommand(lastRobotIp, "tokill=`ps aux | grep \"ffmpeg -f video4linux2 -i /dev/video0 -pix_fmt yuv420p -r 5 -f rtp rtp://" 
		    				+ ip + ":1234\" | awk '{ print $2 }'`; kill -9 $tokill");
				} catch (JSchException e) {
					Log.w(tag, "Napotkano b³¹d podczas próby zamkniêcia ostatnio u¿ywanego strumienia.");
					e.printStackTrace();
				} catch (IOException e) {
					Log.w(tag, "Napotkano b³¹d podczas próby zamkniêcia ostatnio u¿ywanego strumienia.");
					e.printStackTrace();
				}
		        
		        try {
		    		Log.i(tag, "IP urz¹dzenia: " + ip + 
		    				"\nIP robota: " + param[0] +
		    				"\nInicjowanie po³¹czenia SSH..." +
		    				"\nnohup ffmpeg -f video4linux2 -i /dev/video0 -pix_fmt yuv420p -r 5 -f rtp rtp://" 
		    				+ ip + ":1234 &");
					execSSHCommand(param[0], "nohup ffmpeg -f video4linux2 -i /dev/video0 -pix_fmt yuv420p -r 5 -f rtp rtp://" 
							+ ip + ":1234 < /dev/null > /dev/null 2>&1 &");
//					execSSHCommand(param[0], "echo " + password + " | sudo -S ffmpeg -f video4linux2 -i /dev/video0 -r 10 -f rtp rtp://" 
//					+ ip + ":1234"); 
					lastRobotIp = param[0];
					
					Log.i(tag, "Przygotowywanie do odbierania strumienia...");
					Log.i(tag, "Oczekiwanie na po³¹czenie ze strumieniem wideo...");
					grabber.restart();
					Log.i(tag, "Pomyœlnie po³¹czono ze strumieniem.");
				} catch(JSchException e) {
					Log.w(tag, "Nie uda³o siê uruchomiæ strumieniowania wideo na robocie.");
					e.printStackTrace();
				} catch(FrameGrabber.Exception e) {
					Log.w(tag, "Nie uda³o siê po³¹czyæ ze strumieniem wideo.");
					e.printStackTrace();
				} catch(UnknownHostException e) {
					Log.e(tag, "B³¹d podczas tworzenia strumienia wejœcia dla komendy zdalnej!");
					e.printStackTrace();
				} catch(IOException e) {
					Log.e(tag, "B³¹d podczas uzyskiwania adresu ip!");
					e.printStackTrace();
				}
				
				return grabber;
			}

			@Override
			protected void onPostExecute(FFmpegFrameGrabber grabber) {   
		        ((TaskDelegate) activity.getRenderThread()).streamActivationResult(grabber);
		    }
		}.execute(activity.getSelectedItem().getIp());
	}
	
	private File createTempFile(String ip) {
	    try {
			File sdp = File.createTempFile("stream", ".sdp");

	    	BufferedReader br = new BufferedReader(new InputStreamReader(
	    			activity.getResources().openRawResource(R.raw.stream)));
	    	OutputStream out = new FileOutputStream(sdp); 

	        String line;
	        while((line = br.readLine()) != null) {
	        	if(line.contains("c=IN IP4 ")) {
	        		line = "c=IN IP4 " + ip;
	        	} else if(line.contains("o=- 0 0 IN IP4 127.0.0.1")) {
	        		line = "o=- " + String.valueOf(sessionNo++) + " 0 IN IP4 127.0.0.1";
	        	}
	        	line += "\n";
	            out.write(line.getBytes(), 0, line.length());
	        }
	        br.close();
	        out.flush();
	        out.close();
	        Log.i(tag, "Zapisano plik SDP na karcie SD.");
	        
	        return sdp;
	    } catch(Exception e) {
	        Log.e(tag, "Nie uda³o siê zapisaæ pliku SDP na karcie SD.");
	        e.printStackTrace();
	    }
	    
	    return null;
	}
	
	private void execSSHCommand(String address, String command) throws JSchException, IOException {
		JSch jsch = new JSch();
		Session session = jsch.getSession(login, address, 22);
	    session.setPassword(password);
	    session.setConfig("StrictHostKeyChecking", "no");
	    session.connect(8000);
	    
	    ChannelExec channel = (ChannelExec) session.openChannel("exec");
		Log.i(tag, "Uruchamianie FFMPEG...");
		channel.setCommand(command);
        channel.connect();
        channel.disconnect();
	    session.disconnect();
	}
}
