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

package org.eclipse.ui.contexts.activationservice;

/**
 * <p>
 * An instance of <code>ContextActivationServiceEvent</code> describes changes to an
 * instance of <code>IContextActivationService</code>.
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IContextActivationService
 * @see IContextActivationServiceListener#contextActivationServiceChanged
 */
public final class ContextActivationServiceEvent {

	private boolean activeContextIdsChanged;
	private IContextActivationService contextActivationService;

	/**
	 * TODO javadoc
	 * 
	 * @param contextActivationService
	 * @param activeContextIdsChanged
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
	 * Returns the instance of <code>IContextActivationService</code> that has
	 * changed.
	 * 
	 * @return the instance of <code>IContextActivationService</code> that has
	 *         changed. Guaranteed not to be <code>null</code>.
	 */
	public IContextActivationService getContextActivationService() {
		return contextActivationService;
	}

	/**
	 * TODO javadoc
	 */
	public boolean haveActiveContextIdsChanged() {
		return activeContextIdsChanged;
	}
}
