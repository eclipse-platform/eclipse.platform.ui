package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

public class InspectorContentProvider extends BasicContentProvider implements ITreeContentProvider, IDebugEventListener {
	
	/**
	 * The inspect items in the viewer	 
	 */
	protected InspectorList fInspectorList;
	
	/**
	 * The action that removes all the items
	 */
	 protected RemoveAllFromInspectorAction fRemoveAllFromInspectorAction;
	 
	/**
	 * A table of root values in the inspector
	 */
	protected Hashtable fRootValuesTable;
		
	/**
	 * Constructs a new inspector content provider
	 */
	public InspectorContentProvider(RemoveAllFromInspectorAction action) {
		fRemoveAllFromInspectorAction= action;
		fInspectorList = new InspectorList(3);
		fRootValuesTable = new Hashtable(3);
		DebugPlugin.getDefault().addDebugEventListener(this);
		enableRemoveAllFromInspectorAction();
	}
		
	/**
	 * @see ITreeContentProvider
	 */
	public Object getParent(Object child) {
		if (fInspectorList == child) {
			return null;
		} else if (child instanceof InspectItem) {
			return fInspectorList;
		} else if (child instanceof IVariable) {
			Object parent = ((IVariable)child).getParent();
			if (parent instanceof IValue) {
				IValue value = (IValue)parent;
				Object inspectItem = fRootValuesTable.get(value);
				if (inspectItem != null) {
					return inspectItem;
				} else {
					return value.getVariable();
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * @see BasicContentProvider#doGetChildren(Object)
	 */
	protected Object[] doGetChildren(Object parent) {
		if (parent == fInspectorList) {
			return fInspectorList.getList().toArray();
		}
		try {
			if (parent instanceof IVariable) {
				return ((IVariable)parent).getValue().getChildren();
			}
			if (parent instanceof InspectItem) {
				return ((InspectItem)parent).getValue().getChildren();
			}
		} catch (DebugException de) {
			DebugUIUtils.logError(de);
		}
		return new Object[0];
	}

	/**
	 * @see ITreeContentProvider
	 */
	public boolean hasChildren(Object parent) {
		if (parent == fInspectorList) {
			return !fInspectorList.isEmpty();
		} 
		try {
			if (parent instanceof InspectItem) {
				return ((InspectItem)parent).getValue().hasChildren();
			} else{
				return ((IVariable)parent).getValue().hasChildren();
			}
		} catch (DebugException de) {
			return false;
		}
	}
	
	/**
	 * @see IStructuredContentProvider
	 */
	public Object[] getElements(Object parent) {
		return getChildren(parent);
	}

	/**
	 * @see IContentProvider
	 */
	public void dispose() {
		fInspectorList = null;
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}

	/**
	 * Returns the list of inspect items being inspected.
	 */
	public InspectorList getInspectorList() {
		return fInspectorList;
	}
	
	/**
	 * @see BasicContentProvider#doHandleDebug(Event)
	 */
	protected void doHandleDebugEvent(DebugEvent event) {
		if (event.getKind() == DebugEvent.SUSPEND || 
			event.getKind() == DebugEvent.TERMINATE ||
			event.getKind() == DebugEvent.CHANGE) {
			// remove any de-allocated values
			Iterator itr = fInspectorList.getList().iterator();
			while (itr.hasNext()) {
				InspectItem item = (InspectItem)itr.next();
				boolean allocated= false;
				try {
					allocated= item.getValue().isAllocated();
				} catch (DebugException de) {
				}
				if (!allocated) {
					itr.remove();
					fRootValuesTable.remove(item.getValue());
				}
			}
			refresh();
			enableRemoveAllFromInspectorAction();
		}
	}

	/**
	 * Helper method to remove the given element
	 */
	protected void remove(final Object element) {
		Runnable r= new Runnable() {
			public void run() {			
				((TreeViewer)fViewer).remove(element);
				enableRemoveAllFromInspectorAction();
			}
		};
		asyncExec(r);
	}
	
	/**
	 * Helper method to remove all elements
	 */
	protected void removeAll() {
		Runnable r= new Runnable() {
				public void run() {	
					fInspectorList.getList().clear();
					refresh();
					enableRemoveAllFromInspectorAction();
				}
			};
		asyncExec(r);
	}

	/**
	 * Helper method for inserting the given element
	 */
	protected void insert(final Object element) {
		final Object parent = getParent(element);
		if (parent != null) {
			Runnable r= new Runnable() {
				public void run() {	
					TreeViewer tempViewer= (TreeViewer)fViewer;		
					tempViewer.add(parent, element);				
					tempViewer.setExpandedState(element, true);
					tempViewer.setSelection(new StructuredSelection(element));
					enableRemoveAllFromInspectorAction();
				}
			};
			asyncExec(r);
		}
	}
	
	/**
	 * Adds a inspect item to the list
	 */
	public void addToInspector(InspectItem item) {
		List inspectorList = getInspectorList().getList();
		if (!inspectorList.contains(item)) {
			fRootValuesTable.put(item.getValue(), item);
			inspectorList.add(item);
			insert(item);
		}
	}

	/**
	 * Removes a inspect item from the list
	 */
	public void removeFromInspector(InspectItem item) {
		getInspectorList().getList().remove(item);
		fRootValuesTable.remove(item.getValue());
		remove(item);
	}
	
	/**
	 * Enable/disable the <code>RemoveAllFromInspectorAction<code> based on whether the inspector
	 * list is currently empty.
	 */
	public void enableRemoveAllFromInspectorAction() {
		boolean enable= getInspectorList().isEmpty() ? false : true;
		fRemoveAllFromInspectorAction.setEnabled(enable);
	}
}