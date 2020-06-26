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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.bigdatasdn.bdms.BigDataManagementSystem;
import org.cloudbus.cloudsim.bigdatasdn.bdms.BigDataTask;
import org.cloudbus.cloudsim.bigdatasdn.bdms.Flow;
import org.cloudbus.cloudsim.bigdatasdn.bdms.SDNController;
import org.cloudbus.cloudsim.bigdatasdn.bdms.VMManager;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.App;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.AppsParser;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.Mapper;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.Reducer;

/**
 * 
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */

public class PrintResults {	
	private String appFilename;
	private SDNController sdnController;
	private BigDataManagementSystem bdms;
	private VMManager vmManager;
	private VmAllocationPolicy vmPolicy;
	
	
	public PrintResults(SDNController sdnController, BigDataManagementSystem bdms,
		VMManager vmManager, VmAllocationPolicy vmPolicy)  {
		this.sdnController = sdnController;
		this.bdms = bdms;
		this.vmManager = vmManager;
		this.vmPolicy = vmPolicy;
	}
	
	public void printAllResults()  throws IOException{
		setFileName();		
		printSDNNetworkPackages();			
        printApps();
        printMapTaskApp();		
        printReduceTaskApp();	
        printCSVFile();
	}
		

	public void printSDNNetworkPackages(){
		List<Flow> SDNNetworkPackages = sdnController.getStoredFlowsHis();
		Log.printLine();		
		Log.printLine("=========================== SDN Network Results ========================");	
		Log.printLine(String.format("%1s %12s %23s %27s %19s %22s %27s %33s %27s %27s", "App_Name"   
				, "From"  
				, "To"  
				, "WorkloadSize_MB"  
				, "Datacenter"  
				, "Application_Master"  
				,"Node_Manager"  
				,"SDN_Start_Time" 
				,"SDN_Execution_Time" 
				, "SDN_Finish_Time"));
	
//		new DecimalFormat("0.00").format(App.getEndTime())
		for (int i = 0; i < SDNNetworkPackages.size(); i++) {
			Flow flow = SDNNetworkPackages.get(i);
			if(flow.getNodeManager() != null){
			Log.printLine(String.format("%1s %15s %29s %20s %21s %25s %26s %26s %26s %26s", flow.getAppName() 
					, flow.getAppNameSrc() 
					, flow.getAppNameDest()  
					, flow.getSize()  
					, flow.getAm().datacenter.getName()   			
					, flow.getAm().getName()   
					, flow.getNodeManager().getName() 
					, new DecimalFormat("0.00").format(flow.getStartTime()) 
					, new DecimalFormat("0.00").format(flow.getFinishTime() -  flow.getStartTime())   
					, new DecimalFormat("0.00").format(flow.getFinishTime())));
			} 
		}
	}
	
