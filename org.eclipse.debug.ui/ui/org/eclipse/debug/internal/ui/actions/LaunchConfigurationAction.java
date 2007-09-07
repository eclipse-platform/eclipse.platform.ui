/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import com.ibm.icu.text.MessageFormat;

/**
 * This class provides an action wrapper for adding launch configuration actions to the context menu
 * of the Run->... menu item
 * 
 * @since 3.3
 */
public class LaunchConfigurationAction extends Action {

	private ILaunchConfiguration fConfig;
	private String fMode;
	
	/**
	 * Constructor
	 * @param mode
	 * @param text the text for the action
	 * @param image the image for the action
	 */
	public LaunchConfigurationAction(ILaunchConfiguration config, String mode, String text, ImageDescriptor image, int accelerator) {
		super(MessageFormat.format(ActionMessages.LaunchConfigurationAction_0, new String[] {Integer.toString(accelerator), text}), image);
		fConfig = config;
		fMode = mode;
	}
	
	/**
	 * Allows access to the launch configuration associated with the action
	 * @return the associated launch configuration
	 */
	public ILaunchConfiguration getLaunchConfiguration() {
		return fConfig;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		DebugUITools.launch(fConfig, fMode);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(Event event) {
		if ((event.stateMask & SWT.MOD1) > 0) {
			try {
				ILaunchGroup group = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(fConfig.getType(), fMode);
				if(group != null) {
					DebugUITools.openLaunchConfigurationDialogOnGroup(DebugUIPlugin.getShell(), new StructuredSelection(fConfig), group.getIdentifier());
				}
				else {
					run();
				}
			}
			catch(CoreException ce) {}
		}
		else {
			run();
		}
	}
}
