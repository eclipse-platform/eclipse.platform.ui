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
import java.util.Hashtable;

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
/**
 * A variation of the expandable composite that adds optional description below
 * the title.
 * 
 * TODO (dejan) - spell out subclass contract
 * 
 * @since 3.0
 */
public class Section extends ExpandableComposite {
	/**
	 * Description style. If used, description will be rendered below the title.
	 */
	public static final int DESCRIPTION = 1 << 7;
	private Label descriptionLabel;
	private Control separator;
	private Hashtable titleColors;
	private static final String COLOR_BG = "bg";
	private static final String COLOR_GBG = "gbg";
	private static final String COLOR_FG = "fg";
	private static final String COLOR_BORDER = "border";
	/**
	 * Creates a new section instance in the provided parent.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param style
	 *            the style to use
	 */
	public Section(Composite parent, int style) {
		super(parent, SWT.NULL, style);
		if ((style & DESCRIPTION) != 0) {
			descriptionLabel = new Label(this, SWT.WRAP);
		}
	}
	protected void internalSetExpanded(boolean expanded) {
		super.internalSetExpanded(expanded);
		reflow();
	}
	protected void reflow() {
		Composite c = this;
		while (c != null) {
			c.setRedraw(false);
			c = c.getParent();
			if (c instanceof ScrolledForm) {
				break;
			}
		}
		c = this;
		while (c != null) {
			c.layout(true);
			c = c.getParent();
			if (c instanceof ScrolledForm) {
				((ScrolledForm) c).reflow(true);
				break;
			}
		}
		c = this;
		while (c != null) {
			c.setRedraw(true);
			c = c.getParent();
			if (c instanceof ScrolledForm) {
				break;
			}
		}
	}
	/**
	 * Sets the description text. Has no effect of DESCRIPTION style was not
	 * used to create the control.
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		if (descriptionLabel != null)
			descriptionLabel.setText(description);
	}
	/**
	 * Returns the current description text.
	 * 
	 * @return description text or <code>null</code> if DESCRIPTION style was
	 *         not used to create the control.
	 */
	public String getDescription() {
		if (descriptionLabel != null)
			return descriptionLabel.getText();
		return null;
	}
	/**
	 * Sets the separator control of this section. The separator must not be
	 * <samp>null </samp> and must be a direct child of this container. If
	 * defined, separator will be placed below the title text and will remain
	 * visible regardless of the expansion state.
	 * 
	 * @param separator
	 *            the separator that will be placed below the title text.
	 */
	public void setSeparatorControl(Control separator) {
		Assert.isTrue(separator != null && separator.getParent().equals(this));
		this.separator = separator;
	}
	/**
	 * Returns the control that is used as a separator betweeen the title and
	 * the client, or <samp>null </samp> if not set.
	 * 
	 * @return separator control or <samp>null </samp> if not set.
	 */
	public Control getSeparatorControl() {
		return separator;
	}
	/**
	 * Sets the background of the section.
	 * 
	 * @param bg
	 *            the new background
	 */
	public void setBackground(Color bg) {
		super.setBackground(bg);
		if (descriptionLabel != null)
			descriptionLabel.setBackground(bg);
	}
	/**
	 * Sets the foreground of the section.
	 * 
	 * @param fg
	 *            the new foreground.
	 */
	public void setForeground(Color fg) {
		super.setForeground(fg);
		if (descriptionLabel != null)
			descriptionLabel.setForeground(fg);
	}
	/**
	 * Returns the control used to render the description.
	 * 
	 * @return description control or <code>null</code> if DESCRIPTION style
	 *         was not used to create the control.
	 */
	protected Control getDescriptionControl() {
		return descriptionLabel;
	}
	public void setTitleBarBorderColor(Color color) {
		putTitleBarColor(COLOR_BORDER, color);
	}
	public void setTitleBarForeground(Color color) {
		putTitleBarColor(COLOR_FG, color);
	}
	public void setTitleBarBackground(Color color) {
		putTitleBarColor(COLOR_BG, color);
		textLabel.setBackground(color);
		if (toggle!=null)
			toggle.setBackground(color);
	}
	public void setTitleGradientBackground(Color color) {
		putTitleBarColor(COLOR_GBG, color);
		//textLabel.setBackground(color);
		//if (toggle!=null)
		//	toggle.setBackground(color);
	}
	public Color getTitleBarBorderColor() {
		if (titleColors==null) return null;
		return (Color)titleColors.get(COLOR_BORDER);
	}
	public Color getTitleGradientBackground() {
		if (titleColors==null) return null;
		return (Color)titleColors.get(COLOR_GBG);
	}
	public Color getTitleBarForeground() {
		if (titleColors==null) return null;
		return (Color)titleColors.get(COLOR_FG);
	}
	public Color getTitleBarBackground() {
		if (titleColors==null) return null;
		return (Color)titleColors.get(COLOR_BG);
	}
	private void putTitleBarColor(String key, Color color) {
		if (titleColors==null)
			titleColors = new Hashtable();
		titleColors.put(key, color);
	}
	protected void onPaint(PaintEvent e) {
		Color bg = null;
		Color gbg = null;
		Color fg = null;
		Color border = null;
		if (titleColors!=null) {
			bg = (Color)titleColors.get(COLOR_BG);
			gbg = (Color)titleColors.get(COLOR_GBG);
			fg = (Color)titleColors.get(COLOR_FG);
			border = (Color)titleColors.get(COLOR_BORDER);
		}
		if (bg==null) bg = getBackground();
		if (fg ==null) fg = getForeground();
		if (border==null) border = fg;
		if (gbg==null) gbg = bg;

		Rectangle bounds = getClientArea();
		Point tsize = null;
		Point tcsize = null;
		if (toggle != null)
			tsize = toggle.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		int twidth = bounds.width - marginWidth - marginWidth;
		if (tsize != null)
			twidth -= tsize.x + GAP;
		if (getTextClient() != null)
			tcsize = getTextClient().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		if (tcsize != null)
			twidth -= tcsize.x + GAP;
		Point size = textLabel.computeSize(twidth, SWT.DEFAULT, true);
		int tvmargin = GAP;
		int theight = 0;
		if (tsize != null)
			theight += Math.max(theight, tsize.y);
		if (tcsize != null)
			theight = Math.max(theight, tcsize.y);
		theight = Math.max(theight, size.y);
		theight += tvmargin + tvmargin;
		int midpoint = (theight *66)/100;
		int rem = theight - midpoint;
		GC gc = e.gc;
		//gc.fillRectangle(marginWidth, marginHeight, bounds.width - 1
		//		- marginWidth - marginWidth, solidHeight - 1);
		gc.setForeground(bg);
		gc.setBackground(gbg);
		gc.fillGradientRectangle(marginWidth, marginHeight, bounds.width - 1
				- marginWidth - marginWidth, midpoint - 1, true);
		gc.setForeground(gbg);
		gc.setBackground(getBackground());
		gc.fillGradientRectangle(marginWidth, marginHeight+midpoint-1, bounds.width - 1
				- marginWidth - marginWidth, rem - 1, true);
		gc.setForeground(border);
		gc.drawLine(marginWidth, marginHeight+2, marginWidth, marginHeight+theight-1);
		gc.drawLine(marginWidth, marginHeight+2, marginWidth+2, marginHeight);
		gc.drawLine(marginWidth+2, marginHeight, bounds.width-marginWidth-3, marginHeight);
		gc.drawLine(bounds.width-marginWidth-3, marginHeight, bounds.width-marginWidth-1, marginHeight+2);
		gc.drawLine(bounds.width-marginWidth-1, marginHeight+2, bounds.width-marginWidth-1, marginHeight+theight-1);
		if (toggle != null && !isExpanded()) {
			gc.drawLine(marginWidth, marginHeight+theight-1, bounds.width-marginWidth-1, marginHeight+theight-1);
		}

	}
}