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

import org.cloudbus.cloudsim.bigdatasdn.mapreduce.App;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.Reducer;

/**
 * 
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */

public abstract class DataPlacementPolicy {
	
	private String policyName;	
	public abstract List<Flow> distributeDataBlocks(int bdmsId, App job, int vmSrcId, HDFS hdfs);
	public abstract List<Flow> distributeMapperOutput(int bdmsId, App job, BigDataTask task);
	public abstract List<Flow> distributeReducerOutput(int bdmsId, App job, Reducer task, HDFS hdfs);	

	
	public String getPolicyName() {
		return policyName;
	}
	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}		 
}
