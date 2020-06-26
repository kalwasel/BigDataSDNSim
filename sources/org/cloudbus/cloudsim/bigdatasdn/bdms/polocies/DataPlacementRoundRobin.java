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

package org.cloudbus.cloudsim.bigdatasdn.bdms.polocies;

import java.util.ArrayList;
import java.util.List;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.bigdatasdn.bdms.BigDataTask;
import org.cloudbus.cloudsim.bigdatasdn.bdms.DataPlacementPolicy;
import org.cloudbus.cloudsim.bigdatasdn.bdms.Flow;
import org.cloudbus.cloudsim.bigdatasdn.bdms.HDFS;
import org.cloudbus.cloudsim.bigdatasdn.bdms.NodeManager;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.App;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.Mapper;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.Reducer;
import org.cloudbus.cloudsim.core.CloudSim;


/**
 * 
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */

public class DataPlacementRoundRobin extends DataPlacementPolicy{
	
	private static int index = 0;
	private static int mappersIndex = 0;
	private static int reducersIndex = 0;
	private static int count = 0;
	private int pesNumber = 1;
   	private	UtilizationModel utilizationModelCpu;
   	private UtilizationModel utilizationModelRam;
   	private	UtilizationModel utilizationModelBw;
   	double time = 0;
   	private List<NodeManager> selectedVMs =  new ArrayList<>();

	public DataPlacementRoundRobin(){
   		setPolicyName("RoundRobin");
   	}
   	
	@Override
	public List<Flow> distributeDataBlocks(int bdmsId, App app, int vmSrcId, HDFS hdfs) {
		
		List<Flow> flowList = new ArrayList<Flow>(); 
		double blockSize;	
		double defaultNumMappers = Math.ceil(app.getHDFSToMapperNetworkWorload()/app.getHdfsBlockSize());
		
		long requestedMappers = app.getNumberofMapTask();
		if (requestedMappers > defaultNumMappers){
			defaultNumMappers = requestedMappers;
			int newNumOfMappers = (int) defaultNumMappers;
			app.setNumberofMapTask(newNumOfMappers);
			blockSize = app.getHDFSToMapperNetworkWorload()/defaultNumMappers;
			app.setHdfsBlockSize(blockSize);
		} else {			
			blockSize = app.getHdfsBlockSize();	
			app.setNumberofMapTask((int) defaultNumMappers);
		}
		
		double hdfsTotalDataSize = app.getHDFSToMapperNetworkWorload();

		List<Mapper> mapperList = app.getMapperList();
		if(mapperList.isEmpty()){
			createMapTasks(app);
		}
		
		NodeManager selectedNM = null;

		int numOfReplication = hdfs.getRepliaction() * (int) defaultNumMappers;
		app.setNumOfDataBlocks(numOfReplication);

		double dataSizePerVM = 0;

		double reminder = hdfsTotalDataSize%blockSize; 		
		for (int k = 1; k <= defaultNumMappers; k++){	
			
			for (int i = 0; i <  hdfs.getRepliaction(); i++){
				
				if(defaultNumMappers == k && reminder != 0){
					dataSizePerVM = hdfsTotalDataSize%blockSize; 		
				} else {
					dataSizePerVM  = blockSize;
				}
				for (NodeManager nm : app.getMpAM().getNodeManagerList()){
					if (selectedVMs.size() == app.getMpAM().getNodeManagerList().size()){
						selectedVMs.clear();
					}
					if(!selectedVMs.contains(nm))
					{
						selectedNM = nm;
						selectedVMs.add(nm);
						break;
					}
				}
				
				String vmNameSrc = "HDFS_Block_" + k;			
				String packageType = "vm"; 
				
				String vmNameDest = selectedNM.getName();
				int toVmId = selectedNM.getVmID();				
			
				app.setCountFlows(1);			
				Flow flow  = new Flow(app.getMpAM(), vmNameSrc,vmNameDest,vmSrcId, toVmId, dataSizePerVM, app.getCountFlows(), packageType);	
				flow.setApp(app);
				flow.setSubmitTime(CloudSim.clock());
				flow.setAppPriority(app.getAppPriority());
//				System.out.println("HDFS to Mapper flow: " + i +" " +  dataSizePerVM);
				flow.setNodeManager(selectedNM);
	
				flow.setAppName(app.getAppName());
	 
				flow.setAppId(app.getAppId());
				flow.setAckEntity(hdfs.getId());
				time = time + CloudSim.getMinTimeBetweenEvents();			
				flowList.add(flow);	
			}	
		}
			return flowList;
	}
	

