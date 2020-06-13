import java.util.List;

import com.android.hierarchyviewerlib.models.ViewNode;

public interface IFindsById {
	ViewNode findNodeById(String id);
	List<ViewNode> findNodesById(String id);
}
