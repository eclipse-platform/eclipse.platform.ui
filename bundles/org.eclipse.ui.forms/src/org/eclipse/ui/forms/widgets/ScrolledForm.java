/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.IMessage;

/**
 * ScrolledForm is a control that is capable of scrolling an instance of the
 * Form class. It should be created in a parent that will allow it to use all
 * the available area (for example, a shell, a view or an editor).
 * <p>
 * Children of the form should typically be created using FormToolkit to match
 * the appearance and behaviour. When creating children, use a form body as a
 * parent by calling 'getBody()' on the form instance. Example:
 * 
 * <pre>
 * FormToolkit toolkit = new FormToolkit(parent.getDisplay());
 * ScrolledForm form = toolkit.createScrolledForm(parent);
 * form.setText(&quot;Sample form&quot;);
 * form.getBody().setLayout(new GridLayout());
 * toolkit.createButton(form.getBody(), &quot;Checkbox&quot;, SWT.CHECK);
 * </pre>
 * 
 * <p>
 * No layout manager has been set on the body. Clients are required to set the
 * desired layout manager explicitly.
 * <p>
 * Although the class is not final, it is not expected to be be extended.
 * 
 * @since 3.0
 */
public class ScrolledForm extends SharedScrolledComposite {
	private Form content;

	public ScrolledForm(Composite parent) {
		this(parent, SWT.V_SCROLL | SWT.H_SCROLL);
	}

	/**
	 * Creates the form control as a child of the provided parent.
	 * 
	 * @param parent
	 *            the parent widget
	 */
	public ScrolledForm(Composite parent, int style) {
		super(parent, style);
		super.setMenu(parent.getMenu());
		content = new Form(this, SWT.NULL);
		super.setContent(content);
		content.setMenu(getMenu());
	}

	/**
	 * Passes the menu to the body.
	 * 
	 * @param menu
	 */
	public void setMenu(Menu menu) {
		super.setMenu(menu);
		if (content != null)
			content.setMenu(menu);
	}

	/**
	 * Returns the title text that will be rendered at the top of the form.
	 * 
	 * @return the title text
	 */
	public String getText() {
		return content.getText();
	}

	/**
	 * Returns the title image that will be rendered to the left of the title.
	 * 
	 * @return the title image
	 */
	public Image getImage() {
		return content.getImage();
	}

	/**
	 * Sets the foreground color of the form. This color will also be used for
	 * the body.
	 */
	public void setForeground(Color fg) {
		super.setForeground(fg);
		content.setForeground(fg);
	}

	/**
	 * Sets the background color of the form. This color will also be used for
	 * the body.
	 */
	public void setBackground(Color bg) {
		super.setBackground(bg);
		content.setBackground(bg);
	}

	/**
	 * The form sets the content widget. This method should not be called by
	 * classes that instantiate this widget.
	 */
	public final void setContent(Control c) {
	}

	/**
	 * Sets the text to be rendered at the top of the form above the body as a
	 * title.
	 * <p>
	 * <strong>Note:</strong> Mnemonics are indicated by an '&amp;' that causes
	 * the next character to be the mnemonic. Mnemonics are not applicable in
	 * the case of the form title but need to be taken into acount due to the
	 * usage of the underlying widget that renders mnemonics in the title area.
	 * The mnemonic indicator character '&amp;' can be escaped by doubling it in
	 * the string, causing a single '&amp;' to be displayed.
	 * </p>
	 * 
	 * @param text
	 *            the title text
	 */
	public void setText(String text) {
		content.setText(text);
		reflow(true);
	}

	/**
	 * Sets the image to be rendered to the left of the title.
	 * 
	 * @param image
	 *            the title image or <code>null</code> for no image.
	 */
	public void setImage(Image image) {
		content.setImage(image);
		reflow(true);
	}

	/**
	 * Returns the optional background image of this form. The image is rendered
	 * starting at the position 0,0 and is painted behind the title.
	 * 
	 * @return Returns the background image.
	 */
	public Image getBackgroundImage() {
		return content.getBackgroundImage();
	}

	/**
	 * Sets the optional background image to be rendered behind the title
	 * starting at the position 0,0.
	 * 
	 * @param backgroundImage
	 *            The backgroundImage to set.
	 */
	public void setBackgroundImage(Image backgroundImage) {
		content.setBackgroundImage(backgroundImage);
	}

	/**
	 * Returns the tool bar manager that is used to manage tool items in the
	 * form's title area.
	 * 
	 * @return form tool bar manager
	 */
	public IToolBarManager getToolBarManager() {
		return content.getToolBarManager();
	}

	/**
	 * Updates the local tool bar manager if used. Does nothing if local tool
	 * bar manager has not been created yet.
	 */
	public void updateToolBar() {
		content.updateToolBar();
	}

	/**
	 * Returns the container that occupies the body of the form (the form area
	 * below the title). Use this container as a parent for the controls that
	 * should be in the form. No layout manager has been set on the form body.
	 * 
	 * @return Returns the body of the form.
	 */
	public Composite getBody() {
		return content.getBody();
	}

	/**
	 * Returns the instance of the form owned by the scrolled form.
	 * 
	 * @return the form instance
	 */
	public Form getForm() {
		return content;
	}

	/**
	 * Sets the form's busy state. Busy form will display 'busy' animation in
	 * the area of the title image.
	 * 
	 * @param busy
	 *            the form's busy state
	 * @see Form#setBusy(boolean)
	 * @since 3.3
	 */

	public void setBusy(boolean busy) {
		content.setBusy(busy);
		reflow(true);
	}

	/**
	 * Sets the optional head client.
	 * 
	 * @param headClient
	 *            the optional child of the head
	 * @see Form#setHeadClient(Control)
	 * @since 3.3
	 */
	public void setHeadClient(Control headClient) {
		content.setHeadClient(headClient);
		reflow(true);
	}

	/**
	 * Sets the form message.
	 * 
	 * @param newMessage
	 *            the message text or <code>null</code> to reset.
	 * @param newType
	 *            as defined in
	 *            {@link org.eclipse.jface.dialogs.IMessageProvider}.
	 * @param messages
	 * 			 an optional array of children that itemize individual
	 * 			messages or <code>null</code> for a simple message.
	 * @since 3.3
	 * @see Form#setMessage(String, int)
	 */
	public void setMessage(String newMessage, int newType, IMessage[] messages) {
		content.setMessage(newMessage, newType, messages);
		reflow(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IMessageContainer#setMessage(java.lang.String,
	 *      int)
	 */
	public void setMessage(String newMessage, int newType) {
		this.setMessage(newMessage, newType, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessage()
	 */
	public String getMessage() {
		return content.getMessage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessageType()
	 */
	public int getMessageType() {
		return content.getMessageType();
	}
}