	public void printApps() {		
		Log.printLine();
		Log.printLine("=========================== MapReduce Processing Outputs ========================");
		Log.printLine(String.format("%1s %23s %23s %23s %23s %21s %25s %33s %38s %37s"
				, "app_Name"
				, "Number_of_Mappers"
				, "Number_of_Reducers"
				, "app_Submit_time"
				, "app_Start_time" 
				, "app_End_Time"  
				, "app_Total_Time"
				, "HDFS_To_Mappers_Network_Time"  
				, "Mappers_To_Reducers_Network_Time"  
				, "Reducers_To_HDFS_Network_Time"  
				));
		for(App app : AppsParser.appList){		
			double hdfsMaxNetworkTime = app.getNetworkStatistics().getNetworkTime("vm");
			double mappersMaxNetworkTime = app.getNetworkStatistics().getNetworkTime("reducer");
			double reducersMaxNetworkTime = app.getNetworkStatistics().getNetworkTime("hdfs");
				Log.printLine(String.format("%1s %21s %19s %25s %25s %24s %23s %25s %34s %40s"
						, app.getAppName()
						, app.getNumberofMapTask()
						, app.getNumberOfReduceTask()
						, app.getAppSubmitTime()
						,  new DecimalFormat("0.00").format(app.getAppStartTime())
						, new DecimalFormat("0.00").format(app.getEndTime())
						, new DecimalFormat("0.00").format(app.getEndTime() - app.getAppStartTime())
						, new DecimalFormat("0.00").format(hdfsMaxNetworkTime)
						, new DecimalFormat("0.00").format(mappersMaxNetworkTime)
						, new DecimalFormat("0.00").format(reducersMaxNetworkTime)															
						));
		}
		
	}

	
	public  void printMapTaskApp() {		
		String indent = "     ";
		Log.printLine();
		Log.printLine("=========================== Mappers Outputs ========================");

		Log.printLine();
		
		
		Log.printLine((String.format("%1s %15s %15s %29s %16s   %24s  %20s  %20s %18s  %23s  %20s  %33s  %33s "
				,"app_Name" 
				,"app_ID" 
				,"Map_ID" 
				, "MapTask_Length(MI)" 
				, "Type"
				, "Datacenter_ID"
				, "HOST_ID" 
				, "VM_ID" 
				, "VM_Type" 
				, "Delay_Time" 
				, "Start_Time" 
				, "Execution_Time"
				, "Finish_Time")));

		DecimalFormat dft = new DecimalFormat("0.00");
		for (App app : AppsParser.appList){			
			List<Mapper> mappers = app.getMapperList();
			for (Mapper mp : mappers) {					
				BigDataTask task = mp.getMapTaskCloudlet();
				Log.printLine((String.format("%1s %15s %18s %23s %22s %21s  %21s  %22s %20s  %20s  %20s  %35s  %35s"
						, task.getAppName()  
						, task.getAppId()  
						, task.getMapReduceTask().getTaskName()
						, indent + dft.format(task.getCloudletLength())						 
						, mp.getTaskType()
						, task.getResourceId()							
						, task.getHostId()  
						, task.getVmId()  
						, vmManager.getvmType()							
						, dft.format(task.getExecStartTime() - task.getSubmissionTime()) 							
						, dft.format(task.getExecStartTime()) 							
						, dft.format(task.getActualCPUTime())							
						, dft.format(task.getFinishTime()))));			
			}		
		}
	}

	
	public void printReduceTaskApp() {		
		Log.printLine();

		Log.printLine("============================== Reducers Outputs ======================================");
		Log.printLine((String.format("%1s %15s %15s %32s %14s %29s  %19s  %18s  %17s %25s  %27s  %30s  %29s "
				, "app_Name"
				, "app_ID"  
				, "Reduce_ID" 				
				, "ReduceTask_Length(MI)" 
				, "Type" 
				, "Datacenter_ID"
				, "HOST_ID"  
				, "VM_ID"
				, "VM_Type" 
				, "Delay_Time"
				, "Start_Time"
				, "Execution_Time"
				, "Finish_Time")));

		DecimalFormat dft = new DecimalFormat("0.00");
		for (App app : AppsParser.appList){
			List<Reducer> reducers = app.getReducerList();
			for (Reducer rd : reducers) {	
				BigDataTask task = rd.getReduceTaskCloudlet();
			
				Log.printLine((String.format("%1s %15s %15s %32s %18s %26s  %19s  %18s  %19s %22s  %30s  %30s  %30s "
						, task.getAppName()  
						, task.getAppId() 
						, rd.getName()  
						, dft.format(task.getCloudletLength())					
						, rd.getTaskType()
						, task.getResourceId()
						, task.getHostId()
						, task.getVmId()						
						, vmManager.getvmType()	 							
						, dft.format(task.getExecStartTime() - task.getSubmissionTime()) 							
						, dft.format(task.getExecStartTime()) 							
						, dft.format(task.getActualCPUTime()) 							
						, dft.format(task.getFinishTime()))));				

			}
		}
	}


/*
 * Print results to a CSV file 
 * 
 */

	XSSFWorkbook workbook = new XSSFWorkbook();
    int rowNum = 0;
    XSSFRow row;

