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
 * An instance of <code>KeyConfigurationEvent</code> describes changes to an
 * instance of <code>IKeyConfiguration</code>.
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IKeyConfiguration
 * @see IKeyConfigurationListener#commandChanged
 */
public final class KeyConfigurationEvent {

	private boolean activeChanged;
	private boolean definedChanged;
	private boolean descriptionChanged;
	private IKeyConfiguration keyConfiguration;
	private boolean nameChanged;
	private boolean parentIdChanged;

	/**
	 * TODO javadoc
	 * 
	 * @param keyConfiguration
	 * @param activeChanged
	 * @param definedChanged
	 * @param descriptionChanged
	 * @param nameChanged
	 * @param parentIdChanged
	 */
	public KeyConfigurationEvent(
		IKeyConfiguration keyConfiguration,
		boolean activeChanged,
		boolean definedChanged,
		boolean descriptionChanged,
		boolean nameChanged,
		boolean parentIdChanged) {
		if (keyConfiguration == null)
			throw new NullPointerException();

		this.keyConfiguration = keyConfiguration;
		this.activeChanged = activeChanged;
		this.definedChanged = definedChanged;
		this.descriptionChanged = descriptionChanged;
		this.nameChanged = nameChanged;
		this.parentIdChanged = parentIdChanged;
	}

	/**
	 * Returns the instance of <code>IKeyConfiguration</code> that has
	 * changed.
	 * 
	 * @return the instance of <code>IKeyConfiguration</code> that has
	 *         changed. Guaranteed not to be <code>null</code>.
	 */
	public IKeyConfiguration getKeyConfiguration() {
		return keyConfiguration;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasActiveChanged() {
		return activeChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasDefinedChanged() {
		return definedChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasDescriptionChanged() {
		return descriptionChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasNameChanged() {
		return nameChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasParentIdChanged() {
		return parentIdChanged;
	}
}
