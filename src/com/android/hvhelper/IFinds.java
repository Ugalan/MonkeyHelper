package com.android.hvhelper;
import java.util.List;

import com.android.hierarchyviewerlib.models.ViewNode;

public interface IFinds {
	ViewNode findNodeById(String id);
	ViewNode findNodeByText(String text);
	List<ViewNode> findNodesById(String id);
}
