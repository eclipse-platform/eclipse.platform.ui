package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A launch listener is notified of launches as they
 * are registered and deregistered with the launch manager.
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
	 * Notifies this listener that the specified launch has been deregistered.
	 *
	 * @param launch the deregistered launch
	 */
	public void launchDeregistered(ILaunch launch);
	/**
	 * Notifies this listener that the specified launch has been registered.
	 * 
	 * @param launch the registered launch
	 */
	public void launchRegistered(ILaunch launch);
}
