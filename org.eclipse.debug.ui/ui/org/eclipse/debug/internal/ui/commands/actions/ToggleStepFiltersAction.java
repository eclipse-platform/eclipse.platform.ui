/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands.actions;

import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.commands.IStepFiltersCommand;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

public class ToggleStepFiltersAction extends DebugCommandAction implements IPropertyChangeListener {
	
	private boolean fInitialized = !DebugUITools.isUseStepFilters();
	
	public ImageDescriptor getDisabledImageDescriptor() {
		return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_TOGGLE_STEP_FILTERS);
	}

	public String getHelpContextId() {
		return "step_with_filters_action_context"; //$NON-NLS-1$
	}

	public ImageDescriptor getHoverImageDescriptor() {
		return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TOGGLE_STEP_FILTERS);
	}

	public String getId() {
		return "org.eclipse.debug.ui.actions.ToggleStepFilters"; //$NON-NLS-1$
	}

	public ImageDescriptor getImageDescriptor() {
		return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TOGGLE_STEP_FILTERS);
	}

	public String getText() {
		return ActionMessages.ToggleStepFiltersAction_0;
	}

	public String getToolTipText() {		
		return ActionMessages.ToggleStepFiltersAction_0;
	}

	protected boolean getInitialEnablement() {
		return true;
	}

	protected Class getCommandType() {
		return IStepFiltersCommand.class;
	}

    public void run() {
    	// ignore initial call to run from abstract debug view
    	// that runs the action to initialize it's state when
    	// the workbench persisted the action as "on"
    	if (fInitialized) {
    		DebugUITools.setUseStepFilters(!DebugUITools.isUseStepFilters());
    	} else {
    		fInitialized = true;
    	}
    }
	
    public int getStyle() {
    	return AS_CHECK_BOX;
    }

	public void debugContextChanged(DebugContextEvent event) {
		ISelection context = event.getContext();
		if (context.isEmpty()) {
			setEnabled(true);
		} else {
			super.debugContextChanged(event);
		}
	}

	public void init(IWorkbenchPart part) {
		super.init(part);
		initState();
	}

	public void init(IWorkbenchWindow window) {
		super.init(window);
		initState();
	}
    
    protected void initState() {
    	DebugUIPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(this);
    }

	public void dispose() {
		super.dispose();
		DebugUIPlugin.getDefault().getPluginPreferences().removePropertyChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IInternalDebugUIConstants.PREF_USE_STEP_FILTERS)) {
			boolean checked = DebugUITools.isUseStepFilters();
			setChecked(checked);
			DebugCommandActionDelegate delegate = getDelegate();
			if (delegate != null) {
				delegate.setChecked(checked);
			}
		}		
	}
    
    
}
