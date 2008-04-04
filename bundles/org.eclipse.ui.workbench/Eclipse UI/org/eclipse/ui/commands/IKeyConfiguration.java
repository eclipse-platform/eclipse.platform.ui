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

/**
 * <p>
 * An instance of <code>IKeyConfiguration</code> is a handle representing a
 * key configuration as defined by the extension point
 * <code>org.eclipse.ui.commands</code>. The identifier of the handle is
 * identifier of the key configuration being represented.
 * </p>
 * <p>
 * An instance of <code>IKeyConfiguration</code> can be obtained from an
 * instance of <code>ICommandManager</code> for any identifier, whether or not
 * a key configuration with that identifier defined in the plugin registry.
 * </p>
 * <p>
 * The handle-based nature of this API allows it to work well with runtime
 * plugin activation and deactivation. If a key configuration is defined, that
 * means that its corresponding plug-in is active. If the plug-in is then
 * deactivated, the configuration will still exist but it will be undefined.
 * An attempt to use an undefined key configuration will result in a
 * <code>NotDefinedException</code> being thrown.
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @see IKeyConfigurationListener
 * @see ICommandManager
 * @see org.eclipse.jface.bindings.Scheme
 * @deprecated Please use the bindings support in the "org.eclipse.jface"
 * plug-in instead.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IKeyConfiguration extends Comparable {

    /**
     * Registers an instance of <code>IKeyConfigurationListener</code> to
     * listen for changes to attributes of this instance.
     * 
     * @param keyConfigurationListener
     *            the instance of <code>IKeyConfigurationListener</code> to
     *            register. Must not be <code>null</code>. If an attempt is
     *            made to register an instance of <code>IKeyConfigurationListener</code>
     *            which is already registered with this instance, no operation
     *            is performed.
     */
    void addKeyConfigurationListener(
            IKeyConfigurationListener keyConfigurationListener);

    /**
     * <p>
     * Returns the description of the key configuration represented by this
     * handle, suitable for display to the user.
     * </p>
     * <p>
     * Notification is sent to all registered listeners if this attribute
     * changes.
     * </p>
     * 
     * @return the description of the key configuration represented by this
     *         handle. Guaranteed not to be <code>null</code>.
     * @throws NotDefinedException
     *             if the key configuration represented by this handle is not
     *             defined.
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
     * Returns the name of the key configuration represented by this handle,
     * suitable for display to the user.
     * </p>
     * <p>
     * Notification is sent to all registered listeners if this attribute
     * changes.
     * </p>
     * 
     * @return the name of the key configuration represented by this handle.
     *         Guaranteed not to be <code>null</code>.
     * @throws NotDefinedException
     *             if the key configuration represented by this handle is not
     *             defined.
     */
    String getName() throws NotDefinedException;

    /**
     * <p>
     * Returns the identifier of the parent of the key configuration
     * represented by this handle.
     * </p>
     * <p>
     * Notification is sent to all registered listeners if this attribute
     * changes.
     * </p>
     * 
     * @return the identifier of the parent of the key configuration
     *         represented by this handle. May be <code>null</code>.
     * @throws NotDefinedException
     *             if the key configuration represented by this handle is not
     *             defined.
     */
    String getParentId() throws NotDefinedException;

    /**
     * <p>
     * Returns whether or not this command is active. Instances of
     * <code>ICommand</code> are activated and deactivated by the instance of
     * <code>ICommandManager</code> from whence they were brokered.
     * </p>
     * <p>
     * Notification is sent to all registered listeners if this attribute
     * changes.
     * </p>
     * 
     * @return <code>true</code>, iff this command is active.
     */
    boolean isActive();

    /**
     * <p>
     * Returns whether or not the key configuration represented by this handle
     * is defined.
     * </p>
     * <p>
     * Notification is sent to all registered listeners if this attribute
     * changes.
     * </p>
     * 
     * @return <code>true</code>, iff the key configuration represented by
     *         this handle is defined.
     */
    boolean isDefined();

    /**
     * Unregisters an instance of <code>IKeyConfigurationListener</code>
     * listening for changes to attributes of this instance.
     * 
     * @param keyConfigurationListener
     *            the instance of <code>IKeyConfigurationListener</code> to
     *            unregister. Must not be <code>null</code>. If an attempt is
     *            made to unregister an instance of
     *            <code>IKeyConfigurationListener</code> which is not already
     *            registered with this instance, no operation is performed.
     */
    void removeKeyConfigurationListener(
            IKeyConfigurationListener keyConfigurationListener);
}
