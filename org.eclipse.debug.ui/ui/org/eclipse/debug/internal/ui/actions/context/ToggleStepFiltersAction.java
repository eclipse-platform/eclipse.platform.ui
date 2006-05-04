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
package org.eclipse.debug.internal.ui.actions.context;

import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepFiltersAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;

public class ToggleStepFiltersAction extends AbstractDebugContextAction {

	public ToggleStepFiltersAction()
	{
		super();
		setEnabled(true);
	}
	
	protected void doAction(Object target) {
		// do nothing, the action is done by ToggleStepFiltersActionDelegate instead
		// since this action does not have access to the checked state of the window button
	}

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
		// TODO:  this #getText method is not currently used, putting non-nl string
		// until properties files are opened to be updated again.
		return "Toggle Step Filters"; //$NON-NLS-1$
	}

	public String getToolTipText() {
		// TODO:  this #getToolTipText method is not currently used, putting non-nl string
		// until properties files are opened to be updated again.		
		return "Toggle Step Filters"; //$NON-NLS-1$
	}

	protected void isEnabledFor(Object element, IBooleanRequestMonitor monitor) {
    	if (element instanceof IAdaptable)
    	{
    		IAsynchronousStepFiltersAdapter stepFilters = (IAsynchronousStepFiltersAdapter)((IAdaptable)element).getAdapter(IAsynchronousStepFiltersAdapter.class);
    		if (stepFilters != null)
    		{
    			stepFilters.supportsStepFilters(element, monitor);
    		}
    		else
    		{
    			notSupported(monitor);
    		}
    	}

	}

	protected void updateEnableStateForContext(IStructuredSelection selection) {
		
	   int size = selection.size();
       if (size == 1)
       {
    	   BooleanRequestMonitor monitor = new BooleanRequestMonitor(this, size);
	
	        Iterator itr = selection.iterator();
	        while (itr.hasNext()) {
	            Object element = itr.next();
	            isEnabledFor(element, monitor);
	        }
        }
        else
        {
        	BooleanRequestMonitor monitor = new BooleanRequestMonitor(this, 1);
        	monitor.setResult(true);
        	monitor.done();
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getInitialEnablement()
	 */
	protected boolean getInitialEnablement() {
		return true;
	}
	
	

}
