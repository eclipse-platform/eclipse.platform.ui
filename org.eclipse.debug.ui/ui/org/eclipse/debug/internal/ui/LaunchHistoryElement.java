package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.ui.model.IWorkbenchAdapter; 

 /**
  * Stores information required to re-launch a
  * previous launch.
  */
public class LaunchHistoryElement {
	
	/**
	 * The identifier of the launcher used
	 */
	protected String fLauncherIdentifier= null;
	
	/**
	 * The memento of the launched element
	 */
	protected String fMemento= null;
	
	/**
	 * The launch mode
	 */
	protected String fMode=null;
	
	/**
	 * The label of the launch
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
	 * Returns the mode of the lanuch.
	 */
	public String getMode() {
		return fMode;
	}

	/**
	 * Returns the label of the launch
	 */
	public String getLabel() {
		return fLabel;
	}
	
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
	
	public ILauncher getLauncher() {
		ILauncher[] launchers = DebugPlugin.getDefault().getLaunchManager().getLaunchers();
		for (int i = 0; i < launchers.length; i++) {
			if (launchers[i].getIdentifier().equals(getLauncherIdentifier())) {
				return launchers[i];
			}
		}
		return null;
	}
	
	public Object getLaunchElement() {
		ILauncher launcher = getLauncher();
		if (launcher != null) {
			return launcher.getDelegate().getLaunchObject(getElementMemento());
		}
		return null;
	}
	
}

