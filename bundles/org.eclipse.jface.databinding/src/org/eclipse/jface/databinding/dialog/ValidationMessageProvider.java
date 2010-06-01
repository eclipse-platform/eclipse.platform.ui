/*******************************************************************************
 * Copyright (c) 2009, 2010 Ovidio Mallo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ovidio Mallo - initial API and implementation (bug 248877)
 ******************************************************************************/

package org.eclipse.jface.databinding.dialog;

import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;

/**
 * Standard implementation of the {@link IValidationMessageProvider} interface.
 * 
 * @since 1.4
 */
public class ValidationMessageProvider implements IValidationMessageProvider {

	/**
	 * Returns the {@link IStatus#getMessage() message} of the
	 * <code>IStatus</code> contained in the provided
	 * <code>validationStatusProvider</code> as is or <code>null</code> if the
	 * <code>validationStatusProvider</code> is itself <code>null</code>.
	 */
	public String getMessage(ValidationStatusProvider statusProvider) {
		if (statusProvider != null) {
			IStatus status = (IStatus) statusProvider.getValidationStatus()
					.getValue();
			return status.getMessage();
		}
		return null;
	}

	/**
	 * Returns the message type defined in {@link IMessageProvider} which
	 * naturally maps to the {@link IStatus#getSeverity()} of the
	 * <code>IStatus</code> contained in the provided
	 * <code>validationStatusProvider</code>.
	 */
	public int getMessageType(ValidationStatusProvider statusProvider) {
		if (statusProvider == null) {
			return IMessageProvider.NONE;
		}

		IStatus status = (IStatus) statusProvider.getValidationStatus()
				.getValue();
		int severity = status.getSeverity();
		switch (severity) {
		case IStatus.OK:
			return IMessageProvider.NONE;
		case IStatus.CANCEL:
			return IMessageProvider.NONE;
		case IStatus.INFO:
			return IMessageProvider.INFORMATION;
		case IStatus.WARNING:
			return IMessageProvider.WARNING;
		case IStatus.ERROR:
			return IMessageProvider.ERROR;
		default:
			Assert.isTrue(false, "incomplete switch statement"); //$NON-NLS-1$
			return -1; // unreachable
		}
	}
}
