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

import java.util.ArrayList;
import java.util.List;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.MapReduceAM;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.Mapper;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.Reducer;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;


/**
 * 
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */

public class NodeManager extends SimEntity {	 	
	public MapReduceAM applicationMasterJobTracker;

	private List<Mapper> mapperList;
	private List<Reducer> reducerList;

	private Vm vm;

	public NodeManager(String name, int NodeManagerID) 
	{		
		super(name);
		this.mapperList = new ArrayList<Mapper>();
		this.reducerList = new ArrayList<Reducer>();
	}

	public int getNumMapReduceTasks(){
		int num = this.mapperList.size() + this.reducerList.size();
		return num;
	}

	public void addMapperToList(Mapper mapper){
		this.mapperList.add(mapper);
	}

	public List<Mapper> getMapperList(){
		return this.mapperList;
	}

	public void removeMapper(Mapper mapper){
		this.mapperList.remove(mapper);
	}

	public void addReducerToList(Reducer reducer){
		this.reducerList.add(reducer);
	}
	public List<Reducer> getReducerList(){
		return this.reducerList;
	}
	
	public void removeReducerToList(Reducer reducer){
		this.reducerList.remove(reducer);
	}
	
	public void setNodeManagerVm(Vm vm){
		this.vm = vm;
	}
	public Vm getNodeManagerVm(){
		return this.vm;
	}
	
	public int getVmID(){
		return this.vm.getId();
	}
		
	@Override
	public void startEntity() {
		Log.printLine(CloudSim.clock() + ": " + getName() + " is starting... " + this.getId());
	}

	@Override
	public void processEvent(SimEvent ev) 
	{
		switch (ev.getTag()) 
		{
			case BigDataSDNSimTags.NODE_MANAGER_INTERNAL_HEART_BEAT:				
				break;														
		}		
	}	
		
	@Override
	public void shutdownEntity() {
		Log.printLine(getName() + " is shutting down...");
	}	
}
