/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.IRootVariablesContentProvider;
import org.eclipse.debug.ui.IObjectBrowser;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Provide the contents for a variables viewer.
 */
public class VariablesViewContentProvider implements ITreeContentProvider,
														IRootVariablesContentProvider {
	
	/**
	 * The view that owns this content provider.
	 */
	private IDebugView fDebugView;
	
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
	
	private IStackFrame fStackFrameInput;
	
	/**
	 * Constructs a new provider
	 */
	public VariablesViewContentProvider(IDebugView view) {
		fParentCache = new HashMap(10);
		setDebugView(view);
	}

	/**
	 * Returns the <code>IVariable</code>s for the given <code>IDebugElement</code>.
	 */
	public Object[] getElements(Object parent) {
		return getChildren(parent);
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
				children = getModelSpecificVariableChildren((IVariable)parent);
			}
			if (children != null) {
				cache(parent, children);
				return children;
			}
		} catch (DebugException de) {
			if (getExceptionHandler() != null) {
				getExceptionHandler().handleException(de);
			} else {
				DebugUIPlugin.log(de);
			}
		}
		return new Object[0];
	}
	
	protected IVariable[] getModelSpecificVariableChildren(IVariable parent) throws DebugException {
		IObjectBrowser objectBrowser = getObjectBrowser(getDebugModelId(parent));
		return objectBrowser.getChildren(getDebugView(), parent.getValue());
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
		try {
			if (element instanceof IVariable) {
				return hasModelSpecificVariableChildren((IVariable)element);
			}
			if (element instanceof IValue) {
				return ((IValue)element).hasVariables();
			}
			if (element instanceof IStackFrame) {
				return ((IStackFrame)element).hasVariables();
			}
		} catch (DebugException de) {
			DebugUIPlugin.log(de);
			return false;
		}
		return false;
	}
	
	protected boolean hasModelSpecificVariableChildren(IVariable parent) throws DebugException {
		IObjectBrowser contentProvider = getObjectBrowser(getDebugModelId(parent));
		return contentProvider.hasChildren(getDebugView(), parent.getValue());
	}
	
	/**
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		clearCache();
		if (newInput instanceof IStackFrame) {
			fStackFrameInput = (IStackFrame) newInput;
		} else {
			fStackFrameInput = null;
		}
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
	 * Extract the debug model id from the specified <code>IDebugElement</code>
	 * and return it.
	 */
	protected  String getDebugModelId(IDebugElement debugElement) {
		return debugElement.getModelIdentifier();
	}
		
	protected IObjectBrowser getObjectBrowser(String debugModelId) {
		ObjectBrowserManager mgr = DebugUIPlugin.getDefault().getObjectBrowserManager();
		if (getUseObjectBrowsers()) {
			return mgr.getObjectBrowser(debugModelId);		
		} else {
			return mgr.getDefaultObjectBrowser();
		}
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IRootVariablesContentProvider#setUseContentProviders(boolean)
	 */
	public void setUseContentProviders(boolean flag) {
		fUseObjectBrowsers = flag;
	}
	
	public boolean getUseObjectBrowsers() {
		return fUseObjectBrowsers;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IRootVariablesContentProvider#getThread()
	 */
	public IThread getThread() {
		if (fStackFrameInput == null) {
			return null;
		}
		return fStackFrameInput.getThread();
	}

	private void setDebugView(IDebugView view) {
		fDebugView = view;
	}

	protected IDebugView getDebugView() {
		return fDebugView;
	}
	
}

