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
    
    public static By id(String nodeId) {
    	return new By(){
    		{
    			findNodeMethod = context -> ((IFinds) context).findNodeById(nodeId);
    			findNodesMethod = context -> ((IFinds) context).findNodesById(nodeId);
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
