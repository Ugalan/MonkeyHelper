import java.util.List;

import com.android.hierarchyviewerlib.models.ViewNode;

public interface ISearchContext {
	ViewNode findNode(By by);
	List<ViewNode> findNodes(By by);
}
