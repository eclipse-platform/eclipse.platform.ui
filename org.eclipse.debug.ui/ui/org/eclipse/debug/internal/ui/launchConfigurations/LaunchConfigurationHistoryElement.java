package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * A wrapper for entries in a launch history list.  
 */
public class LaunchConfigurationHistoryElement {	
	/**
	 * The launch configuration
	 */
	private ILaunchConfiguration fLaunchConfiguration;
	
	/**
	 * The mode in which the launch configuration was launched
	 */
	private String fMode;
	
	public LaunchConfigurationHistoryElement(ILaunchConfiguration launchConfiguration, 
											  String mode) {
		setLaunchConfiguration(launchConfiguration);
		setMode(mode);
	}
	
	/**
	 * Sets the launch configuration for this history element
	 */
	private void setLaunchConfiguration(ILaunchConfiguration launchConfiguration) {
		fLaunchConfiguration = launchConfiguration;
	}

	/**
	 * Returns the launch configuration for this history element
	 */
	public ILaunchConfiguration getLaunchConfiguration() {
		return fLaunchConfiguration;
	}

	/**
	 * Sets the mode for this history element
	 */
	private void setMode(String mode) {
		fMode = mode;
	}

	/**
	 * Returns the mode for this history element
	 */
	public String getMode() {
		return fMode;
	}
		
	/**
	 * Returns the label for this history element
	 */
	public String getLabel() {
		return DebugUIPlugin.getDefaultLabelProvider().getText(getLaunchConfiguration());
	}
	
	/**
	 * Returns whether this history element represents
	 * a favorite launch configuration.
	 * 
	 * @return whether this history element represents
	 * a favorite launch configuration
	 */
	public boolean isFavorite() {
		if (getLaunchConfiguration() != null) {
			try {
				if (getMode().equals(ILaunchManager.DEBUG_MODE)) {
					return getLaunchConfiguration().getAttribute(IDebugUIConstants.ATTR_DEBUG_FAVORITE, false);
				} else {
					return getLaunchConfiguration().getAttribute(IDebugUIConstants.ATTR_RUN_FAVORITE, false);
				}
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		return false;
	}
	
	/**
	 * @see Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof LaunchConfigurationHistoryElement) {
			LaunchConfigurationHistoryElement e= (LaunchConfigurationHistoryElement)o;
			return getLaunchConfiguration().equals(e.getLaunchConfiguration()) &&
			getMode().equals(e.getMode());
		}
		return false;
	}
	
	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return getLaunchConfiguration().hashCode();
	}
		
}
