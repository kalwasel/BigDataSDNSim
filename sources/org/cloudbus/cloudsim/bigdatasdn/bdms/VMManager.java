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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.bigdatasdn.bdms.polocies.CloudletSchedulerTimeSharedMutipleCPUs;

/**
* 
* @author Khaled Alwasel
* @contact kalwasel@gmail.com
* @since BigDataSDNSim 1.0
*/

public class VMManager {
	private Map<Integer, String> vmNameTable; // id to name 
	private Map<String, Integer> vmIdTable; // name to id
	protected LinkedList<Vm> vmList;
	int vmId = 0;
	private String cloudletPolicy;	
	private int vmType;

	public VMManager(String name, String cloudletPolicy) {
		vmIdTable = new HashMap<String, Integer>();
		this.cloudletPolicy = cloudletPolicy; 
	}
		
	public String getVmNameById(int vmId){
		return this.vmNameTable.get(vmId);
	}
	
	public void setVmIdByName(){
		for(Integer vmId : this.vmNameTable.keySet()){
			String vmName = this.vmNameTable.get(vmId);
			this.vmIdTable.put(vmName, vmId);
		}
	}
	
	public int getVmIdByName(String vmName){
		return this.vmIdTable.get(vmName);
	}
	

	
	public List<Vm> createVM(int vmType, int userId, int vms, int idShift) {
		vmNameTable = new HashMap<Integer, String>();
		List<Vm> vmTemList = new ArrayList<Vm>();

		double size; // image size (MB)
		int ram; // vm memory (MB)
		double mips;
		double bw;
		int pesNumber; // number of cpus
		String vmm; // VMM name
		this.vmType = vmType;
		if (vmType == 1){
			// VM Parameters
			size = 10000; // image size (MB)
			ram = 512; // vm memory (MB)
			mips = 250;
			bw = 0;
			pesNumber = 1; // number of cpus
			vmm = "Xen"; // VMM name
		} else if (vmType == 2){
			// VM Parameters
			size = 20000; // image size (MB)
			ram = 1024; // vm memory (MB)
			mips = 500;
			bw = 0;
			pesNumber = 2; // number of cpus
			vmm = "Xen"; // VMM name
			
			// Khaled 
		} else if (vmType == 3){
			size = 800000; // image size (MB)- 800 GB (SSD)
			ram = 32750; // vm memory (MB)-- 15.25 GiB (Gibibyte)
			mips = 1250; // bogomips 4 (core) * 1250 = 5000    
			bw = 0; // *.xlarge = 700-900 MBit/s (Mbps) (Moderate Network Performance) 
			pesNumber = 4; // number of cpus
			vmm = "Xen"; // VMM name
		}
		
		else {
			// VM Parameters
			size = 40000; // image size (MB)
			ram = 2048; // vm memory (MB)
			mips = 1000;
			bw = 0; // Mbps --> 1 GB
			pesNumber = 4; // number of cpus
			vmm = "Xen"; // VMM name
		}

		// create VMs
		Vm[] vm = new Vm[vms];
		String vmName;
		

		List<CloudletScheduler> clPolicyList = createCloudletPolicy(vms); 
		for (int i = 0; i < vms; i++) {
			CloudletScheduler cloudletPolicy = clPolicyList.get(i);			
			vm[i] = new Vm(idShift + i, userId, mips, pesNumber, ram,
					bw, size, vmm, cloudletPolicy, vmType);
			vmTemList.add(vm[i]);
			if(i == 0){
				// assign the first VM to HDFS 
				vmName = "HDFS";
				vmNameTable.put(i, vmName);
			} else{
				vmName = "vm_" +i;
				vmNameTable.put(i,vmName);
			}			
		}
		
		setVmIdByName();
		return vmTemList;
	}	

	private List<CloudletScheduler> createCloudletPolicy(int noVMs){
		List<CloudletScheduler> cloudletPolicy = new ArrayList<>();
		if(this.cloudletPolicy.equals("TimeSharedMutipleCPUs")){		
			for (int i = 0; i < noVMs; i++) {
				CloudletScheduler clPolicy = new CloudletSchedulerTimeSharedMutipleCPUs();
				clPolicy.setPolicyName("TimeSharedMutipleCPUs");
				cloudletPolicy.add(clPolicy);
			}
			
		} else if(this.cloudletPolicy.equals("TimeShared")){		
			for (int i = 0; i < noVMs; i++) {
				CloudletScheduler clPolicy = new CloudletSchedulerTimeShared(); 
				clPolicy.setPolicyName("TimeShared");
				cloudletPolicy.add(clPolicy);
			}
			
		} else if(this.cloudletPolicy.equals("SpaceShared")){		
			for (int i = 0; i < noVMs; i++) {
				CloudletScheduler clPolicy = new CloudletSchedulerSpaceShared(); 
				clPolicy.setPolicyName("SpaceShared");
				cloudletPolicy.add(clPolicy);
			}
		} else {
				System.out.println("Must select a cloudlet policy!");;
			cloudletPolicy = null;
		}
		return cloudletPolicy;
	}
	

	public String getCloudletPolicyName() {
		return cloudletPolicy;
	}
	
	public String getvmType() {
		if (vmType == 1)
			return "Small";
		else if (vmType == 2)
			return "Medium";
		else
			return "Large";
	}
}
