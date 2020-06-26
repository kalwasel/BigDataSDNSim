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


package org.cloudbus.cloudsim.bigdatasdn.mapreduce;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */


public class AppsParser {
	public static List<App> appList = new ArrayList<App>();
//	public static int numberOfMapper;
//	public static int numberReducer;	
	
	public static void startParsingExcelAppFile(String appFileName){
			
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(appFileName));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String line;
			try {
				@SuppressWarnings("unused")
				String head = br.readLine();
				
				int appID;
				int repliaction;
				String appType;				
				double appStartTime;
				int CPUno;
				double mapInputBytes; // convert to bit * 80
				long mapMips;
				
				double suffleBytes; // convert to bit * 80
				long reduceMips; 
				
				double reduceOutputBytes; // convert to bit * 80
				int numberOfMappers;
				int numberOfReducers;
				int priority;
				double hdfsBlockSize;
				
				while ((line = br.readLine()) != null) {					
					String[] splitLine = line.split(",");
					Queue<String> lineitems = new LinkedList<String>(Arrays.asList(splitLine));
					// app0,49,740773,1,2339561,1,627471,0,0,0,None
					if(lineitems.isEmpty()){
						break;
					}
					repliaction =  Integer.parseInt(lineitems.poll());
					appType =  lineitems.poll();
					
					appID =  Integer.parseInt(lineitems.poll());
					appStartTime = Double.parseDouble(lineitems.poll());
					CPUno = Integer.parseInt(lineitems.poll());
					mapInputBytes = Double.parseDouble(lineitems.poll());
					mapMips = Long.parseLong(lineitems.poll());

					suffleBytes = Double.parseDouble(lineitems.poll());
					reduceMips = Long.parseLong(lineitems.poll());
					reduceOutputBytes = Double.parseDouble(lineitems.poll());
					
					numberOfMappers = Integer.parseInt(lineitems.poll());
					numberOfReducers = Integer.parseInt(lineitems.poll());
					priority = Integer.parseInt(lineitems.poll());				
					hdfsBlockSize = Double.parseDouble(lineitems.poll()); 

					// convert data from MB to bits 
					mapInputBytes = mapInputBytes * 1000;
					suffleBytes = suffleBytes * 1000;
					reduceOutputBytes = reduceOutputBytes * 1000;
					
					
					
					App app = new App(appID,	
							repliaction,
							appType,							
							appStartTime,
							CPUno,
							mapInputBytes,
							mapMips, 
							suffleBytes,
							reduceMips,
							reduceOutputBytes,							
							numberOfMappers,
							numberOfReducers, 
							priority, 
							hdfsBlockSize					
							);				
//					numberOfMapper = numberOfMappers;
//					numberReducer = numberOfReducers;
					appList.add(app);										
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("");	
	}
}
