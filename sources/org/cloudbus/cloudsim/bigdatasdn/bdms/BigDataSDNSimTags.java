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

/**
 * 
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */

public class BigDataSDNSimTags {
	
	private static final int BDSDN_BASE = 80000000;

	public static final int SDN_INTERNAL_EVENT = BDSDN_BASE + 1; 	
	public static final int MAP_REDUCE_CREATE = BDSDN_BASE + 2;	
	public static final int MAPPER_FINISHED_EXECUTION = BDSDN_BASE + 3;
	public static final int REDUCER_FINISHED_EXECUTION = BDSDN_BASE + 4;	
	public static final int START_TRANSMISSION = BDSDN_BASE + 5;	
	public static final int EXECUTE_REDUCER_CLOUDLET = BDSDN_BASE + 6;	
	public static final int NODE_MANAGER_INTERNAL_HEART_BEAT = BDSDN_BASE + 7;	
	public static final int INTERNAL_MAPPER_HEART_BEAT = BDSDN_BASE + 8;	
	public static final int INTERNAL_REDUCER_HEART_BEAT = BDSDN_BASE + 9;	
	public static final int EXECUTE_MAP_TASK = BDSDN_BASE + 10;	
	public static final int EXECUTE_REDUCE_TASK = BDSDN_BASE + 11;
	public static final int SDN_INTERNAL_LOAD_BALANCING = BDSDN_BASE + 12;
	public static final int INTERNAL_SDN_BW_Reservation = BDSDN_BASE + 13;
	public static final int TRANSFER_BLOCKS =  BDSDN_BASE + 14;	
	public static final int Transmission_ACK = BDSDN_BASE + 15;
	public static final int MAP_REDUCE_APP_FINISHED = BDSDN_BASE + 16;	
}
