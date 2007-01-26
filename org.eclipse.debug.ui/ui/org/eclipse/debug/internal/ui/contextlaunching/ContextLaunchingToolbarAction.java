/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contextlaunching;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupExtension;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * A wrapper class for a context launching toolbar action
 * 
 * @see ContextRunner
 * @see ContextLaunchingAction
 * @see Action
 * @see IWorkbenchWindowActionDelegate
 * 
 *  @since 3.3
 *  EXPERIMENTAL
 *  CONTEXTLAUNCHING
 */
public class ContextLaunchingToolbarAction extends Action implements IWorkbenchWindowActionDelegate {

	/**
	 * The mode this action applies to
	 */
	private String fMode = null;
	
	/**
	 * Constructor
	 * @param id
	 */
	public ContextLaunchingToolbarAction(String id) {
		LaunchGroupExtension extension = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(id);
		if (extension != null) {
			fMode = extension.getMode();
			setText(extension.getLabel());
			setImageDescriptor(extension.getImageDescriptor());
		}
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {}
	
	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {}

	/**
	 * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(Event event) {
		ContextRunner.getDefault().launch(fMode);
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		//do nothing
	}
}
