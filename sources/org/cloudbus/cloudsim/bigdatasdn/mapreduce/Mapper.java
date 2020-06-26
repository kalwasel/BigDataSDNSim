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

package org.cloudbus.cloudsim.bigdatasdn.mapreduce;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.bigdatasdn.bdms.BigDataTask;
import org.cloudbus.cloudsim.bigdatasdn.bdms.BigDataSDNSimTags;
import org.cloudbus.cloudsim.bigdatasdn.bdms.NodeManager;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 * 
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */

public class Mapper extends SimEntity implements MapReduceTask{
	private App app;
	private BigDataTask mapTaskCloudlet;
	private NodeManager nodeManager;
	private double hdfsToMapperWorkload; 
	private double mapperToReducersWorkload;
	private long mapMips;
		
	public Mapper(String name, App app, BigDataTask mapTask) {
		super(name);		 
		this.app = app;
		this.mapTaskCloudlet = mapTask;	
	}

	public App getApp() {
		return app;
	}

	public void setApp(App app) {
		this.app = app;
	}

	@Override
	public void startEntity() {
		Log.printLine(CloudSim.clock() + ": " + this.getName() +   " entityId " + this.getId());		
	}

	@Override
	public void processEvent(SimEvent ev) {		
		switch (ev.getTag()) {		
			case BigDataSDNSimTags.EXECUTE_MAP_TASK:			
					processExecuteMapTask(ev);					
					break;
			case BigDataSDNSimTags.INTERNAL_MAPPER_HEART_BEAT:
				
				break;
	
			}
	}

	private void processExecuteMapTask(SimEvent ev) {		
		int dcId = (int) ev.getData();
		sendNow(dcId, CloudSimTags.CLOUDLET_SUBMIT, this.mapTaskCloudlet);		
	}

	@Override
	public void shutdownEntity() {
		Log.printLine(getName() + " is shutting down...");		
	}
	
	public void map(){
		
	}

	public double getProgress(){
	
		return 0;
	}		
	
	public NodeManager getNodeManager() {
		return this.nodeManager;
	}

	public void setNodeManager(NodeManager nodeManager) {
		this.nodeManager = nodeManager;
	}
	
	public double getHDFSToMapperWorkload() {
		return hdfsToMapperWorkload;
	}

	public void setHDFSToMapperWorkload(double hdfsToMapperWorkload) {
		this.hdfsToMapperWorkload = hdfsToMapperWorkload;
	}

	public double getMapperToReducersWorkload() {
		return mapperToReducersWorkload;
	}

	public void setMapperToReducersWorkload(long mapperToReducersWorkload) {
		this.mapperToReducersWorkload = mapperToReducersWorkload;
	}

	public long getMapMips() {
		return mapMips;
	}

	public void setMapMips(long mapMips) {
		this.mapMips = mapMips;
	}

	public BigDataTask getMapTaskCloudlet() {
		return mapTaskCloudlet;
	}

	public void setMapTaskCloudlet(BigDataTask mapTaskCloudlet) {
		this.mapTaskCloudlet = mapTaskCloudlet;
	}
	
	@Override
	public String getTaskName(){
		String name = this.getName();
		return name;
	}

	@Override
	public String getTaskType() {
		return "MAP";
	}

}