/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;

/**
 * Form is a control that is capable of scrolling its content. It renders the
 * content below the title and can accept optional background image. Form
 * should be created in a parent that will allow it to use all the available
 * area (for example, a shell, a view or an editor).
 * <p>
 * Children of the form should typically be created using FormToolkit to match
 * the appearance and behaviour. When creating children, use a form body as a
 * parent by calling 'getBody()' on the form instance. Example:
 * 
 * <pre>
 *  Form form = new Form(parent); FormToolkit toolkit = new FormToolkit(parent.getDisplay()); form.setText("Sample form"); form.getBody().setLayout(new GridLayout()); toolkit.createButton(form.getBody(), "Checkbox", SWT.CHECK);
 * </pre>
 * 
 * 
 * 
 * 
 * <p>
 * No layout manager has been set on the body. Clients are required to set the
 * desired layout manager explicitly. are
 * 
 * @since 3.0
 */

public class Form extends SharedScrolledComposite {
	private int TITLE_HMARGIN = 10;
	private int TITLE_VMARGIN = 5;
	private Image backgroundImage;
	private String text;
	private Composite body;
	
	private class ContentComposite extends Composite {
		public ContentComposite(Composite parent, int style) {
			super(parent, style);
		}
		public Point computeSize(int wHint, int hHint, boolean changed) {
			return ((FormLayout)getLayout()).computeSize(this, wHint, hHint, changed);
		}
	}

	private class FormLayout extends Layout implements ILayoutExtension {

		public int computeMinimumWidth(
			Composite composite,
			boolean flushCache) {
			return computeSize(composite, 5, SWT.DEFAULT, flushCache).x;
		}
		public int computeMaximumWidth(
			Composite composite,
			boolean flushCache) {
			return computeSize(
				composite,
				SWT.DEFAULT,
				SWT.DEFAULT,
				flushCache).x;
		}
		public Point computeSize(
			Composite composite,
			int wHint,
			int hHint,
			boolean flushCache) {
			int width = 0;
			int height = 0;

			if (text != null) {
				GC gc = new GC(composite);
				gc.setFont(getFont());
				if (wHint != SWT.DEFAULT) {
					Point wsize = FormUtil.computeWrapSize(gc, text, wHint);
					width = wsize.x;
					height = wsize.y;
				} else {
					Point extent = gc.textExtent(text);
					width = extent.x;
					height = extent.y;
				}
				gc.dispose();
				height += TITLE_VMARGIN * 2;
				width += TITLE_HMARGIN * 2;
			}
			int ihHint = hHint;
			if (ihHint>0 && ihHint!=SWT.DEFAULT)
				ihHint -= height;

			Point bsize =
				body.computeSize(
					FormUtil.getWidthHint(wHint, body),
					FormUtil.getHeightHint(ihHint, body),
					flushCache);
			width = Math.max(bsize.x, width);
			height += bsize.y;
			return new Point(width, height);
		}

		protected void layout(Composite composite, boolean flushCache) {
			Rectangle carea = composite.getClientArea();
			int height = 0;
			if (text != null) {
				GC gc = new GC(composite);
				gc.setFont(getFont());
				height
					+= FormUtil.computeWrapSize(
						gc,
						text,
						carea.width - TITLE_HMARGIN * 2).y;
				gc.dispose();
				height += TITLE_VMARGIN * 2;
			}
			body.setBounds(0, height, carea.width, carea.height - height);
		}
	}
	/**
	 * Creates the form control as a child of the provided parent.
	 * 
	 * @param parent
	 *            the parent widget
	 */
	public Form(Composite parent) {
		super(parent, SWT.NULL);
		final Composite content = new ContentComposite(this, SWT.NULL);
		content.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event e) {
				onPaint(content, e.gc);
			}
		});
		super.setContent(content);
		content.setLayout(new FormLayout());

		body = new LayoutComposite(content, SWT.NULL);
		body.setMenu(parent.getMenu());
	}
	/**
	 * Returns the title text that will be rendered at the top of the form.
	 * 
	 * @return the title text
	 */
	public String getText() {
		return text;
	}
	/**
	 * Sets the foreground color of the form. This color will also be used for
	 * the body.
	 */
	public void setForeground(Color fg) {
		super.setForeground(fg);
		body.setForeground(fg);
	}

	/**
	 * Sets the background color of the form. This color will also be used for
	 * the body.
	 */
	public void setBackground(Color bg) {
		super.setBackground(bg);
		body.setBackground(bg);
	}
/**
 * The form sets the content widget. This method should not be called by 
 * classes that instantiate this widget.
 */
	public final void setContent(Control c) {}

	/**
	 * Sets the text to be rendered at the top of the form above the body as a
	 * title.
	 * 
	 * @param text
	 *            the title text
	 */
	public void setText(String text) {
		this.text = text;
		layout(true);
		redraw();
	}
	/**
	 * Returns the optional background image of this form.
	 * 
	 * @return Returns the backgroundImage.
	 */
	public Image getBackgroundImage() {
		return backgroundImage;
	}

	/**
	 * Sets the optional background image to be rendered behind the title and
	 * the body of the form.
	 * 
	 * @param backgroundImage
	 *            The backgroundImage to set.
	 */
	public void setBackgroundImage(Image backgroundImage) {
		this.backgroundImage = backgroundImage;
		redraw();
	}

	/**
	 * Recomputes the body layout and form scroll bars. The method should be
	 * used when changes somewhere in the form body invalidate the current
	 * layout and/or scroll bars.
	 * 
	 * @param flushCache
	 */
	public void reflow(boolean flushCache) {
		if (body!=null) {
			body.layout(flushCache);
		}
		super.reflow(flushCache);
	}
	/**
	 * Returns the container that occupies the body of the form (the form area
	 * below the title). Use this container as a parent of controls that should
	 * be in the form. No layout manager has been set on the form body.
	 * 
	 * @return Returns the body of the form.
	 */
	public Composite getBody() {
		return body;
	}

	private void onPaint(Composite c, GC gc) {
		Rectangle carea = c.getClientArea();
		if (backgroundImage != null) {
			gc.drawImage(backgroundImage, 0, 0);
		}
		if (text != null) {
			gc.setBackground(getBackground());
			gc.setForeground(getForeground());
			gc.setFont(getFont());
			FormUtil.paintWrapText(
				gc,
				new Point(carea.width, carea.height),
				text,
				TITLE_HMARGIN,
				TITLE_VMARGIN);
		}
	}
}