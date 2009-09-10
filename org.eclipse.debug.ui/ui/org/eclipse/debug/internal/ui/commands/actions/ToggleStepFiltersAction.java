/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.commands.IStepFiltersHandler;
import org.eclipse.debug.internal.core.StepFilterManager;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.DebugCommandAction;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * This class provides the action for toggling step filters on or off for the debug view
 */
public class ToggleStepFiltersAction extends DebugCommandAction implements IPropertyChangeListener {
	
	private boolean fInitialized = !DebugUITools.isUseStepFilters();
	
	/**
	 * @see org.eclipse.debug.internal.ui.commands.actions.DebugCommandAction#getDisabledImageDescriptor()
	 */
	public ImageDescriptor getDisabledImageDescriptor() {
		return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_TOGGLE_STEP_FILTERS);
	}

	/**
	 * @see org.eclipse.debug.internal.ui.commands.actions.DebugCommandAction#getHelpContextId()
	 */
	public String getHelpContextId() {
		return "org.eclipse.debug.ui.step_with_filters_action_context"; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.debug.internal.ui.commands.actions.DebugCommandAction#getHoverImageDescriptor()
	 */
	public ImageDescriptor getHoverImageDescriptor() {
		return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TOGGLE_STEP_FILTERS);
	}

	/**
	 * @see org.eclipse.debug.internal.ui.commands.actions.DebugCommandAction#getId()
	 */
	public String getId() {
		return "org.eclipse.debug.ui.actions.ToggleStepFilters"; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.debug.internal.ui.commands.actions.DebugCommandAction#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TOGGLE_STEP_FILTERS);
	}

	/**
	 * @see org.eclipse.debug.internal.ui.commands.actions.DebugCommandAction#getText()
	 */
	public String getText() {
		return ActionMessages.ToggleStepFiltersAction_1;
	}

	/**
	 * @see org.eclipse.debug.internal.ui.commands.actions.DebugCommandAction#getToolTipText()
	 */
	public String getToolTipText() {		
		return ActionMessages.ToggleStepFiltersAction_0;
	}

	/**
	 * @see org.eclipse.debug.internal.ui.commands.actions.DebugCommandAction#getInitialEnablement()
	 */
	protected boolean getInitialEnablement() {
		return true;
	}

	/**
	 * @see org.eclipse.debug.internal.ui.commands.actions.DebugCommandAction#getCommandType()
	 */
	protected Class getCommandType() {
		return IStepFiltersHandler.class;
	}

    /**
     * @see org.eclipse.debug.internal.ui.commands.actions.DebugCommandAction#run()
     */
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
	
    /**
     * @see org.eclipse.jface.action.Action#getStyle()
     */
    public int getStyle() {
    	return AS_CHECK_BOX;
    }

	/**
	 * @see org.eclipse.debug.internal.ui.commands.actions.DebugCommandAction#debugContextChanged(org.eclipse.debug.ui.contexts.DebugContextEvent)
	 */
	public void debugContextChanged(DebugContextEvent event) {
		ISelection context = event.getContext();
		if (context.isEmpty()) {
			setEnabled(true);
		} else {
			super.debugContextChanged(event);
		}
	}

	/**
	 * @see org.eclipse.debug.internal.ui.commands.actions.DebugCommandAction#init(org.eclipse.ui.IWorkbenchPart)
	 */
	public void init(IWorkbenchPart part) {
		super.init(part);
		initState();
	}

	/**
	 * @see org.eclipse.debug.internal.ui.commands.actions.DebugCommandAction#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		super.init(window);
		initState();
	}
    
    /**
     * Initializes the state, by adding this action as a property listener 
     */
    protected void initState() {
    	DebugPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(this);
    }

	/**
	 * @see org.eclipse.debug.internal.ui.commands.actions.DebugCommandAction#dispose()
	 */
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().getPluginPreferences().removePropertyChangeListener(this);
	}

	/**
	 * @see org.eclipse.core.runtime.Preferences$IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(StepFilterManager.PREF_USE_STEP_FILTERS)) {
			boolean checked = DebugUITools.isUseStepFilters();
			setChecked(checked);
			IAction action = getActionProxy();
			if (action != null) {
				action.setChecked(checked);
			}
		}		
	}
    
    
}
