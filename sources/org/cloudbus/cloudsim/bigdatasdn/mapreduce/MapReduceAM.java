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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.bigdatasdn.bdms.ApplicationMaster;
import org.cloudbus.cloudsim.bigdatasdn.bdms.BigDataDatacenter;
import org.cloudbus.cloudsim.bigdatasdn.bdms.BigDataManagementSystem;
import org.cloudbus.cloudsim.bigdatasdn.bdms.BigDataTask;
import org.cloudbus.cloudsim.bigdatasdn.bdms.BigDataSDNSimTags;
import org.cloudbus.cloudsim.bigdatasdn.bdms.DataPlacementPolicy;
import org.cloudbus.cloudsim.bigdatasdn.bdms.Flow;
import org.cloudbus.cloudsim.bigdatasdn.bdms.NodeManager;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.sdn.NetworkOperatingSystem;

/**
 * 
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */

public class MapReduceAM extends ApplicationMaster {
		
	private int countRdFlows = 0;
	private int countRdFinalFlows = 0;
	public BigDataDatacenter datacenter; 
	private BigDataManagementSystem bdms;

	protected List<? extends Cloudlet> cloudletReceivedList;  
	private Map<NodeManager, Vm> NodeManagerVm =  new HashMap<NodeManager, Vm>();
	private Map<Vm, NodeManager> VmNodeManager = new HashMap<Vm, NodeManager>();
	private App mapReduceApp;
	
	
	public MapReduceAM(String name, int id, BigDataManagementSystem bdms,
			BigDataDatacenter dc, NetworkOperatingSystem nos, App mapReduceApp) {
				super(name);				
				datacenter =  dc;
				this.bdms = bdms;							
				this.cloudletReceivedList = new ArrayList<Cloudlet>();
				this.mapReduceApp = mapReduceApp;
	}	
	
	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag())
		{				
			case BigDataSDNSimTags.MAPPER_FINISHED_EXECUTION:
				BigDataTask mapTask = (BigDataTask) ev.getData();
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + mapTask.getCloudletId() + " received");		
				addCloudletToReceivedList(mapTask);
				transferMapperOutputToReducers(mapTask);
				break;					
			case BigDataSDNSimTags.Transmission_ACK: 				
				Flow flow = (Flow) ev.getData();
				processReceivedData(flow);
				break;		
			case BigDataSDNSimTags.REDUCER_FINISHED_EXECUTION:
				BigDataTask reduceTask = (BigDataTask) ev.getData();
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + reduceTask.getCloudletId() + " received");
				addCloudletToReceivedList(reduceTask);
				transferDataFromReducerToHDFS(reduceTask);
				break;										
		}

	}
	
	public NodeManager getNodeManagerByVm(Vm vm){
		return this.VmNodeManager.get(vm);
	}
	
	public Vm getVmByNodeManager(NodeManager tr){
		return this.NodeManagerVm.get(tr);
	}
	
	private void processReceivedData(Flow pkt){
		String pktType = pkt.getFlowType();
		switch(pktType){
		case "reducer":
			executeReduceTasks(pkt);
			break;
			
		case "hdfs":		
			checkAppCompletion(pkt);
			break; 		
		
		case "reducerToMainVM":		
			checkFinalCompletion(pkt);
			break; 
		}

	}
	
	private void checkFinalCompletion(Flow flowRd){
		countRdFinalFlows++;
		int  crp = this.mapReduceApp.getFinalReducerData().size();
		if(countRdFinalFlows == crp){
			flowRd.getApp().setEndTime(CloudSim.clock);
			// notify BDMS			
			sendNow(this.bdms.getId(), BigDataSDNSimTags.MAP_REDUCE_APP_FINISHED);
		}
	}
	
	private void checkAppCompletion(Flow flowRd){
		App app = flowRd.getApp();
		countRdFlows++;		
		boolean checkReducerStatus = true;
		for (Reducer rd : app.getReducerList()){
			if(!rd.isFinished){
				checkReducerStatus = false;
				break;
			}
		}

		int  crp = this.mapReduceApp.getCountReducerFlows();
		if(checkReducerStatus == true &&  crp == countRdFlows){
			// app is finished
			transferFinalReducerData(flowRd.getApp());			
		}
		
	}
	
	private void transferFinalReducerData(App app){			
		List<Flow> pktList = app.getFinalReducerData();			
		// Wait for all reducers to finish before sending their data 
		if(pktList.size() == app.getNumberOfReduceTask()){					
			for (Flow pkt : pktList){
				sendNow(this.bdms.getSDNController().getId(), BigDataSDNSimTags.START_TRANSMISSION, pkt);
			}
		}
	}
	private void transferDataFromReducerToHDFS(BigDataTask task){
		Reducer rd = (Reducer) task.getMapReduceTask();
		
		Log.printLine(CloudSim.clock() + ": " + getName() + " is requesting the SDN controller to trasnfer data from " +
				rd.getName() + " (Cloudlet " + rd.getReduceTaskCloudlet().getCloudletId() + ")" + " to " + "HDFS");

		DataPlacementPolicy placement = this.bdms.getHdfs().getDataPlacementPolicy();			
		List<Flow> pktList = placement.distributeReducerOutput(this.bdms.getId(), task.getApp(), rd, this.bdms.getHdfs());	
		if(pktList == null){
			/*
			 * There is no other reducer to transfer data to, send the final output of every reducer to the main VM (HDFS) 
			 */
			transferFinalReducerData(rd.getApp());
		} else {
			for (Flow pkt : pktList){
				sendNow(this.bdms.getSDNController().getId(), BigDataSDNSimTags.START_TRANSMISSION, pkt);
			}
		}								
	}
		
	private void executeReduceTasks(Flow flow) {		
		String reducerName = flow.getAppNameDest(); 
		App app = flow.getApp();		
		Reducer reducer = app.getReducerByName(reducerName);
		if(reducer != null){
			reducer.countMappers(1);
			if(reducer.getAssoicatedNumOfMappers() == reducer.getCountAssoicatedNumOfMappers()){
				Log.printLine(CloudSim.clock()
						+ ": "
						+ getName()
						+ " intermediate data received from mappers." + this.getName() + " is trying to execute " + reducer.getName());
				flow.setAppName(reducer.getApp().getAppName());
				BigDataTask reduceMap = reducer.getReduceTaskCloudlet();
				Vm vm = reduceMap.getNodeManager().getNodeManagerVm();
				reduceMap.setVmId(vm.getId());
				reduceMap.setHostId(vm.getHost().getId());				
				int tag = BigDataSDNSimTags.EXECUTE_REDUCE_TASK;			
				sendNow(reducer.getId(),tag, this.datacenter.getId());									
			}
		} else{
			Log.printLine(getName() + " could not find  reducers for the recieved package! ");
		}	
	}	
	
	private void transferMapperOutputToReducers(BigDataTask task){
					
		Log.printLine(CloudSim.clock() + ": " + this.getName() + " is requesting the SDN controller to trasnfer data from mappers to reducers");
			DataPlacementPolicy placement = this.bdms.getHdfs().getDataPlacementPolicy();			
			List<Flow> pktList = placement.distributeMapperOutput(this.bdms.getId(), task.getApp(), task);		
			for (Flow pkt : pktList){
				sendNow(this.bdms.getSDNController().getId(), BigDataSDNSimTags.START_TRANSMISSION, pkt);
			}				
	}		

	public BigDataManagementSystem getBdms() {
		return bdms;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletReceivedList() {
		return (List<T>) cloudletReceivedList;
	}
	
	protected void addCloudletToReceivedList(BigDataTask bigDataTask) {
		getCloudletReceivedList().add(bigDataTask);
	}
 
	public List<NodeManager> getNodeManagerList(){		
		return this.NodeManagerList;
	}
	
	@Override
	public void startEntity() {
		Log.printLine(CloudSim.clock() + ": " + getName() + " is starting... " + this.getId());			
	}
	
	@Override
	public void shutdownEntity() {
		Log.printLine(getName() + " is shutting down...");
	}
}
