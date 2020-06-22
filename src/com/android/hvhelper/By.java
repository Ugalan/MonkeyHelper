package com.android.hvhelper;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

import com.android.hierarchyviewerlib.models.ViewNode;

public class By {
	protected Function<ISearchContext, ViewNode> findNodeMethod;
	protected Function<ISearchContext, List<ViewNode>> findNodesMethod;		

    public ViewNode findNode(ISearchContext iContext) {
    	return findNodeMethod.apply(iContext);
    }
    
    public List<ViewNode> findNodes(ISearchContext iContext) {
    	return findNodesMethod.apply(iContext);
    }
    
    public static By id(String id) {
    	return new By(){
    		{
    			findNodeMethod = context -> ((IFinds) context).findNodeById(id);
    			findNodesMethod = context -> ((IFinds) context).findNodesById(id);
    		}    		
    	};
    }
    
    public static By id(String id, ViewNode parNode) {
    	return new By(){
    		{
    			findNodeMethod = context -> ((IFinds) context).findNodeById(id, parNode);
    		}    		
    	};
    }
    
    public static By id(String id, String windowName) {
    	return new By(){
    		{
    			findNodeMethod = context -> ((IFinds) context).findNodeById(id, windowName);
    		}    		
    	};
    }
    
    public static By id(String id, String parId, String windowName) {
    	return new By(){
    		{
    			findNodeMethod = context -> ((IFinds) context).findNodeById(id, parId, windowName);
    		}    		
    	};
    }
    
    public static By id(String id, String locId, int parNum) {
    	return new By(){
    		{
    			findNodeMethod = context -> ((IFinds) context).findNodeById(id, locId, parNum);
    		}    		
    	};
    }
    
    public static By id(String id, String text, int parNum, CompType textCt) {
    	return new By(){
    		{
    			findNodeMethod = context -> ((IFinds) context).findNodeById(id, text, parNum, textCt);
    		}    		
    	};
    }
    
    public static By text(String text) {
    	return new By(){
    		{
    			findNodeMethod = context -> ((IFinds) context).findNodeByText(text);
    		}    		
    	};
    }
}
