package org.eclipse.debug.internal.ui.viewers.update;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.internal.ui.viewers.IModelDeltaNode;

public class ModelDeltaNode implements IModelDeltaNode {

	private IModelDeltaNode fParent;
	private Object fElement;
	private int fFlags;
	private List fNodes = new ArrayList();

	public ModelDeltaNode(Object element, int flags) {
		fElement = element;
		fFlags = flags;
	}

	public Object getElement() {
		return fElement;
	}

	public int getFlags() {
		return fFlags;
	}

	public IModelDeltaNode addNode(Object element, int flags) {
		ModelDeltaNode node = new ModelDeltaNode(element, flags);
		node.setParent(this);
		fNodes.add(node);
		return node;
	}

	void setParent(ModelDeltaNode node) {
		fParent = node;
	}
	
	public IModelDeltaNode getParent() {
		return fParent;
	}

	public ModelDeltaNode[] getNodes() {
		return (ModelDeltaNode[]) fNodes.toArray(new ModelDeltaNode[0]);
	}

}
