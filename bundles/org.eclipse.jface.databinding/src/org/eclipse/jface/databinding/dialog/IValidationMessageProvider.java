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
import org.eclipse.jface.dialogs.IMessageProvider;

/**
 * Simple interface to provide a validation message text and a message type for
 * a given {@link ValidationStatusProvider}.
 * 
 * <p>
 * Can be used in dialogs to display a message text along with an icon
 * reflecting the validation status.
 * </p>
 * 
 * @since 1.4
 */
public interface IValidationMessageProvider {

	/**
	 * Returns the validation message text for the given validation status
	 * provider.
	 * 
	 * @param statusProvider
	 *            the {@link ValidationStatusProvider} for which to provide a
	 *            message text. May be <code>null</code>.
	 * @return The validation message text for the given
	 *         <code>validationStatusProvider</code>. May be <code>null</code>.
	 */
	public String getMessage(ValidationStatusProvider statusProvider);

	/**
	 * Returns the validation message type as one of the constants defined in
	 * {@link IMessageProvider} for the given validation status provider.
	 * 
	 * @param statusProvider
	 *            the {@link ValidationStatusProvider} for which to provide a
	 *            message type. May be <code>null</code>.
	 * @return The validation message type for the given
	 *         <code>validationStatusProvider</code>.
	 */
	public int getMessageType(ValidationStatusProvider statusProvider);
}
