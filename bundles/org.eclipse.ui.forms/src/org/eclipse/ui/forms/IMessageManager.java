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
import org.eclipse.swt.widgets.Control;

/**
 * This interface provides for managing messages in a form. It is responsible
 * for:
 * <ul>
 * <li>Bridging the concept of messages and field decorations</li>
 * <li>Adding multiple messages per field in a form</li>
 * <li>Rolling up local messages to the form header</li>
 * <li>Adding multiple general messages to the form header</li>
 * </ul>
 * 
 * @since 3.3
 * @see IMessageProvider
 * @see IMessageContainer
 */

public interface IMessageManager {

	/**
	 * Adds a general message that is not associated with any decorated field.
	 * 
	 * @param key
	 *            a unique message key that will be used to look the message up
	 *            later
	 * 
	 * @param messageText
	 *            the message to add
	 * @param type
	 *            the message type as defined in <code>IMessageProvider</code>.
	 */
	public void addMessage(Object key, String messageText, int type);

	/**
	 * Adds a message that should be associated with the provided control.
	 * 
	 * @param key
	 *            the unique message key
	 * @param messageText
	 *            the message to add
	 * @param type
	 *            the message type
	 * @param control
	 *            the control to associate the message with
	 */
	public void addMessage(Object key, String messageText, int type,
			Control control);

	/**
	 * Removes the provided general message.
	 * 
	 * @param key
	 *            the key of the message to remove
	 */
	public void removeMessage(Object key);

	/**
	 * Removes all the general messages. If there are local messages associated
	 * with controls, the replacement message may show up drawing user's
	 * attention to these local messages. Otherwise, the container will clear
	 * the message area.
	 */
	public void removeMessages();

	/**
	 * Removes the message associated with the provided control.
	 * 
	 * @param key
	 *            the id of the message to remove
	 * @param control
	 *            the control the message is associated with
	 */
	public void removeMessage(Object key, Control control);

	/**
	 * Removes all the messages associated with the provided control.
	 * 
	 * @param control
	 *            the control the messages are associated with
	 */
	public void removeMessages(Control control);

	/**
	 * Removes all the local field messages and all the general container
	 * messages.
	 */
	public void removeAllMessages();

	/**
	 * Updates the message container with the messages currently in the manager.
	 * Use this method when some of the controls previously managed have been
	 * disposed. Automatic update on control dispose is not done to avoid an
	 * attempt to update a container that is in the process of being disposed
	 * itself.
	 */
	public void update();
}