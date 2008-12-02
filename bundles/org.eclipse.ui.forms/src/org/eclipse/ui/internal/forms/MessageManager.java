/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Cabe (benjamin.cabe@anyware-tech.com) - patch (see Bugzilla #255466) 
 ******************************************************************************/

package org.eclipse.ui.internal.forms;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.IMessagePrefixProvider;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * @see IMessageManager
 */

public class MessageManager implements IMessageManager {
	private static final DefaultPrefixProvider DEFAULT_PREFIX_PROVIDER = new DefaultPrefixProvider();
	private ArrayList messages = new ArrayList();
	private ArrayList oldMessages;
	private Hashtable decorators = new Hashtable();
	private Hashtable oldDecorators;
	private boolean autoUpdate = true;
	private Form form;
	private IMessagePrefixProvider prefixProvider = DEFAULT_PREFIX_PROVIDER;
	private int decorationPosition = SWT.LEFT | SWT.BOTTOM;
	private static FieldDecoration standardError = FieldDecorationRegistry
			.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
	private static FieldDecoration standardWarning = FieldDecorationRegistry
			.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_WARNING);
	private static FieldDecoration standardInformation = FieldDecorationRegistry
	.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION);

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

	static class Message implements IMessage {
		Control control;
		Object data;
		Object key;
		String message;
		int type;
		String prefix;

		Message(Object key, String message, int type, Object data) {
			this.key = key;
			this.message = message;
			this.type = type;
			this.data = data;
		}
		
		private Message(Message message) {
			this.key = message.key;
			this.message = message.message;
			this.type = message.type;
			this.data = message.data;
			this.prefix = message.prefix;
			this.control = message.control;
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.forms.messages.IMessage#getControl()
		 */
		public Control getControl() {
			return control;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.forms.messages.IMessage#getData()
		 */
		public Object getData() {
			return data;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.forms.messages.IMessage#getPrefix()
		 */
		public String getPrefix() {
			return prefix;
		}
		
		public boolean equals(Object obj) {
			if (!(obj instanceof Message))
				return false;
			Message msg = (Message) obj;
			return (msg.getPrefix() == null ? getPrefix() == null : msg.getPrefix().equals(getPrefix())) &&
					(msg.getControl() == null ? getControl() == null : msg.getControl().equals(getControl())) &&
					(msg.getMessageType() == getMessageType()) &&
					(msg.getMessage() == null ? getMessage() == null : msg.getMessage().equals(getMessage())) &&
					msg.getKey().equals(getKey());
		}
	}

	static class DefaultPrefixProvider implements IMessagePrefixProvider {

		public String getPrefix(Control c) {
			Composite parent = c.getParent();
			Control[] siblings = parent.getChildren();
			for (int i = 0; i < siblings.length; i++) {
				if (siblings[i] == c) {
					// this is us - go backward until you hit
					// a label-like widget
					for (int j = i - 1; j >= 0; j--) {
						Control label = siblings[j];
						String ltext = null;
						if (label instanceof Label) {
							ltext = ((Label) label).getText();
						} else if (label instanceof Hyperlink) {
							ltext = ((Hyperlink) label).getText();
						} else if (label instanceof CLabel) {
							ltext = ((CLabel) label).getText();
						}
						if (ltext != null) {
							if (!ltext.endsWith(":")) //$NON-NLS-1$
								return ltext + ": "; //$NON-NLS-1$
							return ltext + " "; //$NON-NLS-1$
						}
					}
					break;
				}
			}
			return null;
		}
	}

	class ControlDecorator {
		private ControlDecoration decoration;
		private ArrayList controlMessages = new ArrayList();
		private String prefix;

		ControlDecorator(Control control) {
			this.decoration = new ControlDecoration(control, decorationPosition, form.getBody());
		}
		
		private ControlDecorator (ControlDecorator cd) {
			this.decoration = cd.decoration;
			this.prefix = cd.prefix;
			for (Iterator i = cd.controlMessages.iterator(); i.hasNext();)
				this.controlMessages.add(new Message((Message)i.next()));
		}

		public boolean isDisposed() {
			return decoration.getControl() == null;
		}

		void updatePrefix() {
			prefix = null;
		}

		void updatePosition() {
			Control control = decoration.getControl();
			decoration.dispose();
			this.decoration = new ControlDecoration(control, decorationPosition, form.getBody());
			update();
		}

		String getPrefix() {
			if (prefix == null)
				createPrefix();
			return prefix;
		}

		private void createPrefix() {
			if (prefixProvider == null) {
				prefix = ""; //$NON-NLS-1$
				return;
			}
			prefix = prefixProvider.getPrefix(decoration.getControl());
			if (prefix == null)
				// make a prefix anyway
				prefix = ""; //$NON-NLS-1$
		}

		void addAll(ArrayList target) {
			target.addAll(controlMessages);
		}

		void addMessage(Object key, String text, Object data, int type) {
			Message message = MessageManager.this.addMessage(getPrefix(), key,
					text, data, type, controlMessages);
			message.control = decoration.getControl();
			if (isAutoUpdate())
				update();
		}

		boolean removeMessage(Object key) {
			Message message = findMessage(key, controlMessages);
			if (message != null) {
				controlMessages.remove(message);
				if (isAutoUpdate())
					update();
			}
			return message != null;
		}

		boolean removeMessages() {
			if (controlMessages.isEmpty())
				return false;
			controlMessages.clear();
			if (isAutoUpdate())
				update();
			return true;
		}

		public void update() {
			if (controlMessages.isEmpty()) {
				decoration.setDescriptionText(null);
				decoration.hide();
			} else {
				ArrayList peers = createPeers(controlMessages);
				int type = ((IMessage) peers.get(0)).getMessageType();
				String description = createDetails(createPeers(peers), true);
				if (type == IMessageProvider.ERROR)
					decoration.setImage(standardError.getImage());
				else if (type == IMessageProvider.WARNING)
					decoration.setImage(standardWarning.getImage());
				else if (type == IMessageProvider.INFORMATION)
					decoration.setImage(standardInformation.getImage());
				decoration.setDescriptionText(description);
				decoration.show();
			}
		}
		
		public boolean equals(Object obj) {
			if (!(obj instanceof ControlDecorator))
				return false;
			ControlDecorator cd = (ControlDecorator) obj;
			if (!cd.decoration.equals(decoration))
				return false;
			return cd.getPrefix().equals(getPrefix());
		}
		
		boolean hasSameMessages(ControlDecorator cd) {
			if (cd.controlMessages.size() != controlMessages.size())
				return false;
			if (!cd.controlMessages.containsAll(controlMessages))
				return false;
			return true;
		}
	}

	/**
	 * Creates a new instance of the message manager that will work with the
	 * provided form.
	 * 
	 * @param scrolledForm
	 *            the form to control
	 */
	public MessageManager(ScrolledForm scrolledForm) {
		this.form = scrolledForm.getForm();
	}

	/**
	 * Creates a new instance of the message manager that will work with the
	 * provided form.
	 * 
	 * @param form
	 *            the form to control
	 * @since org.eclipse.ui.forms 3.4
	 */
	public MessageManager(Form form) {
		this.form = form;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#addMessage(java.lang.Object,
	 *      java.lang.String, int)
	 */
	public void addMessage(Object key, String messageText, Object data, int type) {
		addMessage(null, key, messageText, data, type, messages);
		if (isAutoUpdate())
			updateForm();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#addMessage(java.lang.Object,
	 *      java.lang.String, int, org.eclipse.swt.widgets.Control)
	 */
	public void addMessage(Object key, String messageText, Object data,
			int type, Control control) {
		ControlDecorator dec = (ControlDecorator) decorators.get(control);

		if (dec == null) {
			dec = new ControlDecorator(control);
			decorators.put(control, dec);
		}
		dec.addMessage(key, messageText, data, type);
		if (isAutoUpdate())
			updateForm();
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
			if (isAutoUpdate())
				updateForm();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#removeMessages()
	 */
	public void removeMessages() {
		if (!messages.isEmpty()) {
			messages.clear();
			if (isAutoUpdate())
				updateForm();
		}
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
		if (dec.removeMessage(key))
			if (isAutoUpdate())
				updateForm();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#removeMessages(org.eclipse.swt.widgets.Control)
	 */
	public void removeMessages(Control control) {
		ControlDecorator dec = (ControlDecorator) decorators.get(control);
		if (dec != null) {
			if (dec.removeMessages()) {
				if (isAutoUpdate())
					updateForm();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#removeAllMessages()
	 */
	public void removeAllMessages() {
		boolean needsUpdate = false;
		for (Enumeration enm = decorators.elements(); enm.hasMoreElements();) {
			ControlDecorator control = (ControlDecorator) enm.nextElement();
			if (control.removeMessages())
				needsUpdate = true;
		}
		if (!messages.isEmpty()) {
			messages.clear();
			needsUpdate = true;
		}
		if (needsUpdate && isAutoUpdate())
			updateForm();
	}

	/*
	 * Adds the message if it does not already exist in the provided list.
	 */

	private Message addMessage(String prefix, Object key, String messageText,
			Object data, int type, ArrayList list) {
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
		return message;
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
		// Update decorations
		for (Iterator iter = decorators.values().iterator(); iter.hasNext();) {
			ControlDecorator dec = (ControlDecorator) iter.next();
			dec.update();
		}
		// Update the form
		updateForm();
	}

	/*
	 * Updates the container by rolling the messages up from the controls.
	 */

	private void updateForm() {
		ArrayList mergedList = new ArrayList();
		mergedList.addAll(messages);
		for (Enumeration enm = decorators.elements(); enm.hasMoreElements();) {
			ControlDecorator dec = (ControlDecorator) enm.nextElement();
			dec.addAll(mergedList);
		}
		update(mergedList);
	}

	private void update(ArrayList mergedList) {
		pruneControlDecorators();
		if (form.getHead().getBounds().height == 0 || mergedList.isEmpty() || mergedList == null) {
			form.setMessage(null, IMessageProvider.NONE);
			return;
		}
		ArrayList peers = createPeers(mergedList);
		int maxType = ((IMessage) peers.get(0)).getMessageType();
		String messageText;
		IMessage[] array = (IMessage[]) peers
				.toArray(new IMessage[peers.size()]);
		if (peers.size() == 1 && ((Message) peers.get(0)).prefix == null) {
			// a single message
			IMessage message = (IMessage) peers.get(0);
			messageText = message.getMessage();
			form.setMessage(messageText, maxType, array);
		} else {
			// show a summary message for the message
			// and list of errors for the details
			if (peers.size() > 1)
				messageText = Messages.bind(
						MULTIPLE_MESSAGE_SUMMARY_KEYS[maxType],
						new String[] { peers.size() + "" }); //$NON-NLS-1$
			else
				messageText = SINGLE_MESSAGE_SUMMARY_KEYS[maxType];
			form.setMessage(messageText, maxType, array);
		}
	}

	private static String getFullMessage(IMessage message) {
		if (message.getPrefix() == null)
			return message.getMessage();
		return message.getPrefix() + message.getMessage();
	}

	private ArrayList createPeers(ArrayList messages) {
		ArrayList peers = new ArrayList();
		int maxType = 0;
		for (int i = 0; i < messages.size(); i++) {
			Message message = (Message) messages.get(i);
			if (message.type > maxType) {
				peers.clear();
				maxType = message.type;
			}
			if (message.type == maxType)
				peers.add(message);
		}
		return peers;
	}

	private String createDetails(ArrayList messages, boolean excludePrefix) {
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);

		for (int i = 0; i < messages.size(); i++) {
			if (i > 0)
				out.println();
			IMessage m = (IMessage) messages.get(i);
			out.print(excludePrefix ? m.getMessage() : getFullMessage(m));
		}
		out.flush();
		return sw.toString();
	}

	public static String createDetails(IMessage[] messages) {
		if (messages == null || messages.length == 0)
			return null;
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);

		for (int i = 0; i < messages.length; i++) {
			if (i > 0)
				out.println();
			out.print(getFullMessage(messages[i]));
		}
		out.flush();
		return sw.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#createSummary(org.eclipse.ui.forms.IMessage[])
	 */
	public String createSummary(IMessage[] messages) {
		return createDetails(messages);
	}

	private void pruneControlDecorators() {
		for (Iterator iter = decorators.values().iterator(); iter.hasNext();) {
			ControlDecorator dec = (ControlDecorator) iter.next();
			if (dec.isDisposed())
				iter.remove();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#getMessagePrefixProvider()
	 */
	public IMessagePrefixProvider getMessagePrefixProvider() {
		return prefixProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#setMessagePrefixProvider(org.eclipse.ui.forms.IMessagePrefixProvider)
	 */
	public void setMessagePrefixProvider(IMessagePrefixProvider provider) {
		this.prefixProvider = provider;
		for (Iterator iter = decorators.values().iterator(); iter.hasNext();) {
			ControlDecorator dec = (ControlDecorator) iter.next();
			dec.updatePrefix();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#getDecorationPosition()
	 */
	public int getDecorationPosition() {
		return decorationPosition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#setDecorationPosition(int)
	 */
	public void setDecorationPosition(int position) {
		this.decorationPosition = position;
		for (Iterator iter = decorators.values().iterator(); iter.hasNext();) {
			ControlDecorator dec = (ControlDecorator) iter.next();
			dec.updatePosition();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#isAutoUpdate()
	 */
	public boolean isAutoUpdate() {
		return autoUpdate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageManager#setAutoUpdate(boolean)
	 */
	public void setAutoUpdate(boolean autoUpdate) {
		boolean needsCaching = this.autoUpdate && !autoUpdate;
		boolean needsUpdate = !this.autoUpdate && autoUpdate;
		this.autoUpdate = autoUpdate;
		if (needsUpdate && isCacheChanged())
			update();
		if (needsCaching) {
			oldMessages = new ArrayList();
			for (Iterator i = messages.iterator(); i.hasNext();)
				oldMessages.add(new Message((Message)i.next()));
			oldDecorators = new Hashtable();
			for (Enumeration e = decorators.keys(); e.hasMoreElements();) {
				Object key = e.nextElement();
				oldDecorators.put(key, new ControlDecorator((ControlDecorator)decorators.get(key)));
			}
		}
	}
	
	private boolean isCacheChanged() {
		boolean result = false;
		result = checkMessageCache() || checkDecoratorCache();
		oldMessages.clear();
		oldMessages = null;
		oldDecorators.clear();
		oldDecorators = null;
		return result;
	}
	
	private boolean checkMessageCache() {
		if (oldMessages == null)
			return false;
		if (messages.size() != oldMessages.size())
			return true;
		if (!oldMessages.containsAll(messages))
			return true;
		return false;
	}
	
	private boolean checkDecoratorCache() {
		if (oldDecorators == null)
			return false;
		for (Iterator i = decorators.entrySet().iterator(); i.hasNext();) {
			Entry next = (Entry)i.next();
			ControlDecorator cd = (ControlDecorator)next.getValue();
			ControlDecorator oldCd = (ControlDecorator) oldDecorators.get(cd.decoration.getControl());
			if ((oldCd == null && cd.controlMessages.size() > 0) || (oldCd != null && !cd.hasSameMessages(oldCd)))
				return true;
		}
		return false;
	}
}