package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;

import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jface.viewers.*;

public class InspectorContentProvider extends BasicContentProvider implements ITreeContentProvider, IDebugEventListener {
	
	/**
	 * The inspect items in the viewer	 
	 */
	protected InspectorList fInspectorList;
	
	/**
	 * The action that removes all the items
	 */
	 protected RemoveAllFromInspectorAction fRemoveAllFromInspectorAction;
	 
	protected HashMap fParentCache;
		
	/**
	 * Constructs a new inspector content provider
	 */
	public InspectorContentProvider(RemoveAllFromInspectorAction action) {
		fRemoveAllFromInspectorAction= action;
		fInspectorList = new InspectorList(3);
		fParentCache = new HashMap(10);
		DebugPlugin.getDefault().addDebugEventListener(this);
		enableRemoveAllFromInspectorAction();
	}
		
	/**
	 * @see ITreeContentProvider
	 */
	public Object getParent(Object child) {
		if (child instanceof InspectItem) {
			return fInspectorList;
		}
		return fParentCache.get(child);
		
	}

	/**
	 * @see BasicContentProvider#doGetChildren(Object)
	 */
	protected Object[] doGetChildren(Object parent) {
		Object[] children= null;
		if (parent == fInspectorList) {
			children = fInspectorList.getList().toArray();
		} else {	
			try {
				if (parent instanceof InspectItem) {
					children = ((InspectItem)parent).getValue().getVariables();
				} else if (parent instanceof IVariable) {
					children = ((IVariable)parent).getValue().getVariables();
				}

			} catch (DebugException de) {
				DebugUIPlugin.logError(de);
			}
		}
		
		if (children == null) {
			return new Object[0];
		}
			
		for (int i = 0; i < children.length; i++) {
			fParentCache.put(children[i], parent);
		}
		return children;

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
		super.dispose();
		fInspectorList = new InspectorList(0);
		DebugPlugin.getDefault().removeDebugEventListener(this);
		fParentCache= null;
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
		if (!fInspectorList.isEmpty() && (event.getKind() == DebugEvent.SUSPEND || 
			event.getKind() == DebugEvent.TERMINATE ||
			event.getKind() == DebugEvent.CHANGE)) {
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
					clearCache(item);
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
				if (!isDisposed()) {
					((TreeViewer)fViewer).remove(element);
					enableRemoveAllFromInspectorAction();
				}
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
					if (!isDisposed()) {
						fInspectorList.getList().clear();
						refresh();
						enableRemoveAllFromInspectorAction();
					}
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
					if (!isDisposed()) {
						TreeViewer tempViewer= (TreeViewer)fViewer;		
						tempViewer.add(parent, element);				
						tempViewer.setExpandedState(element, true);
						tempViewer.setSelection(new StructuredSelection(element));
						enableRemoveAllFromInspectorAction();
					}
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
			inspectorList.add(item);
			insert(item);
		}
	}

	/**
	 * Removes a inspect item from the list
	 */
	public void removeFromInspector(InspectItem item) {
		getInspectorList().getList().remove(item);
		clearCache(item);
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
	
	protected void clearCache(Object parent) {
		Iterator iter = ((HashMap)fParentCache.clone()).keySet().iterator();
		while (iter.hasNext()) {
			Object child = iter.next();
			if (parent.equals(fParentCache.get(child))) {
				fParentCache.remove(child);
				clearCache(child);
			}			
		}
	}
}