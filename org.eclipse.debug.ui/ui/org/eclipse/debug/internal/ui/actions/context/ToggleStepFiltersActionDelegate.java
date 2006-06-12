/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.context;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Turns step filters on/off for a selected target.
 */
public class ToggleStepFiltersActionDelegate extends AbstractDebugContextActionDelegate implements IPropertyChangeListener, IViewActionDelegate {

	public ToggleStepFiltersActionDelegate()
	{
		setAction(new ToggleStepFiltersAction());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
		super.init(action);
        getWindowAction().setChecked(isUseStepFilters());
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
		//TODO: revert once API freeze is over
		if (event.getProperty().equals(IInternalDebugUIConstants.PREF_USE_STEP_FILTERS/*IDebugUIConstants.PREF_USE_STEP_FILTERS*/)) {
			Object newValue= event.getNewValue();
			if (newValue instanceof Boolean) {
				getWindowAction().setChecked(((Boolean)(newValue)).booleanValue());
			} else if (newValue instanceof String) {
				getWindowAction().setChecked(Boolean.valueOf((String)newValue).booleanValue());
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		getPreferenceStore().removePropertyChangeListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		DebugUITools.setUseStepFilters(!DebugUITools.isUseStepFilters());
	}


	public void init(IViewPart view) {
		init(view.getSite().getWorkbenchWindow());
	}
}
