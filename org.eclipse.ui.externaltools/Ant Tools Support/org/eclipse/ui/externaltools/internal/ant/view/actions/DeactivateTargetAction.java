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

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.ant.view.elements.TargetNode;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.texteditor.IUpdate;

public class DeactivateTargetAction extends Action implements IUpdate {
	
	private AntView view;

	public DeactivateTargetAction(AntView view) {
		super(AntViewActionMessages.getString("DeactivateTargetAction.Deactivate"), ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_DEACTIVATE)); //$NON-NLS-1$
		setDescription(AntViewActionMessages.getString("DeactivateTargetAction.Deactivate_selected")); //$NON-NLS-1$
		this.view= view;
	}
	
	public void run() {
		view.deactivateSelectedTargets();
	}
	
	/**
	 * Updates the enablement of this action based on the user's selection
	 */
	public void update() {
		IStructuredSelection selection= (IStructuredSelection) view.getTargetViewer().getSelection();
		setEnabled(!selection.isEmpty());
	}

	/**
	 * Returns the selected target in the target viewer or <code>null</code> if
	 * no target is selected or more than one element is selected.
	 *
	 * @return TargetNode the selected target
	 */
	public TargetNode getSelectedTarget() {
		IStructuredSelection selection= (IStructuredSelection) view.getTargetViewer().getSelection();
		if (selection.isEmpty()) {
			return null;
		}
		Iterator iter= selection.iterator();
		while (iter.hasNext()) {
			Object data= iter.next();
			if (iter.hasNext() || !(data instanceof TargetNode)) {
				// Only enable for single selection of a TargetNode
				return null;
			}
		}
		return (TargetNode)selection.getFirstElement();
	}

}
