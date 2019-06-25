package com.ttstream.wowza.epg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.Timer;

import com.wowza.wms.logging.WMSLoggerFactory;

/*
 * This is the main business logic to prepare start record or stop record
 * Each RecoderController is for one live channel, 
 * one live channel maybe have many record task according the EPG
 * but we just start record for the nearest one
 */
public class RecoderController extends java.util.TimerTask  {

	private ArrayList<RecoderTask> rlist = new ArrayList<RecoderTask>();
	
	private RecoderTask rrt = null;
	
	private String filePath = "";
	
	private String fileName = "";
	
	private Boolean isStarted = false;
	
	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}
	
	public String getFilePath()
	{
		return this.filePath;
	}
	
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}
	
	public String getFileName()
	{
		return this.fileName;
	}
	
		
	public Boolean isStarted()
	{
		return this.isStarted;
	}
	
	public void reset()
	{
		this.isStarted = false;
	}
	public void reloadTask()
	{
        if (this.fileName =="") return;
      	/*
      	 * the file name base on {appName}_{streamName}, so that we can know which live channel should be recorded
      	 * you can change the file name format according your requirement
      	 */
		String app_stream = this.fileName.substring(0, fileName.lastIndexOf("."));
		String appName = app_stream.split("_")[0];
		String streamName = app_stream.split("_")[1];
		if (appName =="" || streamName =="")
		{	  
		      WMSLoggerFactory.getLogger(null).warn("recoder task file name is not blank ,but app name or stream name is blank , skip this file !");
		      return; 
		}
		    
		try
		{
		        FileInputStream fis = new FileInputStream(this.filePath + File.separator +fileName);
		        InputStreamReader isr=new InputStreamReader(fis, "UTF-8");
	            BufferedReader br = new BufferedReader(isr);
	        
	            String line="";
	            String[] arrs=null;
	            
	            while ((line=br.readLine())!=null) {
	                arrs=line.split(",");
	                /*
	                 * in this configure file, each line is a record task,
	                 * the first column is just a id 
	                 * the second column is start time in format "YYYYMMDDHHMM", for example 201906151030, the seconds is 00 in default
	                 * the third column is end time in format "YYYYMMDDHHMM", is same as the second column
	                 * the 4th column is just a name for this TV program
	                 * in this file, all the lins must sort by start time, and No overlap allowed
	                 */
	                WMSLoggerFactory.getLogger(null).info(arrs[0] + " : " + arrs[1] + " : " + arrs[2] +" : " + arrs[3]);
	                RecoderTask rt = new RecoderTask();
	                rt.setId(arrs[0]);
	                rt.setStartDate(arrs[1].substring(0,8));
	                rt.setStartTime(arrs[1].substring(8,12));
	                rt.setEndDate(arrs[2].substring(0,8));
	                rt.setEndTime(arrs[2].substring(8,12));
	                rt.setName(arrs[3]);
	                rt.setAppName(appName);
	                rt.setStreamName(streamName);
	                // this is file path,the recording file will be stored at here
	                rt.setOutPutPath(this.filePath + File.separator + appName + File.separator + streamName +File.separator);
	                rlist.add(rt);
	            }
	            br.close();
	            isr.close();
	            fis.close();
	            WMSLoggerFactory.getLogger(null).info("read recoder task from task file successed file name : "+fileName);
		 }catch (IOException e)
		 {
	        	WMSLoggerFactory.getLogger(null).error("read recode task from task file failed, file name : "+fileName);
	        	e.printStackTrace();
	        	return;
		 }
	}
	
	
	/*
	 * find the nearest record task
	 */
	public void findRecentlyTask()
	{
		if (rlist.size() == 0) return;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
		Date now = null ;
	  	String now_str = "";	  	
		
		for (int i=0;i<rlist.size();i++)
		{
			  now = new Date();
			  now_str = dateFormat.format(now);
			  
			  int end_result = (rlist.get(i).getEndDate()+rlist.get(i).getEndTime()).compareTo(now_str);
			
			  // find a new task
			  if (end_result > 0)
			  {
				  rrt = rlist.get(i);
				  break;  
			  }
	    }
	}
	
	/*
	 * if there are a nearest record task, add it to record schedule
	 */
	public void prepareRecoder()
	{
		if (rrt == null)
		{
			WMSLoggerFactory.getLogger(null).info("have no any new recode task in " +this.fileName +" !");
			return;
		}
		Timer timer = new Timer();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
		Date startRecoderDate = null;
		Date endRecoderDate = null;
		
		try
	  	{	
			  startRecoderDate =  dateFormat.parse(rrt.getStartDate()+rrt.getStartTime());
		      endRecoderDate = dateFormat.parse(rrt.getEndDate()+rrt.getEndTime());
		}
		catch(Exception e)
		{
			  WMSLoggerFactory.getLogger(null).error("parse startDateTime or endDateTime failed !");
			  e.printStackTrace();
			  return;
		}
		
		Date now = new Date();
		String now_str = dateFormat.format(now);
	  	
		int start_result = (rrt.getStartDate()+rrt.getStartTime()).compareTo(now_str);
		
		if (start_result <= 0)
	  	{
		   WMSLoggerFactory.getLogger(null).info("find a new recoder task , it need to start immediately ! app : "+rrt.getAppName() +" , stream : "+rrt.getStreamName() +"  program id : "+ rrt.getId());
		   startRecoderDate = now;
	  	}
		else
		{
			  WMSLoggerFactory.getLogger(null).info("find a new recoder task , it need to wait for start ! app : "+rrt.getAppName() +" , stream : "+rrt.getStreamName() +"  program id : "+ rrt.getId());
		}
		
		try
		{     
	    	this.isStarted = true ;  
			/*
			 * schedule two timer task, one for start record ,the other for stop record
			 */
	    	timer.schedule(rrt,startRecoderDate);
	    	
			RecoderStopTask rst= new RecoderStopTask();
	    	rst.setAppName(rrt.getAppName());
	    	rst.setStreamName(rrt.getStreamName());
			
	    	timer.schedule(rst,endRecoderDate);
	    	  
		    WMSLoggerFactory.getLogger(null).info("timer scheduled one live streaming recode task ,app:"+rrt.getAppName() +"  stream : "+rrt.getStreamName());
		}
		catch (Exception e)
		{
			  WMSLoggerFactory.getLogger(null).error(": Failed to recode for "+rrt.getAppName() +"  stream : "+rrt.getStreamName() +", program id : "+rrt.getId());
			  e.printStackTrace();
			  
		}
	}
	
	
	public void run()
	{
      	/*
      	 * 
      	 */
		if (this.isStarted){
      		return;
      	}
      	
		reloadTask();
      	findRecentlyTask();
		prepareRecoder();
	}
	
}
