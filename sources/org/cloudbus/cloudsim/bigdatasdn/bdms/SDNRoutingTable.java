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

import org.cloudbus.cloudsim.sdn.NetworkNIC;
import org.cloudbus.cloudsim.sdn.SDNHost;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * 
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */

public class SDNRoutingTable {

	int srcVmID; 
	int destVmId; 
	SDNHost srcHost;
	SDNHost destHost;
	List<NetworkNIC> routes;  
	List<List<NetworkNIC>> routesList; 
	Multimap<Integer, List<NetworkNIC>> routeMap;
	
	public SDNRoutingTable(SDNHost srcHost, SDNHost destHost, List<NetworkNIC> routes) {		
		this.srcHost = srcHost;
		this.destHost = destHost;
		this.routes = routes;
		routesList = new ArrayList<List<NetworkNIC>>();
		routeMap = HashMultimap.create();
	}	
	public List<NetworkNIC> getRoute(SDNHost src, SDNHost dest){
		
		if(srcHost.equals(src) && destHost.equals(dest)){
			return routes;	
		}		
		return null;
	}
	
	public SDNRoutingTable getTable(SDNHost src, SDNHost dest){
		if(srcHost.equals(src) && destHost.equals(dest)){
			return this;	
		}		
		return null;
	}
		
	public void updateSDNTable(List<NetworkNIC> route){
		this.routes = route;
	}
	
	public void addSDNMutipleRoutes(List<NetworkNIC> route){
		List<NetworkNIC> nodes = new ArrayList<>();
		nodes = route;		
		this.routesList.add(nodes);		
	}	
}
