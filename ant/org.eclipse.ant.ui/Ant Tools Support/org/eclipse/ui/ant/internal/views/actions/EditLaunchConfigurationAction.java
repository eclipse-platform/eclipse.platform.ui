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
package org.eclipse.ui.ant.internal.views.actions;


import java.util.Iterator;
import java.util.List;

import org.eclipse.ant.ui.internal.launchConfigurations.AntLaunchShortcut;
import org.eclipse.ant.ui.internal.model.AntUtil;
import org.eclipse.ant.ui.internal.views.AntView;
import org.eclipse.ant.ui.internal.views.elements.AntNode;
import org.eclipse.ant.ui.internal.views.elements.ProjectNode;
import org.eclipse.ant.ui.internal.views.elements.TargetNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Action for activating a target node selected in the ant view
 */
public class EditLaunchConfigurationAction extends Action implements IUpdate {
	
	private AntView view;
	private ProjectNode projectNode;
	
	public EditLaunchConfigurationAction(AntView view) {
		super(AntViewActionMessages.getString("EditLaunchConfigurationAction.Properties")); //$NON-NLS-1$
		setDescription(AntViewActionMessages.getString("EditLaunchConfigurationAction.Edit")); //$NON-NLS-1$
		this.view= view;
		WorkbenchHelp.setHelp(this, IExternalToolsHelpContextIds.EDIT_LAUNCH_CONFIGURATION_ACTION);
	}
	
	public void run() {
		IFile file= AntUtil.getFile(projectNode.getBuildFileName());
		ILaunchConfiguration configuration= null;
		List configurations = AntLaunchShortcut.findExistingLaunchConfigurations(file);
		if (configurations.isEmpty()) {
			configuration = AntLaunchShortcut.createDefaultLaunchConfiguration(file);
		} else {
			if (configurations.size() == 1) {
				configuration= (ILaunchConfiguration)configurations.get(0);
			} else {
				configuration= AntLaunchShortcut.chooseConfig(configurations);
				if (configuration == null) {
					// User cancelled selection
					return;
				}
			}
		}
		if (configuration != null) {
			DebugUITools.openLaunchConfigurationPropertiesDialog(view.getSite().getShell(), configuration, IExternalToolConstants.ID_EXTERNAL_TOOLS_BUILDER_LAUNCH_GROUP);
		}
	}

	/**
	 * Updates the enablement of this action based on the user's selection
	 */
	public void update() {
		boolean enabled= false;
		IStructuredSelection selection= (IStructuredSelection) view.getProjectViewer().getSelection();
		if (selection.size() == 1) {
			Object element= selection.getFirstElement();
			if (element instanceof ProjectNode) {
				if (!((ProjectNode)element).isErrorNode()) {
					enabled= true;
					projectNode= (ProjectNode)element;
				}
			} else if (element instanceof TargetNode) {
				if (!((TargetNode)element).isErrorNode()) {
					enabled= true;
					projectNode= getProjectNode((AntNode)element);
				}
			} else if (element instanceof AntNode) {
				projectNode= getProjectNode((AntNode)element);
				enabled= true;
			}
		} else if (!selection.isEmpty()){
			//all non-error nodes from the same project
			Iterator iter= selection.iterator();
			Object data= null;
			enabled= true;
			ProjectNode tempProjectNode= null;
			while (iter.hasNext()) {
				data= iter.next();
				if (!(data instanceof AntNode)) {
					continue;
				}
				if (data instanceof TargetNode) {
					TargetNode targetNode= (TargetNode) data;
					if(targetNode.isErrorNode()) {
						enabled= false;
						break;
					}
				}
				if (data instanceof ProjectNode) {
					ProjectNode projectNode= (ProjectNode) data;
					if(projectNode.isErrorNode()) {
						enabled= false;
						break;
					}
				}
			
				tempProjectNode= getProjectNode((AntNode)data);
				if (projectNode != null) {
					if (!(projectNode.equals(tempProjectNode))) {
						enabled= false;
						break;
					}
				} else {
					projectNode= tempProjectNode;
				}
			}
		}
		
		if (!enabled) {
			projectNode= null;
		}
		setEnabled(enabled);
	}

	/**
	 * Method getProjectNode.
	 * @param antNode
	 * @return ProjectNode
	 */
	private ProjectNode getProjectNode(AntNode antNode) {
		AntNode parentNode= null;
		if (antNode instanceof ProjectNode) {
			parentNode= antNode;
		} else {
			parentNode= antNode.getParent();
			while (parentNode != null && !(parentNode instanceof ProjectNode)) {
				parentNode= parentNode.getParent();
			}
		}
		return (ProjectNode)parentNode;
	}
}
