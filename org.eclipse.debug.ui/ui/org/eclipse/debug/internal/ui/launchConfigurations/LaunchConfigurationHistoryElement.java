package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.ILaunchConfiguration;

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
	
	/**
	 * The label for the launch
	 */
	private String fLabel;

	public LaunchConfigurationHistoryElement(ILaunchConfiguration launchConfiguration, 
											  String mode,
											  String label) {
		setLaunchConfiguration(launchConfiguration);
		setMode(mode);
		setLabel(label);
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
	
	
}
