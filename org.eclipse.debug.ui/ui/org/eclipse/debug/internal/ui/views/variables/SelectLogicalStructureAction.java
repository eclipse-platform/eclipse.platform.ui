/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.debug.internal.core.LogicalStructureManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.BusyIndicator;

/**
 * Action to set the logical structure to display for a variable (enables/disables
 * logical structure types for the same variable).
 */
public class SelectLogicalStructureAction extends Action {
	
	private VariablesView fView;
    private ILogicalStructureType fType;
    private ILogicalStructureType[] fAvailableTypes;

	/**
	 * 
	 * @param view Variables view
	 * @param group group of applicable structures
	 * @param value the value for which logical structures are to be chosen
	 * @param index the offset into the given group that this action enables
	 */
	public SelectLogicalStructureAction(VariablesView view, ILogicalStructureType type, IValue value, ILogicalStructureType[] availableTypes) {
		super(type.getDescription(value), IAction.AS_CHECK_BOX);
		setView(view);
        fAvailableTypes= availableTypes;
		fType= type;
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
                // Checking this action sets the type to fType, unchecking it sets the type
                // to null ("none selected")
                ILogicalStructureType type= null;
                if (isChecked()) {
                    type= fType;
                }
                LogicalStructureManager.getDefault().setEnabledType(fAvailableTypes, type);
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