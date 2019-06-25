package com.ttstream.wowza.epg;

import com.wowza.wms.application.IApplication;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.livestreamrecord.manager.ILiveStreamRecordManager;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.vhost.IVHost;
import com.wowza.wms.vhost.VHostSingleton;

public class RecoderStopTask  extends java.util.TimerTask {

	
	private String appName = "";
	private String streamName = "";
	
	public void setAppName(String appName)
	{
		this.appName = appName;
	}
	public String getAppName()
	{
		return this.appName;
	}
	
	public void setStreamName(String streamName)
	{
		this.streamName = streamName;
	}
	
	public String getStreamName()
	{
		return this.streamName;
	}

	
	public void run()
	{
		IVHost vhost = null;
		IApplication app = null;
		IApplicationInstance appIns = null;
			
		final String CLASS_NAME = "RecoderStopTask";
		
		try
		{
			vhost = VHostSingleton.getInstance(IVHost.VHOST_DEFAULT);
			if(vhost == null)
			{
				WMSLoggerFactory.getLogger(null).warn(CLASS_NAME + ": Failed to get Vhost, recoder task can not run.");
				return;
			}
		}
		catch (Exception e)
		{
			WMSLoggerFactory.getLogger(null).error(CLASS_NAME + ": Failed to get Vhost, recoder task can not run.", e);
			e.printStackTrace();
			return;
		}
		
		try
		{
			app = vhost.getApplication(this.appName);
			appIns = app.getAppInstance(IApplicationInstance.DEFAULT_APPINSTANCE_NAME);
			if ((app == null)||(appIns == null))
			{
				WMSLoggerFactory.getLogger(null).warn(CLASS_NAME + ": Failed to get app :" + this.appName + " or it's default instance, recoder task can not run.");
				return;
			}
			
		}
		catch (Exception e)
		{
			WMSLoggerFactory.getLogger(null).error(CLASS_NAME + ": Failed to get app :" + this.appName + " or it's default instance, recoder task can not run.", e);
			e.printStackTrace();
			return;	
		}
		
		try
		{
		  ILiveStreamRecordManager lrm = vhost.getLiveStreamRecordManager();
		  lrm.stopRecording(appIns,this.streamName);
		  WMSLoggerFactory.getLogger(null).info(CLASS_NAME +" stop live streaming recoding successed, app : "+this.appName +" , streamName : "+ this.streamName +" !");	
		}
		catch(Exception e)
		{
			WMSLoggerFactory.getLogger(null).error(CLASS_NAME +" stop live streaming recoding failed, app : "+this.appName +" , streamName : "+ this.streamName +" !");	
		    e.printStackTrace();
		    return;
		}
		String fileName = this.appName + "_" + this.streamName+".rtask";
		try
		{
		    RecoderController  rc = CheckRecoderFileTask.getRecoderController(fileName);
		    /*
		     * let recoderController reset when current record task was stopped 
		     */
		    rc.reset();
		}
		catch(Exception e)
		{
			WMSLoggerFactory.getLogger(null).error(CLASS_NAME +" meet a error when reset recoderController : " +fileName +" !" ,e);
			e.printStackTrace();
		}
		
		/*
		
		String fileName = this.appName + "_" + this.streamName+".rtask";
		try
		{
		    RecoderController  rc = CheckRecoderFileTask.getRecoderController(fileName);
		    rc.reloadTask();
		    rc.findRecentlyTask();
		    rc.prepareRecoder();
		}catch(Exception e)
		{
			WMSLoggerFactory.getLogger(null).error(CLASS_NAME +" meet a error ,when prepare recoding next program !");
			e.printStackTrace();
		}
		*/
	}
}
