/*
 * Title:        BigDataSDNSim 1.0
 * Description:  BigDataSDNSim enables the simulating of MapReduce, big data management systems (YARN), 
 * 				 and software-defined networking (SDN) within cloud environments.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2020, Newcastle University (UK) and Saudi Electronic University (Saudi Arabia) 
 * 
 */

package org.cloudbus.cloudsim.bigdatasdn.bdms;

import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.App;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.MapReduceAM;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.MapReduceTask;

/**
 * 
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */

public class BigDataTask extends Cloudlet {
	
	private String appName;	
	private int appId;		
	private NodeManager nm;
	private MapReduceTask mapReduceTask;
	private App app;
	private int CPUNo;

	public BigDataTask(int appSplitId, int CPUNo, double appSplitMILength, int pesNumber,
			double appSplitFileSize, double MapTaskOutputFileSize,
			UtilizationModel utilizationModelCpu,
			UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw,boolean record, List<String> fileNameList, 
			String appName, int appId, int taskType) {
		
		super(appSplitId, appSplitMILength, pesNumber, appSplitFileSize,
				MapTaskOutputFileSize, utilizationModelCpu, utilizationModelRam,
				utilizationModelBw,true,fileNameList);	
		
		this.CPUNo = CPUNo;
		this.appName = appName;
		this.appId = appId;
     }

	public int getCPUNo() {
		return CPUNo;
	}
	
	public App getApp() {
		return app;
	}

	public void setApp(App app) {
		this.app = app;
	}

	private MapReduceAM MapReduceAM;
	
	
	public MapReduceAM getMapReduceAM() {
		return MapReduceAM;
	}

	public void setMapReduceAM(MapReduceAM mapReduceAM) {
		MapReduceAM = mapReduceAM;
	}

	public NodeManager getNodeManager() {
		return nm;
	}

	public void setNodeManager(NodeManager nm) {
		this.nm = nm;
	}

	public String getAppName()
	{
		return appName;
	}	
	
	public int getAppId()
	{
		return appId;		
	}		
	public void setMapReduceTask(MapReduceTask mapReduceTask) {
		this.mapReduceTask = mapReduceTask;
	}
	
	public MapReduceTask getMapReduceTask() {
		return this.mapReduceTask; 
	}
	
}
	