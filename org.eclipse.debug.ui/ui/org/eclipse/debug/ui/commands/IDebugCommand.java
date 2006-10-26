/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.commands;


/**
 * A debug command represents a debugger function that can be enabled or disabled
 * and executed. Specific commands extend this interface.
 * <p>
 * The debug platform defines interfaces for common debug commands and provides
 * actions that operate against these interfaces. For example, the platform
 * provides a terminate action that operates on the active debug context. The
 * action delegates to the active context's <code>ITerminateCommand</code>
 * implementation to update its enabled state and execute the command. Debug
 * model elements may implement supported command interfaces directly or
 * provide them as adapters.
 * </p>
 * <p>
 * Clients are not intended to implement this interface directly. Clients may 
 * implement specific command interfaces that extend this interface.
 * </p>
 * @see org.eclipse.core.runtime.IAdaptable
 * @see IDisconnectCommand
 * @see IDropToFrameCommand
 * @see IResumeCommand
 * @see IStepFiltersCommand
 * @see IStepIntoCommand
 * @see IStepOverCommand
 * @see IStepReturnCommand
 * @see ISuspendCommand
 * @see ITerminateCommand
 * @since 3.3
 * <p>
 * <strong>EXPERIMENTAL</strong>. This interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * remain unchanged during the 3.3 release cycle. Please do not use this API
 * without consulting with the Platform/Debug team.
 * </p>
 */
public interface IDebugCommand {
	
	/**
	 * Determines whether this command may be executed on the specified element
	 * by reporting enabled state to the given monitor.
	 * <p>
	 * Implementations must be non-blocking and may respond asynchronously to the
	 * given monitor. Errors can reported by setting an appropriate status
	 * on the given monitor or cancelled by calling <code>setCancelled(boolean)</code>.
	 * A <code>null</code> status is equivalent to an OK status. When a request is
	 * complete, has encountered an error, or cancelled, implementations must call
	 * <code>done()</code> on the given monitor.
	 * </p>
	 * @param element element for which this command may be enabled
	 * @param monitor accepts enabled state
	 */
	public void canExecute(Object element, IBooleanStatusMonitor monitor);
	
	/**
	 * Executes this command on the specified element reporting status
	 * to the given monitor and returns whether this command should
	 * remain enabled while the command is executing.
	 * <p>
	 * Implementations must be non-blocking and may respond asynchronously to the
	 * given monitor. Errors can reported by setting an appropriate status
	 * on the given monitor or cancelled by calling <code>setCancelled(boolean)</code>.
	 * A <code>null</code> status is equivalent to an OK status. When a request is
	 * complete, has encountered an error, or cancelled, implementations must call
	 * <code>done()</code> on the given monitor.
	 * </p>
	 * @param element element to execute command on
	 * @param monitor status monitor
	 * @return whether the command remains enabled while command is executing
	 */
	public boolean execute(Object element, IStatusMonitor monitor);

}
