import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.graphics.Point;

import com.android.chimpchat.ChimpManager;
import com.android.chimpchat.adb.AdbBackend;
import com.android.chimpchat.adb.AdbChimpDevice;
import com.android.chimpchat.core.IChimpImage;
import com.android.chimpchat.hierarchyviewer.HierarchyViewer;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.hierarchyviewerlib.device.DeviceBridge;
import com.android.hierarchyviewerlib.device.HvDeviceFactory;
import com.android.hierarchyviewerlib.device.IHvDevice;
import com.android.hierarchyviewerlib.device.ViewServerDevice;
import com.android.hierarchyviewerlib.models.ViewNode;
import com.android.hierarchyviewerlib.models.Window;
import com.android.hierarchyviewerlib.models.ViewNode.Property;

/**
 * @author Ugalan
 *
 */
/**
 * @author Ugalan
 *
 */
public class HvHelper {
	class ChimpManagerEx extends ChimpManager {
		public ChimpManagerEx(Socket monkeySocket) throws IOException {
			super(monkeySocket);
			// TODO 自动生成的构造函数存根
		}
	}
	
	private String _sn = null;
	public AdbChimpDevice _device = null;
	private AdbBackend _adb = null;
	private HierarchyViewer _view = null;
	static ChimpManager _mgr = null;
	public static IDevice _iDevice;
	IHvDevice _ihvDevice = null;
	
	public HvHelper(String sn) throws Exception{
		this._sn = sn;
		initDevice() ;
	}
	
	public void initDevice() throws Exception {
        _adb = new AdbBackend(); 
        _device = (AdbChimpDevice) _adb.waitForConnection(10000, _sn);
        _mgr = _device.getManager(); // .tap(250, 250);
        _view = _device.getHierarchyViewer();
        _iDevice = getIDevice(_sn);
        _ihvDevice = HvDeviceFactory.create(_iDevice);
        // _ihvDevice.initializeViewDebug();
	}
	
    public void clearEnv()
    {
        // DeviceBridge.stopListenForDevices();
        DeviceBridge.stopViewServer(_iDevice); // 关闭ViewServer
        // AndroidDebugBridge.disconnectBridge(); // 断开adb连接
        // AndroidDebugBridge.terminate();
    }
	
    private IDevice getIDevice(String sn) throws Exception{
        AndroidDebugBridge bridge = AndroidDebugBridge.createBridge();
        waitDevicesList(bridge);
        IDevice[] devices = bridge.getDevices();

        for (IDevice item : devices){
            if (item.getSerialNumber() == sn)
                return item;
        }

        return null;
    }
	
	/** monkey --port 12345 卡死的问题
	 * */
	public void startChimpDevice() {
		ExecutorService exec = Executors.newFixedThreadPool(1);
 
		Callable<String> call = new Callable<String>() {
			public String call() throws Exception {
				_device = (AdbChimpDevice) _adb.waitForConnection(5000, "127.0.0.1:21503");
				return "Done";
			}
		};
 
		try {
			Future<String> future = exec.submit(call);
			String obj = future.get(60 * 1000 * 10, TimeUnit.MILLISECONDS);
		} 
		catch (Exception e) {
			AndroidDebugBridge.getBridge().restart(); // adb kill-server, adb start-server
			e.printStackTrace();
		}
		finally{
			exec.shutdown(); // 关闭线程池
		}		
	}
	
	private static IDevice getDevice(int index) {
		IDevice device = null;
		AndroidDebugBridge bridge = AndroidDebugBridge.createBridge();
		waitDevicesList(bridge);
		IDevice devices[] = bridge.getDevices();
		
		if(devices.length < index){
			System.err.print("没有检测到第" + index + "个设备");
		}
		else{
			device = devices[index];
		}
		
		return device;
	}
	
	private static void waitDevicesList(AndroidDebugBridge bridge) {
		int count = 0;
		while (bridge.hasInitialDeviceList() == false) {
			try {
				Thread.sleep(500);
				count++;
			 } 
			catch (InterruptedException e) {
			}
			
			if (count > 10) {
				System.err.print("等待获取设备超时");
				break;
			}
		}
	}
	
    public Window getFocusedWindow(){
        // return DeviceBridge.loadWindows(ihvDevice, iDevice)[DeviceBridge.getFocusedWindow(iDevice)];
        return getWindow(_view.getFocusedWindowName(), CompType.Equals); // 待解决：未考虑多个窗体名一样的情况
    }
    
    public ViewNode getRootNode(){
        return DeviceBridge.loadWindowData( new Window(new ViewServerDevice(_iDevice), "", 0xffffffff));
    }
	
