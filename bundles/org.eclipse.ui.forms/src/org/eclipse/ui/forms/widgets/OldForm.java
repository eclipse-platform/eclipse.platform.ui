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
 * title image can be set and is rendered to the left of the title.
 * <p>
 * Since 3.2, the form supports status messages. These messages can have various
 * severity (error, warning, info or none). Message tray can be minimized and
 * later restored by the user, but can only be closed programmatically.
 * <p>
 * Form can have a background image behind the title text. The image can be
 * painted as-is, or tiled as many times as needed to fill the title area.
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
public class OldForm extends Composite {
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
			bodyCache.setBounds(0, hsize.y, carea.width, carea.height - hsize.y);
		}
	}

	/**
	 * Creates the form content control as a child of the provided parent.
	 * 
	 * @param parent
	 *            the parent widget
	 */
	public OldForm(Composite parent, int style) {
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
	 * @return the title image
	 * @since 3.2
	 */
	public Image getImage() {
		return head.getImage();
	}

	/**
	 * Sets the foreground color of the form. This color will also be used for
	 * the body.
	 */
	public void setForeground(Color fg) {
		super.setForeground(fg);
		head.setForeground(fg);
		body.setForeground(fg);
	}

	/**
	 * Sets the background color of the form. This color will also be used for
	 * the body.
	 */
	public void setBackground(Color bg) {
		super.setBackground(bg);
		head.setBackground(bg);
		body.setBackground(bg);
	}
	
	/**
	 * Sets the font of the header text.
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

	public void setTextBackground(Color[] gradientColors, int[] percents,
			boolean vertical) {
		head.setTextBackground(gradientColors, percents, vertical);
	}

	/**
	 * Returns the optional background image of this form. The image is rendered
	 * starting at the position 0,0 and is painted behind the title.
	 * 
	 * @return Returns the background image.
	 */
	public Image getBackgroundImage() {
		return head.getBackgroundImage();
	}

	/**
	 * Sets the optional background image to be rendered behind the title
	 * starting at the position 0,0.
	 * 
	 * @param backgroundImage
	 *            The backgroundImage to set.
	 */
	public void setBackgroundImage(Image backgroundImage) {
		head.setBackgroundImage(backgroundImage);
		redraw();
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
	 * TODO add javadoc
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
	 * experimental - do not use yet TODO add javadoc
	 */
	public void setSeparatorVisible(boolean addSeparator) {
head.setSeparatorVisible(addSeparator);
	}

	/**
	 * experimental - do not use yet TODO add javadoc
	 */

	public Color getSeparatorColor() {
		return head.getSeparatorColor();
	}

	/**
	 * experimental - do not use yet TODO add javadoc
	 */
	public void setSeparatorColor(Color separatorColor) {
		head.setSeparatorColor(separatorColor);
	}

	/**
	 * Sets the message for this form.
	 * 
	 * @param message
	 *            the message, or <code>null</code> to clear the message
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
	 * Tests if the form is in the 'busy' state.
	 * 
	 * @return <code>true</code> if busy, <code>false</code> otherwise.
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
	 */

	public void setBusy(boolean busy) {
		head.setBusy(busy);
	}
}