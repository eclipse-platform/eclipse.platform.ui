/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.commands;

import java.util.Map;
import java.util.Set;

import org.eclipse.ui.internal.commands.CommandManagerFactory;
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
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see CommandManagerFactory
 * @see ICommand
 * @see ICommandManagerListener
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
	 * @return the active key configuration. May be <code>null</code>. 
	 */
    String getActiveKeyConfigurationId();

	/**
	 * Returns the active locale.
	 * <p>
	 * Notification is sent to all registered listeners if this property
	 * changes.
	 * </p>
	 * 
	 * @return the active locale. May be <code>null</code>. 
	 */
    String getActiveLocale();

	/**
	 * Returns the active platform.
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
     * @return a handle to a command.
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
     * TODO javadoc
     */
    Map getPartialMatches(KeySequence keySequence);

    /**
     * TODO javadoc
     */
    String getPerfectMatch(KeySequence keySequence);

    /**
     * TODO javadoc
     */
    boolean isPartialMatch(KeySequence keySequence);

    /**
     * TODO javadoc
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