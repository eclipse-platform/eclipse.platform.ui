package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.*;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * Provide the contents for a variables viewer.
 */
public class VariablesContentProvider extends BasicContentProvider implements IDebugEventListener, ITreeContentProvider {
	/**
	 * Constructs a new provider
	 */
	public VariablesContentProvider() {
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	/**
	 * @see BasicContentProvider#doGetChildren(Object)
	 */
	protected Object[] doGetChildren(Object parent) {
		try {
			if (parent instanceof IStackFrame || parent instanceof IValue) {
				return ((IDebugElement) parent).getChildren();
			} else if (parent instanceof IVariable) {
				return ((IVariable)parent).getValue().getChildren();
			}
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
		Object parent = null;
		if (item instanceof IVariable) {
			parent = ((IVariable) item).getParent();
		}
		if (parent instanceof IValue) {
			parent = ((IValue)parent).getVariable();
		}
		return parent;
	}

	/**
	 * @see BasicContentProvider#doHandleDebug(Event)
	 */
	protected void doHandleDebugEvent(DebugEvent event) {
		switch (event.getKind()) {
			case DebugEvent.SUSPEND:
				refresh();
				break;
			case DebugEvent.CHANGE:
				refresh(event.getSource());
				break;
		}
	}

	/**
	 * @see ITreeContentProvider
	 */
	public boolean hasChildren(Object item) {
		try {
			if (item instanceof IVariable) {
				return ((IVariable)item).getValue().hasChildren();
			} else {
				return ((IDebugElement) item).hasChildren();
			}
		} catch (DebugException de) {
			return false;
		}
	}

	/**
	 * Unregisters this content provider from the debug plugin so that
	 * this object can be garbage-collected.
	 */
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}
}

