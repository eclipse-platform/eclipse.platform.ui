package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventListener;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * Provide the contents for a variables viewer.
 */
public class VariablesContentProvider extends BasicContentProvider implements IDebugEventListener, ITreeContentProvider {
	
	private VariablesView fVariablesView;
	private HashMap fParentCache;
	
	/**
	 * Constructs a new provider
	 */
	public VariablesContentProvider(VariablesView variablesView) {
		DebugPlugin.getDefault().addDebugEventListener(this);
		fVariablesView = variablesView;
		fParentCache = new HashMap(10);
	}

	/**
	 * @see BasicContentProvider#doGetChildren(Object)
	 */
	protected Object[] doGetChildren(Object parent) {
		Object[] children= null;
		try {
			if (parent instanceof IStackFrame) {
				IStackFrame sf= (IStackFrame)parent;
				if (sf.isSuspended()) {
					children = sf.getVariables();
				}
			} else if (parent instanceof IVariable) {
				children = ((IVariable)parent).getValue().getVariables();
			}
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					fParentCache.put(children[i], parent);
				}
				return children;
			}
		} catch (DebugException e) {
			DebugUIPlugin.logError(e);
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
		// if the stack frame which is an input to this view
		// is no longer valid (i.e. its thread has resumed), do
		// not update (bug 6518)
		Object input = fViewer.getInput();
		if (input instanceof IStackFrame) {
			if (!((IStackFrame)input).isSuspended()) {
				return;
			}
		}
		
		switch (event.getKind()) {
			case DebugEvent.SUSPEND:
			case DebugEvent.CHANGE:
				refresh();

				// We have to be careful NOT to populate the detail pane in the
				// variables view on any CHANGE DebugEvent, since the very act of 
				// populating the detail pane does an evaluation, which queues up
				// a CHANGE DebugEvent, which would lead to an infinite loop.  It's
				// probably safer to add invidual event details here as needed,
				// rather than try to exclude the ones we think are problematic.
				if (event.getDetail() == DebugEvent.STEP_END) {
					fVariablesView.populateDetailPane();
				}
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

