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

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.IRootVariablesContentProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action to toggle the use of contributed variables content providers on and off.
 * When on, all registered variables content providers for the current debug model
 * are used.  When off, the default content provider (that shows all children)
 * is used for all debug models.
 */
public class VariablesContentProvidersToggleAction extends Action {
	
	private IDebugView fView;

	public VariablesContentProvidersToggleAction(IDebugView view) {
		super("ToggleVariablesContentProviders", Action.AS_CHECK_BOX);
		setView(view);
		setToolTipText("Toggle variable filters"); 
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_VARIABLES_CONTENT_PROVIDERS));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_VARIABLES_CONTENT_PROVIDERS));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_VARIABLES_CONTENT_PROVIDERS));
		setId(DebugUIPlugin.getUniqueIdentifier() + ".ToggleVariablesContentProviders"); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IDebugHelpContextIds.VARIABLES_CONTENT_PROVIDERS_ACTION);
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		valueChanged(isChecked());
	}

	private void valueChanged(boolean on) {
		if (getView().getViewer().getControl().isDisposed()) {
			return;
		}
		
		// Retrieve the owning view's content provider and set the flag on it if it's
		// of the appropriate type
		IRootVariablesContentProvider contentProvider = (IRootVariablesContentProvider) getView().getAdapter(IRootVariablesContentProvider.class);
		if (contentProvider != null) {
			contentProvider.setUseContentProviders(on);	
			BusyIndicator.showWhile(getView().getViewer().getControl().getDisplay(), new Runnable() {
				public void run() {
					getView().getViewer().refresh();					
				}
			});			
		}
	}

	/**
	 * @see Action#setChecked(boolean)
	 */
	public void setChecked(boolean value) {
		super.setChecked(value);
		//valueChanged(value);
	}
	
	protected IDebugView getView() {
		return fView;
	}

	protected void setView(IDebugView view) {
		fView = view;
	}

}
