/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;


import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * 
 */
public abstract class VariableViewToggleAction implements IViewActionDelegate, IActionDelegate2 {
	
	private IViewPart fView;
	private IAction fAction;

	public VariableViewToggleAction() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		fView = view;
		boolean checked = getPreferenceValue(view);
		fAction.setChecked(checked);
		run(fAction);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
		fAction = action;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IPreferenceStore store = getPreferenceStore();
		String key = getView().getSite().getId() + "." + getPreferenceKey(); //$NON-NLS-1$
		store.setValue(key, action.isChecked());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	protected IPreferenceStore getPreferenceStore() {
		return DebugUIPlugin.getDefault().getPreferenceStore();
	}
	
	/**
	 * Returns the value of this filters preference (on/off) for the given
	 * view.
	 * 
	 * @param part
	 * @return boolean
	 */
	protected boolean getPreferenceValue(IViewPart part) {
		String baseKey = getPreferenceKey();
		String viewKey = part.getSite().getId();
		String compositeKey = viewKey + "." + baseKey; //$NON-NLS-1$
		IPreferenceStore store = getPreferenceStore();
		boolean value = false;
		if (store.contains(compositeKey)) {
			value = store.getBoolean(compositeKey);
		} else {
			value = store.getBoolean(baseKey);
		}
		return value;		
	}
	
	/**
	 * Returns the key for this action's preference
	 * 
	 * @return String
	 */
	protected abstract String getPreferenceKey(); 
		
	protected IViewPart getView() {
		return fView;
	}
	
	protected StructuredViewer getStructuredViewer() {
		IDebugView view = (IDebugView)getView().getAdapter(IDebugView.class);
		if (view != null) {
			Viewer viewer = view.getViewer();
			if (viewer instanceof StructuredViewer) {
				return (StructuredViewer)viewer;
			}
		}		
		return null;
	}
}
