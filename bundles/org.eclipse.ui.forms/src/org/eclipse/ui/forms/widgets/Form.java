/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.internal.forms.MessageManager;
import org.eclipse.ui.internal.forms.widgets.FormHeading;
import org.eclipse.ui.internal.forms.widgets.FormUtil;

/**
 * Form is a custom control that renders a title and an optional background
 * image above the body composite. It can be used alone when part of parents
 * that are scrolled. If scrolling is required, use <code>ScrolledForm</code>
 * instead because it has an instance of <code>Form</code> and adds scrolling
 * capability.
 * <p>
 * Form can have a title if set. If not set, title area will not be left empty -
 * form body will be resized to fill the entire form. In addition, an optional
 * title image can be set and is rendered to the left of the title (since 3.2).
 * <p>
 * Form can have a title drop down menu if the menu bar manager is not empty
 * (since 3.3).
 * <p>
 * Form title can support drag and drop if drag and drop support methods are
 * invoked. When used, additional decoration is rendered behind the title to
 * reinforce the drag and drop ability (since 3.3).
 * <p>
 * The form supports status messages. These messages can have various severity
 * (error, warning, info or none). If status hyperlink handler is specified, the
 * messages with the specified severity other than none will be rendered as
 * hyperlinks.
 * <p>
 * Form can have a background image behind the title text. The image is tiled as
 * many times as needed to fill the title area. Alternatively, gradient
 * background can be painted vertically or horizontally.
 * <p>
 * Form can be put in a 'busy' state. While in this state, title image is
 * replaced with an animation that lasts as long as the 'busy' state is active.
 * <p>
 * It is possible to create an optional head client control. When created, this
 * control is placed in the form heading as a second row.
 * <p>
 * Form has a custom layout manager that is wrap-enabled. If a form is placed in
 * a composite whose layout manager implements ILayoutExtension, the body of the
 * form will participate in wrapping as long as its layout manager implements
 * ILayoutExtension as well.
 * <p>
 * Children of the form should typically be created using FormToolkit to match
 * the appearance and behaviour. When creating children, use the form body as a
 * parent by calling 'getBody()' on the form instance. Example:
 * 
 * <pre>
 * FormToolkit toolkit = new FormToolkit(parent.getDisplay());
 * Form form = toolkit.createForm(parent);
 * form.setText(&quot;Sample form&quot;);
 * form.getBody().setLayout(new GridLayout());
 * toolkit.createButton(form.getBody(), &quot;Checkbox&quot;, SWT.CHECK);
 * </pre>
 * 
 * <p>
 * No layout manager has been set on the body. Clients are required to set the
 * desired layout manager explicitly.
 * <p>
 * Although the class is not final, it should not be subclassed.
 * 
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class Form extends Composite {
	private FormHeading head;

	private Composite body;

	private SizeCache bodyCache = new SizeCache();

	private SizeCache headCache = new SizeCache();

	private FormText selectionText;

	private MessageManager messageManager;

	private class FormLayout extends Layout implements ILayoutExtension {
		public int computeMinimumWidth(Composite composite, boolean flushCache) {
			return computeSize(composite, 5, SWT.DEFAULT, flushCache).x;
		}

		public int computeMaximumWidth(Composite composite, boolean flushCache) {
			return computeSize(composite, SWT.DEFAULT, SWT.DEFAULT, flushCache).x;
		}

		public Point computeSize(Composite composite, int wHint, int hHint,
				boolean flushCache) {
			if (flushCache) {
				bodyCache.flush();
				headCache.flush();
			}
			bodyCache.setControl(body);
			headCache.setControl(head);

			int width = 0;
			int height = 0;

			Point hsize = headCache.computeSize(FormUtil.getWidthHint(wHint,
					head), SWT.DEFAULT);
			width = Math.max(hsize.x, width);
			height = hsize.y;
			
			boolean ignoreBody=getData(FormUtil.IGNORE_BODY)!=null;
			
			Point bsize;
			if (ignoreBody)
				bsize = new Point(0,0);
			else
				bsize = bodyCache.computeSize(FormUtil.getWidthHint(wHint,
					body), SWT.DEFAULT);
			width = Math.max(bsize.x, width);
			height += bsize.y;
			return new Point(width, height);
		}

		protected void layout(Composite composite, boolean flushCache) {
			if (flushCache) {
				bodyCache.flush();
				headCache.flush();
			}
			bodyCache.setControl(body);
			headCache.setControl(head);
			Rectangle carea = composite.getClientArea();

			Point hsize = headCache.computeSize(carea.width, SWT.DEFAULT);
			headCache.setBounds(0, 0, carea.width, hsize.y);
			bodyCache
					.setBounds(0, hsize.y, carea.width, carea.height - hsize.y);
		}
	}

	/**
	 * Creates the form content control as a child of the provided parent.
	 * 
	 * @param parent
	 *            the parent widget
	 */
	public Form(Composite parent, int style) {
		super(parent, SWT.NO_BACKGROUND | style);
		super.setLayout(new FormLayout());
		head = new FormHeading(this, SWT.NULL);
		head.setMenu(parent.getMenu());
		body = new LayoutComposite(this, SWT.NULL);
		body.setMenu(parent.getMenu());
	}

	/**
	 * Passes the menu to the form body.
	 * 
	 * @param menu
	 *            the parent menu
	 */
	public void setMenu(Menu menu) {
		super.setMenu(menu);
		head.setMenu(menu);
		body.setMenu(menu);
	}

	/**
	 * Fully delegates the size computation to the internal layout manager.
	 */
	public final Point computeSize(int wHint, int hHint, boolean changed) {
		return ((FormLayout) getLayout()).computeSize(this, wHint, hHint,
				changed);
	}

	/**
	 * Prevents from changing the custom control layout.
	 */
	public final void setLayout(Layout layout) {
	}

	/**
	 * Returns the title text that will be rendered at the top of the form.
	 * 
	 * @return the title text
	 */
	public String getText() {
		return head.getText();
	}

	/**
	 * Returns the title image that will be rendered to the left of the title.
	 * 
	 * @return the title image or <code>null</code> if not set.
	 * @since 3.2
	 */
	public Image getImage() {
		return head.getImage();
	}

	/**
	 * Sets the foreground color of the form. This color will also be used for
	 * the body.
	 * 
	 * @param fg
	 *            the foreground color
	 */
	public void setForeground(Color fg) {
		super.setForeground(fg);
		head.setForeground(fg);
		body.setForeground(fg);
	}

	/**
	 * Sets the background color of the form. This color will also be used for
	 * the body.
	 * 
	 * @param bg
	 *            the background color
	 */
	public void setBackground(Color bg) {
		super.setBackground(bg);
		head.setBackground(bg);
		body.setBackground(bg);
	}

	/**
	 * Sets the font of the header text.
	 * 
	 * @param font
	 *            the new font
	 */
	public void setFont(Font font) {
		super.setFont(font);
		head.setFont(font);
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
		head.setText(text);
		layout();
		redraw();
	}

	/**
	 * Sets the image to be rendered to the left of the title. This image will
	 * be temporarily hidden in two cases:
	 * 
	 * <ol>
	 * <li>When the form is busy - replaced with a busy animation</li>
	 * <li>When the form has message set - replaced with the image indicating
	 * message severity</li>
	 * </ol>
	 * 
	 * @param image
	 *            the title image or <code>null</code> to show no image.
	 * @since 3.2
	 */
	public void setImage(Image image) {
		head.setImage(image);
		layout();
		redraw();
	}

	/**
	 * Sets the background colors to be painted behind the title text in a
	 * gradient. Note that this method will reset color previously set by
	 * {@link #setBackground(Color)}. This is necessary for the simulated
	 * transparency of the heading in all of its children control.
	 * 
	 * @param gradientColors
	 *            the array of colors that form the gradient
	 * @param percents
	 *            the partition of the overall space between the gradient colors
	 * @param vertical
	 *            of <code>true</code>, the gradient will be rendered
	 *            vertically, if <code>false</code> the orientation will be
	 *            horizontal.
	 */

	public void setTextBackground(Color[] gradientColors, int[] percents,
			boolean vertical) {
		head.setTextBackground(gradientColors, percents, vertical);
	}

	/**
	 * Returns the optional background image of the form head.
	 * 
	 * @return the background image or <code>null</code> if not specified.
	 */
	public Image getBackgroundImage() {
		return head.getHeadingBackgroundImage();
	}

	/**
	 * Sets the optional background image to be rendered behind the title
	 * starting at the position 0,0. If the image is smaller than the container
	 * in any dimension, it will be tiled.
	 * 
	 * As of version 3.2, this method only supports SWT.BITMAP image types. This is
	 * because the rendering is now delegated to SWT which imposes this restriction
	 * on background images, 
	 * 
	 * @param backgroundImage
	 *            the head background image.
	 * 
	 */
	public void setBackgroundImage(Image backgroundImage) {
		head.setHeadingBackgroundImage(backgroundImage);
	}

	/**
	 * Returns the tool bar manager that is used to manage tool items in the
	 * form's title area.
	 * 
	 * @return form tool bar manager
	 */
	public IToolBarManager getToolBarManager() {
		return head.getToolBarManager();
	}

	/**
	 * Sets the tool bar vertical alignment relative to the header. Can be
	 * useful when there is more free space at the second row (with the head
	 * client).
	 * 
	 * @param alignment
	 *            SWT.TOP or SWT.BOTTOM
	 * @since 3.3
	 */

	public void setToolBarVerticalAlignment(int alignment) {
		head.setToolBarAlignment(alignment);
	}

	/**
	 * Returns the current tool bar alignment (if used).
	 * 
	 * @return SWT.TOP or SWT.BOTTOM
	 * @since 3.3
	 */

	public int getToolBarVerticalAlignment() {
		return head.getToolBarAlignment();
	}

	/**
	 * Returns the menu manager that is used to manage title area drop-down menu
	 * items.
	 * 
	 * @return title area drop-down menu manager
	 * @since 3.3
	 */
	public IMenuManager getMenuManager() {
		return head.getMenuManager();
	}

	/**
	 * Updates the local tool bar manager if used. Does nothing if local tool
	 * bar manager has not been created yet.
	 */
	public void updateToolBar() {
		head.updateToolBar();
	}

	/**
	 * Returns the container that occupies the head of the form (the form area
	 * above the body). Use this container as a parent for the head client.
	 * 
	 * @return the head of the form.
	 * @since 3.2
	 */
	public Composite getHead() {
		return head;
	}

	/**
	 * Returns the optional head client if set.
	 * 
	 * @return the head client or <code>null</code> if not set.
	 * @see #setHeadClient(Control)
	 * @since 3.2
	 */
	public Control getHeadClient() {
		return head.getHeadClient();
	}

	/**
	 * Sets the optional head client. Head client is placed after the form
	 * title. This option causes the tool bar to be placed in the second raw of
	 * the header (below the head client).
	 * <p>
	 * The head client must be a child of the composite returned by
	 * <code>getHead()</code> method.
	 * 
	 * @param headClient
	 *            the optional child of the head
	 * @since 3.2
	 */
	public void setHeadClient(Control headClient) {
		head.setHeadClient(headClient);
		layout();
	}

	/**
	 * Returns the container that occupies the body of the form (the form area
	 * below the title). Use this container as a parent for the controls that
	 * should be in the form. No layout manager has been set on the form body.
	 * 
	 * @return Returns the body of the form.
	 */
	public Composite getBody() {
		return body;
	}

	/**
	 * Tests if the background image is tiled to cover the entire area of the
	 * form heading.
	 * 
	 * @return <code>true</code> if heading background image is tiled,
	 *         <code>false</code> otherwise.
	 */
	public boolean isBackgroundImageTiled() {
		return head.isBackgroundImageTiled();
	}

	/**
	 * Sets whether the header background image is repeated to cover the entire
	 * heading area or not.
	 * 
	 * @param backgroundImageTiled
	 *            set <code>true</code> to tile the image, or
	 *            <code>false</code> to paint the background image only once
	 *            at 0,0
	 */
	public void setBackgroundImageTiled(boolean backgroundImageTiled) {
		head.setBackgroundImageTiled(backgroundImageTiled);
	}

	/**
	 * Returns the background image alignment.
	 * 
	 * @deprecated due to the underlying widget limitations, background image is
	 *             either painted at 0,0 and/or tiled.
	 * @return SWT.LEFT
	 */
	public int getBackgroundImageAlignment() {
		return SWT.LEFT;
	}

	/**
	 * Sets the background image alignment.
	 * 
	 * @deprecated due to the underlying widget limitations, background image is
	 *             always tiled and alignment cannot be controlled.
	 * @param backgroundImageAlignment
	 *            The backgroundImageAlignment to set.
	 * @since 3.1
	 */
	public void setBackgroundImageAlignment(int backgroundImageAlignment) {
	}

	/**
	 * Tests if background image is clipped.
	 * 
	 * @deprecated due to the underlying widget limitations, background image is
	 *             always clipped.
	 * @return true
	 * @since 3.1
	 */
	public boolean isBackgroundImageClipped() {
		return true;
	}

	/**
	 * Sets whether the background image is clipped.
	 * 
	 * @deprecated due to the underlying widget limitations, background image is
	 *             always clipped.
	 * @param backgroundImageClipped
	 *            the value to set
	 * @since 3.1
	 */
	public void setBackgroundImageClipped(boolean backgroundImageClipped) {
	}

	/**
	 * Tests if the form head separator is visible.
	 * 
	 * @return <code>true</code> if the head/body separator is visible,
	 *         <code>false</code> otherwise
	 * @since 3.2
	 */
	public boolean isSeparatorVisible() {
		return head.isSeparatorVisible();
	}

	/**
	 * If set, adds a separator between the head and body. Since 3.3, the colors
	 * that are used to render it are {@link IFormColors#H_BOTTOM_KEYLINE1} and
	 * {@link IFormColors#H_BOTTOM_KEYLINE2}.
	 * 
	 * @param addSeparator
	 *            <code>true</code> to make the separator visible,
	 *            <code>false</code> otherwise.
	 * @since 3.2
	 */
	public void setSeparatorVisible(boolean addSeparator) {
		head.setSeparatorVisible(addSeparator);
	}

	/**
	 * Returns the color used to render the optional head separator. If gradient
	 * text background is used additional colors from the gradient will be used
	 * to render the separator.
	 * 
	 * @return separator color or <code>null</code> if not set.
	 * @since 3.2
	 * @deprecated use <code>getHeadColor(IFormColors.H_BOTTOM_KEYLINE2)</code>
	 */

	public Color getSeparatorColor() {
		return head.getColor(IFormColors.H_BOTTOM_KEYLINE2);
	}

	/**
	 * Sets the color to be used to render the optional head separator.
	 * 
	 * @param separatorColor
	 *            the color to render the head separator or <code>null</code>
	 *            to use the default color.
	 * @since 3.2
	 * @deprecated use
	 *             <code>setHeadColor(IFormColors.H_BOTTOM_KEYLINE2, separatorColor)</code>
	 */
	public void setSeparatorColor(Color separatorColor) {
		head.putColor(IFormColors.H_BOTTOM_KEYLINE2, separatorColor);
	}

	/**
	 * Sets the color used to paint an aspect of the form heading.
	 * 
	 * @param key
	 *            a valid form heading color key as defined in
	 *            {@link IFormColors}. Relevant keys all start with an H_
	 *            prefix.
	 * @param color
	 *            the color to use for the provided key
	 * @since 3.3
	 */

	public void setHeadColor(String key, Color color) {
		head.putColor(key, color);
	}

	/**
	 * Returns the color that is currently use to paint an aspect of the form
	 * heading, or <code>null</code> if not defined.
	 * 
	 * @param key
	 *            the color key
	 * @return the color object or <code>null</code> if not set.
	 * @since 3.3
	 */

	public Color getHeadColor(String key) {
		return head.getColor(key);
	}

	/**
	 * Sets the message for this form. Message text is rendered in the form head
	 * when shown.
	 * 
	 * @param message
	 *            the message, or <code>null</code> to clear the message
	 * @see #setMessage(String, int)
	 * @since 3.2
	 */
	public void setMessage(String message) {
		this.setMessage(message, 0, null);
	}

	/**
	 * Sets the message for this form with an indication of what type of message
	 * it is.
	 * <p>
	 * The valid message types are one of <code>NONE</code>,
	 * <code>INFORMATION</code>,<code>WARNING</code>, or
	 * <code>ERROR</code> defined in IMessageProvider interface.
	 * </p>
	 * 
	 * @param newMessage
	 *            the message, or <code>null</code> to clear the message
	 * @param newType
	 *            the message type
	 * @see org.eclipse.jface.dialogs.IMessageProvider
	 * @since 3.2
	 */

	public void setMessage(String newMessage, int newType) {
		this.setMessage(newMessage, newType, null);
	}

	/**
	 * Sets the message for this form with an indication of what type of message
	 * it is.
	 * <p>
	 * The valid message types are one of <code>NONE</code>,
	 * <code>INFORMATION</code>,<code>WARNING</code>, or
	 * <code>ERROR</code> defined in IMessageProvider interface.
	 * </p>
	 * <p>
	 * In addition to the summary message, this method also sets an array of
	 * individual messages.
	 * 
	 * 
	 * @param newMessage
	 *            the message, or <code>null</code> to clear the message
	 * @param newType
	 *            the message type
	 * @param children
	 *            the individual messages that contributed to the overall
	 *            message
	 * @see org.eclipse.jface.dialogs.IMessageProvider
	 * @since 3.3
	 */

	public void setMessage(String newMessage, int newType, IMessage[] children) {
		head.showMessage(newMessage, newType, children);
		layout();
	}

	/**
	 * Adds a message hyperlink listener. If at least one listener is present,
	 * messages will be rendered as hyperlinks.
	 * 
	 * @param listener
	 * @see #removeMessageHyperlinkListener(IHyperlinkListener)
	 * @since 3.3
	 */
	public void addMessageHyperlinkListener(IHyperlinkListener listener) {
		head.addMessageHyperlinkListener(listener);
	}

	/**
	 * Remove the message hyperlink listener.
	 * 
	 * @param listener
	 * @see #addMessageHyperlinkListener(IHyperlinkListener)
	 * @since 3.3
	 */
	public void removeMessageHyperlinkListener(IHyperlinkListener listener) {
		head.removeMessageHyperlinkListener(listener);
	}

	/**
	 * Tests if the form is in the 'busy' state. Busy form displays 'busy'
	 * animation in the area of the title image.
	 * 
	 * @return <code>true</code> if busy, <code>false</code> otherwise.
	 * @since 3.2
	 */

	public boolean isBusy() {
		return head.isBusy();
	}

	/**
	 * Sets the form's busy state. Busy form will display 'busy' animation in
	 * the area of the title image.
	 * 
	 * @param busy
	 *            the form's busy state
	 * @since 3.2
	 */

	public void setBusy(boolean busy) {
		head.setBusy(busy);
	}

	/**
	 * Adds support for dragging items out of the form title area via a user
	 * drag-and-drop operation.
	 * 
	 * @param operations
	 *            a bitwise OR of the supported drag and drop operation types (
	 *            <code>DROP_COPY</code>,<code>DROP_LINK</code>, and
	 *            <code>DROP_MOVE</code>)
	 * @param transferTypes
	 *            the transfer types that are supported by the drag operation
	 * @param listener
	 *            the callback that will be invoked to set the drag data and to
	 *            cleanup after the drag and drop operation finishes
	 * @see org.eclipse.swt.dnd.DND
	 * @since 3.3
	 */
	public void addTitleDragSupport(int operations, Transfer[] transferTypes,
			DragSourceListener listener) {
		head.addDragSupport(operations, transferTypes, listener);
	}

	/**
	 * Adds support for dropping items into the form title area via a user
	 * drag-and-drop operation.
	 * 
	 * @param operations
	 *            a bitwise OR of the supported drag and drop operation types (
	 *            <code>DROP_COPY</code>,<code>DROP_LINK</code>, and
	 *            <code>DROP_MOVE</code>)
	 * @param transferTypes
	 *            the transfer types that are supported by the drop operation
	 * @param listener
	 *            the callback that will be invoked after the drag and drop
	 *            operation finishes
	 * @see org.eclipse.swt.dnd.DND
	 * @since 3.3
	 */
	public void addTitleDropSupport(int operations, Transfer[] transferTypes,
			DropTargetListener listener) {
		head.addDropSupport(operations, transferTypes, listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessage()
	 */
	public String getMessage() {
		return head.getMessage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessageType()
	 */
	public int getMessageType() {
		return head.getMessageType();
	}

	/**
	 * Returns the children messages that the cause of the summary message
	 * currently set on the form.
	 * 
	 * @return an array of children messages or <code>null</code> if not set.
	 * @see #setMessage(String, int, IMessage[])
	 * @since 3.3
	 */
	public IMessage[] getChildrenMessages() {
		return head.getChildrenMessages();
	}

	void setSelectionText(FormText text) {
		if (selectionText != null && selectionText != text) {
			selectionText.clearSelection();
		}
		this.selectionText = text;
	}
	
	/**
	 * Returns the message manager that will keep track of messages in this
	 * form. 
	 * 
	 * @return the message manager instance
	 * @since org.eclipse.ui.forms 3.4
	 */
	public IMessageManager getMessageManager() {
		if (messageManager == null)
			messageManager = new MessageManager(this);
		return messageManager;
	}
}