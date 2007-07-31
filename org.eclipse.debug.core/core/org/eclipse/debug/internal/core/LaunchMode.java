/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

import com.ibm.icu.text.MessageFormat;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchMode;

/**
 * Proxy to a launch mode extension.
 * 
 * @see IConfigurationElementConstants
 */
public class LaunchMode implements ILaunchMode {

	private IConfigurationElement fConfigurationElement;
	
	/**
	 * Constructs a new launch mode.
	 * 
	 * @param element configuration element
	 * @exception CoreException if required attributes are missing
	 */
	public LaunchMode(IConfigurationElement element) throws CoreException {
		fConfigurationElement = element;
		verifyAttributes();
	}

	/**
	 * Verifies required attributes.
	 * 
	 * @exception CoreException if required attributes are missing
	 */
	private void verifyAttributes() throws CoreException {
		verifyAttributeExists(IConfigurationElementConstants.MODE);
		verifyAttributeExists(IConfigurationElementConstants.LABEL);
	}
	
	/**
	 * Verifies the given attribute exists
	 * 
	 * @exception CoreException if attribute does not exist
	 */
	private void verifyAttributeExists(String name) throws CoreException {
		if (fConfigurationElement.getAttribute(name) == null) {
			missingAttribute(name);
		}
	}

	/**
	 * This method is used to create a new internal error describing that the specified attribute
	 * is missing 
	 * @param attrName the name of the attribute that is missing
	 * @throws CoreException
	 */
	private void missingAttribute(String attrName) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.ERROR, MessageFormat.format(DebugCoreMessages.LaunchMode_1,new String[]{attrName}), null));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchMode#getIdentifier()
	 */
	public String getIdentifier() {
		return fConfigurationElement.getAttribute(IConfigurationElementConstants.MODE);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchMode#getLabel()
	 */
	public String getLabel() {
		return fConfigurationElement.getAttribute(IConfigurationElementConstants.LABEL);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchMode#getLaunchAsLabel()
	 */
	public String getLaunchAsLabel() {
		String label = fConfigurationElement.getAttribute(IConfigurationElementConstants.LAUNCH_AS_LABEL);
		if (label == null) {
			return MessageFormat.format(DebugCoreMessages.LaunchMode_0, new String[]{getLabel()});
		}
		return label;
	}
}
