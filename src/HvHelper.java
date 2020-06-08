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
			// TODO �Զ����ɵĹ��캯�����
		}
	}
	
	private String _sn = null;
	private AdbChimpDevice _device = null;
	private AdbBackend _adb = null;
	private HierarchyViewer _view = null;
	static ChimpManager _mgr = null;
	public static IDevice _iDevice;
	IHvDevice _ihvDevice = null;
	
	public HvHelper(String sn) throws Exception{
		this._sn = sn;
		initDevice() ;
	}
	
	public void monkeyTest() {        
        _device.touch(250, 250, com.android.chimpchat.core.TouchPressType.DOWN_AND_UP); 
        IChimpImage img = _device.takeSnapshot();
        String strHigh = _device.getProperty("display.height");
        DeviceBridge.initDebugBridge("D:\\A3pool\\AndroidSDK\\platform-tools\\adb.exe");
        DeviceBridge.initDebugBridge("adb.exe");
        AndroidDebugBridge.init(false);
		AndroidDebugBridge bridge = AndroidDebugBridge.createBridge();        	
        IDevice[] iDevices = bridge.getDevices();
        for (IDevice item : iDevices){
        	System.out.println(String.format("IDevice Name = ��%s��", item.getName()) );	
        }
	}
	
	public void initDevice() throws Exception {
        _adb = new AdbBackend(); 
        _device = (AdbChimpDevice) _adb.waitForConnection(10000, "127.0.0.1:21503"); // ��ңģ����
        _mgr = _device.getManager(); // .tap(250, 250);
        _view = _device.getHierarchyViewer();
        _iDevice = getIDevice(_sn);
        _ihvDevice = HvDeviceFactory.create(_iDevice);
        _ihvDevice.initializeViewDebug();
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
	
    private void findNodesByPpy( P ppyName, ViewNode parNode, String expcVal, CompType ct, List<ViewNode> outNodes){    	
        if (Compare(expcVal, getProperty(parNode, ppyName), ct)){
        	outNodes.add(parNode);
        }
        
        for (ViewNode child : parNode.children) {
            ViewNode found = findNodeByPpy(ppyName, child, expcVal, ct);
            if (found != null) {
            	outNodes.add(found);
            }
        }
        
        // return outNodes;
    }
	
    public List<ViewNode> findNodesByPp( P ppyName, ViewNode parNode, String expcVal, CompType ct){
    	List<ViewNode> outNodes = new ArrayList<>();        
        findNodesByPpy(ppyName, parNode, expcVal, ct, outNodes);
        return outNodes;
    }
	
    public ViewNode findNodeByPpy(P ppyName, ViewNode parNode, String expcVal, CompType ct){
    	// this.left = this.namedProperties.containsKey("mLeft") ? this.getInt("mLeft", 0) : this.getInt("layout:mLeft", 0);
        if (Compare(expcVal, getProperty(parNode, ppyName), ct)){
        	return parNode;
        }        
        
        for (ViewNode child : parNode.children) {
            ViewNode found = findNodeByPpy(ppyName, child, expcVal, ct);
            if (found != null) {
                return found;
            }
        }
        
        return null;
    }
    
    public ViewNode findNodeByPpy(P ppyName, String expcVal, CompType ct){
    	ViewNode parNode = this.getRootNode();
    	return findNodeByPpy(ppyName, parNode, expcVal, ct);
    }
    
    public ViewNode findNodeByPpy(P ppyName, String windowName, String expcVal, CompType ct){
    	ViewNode parNode = this.findNodeByWindow(windowName);        
        return findNodeByPpy(ppyName, parNode, expcVal, ct);
    }
    
    public ViewNode findNodeByPpy(P ppyName, String parId, String windowName, String expcVal, CompType ct){
    	ViewNode parNode = this.findNodeById(parId, windowName);        
        return findNodeByPpy(ppyName, parNode, expcVal, ct);
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
    	return this.findNodeByPpy(P.text_mText, text, CompType.Equals);
    }

    public ViewNode findNodeByText(String text, ViewNode parNode){
    	return this.findNodeByPpy(P.text_mText, parNode, text, CompType.Equals);
    }
    
    public ViewNode findNodeByText( String text, String windowName){
    	return this.findNodeByPpy(P.text_mText, windowName, text, CompType.Equals);
    }

    public ViewNode findNodeByText(String text, String parId, String windowName){
    	return this.findNodeByPpy(P.text_mText, parId, windowName, text, CompType.Equals);
    }

    public ViewNode tryFindNodeByIdTr(String parId, String nodeId){
        return null;
    }
    
    public ViewNode findNodeByPpy2(P ppyName) {
    	//return this.findNodeByProperty(parNode, property, expectValue, ct)(nodeId);
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
