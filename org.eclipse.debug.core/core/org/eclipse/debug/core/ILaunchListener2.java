/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core;

/**
 * Enhancements to the launch listener interface. Provides notification
 * when a launch terminates.
 * <p>
 * Clients may implementing launch listeners may implement
 * this interface.
 * </p>
 * @since 3.0
 */
public interface ILaunchListener2 extends ILaunchListener {
	
	/**
	 * Notification that the given launch has terminated.
	 * 
	 * @param launch the launch that has terminated
	 */
	public void launchTerminated(ILaunch launch);
}
