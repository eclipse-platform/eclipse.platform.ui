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
 * <p>
 * An instance of <code>IdentifierEvent</code> describes changes to an
 * instance of <code>IIdentifier</code>.
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IIdentifier
 * @see IIdentifierListener#identifierChanged
 */
public final class IdentifierEvent {
	private boolean activityIdsChanged;
	private boolean enabledChanged;
	private IIdentifier identifier;

	/**
	 * TODO javadoc
	 * 
	 * @param identifier
	 * @param availableChanged
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
	 * Returns the instance of <code>IIdentifier</code> that has changed.
	 * 
	 * @return the instance of <code>IIdentifier</code> that has changed.
	 *         Guaranteed not to be <code>null</code>.
	 */
	public IIdentifier getIdentifier() {
		return identifier;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasActivityIdsChanged() {
		return activityIdsChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasEnabledChanged() {
		return enabledChanged;
	}
}
