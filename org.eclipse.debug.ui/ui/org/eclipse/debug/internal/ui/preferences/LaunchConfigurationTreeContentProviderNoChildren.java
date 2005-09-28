/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.preferences;

import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationTreeContentProvider;
import org.eclipse.swt.widgets.Shell;

/**
 * Overwritten class to provide the list of launch configuration types without including 
 * the actual launch configurations as children of the types.
 * 
 * @since 3.2
 */
public class LaunchConfigurationTreeContentProviderNoChildren extends LaunchConfigurationTreeContentProvider {

	/**
	 * Default constructor
	 * @param mode the mode to display or null to view all modes
	 * @param shell the shell
	 */
	public LaunchConfigurationTreeContentProviderNoChildren(String mode, Shell shell) {
		super(mode, shell);
	}//end constructor

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		return new Object[] {};
	}
}//end class
