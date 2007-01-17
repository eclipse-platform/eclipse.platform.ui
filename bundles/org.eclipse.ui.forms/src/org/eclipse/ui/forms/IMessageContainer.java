/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.forms;

import org.eclipse.jface.dialogs.IMessageProvider;

/**
 * Classes that implement this interface can be managed by the message manager.
 * 
 * @since 3.3
 */
public interface IMessageContainer extends IMessageProvider {

	/**
	 * Sets the message with optional detailed text.
	 * 
	 * @param message
	 *            the message or the summary or <code>null</code> to clear the
	 *            container.
	 * @param details
	 *            optional details or <code>null</code> if not available.
	 * @param type
	 *            the message type as defined in {@link IMessageProvider}
	 */
	void setMessage(String message, String details, int type);
}