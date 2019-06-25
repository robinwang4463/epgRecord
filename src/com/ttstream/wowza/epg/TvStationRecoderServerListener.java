package com.ttstream.wowza.epg;


import com.wowza.wms.logging.*;
import com.wowza.wms.server.*;
import java.util.Timer;

/*
 * This is a Server Listener of Wowza Streaming Engine, It will reload the epg record task configure file every 5 minutes
 */
public class TvStationRecoderServerListener implements IServerNotify2 {

		
	public void onServerConfigLoaded(IServer server) {
		WMSLoggerFactory.getLogger(null).info("onServerConfigLoaded");
	}

	public void onServerCreate(IServer server) {
		WMSLoggerFactory.getLogger(null).info("onServerCreate");
	}

	public void onServerInit(IServer server) {
		WMSLoggerFactory.getLogger(null).info("onServerInit");
		
		Timer timer = new Timer();
		CheckRecoderFileTask crft = new CheckRecoderFileTask();
		//you can adjust this time interval, 300000 means 5 minutes
		timer.schedule(crft,0,300000);
	}

	
	static class MyRecoderTask extends java.util.TimerTask{      
        public void run(){     
            System.out.println("________");     
        }     
    }   

	public void onServerShutdownStart(IServer server) {
		WMSLoggerFactory.getLogger(null).info("onServerShutdownStart");
	}

	public void onServerShutdownComplete(IServer server) {
		WMSLoggerFactory.getLogger(null).info("onServerShutdownComplete");
	}

}
