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
import com.android.chimpchat.core.TouchPressType;
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
import com.android.monkeyrunner.MonkeyDevice;
import com.android.monkeyrunner.easy.EasyMonkeyDevice;
// import com.android.monkeyrunner.easy.By;

/**
 * @author Ugalan
 *
 */
/**
 * @author Ugalan
 *
 */
public class HvHelper implements IFindsById, ISearchContext{
	class ChimpManagerEx extends ChimpManager {
		public ChimpManagerEx(Socket monkeySocket) throws IOException {
			super(monkeySocket);
			// TODO �Զ����ɵĹ��캯�����
		}
	}
	
	private String _sn = null;
	public AdbChimpDevice _device = null;
	public static EasyMonkeyDevice _mDevice = null;
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
        _mDevice = new EasyMonkeyDevice(new MonkeyDevice(_device));
        _mgr = _device.getManager(); // .tap(250, 250);
        _view = _device.getHierarchyViewer();
        _iDevice = getIDevice(_sn);
        _ihvDevice = HvDeviceFactory.create(_iDevice);
        // _ihvDevice.initializeViewDebug();
	}
	
    public void clearEnv()
    {
        // DeviceBridge.stopListenForDevices();
        DeviceBridge.stopViewServer(_iDevice); // �ر�ViewServer
        // AndroidDebugBridge.disconnectBridge(); // �Ͽ�adb����
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
	
	/** monkey --port 12345 ����������
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
			exec.shutdown(); // �ر��̳߳�
		}		
	}
	
	private static IDevice getDevice(int index) {
		IDevice device = null;
		AndroidDebugBridge bridge = AndroidDebugBridge.createBridge();
		waitDevicesList(bridge);
		IDevice devices[] = bridge.getDevices();
		
		if(devices.length < index){
			System.err.print("û�м�⵽��" + index + "���豸");
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
				System.err.print("�ȴ���ȡ�豸��ʱ");
				break;
			}
		}
	}
	
    public Window getFocusedWindow(){
        // return DeviceBridge.loadWindows(ihvDevice, iDevice)[DeviceBridge.getFocusedWindow(iDevice)];
        return getWindow(_view.getFocusedWindowName(), CompType.Equals); // �������δ���Ƕ��������һ�������
    }
    
    public ViewNode getRootNode(){
        return DeviceBridge.loadWindowData( new Window(new ViewServerDevice(_iDevice), "", 0xffffffff));
    }
	
	private static Window getWindow(String windowName, CompType ct) {
        IDevice iDevice = null;	
        iDevice = getDevice(0);
        System.out.println(String.format("IDevice Name = ��%s��", iDevice.getName()) );
        IHvDevice viewDevice = HvDeviceFactory.create(iDevice);
        viewDevice.initializeViewDebug();
        Window[] windows =  DeviceBridge.loadWindows(viewDevice, iDevice);
        
        for (Window item : windows){
        	String curName = item.getTitle();
        	System.out.println(String.format("Window Title = ��%s��", curName) );
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
		String action = "android.intent.action.MAIN";   // �������Ȩ��
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
	 * �ж�str�ַ����Ƿ��ܹ���regexƥ��
	 * ��a*b?d����ƥ��aAAAbcd
	 * @param str �����ַ���
	 * @param regex ����*�򣿵�ƥ����ʽ
	 * @param ignoreCase ��Сд����
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
		return matches(str, regex.replaceAll("(^|([^\\\\]))[\\*]{2,}", "$2*"));// ȥ������*��
	}

	private static boolean matches(String str, String regex) {
		// ���str��regex��ȫ��ȣ���str��������б�ܣ��򷵻�true��
		if (str.equals(regex) && str.indexOf('\\') < 0) {
			return true;
		}
		
		int rIdx = 0, sIdx = 0; // ͬʱ����Դ�ַ�����ƥ����ʽ
		while (rIdx < regex.length() && sIdx < str.length()) {
			char c = regex.charAt(rIdx); // ��ƥ����ʽΪ����
			switch (c) {
			case '*': // ƥ�䵽*�Ž�����һ��ݹ�
				String tempSource = str.substring(sIdx); // ȥ��ǰ���Ѿ���ȫƥ���ǰ׺
				String tempRegex = regex.substring(rIdx + 1); // ���Ǻź�һλ��ʼ��Ϊ���µ�ƥ����ʽ
				for (int j = 0; j <= tempSource.length(); j++) { // �˴��ȺŲ���ȱ���磨ABCD��*�����Ⱥ��ܴ��("", *)����
					if (matches(tempSource.substring(j), tempRegex)) { // ����ͨ�ĵݹ�˼·
						return true;
					}
				}
				return false; // �ų�����Ǳ�ڿ����ԣ��򷵻�false
			case '?':
				break;
			case '\\': // ƥ�䵽��б������һλ��ƥ����һ���ַ���
				c = regex.charAt(++rIdx);
			default:
				if (str.charAt(sIdx) != c) {
					return false;// ��ͨ�ַ���ƥ��
				}
			}
			rIdx++;
			sIdx++;
		}
		// ����str��ƥ����ȫ����regexҲ��ƥ��������ֻʣһ��*��
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
    
    public List<ViewNode> findNodesById( String nodeId){
    	List<ViewNode> outNodes = new ArrayList<>();
    	ViewNode parNode = this.getRootNode();
        findNodesByPpy(nodeId, parNode, P.mID, CompType.Equals, outNodes);
        return outNodes;
    }    
    
    private boolean isNodeMatch( ViewNode node, NodeLocMap locMap){    	
    	for (NodeLocInfo locInfo : locMap.locInfo){ // �������Զ�λһ���ڵ�
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
    
    
    /** ����ViewNode�Ĺ���������ȡViewNode
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
    
	public ViewNode findNode(By by) {
		return by.findNode((ISearchContext)this);
	}

	public List<ViewNode> findNodes(By by) {
		// TODO �Զ����ɵķ������
		return null;
	}
	
	/*public ViewNode findNode(By by)  throws Exception{
		return by.findNode(this);
    }*/
    
	public static void touch(String nodeId)  throws Exception{
		// _mDevice.touch(By.id(nodeId), TouchPressType.DOWN_AND_UP);
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
