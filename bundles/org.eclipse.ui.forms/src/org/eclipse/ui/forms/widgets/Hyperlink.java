/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.forms.widgets.*;

/**
 * Hyperlink is a concrete implementation of the abstract base class that draws
 * text in the client area. Text can be wrapped and underlined. Hyperlink is
 * typically added to the hyperlink group so that certain properties are managed
 * for all the hyperlinks that belong to it.
 * <p>
 * Hyperlink can be extended.
 * 
 * @see org.eclipse.ui.forms.HyperlinkGroup
 * @since 3.0
 */
public class Hyperlink extends AbstractHyperlink {
	private String text;

	private boolean underlined;

	/**
	 * Creates a new hyperlink control in the provided parent.
	 * 
	 * @param parent
	 *            the control parent
	 * @param style
	 *            the widget style
	 */
	public Hyperlink(Composite parent, int style) {
		super(parent, SWT.NO_BACKGROUND | style);
		initAccessible();
	}

	protected void initAccessible() {
		Accessible accessible = getAccessible();
		accessible.addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent e) {
				e.result = getText();
				if (e.result == null)
					getHelp(e);
			}

			public void getHelp(AccessibleEvent e) {
				e.result = getToolTipText();
			}
		});
		accessible.addAccessibleControlListener(new AccessibleControlAdapter() {
			public void getChildAtPoint(AccessibleControlEvent e) {
				Point pt = toControl(new Point(e.x, e.y));
				e.childID = (getBounds().contains(pt)) ? ACC.CHILDID_SELF
						: ACC.CHILDID_NONE;
			}

			public void getLocation(AccessibleControlEvent e) {
				Rectangle location = getBounds();
				Point pt = toDisplay(new Point(location.x, location.y));
				e.x = pt.x;
				e.y = pt.y;
				e.width = location.width;
				e.height = location.height;
			}

			public void getChildCount(AccessibleControlEvent e) {
				e.detail = 0;
			}

			public void getRole(AccessibleControlEvent e) {
				e.detail = ACC.ROLE_LINK;
			}

			public void getState(AccessibleControlEvent e) {
				int state = ACC.STATE_NORMAL;
				if (Hyperlink.this.getSelection())
					state = ACC.STATE_SELECTED | ACC.STATE_FOCUSED;
				e.detail = state;
			}
		});
	}

	/**
	 * Sets the underlined state. It is not necessary to call this method when
	 * in a hyperlink group.
	 * 
	 * @param underlined
	 *            if <samp>true </samp>, a line will be drawn below the text for
	 *            each wrapped line.
	 */
	public void setUnderlined(boolean underlined) {
		this.underlined = underlined;
		redraw();
	}

	/**
	 * Returns the underline state of the hyperlink.
	 * 
	 * @return <samp>true </samp> if text is underlined, <samp>false </samp>
	 *         otherwise.
	 */
	public boolean isUnderlined() {
		return underlined;
	}

	/**
	 * Overrides the parent by incorporating the margin.
	 */
	public Point computeSize(int wHint, int hHint, boolean changed) {
		checkWidget();
		int innerWidth = wHint;
		if (innerWidth != SWT.DEFAULT)
			innerWidth -= marginWidth * 2;
		Point textSize = computeTextSize(innerWidth, hHint);
		int textWidth = textSize.x + 2 * marginWidth;
		int textHeight = textSize.y + 2 * marginHeight;
		return new Point(textWidth, textHeight);
	}

	/**
	 * Returns the current hyperlink text.
	 * 
	 * @return hyperlink text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text of this hyperlink.
	 * 
	 * @param text
	 *            the hyperlink text
	 */
	public void setText(String text) {
		if (text != null)
			this.text = text;
		else
			this.text = ""; //$NON-NLS-1$
		redraw();
	}

	/**
	 * Paints the hyperlink text.
	 * 
	 * @param gc
	 *            graphic context
	 */
	protected void paintHyperlink(GC gc) {
		Rectangle carea = getClientArea();
		Rectangle bounds = new Rectangle(marginWidth, marginHeight, carea.width
				- marginWidth - marginWidth, carea.height - marginHeight
				- marginHeight);
		paintText(gc, bounds);
	}

	/**
	 * Paints the hyperlink text in provided bounding rectangle.
	 * 
	 * @param gc
	 *            graphic context
	 * @param bounds
	 *            the bounding rectangle in which to paint the text
	 */
	protected void paintText(GC gc, Rectangle bounds) {
		gc.setFont(getFont());
		gc.setForeground(getForeground());
		if ((getStyle() & SWT.WRAP) != 0) {
			FormUtil.paintWrapText(gc, text, bounds, underlined);
		} else {
			Point textSize = computeTextSize(bounds.width, SWT.DEFAULT);
			int textWidth = textSize.x;
			int textHeight = textSize.y;
			gc.drawText(getText(), bounds.x, bounds.y, true);
			if (underlined) {
				int descent = gc.getFontMetrics().getDescent();
				int lineY = bounds.y + textHeight - descent + 1;
				gc.drawLine(bounds.x, lineY, bounds.x + textWidth, lineY);
			}
		}
	}

	protected Point computeTextSize(int wHint, int hHint) {
		Point extent;
		GC gc = new GC(this);
		gc.setFont(getFont());
		if ((getStyle() & SWT.WRAP) != 0 && wHint != SWT.DEFAULT) {
			extent = FormUtil.computeWrapSize(gc, getText(), wHint);
		} else {
			extent = gc.textExtent(getText());
		}
		gc.dispose();
		return extent;
	}
}