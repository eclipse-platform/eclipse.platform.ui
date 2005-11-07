package org.eclipse.debug.internal.ui.viewers.update;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.internal.ui.viewers.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.IModelDeltaNode;

public class ModelDelta implements IModelDelta {
	
	private List fNodes = new ArrayList();
	
	public IModelDeltaNode addNode(Object element, int flags) {
		ModelDeltaNode node = new ModelDeltaNode(element, flags);
		fNodes.add(node);
		return node;
	}

	public IModelDeltaNode[] getNodes() {
		return (ModelDeltaNode[]) fNodes.toArray(new ModelDeltaNode[0]);
	}

}
