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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.internal.forms.Messages;

/**
 * Use this class to work with message containers that contain decorated fields.
 * The class provides for:
 * <ul>
 * <li>Bridging the concept of messages and field decorations</li>
 * <li>Adding multiple messages per field</li>
 * <li>Rolling up local messages to the message container</li>
 * <li>Adding multiple general messages to the message container</li>
 * </ul>
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @see IMessageContainer
 * @see IMessageContainerWithDetails
 * @since 3.3
 */

public class MessageManager {
	private ArrayList messages = new ArrayList();
	private Hashtable decorators = new Hashtable();
	private IMessageContainer messageContainer;
	private static FieldDecoration standardError = FieldDecorationRegistry
			.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
	private static FieldDecoration standardWarning = FieldDecorationRegistry
			.getDefault().getFieldDecoration(
					FieldDecorationRegistry.DEC_WARNING);

	private static final String[] SINGLE_MESSAGE_SUMMARY_KEYS = {
			Messages.MessageManager_sMessageSummary,
			Messages.MessageManager_sMessageSummary,
			Messages.MessageManager_sWarningSummary,
			Messages.MessageManager_sErrorSummary };

	private static final String[] MULTIPLE_MESSAGE_SUMMARY_KEYS = {
			Messages.MessageManager_pMessageSummary,
			Messages.MessageManager_pMessageSummary,
			Messages.MessageManager_pWarningSummary,
			Messages.MessageManager_pErrorSummary };

	class Message implements IMessage {
		Object key;
		String message;
		int type;
		String prefix;
		Object data;

		Message(Object key, String message, int type, Object data) {
			this.key = key;
			this.message = message;
			this.type = type;
			this.data = data;
		}

