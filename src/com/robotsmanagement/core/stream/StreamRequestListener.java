package com.robotsmanagement.core.stream;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.VideoView;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.robotsmanagement.R;
import com.robotsmanagement.core.MainActivity;

public class StreamRequestListener implements OnClickListener {
	
	private static final String tag = StreamRequestListener.class.getName();
	private static final String login = "lorens";
	private static final String password = "konik001";
	private static final String videoUrl = "rtsp://%s:8554/stream";
	private static final String killCommand = "kill `cat stream_pid | awk '{print $1}'`";
	private static final String command = "nohup cvlc v4l2:///dev/video0 "
			+ ":v4l2-standard= :live-caching=300 :sout=\"#transcode{vcodec=h264,fps=12,"
			+ "scale=Automatycznie,width=176,height=144,acodec=none}:rtp{sdp=rtsp://:8554/stream}\" "
			+ ":sout-keep > /dev/null 2>&1 & echo $! > stream_pid";

	private final MainActivity activity;
	private boolean isStreamRunning;
	private final VideoView videoView; 
	
	public StreamRequestListener(MainActivity activity) {
		this.activity = activity;
		this.isStreamRunning = false;
		videoView = (VideoView) activity.findViewById(R.id.video_view);
	}

	@Override
	public void onClick(View v) {
		if(activity.getSelectedItem() == null)
				return;
		
		if(isStreamRunning) {
			videoView.setVisibility(View.VISIBLE);
			(new AsyncTask<String, Void, Void>() {

				@Override
				protected Void doInBackground(String... param) {
			        try {
			    		Log.i(tag, "IP urz¹dzenia: " + getIp() + "\nIP robota: " + param[0] +
			    				"\nInicjowanie po³¹czenia SSH..." + "\n" + command);
						execSSHCommand(param[0], param[1]);
					} catch(JSchException e) {
						e.printStackTrace();
					} catch(UnknownHostException e) {
						e.printStackTrace();
					} catch(IOException e) {
						e.printStackTrace();
					}
			        
					return null;
				}
			}).execute(activity.getSelectedItem().getIp(), killCommand);
			activity.stopVideo();
			activity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					videoView.setVisibility(View.GONE);
				}
			});
			isStreamRunning = false;
			return;
		}
		
		(new AsyncTask<String, Void, Void>() {

			@Override
			protected Void doInBackground(String... param) {
		        try {
		    		Log.i(tag, "IP urz¹dzenia: " + getIp() + "\nIP robota: " + param[0] +
		    				"\nInicjowanie po³¹czenia SSH..." + "\n" + command);
					execSSHCommand(param[0], param[1]);
					isStreamRunning = true;
					activity.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							videoView.setVisibility(View.VISIBLE);
						}
					});
					
				} catch(JSchException e) {
					e.printStackTrace();
				} catch(UnknownHostException e) {
					e.printStackTrace();
				} catch(IOException e) {
					e.printStackTrace();
				}
		        
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				
				// opoznione uruchamianie odtwarzania
				new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						return null;
					}
					
					@Override
					protected void onPostExecute(Void result) {
						activity.playVideo(videoUrl);
					}
					
				}.execute();
			}
		}).execute(activity.getSelectedItem().getIp(), command);
		
	}
	
	private void execSSHCommand(String address, String command) throws JSchException, IOException {
		JSch jsch = new JSch();
		Session session = jsch.getSession(login, address, 22);
	    session.setPassword(password);
	    session.setConfig("StrictHostKeyChecking", "no");
	    session.connect(3000);
	    
	    ChannelExec channel = (ChannelExec) session.openChannel("exec");
		channel.setCommand(command);
        channel.connect();
        channel.disconnect();
	    session.disconnect();
	}
	
	private String getIp() {
		String ip = "";
		
		try {
			WifiInfo wi = ((WifiManager) activity.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
			byte[] bIp = BigInteger.valueOf(wi.getIpAddress()).toByteArray();
			byte[] bIp2 = new byte[bIp.length];
			for(int i = 0; i < bIp.length; i++)
				bIp2[bIp2.length - 1 - i] = bIp[i];
			InetAddress ia = InetAddress.getByAddress(bIp2);
			ip = ia.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		return ip;
	}
}
