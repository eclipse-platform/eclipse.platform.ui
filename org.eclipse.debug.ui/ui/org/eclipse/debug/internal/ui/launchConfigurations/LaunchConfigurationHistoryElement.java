package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.ui.IDebugUIConstants;

/********************************************************************************
 * 								IMPORTANT
 * This class must support TWO styles of launch history, the 'old' style, that used
 * an ILauncher ID and the memento of the launched element, and the 'new' style
 * that simply uses launch configurations.  Eventually, the old support will be
 * removed, but it must be present for some transitional period to allow launcher
 * contributors time to switch over.  All code in this class that is for old
 * style support is bracketed as follows:
 * // TXN
 * . . . code . . .
 * // End of TXN
 * Such code can simply be removed when old-style support is removed.
 * 
 ********************************************************************************/

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
				DebugUIPlugin.logError(e);
			}
		}
		return false;
	}
	
	// TXN
	/**
	 * @see Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof LaunchConfigurationHistoryElement) {
			LaunchConfigurationHistoryElement e= (LaunchConfigurationHistoryElement)o;
			if (isConfigurationBased()) {
				return getLaunchConfiguration().equals(e.getLaunchConfiguration());
			}
			else {	
				return
					getLauncherIdentifier().equals(e.getLauncherIdentifier()) &&
					getElementMemento().equals(e.getElementMemento()) &&
					getMode().equals(getMode());
			}
		}
		return false;
	}
	
	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		if (isConfigurationBased()) {
			return getLaunchConfiguration().hashCode();
		} else {
			return getElementMemento().hashCode();
		}
	}
		
	/**
	 * Returns the launcher that this history element represents.
	 * Returns null if no launcher is currently registered with the launch 
	 * manager that matches the launch identifier of this history element.
	 */
	public ILauncher getLauncher() {
		ILauncher[] launchers = DebugPlugin.getDefault().getLaunchManager().getLaunchers();
		for (int i = 0; i < launchers.length; i++) {
			if (launchers[i].getIdentifier().equals(getLauncherIdentifier())) {
				return launchers[i];
			}
		}
		return null;
	}
	
	/**
	 * Returns the launch element as decribed by the element's memento.
	 * 
	 * @see org.eclipse.debug.core.model.ILauncherDelegate#getLaunchObject(String)
	 */
	public Object getLaunchElement() {
		ILauncher launcher = getLauncher();
		if (launcher != null) {
			return launcher.getDelegate().getLaunchObject(getElementMemento());
		}
		return null;
	}	
	
	/**
	 * Return whether this history element is based on a launch configuration.  
	 */
	public boolean isConfigurationBased() {
		return fConfigurationBased;
	}
	// End of TXN

}
