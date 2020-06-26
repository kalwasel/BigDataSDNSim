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

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.bigdatasdn.bdms.polocies.SDNMapReduceSchedulingPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;


import org.cloudbus.cloudsim.sdn.Channel;
import org.cloudbus.cloudsim.sdn.Link;
import org.cloudbus.cloudsim.sdn.NetworkOperatingSystem;
import org.cloudbus.cloudsim.sdn.NetworkNIC;
import org.cloudbus.cloudsim.sdn.SDNHost;
import org.cloudbus.cloudsim.sdn.Switch;

/**
 *  
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */

public class SDNController extends NetworkOperatingSystem {
	
	private List<Channel> channelsHistory;
	private List<Flow> flowsHistory;	
	private VMManager vmManager;
	private SDNRoutingPolicy sdnRoutingPoloicy;
	private SDNMapReduceSchedulingPolicy sdnSchedulingPolicy;	
	
	public SDNController( SDNMapReduceSchedulingPolicy sdnPolicy, SDNRoutingPolicy sdnRouting) {				
		super();
		this.sdnSchedulingPolicy = sdnPolicy;
		this.sdnRoutingPoloicy = sdnRouting;

	
		
		this.channelsHistory = new ArrayList<>();
		this.flowsHistory = new ArrayList<>();		
	}
	
	@Override
	public void processEvent(SimEvent ev) {
		int tag = ev.getTag();
		
		switch(tag){
		
		case BigDataSDNSimTags.START_TRANSMISSION:		
			Flow flow = (Flow) ev.getData();			
			scheduleFlow(flow);
			break;
			
		case BigDataSDNSimTags.SDN_INTERNAL_EVENT: 
			internalFlowProcess(); 
			break;
		
//		case BigDataSDNSimTags.SDN_INTERNAL_LOAD_BALANCING:
//			loadBalanceTraffic();
//			break;
			
//		case BigDataSDNSimTags.INTERNAL_SDN_BW_Reservation:
			// for later use 
//			updateChannelBW();
//			break;
			
		default: System.out.println(this.getName() + ": Unknown event received by "+super.getName()+". Tag:"+ev.getTag());
		}
	}
	
	private void scheduleFlow(Flow flow){			
//		String policyName = getSdnSchedulingPolicy().getPolicyName();		
//		switch(policyName){
//		case "FairShair":
		getSdnSchedulingPolicy().setAppFlowSubmitTime(flow, CloudSim.clock()); 					
		startTransmitting(flow);	
//				break;
//		}
	}
	
	public void startTransmitting(Flow flow) {		
		
		int srcVm = flow.getOrigin();
		int dstVm = flow.getDestination();

		SDNHost srchost = findSDNHost(srcVm);
		SDNHost dsthost = findSDNHost(dstVm);
		int flowId = flow.getFlowId();
		
		if (srchost != null)
		{
			if(flow.hasRoute == false){				
				if(srchost.equals(dsthost)) {
					Log.printLine(CloudSim.clock() + ": " + getName() + ": Source SDN Host is same as destination. No need for routing!");			
					srchost.addVMRoute(srcVm, dstVm, flowId, dsthost);
					List<NetworkNIC> listNodes = new ArrayList<NetworkNIC>();					
					listNodes.add(srchost);
					getSdnSchedulingPolicy().setAppFlowStartTime(flow, flow.getSubmitTime()); // no transmission 
					flow.getApp().getNetworkStatistics().setFlowRoute(flow, listNodes);		
					removeCompletedFlows(flow);
				} else {					
					boolean findRoute = buildSDNForwardingTableVmBased(srchost, srcVm, dstVm, flowId, flow);
					getSdnSchedulingPolicy().setAppFlowStartTime(flow, CloudSim.clock()); // no transmission 
					pushFlowToNetwork(srchost, flow);	
					if(!findRoute){						
						System.out.println(this.getName() + " Could not find route from " + srchost.getName() + " to " + dsthost.getName());				
					}
				}
			
				flow.setStartTime(CloudSim.clock());
				this.flowsHistory.add(flow);

				flow.hasRoute = true;					
			}			
						
			if(flow.getApp().getAppStartTime() == -1){
				flow.getApp().setAppStartTime(CloudSim.clock());
			}
			
			flow.getApp().getNetworkStatistics().addFlowHistory(flow); // keep the history of Flows 
		}
	}
	
	
	protected void pushFlowToNetwork(NetworkNIC sender, Flow flow) { 
		int src = flow.getOrigin();
		int dst = flow.getDestination();
		int flowId = flow.getFlowId();	

		if(sender.equals(sender.getVMRoute(src, dst, flowId))) {
			removeCompletedFlows(flow);			
			return;
		}
		
		updateFlowProcessing();
		
		Channel channel; 

		if(sdnSchedulingPolicy.getPolicyName().contains("LoadBalancing")){
			
			channel = createChannel(src, dst, flowId, sender, flow);												
			addChannel(src, dst, flowId, channel);
		} else {
		// srcVM, dstVM
		channel = findChannel(src, dst, flowId);

			if(channel == null) {
				channel = createChannel(src, dst, flowId, sender, flow);												
				addChannel(src, dst, flowId, channel);
			}
		}
		
		this.channelsHistory.add(channel);
		channel.addFlowToList(flow);			
		channel.addTransmission(flow);

		sendInternalEvent();		
	}

