package org.eclipse.ui.externaltools.internal.ant.view.actions;

/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/

import org.eclipse.jface.action.Action;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Action which affects the selected targets in the active targets pane of the
 * ant view. The selected targets are moved up in the order.
 */
public class TargetMoveUpAction extends Action implements IUpdate {
	
	private AntView view;

	public TargetMoveUpAction(AntView view) {
		super(AntViewActionMessages.getString("TargetMoveUpAction.Move_Up_1"), ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_MOVE_UP)); //$NON-NLS-1$
		setDescription(AntViewActionMessages.getString("TargetMoveUpAction.Move_up_2")); //$NON-NLS-1$
		setToolTipText(AntViewActionMessages.getString("TargetMoveUpAction.Move_up_2")); //$NON-NLS-1$
		this.view= view;
	}
	
	/**
	 * Tells the Ant view to move the selected targets up.
	 */
	public void run() {
		view.moveUpTargets();
	}
	
	/**
	 * Updates the enablement of this action based on the user's selection
	 */
	public void update() {
		int indices[]= view.getTargetViewer().getTable().getSelectionIndices();
		if (indices.length == 0) {
			setEnabled(false);
		} else {
			setEnabled(indices[0] != 0);
		} 
	}

}
