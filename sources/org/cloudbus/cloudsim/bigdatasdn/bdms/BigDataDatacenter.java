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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.SDNHost;
import org.cloudbus.cloudsim.sdn.Switch;
import org.cloudbus.cloudsim.sdn.example.policies.VmSchedulerTimeSharedEnergy;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


/**
 * 
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */

public class BigDataDatacenter extends Datacenter {
 
	protected Topology topology;
	protected List<Host> hosts = new ArrayList<Host>();
	protected List<SDNHost> sdnhosts;
	protected List<Switch> switches;

	public static int cloudletsNumers = 0;
	int lastProcessTime; 	

	public BigDataDatacenter(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);		
		System.out.println(this.getName() + " is creating the physical topology...");
	
	}
	
	public void initPhysicalTopology(String physicalTopologyFileName) {
		 topology  = new Topology();		 
		 sdnhosts = new ArrayList<SDNHost>();
		 switches= new ArrayList<Switch>();
		int hostId=0;
		Hashtable<String,Integer> nameIdTable = new Hashtable<String, Integer>();
		try {    		
			JSONObject doc = (JSONObject) JSONValue.parse(new FileReader(physicalTopologyFileName));
    		
    		JSONArray nodes = (JSONArray) doc.get("nodes");
    		@SuppressWarnings("unchecked")
			Iterator<JSONObject> iter =nodes.iterator(); 
			
    		System.out.println("Create Hosts first and SWs/SDNs second!");
			
    		while(iter.hasNext()){
				// first create Hosts 
				JSONObject node = iter.next();
				String nodeType = (String) node.get("type");
				String nodeName = (String) node.get("name");
				
				if(nodeType.equalsIgnoreCase("host")){
					long pes = (Long) node.get("pes");
					long mips = (Long) node.get("mips");
					int ram = new BigDecimal((Long)node.get("ram")).intValueExact();
					long storage = (Long) node.get("storage");
					
					long bw = 1; 
					
					int num = 1;
					if (node.get("nums")!= null)
						num = new BigDecimal((Long)node.get("nums")).intValueExact();					
					for(int n = 0; n< num; n++) {
						String nodeName2 = nodeName;
						if(num >1) nodeName2 = nodeName + n;
						Host host = createHost(hostId, ram, bw, storage, pes, mips);
						host.setDatacenter(this);
						SDNHost sdnHost = new SDNHost(host, nodeName2);
						nameIdTable.put(nodeName2, sdnHost.getAddress());
						
						hostId++;
						
						this.topology.addNode(sdnHost);
						this.hosts.add(host);
						this.sdnhosts.add(sdnHost);
					}
					
				} else {
					int MAX_PORTS = 256;
							
					long bw = 0;
					long iops = (Long) node.get("iops");
					int upports = MAX_PORTS;
					int downports = MAX_PORTS;
					if (node.get("upports")!= null)
						upports = new BigDecimal((Long)node.get("upports")).intValueExact();
					if (node.get("downports")!= null)
						downports = new BigDecimal((Long)node.get("downports")).intValueExact();
					Switch sw = null;
					sw = new Switch(nodeName, bw, iops, upports, downports);					
					if(sw != null) {
						nameIdTable.put(nodeName, sw.getAddress());
						this.topology.addNode(sw);
						this.switches.add(sw);
					}
				}
			}
				
			JSONArray links = (JSONArray) doc.get("links");
			@SuppressWarnings("unchecked")
			Iterator<JSONObject> linksIter =links.iterator(); 
			System.out.println("Create links third!");			
			while(linksIter.hasNext()){
				JSONObject link = linksIter.next();
				String src = (String) link.get("source");  
				String dst = (String) link.get("destination");
				double lat = (Double) link.get("latency");
				long bw = (long) link.get("bw");
				int srcAddress = nameIdTable.get(src);
				if(dst.equals("")){
					System.out.println("Null!");			
				}
				int dstAddress = nameIdTable.get(dst);
				topology.addLink(srcAddress, dstAddress, lat, bw);
			}
    		
    		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
	}
	
	public void setHostsList(List<Host> hosts){
		this.hosts = hosts;
	}
	
	public void feedSDNWithTopology(SDNController controller){
		controller.setTopology(topology, hosts, sdnhosts, switches);
	}
	
	@Override
	public void processOtherEvent(SimEvent ev){
		switch(ev.getTag()){	
			default: System.out.println("Unknown event recevied by SDNDatacenter. Tag:"+ev.getTag());
		}
	}
	
	protected Host createHost(int hostId, int ram, long bw, long storage, long pes, double mips) {
		LinkedList<Pe> peList = new LinkedList<Pe>();
		int peId=0;
		for(int i=0;i<pes;i++) peList.add(new Pe(peId++,new PeProvisionerSimple(mips)));
		
		RamProvisioner ramPro = new RamProvisionerSimple(ram);
		BwProvisioner bwPro = new BwProvisionerSimple(bw);
		VmScheduler vmScheduler = new VmSchedulerTimeSharedEnergy(peList);		
		Host newHost = new Host(hostId, ramPro, bwPro, storage, peList, vmScheduler);
		
		return newHost;		
	}
	
	@Override
	protected void checkCloudletCompletion() {
		List<? extends Host> list = getVmAllocationPolicy().getHostList();
		for (int i = 0; i < list.size(); i++) {
			Host host = list.get(i);
			for (Vm vm : host.getVmList()) {
				while (vm.getCloudletScheduler().isFinishedCloudlets()) {
					Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
					if (cl != null) {
						BigDataTask task = (BigDataTask) cl;
						String taskType = task.getMapReduceTask().getTaskType();								
						if(taskType.equals("MAP")){
							sendNow(task.getMapReduceAM().getId(), BigDataSDNSimTags.MAPPER_FINISHED_EXECUTION, task);							
						} else if (taskType.equals("REDUCE")){				
							sendNow(task.getMapReduceAM().getId(), BigDataSDNSimTags.REDUCER_FINISHED_EXECUTION, cl);
							}
						else{
								System.out.println(getName() + " could not find the task tracker of the given mapper! " +cl.getCloudletId());
							}	
					} else{							
							System.out.println(getName() + " could not find the task type!");							
					}		
				}
			}
		}
	}
}
