/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.commands;

/**
 * An instance of this class describes changes to an instance of <code>ICommandManager</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see ICommandManagerListener#commandManagerChanged
 */
public final class CommandManagerEvent {

    private boolean activeContextIdsChanged;

    private boolean activeKeyConfigurationIdChanged;

    private boolean activeLocaleChanged;

    private boolean activePlatformChanged;

    private ICommandManager commandManager;

    private boolean definedCategoryIdsChanged;

    private boolean definedCommandIdsChanged;

    private boolean definedKeyConfigurationIdsChanged;

    private boolean handlersByCommandIdChanged;

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
     * @param handlersByCommandIdChanged
     *            true, iff the handlersByCommandIdChanged property changed.
     */
    public CommandManagerEvent(ICommandManager commandManager,
            boolean activeContextIdsChanged,
            boolean activeKeyConfigurationIdChanged,
            boolean activeLocaleChanged, boolean activePlatformChanged,
            boolean definedCategoryIdsChanged,
            boolean definedCommandIdsChanged,
            boolean definedKeyConfigurationIdsChanged,
            boolean handlersByCommandIdChanged) {
        if (commandManager == null) throw new NullPointerException();

        this.commandManager = commandManager;
        this.activeContextIdsChanged = activeContextIdsChanged;
        this.activeKeyConfigurationIdChanged = activeKeyConfigurationIdChanged;
        this.activeLocaleChanged = activeLocaleChanged;
        this.activePlatformChanged = activePlatformChanged;
        this.definedCategoryIdsChanged = definedCategoryIdsChanged;
        this.definedCommandIdsChanged = definedCommandIdsChanged;
        this.definedKeyConfigurationIdsChanged = definedKeyConfigurationIdsChanged;
        this.handlersByCommandIdChanged = handlersByCommandIdChanged;
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
     * Returns whether or not the activeContextIds property changed.
     * 
     * @return true, iff the activeContextIds property changed.
     */
    public boolean haveActiveContextIdsChanged() {
        return activeContextIdsChanged;
    }

    /**
     * Returns whether or not the handlersByCommandId property changed.
     * 
     * @return true, iff the handlersByCommandId property changed.
     */
    public boolean haveHandlersByCommandIdChanged() {
        return handlersByCommandIdChanged;
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
