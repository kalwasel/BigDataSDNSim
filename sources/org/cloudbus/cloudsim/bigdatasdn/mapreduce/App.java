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

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.bigdatasdn.bdms.BigDataTask;
import org.cloudbus.cloudsim.bigdatasdn.bdms.Flow;
import org.cloudbus.cloudsim.bigdatasdn.bdms.NetworkStatistics;





/**
 * 
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */


public class App {	
	private String appName;
	private MapReduceAM mpAM;
	private int numOfDataBlocks;
	private int countNumOfDataBlocks = 0;	
	private int numberOfMapTask;
	private int numberOfReduceTask;			
	private double hdfsBlockSize;		
	private int appId;	
	private double appMipsTotalLength;	// map mips + reducer mips 
	private double originalAppDataSize; // with no replication
	private double alterredAppDataSize; // with replication		
	private List<Mapper> mapperList;
	private List<Reducer> reducerList;	
	private double submitTime;	
	private double startTime = -1;
	private double endTime;	

	private int CPUno; // number of CPUs required by each mapper and reducer 

	private double hdfsToMapperWorkload; // convert to bit * 8
	private double mapperToReducersWorkload; // convert to bit * 8
	private double reducerToHDFSWorkload; // convert to bit * 8
	private long mapMips;
	private long reduceMips; 

	private String appType; // MapReduce, Stream, etc.  
	private int appPriority; // 0 = no priority, 1 = low, 2 = medium, 3 = high			

	private NetworkStatistics networkStatistics;	 
	private int countFlows = 0;
	private int replicaNum;
	private int countReducerFlows = 0;
	protected List<Flow> finalReducerData = new ArrayList<>(); // final output of reducers 
	
	
	public List<Flow> getFinalReducerData() {
		return finalReducerData;
	}

	public void addFinalReducerData(Flow flow) {
		this.finalReducerData.add(flow);
	}

	public int getCountReducerFlows() {
		return countReducerFlows;
	}

	public void countReducerFlows(int countReducerFlows) {
		this.countReducerFlows += countReducerFlows;
	}

	public App(int appId, int replication, String appType, double appSubmitTime,
			int CPUno, 
			double mapInputBytes,
			long mapMips, 
			double suffleBytes,
			long reduceMips,
			double reduceOutputBytes,			
			int numberOfMappers,
			int numberOfReducers, 
			int priority,
			double hdfsBlockSize){
				
		this.appId = appId;
		this.replicaNum = replication;
		this.appType = appType;
		this.appName = "app_" + appId;
		this.submitTime = appSubmitTime;
		this.CPUno = CPUno;
		this.hdfsToMapperWorkload = mapInputBytes;		
		this.mapMips = mapMips; 				
		this.mapperToReducersWorkload = suffleBytes;
		this.reduceMips = reduceMips;
		this.reducerToHDFSWorkload = reduceOutputBytes;
		this.numberOfMapTask = numberOfMappers;
		this.numberOfReduceTask = numberOfReducers;
		this.appPriority  = priority;
		this.hdfsBlockSize = hdfsBlockSize;		
		this.mapperList = new ArrayList<Mapper>();
		this.reducerList = new ArrayList<Reducer>();		
		this.originalAppDataSize = this.hdfsToMapperWorkload + this.mapperToReducersWorkload + 	this.reducerToHDFSWorkload;
		this.appMipsTotalLength = this.mapMips + this.reduceMips;
		
		networkStatistics = new NetworkStatistics();
	 }
	
	public int getAppPriority() {
		return appPriority;
	}

	public int getReplicaNum() {
		return replicaNum;
	}

	
	public void setMapReduceAM(MapReduceAM mpAM){
		this.mpAM = mpAM;
	}	
	
	public double getAppStartTime() {
		return startTime;
	}
	public void setAppStartTime(double startTime) {
		this.startTime = startTime;
	}
	
