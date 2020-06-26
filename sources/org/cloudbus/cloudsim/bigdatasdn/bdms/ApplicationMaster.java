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

import org.cloudbus.cloudsim.core.SimEntity;


/**
 * ApplicationMaster represents big data programming models (MapReduce, Stream, etc.).
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */

public abstract class ApplicationMaster extends SimEntity {		
	public ApplicationMaster(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	protected List<NodeManager> NodeManagerList  = new ArrayList<NodeManager>();  // every application can have different number of node managers
		
	public void setNodemanagers(List<NodeManager> nmList) {
		this.NodeManagerList = nmList; 		
	}

	
	public List<NodeManager> getNM() {
		// TODO Auto-generated method stub
		return null;
	}
}
