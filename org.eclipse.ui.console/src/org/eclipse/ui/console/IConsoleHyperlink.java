
package org.eclipse.ui.console;

public interface IConsoleHyperlink {
	
	/**
	 * Notification that the mouse has entered this link's region.
	 */
	public void linkEntered();
	
	/**
	 * Notification that the mouse has exited this link's region
	 */
	public void linkExited();
	
	/**
	 * Notification that this link has been activated. Performs
	 * context specific linking.
	 */
	public void linkActivated();
}
