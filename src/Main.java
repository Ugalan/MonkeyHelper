import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.android.chimpchat.ChimpManager;
import com.android.chimpchat.adb.AdbBackend;
import com.android.chimpchat.adb.AdbChimpDevice;
import com.android.chimpchat.adb.LinearInterpolator;
import com.android.chimpchat.core.IChimpImage;
import com.android.chimpchat.hierarchyviewer.HierarchyViewer;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.hierarchyviewerlib.device.DeviceBridge;
import com.android.hierarchyviewerlib.device.HvDeviceFactory;
import com.android.hierarchyviewerlib.device.IHvDevice;
import com.android.hierarchyviewerlib.models.ViewNode;
import com.android.hierarchyviewerlib.models.ViewNode.Property;
import com.android.hvhelper.By;
import com.android.hvhelper.HvHelper;
import com.android.hvhelper.Id;
import com.android.hvhelper.NodeLocMap;
import com.android.hvhelper.Ref;
import com.android.hierarchyviewerlib.models.Window;

public class Main{    
	public static HvHelper hvh = null;
	public static void main(String[] args) throws Exception{
		/*BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        while(null != (line = br.readLine())) {
         }*/        
		hvh = new HvHelper("127.0.0.1:21503"); // åÐÒ£Ä£ÄâÆ÷
		Ref byRef = new Ref();
		// ViewNode node = null;
		// node = hvh.findNode(By.id(Id.showToastButton));
		if (hvh.isNodeVisibility(By.id(Id.showToastButton), byRef)){
			hvh.touch(byRef.node);
		}
		
		// NodeLocMap locMap_00 = new NodeLocMap();
		
		// Image img = hvh.captureNode(node);
		// hvh.saveImg("d:/test.jpg", img);
		
		// IChimpImage iimg = hvh.captureNode(node);
		// iimg.writeToFile("d:/test333.jpg", "png");
		
		// ViewNode node = hvh.findNode(By.text("Displays a Toast"));
		// System.out.println(node.id);
		/*ViewNode node = hvh.findNodeById(Id.list);
		NodeLocInfo locInfo_00_Id = new NodeLocInfo(Id.list, P.mID, CompType.Equals);
		NodeLocInfo locInfo_01_Id = new NodeLocInfo(Id.text1, P.mID, CompType.Equals);
		// NodeLocInfo locInfo_01_Text = new NodeLocInfo("Media", P.text_mText, CompType.Equals);
		
		NodeLocMap locMap_00 = new NodeLocMap();
		locMap_00.add(locInfo_00_Id);
		
		NodeLocMap locMap_01 = new NodeLocMap();
		locMap_01.add(locInfo_01_Id);
		// locMap_01.locInfo.add(locInfo_01_Text);		
		
		List<NodeLocMap> locMaps = new ArrayList<>();
		locMaps.add(locMap_00);
		locMaps.add(locMap_01);
		List<ViewNode> outNodes = hvh.findNodesByMaps(node, locMaps);
		
		System.out.println(outNodes.size());*/
        System.out.println("________________________________________________TEST PASS______________________________________________________");
    }
	
	public void monkeyTest() {        
		hvh._device.touch(250, 250, com.android.chimpchat.core.TouchPressType.DOWN_AND_UP); 
        IChimpImage img = hvh._device.takeSnapshot();
        String strHigh = hvh._device.getProperty("display.height");
        DeviceBridge.initDebugBridge("adb.exe");
        AndroidDebugBridge.init(false);
		AndroidDebugBridge bridge = AndroidDebugBridge.createBridge();        	
        IDevice[] iDevices = bridge.getDevices();
        for (IDevice item : iDevices){
        	System.out.println(String.format("IDevice Name = ¡¾%s¡¿", item.getName()) );	
        }
	}
}
