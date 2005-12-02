package org.eclipse.debug.internal.ui.viewers.update;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.internal.ui.viewers.IModelDeltaNode;

public class ModelDeltaNode implements IModelDeltaNode {

	private IModelDeltaNode fParent;
	private Object fElement;
	private int fFlags;
	private List fNodes = new ArrayList();
	private Object fNewElement;
	private int fIndex;

	public ModelDeltaNode(Object element, int flags) {
		fElement = element;
		fFlags = flags;
	}

	public ModelDeltaNode(Object element, Object newElement, int flags) {
        fElement = element;
        fNewElement = newElement;
        fFlags = flags;
    }

    public ModelDeltaNode(Object element, int index, int flags) {
        fElement = element;
        fIndex = index;
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

    public IModelDeltaNode addNode(Object element, Object replacement, int flags) {
        ModelDeltaNode node = new ModelDeltaNode(element, replacement, flags);
        node.setParent(this);
        fNodes.add(node);
        return node;
    }

    public IModelDeltaNode addNode(Object element, int index, int flags) {
        ModelDeltaNode node = new ModelDeltaNode(element, index, flags);
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

    public Object getNewElement() {
        return fNewElement;
    }

    public int getIndex() {
        return fIndex;
    }
    
	public ModelDeltaNode[] getNodes() {
		return (ModelDeltaNode[]) fNodes.toArray(new ModelDeltaNode[0]);
	}
}