	public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}	

	public double getReduceMips() {
		return reduceMips;
	}

	public void setReduceMips(long reduceMips) {
		this.reduceMips = reduceMips;
	}
	
	public double getMapMips() {
		return mapMips;
	}

	public void setMapMips(long mapMips) {
		this.mapMips = mapMips;
	}

	public double getAppSubmitTime() {
		return submitTime;
	}
	
	public double getEndTime() {
		return endTime;
	}

	public void setEndTime(double endTime) {
		this.endTime = endTime;
	}
	
	public double getHDFSToMapperNetworkWorload(){
		return this.hdfsToMapperWorkload;
	}	
	public double getMappertoReducerNetworkWorload(){
		return this.mapperToReducersWorkload;
	}	
	public double getReducerToHDFSNetworkWorload(){
		return this.reducerToHDFSWorkload;
	}		

	public void addMapperToList(Mapper mapper){
		this.mapperList.add(mapper);
	}

	public List<Mapper> getMapperList(){
		return this.mapperList;
	}

	public void addReducerToList(Reducer reducer){
		this.reducerList.add(reducer);
	}
	
	public List<Reducer> getReducerList(){
		return this.reducerList;
	}	
	
	public Reducer getReducerByName(String name){
		Reducer reducer = null;
		for(Reducer rd : this.getReducerList()){
			if(rd.getName().equals(name)){
				reducer = rd;
			}
		}
		return reducer;
	}	
	
	public int getNumberofMapTask(){
		return numberOfMapTask;
	}

	public void setNumberofMapTask(int numberofMapTask){
		this.numberOfMapTask = numberofMapTask;
	}

	public int getNumberOfReduceTask(){
		return numberOfReduceTask;
	}

	public void setNumberOfReduceTask(int numberOfReduceTask){
		this.numberOfReduceTask = numberOfReduceTask;
	}

	public double getHdfsBlockSize(){
		return this.hdfsBlockSize;
	}
	
	public void setHdfsBlockSize(double newSize){
		this.hdfsBlockSize = newSize;
	}

	public int getAppId(){
		return appId;
	}

	public void setAppId(int appId){
		this.appId = appId;
	}

	public String getAppName(){
		return appName;
	}

	public void setAppName(String appName){
		this.appName = appName;
	}

	public double getAppMipsTotalLength(){
		return appMipsTotalLength;
	}

	public void setAppMipsTotalLength(double appMITotalLength){
		this.appMipsTotalLength = appMITotalLength;
	}
	
	public String appToString(){
		return appId + " " + appName;
	}
	
	public int getNumOfDataBlocks() {
		return numOfDataBlocks;
	}
	public void setNumOfDataBlocks(int numOfDataBlocks) {
		this.numOfDataBlocks = numOfDataBlocks;
	}
	
	public int getCountNumOfDataBlocks() {
		return countNumOfDataBlocks;
	}
	public void setCountNumOfDataBlocks(int countNumOfDataBlocks) {
		this.countNumOfDataBlocks += countNumOfDataBlocks;
	}
	
	public void setAppTotalFileSize(double originalAppDataSize){
		this.originalAppDataSize = originalAppDataSize;
	}
	public double getAppTotalFileSize(){
		return this.originalAppDataSize;
	}	
	
	public double getAlterredAppDataSize() {
		return alterredAppDataSize;
	}
	public void setAlterredAppDataSize(double alterredAppDataSize) {
		this.alterredAppDataSize = alterredAppDataSize;
	}

	public void setHdfsToMapperWorkload(double hdfsToMapperWorkload) {
		this.hdfsToMapperWorkload = hdfsToMapperWorkload;
	}

	public void setReducerToHDFSWorkload(double reducerToHDFSWorkload) {
		this.reducerToHDFSWorkload = reducerToHDFSWorkload;
	}
	
	public MapReduceAM getMpAM() {
		return mpAM;
	}
		
	public int getCPUno() {
		return CPUno;
	}
	
	public NetworkStatistics getNetworkStatistics() {
		return networkStatistics;
	}
	
	public double getFirstMapperFinishTime(){
		double minTime = Integer.MAX_VALUE;
		double currentTime;
		for (Mapper mp : this.mapperList){
			BigDataTask task = mp.getMapTaskCloudlet();	
			currentTime = task.getFinishTime();
			if(currentTime < minTime){
				minTime = currentTime;
			}
		}
			return minTime;
		}		
	
	public double getFirstReducerFinishTime(){
		double minTime = Integer.MAX_VALUE;
		double currentTime;
		for (Reducer rd : this.reducerList){
			BigDataTask task = rd.getReduceTaskCloudlet();	
			currentTime = task.getFinishTime();
			if(currentTime < minTime){
				minTime = currentTime;
			}
		}
			return minTime;
		}	
	
	public int getCountFlows() {
		return countFlows;
	}

	public void setCountFlows(int countFlows) {
		this.countFlows += countFlows;
	}
	
}
