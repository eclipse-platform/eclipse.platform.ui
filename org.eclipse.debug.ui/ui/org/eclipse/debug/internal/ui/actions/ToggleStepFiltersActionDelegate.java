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
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStepFilters;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate2;

/**
 * Turns step filters on/off for a selected target.
 */
public class ToggleStepFiltersActionDelegate extends AbstractDebugActionDelegate implements IActionDelegate2, IPropertyChangeListener {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#doAction(java.lang.Object)
	 */
	protected void doAction(Object element) {
		// do nothing - we override #run(IAction)
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
		setAction(action);
		action.setChecked(isUseStepFilters());
		getPreferenceStore().addPropertyChangeListener(this);
	}
	
	private boolean isUseStepFilters() {
		return DebugUIPlugin.getDefault().getStepFilterManager().isUseStepFilters();
	}
	
	private IPreferenceStore getPreferenceStore() {
		return DebugUIPlugin.getDefault().getPreferenceStore();
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IInternalDebugUIConstants.PREF_USE_STEP_FILTERS)) {
			Object newValue= event.getNewValue();
			if (newValue instanceof Boolean) {
				getAction().setChecked(((Boolean)(newValue)).booleanValue());
			} else if (newValue instanceof String) {
				getAction().setChecked(Boolean.valueOf((String)newValue).booleanValue());
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		super.dispose();
		getPreferenceStore().removePropertyChangeListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		DebugUITools.setUseStepFilters(action.isChecked());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#initialize(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	protected boolean initialize(IAction action, ISelection selection) {
		boolean res = super.initialize(action, selection);
		init(action);
		return res;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#update(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	protected void update(IAction action, ISelection s) {
		boolean enabled = true;
		if (s != null && !s.isEmpty()) {
			if (s instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection)s;
				if (ss.size() == 1 && ss.getFirstElement() instanceof IDebugElement) {
					// only disable if the selection does not support step filters
					IDebugElement element = (IDebugElement) ss.getFirstElement();
					IDebugTarget target = element.getDebugTarget();
					if (target instanceof IStepFilters) {
						IStepFilters filters = (IStepFilters)target;
						enabled = filters.supportsStepFilters();
					} else {
						enabled = false;
					}
				}
			}
		}
		action.setEnabled(enabled);
	}
}
