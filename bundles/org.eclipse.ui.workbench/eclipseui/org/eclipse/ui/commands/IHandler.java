/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.commands;

import java.util.Map;

/**
 * A handler is the pluggable piece of a command that handles execution. Each
 * command can have zero or more handlers associated with it (in general), of
 * which only one will be active at any given moment in time. When the command
 * is asked to execute, it will simply pass that request on to its active
 * handler, if any.
 * <p>
 * This interface is not intended to be extended by clients.
 * </p>
 *
 * @since 3.0
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead. This
 *             API is scheduled for deletion, see Bug 431177 for details
 * @see org.eclipse.core.commands.IHandler
 * @noreference This interface is scheduled for deletion.
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
@Deprecated
@SuppressWarnings("all")
public interface IHandler {

	/**
	 * Registers an instance of <code>IHandlerListener</code> to listen for changes
	 * to properties of this instance.
	 *
	 * @param handlerListener the instance to register. Must not be
	 *                        <code>null</code>. If an attempt is made to register
	 *                        an instance which is already registered with this
	 *                        instance, no operation is performed.
	 */
	@Deprecated
	void addHandlerListener(IHandlerListener handlerListener);

	/**
	 * Disposes of this handler. This method is run once when the object is no
	 * longer referenced. This can be used as an opportunity to unhook listeners
	 * from other objects.
	 */
	@Deprecated
	void dispose();

	/**
	 * Executes with the map of parameter values by name.
	 *
	 * @param parameterValuesByName the map of parameter values by name. Reserved
	 *                              for future use, must be <code>null</code>.
	 * @return the result of the execution. Reserved for future use, must be
	 *         <code>null</code>.
	 * @throws ExecutionException if an exception occurred during execution.
	 */
	@Deprecated
	Object execute(Map parameterValuesByName) throws ExecutionException;

	/**
	 * Returns the map of attribute values by name.
	 * <p>
	 * Notification is sent to all registered listeners if this property changes.
	 * </p>
	 *
	 * @return the map of attribute values by name. This map may be empty, but is
	 *         guaranteed not to be <code>null</code>. If this map is not empty, its
	 *         collection of keys is guaranteed to only contain instances of
	 *         <code>String</code>.
	 */
	@Deprecated
	Map getAttributeValuesByName();

	/**
	 * Unregisters an instance of <code>IPropertyListener</code> listening for
	 * changes to properties of this instance.
	 *
	 * @param handlerListener the instance to unregister. Must not be
	 *                        <code>null</code>. If an attempt is made to unregister
	 *                        an instance which is not already registered with this
	 *                        instance, no operation is performed.
	 */
	@Deprecated
	void removeHandlerListener(IHandlerListener handlerListener);
}
