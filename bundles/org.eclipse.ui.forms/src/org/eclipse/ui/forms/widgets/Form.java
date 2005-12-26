/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
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
 * The form supports status messages. These messages can have various
 * severity (error, warning, info or none). Message tray can be minimized and
 * later restored by the user, but can only be closed programmatically.
 * <p>
 * Form can have a background image behind the title text. The image can be
 * painted as-is, or tiled as many times as needed to fill the title area.
 * Alternatively, gradient background can be painted vertically or
 * horizontally.
 * <p>Form can be put in the 'busy' state. While in this state,
 * title image is replaced with an animation that lasts as long as the
 * 'busy' state is active.
 * <p>It is possible to create an optional head client control.
 * When created, this control is placed next to the title. If title tool
 * bar is also present, a new row is created in the header, and the tool
 * bar is right-justified in the second row.
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
public class Form extends Composite {
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
	 * Sets the image to be rendered to the left of the title.
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
	 * gradient.
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
	 * starting at the position 0,0.
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
	 * TODO add javadoc -
	 * 
	 * @return Returns the backgroundImageTiled.
	 */
	public boolean isBackgroundImageTiled() {
		return head.isBackgroundImageTiled();
	}

	/**
	 * TODO add javadoc
	 * 
	 * @param backgroundImageTiled
	 *            The backgroundImageTiled to set.
	 */
	public void setBackgroundImageTiled(boolean backgroundImageTiled) {
		head.setBackgroundImageTiled(backgroundImageTiled);
	}

	/**
	 * @return Returns the backgroundImageAlignment. TODO add javadoc
	 * @since 3.1
	 */
	public int getBackgroundImageAlignment() {
		return head.getBackgroundImageAlignment();
	}

	/**
	 * @param backgroundImageAlignment
	 *            The backgroundImageAlignment to set. TODO add javadoc
	 * @since 3.1
	 */
	public void setBackgroundImageAlignment(int backgroundImageAlignment) {
		head.setBackgroundImageAlignment(backgroundImageAlignment);
	}

	/**
	 * @return Returns the backgroundImageClipped.
	 * @since 3.1
	 */
	public boolean isBackgroundImageClipped() {
		return head.isBackgroundImageClipped();
	}

	/**
	 * @param backgroundImageClipped
	 *            The backgroundImageClipped to set.
	 * @since 3.1
	 */
	public void setBackgroundImageClipped(boolean backgroundImageClipped) {
		head.setBackgroundImageClipped(backgroundImageClipped);
	}

	void setSelectionText(FormText text) {
		if (selectionText != null && selectionText != text) {
			selectionText.clearSelection();
		}
		this.selectionText = text;
	}

	/**
	 * TODO add javadoc experimental - do not use yet
	 * 
	 * @return <code>true</code> if the receiver is a visible separator,
	 *         <code>false</code> otherwise
	 */
	public boolean isSeparatorVisible() {
		return head.isSeparatorVisible();
	}

	/**
	 * If set, adds a separator between the head and body. If gradient text
	 * background is used, the separator will use gradient colors.
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
	 * Returns the color used to render the optional head separator.
	 * 
	 * @return separator color or <code>null</code> if not set.
	 */

	public Color getSeparatorColor() {
		return head.getSeparatorColor();
	}

	/**
	 * Sets the color to be used to render the optional head separator.
	 * 
	 * @param separatorColor
	 *            the color to render the head separator or <code>null</code>
	 *            to use the default color.
	 * @since 3.2
	 */
	public void setSeparatorColor(Color separatorColor) {
		head.setSeparatorColor(separatorColor);
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
		head.setMessage(message);
	}

	/**
	 * Sets the message for this form with an indication of what type of message
	 * it is.
	 * <p>
	 * The valid message types are one of <code>NONE</code>,
	 * <code>INFORMATION</code>,<code>WARNING</code>, or
	 * <code>ERROR</code>.
	 * </p>
	 * <p>
	 * 
	 * @param newMessage
	 *            the message, or <code>null</code> to clear the message
	 * @param newType
	 *            the message type
	 * @since 3.2
	 */

	public void setMessage(String newMessage, int newType) {
		head.setMessage(newMessage, newType);
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
}