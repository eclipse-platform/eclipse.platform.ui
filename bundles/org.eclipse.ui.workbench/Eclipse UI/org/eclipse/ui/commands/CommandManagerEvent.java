/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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
 * <p>
 * An instance of <code>ICommandManagerEvent</code> describes changes to an
 * instance of <code>ICommandManager</code>.
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see ICommandManager
 * @see ICommandManagerListener#commandManagerChanged
 */
public final class CommandManagerEvent {

	private boolean activeActivityIdsChanged;
	private boolean activeKeyConfigurationIdChanged;
	private boolean activeLocaleChanged;
	private boolean activePlatformChanged;
	private ICommandManager commandManager;
	private boolean definedCategoryIdsChanged;
	private boolean definedCommandIdsChanged;
	private boolean definedKeyConfigurationIdsChanged;
	private boolean modeChanged;

	/**
	 * TODO javadoc
	 * 
	 * @param commandManager
	 * @param activeActivityIdsChanged
	 * @param activeKeyConfigurationIdChanged
	 * @param activeLocaleChanged
	 * @param activePlatformChanged
	 * @param definedCategoryIdsChanged
	 * @param definedCommandIdsChanged
	 * @param definedKeyConfigurationIdsChanged
	 * @param modeChanged
	 */
	public CommandManagerEvent(
		ICommandManager commandManager,
		boolean activeActivityIdsChanged,
		boolean activeKeyConfigurationIdChanged,
		boolean activeLocaleChanged,
		boolean activePlatformChanged,
		boolean definedCategoryIdsChanged,
		boolean definedCommandIdsChanged,
		boolean definedKeyConfigurationIdsChanged,
		boolean modeChanged) {
		if (commandManager == null)
			throw new NullPointerException();

		this.commandManager = commandManager;
		this.activeActivityIdsChanged = activeActivityIdsChanged;
		this.activeKeyConfigurationIdChanged = activeKeyConfigurationIdChanged;
		this.activeLocaleChanged = activeLocaleChanged;
		this.activePlatformChanged = activePlatformChanged;
		this.definedCategoryIdsChanged = definedCategoryIdsChanged;
		this.definedCommandIdsChanged = definedCommandIdsChanged;
		this.definedKeyConfigurationIdsChanged =
			definedKeyConfigurationIdsChanged;
		this.modeChanged = modeChanged;
	}

	/**
	 * Returns the instance of <code>ICommandManager</code> that has changed.
	 * 
	 * @return the instance of <code>ICommandManager</code> that has changed.
	 *         Guaranteed not to be <code>null</code>.
	 */
	public ICommandManager getCommandManager() {
		return commandManager;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasActiveKeyConfigurationIdChanged() {
		return activeKeyConfigurationIdChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasActiveLocaleChanged() {
		return activeLocaleChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasActivePlatformChanged() {
		return activePlatformChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasModeChanged() {
		return modeChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean haveActiveActivityIdsChanged() {
		return activeActivityIdsChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean haveDefinedCategoryIdsChanged() {
		return definedCategoryIdsChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean haveDefinedCommandIdsChanged() {
		return definedCommandIdsChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean haveDefinedKeyConfigurationIdsChanged() {
		return definedKeyConfigurationIdsChanged;
	}
}
