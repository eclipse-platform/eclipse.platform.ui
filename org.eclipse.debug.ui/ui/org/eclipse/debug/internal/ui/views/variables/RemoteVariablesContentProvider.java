/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.debug.internal.ui.views.RemoteTreeContentManager;
import org.eclipse.debug.internal.ui.views.RemoteTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;

/**
 * Provide the contents for a variables viewer.
 */
public class RemoteVariablesContentProvider extends BaseWorkbenchContentProvider {
	
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
	 * Flag indicating whether contributed content providers should be used or not.
	 */
	private boolean fUseObjectBrowsers;
	
	/**
	 * Remote content manager to retrieve content in the background.
	 */
	private RemoteVariableContentManager fManager;
	
	/**
	 * Constructs a new provider
	 */
	public RemoteVariablesContentProvider(RemoteTreeViewer viewer, IWorkbenchPartSite site, VariablesView view) {
	    fManager = (RemoteVariableContentManager)createContentManager(viewer, site, view); 
		fParentCache = new HashMap(10);		
	}
	
	protected RemoteTreeContentManager createContentManager(RemoteTreeViewer viewer, IWorkbenchPartSite site, VariablesView view) {
		return new RemoteVariableContentManager(this, viewer, site, view);
	}

	/**
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
        Object[] children = fManager.getChildren(parent);
        if (children == null) {
            children = super.getChildren(parent);
        }
        if (children != null) {
			cache(parent, children);
			return children;
		}
        return new Object[0];
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
			Object child = children[i];
			// avoid cycles in the cache, which can happen for
			// recursive data structures
			if (!fParentCache.containsKey(child)) {
				fParentCache.put(child, parent);
			}
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
		fManager.clearHasChildrenCache();
		fManager.cancel();
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
	public void removeCache(Object[] children) {
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
        return fManager.mayHaveChildren(element);
	}
		
	/**
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		clearCache();
		fManager.cancel();
        fManager.clearHasChildrenCache();
	}
	
	/**
	 * Return all cached decendants of the given parent.
	 * 
	 * @param parent the element whose decendants are to be calculated
	 * @return list of decendants that have been cached for
	 *  the given parent
	 */
	public List getCachedDecendants(Object parent) {
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
	
	/** 
	 * Show logical structure of values 
	 */
	public void setShowLogicalStructure(boolean flag) {
		fUseObjectBrowsers = flag;
	}
	
	public boolean isShowLogicalStructure() {
		return fUseObjectBrowsers;
	}
	
	public RemoteVariableContentManager getContentManager() {
		return fManager;
	}
	
}

