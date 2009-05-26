/*******************************************************************************
 *  Copyright (c) 2007, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.forms;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.Form;

/**
 * This interface provides for managing typed messages in a form. Typed messages
 * are messages associated with a type that indicates their severity (error,
 * warning, information). The interface is responsible for:
 * <ul>
 * <li>Bridging the concept of typed messages and control decorations</li>
 * <li>Adding one or more messages per control in a form</li>
 * <li>Rolling the local messages up to the form header</li>
 * <li>Adding one or more general messages to the form header</li>
 * </ul>
 * <p>
 * To use it in a form, do the following:
 * <ol>
 * <li>For each interactive control, add a listener to it to monitor user input</li>
 * <li>Every time the input changes, validate it. If there is a problem, add a
 * message with a unique key to the manager. If there is already a message with
 * the same key in the manager, its type and message text will be replaced (no
 * duplicates). Note that you add can messages with different keys to the same
 * control to track multiple problems with the user input.</li>
 * <li>If the problem has been cleared, remove the message using the key
 * (attempting to remove a message that is not in the manager is safe).</li>
 * <li>If something happens in the form that is not related to any control, use
 * the other <code>addMessage</code> method.</li>
 * </ol>
 * <p>
 * This interface should only be referenced. It must not be implemented or
 * extended.
 * </p>
 * 
 * @since 3.3
 * @see IMessageProvider
 * @see IManagedForm
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */

public interface IMessageManager {
	/**
	 * Adds a general message that is not associated with any decorated field.
	 * Note that subsequent calls using the same key will not result in
	 * duplicate messages. Instead, the previous message with the same key will
	 * be replaced with the new message.
	 * 
	 * @param key
	 *            a unique message key that will be used to look the message up
	 *            later
	 * 
	 * @param messageText
	 *            the message to add
	 * @param data
	 *            an object for application use (can be <code>null</code>)
	 * @param type
	 *            the message type as defined in <code>IMessageProvider</code>.
	 */
	void addMessage(Object key, String messageText, Object data, int type);

	/**
	 * Adds a message that should be associated with the provided control. Note
	 * that subsequent calls using the same key will not result in duplicate
	 * messages. Instead, the previous message with the same key will be
	 * replaced with the new message.
	 * 
	 * @param key
	 *            the unique message key
	 * @param messageText
	 *            the message to add
	 * @param data
	 *            an object for application use (can be <code>null</code>)
	 * @param type
	 *            the message type
	 * @param control
	 *            the control to associate the message with
	 */
	void addMessage(Object key, String messageText, Object data, int type,
			Control control);

	/**
	 * Removes the general message with the provided key. Does nothing if
	 * message for the key does not exist.
	 * 
	 * @param key
	 *            the key of the message to remove
	 */
	void removeMessage(Object key);

	/**
	 * Removes all the general messages. If there are local messages associated
	 * with controls, the replacement message may show up drawing user's
	 * attention to these local messages. Otherwise, the container will clear
	 * the message area.
	 */
	void removeMessages();

	/**
	 * Removes a keyed message associated with the provided control. Does
	 * nothing if the message for that key does not exist.
	 * 
	 * @param key
	 *            the id of the message to remove
	 * @param control
	 *            the control the message is associated with
	 */
	void removeMessage(Object key, Control control);

	/**
	 * Removes all the messages associated with the provided control. Does
	 * nothing if there are no messages for this control.
	 * 
	 * @param control
	 *            the control the messages are associated with
	 */
	void removeMessages(Control control);

	/**
	 * Removes all the local field messages and all the general container
	 * messages.
	 */
	void removeAllMessages();

	/**
	 * Updates the message container with the messages currently in the manager.
	 * There are two scenarios in which a client may want to use this method:
	 * <ol>
	 * <li>When controls previously managed by this manager have been disposed.</li>
	 * <li>When automatic update has been turned off.</li>
	 * </ol>
	 * In all other situations, the manager will keep the form in sync
	 * automatically.
	 * 
	 * @see #setAutoUpdate(boolean)
	 */
	void update();

	/**
	 * Controls whether the form is automatically updated when messages are
	 * added or removed. By default, auto update is on. Clients can turn it off
	 * prior to adding or removing a number of messages as a batch. Turning it
	 * back on will trigger an update.
	 * 
	 * @param enabled
	 *            sets the state of the automatic update
	 */
	void setAutoUpdate(boolean enabled);

	/**
	 * Tests whether the form will be automatically updated when messages are
	 * added or removed.
	 * 
	 * @return <code>true</code> if auto update is active, <code>false</code>
	 *         otherwise.
	 */
	boolean isAutoUpdate();

	/**
	 * Sets the alternative message prefix provider. The default prefix provider
	 * is set by the manager.
	 * 
	 * @param provider
	 *            the new prefix provider or <code>null</code> to turn the
	 *            prefix generation off.
	 */
	void setMessagePrefixProvider(IMessagePrefixProvider provider);

	/**
	 * @return the current prefix provider or <code>null</code> if prefixes
	 *         are not generated.
	 */
	IMessagePrefixProvider getMessagePrefixProvider();

	/**
	 * Message manager uses SWT.LEFT|SWT.BOTTOM for the default decoration
	 * position. Use this method to change it.
	 * 
	 * @param position
	 *            the decoration position
	 * @see ControlDecoration
	 */
	void setDecorationPosition(int position);

	/**
	 * Returns the currently used decoration position for all control messages.
	 * 
	 * @return the current decoration position
	 */

	int getDecorationPosition();

	/**
	 * When message manager is used in context of a form, and there are
	 * hyperlink listeners for messages in the header, the hyperlink event will
	 * carry an object of type <code>IMessage[]</code> as an href. You can use
	 * this method to create a summary text from this array consistent with the
	 * tool tip used by the form header.
	 * 
	 * @param messages
	 *            an array of messages
	 * @return a textual representation of the messages with one message per
	 *         line.
	 * @see Form#addMessageHyperlinkListener(org.eclipse.ui.forms.events.IHyperlinkListener)
	 */
	String createSummary(IMessage[] messages);
}