		public Object getData() {
			return data;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.IMessage#getKey()
		 */
		public Object getKey() {
			return key;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessage()
		 */
		public String getMessage() {
			return message;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessageType()
		 */
		public int getMessageType() {
			return type;
		}

		String getFullMessage() {
			if (prefix == null)
				return message;
			return prefix + message;
		}

	}

	class ControlDecorator implements IMessageContainerWithDetails {
		private ControlDecoration decoration;
		private ArrayList controlMessages = new ArrayList();
		private String message;
		private int type;
		private String prefix;

		ControlDecorator(Control control) {
			this.decoration = new ControlDecoration(control, SWT.LEFT
					| SWT.BOTTOM);
		}

		String getPrefix() {
			if (prefix == null)
				createPrefix();
			return prefix;
		}

		private void createPrefix() {
			Control c = decoration.getControl();
			Composite parent = c.getParent();
			Control[] siblings = parent.getChildren();
			for (int i = 0; i < siblings.length; i++) {
				if (siblings[i] == c) {
					// this is us - go backward until you hit
					// a label
					for (int j = i - 1; j >= 0; j--) {
						Control label = siblings[j];
						if (label instanceof Label) {
							prefix = ((Label) label).getText() + ": "; //$NON-NLS-1$
							return;
						}
					}
					break;
				}
			}
			// make a prefix anyway
			prefix = ""; //$NON-NLS-1$
		}

		void addAll(ArrayList target) {
			target.addAll(controlMessages);
		}

		void addMessage(Object key, String text, int type, Object data) {
			MessageManager.this.addMessage(getPrefix(), key, text, type, data,
					controlMessages);
			updateMessageContainer(this, controlMessages, true);
		}

		void removeMessage(Object key) {
			IMessage message = findMessage(key, controlMessages);
			if (message != null) {
				controlMessages.remove(message);
				updateMessageContainer(this, controlMessages, true);
			}
		}

		void removeMessages() {
			controlMessages.clear();
			updateMessageContainer(this, controlMessages, true);
		}

		boolean isEmpty() {
			return controlMessages.isEmpty();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.IMessageContainer#setMessage(java.lang.String,
		 *      int)
		 */
		public void setMessage(String newMessage, int newType) {
			if (this.message != null && newMessage != null
					&& newMessage.equals(this.message) && newType == this.type)
				return;
			this.message = newMessage;
			this.type = newType;
			update();
		}

		public void setMessage(String newMessage, String details,
				IMessage[] messages, int type) {
			setMessage(newMessage, type);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessage()
		 */
		public String getMessage() {
			return message;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessageType()
		 */
		public int getMessageType() {
			return type;
		}

		private void update() {
			if (message == null)
				decoration.hide();
			else {
				if (type == IMessageProvider.ERROR)
					decoration.setImage(standardError.getImage());
				else if (type == IMessageProvider.WARNING)
					decoration.setImage(standardWarning.getImage());
				decoration.setDescriptionText(message);
				decoration.show();
			}
		}
	}

	/**
	 * Creates a new instance of the message manager that will work with the
	 * provided message container.
	 * 
	 * @param messageContainer
	 *            the container to control
	 */
	public MessageManager(IMessageContainer messageContainer) {
		this.messageContainer = messageContainer;
	}

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
	 * @param data
	 *            an optional object for application use or <code>null</code>.
	 */

	public void addMessage(Object key, String messageText, int type, Object data) {
		addMessage(null, key, messageText, type, data, messages);
		updateMessageContainer();
	}

	/**
	 * Adds a message that should be associated with the provided control.
	 * 
	 * @param key
	 *            the unique message key
	 * @param messageText
	 *            the message to add
	 * @param type
	 *            the message type
	 * @param data
	 *            an optional data object for application use or
	 *            <code>null</code>.
	 * @param control
	 *            the control to associate the message with
	 */

	public void addMessage(Object key, String messageText, int type,
			Object data, Control control) {
		ControlDecorator dec = (ControlDecorator) decorators.get(control);
		if (dec == null) {
			dec = new ControlDecorator(control);
			decorators.put(control, dec);
			control.addDisposeListener(new DisposeListener() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
				 */
				public void widgetDisposed(DisposeEvent e) {
					decorators.remove(e.widget);
					updateMessageContainer();
				}
			});
		}
		dec.addMessage(key, messageText, type, data);
		updateMessageContainer();
	}

	/**
	 * Removes the provided general message.
	 * 
	 * @param key
	 *            the key of the message to remove
	 */

	public void removeMessage(Object key) {
		Message message = findMessage(key, messages);
		if (message != null) {
			messages.remove(message);
			updateMessageContainer();
		}
	}

	/**
	 * Removes all the general messages. If there are local messages associated
	 * with controls, the replacement message may show up drawing user's
	 * attention to these local messages. Otherwise, the container will clear
	 * the message area.
	 */
	public void removeMessages() {
		messages.clear();
		updateMessageContainer();
	}

	/**
	 * Removes the message associated with the provided control.
	 * 
	 * @param key
	 *            the id of the message to remove
	 * @param control
	 *            the control the message is associated with
	 */

	public void removeMessage(Object key, Control control) {
		ControlDecorator dec = (ControlDecorator) decorators.get(control);
		if (dec == null)
			return;
		dec.removeMessage(key);
		updateMessageContainer();
	}

	/**
	 * Removes all the messages associated with the provided control.
	 * 
	 * @param control
	 *            the control the messages are associated with
	 */

	public void removeMessages(Control control) {
		ControlDecorator dec = (ControlDecorator) decorators.get(control);
		dec.removeMessages();
		updateMessageContainer();
	}

	/**
	 * Removes all the local field messages and all the general container
	 * messages.
	 */

	public void removeAllMessages() {
		for (Enumeration enm = decorators.elements(); enm.hasMoreElements();) {
			ControlDecorator control = (ControlDecorator) enm.nextElement();
			control.removeMessages();
		}
		messages.clear();
		updateMessageContainer();
	}

	/*
	 * Adds the message if it does not already exist in the provided list.
	 */

	private void addMessage(String prefix, Object key, String messageText,
			int type, Object data, ArrayList list) {
		Message message = findMessage(key, list);
		if (message == null) {
			message = new Message(key, messageText, type, data);
			message.prefix = prefix;
			list.add(message);
		} else {
			message.message = messageText;
			message.type = type;
			message.data = data;
		}
	}

	/*
	 * Finds the message with the provided key in the provided list.
	 */

	private Message findMessage(Object key, ArrayList list) {
		for (int i = 0; i < list.size(); i++) {
			Message message = (Message) list.get(i);
			if (message.getKey().equals(key))
				return message;
		}
		return null;
	}

	/*
	 * Updates the entire container by building up a merged list that contains
	 * messages from each decorated field plus messages from the container
	 * itself.
	 */

	private void updateMessageContainer() {
		ArrayList mergedList = new ArrayList();
		mergedList.addAll(messages);
		for (Enumeration enm = decorators.elements(); enm.hasMoreElements();) {
			ControlDecorator dec = (ControlDecorator) enm.nextElement();
			dec.addAll(mergedList);
		}
		updateMessageContainer(messageContainer, mergedList, false);
	}

	/*
	 * This method works with a generic message container when a list of
	 * messages of various types need to be shown. The messages with the highest
	 * type are picked first. If there are more than one with this type, a
	 * multiple message is constructed; otherwise, the message is used as-is.
	 */

	private void updateMessageContainer(IMessageContainer container,
			ArrayList messages, boolean showAll) {
		if (messages.isEmpty() || messages == null) {
			container.setMessage(null, IMessageProvider.NONE);
			return;
		}
		int maxType = 0;
		// create a subset of messages with the highest type
		ArrayList peers = new ArrayList();
		for (int i = 0; i < messages.size(); i++) {
			Message message = (Message) messages.get(i);
			if (message.type > maxType) {
				peers.clear();
				maxType = message.type;
			}
			if (message.type == maxType)
				peers.add(message);
		}
		String messageText;
		String details = null;
		if (peers.size() == 1 && ((Message) peers.get(0)).prefix == null) {
			// a single message
			messageText = ((Message) peers.get(0)).message;
		} else {
			StringWriter sw = new StringWriter();
			PrintWriter out = new PrintWriter(sw);
			// StringBuffer sw = new StringBuffer();
			for (int i = 0; i < peers.size(); i++) {
				if (i > 0)
					out.println();
				Message m = (Message) peers.get(i);
				out.print(showAll ? m.message : m.getFullMessage());
			}
			out.flush();
			if (showAll)
				messageText = sw.toString();
			else {
				// show a summary message for the message
				// and list of errors for the details
				if (peers.size() > 1)
					messageText = Messages.bind(
							MULTIPLE_MESSAGE_SUMMARY_KEYS[maxType],
							new String[] { peers.size() + "" }); //$NON-NLS-1$
				else
					messageText = SINGLE_MESSAGE_SUMMARY_KEYS[maxType];
				details = sw.toString();
			}
		}
		if (container instanceof IMessageContainerWithDetails)
			((IMessageContainerWithDetails) container).setMessage(messageText,
					details, (IMessage[]) messages
							.toArray(new IMessage[messages.size()]), maxType);
		else
			container.setMessage(messageText, maxType);
	}
}