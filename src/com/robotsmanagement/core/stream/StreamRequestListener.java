package com.robotsmanagement.core.stream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class StreamRequestListener implements OnClickListener {
	
	private final static String tag = StreamRequestListener.class.getName();
	private final static String login = "panda";
	private final static String password = "panda2013";

	private FFmpegFrameGrabber grabber;
	private MainActivity activity;
	private String lastRobotIp;
	private String lastIp;
	
	public StreamRequestListener(MainActivity activity) {
		this.activity = activity;
		
	    try {
	    	InputStream in = activity.getResources().openRawResource(R.raw.stream);
	    	OutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/stream.sdp");

	        byte[] buffer = new byte[65536 * 2];
	        int read;
	        while((read = in.read(buffer)) != -1) {
	            out.write(buffer, 0, read);
	        }
	        in.close();
	        in = null;
	        out.flush();
	        out.close();
	        out = null;
	        Log.i(tag, "Zapisano plik SDP na karcie SD.");
	    } catch(Exception e) {
	        Log.e(tag, "Nie uda³o siê zapisaæ pliku SDP na karcie SD.");
	        e.printStackTrace();
	    }
	}

	@Override
	public void onClick(View v) { 
//		if(activity.getSelectedItem() == null)
//				return;
		
		try {
			execSSHCommand(lastRobotIp, "tokill=`ps aux | grep \"ffmpeg -f v4l2 -i /dev/video0 -r 10 -f rtp rtp://" +
					lastIp + ":1234\" | awk '{ print $2 }'`; kill -9 $tokill");
		} catch (JSchException e) {
			Log.w(tag, "Napotkano b³¹d podczas próby zamkniêcia ostatnio u¿ywanego strumienia.");
			e.printStackTrace();
		} catch (IOException e) {
			Log.w(tag, "Napotkano b³¹d podczas próby zamkniêcia ostatnio u¿ywanego strumienia.");
			e.printStackTrace();
		}
		
		new AsyncTask<String, Void, FFmpegFrameGrabber>() {

			@Override
			protected FFmpegFrameGrabber doInBackground(String... param) {
				Log.i(tag, "Uruchamianie strumieniowania wideo...");
				
		        try {
		    		WifiInfo wi = ((WifiManager) activity.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
		    	    byte[] bIp = BigInteger.valueOf(wi.getIpAddress()).toByteArray();
		    	    byte[] bIp2 = new byte[bIp.length];
		    	    for(int i = 0; i < bIp.length; i++)
		    	    	bIp2[bIp2.length - 1 - i] = bIp[i];
		    	    InetAddress ia = InetAddress.getByAddress(bIp2);
		    	    String ip = ia.getHostAddress();

		    		Log.i(tag, "IP urz¹dzenia: " + ip + 
		    				"\nIP robota: " + param[0] +
		    				"\nInicjowanie po³¹czenia SSH...");
					execSSHCommand(param[0], "echo " + password + " | sudo -S ffmpeg -f video4linux2 -i /dev/video0 -r 10 -f rtp rtp://" 
							+ ip + ":1234");
					lastIp = ip;
					lastRobotIp = param[0];
					
					Log.i(tag, "Przygotowywanie do odbierania strumienia...");
					grabber = FFmpegFrameGrabber.createDefault(new File(
							Environment.getExternalStorageDirectory().toString(), "stream.sdp"));
					Log.i(tag, "Oczekiwanie na po³¹czenie ze strumieniem wideo...");
					grabber.start();
					Log.i(tag, "Pomyœlnie po³¹czono ze strumieniem.");
				} catch (JSchException e) {
					Log.w(tag, "Nie uda³o siê uruchomiæ strumieniowania wideo na robocie.");
					e.printStackTrace();
				} catch(FrameGrabber.Exception e) {
					Log.w(tag, "Nie uda³o siê po³¹czyæ ze strumieniem wideo.");
					e.printStackTrace();
				} catch (UnknownHostException e) {
					Log.e(tag, "B³¹d podczas tworzenia strumienia wejœcia dla komendy zdalnej!");
					e.printStackTrace();
				} catch (IOException e) {
					Log.e(tag, "B³¹d podczas uzyskiwania adresu ip!");
					e.printStackTrace();
				}
				
				return grabber;
			}

			@Override
			protected void onPostExecute(FFmpegFrameGrabber grabber) {   
		        ((TaskDelegate) activity.getRenderThread()).streamActivationResult(grabber);
		    }
		}.execute("192.168.2.202");//activity.getSelectedItem().getIp());
	}
	
	private void execSSHCommand(String address, String command) throws JSchException, IOException {
		JSch jsch = new JSch();
		Session session = jsch.getSession(login, address, 22);
	    session.setPassword(password);
	    session.setConfig("StrictHostKeyChecking", "no");
	    session.connect(8000);
	    
	    ChannelExec channel = (ChannelExec) session.openChannel("exec");
//        InputStream is = channel.getInputStream();
		Log.i(tag, "Uruchamianie FFMPEG...");
		channel.setCommand(command);
        channel.connect();
        
//        byte[] tmp = new byte[1024];
//        while(true){
//		    while(is.available() > 0) {
//		    	if(is.read(tmp, 0, 1024) < 0)
//		    		break;
//		    	Log.i(tag, new String(tmp));
//		    }
//		    if(channel.isClosed()) {
//		    	if(is.available()>0) continue;
//		    	Log.i(tag, "exit-status: " + channel.getExitStatus());
//		    	break;
//		    }
//			try{Thread.sleep(1000);}catch(Exception ee){}
//    	} 

        channel.disconnect();
	    session.disconnect();
	}
}
