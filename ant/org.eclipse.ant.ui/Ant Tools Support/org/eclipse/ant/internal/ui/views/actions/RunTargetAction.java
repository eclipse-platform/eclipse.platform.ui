/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.views.actions;

import java.util.Iterator;

import org.eclipse.ant.internal.ui.AntUIImages;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.ant.internal.ui.IAntUIHelpContextIds;
import org.eclipse.ant.internal.ui.launchConfigurations.AntLaunchShortcut;
import org.eclipse.ant.internal.ui.model.AntElementNode;
import org.eclipse.ant.internal.ui.model.AntProjectNode;
import org.eclipse.ant.internal.ui.model.AntTargetNode;
import org.eclipse.ant.internal.ui.views.AntView;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Action which runs the selected target or the default target of the selected
 * project in the AntView.
 */
public class RunTargetAction extends Action implements IUpdate {
	
	private AntView fView;
	
	/**
	 * Creates a new <code>RunTargetAction</code> which will execute
	 * targets in the given view.
	 * @param view the Ant view whose selection this action will use when
	 * determining which target to run.
	 */
	public RunTargetAction(AntView view) {
		
		setText(AntViewActionMessages.getString("RunTargetAction.Run_1")); //$NON-NLS-1$
		setImageDescriptor(AntUIImages.getImageDescriptor(IAntUIConstants.IMG_RUN));
		WorkbenchHelp.setHelp(this, IAntUIHelpContextIds.RUN_TARGET_ACTION);

		setToolTipText(AntViewActionMessages.getString("RunTargetAction.Run_Default")); //$NON-NLS-1$
		fView= view;
	}

	/**
	 * Executes the selected target or project in the Ant view.
	 */
	public void run() {
		UIJob job= new UIJob(AntViewActionMessages.getString("RunTargetAction.2")) { //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				launch(getSelectedElement());
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
	
	/**
	 * Launches the given Ant element node
	 * @param node the node to use to launch
	 * @see AntLaunchShortcut.launch(AntElementNode)
	 */
	public void launch(AntElementNode node) {
		AntLaunchShortcut shortcut= new AntLaunchShortcut();
		shortcut.setShowDialog(false);
		shortcut.launch(node);
	}

	/**
	 * Updates the enablement of this action based on the user's selection
	 */
	public void update() {
		AntElementNode selection= getSelectedElement();
		boolean enabled= false;
		if (selection instanceof AntTargetNode) {
			if (!((AntTargetNode) selection).isErrorNode()) {
				enabled= true;
			}
		} else if (selection instanceof AntProjectNode) {
			if (!((AntProjectNode) selection).isErrorNode()) {
				enabled= true;
			}
		}
		setEnabled(enabled);
	}
	
	/**
	 * Returns the selected target or project node or <code>null</code> if no target or
	 * project is selected or if more than one element is selected.
	 * 
	 * @return AntElementNode the selected <code>AntTargetNode</code> or <code>AntProjectNode</code>
	 */
	private AntElementNode getSelectedElement() {
		IStructuredSelection selection= (IStructuredSelection) fView.getViewer().getSelection();
		if (selection.isEmpty()) {
			return null;
		}
		Iterator iter= selection.iterator();
		Object data= iter.next();
		if (iter.hasNext() || (!(data instanceof AntTargetNode) && !(data instanceof AntProjectNode))) {
			// Only return a AntTargetNode or AntProjectNode
			return null;
		}
		return (AntElementNode) data;
	}	
}