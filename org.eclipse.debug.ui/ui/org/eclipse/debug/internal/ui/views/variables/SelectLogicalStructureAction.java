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

import org.eclipse.debug.core.ILogicalStructureType;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.custom.BusyIndicator;

/**
 * Action to set the logical structure to display for a variable (enables/disables
 * logical structure types for the same variable).
 */
public class SelectLogicalStructureAction extends Action {
	
	private VariablesView fView;
	private ILogicalStructureType[] fTypes;
	private int fIndex;
	private String fDescription = null;

	/**
	 * 
	 * @param view Variables view
	 * @param group group of applicable structures
	 * @param value the value for which logical structures are to be chosen
	 * @param index the offset into the given group that this action enables
	 */
	public SelectLogicalStructureAction(VariablesView view, ILogicalStructureType[] group, IValue value, int index) {
		super(group[index].getDescription(value), IAction.AS_CHECK_BOX);
		fDescription = getText();
		setView(view);
		fTypes = group;
		fIndex = index;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		valueChanged();
	}

	private void valueChanged() {
		if (!getView().isAvailable()) {
			return;
		}
		BusyIndicator.showWhile(getView().getViewer().getControl().getDisplay(), new Runnable() {
			public void run() {
				IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
				for (int i = 0; i < fTypes.length; i++) {
					if (i == fIndex && isChecked()) {
						store.setValue(VariablesView.LOGICAL_STRUCTURE_TYPE_PREFIX + fTypes[i].getId(), 1);
					} else {
						store.setValue(VariablesView.LOGICAL_STRUCTURE_TYPE_PREFIX + fTypes[i].getId(), -1);
					}
				}
				getView().getViewer().refresh();					
			}
		});			
	}
	
	protected VariablesView getView() {
		return fView;
	}

	protected void setView(VariablesView view) {
		fView = view;
	}
}