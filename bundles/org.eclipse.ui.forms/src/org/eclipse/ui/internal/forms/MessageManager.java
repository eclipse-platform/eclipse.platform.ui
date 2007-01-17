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

package org.eclipse.ui.internal.forms;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IMessageContainer;
import org.eclipse.ui.forms.IMessageManager;

/**
 * @see IMessageManager
 */

public class MessageManager implements IMessageManager {
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

	class Message {
		Object key;
		String message;
		int type;
		String prefix;

		Message(Object key, String message, int type) {
			this.key = key;
			this.message = message;
			this.type = type;
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

	class ControlDecorator implements IMessageContainer {
		private ControlDecoration decoration;
		private ArrayList controlMessages = new ArrayList();
		private String message;
		private int type;
		private String prefix;

		ControlDecorator(Control control) {
			this.decoration = new ControlDecoration(control, SWT.LEFT
					| SWT.BOTTOM);
		}

		public void dispose() {
			decoration.dispose();
		}

		public boolean isDisposed() {
			return decoration.getControl() == null;
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

		void addMessage(Object key, String text, int type) {
			MessageManager.this.addMessage(getPrefix(), key, text, type,
					controlMessages);
			updateMessageContainer(this, controlMessages, true);
		}

		void removeMessage(Object key) {
			Message message = findMessage(key, controlMessages);
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
		public void setMessage(String newMessage, String details, int newType) {
			if (this.message != null && newMessage != null
					&& newMessage.equals(this.message) && newType == this.type)
				return;
			this.message = newMessage;
			this.type = newType;
			update();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#addMessage(java.lang.Object,
	 *      java.lang.String, int)
	 */
	public void addMessage(Object key, String messageText, int type) {
		addMessage(null, key, messageText, type, messages);
		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#addMessage(java.lang.Object,
	 *      java.lang.String, int, org.eclipse.swt.widgets.Control)
	 */
	public void addMessage(Object key, String messageText, int type,
			Control control) {
		ControlDecorator dec = (ControlDecorator) decorators.get(control);

		if (dec == null) {
			dec = new ControlDecorator(control);
			decorators.put(control, dec);
		}
		dec.addMessage(key, messageText, type);
		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#removeMessage(java.lang.Object)
	 */
	public void removeMessage(Object key) {
		Message message = findMessage(key, messages);
		if (message != null) {
			messages.remove(message);
			update();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#removeMessages()
	 */
	public void removeMessages() {
		messages.clear();
		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#removeMessage(java.lang.Object,
	 *      org.eclipse.swt.widgets.Control)
	 */
	public void removeMessage(Object key, Control control) {
		ControlDecorator dec = (ControlDecorator) decorators.get(control);
		if (dec == null)
			return;
		dec.removeMessage(key);
		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#removeMessages(org.eclipse.swt.widgets.Control)
	 */
	public void removeMessages(Control control) {
		ControlDecorator dec = (ControlDecorator) decorators.get(control);
		dec.removeMessages();
		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#removeAllMessages()
	 */
	public void removeAllMessages() {
		for (Enumeration enm = decorators.elements(); enm.hasMoreElements();) {
			ControlDecorator control = (ControlDecorator) enm.nextElement();
			control.removeMessages();
		}
		messages.clear();
		update();
	}

	/*
	 * Adds the message if it does not already exist in the provided list.
	 */

	private void addMessage(String prefix, Object key, String messageText,
			int type, ArrayList list) {
		Message message = findMessage(key, list);
		if (message == null) {
			message = new Message(key, messageText, type);
			message.prefix = prefix;
			list.add(message);
		} else {
			message.message = messageText;
			message.type = type;
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
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#update()
	 */
	public void update() {
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
		pruneControlDecorators();
		if (messages.isEmpty() || messages == null) {
			container.setMessage(null, null, IMessageProvider.NONE);
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
		container.setMessage(messageText, details, maxType);
	}

	private void pruneControlDecorators() {
		for (Iterator iter = decorators.values().iterator(); iter.hasNext();) {
			ControlDecorator dec = (ControlDecorator) iter.next();
			if (dec.isDisposed())
				iter.remove();
		}
	}
}