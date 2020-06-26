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

public class Reducer extends SimEntity implements MapReduceTask{
	public App app;	
	private BigDataTask reduceTask;
	private NodeManager nodeManager;
	
	private double reducerToStorageWorkload;	
	private long reduceMips; 
	
	private int assoicatedNumOfMappers;
	private int countAssoicatedNumOfMappers = 0;
	boolean isFinished = false;

	public Reducer(String name, App app, BigDataTask reduceTask) {
		super(name);		
		this.app = app;
		this.reduceTask = reduceTask;					
	}

	@Override
	public void startEntity() {
		Log.printLine(CloudSim.clock() + ": " + this.getName());		
	}

	public App getApp() {
		return this.app;
	}

	public void setApp(App app) {
		this.app = app;
	}
	
	@Override
	public void processEvent(SimEvent ev) {				
		switch (ev.getTag()) {
		case BigDataSDNSimTags.EXECUTE_REDUCE_TASK:								
			processExecuteReduceTask(ev);							
		break;
		}		
	}
	
	private void processExecuteReduceTask(SimEvent ev){					
		int dcId = (int) ev.getData();
		sendNow(dcId, CloudSimTags.CLOUDLET_SUBMIT, this.reduceTask);			
	}
		
	@Override
	public void shutdownEntity() {		
		Log.printLine(getName() + " is shutting down...");		
	}	
	
	public double getReducerToStorageWorkload() {
		return reducerToStorageWorkload;
	}

	public void setReducerToStorageWorkload(double reducerToStorageWorkload) {
		this.reducerToStorageWorkload = reducerToStorageWorkload;
	}

	public long getReduceMips() {
		return reduceMips;
	}

	public void setReduceMips(long reduceMips) {
		this.reduceMips = reduceMips;
	}
	
	public NodeManager getNodeManager() {
		return this.nodeManager;
	}

	public void setNodeManager(NodeManager nodeManager) {
		this.nodeManager = nodeManager;
	}
	
	public BigDataTask getReduceTaskCloudlet() {
		return reduceTask;
	}

	public void setReduceTaskCloudlet(BigDataTask reduceTask) {
		this.reduceTask = reduceTask;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	public void countMappers(int count){
		this.countAssoicatedNumOfMappers += count;
	}

	public int getAssoicatedNumOfMappers() {
		return assoicatedNumOfMappers;
	}

	public void setAssoicatedNumOfMappers(int num) {
		this.assoicatedNumOfMappers = num;
	}

	public int getCountAssoicatedNumOfMappers() {
		return countAssoicatedNumOfMappers;
	}

	public void setCountAssoicatedNumOfMappers(int countAssoicatedNumOfMappers) {
		this.countAssoicatedNumOfMappers = countAssoicatedNumOfMappers;
	}
		
	@Override
	public String getTaskName() {
		String name = this.getName();
		return name;
	}
	
	@Override
	public String getTaskType() {
		return "REDUCE";
	}
	
}

