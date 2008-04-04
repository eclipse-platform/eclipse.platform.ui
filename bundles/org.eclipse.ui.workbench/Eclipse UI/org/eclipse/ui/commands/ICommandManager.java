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

import java.util.Map;
import java.util.Set;

import org.eclipse.ui.keys.KeySequence;

/**
 * <p>
 * An instance of <code>ICommandManager</code> can be used to obtain instances
 * of <code>ICommand</code>, as well as manage whether or not those instances
 * are active or inactive, enabled or disabled.
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @see org.eclipse.ui.commands.ICommand
 * @see org.eclipse.ui.commands.ICommandManagerListener
 * @see org.eclipse.core.commands.CommandManager
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICommandManager {

    /**
     * Registers an instance of <code>ICommandManagerListener</code> to listen
     * for changes to attributes of this instance.
     * 
     * @param commandManagerListener
     *            the instance of <code>ICommandManagerListener</code> to
     *            register. Must not be <code>null</code>. If an attempt is
     *            made to register an instance of
     *            <code>ICommandManagerListener</code> which is already
     *            registered with this instance, no operation is performed.
     */
    void addCommandManagerListener(
            ICommandManagerListener commandManagerListener);

    /**
     * Returns the set of identifiers to active contexts.
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return the set of identifiers to active contexts. This set may be
     *         empty, but is guaranteed not to be <code>null</code>. If this
     *         set is not empty, it is guaranteed to only contain instances of
     *         <code>String</code>.
     */
    Set getActiveContextIds();

    /**
     * Returns the active key configuration.
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return the active key configuration identifier. This set may be empty,
     *         but it is guaranteed to not be <code>null</code>. If this set
     *         is not empty, it is guaranteed to only contains instances of
     *         <code>String</code>.
     */
    String getActiveKeyConfigurationId();

    /**
     * Returns the active locale. While this property tends to be simply the
     * result of {@link java.util.Locale#getDefault()}, it may also be changed
     * at runtime by different implementations of command manager.
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return the active locale. May be <code>null</code>.
     */
    String getActiveLocale();

    /**
     * Returns the active platform. While this property tends to be simply the
     * result of {@link org.eclipse.swt.SWT#getPlatform()}, it may also be
     * changed at runtime by different implementations of command manager.
     * <p>
     * Notification is sent to all registered listeners if this property
     * changes.
     * </p>
     * 
     * @return the active platform. May be <code>null</code>.
     */
    String getActivePlatform();

    /**
     * Returns a handle to a category given an identifier.
     * 
     * @param categoryId
     *            an identifier. Must not be <code>null</code>
     * @return a handle to a category.
     */
    ICategory getCategory(String categoryId);

    /**
     * Returns a handle to a command given an identifier.
     * 
     * @param commandId
     *            an identifier. Must not be <code>null</code>
     * @return a handle to a command; never <code>null</code>.
     */
    ICommand getCommand(String commandId);

    /**
     * <p>
     * Returns the set of identifiers to defined categories.
     * </p>
     * <p>
     * Notification is sent to all registered listeners if this attribute
     * changes.
     * </p>
     * 
     * @return the set of identifiers to defined categories. This set may be
     *         empty, but is guaranteed not to be <code>null</code>. If this
     *         set is not empty, it is guaranteed to only contain instances of
     *         <code>String</code>.
     */
    Set getDefinedCategoryIds();

    /**
     * <p>
     * Returns the set of identifiers to defined commands.
     * </p>
     * <p>
     * Notification is sent to all registered listeners if this attribute
     * changes.
     * </p>
     * 
     * @return the set of identifiers to defined commands. This set may be
     *         empty, but is guaranteed not to be <code>null</code>. If this
     *         set is not empty, it is guaranteed to only contain instances of
     *         <code>String</code>.
     */
    Set getDefinedCommandIds();

    /**
     * <p>
     * Returns the set of identifiers to defined key configurations.
     * </p>
     * <p>
     * Notification is sent to all registered listeners if this attribute
     * changes.
     * </p>
     * 
     * @return the set of identifiers to defined key configurations. This set
     *         may be empty, but is guaranteed not to be <code>null</code>.
     *         If this set is not empty, it is guaranteed to only contain
     *         instances of <code>String</code>.
     */
    Set getDefinedKeyConfigurationIds();

    /**
     * Returns a handle to a key configuration given an identifier.
     * 
     * @param keyConfigurationId
     *            an identifier. Must not be <code>null</code>
     * @return a handle to a key configuration.
     */
    IKeyConfiguration getKeyConfiguration(String keyConfigurationId);

    /**
     * Finds all of the commands which have key bindings that start with the
     * given key sequence.
     * 
     * @param keySequence
     *            The prefix to look for; must not be <code>null</code>.
     * @return A map of all of the matching key sequences (
     *         <code>KeySequence</code>) to command identifiers (
     *         <code>String</code>). This map may be empty, but it is never
     *         <code>null</code>.
     */
    Map getPartialMatches(KeySequence keySequence);

    /**
     * Finds the command which has the given key sequence as one of its key
     * bindings.
     * 
     * @param keySequence
     *            The key binding to look for; must not be <code>null</code>.
     * @return The command id for the matching command, if any;
     *         <code>null</code> if none.
     */
    String getPerfectMatch(KeySequence keySequence);

    /**
     * Checks to see whether there are any commands which have key bindings that
     * start with the given key sequence.
     * 
     * @param keySequence
     *            The prefix to look for; must not be <code>null</code>.
     * @return <code>true</code> if at least one command has a key binding
     *         that starts with <code>keySequence</code>;<code>false</code>
     *         otherwise.
     */
    boolean isPartialMatch(KeySequence keySequence);

    /**
     * Checks to see if there is a command with the given key sequence as one of
     * its key bindings.
     * 
     * @param keySequence
     *            The key binding to look for; must not be <code>null</code>.
     * @return <code>true</code> if a command has a matching key binding;
     *         <code>false</code> otherwise.
     */
    boolean isPerfectMatch(KeySequence keySequence);

    /**
     * Unregisters an instance of <code>ICommandManagerListener</code>
     * listening for changes to attributes of this instance.
     * 
     * @param commandManagerListener
     *            the instance of <code>ICommandManagerListener</code> to
     *            unregister. Must not be <code>null</code>. If an attempt is
     *            made to unregister an instance of
     *            <code>ICommandManagerListener</code> which is not already
     *            registered with this instance, no operation is performed.
     */
    void removeCommandManagerListener(
            ICommandManagerListener commandManagerListener);
}
