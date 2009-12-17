/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.commands;

import java.util.Set;

import org.eclipse.ui.internal.util.Util;

/**
 * An instance of this class describes changes to an instance of
 * <code>ICommandManager</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see ICommandManagerListener#commandManagerChanged(CommandManagerEvent)
 * @see org.eclipse.core.commands.CommandManagerEvent
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead.
 */
public final class CommandManagerEvent {

    /**
     * Whether the set of active contexts has changed.
     */
    private final boolean activeContextIdsChanged;

    /**
     * Whether the active key configuration has changed.
     */
    private final boolean activeKeyConfigurationIdChanged;

    /**
     * Whether the locale has changed.
     */
    private final boolean activeLocaleChanged;

    /**
     * Whether the platform has changed.
     */
    private final boolean activePlatformChanged;

    /**
     * Whether the command manager has changed.
     */
    private final ICommandManager commandManager;

    /**
     * Whether the list of defined categories has changed.
     */
    private final boolean definedCategoryIdsChanged;

    /** 
     * Whether the list of defined commands has changed.
     */
    private final boolean definedCommandIdsChanged;

    /**
     * Whether the list of defined key configurations has changed.
     */
    private final boolean definedKeyConfigurationIdsChanged;

    /**
     * The set of the defined categories before the change occurred.  This is a
     * set of strings (category identifiers).
     */
    private final Set previouslyDefinedCategoryIds;

    /**
     * The set of the defined commands before the change occurred.  This is a
     * set of strings (command identifiers).
     */
    private final Set previouslyDefinedCommandIds;

    /**
     * The set of the defined key configurations before the change occurred.
     * This is a set of strings (key configuration identifiers).
     */
    private final Set previouslyDefinedKeyConfigurationIds;

    /**
     * Creates a new instance of this class.
     * 
     * @param commandManager
     *            the instance of the interface that changed.
     * @param activeContextIdsChanged
     *            true, iff the activeContextIdsChanged property changed.
     * @param activeKeyConfigurationIdChanged
     *            true, iff the activeKeyConfigurationIdChanged property
     *            changed.
     * @param activeLocaleChanged
     *            true, iff the activeLocaleChanged property changed.
     * @param activePlatformChanged
     *            true, iff the activePlatformChanged property changed.
     * @param definedCategoryIdsChanged
     *            true, iff the definedCategoryIdsChanged property changed.
     * @param definedCommandIdsChanged
     *            true, iff the definedCommandIdsChanged property changed.
     * @param definedKeyConfigurationIdsChanged
     *            true, iff the definedKeyConfigurationIdsChanged property
     *            changed.
     * @param previouslyDefinedCategoryIds
     *            the set of identifiers to previously defined categories. This
     *            set may be empty. If this set is not empty, it must only
     *            contain instances of <code>String</code>. This set must be
     *            <code>null</code> if definedCategoryIdsChanged is
     *            <code>false</code> and must not be null if
     *            definedCategoryIdsChanged is <code>true</code>.
     * @param previouslyDefinedCommandIds
     *            the set of identifiers to previously defined commands. This
     *            set may be empty. If this set is not empty, it must only
     *            contain instances of <code>String</code>. This set must be
     *            <code>null</code> if definedCommandIdsChanged is
     *            <code>false</code> and must not be null if
     *            definedContextIdsChanged is <code>true</code>.
     * @param previouslyDefinedKeyConfigurationIds
     *            the set of identifiers to previously defined key
     *            configurations. This set may be empty. If this set is not
     *            empty, it must only contain instances of <code>String</code>.
     *            This set must be <code>null</code> if
     *            definedKeyConfigurationIdsChanged is <code>false</code> and
     *            must not be null if definedKeyConfigurationIdsChanged is
     *            <code>true</code>.
     */
    public CommandManagerEvent(ICommandManager commandManager,
            boolean activeContextIdsChanged,
            boolean activeKeyConfigurationIdChanged,
            boolean activeLocaleChanged, boolean activePlatformChanged,
            boolean definedCategoryIdsChanged,
            boolean definedCommandIdsChanged,
            boolean definedKeyConfigurationIdsChanged,
            Set previouslyDefinedCategoryIds, Set previouslyDefinedCommandIds,
            Set previouslyDefinedKeyConfigurationIds) {
        if (commandManager == null) {
			throw new NullPointerException();
		}

        if (!definedCategoryIdsChanged && previouslyDefinedCategoryIds != null) {
			throw new IllegalArgumentException();
		}

        if (!definedCommandIdsChanged && previouslyDefinedCommandIds != null) {
			throw new IllegalArgumentException();
		}

        if (!definedKeyConfigurationIdsChanged
                && previouslyDefinedKeyConfigurationIds != null) {
			throw new IllegalArgumentException();
		}

        if (definedCategoryIdsChanged) {
            this.previouslyDefinedCategoryIds = Util.safeCopy(
                    previouslyDefinedCategoryIds, String.class);
        } else {
            this.previouslyDefinedCategoryIds = null;
        }

        if (definedCommandIdsChanged) {
            this.previouslyDefinedCommandIds = Util.safeCopy(
                    previouslyDefinedCommandIds, String.class);
        } else {
            this.previouslyDefinedCommandIds = null;
        }

        if (definedKeyConfigurationIdsChanged) {
            this.previouslyDefinedKeyConfigurationIds = Util.safeCopy(
                    previouslyDefinedKeyConfigurationIds, String.class);
        } else {
            this.previouslyDefinedKeyConfigurationIds = null;
        }

        this.commandManager = commandManager;
        this.activeContextIdsChanged = activeContextIdsChanged;
        this.activeKeyConfigurationIdChanged = activeKeyConfigurationIdChanged;
        this.activeLocaleChanged = activeLocaleChanged;
        this.activePlatformChanged = activePlatformChanged;
        this.definedCategoryIdsChanged = definedCategoryIdsChanged;
        this.definedCommandIdsChanged = definedCommandIdsChanged;
        this.definedKeyConfigurationIdsChanged = definedKeyConfigurationIdsChanged;
    }

