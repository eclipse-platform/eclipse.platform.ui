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
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action that controls the appearance of the details pane in debug views such
 * as the VariablesView and the ExpressionsView.  Instances of this class can be
 * created to show the detail pane underneath the main tree, to the right of the
 * main tree, or not shown at all.
 * 
 * @since 3.0
 */
public class ToggleDetailPaneAction extends Action {

	private VariablesView fVariablesView;
	
	private String fOrientation;

	public ToggleDetailPaneAction(VariablesView view, String orientation, String hiddenLabel) {
		super("", AS_RADIO_BUTTON); //$NON-NLS-1$
		setVariablesView(view);
		setOrientation(orientation);
				
		if (orientation == IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_UNDERNEATH) {
			setText(ActionMessages.getString("ToggleDetailPaneAction.1"));  //$NON-NLS-1$
			setToolTipText(ActionMessages.getString("ToggleDetailPaneAction.2"));  //$NON-NLS-1$
			setDescription(ActionMessages.getString("ToggleDetailPaneAction.3"));  //$NON-NLS-1$
			setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_DETAIL_PANE_UNDER));
			setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_DETAIL_PANE_UNDER));
			setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_DETAIL_PANE_UNDER));
		} else if (orientation == IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_RIGHT) {
			setText(ActionMessages.getString("ToggleDetailPaneAction.4"));  //$NON-NLS-1$
			setToolTipText(ActionMessages.getString("ToggleDetailPaneAction.5"));  //$NON-NLS-1$
			setDescription(ActionMessages.getString("ToggleDetailPaneAction.6"));  //$NON-NLS-1$
			setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_DETAIL_PANE_RIGHT));
			setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_DETAIL_PANE_RIGHT));
			setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_DETAIL_PANE_RIGHT));
		} else {
			setText(hiddenLabel);
			setToolTipText(ActionMessages.getString("ToggleDetailPaneAction.8"));  //$NON-NLS-1$
			setDescription(ActionMessages.getString("ToggleDetailPaneAction.9"));  //$NON-NLS-1$
			setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_DETAIL_PANE_HIDE));
			setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_DETAIL_PANE_HIDE));
			setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_DETAIL_PANE_HIDE));
		} 		
		WorkbenchHelp.setHelp(this, IDebugHelpContextIds.SHOW_DETAIL_PANE_ACTION);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		getVariablesView().setDetailPaneOrientation(getOrientation()); 
	}
	
	private VariablesView getVariablesView() {
		return fVariablesView;
	}

	private void setVariablesView(VariablesView variablesView) {
		fVariablesView = variablesView;
	}

	private void setOrientation(String orientation) {
		fOrientation = orientation;
	}

	public String getOrientation() {
		return fOrientation;
	}
}

