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

package org.cloudbus.cloudsim.bigdatasdn.example;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;


import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.bigdatasdn.bdms.BigDataDatacenter;
import org.cloudbus.cloudsim.bigdatasdn.bdms.BigDataManagementSystem;
import org.cloudbus.cloudsim.bigdatasdn.bdms.DataPlacementPolicy;
import org.cloudbus.cloudsim.bigdatasdn.bdms.SDNController;
import org.cloudbus.cloudsim.bigdatasdn.bdms.SDNRoutingPolicy;
import org.cloudbus.cloudsim.bigdatasdn.bdms.VMManager;
import org.cloudbus.cloudsim.bigdatasdn.bdms.polocies.DataPlacementRoundRobin;
import org.cloudbus.cloudsim.bigdatasdn.bdms.polocies.MapReduceAppSelectionPolicy;
import org.cloudbus.cloudsim.bigdatasdn.bdms.polocies.SDNMapReducePolicyFairShare;
import org.cloudbus.cloudsim.bigdatasdn.bdms.polocies.SDNMapReduceSchedulingPolicy;
import org.cloudbus.cloudsim.bigdatasdn.bdms.polocies.SDNRoutingTraditionalShortestPath;
import org.cloudbus.cloudsim.bigdatasdn.bdms.polocies.VmAllocationPolicyCombinedLeastFullFirst;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.AppsParser;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.sdn.Switch;
import org.cloudbus.cloudsim.sdn.power.PowerUtilizationMaxHostInterface;


/**
 * This is an example of running a multiple MapReduce applications (three replicas) with prioritization
 * in a traditional network cloud data center (without SDN capabilities)
 * 
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */

public class Example_7_Priority_MultipleApps_ThreeReplicas_SP {

	protected static String physicalTopologyFile 	= "inputFiles/Datacenter_Configuration.json";
	protected static String appFileName = "inputFiles/Priority_MultipleApps_ThreeReplica_Workload.csv"; 		
	private SDNController sdnController;

	/** vmList is made of one or more virtual machines. */
	private static List<Vm> vmList = new ArrayList<Vm>();

	VMManager vmManager;	
	BigDataManagementSystem bdms;
	
	public static void main(String args[]) throws Exception {						
		Example_7_Priority_MultipleApps_ThreeReplicas_SP bigDataSDN = new Example_7_Priority_MultipleApps_ThreeReplicas_SP(); 		
		bigDataSDN.run();
	}

	public void run() throws IOException {

		Log.printLine("Starting Simulation...");
		try {
			int num_user = 1; // number of users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);
			
			// create a datacenter and its topology (hosts, switches, and links)
			VmAllocationPolicy vmPolicy = new VmAllocationPolicyCombinedLeastFullFirst(); // policy to allocate VMs on hosts 
			BigDataDatacenter datacenter = createBigDataDatacenter("Datacenter_0", vmPolicy);
			datacenter.initPhysicalTopology(physicalTopologyFile);			
			vmPolicy.setUpVmTopology(datacenter.getHostList());
		
			// create an SDN controller and set up its policies 
			SDNMapReduceSchedulingPolicy sdnMapReducePolicy = new SDNMapReducePolicyFairShare(); // SDN traffic policy 	
			SDNRoutingPolicy sdnRouting = new SDNRoutingTraditionalShortestPath(); // without SDN (traditional network routing) 
			
			sdnController = new SDNController(sdnMapReducePolicy, sdnRouting);			
			datacenter.feedSDNWithTopology(sdnController);
			
			String cloudletPolicy = "TimeSharedMutipleCPUs";
			vmManager = new VMManager("VMManager", cloudletPolicy);

			MapReduceAppSelectionPolicy mrSelectionAppPolicy = new MapReduceAppSelectionPolicy(); // policy to select MapReduce applications 
						
		    DataPlacementPolicy dataPlacementPolicy = new DataPlacementRoundRobin(); // HDFS replication and placement policy 
			List<BigDataManagementSystem> bdmsList = new ArrayList<BigDataManagementSystem>(); 

			bdms = new BigDataManagementSystem("BDMS", sdnController, mrSelectionAppPolicy, dataPlacementPolicy);	
			bdmsList.add(bdms);
 		
			sdnController.addVmManager(vmManager);
			bdms.addVmManager(vmManager);

			bdms.getDatacenterIdsList().add(datacenter.getId());			
			bdms.setDC(datacenter);
			
			int brokerId = bdms.getId();
			int idShift = 0;
			
			int VMType = 3; // three types 1, 2 & 3 (look at the VMManager class) 			
			int NumberOfVM = 16; 	
			
			vmList = vmManager.createVM(VMType, brokerId, NumberOfVM, idShift);
			sdnController.addVmsToSDNhosts(vmList);
			bdms.submitVmList(vmList);

			bdms.submitAppRequests(appFileName);
			
			double finishTime  = CloudSim.startSimulation();
			
			CloudSim.stopSimulation();
         
            PrintResults pr = new PrintResults(sdnController, bdms, vmManager, vmPolicy);
			pr.printAllResults();

			for (int i = 0; i < AppsParser.appList.size(); i++){
				 AppsParser.appList.get(i).getNetworkStatistics().printForwardingTables();
			}
									
			List<Switch> switchList = sdnController.getSwitchList();
			LogPrinter.printEnergyConsumption(datacenter.getHostList(), switchList, finishTime);

			Log.printLine();
			Log.printLine("Simulation Finished!");

		} catch (Exception e)
		{
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}
	
	
	protected static PowerUtilizationMaxHostInterface maxHostHandler = null;
	protected BigDataDatacenter createBigDataDatacenter(String name, VmAllocationPolicy vmPolicy) {
		
		List<Host> hostList = new ArrayList<Host>();

		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
													// devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		BigDataDatacenter datacenter = null;
		try {					
			maxHostHandler = (PowerUtilizationMaxHostInterface)vmPolicy;
			datacenter = new BigDataDatacenter(name, characteristics, vmPolicy, storageList, 0);						
			datacenter.setHostsList(hostList);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return datacenter;
	}
}