	protected void processCompleteFlows(List<Channel> channels){
		for(Channel ch:channels) {												
			for (Flow flow : ch.getFinishedFlows()){
				removeCompletedFlows(flow);
			}
		}
	}
	
	protected void removeCompletedFlows(Flow flow ){				
		getSdnSchedulingPolicy().removeFlowFromList(flow);	
		System.out.print("Flow " + flow.getFlowId() + " has been transmitted.");
		flow.setFinishTime(CloudSim.clock);
		int tag = BigDataSDNSimTags.Transmission_ACK;		
		sendNow(flow.getAckEntity(), tag, flow);							
	}
	
	 	
	private boolean buildSDNForwardingTableVmBased(NetworkNIC node, int srcVm, int dstVm, int flowId, Flow flow) {		

		SDNHost desthost = findSDNHost(dstVm);
		if(node.equals(desthost))
		{
			return true;
		}	
		
		List<NetworkNIC> nodesOnRoute = new ArrayList<>();	
		SDNHost srcHost = findSDNHost(srcVm);
		nodesOnRoute = sdnRoutingPoloicy.getRoute(flow);

		if(nodesOnRoute == null){
			// Build a route using the given routing policy (Shortest path, etc.)
			nodesOnRoute = sdnRoutingPoloicy.buildRoute(srcHost, desthost, flow); 			
		}		

		// the route starts from the end of the list towards the beginning 
		System.out.println("The route is: vm_" + dstVm + " "+ nodesOnRoute + " vm_" + srcVm); // print route
		
		List<NetworkNIC> listNodes = flow.getApp().getNetworkStatistics().getFlowRoute(flow);			
		
		if(listNodes == null){
			listNodes = new ArrayList<NetworkNIC>();
		}

		NetworkNIC currentNode = null;
		NetworkNIC nextNode = null;

		int iterate = nodesOnRoute.size()-1;
		for(int i = iterate; i >= 0; i--){
			currentNode = nodesOnRoute.get(i); 
			listNodes.add(currentNode);
			if(currentNode.equals(desthost)){
				break;
			}else{
				nextNode = nodesOnRoute.get(i-1);	
			}			
			currentNode.addVMRoute(srcVm, dstVm, flowId, nextNode);
		}	
		
		flow.getApp().getNetworkStatistics().setFlowRoute(flow, listNodes);

		return true;			
	}
	 
	protected Channel createChannel(int src, int dst, int flowId, NetworkNIC srcNode, Flow flow) {		
		List<NetworkNIC> nodes = sdnRoutingPoloicy.getRoute(flow);
		if(nodes == null){
			System.out.println("SDN controller: routing does not exist!");
		}		
		List<Link> links = sdnRoutingPoloicy.getLinks(flow);
		double reqBw = flow.requestedBW;		
		Channel channel = new Channel(flowId, src, dst, nodes, links, reqBw);
		return channel;
	}
		
	public SDNRoutingPolicy getSdnRoutingPoloicy() {
		return this.sdnRoutingPoloicy;
	}

	
	public SDNMapReduceSchedulingPolicy getSdnSchedulingPolicy() {
		return sdnSchedulingPolicy;
	}

	public void addVmsToSDNhosts(List<Vm> vmList){
		this.vmList = vmList;
		
		for (Vm vm : this.vmList){
			NetworkOperatingSystem.debugVmIdName.put(vm.getId(),vmManager.getVmNameById(vm.getId()));
		}		
	}

	public void addVmManager(VMManager vmManager){
		this.vmManager = vmManager;
	}	
	
	public List<Flow> getStoredFlowsHis(){
		return this.flowsHistory;
	}
	
	public void printFlows(){
		
		System.out.println("");
		
		for (Channel ch : this.channelsHistory){
			for( Flow flow : ch.getFlowList()){
			System.out.println(flow.getStartTime() + " From " + flow.getAppNameSrc() + " To " + flow.getAppNameDest() + " -- Priority: " + 
					flow.getAppPriority() + " -- Requested BW: " + ch.getRequestedBandwidth() +
					" -- Allocated BW: " + ch.getAllocatedBandwidth() + " -- BW Changes Log: " +  ch.getBwChangesLogMap());
			} 
		}
	}		
	
	public void setTopology(Topology topology, List<Host> hosts, List<SDNHost> sdnhosts, List<Switch> switches){	
		this.topology = topology;
		this.hosts = hosts;
		this.sdnhosts = sdnhosts;
		this.switches = switches;
		this.sdnRoutingPoloicy.setNodeList(topology.getAllNodes(), topology);		
		this.sdnRoutingPoloicy.buildNodeRelations(topology);
	}

}
