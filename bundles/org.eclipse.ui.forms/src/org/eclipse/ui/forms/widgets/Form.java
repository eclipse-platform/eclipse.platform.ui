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
import org.eclipse.ui.forms.IMessageContainerWithDetails;
import org.eclipse.ui.forms.events.IHyperlinkListener;
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
 * worm will participate in wrapping as long as its layout manager implements
 * ILayoutExtension as well.
 * <p>
 * Children of the form should typically be created using FormToolkit to match
 * the appearance and behaviour. When creating children, use the form body as a
 * parent by calling 'getBody()' on the form instance. Example:
 * 
 * <pre>
 * FormToolkit toolkit = new FormToolkit(parent.getDisplay());
 * Form form = toolkit.createForm(parent);
 * formContent.setText(&quot;Sample form&quot;);
 * formContent.getBody().setLayout(new GridLayout());
 * toolkit.createButton(formContent.getBody(), &quot;Checkbox&quot;, SWT.CHECK);
 * </pre>
 * 
 * <p>
 * No layout manager has been set on the body. Clients are required to set the
 * desired layout manager explicitly.
 * <p>
 * Although the class is not final, it should not be subclassed.
 * 
 * @since 3.0
 */
public class Form extends Composite implements IMessageContainerWithDetails {
	private FormHeading head;

	private Composite body;

	private SizeCache bodyCache = new SizeCache();

	private SizeCache headCache = new SizeCache();

	private FormText selectionText;

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

			Point bsize = bodyCache.computeSize(FormUtil.getWidthHint(wHint,
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
		return head.getBackgroundImage();
	}

	/**
	 * Sets the optional background image to be rendered behind the title
	 * starting at the position 0,0. If the image is smaller than the container
	 * in any dimension, it will be tiled.
	 * 
	 * @since 3.2
	 * 
	 * @param backgroundImage
	 *            the head background image.
	 * 
	 */
	public void setBackgroundImage(Image backgroundImage) {
		head.setBackgroundImage(backgroundImage);
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
	 * @deprecated since 3.2, always returns <code>true</code>.
	 */
	public boolean isBackgroundImageTiled() {
		return true;
	}

	/**
	 * Sets whether the header background image is repeated to cover the entire
	 * heading area or not.
	 * 
	 * @param backgroundImageTiled
	 *            set <code>true</code> to tile the image, or
	 *            <code>false</code> to paint the background image only once
	 * @deprecated since 3.2, the background image is always tiled.
	 */
	public void setBackgroundImageTiled(boolean backgroundImageTiled) {
	}

	/**
	 * Returns the background image alignment.
	 * 
	 * @deprecated due to the underlying widget limitations, background image is
	 *             always tiled and alignment cannot be controlled.
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
	 * @deprecated use {@link #getHeadColor(Form.BOTTOM_KEYLINE2)}
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
	 * @deprecated use {@link #setHeadColor(Form.COLOR_BOTTOM_KEYLINE2, Color)}
	 */
	public void setSeparatorColor(Color separatorColor) {
		head.putColor(IFormColors.H_BOTTOM_KEYLINE2, separatorColor);
	}

	/**
	 * Sets the color used to paint an aspect of the form heading.
	 * 
	 * @param key
	 *            a valid form heading color key as defined in
	 *            {@link IFormColors}.
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
	 * @deprecated use {@link #setMessage(String, int)}
	 * @since 3.2
	 */
	public void setMessage(String message) {
		this.setMessage(message, 0);
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
	 * 
	 * @param newMessage
	 *            the message, or <code>null</code> to clear the message
	 * @param newType
	 *            the message type
	 * @see org.eclipse.jface.dialogs.IMessageProvider
	 * @since 3.2
	 */

	public void setMessage(String newMessage, int newType) {
		setMessage(newMessage, null, null, newType);
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
	 * @see org.eclipse.jface.dialogs.IMessageContainerWithDetails#setMessage(java.lang.String,
	 *      java.lang.String, org.eclipse.jface.dialogs.IMessage[], int)
	 */
	public void setMessage(String message, String details, IMessage[] messages,
			int type) {
		head.setMessage(message, details, messages, type);
		layout();
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

	void setSelectionText(FormText text) {
		if (selectionText != null && selectionText != text) {
			selectionText.clearSelection();
		}
		this.selectionText = text;
	}
}