	private void printCSVFile() throws IOException{
		
	    try {
	    	String[] mainRow = {
	    			  "appIndexing"
	    			, "app_Name"
	    			, "app_Total_Network_Size_GB"
	    			, "app_Total_Size_MIPS"
	    			, "NumberOfMappers"
	    			, "NumberOfReducers"
	    			, "app_Submit_time_Second"
	    			, "app_MapReduce_WaitingTime"
	    			, "app_Start_time_Second" 					
					, "SDN_HDFS_To_Mapper_WaitingTime"
					, "From_HDFS_To_Mapper_SDNnetwork_Time_Second"
					, "SDN_Mapper_To_Reducer_WaitingTime"
					, "From_Mappers_To_Reducers_SDNnetwork_Time_Second"
					, "SDN_Reducer_To_HDFS_WaitingTime"
					, "From_Reducers_To_HDFS_SDNnetwork_Time_Second"					
					, "Total_SDN_Transfer_Time_Second"
					, "app_End_Time_Second"  
					, "app_Total_Time_Second"};		    			    
	    	
	        XSSFSheet sheet = workbook.createSheet("apps");
			row = sheet.createRow((short)rowNum); 
            for(int i = 0; i < mainRow.length; i++) {		            
				row.createCell(i).setCellValue(mainRow[i]);	            	
            }	                  
		int countAppRow = 1;
		int countApp = 0;
		for (App app : AppsParser.appList){					
				row = sheet.createRow(countAppRow++);
				row.createCell(0).setCellValue(countApp++);
				row.createCell(1).setCellValue(app.getAppName());
				row.createCell(2).setCellValue(app.getNetworkStatistics().getTotalFileNetworkData()/1000);
				row.createCell(3).setCellValue(app.getAppMipsTotalLength());					
				row.createCell(4).setCellValue(app.getNumberofMapTask());
				row.createCell(5).setCellValue(app.getNumberOfReduceTask());					
				row.createCell(6).setCellValue((app.getAppSubmitTime()));																								
				row.createCell(7).setCellValue(app.getAppStartTime() - app.getAppSubmitTime()); 							
				row.createCell(8).setCellValue(app.getAppStartTime());
				// SDN_Storage_To_Mapper_WaitingTime (queue time)
				
				double SDN_HDFS_To_Mapper_WaitingTime = app.getNetworkStatistics().getFlowStartTime("vm") - app.getAppStartTime();
				row.createCell(9).setCellValue(SDN_HDFS_To_Mapper_WaitingTime);					
				
				// From Storage To_mapper_sdn transfer time 
				double hdfsMaxNetworkTime = app.getNetworkStatistics().getNetworkTime("vm");	
				row.createCell(10).setCellValue(hdfsMaxNetworkTime);
					
				// SDN_Mapper_To_Reducer_WaitingTime			
				double MrFlowWaitingTime = app.getNetworkStatistics().getFlowStartTime("reducer") - app.getFirstMapperFinishTime();
				row.createCell(11).setCellValue(MrFlowWaitingTime);
								
				//From_Mappers_To_Reducers_SDNnetwork_Time_inSec	
				double mappersMaxNetworkTime = app.getNetworkStatistics().getNetworkTime("reducer");
				row.createCell(12).setCellValue(mappersMaxNetworkTime);
									
				// SDN_Reducer_To_HDFS_WaitingTime
				double RhdfsFlowWaitingTime = app.getNetworkStatistics().getFlowStartTime("hdfs") - app.getFirstReducerFinishTime();
				
				row.createCell(13).setCellValue(RhdfsFlowWaitingTime);
									
				// From_Reducers_To_HDFS_SDNnetwork_Time_inSec
				double reducersMaxNetworkTime = app.getNetworkStatistics().getNetworkTime("hdfs");
				row.createCell(14).setCellValue(reducersMaxNetworkTime);
				
				double networkTotalTransmissionTime = hdfsMaxNetworkTime + mappersMaxNetworkTime + reducersMaxNetworkTime ;
				row.createCell(15).setCellValue(networkTotalTransmissionTime);  
	
				
				// app_End_Time_Second  
				row.createCell(16).setCellValue(app.getEndTime());  
				
				// app_Total_Time_Second
				// get the start time of  transmission from storage to mapper to get app total execution time!
				row.createCell(17).setCellValue(app.getEndTime() - app.getAppStartTime());				
			}						

		    	String[] mainRowMap = {"app_Name" 
						,"app_ID" 
						,"Map_ID" 
						, "MapTask_Length(MI)" 
						, "MapTask_OutPut_File_Size"  
						, "Type"
						, "Datacenter_ID"
						, "HOST_ID" 
						, "VM_ID" 
						, "VM_Type" 
						, "Delay_Time" 
						, "Start_Time_InSec" 
						, "Execution_Time_InSec"
						, "Finish_Time_InSec"};		    			    
		    	
		    	XSSFSheet  sheetMap = workbook.createSheet("mapTasks");  	            
	            int rowNumMap = 0;
	            XSSFRow  rowMap = sheetMap.createRow((short)rowNumMap);	       
	            for(int i = 0; i < mainRowMap.length; i++) {		            
	            	rowMap.createCell(i).setCellValue(mainRowMap[i]);	            	
	            }	            
	            int mapCount = 1;	            
	    		DecimalFormat dft = new DecimalFormat("0.00");
	    		for (App app : AppsParser.appList){			
	    			List<Mapper> mappers = app.getMapperList();
	    			for (Mapper mp : mappers) {	
	    				BigDataTask task = mp.getMapTaskCloudlet();
	    				rowMap = sheetMap.createRow(mapCount++);
	    				rowMap.createCell(0).setCellValue(task.getAppName());
	    				rowMap.createCell(1).setCellValue(task.getAppId() );
	    				rowMap.createCell(2).setCellValue(task.getCloudletId());
	    				rowMap.createCell(3).setCellValue(dft.format(task.getCloudletLength()));
	 						rowMap.createCell(4).setCellValue(dft.format(task.getCloudletOutputSize()));
	 						rowMap.createCell(5).setCellValue(mp.getTaskType());
	    					rowMap.createCell(6).setCellValue(task.getResourceId());
	    					rowMap.createCell(7).setCellValue(task.getHostId());
	    					rowMap.createCell(8).setCellValue(task.getVmId());
	    					rowMap.createCell(9).setCellValue(vmManager.getvmType()	);
	    					rowMap.createCell(10).setCellValue(dft.format(task.getExecStartTime() - task.getSubmissionTime()));
	    					rowMap.createCell(11).setCellValue((Double.parseDouble(dft.format(task.getExecStartTime()))));	 
	    					rowMap.createCell(12).setCellValue((Double.parseDouble(dft.format(task.getActualCPUTime()))));
	    					rowMap.createCell(13).setCellValue((Double.parseDouble(dft.format(task.getFinishTime()))));	    				    				
	    			}
	    		}	            	            	    	
	    	    		
		    	String[] mainRowReduce = {"app_Name"
						, "app_ID"  
						, "Redcue_ID" 				
						, "ReduceTask_Length(MI)" 
						, "Type" 
						, "Datacenter_ID"
						, "HOST_ID"  
						, "VM_ID"
						, "VM_Type" 
						, "Delay_Time"
						, "Start_Time_InSec"
						, "Execution_Time_InSec"
						, "Finish_Time_InSec"};		    			    
		    	
		    
		    	XSSFSheet  sheetReduce = workbook.createSheet("reduceTasks");  	            
	            int rowNumReduce = 0;
	            XSSFRow  rowReduce = sheetReduce.createRow((short)rowNumReduce);	       
	            for(int i = 0; i < mainRowReduce.length; i++) {		            
	            	rowReduce.createCell(i).setCellValue(mainRowReduce[i]);	            	
	            }	            
	        	int reduceCount = 1;
    
	        	//DecimalFormat dft = new DecimalFormat("######.###");
	    		for (App app : AppsParser.appList){
	    			List<Reducer> reducers = app.getReducerList();
	    			for (Reducer rd : reducers) {	
	    				BigDataTask task = rd.getReduceTaskCloudlet();	    			
	    				rowReduce = sheetReduce.createRow(reduceCount++);
	    				rowReduce.createCell(0).setCellValue(task.getAppName());
	    				rowReduce.createCell(1).setCellValue(task.getAppId() );
	    				rowReduce.createCell(2).setCellValue(task.getCloudletId());
	    				rowReduce.createCell(3).setCellValue(dft.format(task.getCloudletLength()));
	    				rowReduce.createCell(4).setCellValue(rd.getTaskType());
						    				
    					rowReduce.createCell(5).setCellValue(task.getResourceId());
    					rowReduce.createCell(6).setCellValue(task.getHostId());
    					rowReduce.createCell(7).setCellValue(task.getVmId());
    					rowReduce.createCell(8).setCellValue(vmManager.getvmType()	);
    					rowReduce.createCell(9).setCellValue(dft.format(task.getExecStartTime() - task.getSubmissionTime()));
    					rowReduce.createCell(10).setCellValue((Double.parseDouble(dft.format(task.getExecStartTime()))));
    					rowReduce.createCell(11).setCellValue((Double.parseDouble(dft.format(task.getActualCPUTime()))));
    					rowReduce.createCell(12).setCellValue((Double.parseDouble(dft.format(task.getFinishTime()))));	    			    												
	    			}
	    		}

	        	
	    // print configuration 
    	String[] mainRowConfig = {
    			  "NumberOfDatacenters"
    			, "NumberOfHosts"
    			, "NumberOfVMs"
    			, "NumberOfNodeManager"    			
    			, "SDN_Routing_Algorithm"
    			, "SDN_MapReduce_Traffic_Algorithm"
    			, "app_Selection_Algorithm"
    			, "app_Data_Placement_Algorithm"
    			, "TaskTracker_MapReduce_Execution_Algorithm"
    			, "HostVmAllocationPolicy"
    			};		    			    		    		        	
    	 XSSFSheet  sheetConfig = workbook.createSheet("appConfigration");  
    	 int rowNumConfig = 0;
         XSSFRow  rowConfig = sheetConfig.createRow((short)rowNumConfig);	       
         for(int i = 0; i < mainRowConfig.length; i++) {		            
        	 rowConfig.createCell(i).setCellValue(mainRowConfig[i]);	            	
         }	               	 
    	int configCount = 1;	
			rowConfig = sheetConfig.createRow(configCount++);
			
			int numDatacenters = this.bdms.getNumOfDatacenters();
			rowConfig.createCell(0).setCellValue(numDatacenters);
			
			int numOfHosts = this.bdms.getDatacenter().getHostList().size();
			rowConfig.createCell(1).setCellValue(numOfHosts);
			
			int numOfVMs = this.bdms.getDatacenter().getVmList().size();
			rowConfig.createCell(2).setCellValue(numOfVMs);
			
			int numNodeManager = this.bdms.getNodeManagerList().size();
			rowConfig.createCell(3).setCellValue(numNodeManager);
								
			rowConfig.createCell(4).setCellValue(this.bdms.getSDNController().getSdnRoutingPoloicy().getPolicyName());						
			rowConfig.createCell(5).setCellValue(this.bdms.getSDNController().getSdnSchedulingPolicy().getPolicyName());
									
			rowConfig.createCell(6).setCellValue(bdms.getMrAppSelectionPolicy().getPolicyName());
			rowConfig.createCell(7).setCellValue(bdms.getHdfs().getDataPlacementPolicy().getPolicyName());
			rowConfig.createCell(8).setCellValue(vmManager.getCloudletPolicyName());
			rowConfig.createCell(9).setCellValue(vmPolicy.getPolicyName() );
						
        FileOutputStream fileOut = new FileOutputStream(appFilename);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();	            	            
    } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }				
	}
	
	private void setFileName () throws IOException{
		String fcfs_CountName = "outputFiles/countFileNames/FCFC_FileName.txt";
		String fairShare_CountName = "outputFiles/countFileNames/FairShare_FileName.txt";		
		String loadBalancing_CountName = "outputFiles/countFileNames/LoadBalancing_FileName.txt";
		
        FileReader fileReaderFCFS = new FileReader(fcfs_CountName);
        FileReader fileReaderFairShare = new FileReader(fairShare_CountName);
        FileReader fileReaderLoadBalancing = new FileReader(loadBalancing_CountName);

        BufferedReader bufferedReaderFCFS = new BufferedReader(fileReaderFCFS);
        BufferedReader bufferedReaderFairShare = new BufferedReader(fileReaderFairShare);
        BufferedReader bufferedReaderLoadBalancing = new BufferedReader(fileReaderLoadBalancing);
        
//        String lineFCFS = bufferedReaderFCFS.readLine();   
        String lineFairShare =  bufferedReaderFairShare.readLine();
        String lineLoadBalancing =  bufferedReaderLoadBalancing.readLine();

    	
    	
    	int value;
    	String sdnAppPolicyName = sdnController.getSdnSchedulingPolicy().getPolicyName();
    	switch(sdnAppPolicyName){
	    	case "FairShair":				
				this.appFilename = "outputFiles/appsResults_FairShare_" + lineFairShare + ".xlsx" ;
				   File fileFairShare = new File(fairShare_CountName);
		            BufferedWriter outputFairShare = new BufferedWriter(new FileWriter(fileFairShare));
		            value = Integer.parseInt(lineFairShare) +1;
		            outputFairShare.write(Integer.toString(value));            	            
		            outputFairShare.close();
	    		break;
	    		
	    	case "LoadBalancing":        		  
				this.appFilename = "outputFiles/appsResults_LoadBalancing_" + lineLoadBalancing + ".xlsx" ;
			    File fileLoadBalancing = new File(loadBalancing_CountName);
	            BufferedWriter outputLoadBalancing = new BufferedWriter(new FileWriter(fileLoadBalancing));
	            value = Integer.parseInt(lineLoadBalancing) +1;                
	            outputLoadBalancing.write(Integer.toString(value));
	            outputLoadBalancing.close();
			break;
    	}
        
        bufferedReaderFCFS.close();
        bufferedReaderFairShare.close();
        bufferedReaderLoadBalancing.close();
		 
	}


}
