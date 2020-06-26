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

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.App;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.Mapper;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 * 
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */

public class HDFS extends SimEntity{

	private BigDataManagementSystem bdms;
	private int repliaction;
	private Vm vm;
	String name = "HDFS";
	DataPlacementPolicy placement;
	NodeManager nodeManager;

	public HDFS(BigDataManagementSystem bdms, DataPlacementPolicy placement){
		super("HDFS");
		this.bdms = bdms;
		this.placement = placement;
	}
	
	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processEvent(SimEvent ev) {
		int tag = ev.getTag();			
		switch(tag){
		case BigDataSDNSimTags.TRANSFER_BLOCKS:
		App app = (App) ev.getData();	
		transferHDFSDataToMappers(app);
		break;

		case BigDataSDNSimTags.Transmission_ACK:
			executeMapTasks((Flow) ev.getData());
			break;
			
		default: System.out.println(this.getName() + ": Unknown event received by "+ ev.getSource() +". Tag:"+ev.getTag());
		}
	}

	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub
		
	}	

	public int getRepliaction() {
		return repliaction;
	}

	public void setRepliaction(int repliaction) {
		this.repliaction = repliaction;
	}
	
	public Vm getVm() {
		return vm;
	}

	public void setVm(Vm vm) {
		this.vm = vm;
	}
	
	
	public NodeManager getNodeManager() {
		return nodeManager;
	}

	public void setNodeManager(NodeManager nodeManager) {
		this.nodeManager = nodeManager;
	}
	
	private void transferHDFSDataToMappers(App app){
		Log.printLine(CloudSim.clock() + ": " + this.name + " is dividing data of " + app.getAppName() + " into blocks");				
		Log.printLine(CloudSim.clock() + ": " + this.name + " is requesting the SDN controller to trasnfer data from HDFS to the mappers of " + app.getAppName());
		
		int vmID = vm.getId();		 
		List<Flow> flowList = placement.distributeDataBlocks(this.bdms.getId(), app, vmID, this);				
		for (Flow flow : flowList){
			sendNow(this.bdms.getSDNController().getId(), BigDataSDNSimTags.START_TRANSMISSION, flow);
		}							
	}

	private void executeMapTasks(Flow flow) {	
		App app = flow.getApp();
		app.setCountNumOfDataBlocks(1);
		System.out.println(this.getName() + ": number of counted blocks: " + app.getCountNumOfDataBlocks());
		if(app.getNumOfDataBlocks() == app.getCountNumOfDataBlocks()){			 			 
			List<Mapper> mapperList = app.getMapperList();
			for (Mapper mapper : mapperList){

				Log.printLine(CloudSim.clock()
						+ ": "
						+ getName()
						+ " is trying to execute " + mapper.getName() + " at VM " + mapper.getNodeManager().getVmID());

				flow.setAppName(mapper.getApp().getAppName());
				BigDataTask taskMap = mapper.getMapTaskCloudlet();
				Vm vm = taskMap.getNodeManager().getNodeManagerVm();
				taskMap.setVmId(vm.getId());
				taskMap.setHostId(vm.getHost().getId());				
				int tag = BigDataSDNSimTags.EXECUTE_MAP_TASK;			
				sendNow(mapper.getId(),tag, this.bdms.getDatacenter().getId());	
			}
		}
	}
		
	public DataPlacementPolicy getDataPlacementPolicy() {
		return placement;
	}

}
