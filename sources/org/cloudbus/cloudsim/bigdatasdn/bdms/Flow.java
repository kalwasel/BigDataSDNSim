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

import org.cloudbus.cloudsim.bigdatasdn.mapreduce.App;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.MapReduceAM;

/**
 *  
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */

public class Flow {
	private String appName;
	MapReduceAM am;
	int flowId;	
	int source ; // --> mapper, reducer, HDFS, etc. 
	int destination; // --> mapper, reducer, VM, etc.	
	double flowSize;
	double amountToBeProcessed; 
	private double submitTime = 0;
	private double startTime = 0;
	private double finishTime = 0;		 
	private String appNameSrc;   
	private String appNameDest;	
	private NodeManager nm;		
	private String flowType; 	
	private boolean isScheduled = false;	
	int appId;
	private int appPriority; 	
	private int ackEntity; // used to notify the respective destination of packet's completion -- khaled	
	public boolean hasRoute = false; 
		
	public Flow(MapReduceAM am, String vmNameSrc, String vmNameDest, int source, int destination, double flowSize, int flowId, String flowType) {
		this.am = am;
		this.appNameSrc = vmNameSrc;
		this.appNameDest = vmNameDest;
		this.amountToBeProcessed = flowSize;
		this.source = source;
		this.destination = destination;
		this.flowSize = flowSize;
		this.flowId = flowId;	
		this.flowType = flowType;
	}	

	public String getFlowType() {
		return flowType;
	}

	public int getAckEntity() {
		return ackEntity;
	}

	public void setAckEntity(int ackEntity) {
		this.ackEntity = ackEntity;
	}

	public int getAppPriority() {
		return appPriority;
	}

	public void setAppPriority(int appPriority) {
		this.appPriority = appPriority;
	}

	public long requestedBW =0;
	private App app;
	
	public App getApp() {
		return app;
	}

	public void setApp(App app) {
		this.app = app;
	}
	
	public boolean isScheduled() {
		return isScheduled;
	}

	public void setIsScheduled(boolean isScheduled) {
		this.isScheduled = isScheduled;
	}

	public void setAppId(int appId){
		this.appId = appId;
	}
	public int getAppId(){
		return this.appId;
	}

	public void setAppName(String appName){
		this.appName = appName;
	}
	public String getAppName(){
		return this.appName;
	}
	
	public int getOrigin() {
		return source;
	}

	public int getDestination() {
		return destination;
	}

	public double getSize() {
		return flowSize;
	}
	
	public void setFlowSize(double size) {
		this.flowSize = size;
	}

	public int getFlowId() {
		return flowId;
	}
	
	public void setFlowId(int flowId) {
		this.flowId = flowId;
	}
	
	public void setStartTime(double time) {
		this.startTime = time;
	}
	
	public void setFinishTime(double time) {
		this.finishTime = time;	
	}
	
	public double getStartTime() {
		return this.startTime;
	}
	public double getFinishTime() {
		return this.finishTime;
	}


	public String getAppNameSrc(){
		return this.appNameSrc;	
	}

	public String getAppNameDest(){
		return this.appNameDest;
	}

	public void setNodeManager(NodeManager nm)
	{
		 this.nm = nm;
	}
	
	public NodeManager getNodeManager()
	{
		return this.nm;
	}
	
	public MapReduceAM getAm() {
		return am;
	}
		
	public double getAmountToBeProcessed() {
		return this.amountToBeProcessed;
	}

	public void addCompletedLength(long completed){
		amountToBeProcessed = amountToBeProcessed - completed;
		if (amountToBeProcessed <= 0){
			amountToBeProcessed = 0;
		}
	}
	
	public boolean isCompleted(){
		return amountToBeProcessed == 0;
	}
	
	public double getSubmitTime() {
		return submitTime;
	}

	public void setSubmitTime(double submitTime) {
		this.submitTime = submitTime;
	}
}
