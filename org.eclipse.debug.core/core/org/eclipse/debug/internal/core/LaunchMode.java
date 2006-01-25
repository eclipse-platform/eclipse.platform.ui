/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

import java.text.MessageFormat;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchMode;

/**
 * Proxy to a launch mode extension.
 */
public class LaunchMode implements ILaunchMode {

	private IConfigurationElement fConfigurationElement;
	
	/**
	 * Constructs a new launch mode.
	 * 
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
		verifyAttributeExists("mode"); //$NON-NLS-1$
		verifyAttributeExists("label"); //$NON-NLS-1$
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

	private void missingAttribute(String attrName) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, MessageFormat.format(DebugCoreMessages.LaunchMode_1,new String[]{attrName}), null));		 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchMode#getIdentifier()
	 */
	public String getIdentifier() {
		return fConfigurationElement.getAttribute("mode"); //$NON-NLS-1$;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchMode#getLabel()
	 */
	public String getLabel() {
		return fConfigurationElement.getAttribute("label"); //$NON-NLS-1$;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchMode#getLaunchAsLabel()
	 */
	public String getLaunchAsLabel() {
		String label = fConfigurationElement.getAttribute("launchAsLabel"); //$NON-NLS-1$
		if (label == null) {
			return MessageFormat.format(DebugCoreMessages.LaunchMode_0, new String[]{getLabel()});
		}
		return label;
	}
}
