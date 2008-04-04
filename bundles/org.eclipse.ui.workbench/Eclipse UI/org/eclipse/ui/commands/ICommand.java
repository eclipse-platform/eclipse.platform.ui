/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.commands;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * An instance of <code>ICommand</code> is a handle representing a command as
 * defined by the extension point <code>org.eclipse.ui.commands</code>. The
 * identifier of the handle is identifier of the command being represented.
 * </p>
 * <p>
 * An instance of <code>ICommand</code> can be obtained from an instance of
 * <code>ICommandManager</code> for any identifier, whether or not a command
 * with that identifier defined in the plugin registry.
 * </p>
 * <p>
 * The handle-based nature of this API allows it to work well with runtime
 * plugin activation and deactivation. If a command is defined, that means that
 * its corresponding plug-in is active. If the plug-in is then deactivated, the
 * command will still exist but it will be undefined. An attempts to use an
 * undefined command will result in a <code>NotDefinedException</code> being
 * thrown.
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @see ICommandListener
 * @see ICommandManager
 * @see org.eclipse.core.commands.Command
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICommand extends Comparable {

    /**
     * Registers an instance of <code>ICommandListener</code> to listen for
     * changes to attributes of this instance.
     * 
     * @param commandListener
     *            the instance of <code>ICommandListener</code> to register.
     *            Must not be <code>null</code>. If an attempt is made to
     *            register an instance of <code>ICommandListener</code> which
     *            is already registered with this instance, no operation is
     *            performed.
     */
    void addCommandListener(ICommandListener commandListener);

    /**
     * Executes with the map of parameter values by name.
     * 
     * @param parameterValuesByName
     *            the map of parameter values by name. Reserved for future use,
     *            must be <code>null</code>.
     * @return the result of the execution. Reserved for future use, must be
     *         <code>null</code>.
     * @throws ExecutionException
     *             if an exception occurred during execution.
     * @throws NotHandledException
     *             if this is not handled.
     */
    Object execute(Map parameterValuesByName) throws ExecutionException,
            NotHandledException;

    /**
     * Returns the map of attribute values by name.
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return the map of attribute values by name. This map may be empty, but
     *         is guaranteed not to be <code>null</code>. If this map is not
     *         empty, its collection of keys is guaranteed to only contain
     *         instances of <code>String</code>.
     * @throws NotHandledException
     *             if this is not handled.
     */
    Map getAttributeValuesByName() throws NotHandledException;

    /**
     * <p>
     * Returns the identifier of the category of the command represented by this
     * handle.
     * </p>
     * <p>
     * Notification is sent to all registered listeners if this attribute
     * changes.
     * </p>
     * 
     * @return the identifier of the category of the command represented by this
     *         handle. May be <code>null</code>.
     * @throws NotDefinedException
     *             if the command represented by this handle is not defined.
     */
    String getCategoryId() throws NotDefinedException;

    /**
     * <p>
     * Returns the description of the command represented by this handle,
     * suitable for display to the user.
     * </p>
     * <p>
     * Notification is sent to all registered listeners if this attribute
     * changes.
     * </p>
     * 
     * @return the description of the command represented by this handle.
     *         Guaranteed not to be <code>null</code>.
     * @throws NotDefinedException
     *             if the command represented by this handle is not defined.
     */
    String getDescription() throws NotDefinedException;

    /**
     * Returns the identifier of this handle.
     * 
     * @return the identifier of this handle. Guaranteed not to be
     *         <code>null</code>.
     */
    String getId();

    /**
     * <p>
     * Returns the list of key sequence bindings for this handle. This method
     * will return all key sequence bindings for this handle's identifier,
     * whether or not the command represented by this handle is defined.
     * </p>
     * <p>
     * Notification is sent to all registered listeners if this attribute
     * changes.
     * </p>
     * 
     * @return the list of key sequence bindings. This list may be empty, but is
     *         guaranteed not to be <code>null</code>. If this list is not
     *         empty, it is guaranteed to only contain instances of
     *         <code>IKeySequenceBinding</code>.
     */
    List getKeySequenceBindings();

    /**
     * <p>
     * Returns the name of the command represented by this handle, suitable for
     * display to the user.
     * </p>
     * <p>
     * Notification is sent to all registered listeners if this attribute
     * changes.
     * </p>
     * 
     * @return the name of the command represented by this handle. Guaranteed
     *         not to be <code>null</code>.
     * @throws NotDefinedException
     *             if the command represented by this handle is not defined.
     */
    String getName() throws NotDefinedException;

    /**
     * <p>
     * Returns whether or not the command represented by this handle is defined.
     * </p>
     * <p>
     * Notification is sent to all registered listeners if this attribute
     * changes.
     * </p>
     * 
     * @return <code>true</code>, iff the command represented by this handle
     *         is defined.
     */
    boolean isDefined();

    /**
     * <p>
     * Returns whether or not this command is handled. A command is handled if
     * it currently has an <code>IHandler</code> instance associated with it.
     * A command needs a handler to carry out the {@link ICommand#execute(Map)}
     * method.
     * </p>
     * <p>
     * Notification is sent to all registered listeners if this attribute
     * changes.
     * </p>
     * 
     * @return <code>true</code>, iff this command is enabled.
     */
    boolean isHandled();

    /**
     * Unregisters an instance of <code>ICommandListener</code> listening for
     * changes to attributes of this instance.
     * 
     * @param commandListener
     *            the instance of <code>ICommandListener</code> to unregister.
     *            Must not be <code>null</code>. If an attempt is made to
     *            unregister an instance of <code>ICommandListener</code>
     *            which is not already registered with this instance, no
     *            operation is performed.
     */
    void removeCommandListener(ICommandListener commandListener);
}
