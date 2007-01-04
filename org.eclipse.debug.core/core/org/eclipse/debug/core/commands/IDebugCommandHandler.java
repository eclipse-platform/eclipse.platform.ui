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
package org.eclipse.debug.core.commands;



/**
 * Handles a command for a debugger. Specific command handlers extend this interface.
 * <p>
 * The debug platform provides actions for common debug commands that operate against
 * these handler interfaces. For example, the platform provides a terminate action that
 * operates on the active debug context (selected element in the debug view). The action
 * delegates to the active context's {@link ITerminateHandler} implementation to update
 * its enabled state and execute the command. Debug model elements may implement supported
 * command handler interfaces directly or provide them as adapters. The debug platform
 * provides implementations of handlers for standard debug models. 
 * </p>
 * <p>
 * Clients are not intended to implement this interface directly. Clients may 
 * implement specific command handler interfaces that extend this interface.
 * </p>
 * @see org.eclipse.core.runtime.IAdaptable
 * @see IDisconnectHandler
 * @see IDropToFrameHandler
 * @see IResumeHandler
 * @see IStepFiltersHandler
 * @see IStepIntoHandler
 * @see IStepOverHandler
 * @see IStepReturnHandler
 * @see ISuspendHandler
 * @see ITerminateHandler
 * @since 3.3
 * <p>
 * <strong>EXPERIMENTAL</strong>. This interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * remain unchanged during the 3.3 release cycle. Please do not use this API
 * without consulting with the Platform/Debug team.
 * </p>
 */
public interface IDebugCommandHandler {
	
	/**
	 * Determines whether this handler can execute on the elements specified
	 * in the given request by reporting enabled state to the request.
	 * <p>
	 * Implementations must be non-blocking and may respond asynchronously to the
	 * given request. Errors can reported by setting an appropriate status
	 * on the given request. A request can be canceled by this handler or caller.
	 * A <code>null</code> status is equivalent to an OK status.
	 * When a request succeeds, fails, or is canceled, implementations must call
	 * <code>done()</code> on the given request.
	 * </p>
	 * <p>
	 * Clients are expected to poll the request (using <code>isCanceled</code>)
	 * periodically and abort at their earliest convenience calling <code>done()</code>
	 * on the request.
	 * </p>
	 * @param elements elements for which this handler may be enabled
	 * @param request collects status and enabled state
	 */
	public void canExecute(IEnabledStateRequest request);
	
	/**
	 * Executes this command on the elements specified in the given request
	 * reporting status to the given request and returns whether this handler should
	 * remain enabled while the command is executing.
	 * <p>
	 * Implementations must be non-blocking and may respond asynchronously to the
	 * given request. Errors can reported by setting an appropriate status
	 * on the given request. A request can be canceled by this handler or the caller. 
	 * A <code>null</code> status is equivalent to an OK status. When a request is
	 * complete, has encountered an error, or cancelled, implementations must call
	 * <code>done()</code> on the given collector.
	 * </p>
	 * <p>
	 * Handlers are expected to poll the request (using <code>isCanceled</code>)
	 * periodically and abort at their earliest convenience calling <code>done()</code>
	 * on the request.
	 * </p>
	 * @param elements elements to execute command on
	 * @param request collects the status of the executing the command
	 * @return whether this handler remains enabled while command is executing
	 */
	public boolean execute(IDebugCommandRequest request);

}
