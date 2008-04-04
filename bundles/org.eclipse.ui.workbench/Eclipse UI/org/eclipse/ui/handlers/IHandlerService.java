/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.handlers;

import java.util.Collection;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.services.IServiceWithSources;

/**
 * <p>
 * Provides services related to activating and deactivating handlers within the
 * workbench.
 * </p>
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * 
 * @since 3.1
 */
public interface IHandlerService extends IServiceWithSources {

	/**
	 * <p>
	 * Activates the given handler from a child service. This is used by slave
	 * and nested services to promote handler activations up to the root. By
	 * using this method, it is possible for handlers coming from a more nested
	 * component to override the nested component.
	 * </p>
	 * 
	 * @param activation
	 *            The activation that is local to the child service; must not be
	 *            <code>null</code>.
	 * @return A token which can be used to later cancel the activation. Only
	 *         someone with access to this token can cancel the activation. The
	 *         activation will automatically be cancelled if the context from
	 *         which this service was retrieved is destroyed. This activation is
	 *         local to this service (i.e., it is not the activation that is
	 *         passed as a parameter).
	 * @since 3.2
	 */
	public IHandlerActivation activateHandler(IHandlerActivation activation);

	/**
	 * <p>
	 * Activates the given handler within the context of this service. If this
	 * service was retrieved from the workbench, then this handler will be
	 * active globally. If the service was retrieved from a nested component,
	 * then the handler will only be active within that component.
	 * </p>
	 * <p>
	 * Also, it is guaranteed that the handlers submitted through a particular
	 * service will be cleaned up when that services is destroyed. So, for
	 * example, a service retrieved from a <code>IWorkbenchPartSite</code>
	 * would deactivate all of its handlers when the site is destroyed.
	 * </p>
	 * 
	 * @param commandId
	 *            The identifier for the command which this handler handles;
	 *            must not be <code>null</code>.
	 * @param handler
	 *            The handler to activate; must not be <code>null</code>.
	 * @return A token which can be used to later cancel the activation. Only
	 *         someone with access to this token can cancel the activation. The
	 *         activation will automatically be cancelled if the context from
	 *         which this service was retrieved is destroyed.
	 */
	public IHandlerActivation activateHandler(String commandId, IHandler handler);

	/**
	 * <p>
	 * Activates the given handler within the context of this service. The
	 * handler becomes active when <code>expression</code> evaluates to
	 * <code>true</code>. This is the same as calling
	 * {@link #activateHandler(String, IHandler, Expression, boolean)} with
	 * global==false.
	 * </p>
	 * <p>
	 * Also, it is guaranteed that the handlers submitted through a particular
	 * service will be cleaned up when that services is destroyed. So, for
	 * example, a service retrieved from a <code>IWorkbenchPartSite</code>
	 * would deactivate all of its handlers when the site is destroyed.
	 * </p>
	 * 
	 * @param commandId
	 *            The identifier for the command which this handler handles;
	 *            must not be <code>null</code>.
	 * @param handler
	 *            The handler to activate; must not be <code>null</code>.
	 * @param expression
	 *            This expression must evaluate to <code>true</code> before
	 *            this handler will really become active. The expression may be
	 *            <code>null</code> if the handler should always be active.
	 * @return A token which can be used to later cancel the activation. Only
	 *         someone with access to this token can cancel the activation. The
	 *         activation will automatically be cancelled if the context from
	 *         which this service was retrieved is destroyed.
	 * 
	 * @see org.eclipse.ui.ISources
	 * @since 3.2
	 */
	public IHandlerActivation activateHandler(String commandId,
			IHandler handler, Expression expression);

	/**
	 * <p>
	 * Activates the given handler within the context of this service. The
	 * handler becomes active when <code>expression</code> evaluates to
	 * <code>true</code>. if global==<code>false</code>, then this
	 * handler service must also be the active service to active the handler.
	 * For example, the handler service on a part is active when that part is
	 * active.
	 * </p>
	 * <p>
	 * Also, it is guaranteed that the handlers submitted through a particular
	 * service will be cleaned up when that services is destroyed. So, for
	 * example, a service retrieved from a <code>IWorkbenchPartSite</code>
	 * would deactivate all of its handlers when the site is destroyed.
	 * </p>
	 * 
	 * @param commandId
	 *            The identifier for the command which this handler handles;
	 *            must not be <code>null</code>.
	 * @param handler
	 *            The handler to activate; must not be <code>null</code>.
	 * @param expression
	 *            This expression must evaluate to <code>true</code> before
	 *            this handler will really become active. The expression may be
	 *            <code>null</code> if the handler should always be active.
	 * @param global
	 *            Indicates that the handler should be activated irrespectively
	 *            of whether the corresponding workbench component (e.g.,
	 *            window, part, etc.) is active.
	 * @return A token which can be used to later cancel the activation. Only
	 *         someone with access to this token can cancel the activation. The
	 *         activation will automatically be cancelled if the context from
	 *         which this service was retrieved is destroyed.
	 * 
	 * @see org.eclipse.ui.ISources
	 * @since 3.2
	 */
	public IHandlerActivation activateHandler(String commandId,
			IHandler handler, Expression expression, boolean global);

