package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A launch listener is notified of launches as they
 * are added and removed from the launch manager. Also,
 * when a process or debug target is added to a launch,
 * listeners are notified of a change.
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see ILaunch
 */
public interface ILaunchListener {	
	/**
	 * Notifies this listener that the specified
	 * launch has been removed.
	 *
	 * @param launch the removed launch
	 * @since 2.0
	 */
	public void launchRemoved(ILaunch launch);
	/**
	 * Notifies this listener that the specified launch
	 * has been added.
	 * 
	 * @param launch the newly added launch
	 * @since 2.0
	 */
	public void launchAdded(ILaunch launch);	
	/**
	 * Notifies this listener that the specified launch
	 * has changed. For example, a process or debug target
	 * has been added to the launch.
	 * 
	 * @param launch the changed launch
	 * @since 2.0
	 */
	public void launchChanged(ILaunch launch);	
}