	private static Window getWindow(String windowName, CompType ct) {
        IDevice iDevice = null;	
        iDevice = getDevice(0);
        System.out.println(String.format("IDevice Name = 【%s】", iDevice.getName()) );
        IHvDevice viewDevice = HvDeviceFactory.create(iDevice);
        viewDevice.initializeViewDebug();
        Window[] windows =  DeviceBridge.loadWindows(viewDevice, iDevice);
        
        for (Window item : windows){
        	String curName = item.getTitle();
        	System.out.println(String.format("Window Title = 【%s】", curName) );
        	if (Compare(windowName, curName, ct) ){
        		return item;
        	}
        }
        return null;
	}
	
	/**
	 * @param component
	 */
	public void startActivity(String component) {
		// "io.selendroid.testapp/.HomeScreenActivity"
		// "io.selendroid.testapp/.WebViewActivity"
		String action = "android.intent.action.MAIN";   // 添加启动权限
        Collection<String> categories = new ArrayList<String>();   
        categories.add("android.intent.category.LAUNCHER");
        _device.startActivity(null, action, null, null, categories,  new HashMap<String, Object>(), component, 0);
	}
	
	public void startActivityByAdb(String component) {
		_device.shell("am start -n " + component); // io.selendroid.testapp/.WebViewActivity
	}
	
	public String getProperty(ViewNode node, String propertyName) {
		Map<String, Property> nodeInfo = node.namedProperties;
		
		if (nodeInfo.containsKey(propertyName)){
			return nodeInfo.get(propertyName).value;
		}
		
		return HvStr.NULL;
	}
	
	public String getProperty(ViewNode node, P ppyName) {		
		return getProperty(node, ppyName.value);
	}
	
	public ViewNode findNodeByWindow(String windowName) {
		// "io.selendroid.testapp/io.selendroid.testapp.HomeScreenActivity"
        return DeviceBridge.loadWindowData(getWindow(windowName, CompType.Equals));         
	}
	
	public static boolean isMatch(String str, String regex) {
		return isMatch(str, regex, false);
	}
	
	/**
	 * 判断str字符串是否能够被regex匹配
	 * 如a*b?d可以匹配aAAAbcd
	 * @param str 任意字符串
	 * @param regex 包含*或？的匹配表达式
	 * @param ignoreCase 大小写敏感
	 * @return
	 */
	public static boolean isMatch(String str, String regex, boolean ignoreCase) {
		if (str == null || regex == null){
			return false;
		}
		if (ignoreCase) {
			str = str.toLowerCase();
			regex = regex.toLowerCase();
		}
		return matches(str, regex.replaceAll("(^|([^\\\\]))[\\*]{2,}", "$2*"));// 去除多余*号
	}

	private static boolean matches(String str, String regex) {
		// 如果str与regex完全相等，且str不包含反斜杠，则返回true。
		if (str.equals(regex) && str.indexOf('\\') < 0) {
			return true;
		}
		
		int rIdx = 0, sIdx = 0; // 同时遍历源字符串与匹配表达式
		while (rIdx < regex.length() && sIdx < str.length()) {
			char c = regex.charAt(rIdx); // 以匹配表达式为主导
			switch (c) {
			case '*': // 匹配到*号进入下一层递归
				String tempSource = str.substring(sIdx); // 去除前面已经完全匹配的前缀
				String tempRegex = regex.substring(rIdx + 1); // 从星号后一位开始认为是新的匹配表达式
				for (int j = 0; j <= tempSource.length(); j++) { // 此处等号不能缺，如（ABCD，*），等号能达成("", *)条件
					if (matches(tempSource.substring(j), tempRegex)) { // 很普通的递归思路
						return true;
					}
				}
				return false; // 排除所有潜在可能性，则返回false
			case '?':
				break;
			case '\\': // 匹配到反斜杠跳过一位，匹配下一个字符串
				c = regex.charAt(++rIdx);
			default:
				if (str.charAt(sIdx) != c) {
					return false;// 普通字符的匹配
				}
			}
			rIdx++;
			sIdx++;
		}
		// 最终str被匹配完全，而regex也被匹配完整或只剩一个*号
		return str.length() == sIdx
				&& (regex.length() == rIdx || regex.length() == rIdx + 1 && regex.charAt(rIdx) == '*');
	}
	
	public static boolean Compare(String expectStr, String realStr, CompType ct)
    {
        switch (ct) {
            case Equals: {
                    return expectStr.equals(realStr);
                }                    
            case Contains: {
                    return realStr.contains(expectStr);
                }
            case Asterisk:{
                    return isMatch(realStr, expectStr);
                }
            default:
                break;
        }
        
        return false;
    }
	
