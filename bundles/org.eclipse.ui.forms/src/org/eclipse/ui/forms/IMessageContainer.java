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

package org.eclipse.ui.forms;

import org.eclipse.jface.dialogs.IMessageProvider;

/**
 * A generic interface for containers capable of displaying messages of a set
 * type (as defined in <code>IMessageProvider</code>). Although there is no
 * firm contract on how these messages are displayed, a typical implementation
 * will have some kind of a header area for this purpose.
 * 
 * <p><strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UA team.</p>
 * 
 * @since 3.3
 */
public interface IMessageContainer extends IMessageProvider {
	/**
	 * Sets the message for this container with an indication of what type of
	 * message it is.
	 * <p>
	 * The valid message types are one of <code>NONE</code>,
	 * <code>INFORMATION</code>,<code>WARNING</code>, or
	 * <code>ERROR</code>.
	 * </p>
	 * 
	 * @param newMessage
	 *            the message, or <code>null</code> to clear the message
	 * @param newType
	 *            the message type
	 */
	public void setMessage(String newMessage, int newType);
}