	/**
	 * <p>
	 * Activates the given handler within the context of this service. The
	 * handler becomes active when <code>expression</code> evaluates to
	 * <code>true</code>.
	 * </p>
	 * <p>
	 * Also, it is guaranteed that the handlers submitted through a particular
	 * service will be cleaned up when that services is destroyed. So, for
	 * example, a service retrieved from a <code>IWorkbenchPartSite</code>
	 * would deactivate all of its handlers when the site is destroyed.
	 * </p>
	 * 
	 * @param commandId
	 *            The identifier for the command which this handler handles;
	 *            must not be <code>null</code>.
	 * @param handler
	 *            The handler to activate; must not be <code>null</code>.
	 * @param expression
	 *            This expression must evaluate to <code>true</code> before
	 *            this handler will really become active. The expression may be
	 *            <code>null</code> if the handler should always be active.
	 * @param sourcePriorities
	 *            The source priorities for the expression.
	 * @return A token which can be used to later cancel the activation. Only
	 *         someone with access to this token can cancel the activation. The
	 *         activation will automatically be cancelled if the context from
	 *         which this service was retrieved is destroyed.
	 * 
	 * @see org.eclipse.ui.ISources
	 * @deprecated Use
	 *             {@link IHandlerService#activateHandler(String, IHandler, Expression)}
	 *             instead.
	 */
	public IHandlerActivation activateHandler(String commandId,
			IHandler handler, Expression expression, int sourcePriorities);

	/**
	 * Creates an execution event based on an SWT event. This execution event
	 * can then be passed to a command for execution.
	 * 
	 * @param command
	 *            The command for which an execution event should be created;
	 *            must not be <code>null</code>.
	 * @param event
	 *            The SWT event triggering the command execution; may be
	 *            <code>null</code>.
	 * @return An execution event suitable for calling
	 *         {@link Command#executeWithChecks(ExecutionEvent)}.
	 * @since 3.2
	 * @see Command#executeWithChecks(ExecutionEvent)
	 */
	public ExecutionEvent createExecutionEvent(Command command, Event event);

	/**
	 * Creates a parameterized execution event based on an SWT event and a
	 * parameterized command. This execution event can then be passed to a
	 * command for execution.
	 * 
	 * @param command
	 *            The parameterized command for which an execution event should
	 *            be created; must not be <code>null</code>.
	 * @param event
	 *            The SWT event triggering the command execution; may be
	 *            <code>null</code>.
	 * @return An execution event suitable for calling
	 *         {@link Command#executeWithChecks(ExecutionEvent)}.
	 * @since 3.2
	 * @see ParameterizedCommand#getCommand()
	 * @see Command#executeWithChecks(ExecutionEvent)
	 */
	public ExecutionEvent createExecutionEvent(ParameterizedCommand command,
			Event event);

	/**
	 * Deactivates the given handler within the context of this service. If the
	 * handler was activated with a different service, then it must be
	 * deactivated from that service instead. It is only possible to retract a
	 * handler activation with this method. That is, you must have the same
	 * <code>IHandlerActivation</code> used to activate the handler.
	 * 
	 * @param activation
	 *            The token that was returned from a call to
	 *            <code>activateHandler</code>; must not be <code>null</code>.
	 */
	public void deactivateHandler(IHandlerActivation activation);

	/**
	 * Deactivates the given handlers within the context of this service. If the
	 * handler was activated with a different service, then it must be
	 * deactivated from that service instead. It is only possible to retract a
	 * handler activation with this method. That is, you must have the same
	 * <code>IHandlerActivation</code> used to activate the handler.
	 * 
	 * @param activations
	 *            The tokens that were returned from a call to
	 *            <code>activateHandler</code>. This collection must only
	 *            contain instances of <code>IHandlerActivation</code>. The
	 *            collection must not be <code>null</code>.
	 */
	public void deactivateHandlers(Collection activations);

