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
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ISuspendResume;

/**
 * An implementation of the debug platform 'run to line' action. The debug
 * platform provides a 'run to line' action that debuggers may implement
 * by providing an adapter (see <code>IAdaptable</code>) of this type.
 * When a part is activated, the 'run to line' action asks the active part
 * for its 'run to line' adapter. If one exists, that adapter is delegated
 * to to perform a 'run to line' operation when the user invokes the action.
 * This allows the platform to provide one command and keybinding for the
 * 'run to line' function to be shared by many debuggers. 
 * <p>
 * The 'run to line' action is enabled when an instance of
 * <code>ISuspendResume</code> is selected in the Debug view, and the active
 * part provides a 'run to line' adapter. 
 * </p>
 * <p>
 * EXPERIMENTAL
 * </p>
 * <p>
 * Clients are intended to implement this interface and provide instances as
 * an adapter on applicable parts (for example, editors) that support the
 * operation.
 * </p>
 * @since 3.0
 */
public interface IRunToLineTarget {
	
	/**
	 * Perform a run to line action on the given element that is 
	 * currently selected in the Debug view. Client implementations
	 * determine the line to run to based on their internal state.
	 * 
	 * @param target suspended element to perform the 'run to line' action on
	 * @throws CoreException if unable to perform the action 
	 */
	public void runToLine(ISuspendResume target) throws CoreException;
	
	/**
	 * Disposes this target performing any required cleanup. When a part
	 * with a 'run to line' adapter is deactivated, its 'run to line' target
	 * is disposed.
	 */
	public void dispose();
	
}
