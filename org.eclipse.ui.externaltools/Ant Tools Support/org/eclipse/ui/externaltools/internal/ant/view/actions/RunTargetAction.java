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
package org.eclipse.ui.externaltools.internal.ant.view.actions;


import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.externaltools.internal.ant.launchConfigurations.AntLaunchShortcut;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.ant.view.elements.ProjectNode;
import org.eclipse.ui.externaltools.internal.ant.view.elements.TargetNode;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Actions which runs the selected target or the default target of the selected
 * project in the AntView.
 */
public class RunTargetAction extends Action implements IUpdate {
	
	private AntView view;
	
	public RunTargetAction(AntView view) {
		super(AntViewActionMessages.getString("RunTargetAction.Run_1"), ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_RUN)); //$NON-NLS-1$
		setToolTipText(AntViewActionMessages.getString("RunTargetAction.Run_Default")); //$NON-NLS-1$
		this.view= view;
		WorkbenchHelp.setHelp(this, IExternalToolsHelpContextIds.RUN_TARGET_ACTION);
	}

	public void run() {
		TargetNode target= getSelectedTarget();
		if (target == null) {
			return;
		}
		run(target);
	}
	
	/**
	 * Executes the given target
	 * @param target
	 */
	public void run(TargetNode target) {
		IFile file= AntUtil.getFile(target.getProject().getBuildFileName());
		AntLaunchShortcut shortcut= new AntLaunchShortcut();
		shortcut.launch(file, ILaunchManager.RUN_MODE, target.getName());
	}

	/**
	 * Updates the enablement of this action based on the user's selection
	 */
	public void update() {
		setEnabled(getSelectedTarget() != null);
	}
	
	/**
	 * Returns the selected target in the project viewer or <code>null</code> if
	 * no target is selected or more than one element is selected.
	 * 
	 * @return TargetNode the selected target
	 */
	private TargetNode getSelectedTarget() {
		IStructuredSelection selection= (IStructuredSelection) view.getProjectViewer().getSelection();
		if (selection.isEmpty()) {
			return null;
		}
		Iterator iter= selection.iterator();
		Object data= iter.next();
		if (iter.hasNext() || (!(data instanceof TargetNode) && !(data instanceof ProjectNode))) {
			// Only enable for single selection of a TargetNode or ProjectNode
			return null;
		}
		if (data instanceof TargetNode) {
			return (TargetNode)selection.getFirstElement();
		} else {
			return ((ProjectNode)selection.getFirstElement()).getDefaultTarget();
		}
	}

}
