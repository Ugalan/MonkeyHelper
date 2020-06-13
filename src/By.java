import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

import com.android.hierarchyviewerlib.models.ViewNode;

public class By {
	protected String id;
	protected Function<ISearchContext, ViewNode> findNodeMethod;
	protected Function<ISearchContext, List<ViewNode>> findNodesMethod;		

    public static By id(String nodeId) {
    	return new By(){
    		{
    			this.id = nodeId;
    			findNodeMethod = context -> ((IFindsById) context).findNodeById(nodeId);
    			findNodesMethod = context -> ((IFindsById) context).findNodesById(nodeId);
    		}    		
    	};
    }
    
    public ViewNode findNode(ISearchContext iContext) {
    	return findNodeMethod.apply(iContext);
    }
}
