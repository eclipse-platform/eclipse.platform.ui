/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * This class provides an action warpper for adding shared launch configuration actions to the context menu
 * of the Run->... menu item
 * 
 * @since 3.3
 */
public class SharedLaunchConfigAction extends Action {

	private ILaunchConfiguration fConfig;
	private String fMode;
	
	/**
	 * Consturtor
	 * @param text the text for the action
	 * @param image the image for the action
	 */
	public SharedLaunchConfigAction(ILaunchConfiguration config, String mode, ImageDescriptor image) {
		super(config.getName(), image);
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

}
