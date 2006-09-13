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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchOption;

import com.ibm.icu.text.MessageFormat;

/**
 * Proxy to a launch option extension
 * 
 * @since 3.3
 */
public class LaunchOption implements ILaunchOption {

	//constants for attribute names
	private static final String ID = "id"; //$NON-NLS-1$
	private static final String LABEL = "label"; //$NON-NLS-1$
	private static final String OPTION = "option"; //$NON-NLS-1$
	
	/**
	 * The associated configuration element
	 */
	private IConfigurationElement fElement = null;
	
	/**
	 * Constructor
	 * @param element the element to associate this launch option with
	 */
	public LaunchOption(IConfigurationElement element) throws CoreException {
		fElement = element;
		verifyAttributesExist();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchOption#getIdentifier()
	 */
	public String getIdentifier() {
		return fElement.getAttribute(ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchOption#getLabel()
	 */
	public String getLabel() {
		return fElement.getAttribute(LABEL);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchOption#getOption()
	 */
	public String getOption() {
		return fElement.getAttribute(OPTION);
	}
	
	/**
	 * Checks to ensure that all of the required attributes are present in the <code>IConfigurationElement</code>
	 * @return true if all attributes are present, throws <code>CoreException</code> otherwise
	 * @throws CoreException
	 */
	private void verifyAttributesExist() throws CoreException {
		if(fElement != null) {
			if(fElement.getAttribute(ID) == null) {
				missingAttribute(ID);
			}
			if(fElement.getAttribute(LABEL) == null) {
				missingAttribute(LABEL);
			}
			if(fElement.getAttribute(OPTION) == null) {
				missingAttribute(OPTION);
			}
		}
	}
	
	/**
	 * Throws a <code>CoreException</code> indicating the specified attribute is missing from the extension
	 * point definition
	 * @param attrName the attribute name that is missing
	 * @throws CoreException
	 */
	private void missingAttribute(String attrName) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, MessageFormat.format(DebugCoreMessages.LaunchOption_0, new String[]{attrName}), null));		 
	}
}