	@Override
	public List<Flow> distributeMapperOutput(int bdmsId, App app, BigDataTask maptask) {		
		
		
		double dataSize = Math.ceil(app.getMappertoReducerNetworkWorload()/app.getNumberOfReduceTask());
		List<Flow> flowList = new ArrayList<Flow>(); 
		
		
		List<Reducer> reducerList = app.getReducerList();

		if(reducerList.isEmpty()){
			createReduceTasks(app);
		}

		for (Reducer reducer : reducerList){	
			
			String vmNameSrc = maptask.getMapReduceTask().getTaskName();
			int vmSrcId = maptask.getNodeManager().getVmID();
	
			String packageType = "reducer";
			
			String vmNameDest = reducer.getName();
			int toVmId = reducer.getNodeManager().getVmID();
	
			Flow flow  = new Flow(app.getMpAM(), vmNameSrc,vmNameDest, vmSrcId, toVmId, dataSize, -1, packageType);	
			flow.setSubmitTime(CloudSim.clock());
			flow.setAppPriority(app.getAppPriority());
			flow.setApp(app);
			flow.setNodeManager(reducer.getNodeManager());
	
			flow.setAppName(reducer.getApp().getAppName());
			
			flow.setAppId(reducer.getApp().getAppId());
			flow.setAckEntity(app.getMpAM().getId());
			time = time + CloudSim.getMinTimeBetweenEvents();
			
			flowList.add(flow);	

		}							
	return flowList;				
	}

	
	@Override
	public List<Flow> distributeReducerOutput(int bdmsId, App app, Reducer reducer, HDFS hdfs) {
		reducer.setFinished(true);

		double dataSize = app.getReducerToHDFSNetworkWorload();
		double blockSize =  app.getHdfsBlockSize();
		double ReplicatedDataSize = 0;
		
		double computedBlockNum = Math.ceil(dataSize/blockSize);
		int NumOfBlocks = (int) computedBlockNum;

		List<Flow> flowList = new ArrayList<Flow>(); 
				
		NodeManager selectedNM = null;
 
		double reminder = dataSize%blockSize; 		

		int NumOfReplica = hdfs.getRepliaction(); // number of replica for each blcok 
		
		if(NumOfReplica > 0){ 
			for (int k = 1; k<= NumOfBlocks; k++){					
				for (int i = 0; i <  NumOfReplica; i++){
					for (NodeManager nm : app.getMpAM().getNodeManagerList()){
						if (selectedVMs.size() == app.getMpAM().getNodeManagerList().size()){
							selectedVMs.clear();
						}
						if(!selectedVMs.contains(nm))
						{
							selectedNM = nm;
							selectedVMs.add(nm);
							break;
						} 
					}
					
					if(k == NumOfBlocks & reminder !=0){
						ReplicatedDataSize = dataSize%blockSize; 			
					} else {
						ReplicatedDataSize  = blockSize;					
					}										
					
					String vmNameSrc = reducer.getName() + "_Block_" + k;	
					int vmSrcId = reducer.getNodeManager().getVmID();		
					String packageType = "hdfs"; // transfer from reducer to other reducers based on replication factor 		
					String vmNameDest = selectedNM.getName();
					int toVmId = selectedNM.getVmID();
		
					Flow flow = createFlow(app, selectedNM, vmNameSrc,vmNameDest,vmSrcId, toVmId, ReplicatedDataSize, -1, packageType);							
					flowList.add(flow);	
					app.countReducerFlows(1);
				}
			}					
		} else {
			flowList = null;
		}
				
		// send the final output of every reducer to the main VM of HDFS to combine and report the final result
		selectedNM = hdfs.getNodeManager();			
		String vmNameSrc = reducer.getName() + "_Final_Data";	
		int vmSrcId = reducer.getNodeManager().getVmID();		
		String packageType = "reducerToMainVM"; // transfer from reducer to other reducers based on replication factor 		
		String vmNameDest = selectedNM.getName();
		int toVmId = selectedNM.getVmID();

		Flow flow = createFlow(app, selectedNM, vmNameSrc,vmNameDest,vmSrcId, toVmId, dataSize, -1, packageType);							

		app.addFinalReducerData(flow); // keep the final output of every reducer here 
		return flowList;
	}
	