	/**
	 * Executes the command with the given identifier and no parameters.
	 * 
	 * @param commandId
	 *            The identifier of the command to execute; must not be
	 *            <code>null</code>.
	 * @param event
	 *            The SWT event triggering the command execution; may be
	 *            <code>null</code>.
	 * @return The return value from the execution; may be <code>null</code>.
	 * @throws ExecutionException
	 *             If the handler has problems executing this command.
	 * @throws NotDefinedException
	 *             If the command you are trying to execute is not defined.
	 * @throws NotEnabledException
	 *             If the command you are trying to execute is not enabled.
	 * @throws NotHandledException
	 *             If there is no handler.
	 * @since 3.2
	 * @see Command#executeWithChecks(ExecutionEvent)
	 */
	public Object executeCommand(String commandId, Event event)
			throws ExecutionException, NotDefinedException,
			NotEnabledException, NotHandledException;

	/**
	 * Executes the given parameterized command.
	 * 
	 * @param command
	 *            The parameterized command to be executed; must not be
	 *            <code>null</code>.
	 * @param event
	 *            The SWT event triggering the command execution; may be
	 *            <code>null</code>.
	 * @return The return value from the execution; may be <code>null</code>.
	 * @throws ExecutionException
	 *             If the handler has problems executing this command.
	 * @throws NotDefinedException
	 *             If the command you are trying to execute is not defined.
	 * @throws NotEnabledException
	 *             If the command you are trying to execute is not enabled.
	 * @throws NotHandledException
	 *             If there is no handler.
	 * @since 3.2
	 * @see Command#executeWithChecks(ExecutionEvent)
	 */
	public Object executeCommand(ParameterizedCommand command, Event event)
			throws ExecutionException, NotDefinedException,
			NotEnabledException, NotHandledException;

	/**
	 * Executes the given parameterized command in the provided context. It
	 * takes care of finding the correct active handler given the context, calls
	 * {@link IHandler2#setEnabled(Object)} to update the enabled state if
	 * supported, and executes with that handler.
	 * 
	 * @param command
	 *            The parameterized command to be executed; must not be
	 *            <code>null</code>.
	 * @param event
	 *            The SWT event triggering the command execution; may be
	 *            <code>null</code>.
	 * @param context
	 *            the evaluation context to run against. Must not be
	 *            <code>null</code>
	 * @return The return value from the execution; may be <code>null</code>.
	 * @throws ExecutionException
	 *             If the handler has problems executing this command.
	 * @throws NotDefinedException
	 *             If the command you are trying to execute is not defined.
	 * @throws NotEnabledException
	 *             If the command you are trying to execute is not enabled.
	 * @throws NotHandledException
	 *             If there is no handler.
	 * @since 3.4
	 * @see Command#executeWithChecks(ExecutionEvent)
	 * @see #createContextSnapshot(boolean)
	 */
	public Object executeCommandInContext(ParameterizedCommand command,
			Event event, IEvaluationContext context) throws ExecutionException,
			NotDefinedException, NotEnabledException, NotHandledException;

	/**
	 * This method creates a copy of the application context returned by
	 * {@link #getCurrentState()}.
	 * 
	 * @param includeSelection
	 *            if <code>true</code>, include the default variable and
	 *            selection variables
	 * @return an context filled with the current set of variables. If selection
	 *         is not included, the default variable is an empty collection
	 * @since 3.4
	 */
	public IEvaluationContext createContextSnapshot(boolean includeSelection);

	/**
	 * Returns an evaluation context representing the current state of the
	 * world. This is equivalent to the application context required by
	 * {@link ExecutionEvent}.
	 * 
	 * @return the current state of the application; never <code>null</code>.
	 * @see ParameterizedCommand#executeWithChecks(Object, Object)
	 * @see ExecutionEvent#ExecutionEvent(Command, java.util.Map, Object,
	 *      Object)
	 */
	public IEvaluationContext getCurrentState();

	/**
	 * <p>
	 * Reads the handler information from the registry. This will overwrite any
	 * of the existing information in the handler service. This method is
	 * intended to be called during start-up. When this method completes, this
	 * handler service will reflect the current state of the registry.
	 * </p>
	 */
	public void readRegistry();

	/**
	 * Sets the help context identifier to associate with a particular handler.
	 * 
	 * @param handler
	 *            The handler with which to register a help context identifier;
	 *            must not be <code>null</code>.
	 * @param helpContextId
	 *            The help context identifier to register; may be
	 *            <code>null</code> if the help context identifier should be
	 *            removed.
	 * @since 3.2
	 */
	public void setHelpContextId(IHandler handler, String helpContextId);
}
