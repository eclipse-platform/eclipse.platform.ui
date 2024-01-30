/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Niklas Kellner  - Named Handler API
 *******************************************************************************/
package org.eclipse.core.commands;

/**
 * A handler is the pluggable piece of a command that handles execution. Each
 * command can have zero or more handlers associated with it (in general), of
 * which only one will be active at any given moment in time. When the command
 * is asked to execute, it will simply pass that request on to its active
 * handler, if any.
 *
 * @see AbstractHandler
 * @since 3.1
 */
public interface IHandler {

	/**
	 * Registers an instance of <code>IHandlerListener</code> to listen for
	 * changes to properties of this instance.
	 *
	 * @param handlerListener
	 *            the instance to register. Must not be <code>null</code>. If
	 *            an attempt is made to register an instance which is already
	 *            registered with this instance, no operation is performed.
	 */
	void addHandlerListener(IHandlerListener handlerListener);

	/**
	 * Disposes of this handler. This method is run once when the object is no
	 * longer referenced. This can be used as an opportunity to unhook listeners
	 * from other objects.
	 */
	public void dispose();

	/**
	 * Executes with the map of parameter values by name.
	 *
	 * @param event
	 *            An event containing all the information about the current
	 *            state of the application; must not be <code>null</code>.
	 * @return the result of the execution. Reserved for future use, must be
	 *         <code>null</code>.
	 * @throws ExecutionException
	 *             if an exception occurred during execution.
	 */
	Object execute(ExecutionEvent event) throws ExecutionException;

	/**
	 * Returns whether this handler is capable of executing at this moment in
	 * time. If the enabled state is other than true clients should also
	 * consider implementing IHandler2 so they can be notified about framework
	 * execution contexts.
	 *
	 * @return <code>true</code> if the command is enabled; <code>false</code>
	 *         otherwise.
	 * @see IHandler2#setEnabled(Object)
	 */
	public boolean isEnabled();

	/**
	 * Returns whether this handler is really capable of handling delegation. In
	 * the case of a handler that is a composition of other handlers, this reply
	 * is intended to indicate whether the handler is truly capable of receiving
	 * delegated responsibilities at this time.
	 *
	 * @return <code>true</code> if the handler is handled; <code>false</code>
	 *         otherwise.
	 */
	public boolean isHandled();

	/**
	 * Unregisters an instance of <code>IHandlerListener</code> listening for
	 * changes to properties of this instance.
	 *
	 * @param handlerListener
	 *            the instance to unregister. Must not be <code>null</code>.
	 *            If an attempt is made to unregister an instance which is not
	 *            already registered with this instance, no operation is
	 *            performed.
	 */
	void removeHandlerListener(IHandlerListener handlerListener);

	/**
	 * Return the label for this handler. The handler can implement this method to
	 * provide a more dynamic label according to the actual action that is
	 * performed. When returning null, callers may instead use the invoking command
	 * label or a generic label.
	 *
	 * @return name of the Handler, can be null
	 *
	 * @since 3.11
	 */
	default String getHandlerLabel() {
		return null;
	}
}
