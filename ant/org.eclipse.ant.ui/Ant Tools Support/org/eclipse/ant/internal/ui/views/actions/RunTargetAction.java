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
import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.AntUtil;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.ant.internal.ui.IAntUIHelpContextIds;
import org.eclipse.ant.internal.ui.launchConfigurations.AntLaunchShortcut;
import org.eclipse.ant.internal.ui.views.AntView;
import org.eclipse.ant.internal.ui.views.elements.AntNode;
import org.eclipse.ant.internal.ui.views.elements.ProjectNode;
import org.eclipse.ant.internal.ui.views.elements.TargetNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchManager;
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
	
	private AntView view;
	private boolean showDialog;
	
	/**
	 * Creates a new <code>RunTargetAction</code> which will execute
	 * targets in the given view.
	 * @param view the Ant view whose selection this action will use when
	 * determining which target to run.
	 * @param showDialog whether or not to display the launch configuration dialog to edit the
	 * associated launch configuration
	 */
	public RunTargetAction(AntView view, boolean showDialog) {
		
		if (showDialog) {
			setText(AntViewActionMessages.getString("RunTargetAction.4")); //$NON-NLS-1$
			setImageDescriptor(AntUIImages.getImageDescriptor(IAntUIConstants.IMG_TAB_ANT_TARGETS));
			WorkbenchHelp.setHelp(this, IAntUIHelpContextIds.EDIT_LAUNCH_CONFIGURATION_ACTION);
		} else {
			setText(AntViewActionMessages.getString("RunTargetAction.Run_1")); //$NON-NLS-1$
			setImageDescriptor(AntUIImages.getImageDescriptor(IAntUIConstants.IMG_RUN));
			WorkbenchHelp.setHelp(this, IAntUIHelpContextIds.RUN_TARGET_ACTION);
		}
		setToolTipText(AntViewActionMessages.getString("RunTargetAction.Run_Default")); //$NON-NLS-1$
		this.view= view;
		this.showDialog= showDialog;
	}

	/**
	 * Executes the selected target or project in the Ant view.
	 */
	public void run() {
		UIJob job= new UIJob(AntViewActionMessages.getString("RunTargetAction.2")) { //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				TargetNode target= getSelectedTarget();
				if (target == null) {
					return new Status(IStatus.ERROR, AntUIPlugin.getUniqueIdentifier(), IStatus.ERROR, AntViewActionMessages.getString("RunTargetAction.3"), null); //$NON-NLS-1$
				}
				runTarget(target);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
	
	/**
	 * Executes the given target
	 * @param target the target to execute
	 */
	public void runTarget(TargetNode target) {
		IFile file= AntUtil.getFile(target.getProject().getBuildFileName());
		if (file == null) {
			AntUIPlugin.getStandardDisplay().beep();
			return;
		}
		AntLaunchShortcut shortcut= new AntLaunchShortcut();
		shortcut.setShowDialog(showDialog);
		shortcut.launch(file, ILaunchManager.RUN_MODE, target.getName());
	}

	/**
	 * Updates the enablement of this action based on the user's selection
	 */
	public void update() {
		AntNode selection= getSelectedElement();
		boolean enabled= false;
		if (selection instanceof TargetNode) {
			if (!((TargetNode) selection).isErrorNode()) {
				enabled= true;
			}
		} else if (selection instanceof ProjectNode) {
			if (!((ProjectNode) selection).isErrorNode()) {
				enabled= true;
			}
		}
		setEnabled(enabled);
	}
	
	/**
	 * Returns the selected target or project node or <code>null</code> if no target or
	 * project is selected or if more than one element is selected.
	 * 
	 * @return AntNode the selected <code>TargetNode</code> or <code>ProjectNode</code>
	 */
	private AntNode getSelectedElement() {
		IStructuredSelection selection= (IStructuredSelection) view.getProjectViewer().getSelection();
		if (selection.isEmpty()) {
			return null;
		}
		Iterator iter= selection.iterator();
		Object data= iter.next();
		if (iter.hasNext() || (!(data instanceof TargetNode) && !(data instanceof ProjectNode))) {
			// Only return a TargetNode or ProjectNode
			return null;
		}
		return (AntNode) data;
	}
	
	/**
	 * Returns the selected target in the project viewer or <code>null</code> if
	 * no target is selected or more than one element is selected.
	 * 
	 * @return TargetNode the selected target
	 */
	private TargetNode getSelectedTarget() {
		AntNode selectedNode= getSelectedElement();
		if (selectedNode instanceof TargetNode) {
			return (TargetNode) selectedNode;
		} else if (selectedNode instanceof ProjectNode) {
			return ((ProjectNode) selectedNode).getDefaultTarget();
		}
		return null;
	}
}