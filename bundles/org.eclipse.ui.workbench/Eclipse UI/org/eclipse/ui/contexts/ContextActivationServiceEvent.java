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

package org.eclipse.ui.contexts;

/**
 * An instance of this class describes changes to an instance of <code>IContextActivationService</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see IContextActivationServiceListener#contextActivationServiceChanged
 */
public final class ContextActivationServiceEvent {
	private boolean activeContextIdsChanged;
	private IContextActivationService contextActivationService;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param contextActivationService
	 *            the instance of the interface that changed.
	 * @param activeContextIdsChanged
	 *            true, iff the activeContextIds property changed.
	 */
	public ContextActivationServiceEvent(
		IContextActivationService contextActivationService,
		boolean activeContextIdsChanged) {
		if (contextActivationService == null)
			throw new NullPointerException();

		this.activeContextIdsChanged = activeContextIdsChanged;
		this.contextActivationService = contextActivationService;
	}

	/**
	 * Returns the instance of the interface that changed.
	 * 
	 * @return the instance of the interface that changed. Guaranteed not to be
	 *         <code>null</code>.
	 */
	public IContextActivationService getContextActivationService() {
		return contextActivationService;
	}

	/**
	 * Returns whether or not the activeContextIdsChanged property changed.
	 * 
	 * @return true, iff the activeContextIdsChanged property changed.
	 */
	public boolean haveActiveContextIdsChanged() {
		return activeContextIdsChanged;
	}
}
