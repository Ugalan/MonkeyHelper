package com.android.hvhelper;
import java.util.List;

import com.android.hierarchyviewerlib.models.ViewNode;

public interface IFinds {
	ViewNode findNodeById(String id);
	ViewNode findNodeById(String id, ViewNode parNode);
	ViewNode findNodeById(String id, String windowName);
	ViewNode findNodeById(String id, String parId, String windowName);
	ViewNode findNodeById(String id, String locId, int parNum);
	ViewNode findNodeById(String id, String text, int parNum, CompType ct);
	ViewNode findNodeByText(String text);
	List<ViewNode> findNodesById(String id);
}
