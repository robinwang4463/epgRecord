package com.ttstream.wowza.epg;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;

import com.wowza.wms.application.IApplication;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.livestreamrecord.manager.ILiveStreamRecordManager;
import com.wowza.wms.livestreamrecord.manager.IStreamRecorderConstants;
import com.wowza.wms.livestreamrecord.manager.StreamRecorderParameters;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.vhost.IVHost;
import com.wowza.wms.vhost.VHostSingleton;

/*
 * this is a timer task, it will call record manager API of Wowza Streaming Engine to start the record task
 */

public class RecoderTask extends java.util.TimerTask{

	private String id = "";
	private String name = "";
	private String startDate = "";
	private String startTime = "";
	private String endDate = "";
	private String endTime = "";
	private String fileName = "";
	
	private String appName = "";
	private String streamName = "";
	private String outPutPath = "";
	
	private Integer status = 0;
	
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
	
	public void setId(String id)
	{
		this.id = id;
	}
	public String getId()
	{
		return this.id;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getName()
	{
		return this.name;
	}
	public void setStartDate(String startDate)
	{
		this.startDate = startDate;
	}
	public String getStartDate()
	{
		return this.startDate;
	}
	
	public void setStartTime(String startTime)
	{
		this.startTime = startTime;
	}
	public String getStartTime()
	{
		return this.startTime;
	}
	public void setEndDate(String endDate)
	{
		this.endDate = endDate;
	}
	public String getEndDate()
	{
		return this.endDate;
	}
	public void setEndTime(String endTime)
	{
		this.endTime = endTime;
	}
	public String getEndTime()
	{
		return this.endTime;
	}
	
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}
	public String getFileName()
	{
		return this.fileName;
	}
	
	public void setOutPutPath(String outPutPath)
	{
		this.outPutPath = outPutPath;
	}
	public String getOutPutPath()
	{
		return this.outPutPath;
	}
	
	
	public Integer getStatus()
	{
		return this.status;
	}
	
	public void run()
	{
		IVHost vhost = null;
		IApplication app = null;
		IApplicationInstance appIns = null;
				
		final String CLASS_NAME = "RecoderTask";
		
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
				WMSLoggerFactory.getLogger(null).error(CLASS_NAME + ": Failed to get app :" + this.appName + " or it's default instance, recoder task can not run.");
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
			WMSLoggerFactory.getLogger(null).info("begain create directory, outPutPath : " +this.outPutPath);
			File op = new File(this.outPutPath);
			
			if (!op.exists() && !op.isDirectory())
			{
			      if (op.mkdirs())
				     WMSLoggerFactory.getLogger(null).info("create directory success !");
			      else
				     WMSLoggerFactory.getLogger(null).error("create directory fail !");
		    }
		 }	
		 catch (Exception e)
		 {
		      WMSLoggerFactory.getLogger(null).error("create directory faild ,please check the directory access right");
		      e.printStackTrace();
		      return;
		 }
		
			
		try
		{
		   StreamRecorderParameters recordParams = new StreamRecorderParameters(appIns);
		   
		   /*
		    * here are some setting of the record feature of Wowza Streaming Engine
		    */
		   recordParams.segmentationType = IStreamRecorderConstants.SEGMENT_NONE;
		   recordParams.fileFormat = IStreamRecorderConstants.FORMAT_MP4;
		   recordParams.startOnKeyFrame =  true;
		   recordParams.recordData = true;
		   recordParams.outputPath = this.outPutPath ;
		   recordParams.versioningOption = IStreamRecorderConstants.APPEND_FILE;
		   	     
		   SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		   Date now = new Date();
		   String now_str = dateFormat.format(now);
		
		   /*
		    * this is file name of recording file
		    */
		   recordParams.outputFile = this.getId() + "_" + now_str;
		   
		   WMSLoggerFactory.getLogger(null).info(CLASS_NAME + ": bagain to recoder a live stream ,app :" + this.appName + " ,  streamName : "+this.streamName+" , id : "+this.getId()+" !");
		   
		   ILiveStreamRecordManager lrm = vhost.getLiveStreamRecordManager();
		   
		   lrm.startRecording(appIns,this.streamName,recordParams);
		}
		catch(Exception e)
		{
			WMSLoggerFactory.getLogger(null).error(CLASS_NAME + ": Failed to recode live stream ,app :" + this.appName + " ,  streamName : "+this.streamName+" , id : "+this.getId()+" !", e);
			e.printStackTrace();
			return;	
		}
	}
}
