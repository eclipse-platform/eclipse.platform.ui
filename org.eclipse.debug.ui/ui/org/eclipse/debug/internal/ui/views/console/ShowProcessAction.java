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
package org.eclipse.debug.internal.ui.views.console;


import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;

/**
 * Sets the console to display output for a specific process.
 */
public class ShowProcessAction extends Action {
	
	private ConsoleView fView;
	private IProcess fProcess;

	/**
	 * Constructor for ShowProcessAction.
	 */
	public ShowProcessAction(ConsoleView view, IProcess process) {
		fView = view;
		fProcess = process;
		IDebugTarget target = (IDebugTarget)process.getAdapter(IDebugTarget.class);
		ImageDescriptor imageDescriptor = null;
		String label = null;
		ILabelProvider labelProvider = DebugUIPlugin.getDefaultLabelProvider();
		if (target == null) {
			if (process.isTerminated()) {
				imageDescriptor = DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_OBJS_OS_PROCESS_TERMINATED);
			} else {
				imageDescriptor = DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_OBJS_OS_PROCESS);
			}
			label = labelProvider.getText(process);
		} else {
			if (target.isTerminated() || target.isDisconnected()) {
				imageDescriptor = DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET_TERMINATED);
			} else {
				imageDescriptor = DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET);
			}
			label = labelProvider.getText(target);
		}
		setImageDescriptor(imageDescriptor);
		// prefix label with launch name
		ILaunch launch = process.getLaunch();
		if (launch != null) {
			ILaunchConfiguration configuration = launch.getLaunchConfiguration();
			if (configuration != null) {
				StringBuffer buffer = new StringBuffer();
				buffer.append('[');
				buffer.append(configuration.getName());
				buffer.append("] "); //$NON-NLS-1$
				buffer.append(label);
				label = buffer.toString();
			}
		}
		setText(label);
	}


	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fView.setMode(ConsoleView.MODE_SPECIFIC_PROCESS);
		fView.setViewerInput(fProcess);
	}

}
