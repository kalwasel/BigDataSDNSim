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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.sdn.NetworkNIC;


/**
 * 
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */

public class NetworkStatistics {

	private List<Flow> flowsHistory;
	Map<Flow, List<NetworkNIC>> flowRoute; // store the route of every flow  	
	
	public List<NetworkNIC> getFlowRoute(Flow pkt) {
		return flowRoute.get(pkt);
	}

	public void setFlowRoute(Flow pkt, List<NetworkNIC> NetworkNIC) {
		this.flowRoute.put(pkt, NetworkNIC);
	}

	public NetworkStatistics(){
	this.flowsHistory = new ArrayList<Flow>();		
	this.flowRoute = new HashMap<Flow, List<NetworkNIC>>();
	}
	
	public void addFlowHistory(Flow pkt){
		this.flowsHistory.add(pkt);
	}
	
	public List<Flow> getFlowsHistory() {
	return this.flowsHistory;
	}	
	
	public double getNetworkTime(String pktType){
		double maxTime = 0;
		double currentTime;
		for(Flow pkt : this.flowsHistory){
			if(pkt.getFlowType().equals(pktType)){
				currentTime = pkt.getFinishTime() - pkt.getStartTime();
				if(currentTime > maxTime){
					maxTime = currentTime;
				}
			}
		}
		return maxTime;
	}
	
	public void printForwardingTables(){
		List<NetworkNIC> list = new ArrayList<NetworkNIC>();
		Log.printLine();
		Log.printLine("##################### Forwarding Tables #######################");
		for (Flow pkt : this.flowRoute.keySet()) {
			list = this.flowRoute.get(pkt);
			Log.printLine(pkt.getAppNameSrc() +" --> "+ list + " --> " + pkt.getAppNameDest());
		}			
	}
	
	public double getTotalFileNetworkData(){
		double countSize = 0; 
		for (Flow pkt : this.flowsHistory){			
				countSize += pkt.getSize();						
		}
		return countSize;
	}
	
	public double getFlowStartTime(String pktType){		
		double minTime = Integer.MAX_VALUE;
		double currentTime;
		for(Flow pkt : this.flowsHistory){
			if(pkt.getFlowType().equals(pktType)){
				currentTime = pkt.getStartTime();
				if(currentTime < minTime){
					minTime = currentTime;
				}
			}
		}
		return minTime;
	}
}
