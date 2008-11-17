/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.model;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.IBreakpointManager;

/**
 * This interface defines a breakpoint import participant.
 * 
 * Participants are used during a breakpoint import operation
 * to specify how breakpoints of the associated marker type should be compared
 * and how the breakpoint should be validated once it is decided it will be imported.
 * <p>
 * Clients may implement this interface.
 * </p>
 * 
 * @see IBreakpointManager
 * @since 3.5
 */
public interface IBreakpointImportParticipant {
	
	/**
	 * Determines if the given attributes match the given breakpoint.
	 * 
	 * @param attributes the map of raw breakpoint attributes read from the import memento
	 * @param breakpoint the current breakpoint context in the import operation
	 * @return true if the breakpoint matches the given attributes, false otherwise
	 * @throws CoreException if an exception occurs
	 */
	public boolean matches(Map attributes, IBreakpoint breakpoint) throws CoreException;
	
	/**
	 * Verifies the state of the breakpoint once it has been imported. This method can be used to correct
	 * attributes of the imported breakpoint once it has been imported.
	 * 
	 * For example: updating line number information or character ranges to ensure the marker appears correctly
	 * 
	 * @param breakpoint the breakpoint to be verified
	 * @throws CoreException if an exception occurs
	 */
	public void verify(IBreakpoint breakpoint) throws CoreException;

}