	private Flow createFlow(App app, NodeManager selectedNM, String vmNameSrc, String vmNameDest, 
							int vmSrcId, int toVmId, double dataSize, int flowId, String packageType){
		Flow flow  = new Flow(app.getMpAM(), vmNameSrc,vmNameDest,vmSrcId, toVmId, dataSize, -1, packageType);
		flow.setApp(app);
		flow.setSubmitTime(CloudSim.clock());
		flow.setAppPriority(app.getAppPriority());

		flow.setNodeManager(selectedNM);

		flow.setAppName(app.getAppName());

		flow.setAppId(app.getAppId());
		flow.setAckEntity(app.getMpAM().getId());
		time = time + CloudSim.getMinTimeBetweenEvents();			

		return flow;
	}
	
	
	private void createMapTasks(App app){		
		List<NodeManager> nmList = app.getMpAM().getNodeManagerList();
		NodeManager selectedNM = null;		
		for (int i = 0; i < app.getNumberofMapTask(); i++){
			if(count == app.getMpAM().getBdms().getVmList().size()){				
				System.out.println("number of block: " + count);
				count = 0;
			}
			selectedNM = nmList.get(count);
			BigDataTask mapTask = null;				
					
			List<String> fileNameList = new ArrayList<String>();			
			fileNameList.add("file" + index);															
			mapTask = new BigDataTask(index,
					app.getCPUno(),
					app.getMapMips(),					
					pesNumber,
					1,
					1,
					utilizationModelCpu,
					utilizationModelRam,
					utilizationModelBw,
					true,
					fileNameList,
					app.getAppName(),
					app.getAppId(),
					1);				 
			mapTask.setUserId(app.getMpAM().getBdms().getId());		
			mapTask.setMapReduceAM(app.getMpAM());
			mapTask.setApp(app);
			Mapper mapper = new Mapper("mapper" + mappersIndex, app, mapTask);	
			mapper.setHDFSToMapperWorkload(app.getHDFSToMapperNetworkWorload()/app.getNumberofMapTask());
			Log.printLine(CloudSim.clock()  + ": HDFS" + " created " + mapper.getName() + " for " + app.getAppName() + " at VM " + count);

			app.addMapperToList(mapper);
						
			selectedNM.addMapperToList(mapper);
			mapper.setNodeManager(selectedNM);
			mapper.getMapTaskCloudlet().setNodeManager(selectedNM);
			mapper.getMapTaskCloudlet().setMapReduceTask(mapper);		
			
			index++;
			mappersIndex++;
			count++;
		}
	}

	
	private void createReduceTasks(App app){
		NodeManager selectedNM = null;
		List<NodeManager> nmList = app.getMpAM().getNodeManagerList();
		for (int j =0; j < app.getNumberOfReduceTask(); j++){
			
			if(count == app.getMpAM().getBdms().getVmList().size()){				
				System.out.println("number of block: " + count);
				count = 0;
			}				
			double ReduceTaskFileSize = 0.0;
			double ReduceTaskOutputSize = 0.0;				
			BigDataTask reduceTask = null;
			List<String> fileNameList = new ArrayList<String>();			
			fileNameList.add("file" + index);	
			reduceTask = new BigDataTask(index,
					app.getCPUno(),
					app.getReduceMips(),
					pesNumber,
					ReduceTaskFileSize,
					ReduceTaskOutputSize,
					utilizationModelCpu,
					utilizationModelRam,
					utilizationModelBw,
					true,
					fileNameList,
					app.getAppName(),
					app.getAppId(),
					2);		
			reduceTask.setUserId(app.getMpAM().getBdms().getId());
			reduceTask.setMapReduceAM(app.getMpAM());
			reduceTask.setApp(app);
			
			Reducer reducer = new Reducer("reducer" + reducersIndex, app, reduceTask);
			reducer.setReducerToStorageWorkload(app.getReducerToHDFSNetworkWorload()/app.getNumberOfReduceTask());

			reducer.setAssoicatedNumOfMappers(app.getNumberofMapTask());
			Log.printLine(CloudSim.clock() + ": HDFS" + " created " + reducer.getName() + " for " + app.getAppName() + " at VM " + count);
	
			app.addReducerToList(reducer);					

			selectedNM = nmList.get(count);
			selectedNM.addReducerToList(reducer);
			reducer.setNodeManager(selectedNM);
			reducer.getReduceTaskCloudlet().setNodeManager(selectedNM);
			reducer.getReduceTaskCloudlet().setMapReduceTask(reducer);		
			
			index++;
			reducersIndex++;
			count++;
		}
	}
	
}
