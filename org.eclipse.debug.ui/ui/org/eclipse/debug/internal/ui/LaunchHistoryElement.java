package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILauncher; 

 /**
  * Stores information required to re-launch a
  * previous launch.
  */
public class LaunchHistoryElement {
	
	/**
	 * The identifier of the launcher used.
	 */
	protected String fLauncherIdentifier= null;
	
	/**
	 * The memento of the launched element.
	 */
	protected String fMemento= null;
	
	/**
	 * The launch mode.
	 */
	protected String fMode=null;
	
	/**
	 * The label of the launch.
	 */
	protected String fLabel=null;
	
	public LaunchHistoryElement(String launcherId, String elementMemento, String mode, String label) {
		fLauncherIdentifier = launcherId;
		fMemento = elementMemento;
		fMode = mode;
		fLabel = label;
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
	
	/**
	 * Returns the mode of the launch.
	 */
	public String getMode() {
		return fMode;
	}

	/**
	 * Returns the label of the launch.
	 */
	public String getLabel() {
		return fLabel;
	}
	
	/**
	 * @see Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof LaunchHistoryElement) {
			LaunchHistoryElement e= (LaunchHistoryElement)o;
			return
				getLauncherIdentifier().equals(e.getLauncherIdentifier()) &&
				getElementMemento().equals(e.getElementMemento()) &&
				getMode().equals(getMode());
		}
		return false;
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
}

