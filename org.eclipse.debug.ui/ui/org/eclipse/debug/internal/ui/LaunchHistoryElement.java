package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IResource; 

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
	
	/**
	 * The resource associated with the launched element
	 */
	protected IResource fResource;
	
	public LaunchHistoryElement(String launcherId, String elementMemento, String mode, String label, IResource resource) {
		fLauncherIdentifier = launcherId;
		fMemento = elementMemento;
		fMode = mode;
		fLabel = label;
		fResource = resource;
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
	
	/**
	 * Returns the resource associated with the launched element
	 */
	public IResource getResource() {
		return fResource;
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
}

