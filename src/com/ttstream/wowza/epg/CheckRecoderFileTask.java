package com.ttstream.wowza.epg;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.Set;
import java.util.HashSet;

import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.vhost.IVHost;
import com.wowza.wms.vhost.VHostSingleton;

/*
 * This is a TimerTask, it will reload epg record task file
 */
public class CheckRecoderFileTask extends java.util.TimerTask {
	
	private static Set<String> fileList = new HashSet<String>();
	private static ArrayList<RecoderController> rcList = new ArrayList<RecoderController>();
	private static ArrayList<Timer> timerList = new ArrayList<Timer>();
	
	public void run()
	{
        IVHost vhost = null;
		
		final String CLASS_NAME = "CheckRecoderFileTask";
		
		try
		{
			vhost = VHostSingleton.getInstance(IVHost.VHOST_DEFAULT);
			if(vhost == null)
			{
				WMSLoggerFactory.getLogger(null).warn(CLASS_NAME + ": Failed to get Vhost, can not check recode task file !");
				return;
			}
		}
		catch (Exception e)
		{
			WMSLoggerFactory.getLogger(null).error(CLASS_NAME + ": Failed to get Vhost, can not check recode task file !");
			e.printStackTrace();
			return;
		}
		
		String homePath = vhost.getHomePath();
		
		String CONTENT_BASE_PATH = homePath + File.separator +"content" ;
		
		
		try
	    {
	      File dir = new File(CONTENT_BASE_PATH);
	      File[] files = dir.listFiles();
	      for (int i=0;i<files.length;i++)
	      {
	    	  if (files[i].isFile())
	    	  {
	    		  String tmpFileName = files[i].getName();
	    	      String ext=tmpFileName.substring(tmpFileName.lastIndexOf(".")+1);   
	    	      
	    	      /*
	    	       * we suppose that the configure file end with extend name ".rtask"
	    	       * you can change it to match your requirement
	    	       * in this case, you can have many configure file, each one is just for one live channel
	    	       */
	    	      
	    		  if (ext.toLowerCase().equals("rtask"))
	    		  {
	    			  // 不会重复的，只有新的文件才会被添加
	    			  if (!fileList.contains(tmpFileName))
	    			  {
	    				  WMSLoggerFactory.getLogger(null).info("find a recoder task file : " + tmpFileName +" ,it will be read immediately !");
	    				  fileList.add(tmpFileName);
	    			  	  RecoderController rct = new RecoderController();
	    			      rct.setFileName(tmpFileName);
	    			      rct.setFilePath(CONTENT_BASE_PATH);
	    			      rcList.add(rct);
	    			      timerList.add(new Timer());
	    			      
	    			      //it is a timer task, we plan execute it every 5 minutes
	    			      timerList.get(timerList.size() -1).schedule(rct,0, 3000000);
	    			  }
	    		  }
	    	   }
	      }
	    }catch(Exception e)
	    {
	    	WMSLoggerFactory.getLogger(null).warn("failed to check rtask file ,stop check file!");
	    	e.printStackTrace();
	    	return;
	    }
	}
	
	public static RecoderController getRecoderController(String fileName)
	{
		RecoderController rc = null;
		
		for (int i=0;i<rcList.size();i++)
		{
		    if (rcList.get(i).getFileName().equals(fileName))
		    {
		    	rc = rcList.get(i);
		    	break;
		    }
		}
		return rc;
	}

}
