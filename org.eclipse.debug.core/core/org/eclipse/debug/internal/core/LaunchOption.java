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
package org.eclipse.debug.internal.core;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.ILaunchOption;

/**
 * Proxy to a launch option extension
 * 
 * @since 3.3
 */
public class LaunchOption implements ILaunchOption {
	
	/**
	 * The associated configuration element
	 */
	private IConfigurationElement fElement = null;
	
	/**
	 * Constructor
	 * @param element the element to associate this launch option with
	 */
	public LaunchOption(IConfigurationElement element) {
		fElement = element;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchOption#getIdentifier()
	 */
	public String getIdentifier() {
		return fElement.getAttribute(IConfigurationElementConstants.ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchOption#getLabel()
	 */
	public String getLabel() {
		return fElement.getAttribute(IConfigurationElementConstants.LABEL);
	}
}
