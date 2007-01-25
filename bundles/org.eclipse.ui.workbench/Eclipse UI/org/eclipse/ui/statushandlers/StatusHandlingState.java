/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.statushandlers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;

/**
 * <p>
 * Contains an instance of IStatus subclass and its handling hint. Used during
 * status handling process. During the process both status and hint can be
 * changed, so a subsequent handler can receive different status or handling
 * hint.
 * </p>
 * 
 * <p>
 * Hint values are defined in {@link StatusManager}.
 * </p>
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 */
public class StatusHandlingState {

	private IStatus status;

	private int handlingHint;

	/**
	 * @param status
	 *            not null
	 * @param handlingHint
	 *            one of the values defined in {@link StatusManager}, for
	 *            instance StatusManager.SHOW
	 */
	public StatusHandlingState(IStatus status, int handlingHint) {
		Assert.isNotNull(status);
		this.status = status;
		this.handlingHint = handlingHint;
	}

	/**
	 * @return Returns the status.
	 */
	public IStatus getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            The status to set, not null.
	 */
	public void setStatus(IStatus status) {
		Assert.isNotNull(status);
		this.status = status;
	}

	/**
	 * @return Returns one of the values defined in {@link StatusManager}, for
	 *         instance StatusManager.SHOW
	 */
	public int getHandlingHint() {
		return handlingHint;
	}

	/**
	 * @param handlingHint
	 *            The handlingHint to set.
	 */
	public void setHandlingHint(int handlingHint) {
		this.handlingHint = handlingHint;
	}
}
