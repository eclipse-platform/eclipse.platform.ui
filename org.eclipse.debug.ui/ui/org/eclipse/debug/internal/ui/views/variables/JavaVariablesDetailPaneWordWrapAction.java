package org.eclipse.debug.internal.ui.views.variables;

/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class JavaVariablesDetailPaneWordWrapAction implements IViewActionDelegate, IPropertyChangeListener {
	
	private boolean fChecked;
	private IAction fAction;
	
	public JavaVariablesDetailPaneWordWrapAction () {
		fChecked= preferenceStore().getBoolean(IDebugUIConstants.PREF_DETAIL_PANE_WORD_WRAP);
		preferenceStore().addPropertyChangeListener(this);
	}

	private IPreferenceStore preferenceStore() {
		return DebugUIPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		preferenceStore().setValue(IDebugUIConstants.PREF_DETAIL_PANE_WORD_WRAP, !fChecked);
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fAction= action;
		fAction.setChecked(fChecked);
	}

	/**
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IDebugUIConstants.PREF_DETAIL_PANE_WORD_WRAP)) {
			fChecked= preferenceStore().getBoolean(IDebugUIConstants.PREF_DETAIL_PANE_WORD_WRAP);
		}
		fAction.setChecked(fChecked);
	}

}
