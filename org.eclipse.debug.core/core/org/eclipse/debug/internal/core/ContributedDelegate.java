/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

/**
 * A placeholder for a launch delegate contributed for a launch mode for an
 * existing launch configuration type.
 */
public class ContributedDelegate {

	/**
	 * The configuration element of the extension.
	 */
	private IConfigurationElement fElement;
	
	/**
	 * Modes this delegate supports.
	 */
	private Set fModes;
	
	/**
	 * Delegate, or <code>null</code> if not yet instantiated.
	 */
	private ILaunchConfigurationDelegate fDelegate;
	
	/**
	 * Constructs a new contributed delegate on the
	 * given configuration element.
	 * 
	 * @param element configuration element
	 */
	protected ContributedDelegate(IConfigurationElement element) {
		setConfigurationElement(element);
	}
	
	/**
	 * Sets this delegate's configuration element.
	 * 
	 * @param element this delegate's configuration element
	 */
	private void setConfigurationElement(IConfigurationElement element) {
		fElement = element;
	}
	
	/**
	 * Returns this delegate's configuration element.
	 * 
	 * @return this delegate's configuration element
	 */
	protected IConfigurationElement getConfigurationElement() {
		return fElement;
	}	
	
	/**
	 * Returns the set of modes specified in the configuration data.
	 * 
	 * @return the set of modes specified in the configuration data
	 */
	protected Set getModes() {
		if (fModes == null) {
			String modes= getConfigurationElement().getAttribute("modes"); //$NON-NLS-1$
			if (modes == null) {
				return new HashSet(0);
			}
			String[] strings = modes.split(","); //$NON-NLS-1$
			fModes = new HashSet(3);
			for (int i = 0; i < strings.length; i++) {
				String string = strings[i];
				fModes.add(string.trim());
			}
		}
		return fModes;
	}
	
	/**
	 * Returns the type identifier of launch configuration type this delegate is
	 * contributed to.
	 */
	protected String getLaunchConfigurationType() {
		return getConfigurationElement().getAttribute("type"); //$NON-NLS-1$
	}
	
	protected ILaunchConfigurationDelegate getDelegate() throws CoreException {
		if (fDelegate == null) {
			Object object = getConfigurationElement().createExecutableExtension("delegate"); //$NON-NLS-1$
			if (object instanceof ILaunchConfigurationDelegate) {
				fDelegate = (ILaunchConfigurationDelegate)object;
			} else {
				throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, MessageFormat.format(DebugCoreMessages.getString("LaunchConfigurationType.Launch_delegate_for_{0}_does_not_implement_required_interface_ILaunchConfigurationDelegate._1"), new String[]{getIdentifier()}), null)); //$NON-NLS-1$
			}		
		}
		return fDelegate;
	}
	
	/**
	 * Returns the identifier of this extension point.
	 */
	protected String getIdentifier() {
		return getConfigurationElement().getAttribute("id"); //$NON-NLS-1$
	}	
}
