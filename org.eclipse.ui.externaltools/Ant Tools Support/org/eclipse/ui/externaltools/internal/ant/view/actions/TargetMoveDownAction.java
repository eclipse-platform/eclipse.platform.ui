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
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Action which affects the selected targets in the active targets pane of the
 * ant view. The selected targets are moved down in the order.
 */
public class TargetMoveDownAction extends Action implements IUpdate {
	
	private AntView view;

	public TargetMoveDownAction(AntView view) {
		super(AntViewActionMessages.getString("TargetMoveDownAction.Move_Down_1"), ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_MOVE_DOWN)); //$NON-NLS-1$
		setDescription(AntViewActionMessages.getString("TargetMoveDownAction.Move_Down_2")); //$NON-NLS-1$
		setToolTipText(AntViewActionMessages.getString("TargetMoveDownAction.Move_Down_2")); //$NON-NLS-1$
		this.view= view;
	}
	
	/**
	 * Tells the Ant view to move the selected targets down.
	 */
	public void run() {
		view.moveDownTargets();
	}
	
	/**
	 * Updates the enablement of this action based on the user's selection
	 */
	public void update() {
		Table table= view.getTargetViewer().getTable();
		int indices[]= table.getSelectionIndices();
		if (indices.length == 0) {
			setEnabled(false);
		} else {
			setEnabled(indices[indices.length - 1] != table.getItemCount() - 1);
		}
	}

}
