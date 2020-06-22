package com.android.hvhelper;
import java.util.ArrayList;
import java.util.List;

public class NodeLocMap {
	List<NodeLocInfo> locInfo = new ArrayList<>();
	boolean byAnyOne = false;
	
	public NodeLocMap(){
	}
	
	public NodeLocMap(boolean byAnyOne){
		this.byAnyOne = byAnyOne;
	}
	
	public void add(NodeLocInfo locInfo){
		this.locInfo.add(locInfo);
	}
}