	public String getText(ViewNode node) {
        return _view.getText(node);      
	}
	
	public Point getNodePoint(ViewNode node) {
        return HierarchyViewer.getAbsolutePositionOfView(node);  
	}
	
    private void findNodesByPpy( String expcVal, ViewNode parNode, P ppyName, CompType ct, List<ViewNode> outNodes){    	
        if (Compare(expcVal, getProperty(parNode, ppyName), ct)){
        	outNodes.add(parNode);
        }
        
        for (ViewNode child : parNode.children) {
        	findNodesByPpy(expcVal, child, ppyName, ct, outNodes);
        }
        
        // return outNodes;
    }
	
    public List<ViewNode> findNodesByPpy( String expcVal, ViewNode parNode, P ppyName, CompType ct){
    	List<ViewNode> outNodes = new ArrayList<>();        
        findNodesByPpy(expcVal, parNode, ppyName, ct, outNodes);
        return outNodes;
    }    
    
    private boolean isNodeMatch( ViewNode node, NodeLocMap locMap){    	
    	for (NodeLocInfo locInfo : locMap.locInfo){ // 多种属性定位一个节点
            if (!Compare(locInfo.expcVal, getProperty(node, locInfo.p), locInfo.ct)){
            	return false;
            }               
    	}        
    	
        return true;
    }
    
    public void findNodesByMap( ViewNode parNode, NodeLocMap locMap, List<ViewNode> outNodes){
        if (isNodeMatch(parNode, locMap)){
        	outNodes.add(parNode);
        }
        
        for (ViewNode child : parNode.children) {
        	findNodesByMap(child, locMap, outNodes);
        }
    }
    
    public List<ViewNode> findNodesByMap( ViewNode parNode, NodeLocMap locMap){
    	List<ViewNode> outNodes = new ArrayList<>();        
    	findNodesByMap( parNode, locMap, outNodes);
        return outNodes;
    }
    
    public void findNodesByMaps( ViewNode parNode, List<NodeLocMap> locMaps, int locIndex, List<ViewNode> outNodes){
    	int mapSize = locMaps.size();
    	int lastIndex = mapSize-1;
        if (locIndex==lastIndex){
        	List<ViewNode> curNodes = findNodesByMap(parNode, locMaps.get(lastIndex));
        	outNodes.addAll(curNodes);
        	return;
        }
    	
		for (int i=locIndex; i<mapSize; i++){
    		List<ViewNode> curNodes = findNodesByMap(parNode, locMaps.get(i));
    		for (ViewNode node : curNodes){
    			findNodesByMaps(node, locMaps, i+1, outNodes);
    		}
		}
    }
    
    /**
	* ViewNode node = hvh.findNodeById(Id.content);
	* NodeLocInfo locInfo_00_Id = new NodeLocInfo(Id.visibleTestArea2, P.mID, CompType.Equals);
	* NodeLocInfo locInfo_01_Id = new NodeLocInfo(Id.showToastButton, P.mID, CompType.Equals);
	* NodeLocInfo locInfo_01_Text = new NodeLocInfo("Displays a Toast", P.text_mText, CompType.Equals);
	*
	* NodeLocMap locMap_00 = new NodeLocMap();
	* locMap_00.locInfo.add(locInfo_00_Id);
	*
	* NodeLocMap locMap_01 = new NodeLocMap();
	* locMap_01.locInfo.add(locInfo_01_Id);
	* locMap_01.locInfo.add(locInfo_01_Text);		
	*
	* List<NodeLocMap> locMaps = new ArrayList<>();
	* locMaps.add(locMap_00);
	* locMaps.add(locMap_01);
	* List<ViewNode> outNodes = hvh.findNodesByMaps(node, locMaps);
    * */
    public List<ViewNode> findNodesByMaps( ViewNode parNode, List<NodeLocMap> locMaps){
    	List<ViewNode> outNodes = new ArrayList<>();        
    	findNodesByMaps( parNode, locMaps, 0, outNodes);
        return outNodes;
    }    
	
    public ViewNode findNodeByPpy(String expcVal,  ViewNode parNode, P ppyName, CompType ct){
    	// this.left = this.namedProperties.containsKey("mLeft") ? this.getInt("mLeft", 0) : this.getInt("layout:mLeft", 0);
        if (Compare(expcVal, getProperty(parNode, ppyName), ct)){
        	return parNode;
        }        
        
        for (ViewNode child : parNode.children) {
            ViewNode found = findNodeByPpy(expcVal, child, ppyName, ct);
            if (found != null) {
                return found;
            }
        }
        
        return null;
    }
    
