/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.model;

 
import org.eclipse.core.runtime.CoreException;

/**
 * A breakpoint that suspends when an associated variable is
 * read or written.
 * <p>
 * Clients may implement this interface. Clients are not required to 
 * implement this interface to implement watchpoints, but those that do inherit
 * default rendering of images for watchpoints from the debug platform's
 * default label provider and actions to toggle access and modification
 * properties of a watchpoint.
 * </p>
 * @since 3.1
 */
public interface IWatchpoint extends IBreakpoint {
	/**
	 * Returns whether this watchpoint will suspend execution when its associated
	 * variable is accessed (read).
	 * 
	 * @return whether this is an access watchpoint
	 * @exception CoreException if unable to access the property
	 * 	on this breakpoint's underlying marker
	 */	
	public boolean isAccess() throws CoreException;
	/**
	 * Sets whether this breakpoint will suspend execution when its associated
	 * variable is accessed.
	 * 
	 * @param access whether to suspend on access
	 * @exception CoreException if unable to set the property on this breakpoint's
	 *  underlying marker or if the capability is not supported
	 */
	public void setAccess(boolean access) throws CoreException;
	/**
	 * Returns whether this watchpoint will suspend execution when its associated
	 * variable is written.
	 * 
	 * @return whether this is a modification watchpoint
	 * @exception CoreException if unable to access the property
	 * 	on this breakpoint's underlying marker
	 */	
	public boolean isModification() throws CoreException;
	/**
	 * Sets whether this breakpoint will suspend execution when its associated
	 * variable is modified.
	 * 
	 * @param modification whether to suspend on modification
	 * @exception CoreException if unable to set the property on
	 * 	this breakpoint's underlying marker or if the capability is not supported
	 */
	public void setModification(boolean modification) throws CoreException;
	/**
	 * Returns whether this breakpoints supports the capability to suspend
	 * when an associated variable is read.
	 * 
	 * @return whether this breakpoints supports the capability to suspend
	 * when an associated variable is read
	 */
	public boolean supportsAccess();
	/**
	 * Returns whether this breakpoints supports the ability to suspend
	 * when an associated variable is written.
	 * 
	 * @return whether this breakpoints supports the ability to suspend
	 * when an associated variable is written
	 */	
	public boolean supportsModification();

}

