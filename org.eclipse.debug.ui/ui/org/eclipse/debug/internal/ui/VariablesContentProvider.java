package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;

import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.*;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * Provide the contents for a variables viewer.
 */
public class VariablesContentProvider extends BasicContentProvider implements IDebugEventListener, ITreeContentProvider {
	
	protected HashMap fParentCache;
	
	/**
	 * Constructs a new provider
	 */
	public VariablesContentProvider() {
		DebugPlugin.getDefault().addDebugEventListener(this);
		fParentCache = new HashMap(10);
	}

	/**
	 * @see BasicContentProvider#doGetChildren(Object)
	 */
	protected Object[] doGetChildren(Object parent) {
		Object[] children= null;
		try {
			if (parent instanceof IStackFrame) {
				children = ((IStackFrame)parent).getVariables();
			} else if (parent instanceof IVariable) {
				children = ((IVariable)parent).getValue().getVariables();
			}
			for (int i = 0; i < children.length; i++) {
				fParentCache.put(children[i], parent);
			}
			return children;
		} catch (DebugException de) {
			DebugUIUtils.logError(de);
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
	 * @see ITreeContentProvider
	 */
	public Object getParent(Object item) {
		return fParentCache.get(item);
	}

	/**
	 * @see BasicContentProvider#doHandleDebug(Event)
	 */
	protected void doHandleDebugEvent(DebugEvent event) {
		switch (event.getKind()) {
			case DebugEvent.SUSPEND:
			case DebugEvent.CHANGE:
				refresh();
				break;
		}
	}

	/**
	 * Unregisters this content provider from the debug plugin so that
	 * this object can be garbage-collected.
	 */
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().removeDebugEventListener(this);
		fParentCache=null;
	}
	
	protected void clearCache() {
		fParentCache.clear();
	}
}

