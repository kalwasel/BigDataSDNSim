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

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.bigdatasdn.bdms.polocies.MapReduceAppSelectionPolicy;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.App;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.AppsParser;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.MapReduceAM;

/**
 *
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */

public class BigDataManagementSystem extends DatacenterBroker {
	
	private MapReduceAppSelectionPolicy mrAppSelectionPolicy;
	private SDNController sdnController;	
	private BigDataDatacenter datacenter;	
	private static int applicationMasterIndexing = 1; // keep incrementing 
	public VMManager vmManager;
	private List<MapReduceAM> applicationMastersList;
	private String appFileName = null; 
	private HDFS hdfs;	
	private  List<Vm> vmList = new ArrayList<Vm>();
	private List<NodeManager> NodeManagerList;  


	public BigDataManagementSystem(String name, SDNController sdnController, MapReduceAppSelectionPolicy mrAppSelectionPolicy,
			DataPlacementPolicy dataPlacementPolicy) throws Exception {
		super(name);		
		hdfs = new HDFS(this, dataPlacementPolicy);
		this.sdnController = sdnController;		
		NodeManagerList  = new ArrayList<NodeManager>();
		setMrAppSelectionPolicy(mrAppSelectionPolicy);	
		applicationMastersList = new ArrayList<MapReduceAM>(); 		
	}
	

	@Override
	public void processEvent(SimEvent ev) {
		int tag = ev.getTag();			
		switch(tag){
		case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
			processResourceCharacteristicsRequest(ev);
			break;
		// Resource characteristics answer
		case CloudSimTags.RESOURCE_CHARACTERISTICS:
			processResourceCharacteristics(ev);
	
			send(this.getId(),CloudSim.getMinTimeBetweenEvents(), BigDataSDNSimTags.MAP_REDUCE_CREATE, this.appFileName);
			break;
			
		case CloudSimTags.VM_CREATE_ACK: 										
				processVmCreate(ev);	
				break;

		case BigDataSDNSimTags.MAP_REDUCE_CREATE:
			createNodeManager();
			createdMapReduceApps();
			break;	

		case CloudSimTags.CLOUDLET_RETURN:
			processCloudletReturn(ev);
			break;	
			
		case BigDataSDNSimTags.MAP_REDUCE_APP_FINISHED:
			checkOtherApps();
			break;				
			
		default: System.out.println(this.getName() + ": Unknown event received by "+ ev.getSource() +". Tag:"+ev.getTag());
		}
	}					
	
	private void checkOtherApps(){
		List<App> appList = mrAppSelectionPolicy.selectApp();
		if(!appList.isEmpty()){
			selectApps();
		}
	}

	protected void processVmCreate(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];
		String datacenterName = CloudSim.getEntityName(datacenterId);
		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
			
			Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
					+ " has been created in " + datacenterName + " SimEntity# " + datacenterId +
					", Host #"
					+ VmList.getById(getVmsCreatedList(), vmId).getHost().getId());						
			
		} else {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
					+ " failed in Datacenter #" + datacenterId);
		}

		incrementVmsAcks();

			// all the acks received, but some VMs were not created
		if (getVmsRequested() == getVmsAcks()) {
			// find id of the next datacenter that has not been tried
			for (int nextDatacenterId : getDatacenterIdsList()) {
				if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
					createVmsInDatacenter(nextDatacenterId);
					return;
				}
			}

			// all datacenters already queried
			if (getVmsCreatedList().size() > 0) { // if some vm were created
				submitCloudlets();
			} else { // no vms created. abort
				Log.printLine(CloudSim.clock() + ": " + getName()
						+ ": none of the required VMs could be created. Aborting");
				finishExecution();
			}
		}
	}
	
	
	private void createNodeManager() {			
		vmList = datacenter.getVmList();					
		for(int i = 0; i < vmList.size(); i++){
			NodeManager nodeManager = new NodeManager("Node_Manager_vm_" + i, i);
			if(i == 0){
				// locate HSDFS in the first VM of the list
				this.getHdfs().setVm(vmList.get(i));		
				this.getHdfs().setNodeManager(nodeManager);				
			}			
			NodeManagerList.add(nodeManager);
			nodeManager.setNodeManagerVm(vmList.get(i));
			Log.printLine(CloudSim.clock() + ": " + getName() + " is trying to create NodeManager#" + i
					+ " in " + this.vmManager.getVmNameById(vmList.get(i).getId())+ " ("+datacenter.getName() +")");			
		}							
	}		
		
	
	public void setDC(BigDataDatacenter datacenter){
		this.datacenter = datacenter;
	}
		
	private void createdMapReduceApps(){
		AppsParser.startParsingExcelAppFile(this.appFileName);		
		// create an application master for every submitted MapReduce application  
		for(App mapReduce : AppsParser.appList){					
			Log.printLine(CloudSim.clock() + ": " + getName() + " is trying to create appliction master #" + applicationMasterIndexing
					+ " in " + datacenter.getName());			
			MapReduceAM mapReduceAM = new MapReduceAM("ApplicationMaster_" + applicationMasterIndexing, applicationMasterIndexing, this, this.datacenter , this.sdnController, mapReduce);
			mapReduceAM.setNodemanagers(this.NodeManagerList);
			applicationMasterIndexing++;
			applicationMastersList.add(mapReduceAM);
			mapReduceAM.getBdms().getHdfs().setRepliaction(mapReduce.getReplicaNum());
			mapReduce.setMapReduceAM(mapReduceAM);
			mrAppSelectionPolicy.addAppToList(mapReduce);
		}
				
		selectApps();
	}
	
	private void selectApps(){
		List<App> appList = mrAppSelectionPolicy.selectApp();

		double submitTime = 0;
		for(App mapReduce : appList){					
			submitTime = mapReduce.getAppSubmitTime() + CloudSim.clock(); 
			send(this.getHdfs().getId(), submitTime, BigDataSDNSimTags.TRANSFER_BLOCKS, mapReduce);
			mrAppSelectionPolicy.addAppToHistory(mapReduce);			
		}	
		mrAppSelectionPolicy.getAppList().removeAll(appList);
	}
	
	public MapReduceAppSelectionPolicy getMrAppSelectionPolicy() {
		return mrAppSelectionPolicy;
	}

	public void setMrAppSelectionPolicy(MapReduceAppSelectionPolicy mrAppSelectionPolicy) {
		this.mrAppSelectionPolicy = mrAppSelectionPolicy;
	}

	public BigDataDatacenter getDatacenter() {
		return datacenter;
	}
	
	public SDNController getSDNController() {
		return sdnController;
	}
	
	public int getNumOfDatacenters(){
		return 1;
	}
	
	@Override
	public void shutdownEntity() {
		
	}	
	
	public HDFS getHdfs() {
		return hdfs;
	}
	
	public void addVmManager(VMManager vmManager){
		this.vmManager = vmManager;
	}

	public void submitAppRequests(String appFileName){
		this.appFileName = appFileName;
	}
	
	public List<NodeManager> getNodeManagerList() {
		return NodeManagerList;
	}

	public  List<MapReduceAM> getMapReduceAM() {
		return this.applicationMastersList ;
	}
}