    /**
     * Returns the instance of the interface that changed.
     * 
     * @return the instance of the interface that changed. Guaranteed not to be
     *         <code>null</code>.
     */
    public ICommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * Returns the set of identifiers to previously defined categories.
     * 
     * @return the set of identifiers to previously defined categories. This set
     *         may be empty. If this set is not empty, it is guaranteed to only
     *         contain instances of <code>String</code>. This set is
     *         guaranteed to be <code>null</code> if
     *         haveDefinedCategoryIdsChanged() is <code>false</code> and is
     *         guaranteed to not be null if haveDefinedCategoryIdsChanged() is
     *         <code>true</code>.
     */
    public Set getPreviouslyDefinedCategoryIds() {
        return previouslyDefinedCategoryIds;
    }

    /**
     * Returns the set of identifiers to previously defined commands.
     * 
     * @return the set of identifiers to previously defined commands. This set
     *         may be empty. If this set is not empty, it is guaranteed to only
     *         contain instances of <code>String</code>. This set is
     *         guaranteed to be <code>null</code> if
     *         haveDefinedCommandIdsChanged() is <code>false</code> and is
     *         guaranteed to not be null if haveDefinedCommandIdsChanged() is
     *         <code>true</code>.
     */
    public Set getPreviouslyDefinedCommandIds() {
        return previouslyDefinedCommandIds;
    }

    /**
     * Returns the set of identifiers to previously defined key conigurations.
     * 
     * @return the set of identifiers to previously defined key configurations.
     *         This set may be empty. If this set is not empty, it is guaranteed
     *         to only contain instances of <code>String</code>. This set is
     *         guaranteed to be <code>null</code> if
     *         haveDefinedKeyConfigurationIdsChanged() is <code>false</code>
     *         and is guaranteed to not be null if
     *         haveDefinedKeyConfigurationIdsChanged() is <code>true</code>.
     */
    public Set getPreviouslyDefinedKeyConfigurationIds() {
        return previouslyDefinedKeyConfigurationIds;
    }

    /**
     * Returns whether or not the activeKeyConfigurationId property changed.
     * 
     * @return true, iff the activeKeyConfigurationId property changed.
     */
    public boolean hasActiveKeyConfigurationIdChanged() {
        return activeKeyConfigurationIdChanged;
    }

    /**
     * Returns whether or not the activeLocale property changed.
     * 
     * @return true, iff the activeLocale property changed.
     */
    public boolean hasActiveLocaleChanged() {
        return activeLocaleChanged;
    }

    /**
     * Returns whether or not the activePlatform property changed.
     * 
     * @return true, iff the activePlatform property changed.
     */
    public boolean hasActivePlatformChanged() {
        return activePlatformChanged;
    }

    /**
     * Returns whether or not the activeContextIds property changed.
     * 
     * @return true, iff the activeContextIds property changed.
     */
    public boolean haveActiveContextIdsChanged() {
        return activeContextIdsChanged;
    }

    /**
     * Returns whether or not the definedCategoryIds property changed.
     * 
     * @return true, iff the definedCategoryIds property changed.
     */
    public boolean haveDefinedCategoryIdsChanged() {
        return definedCategoryIdsChanged;
    }

    /**
     * Returns whether or not the definedCommandIds property changed.
     * 
     * @return true, iff the definedCommandIds property changed.
     */
    public boolean haveDefinedCommandIdsChanged() {
        return definedCommandIdsChanged;
    }

    /**
     * Returns whether or not the definedKeyConfigurationIds property changed.
     * 
     * @return true, iff the definedKeyConfigurationIds property changed.
     */
    public boolean haveDefinedKeyConfigurationIdsChanged() {
        return definedKeyConfigurationIdsChanged;
    }
}
