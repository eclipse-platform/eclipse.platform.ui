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
package org.eclipse.debug.internal.ui.launchConfigurations;

 
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;


/**
 * Proxy to a launch configuration tab group element
 */
public class LaunchConfigurationTabGroupExtension {
	
	/**
	 * The configuration element defining this tab group.
	 */
	private IConfigurationElement fConfig;
	
	/**
	 * Modes this tab group is applicable to or, <code>null</code> if
	 * default.
	 */
	private Set fModes;
	
	/**
	 * Constructs a launch configuration tab extension based
	 * on the given configuration element
	 * 
	 * @param element the configuration element defining the
	 *  attribtues of this launch configuration tab extension
	 * @return a new launch configuration tab extension
	 */
	public LaunchConfigurationTabGroupExtension(IConfigurationElement element) {
		setConfigurationElement(element);
	}
	
	/**
	 * Sets the configuration element that defines the attributes
	 * for this launch configuration tab group extension.
	 * 
	 * @param element configuration element
	 */
	private void setConfigurationElement(IConfigurationElement element) {
		fConfig = element;
	}
	
	/**
	 * Returns the configuration element that defines the attributes
	 * for this launch configuration tab group extension.
	 * 
	 * @param configuration element that defines the attributes
	 *  for this launch configuration tab extension
	 */
	protected IConfigurationElement getConfigurationElement() {
		return fConfig;
	}
	
	/**
	 * Returns the type of launch configuration this tab group is associated with
	 * 
	 * @return the type of launch configuration this tab group is associated with
	 */
	public ILaunchConfigurationType getType() {
		return DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(getTypeIdentifier());
	}
	
	/**
	 * Returns the set of modes specified in the configuration data, or <code>null</code>
	 * if none (i.e. default tab group)
	 * 
	 * @return the set of modes specified in the configuration data, or
	 *  <code>null</code>
	 */
	protected Set getModes() {
		if (fModes == null) {
			String modes= getConfigurationElement().getAttribute("modes"); //$NON-NLS-1$
			if (modes != null) {
				StringTokenizer tokenizer= new StringTokenizer(modes, ","); //$NON-NLS-1$
				fModes = new HashSet(tokenizer.countTokens());
				while (tokenizer.hasMoreTokens()) {
					fModes.add(tokenizer.nextToken().trim());
				}
			}
		}
		return fModes;
	}	
	
	/**
	 * Returns the identifier of the type of launch configuration this
	 * tab group is associated with
	 * 
	 * @return the identifier of the type of launch configuration this
	 *  tab group is associated with
	 */	
	protected String getTypeIdentifier() {
		return getConfigurationElement().getAttribute("type"); //$NON-NLS-1$
	}
	
	/**
	 * Returns the identifier of the help context associated with this tab
	 * group, or <code>null</code> if one was not specified.
	 * 
	 * @return the identifier of this tab group's help context or
	 * <code>null</code>
	 * @since 2.1
	 */	
	protected String getHelpContextId() {
		return getConfigurationElement().getAttribute("helpContextId"); //$NON-NLS-1$		
	}
	
	/**
	 * Returns a new tab group defined by this extension
	 * 
	 * @return a new tab group defined by this extension
	 * @exception CoreException if an exception occurrs instantiating
	 * 	the tab group
	 */
	public ILaunchConfigurationTabGroup newTabGroup() throws CoreException {
		return (ILaunchConfigurationTabGroup)getConfigurationElement().createExecutableExtension("class"); //$NON-NLS-1$
	}


}

