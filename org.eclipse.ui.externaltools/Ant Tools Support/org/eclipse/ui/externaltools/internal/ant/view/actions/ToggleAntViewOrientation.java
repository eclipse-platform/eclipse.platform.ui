package org.eclipse.ui.externaltools.internal.ant.view.actions;

/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/

import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;

/**
 * Action that switches the orientation of the Ant View, toggling it between
 * vertical and horizontal alignment.
 */
public class ToggleAntViewOrientation extends Action {
	
	private AntView view;
	private int orientation;
	
	public ToggleAntViewOrientation(AntView view, int orientation) {
		super("", AS_RADIO_BUTTON);  //$NON-NLS-1$
		this.view= view;
		this.orientation= orientation;
		if (orientation == AntView.HORIZONTAL_ORIENTATION) {
			setText(AntViewActionMessages.getString("SwitchAntViewOrientation.Horizontal_View_Orientation_1")); //$NON-NLS-1$
			setToolTipText(AntViewActionMessages.getString("SwitchAntViewOrientation.Align_horizontally")); //$NON-NLS-1$
			setDescription(AntViewActionMessages.getString("SwitchAntViewOrientation.Align_horizontally")); //$NON-NLS-1$
			ExternalToolsImages.setLocalImageDescriptors(this, "th_horizontal.gif"); //$NON-NLS-1$
		} else if (orientation == AntView.VERTICAL_ORIENTATION) {
			setText(AntViewActionMessages.getString("SwitchAntViewOrientation.Vertical_View_Orientation_4")); //$NON-NLS-1$
			setToolTipText(AntViewActionMessages.getString("SwitchAntViewOrientation.Align_vertically")); //$NON-NLS-1$
			setDescription(AntViewActionMessages.getString("SwitchAntViewOrientation.Align_vertically")); //$NON-NLS-1$
			ExternalToolsImages.setLocalImageDescriptors(this, "th_vertical.gif"); //$NON-NLS-1$
		} else if (orientation== AntView.SINGLE_ORIENTATION){
			setText(AntViewActionMessages.getString("ToggleAntViewOrientation.Project_View_Only_4")); //$NON-NLS-1$
			setToolTipText(AntViewActionMessages.getString("ToggleAntViewOrientation.Only_Project")); //$NON-NLS-1$
			setDescription(AntViewActionMessages.getString("ToggleAntViewOrientation.Only_Project")); //$NON-NLS-1$
			ExternalToolsImages.setLocalImageDescriptors(this, "th_single.gif"); //$NON-NLS-1$
		} else {
			Assert.isTrue(false, AntViewActionMessages.getString("SwitchAntViewOrientation.Invalid")); //$NON-NLS-1$
		}
	}

	/**
	 * Toggle's the ant view's orientation
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		view.setViewOrientation(orientation);
	}
}
