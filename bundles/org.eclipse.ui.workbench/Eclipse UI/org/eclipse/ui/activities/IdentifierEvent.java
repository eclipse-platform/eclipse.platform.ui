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

package org.eclipse.ui.activities;

/**
 * An instance of this class describes changes to an instance of <code>IIdentifier</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see IIdentifierListener#identifierChanged
 */
public final class IdentifierEvent {
	private boolean activityIdsChanged;
	private boolean enabledChanged;
	private IIdentifier identifier;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param identifier
	 *            the instance of the interface that changed.
	 * @param activityIdsChanged
	 *            true, iff the activityIds property changed.
	 * @param enabledChanged
	 *            true, iff the enabled property changed.
	 */
	public IdentifierEvent(
		IIdentifier identifier,
		boolean activityIdsChanged,
		boolean enabledChanged) {
		if (identifier == null)
			throw new NullPointerException();

		this.identifier = identifier;
		this.activityIdsChanged = activityIdsChanged;
		this.enabledChanged = enabledChanged;
	}

	/**
	 * Returns the instance of the interface that changed.
	 * 
	 * @return the instance of the interface that changed. Guaranteed not to be
	 *         <code>null</code>.
	 */
	public IIdentifier getIdentifier() {
		return identifier;
	}

	/**
	 * Returns whether or not the activityIds property changed.
	 * 
	 * @return true, iff the activityIds property changed.
	 */
	public boolean hasActivityIdsChanged() {
		return activityIdsChanged;
	}

	/**
	 * Returns whether or not the enabled property changed.
	 * 
	 * @return true, iff the enabled property changed.
	 */
	public boolean hasEnabledChanged() {
		return enabledChanged;
	}
}
