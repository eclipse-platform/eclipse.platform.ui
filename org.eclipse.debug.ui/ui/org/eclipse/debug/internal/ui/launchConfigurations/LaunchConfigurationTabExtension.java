package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.internal.core.LaunchManager;

/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */

/**
 * Proxy to a launch configuration tab element
 */
public class LaunchConfigurationTabExtension {
	
	/**
	 * The configuration element defining this tab.
	 */
	private IConfigurationElement fConfig;
	
	/**
	 * Constructs a launch configuration tab extension based
	 * on the given configuration element
	 * 
	 * @param element the configuration element defining the
	 *  attribtues of this launch configuration tab extension
	 * @return a new launch configuration tab extension
	 */
	public LaunchConfigurationTabExtension(IConfigurationElement element) {
		setConfigurationElement(element);
	}
	
	/**
	 * Sets the configuration element that defines the attributes
	 * for this launch configuration tab extension.
	 * 
	 * @param element configuration element
	 */
	private void setConfigurationElement(IConfigurationElement element) {
		fConfig = element;
	}
	
	/**
	 * Returns the configuration element that defines the attributes
	 * for this launch configuration tab extension.
	 * 
	 * @param configuration element that defines the attributes
	 *  for this launch configuration tab extension
	 */
	protected IConfigurationElement getConfigurationElement() {
		return fConfig;
	}
	
	/**
	 * Returns whether this tab applies to all launch configurations
	 * or to only a specific type of launch configuration
	 * 
	 * @return whether this tab applies to all launch configurations
	 *  or to only a specific type of launch configuration
	 */
	public boolean isGeneric() {
		return getType() == null;
	}
	
	/**
	 * Returns the type of launch configuration this tab is
	 * intended for, or <code>null</code> if this tab is generic
	 * 
	 * @return the type of launch configuration this tab is
	 *  intended for, or <code>null</code> if this tab is generic
	 */
	public ILaunchConfigurationType getType() {
		String id = getTypeIdentifier();
		if (id == null) {
			return null;
		}
		return DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(id);
	}
	
	/**
	 * Returns the identifier of the type of launch configuration this
	 * tab is intended for, or <code>null</code> if this tab is generic
	 * 
	 * @return the identifier of the type of launch configuration this
	 *  tab is intended for, or <code>null</code> if this tab is generic
	 */	
	protected String getTypeIdentifier() {
		return getConfigurationElement().getAttribute("type");
	}

}

