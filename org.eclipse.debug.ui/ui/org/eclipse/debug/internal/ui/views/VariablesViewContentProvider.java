package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Provide the contents for a variables viewer.
 */
public class VariablesViewContentProvider implements ITreeContentProvider {
	
	/**
	 * A table that maps children to their parent element
	 * such that this content provider can walk back up the
	 * parent chain (since values do not know their
	 * parent).
	 * Map of <code>IVariable</code> (child) -> <code>IVariable</code> (parent).
	 */
	private HashMap fParentCache;
	
	/**
	 * Handler for exceptions as content is retrieved
	 */
	private IDebugExceptionHandler fExceptionHandler = null;
	
	
	/**
	 * Constructs a new provider
	 */
	public VariablesViewContentProvider() {
		fParentCache = new HashMap(10);
	}

	/**
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		Object[] children= null;
		try {
			if (parent instanceof IStackFrame) {
				children = ((IStackFrame)parent).getVariables();
			} else if (parent instanceof IVariable) {
				children = ((IVariable)parent).getValue().getVariables();
			}
			if (children != null) {
				cache(parent, children);
				return children;
			}
		} catch (DebugException e) {
			if (getExceptionHandler() != null) {
				getExceptionHandler().handleException(e);
			} else {
				DebugUIPlugin.log(e);
			}
		}
		return new Object[0];
	}

	/**
	 * Returns the <code>IVariable</code>s for the given <code>IDebugElement</code>.
	 */
	public Object[] getElements(Object parent) {
		return getChildren(parent);
	}

	/**
	 * Caches the given elememts as children of the given
	 * parent.
	 * 
	 * @param parent parent element
	 * @param children children elements
	 */
	protected void cache(Object parent, Object[] children) {		
		for (int i = 0; i < children.length; i++) {
			fParentCache.put(children[i], parent);
		}		
	}
	
	/**
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object item) {
		return fParentCache.get(item);
	}


	/**
	 * Unregisters this content provider from the debug plugin so that
	 * this object can be garbage-collected.
	 */
	public void dispose() {
		fParentCache= null;
		setExceptionHandler(null);
	}
	
	protected void clearCache() {
		if (fParentCache != null) {
			fParentCache.clear();
		}
	}
	
	/**
	 * Remove the cached parent for the given children
	 * 
	 * @param children for which to remove cached parents
	 */
	protected void removeCache(Object[] children) {
		if (fParentCache == null) {
			return;
		}
		for (int i = 0; i < children.length; i++) {
			fParentCache.remove(children[i]);	
		}
	}
	
	/**
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element) {
		try {
			if (element instanceof IVariable) {
				return ((IVariable)element).getValue().hasVariables();
			}
			if (element instanceof IValue) {
				return ((IValue)element).hasVariables();
			}
			if (element instanceof IStackFrame) {
				return ((IStackFrame)element).hasVariables();
			}
		} catch (DebugException e) {
			DebugUIPlugin.log(e);
			return false;
		}
		return false;
	}

	/**
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		clearCache();
	}
	
	/**
	 * Return all cached decendants of the given parent.
	 * 
	 * @param parent the element whose decendants are to be calculated
	 * @return list of decendants that have been cached for
	 *  the given parent
	 */
	protected List getCachedDecendants(Object parent) {
		Iterator children = fParentCache.keySet().iterator();
		List cachedChildren = new ArrayList(10);
		while (children.hasNext()) {
			Object child = children.next();
			if (isCachedDecendant(child, parent)) {
				cachedChildren.add(child);
			}
		}
		return cachedChildren;
	}
	
	/**
	 * Returns whether the given child is a cached descendant
	 * of the given parent.
	 * 
	 * @return whether the given child is a cached descendant
	 * of the given parent
	 */
	protected boolean isCachedDecendant(Object child, Object parent) {
		Object p = getParent(child);
		while (p != null) {
			if (p.equals(parent)) {
				return true;
			}
			p = getParent(p);
		}
		return false;
	}
	
	/**
	 * Sets an exception handler for this content provider.
	 * 
	 * @param handler debug exception handler or <code>null</code>
	 */
	protected void setExceptionHandler(IDebugExceptionHandler handler) {
		fExceptionHandler = handler;
	}
	
	/**
	 * Returns the exception handler for this content provider.
	 * 
	 * @return debug exception handler or <code>null</code>
	 */
	protected IDebugExceptionHandler getExceptionHandler() {
		return fExceptionHandler;
	}	
}

