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

package org.cloudbus.cloudsim.bigdatasdn.bdms.polocies;

import java.util.ArrayList;
import java.util.List;
import org.cloudbus.cloudsim.bigdatasdn.mapreduce.App;

/**
 * 
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since BigDataSDNSim 1.0
 */

public class MapReduceAppSelectionPolicy {
	
	private String policyName;	
	private List<App> appList = new ArrayList<>();
	private List<App> appHistory = new ArrayList<>();
	
	public MapReduceAppSelectionPolicy(){
		
	}
	
	public List<App> selectApp() {	
		List<App> temList = new ArrayList<>();
		int priority;
		boolean isPrioritized = false;
		for(App app : this.appList){
			priority = app.getAppPriority();	
			if(priority > 0){				
				isPrioritized = true;
				break;
			}				
		}
		
		if(isPrioritized == false){
			temList = this.appList;
		} else {
			temList = selectAppByPriority();
		}
		return temList;
	}	
	
	public List<App> selectAppByPriority(){
		List<App> temList = new ArrayList<>();
		List<App> appHighestList = new ArrayList<>();
		List<App> appModerateList = new ArrayList<>();
		List<App> appLowestList = new ArrayList<>();
		int priority;
		for(App app : this.appList){
			priority = app.getAppPriority();
			switch(priority){			
			case 1:
				appLowestList.add(app);
				break;
			case 2:
				appModerateList.add(app);
				break;
			case 3:
				appHighestList.add(app);
				break;
			}
		}
		
		if(appHighestList.size() > 0){
			temList = appHighestList;			
		} else if(appModerateList.size() > 0){
			temList = appModerateList;
		}  else if(appLowestList.size() > 0){
			temList = appLowestList;
		}		
		return temList;
	}
		
	public List<App> getAppList() {
		return appList;
	}
	public void addAppToList(App app) {
		this.appList.add(app);
	}
	
	public String getPolicyName() {
		return policyName;
	}
	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}
	
	public List<App> getAppHistory() {
		return appHistory;
	}
	public void addAppToHistory(App app) {
		this.appHistory.add(app);
	}
}
