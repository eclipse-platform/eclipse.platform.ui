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
package org.eclipse.debug.internal.ui.launchConfigurations;

 
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

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
	 * Perspectives for each mode
	 */
	private Map fPerspectives;
	
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
			IConfigurationElement[] modes= getConfigurationElement().getChildren("launchMode"); //$NON-NLS-1$
			if (modes.length > 0) {
				fModes = new HashSet(modes.length);
				fPerspectives = new Hashtable(modes.length);
				for (int i = 0; i < modes.length; i++) {
					IConfigurationElement element = modes[i];
					String mode = element.getAttribute("mode"); //$NON-NLS-1$
					fModes.add(mode);
					String perspective = element.getAttribute("perspective"); //$NON-NLS-1$
					if (perspective != null) {
						fPerspectives.put(mode, perspective);
					}
				}
			}
		}
		return fModes;
	}
	
	/**
	 * Returns the perspective associated with the given launch
	 * mode, as specified in plug-in XML, or <code>null</code> if none.
	 * 
	 * @param mode launch mode
	 * @return perspective identifier, or <code>null</code>
	 */
	protected String getPerspective(String mode) {
		// ensure modes are initialized
		getModes();
		String id = null;
		if (fPerspectives != null) {
			id = (String)fPerspectives.get(mode);
		}
		return id;
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
	 * @exception CoreException if an exception occurs instantiating
	 * 	the tab group
	 */
	public ILaunchConfigurationTabGroup newTabGroup() throws CoreException {
		return (ILaunchConfigurationTabGroup)getConfigurationElement().createExecutableExtension("class"); //$NON-NLS-1$
	}

	/**
	 * Returns this tab group's description in the given mode.
	 *
	 * @param mode the mode
	 * @return a description of the Launch Mode if available. If not available, attempts to return
	 * a description of the Launch Configuration. If no appropriate description is found an empty string is returned.
	 */
	public String getDescription(String mode) {
		String description = null;
		
		IConfigurationElement[] children = fConfig.getChildren("launchMode"); //$NON-NLS-1$
		if (children!= null && children.length != 0) {
			for (int i=0; i<children.length; i++) {
				IConfigurationElement child = children[i];
				if (child.getAttribute("mode").equals(mode)) { //$NON-NLS-1$
					description = child.getAttribute("description"); //$NON-NLS-1$
				}
			}
		} 
		if (description == null){
			description = fConfig.getAttribute("description"); //$NON-NLS-1$
		}
		
		if (description == null)
			description = ""; //$NON-NLS-1$
		
		return description;
	}
}

