/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.debug.internal.core.IConfigurationElementConstants;
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
	 * Returns the set of modes specified in the configuration data, or <code>null</code>
	 * if none (i.e. default tab group)
	 * 
	 * @return the set of modes specified in the configuration data, or
	 *  <code>null</code>
	 */
	protected Set getModes() {
		if (fModes == null) {
			IConfigurationElement[] modes= getConfigurationElement().getChildren(IConfigurationElementConstants.LAUNCH_MODE);
			if (modes.length > 0) {
				fModes = new HashSet(modes.length);
				fPerspectives = new Hashtable(modes.length);
				IConfigurationElement element = null;
				String perspective = null;
				String mode = null;
				for (int i = 0; i < modes.length; i++) {
					element = modes[i];
					mode = element.getAttribute(IConfigurationElementConstants.MODE);
					fModes.add(mode);
					perspective = element.getAttribute(IConfigurationElementConstants.PERSPECTIVE);
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
		return getConfigurationElement().getAttribute(IConfigurationElementConstants.TYPE);
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
		return getConfigurationElement().getAttribute(IConfigurationElementConstants.HELP_CONTEXT_ID);		
	}
	
	/**
	 * Returns the identifier of the tab group
	 * @return the id of the tab group
	 * 
	 * @since 3.3
	 */
	protected String getIdentifier() {
		return getConfigurationElement().getAttribute(IConfigurationElementConstants.ID); 
	}
	
	/**
	 * Returns a new tab group defined by this extension
	 * 
	 * @return a new tab group defined by this extension
	 * @exception CoreException if an exception occurs instantiating
	 * 	the tab group
	 */
	public ILaunchConfigurationTabGroup newTabGroup() throws CoreException {
		return (ILaunchConfigurationTabGroup)getConfigurationElement().createExecutableExtension(IConfigurationElementConstants.CLASS); 
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
		
		IConfigurationElement[] children = fConfig.getChildren(IConfigurationElementConstants.LAUNCH_MODE);
		if (children!= null && children.length != 0) {
			IConfigurationElement child = null;
			for (int i=0; i<children.length; i++) {
				child = children[i];
				if (child.getAttribute("mode").equals(mode)) { //$NON-NLS-1$
					description = child.getAttribute(IConfigurationElementConstants.DESCRIPTION);
				}
			}
		} 
		if (description == null){
			description = fConfig.getAttribute(IConfigurationElementConstants.DESCRIPTION);
		}
		
		if (description == null)
			description = ""; //$NON-NLS-1$
		
		return description;
	}
	
}

