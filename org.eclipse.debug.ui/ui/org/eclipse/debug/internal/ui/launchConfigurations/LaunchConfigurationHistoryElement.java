package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * A wrapper for entries in a launch history list.  
 */
public class LaunchConfigurationHistoryElement {

	// TXN
	/**
	 * Flag indicating whether this is 'new' style ILaunchConfiguration based history element.
	 */
	private boolean fConfigurationBased;
	
	/**
	 * The identifier of the launcher used.
	 */
	protected String fLauncherIdentifier= null;
	
	/**
	 * The memento of the launched element.
	 */
	protected String fMemento= null;
	// End of TXN
	
	/**
	 * The launch configuration
	 */
	private ILaunchConfiguration fLaunchConfiguration;
	
	/**
	 * The mode in which the launch configuration was launched
	 */
	private String fMode;
	
	/**
	 * The label for the launch
	 */
	private String fLabel;

	public LaunchConfigurationHistoryElement(ILaunchConfiguration launchConfiguration, 
											  String mode,
											  String label) {
		fConfigurationBased = true;
		setLaunchConfiguration(launchConfiguration);
		setMode(mode);
		setLabel(label);
	}

	// TXN
	public LaunchConfigurationHistoryElement(String launcherId, String elementMemento, String mode, String label) {
		fConfigurationBased = false;
		fLauncherIdentifier = launcherId;
		fMemento = elementMemento;
		setMode(mode);
		setLabel(label);
	}
	
	/**
	 * Returns the identifier of the launcher that was
	 * invoked.
	 */
	public String getLauncherIdentifier() {
		return fLauncherIdentifier;
	}
	
	/**
	 * Returns the memento of the element that was
	 * launched.
	 */
	public String getElementMemento() {
		return fMemento;
	}
	// End of TXN
	
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
	 * Sets the label for this history element
	 */
	private void setLabel(String label) {
		fLabel = label;
	}
	
	/**
	 * Returns the label for this history element
	 */
	public String getLabel() {
		return fLabel;
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
			return getLaunchConfiguration().equals(e.getLaunchConfiguration());
		}
		return false;
	}
	
	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return getLaunchConfiguration().hashCode();
	}
		
	
	/**
	 * Return whether this history element is based on a launch configuration.  
	 */
	public boolean isConfigurationBased() {
		return fConfigurationBased;
	}

}
