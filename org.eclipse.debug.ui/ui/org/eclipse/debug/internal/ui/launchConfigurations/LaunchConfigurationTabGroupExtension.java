package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
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