    public ViewNode findNodeByPpy(String expcVal, P ppyName, CompType ct){
    	ViewNode parNode = this.getRootNode();
    	return findNodeByPpy(expcVal, parNode, ppyName, ct);
    }
    
    public ViewNode findNodeByPpy(String expcVal, String windowName, P ppyName,  CompType ct){
    	ViewNode parNode = this.findNodeByWindow(windowName);        
        return findNodeByPpy(expcVal, parNode, ppyName, ct);
    }
    
    public ViewNode findNodeByPpy(String expcVal, String parId, String windowName, P ppyName, CompType ct){
    	ViewNode parNode = this.findNodeById(parId, windowName);        
        return findNodeByPpy(expcVal, parNode, ppyName, ct);
    }
    
    
    /** 根据ViewNode的公共变量获取ViewNode
     * @param parNode
     * @param fieldName
     * @param expcVal
     * @param ct
     * @return
     * @throws Exception
     */
    public ViewNode findNodeByField(ViewNode parNode, F fieldName, String expcVal, CompType ct) throws Exception {
        if (Compare(expcVal, parNode.getClass().getField(fieldName.value).toString(), ct)){
            return parNode;
        }
        
        for (ViewNode child : parNode.children) {
            ViewNode found = findNodeByField(child, fieldName, expcVal, ct);
            if (found != null) {
                return found;
            }
        }

        return null;
    }
	
    public ViewNode findNodeByClass(ViewNode parNode, String className) throws Exception {
        return findNodeByField(parNode, F.name, className, CompType.Equals);
    }
    
    public ViewNode findNodeById(String nodeId) {
    	return _view.findViewById(nodeId);
    }

    public ViewNode findNodeById(String nodeId, ViewNode parNode){
        return _view.findViewById(nodeId, parNode);
    }
    
    public ViewNode findNodeById( String nodeId, String windowName){
        Window window = getWindow(windowName, CompType.Contains);
        ViewNode rootNode = DeviceBridge.loadWindowData(window);
        return findNodeById(nodeId, rootNode);
    }

    public ViewNode findNodeById(String nodeId, String parId, String windowName){
    	ViewNode parNode = findNodeById(windowName, parId);
    	return findNodeById(nodeId, parNode);
    }
    
    public ViewNode findNodeByText(String text) {
    	return this.findNodeByPpy(text, P.text_mText, CompType.Equals);
    }

    public ViewNode findNodeByText(String text, ViewNode parNode){
    	return this.findNodeByPpy(text, parNode, P.text_mText, CompType.Equals);
    }
    
    public ViewNode findNodeByText( String text, String windowName){
    	return this.findNodeByPpy(text, windowName, P.text_mText, CompType.Equals);
    }

    public ViewNode findNodeByText(String text, String parId, String windowName){
    	return this.findNodeByPpy( text, parId, windowName, P.text_mText, CompType.Equals);
    }
    
    public ViewNode tryFindNodeById(String nodeId, int intervalMs, int times, boolean ignoreE) throws Exception{
    	ViewNode node = null;
        for (int i=0; i<times; i++){
        	try {
            	node = this.findNodeById(nodeId);
            	if (node != null){
            		 return node;
            	}
            	// x<5
            	// y=-0.108x^{2}+1.183x+0
            	// y=-0.4(x-2.23)^{2}+1.98 = 5.81s
            	// y=-0.3(x)^{2}+1.5x = 6s
            	// Thread.sleep((long) (-0.4*(i-2.23)*(i-2.23)+1.98)*1000); //_view.wait(intervalMs);.
            	// Thread.sleep((long) (-0.3*i*i+1.5*i)*1000);
            	Thread.sleep(intervalMs); //_view.wait(intervalMs);.
        	} catch (Exception e) {
        	}
        }
        
        if (!ignoreE){
        	throw new NoSuchNodeException();
        }
        
        return null;
    }
	
	public static void drag(int startx, int starty, int endx, int endy, int steps, long ms)  throws Exception{
        final long iterationTime = ms / steps;
        int stepDisX = (endx-startx)/steps;
        int stepDisY = (endy-starty)/steps;
        _mgr.touchDown(startx, starty);
        _mgr.sendMonkeyEvent("ntr");
        for (int i=0; i<steps; i++){
        	_mgr.touchMove(stepDisX, stepDisY);
        	Thread.sleep(iterationTime);
        }
        _mgr.touchUp(endx, endy);
    }
}
