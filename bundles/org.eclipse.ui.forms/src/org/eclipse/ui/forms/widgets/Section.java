/*******************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Michael Williamson (eclipse-bugs@magnaworks.com) - patch (see Bugzilla #92545)
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 430205, 458055
 *     Ralf Petter <ralf.petter@gmail.com> - Bug 509654, 183675
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;

import java.util.Hashtable;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.internal.forms.widgets.FormImages;
import org.eclipse.ui.internal.forms.widgets.FormUtil;

/**
 * A variation of the expandable composite that adds optional description below
 * the title. Section is often used as a basic building block in forms because
 * it provides for logical grouping of information.
 * <p>
 * In case of the TITLE_BAR style, Section renders the title bar in a way
 * compatible with the rest of the workbench. Since it is a widget, all the
 * colors must be supplied directly. When created by the form toolkit, these
 * colors are supplied by the toolkit. The toolkit initializes these colors
 * based on the system colors. For this reason, it is recommended to create the
 * section by the toolkit instead of through its own constructor.
 * <p>
 * Since 3.1, it is possible to set a control to be used for section
 * description. If used, <code>DESCRIPTION</code> style should not be set. A
 * typical way to take advantage of the new method is to set an instance of
 * <code>FormText</code> to provide for hyperlinks and images in the description
 * area.
 *
 * @since 3.0
 */
public class Section extends ExpandableComposite {
	/**
	 * Description style. If used, description will be rendered below the title.
	 */
	public static final int DESCRIPTION = 1 << 7;

	private Control descriptionControl;

	private Control separator;

	private Hashtable<String, Color> titleColors;

	private static final String COLOR_BG = "bg"; //$NON-NLS-1$

	private static final String COLOR_GBG = "gbg"; //$NON-NLS-1$

	private static final String COLOR_BORDER = "border"; //$NON-NLS-1$

	/**
	 * Creates a new section instance in the provided parent.
	 *
	 * @param parent
	 *            the parent composite
	 * @param style
	 *            the style to use
	 */
	public Section(Composite parent, int style) {
		this(parent, SWT.NULL, style);
	}

	Section(Composite parent, int cstyle, int style) {
		super(parent, cstyle | getBackgroundStyle(style), style);
		int rtl = cstyle & SWT.RIGHT_TO_LEFT;
		if ((style & DESCRIPTION) != 0) {
			descriptionControl = new Label(this, SWT.WRAP | rtl);
		}
		if ((style & TITLE_BAR) != 0) {
			Listener listener = e -> {
				Image image = Section.super.getBackgroundImage();
				if (image != null) {
					FormImages.getInstance().markFinished(image, getDisplay());
				}
				Section.super.setBackgroundImage(null);
			};
			addListener(SWT.Dispose, listener);
			addListener(SWT.Resize, listener);
		}
	}

	private static int getBackgroundStyle(int estyle) {
		return ((estyle & TITLE_BAR) != 0) ? SWT.NO_BACKGROUND : SWT.NULL;
	}

	@Override
	protected void internalSetExpanded(boolean expanded) {
		super.internalSetExpanded(expanded);
		if ((getExpansionStyle() & TITLE_BAR) != 0) {
			if (!expanded)
				super.setBackgroundImage(null);
		}
		reflow();
	}

	/**
	 * Reflows this section and all the parents up the hierarchy until a
	 * SharedScrolledComposite is reached.
	 */
	@Override
	protected void reflow() {
		super.reflow();
	}

	/**
	 * Sets the description text. Has no effect if DESCRIPTION style was not
	 * used to create the control.
	 *
	 * @param description
	 */
	public void setDescription(String description) {
		if (descriptionControl instanceof Label)
			((Label) descriptionControl).setText(description);
	}

	/**
	 * Returns the current description text.
	 *
	 * @return description text or <code>null</code> if DESCRIPTION style was
	 *         not used to create the control.
	 */
	public String getDescription() {
		if (descriptionControl instanceof Label)
			return ((Label) descriptionControl).getText();
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
	 * Returns the control that is used as a separator between the title and the
	 * client, or <samp>null </samp> if not set.
	 *
	 * @return separator control or <samp>null </samp> if not set.
	 */
	@Override
	public Control getSeparatorControl() {
		return separator;
	}

	/**
	 * Sets the background of the section.
	 *
	 * @param bg
	 *            the new background
	 */
	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		if (descriptionControl != null
				&& (getExpansionStyle() & DESCRIPTION) != 0)
			descriptionControl.setBackground(bg);
	}

	/**
	 * Sets the foreground of the section.
	 *
	 * @param fg
	 *            the new foreground.
	 */
	@Override
	public void setForeground(Color fg) {
		super.setForeground(fg);
		if (descriptionControl != null
				&& (getExpansionStyle() & DESCRIPTION) != 0)
			descriptionControl.setForeground(fg);
	}

	/**
	 * Returns the control used to render the description. In 3.1, this method
	 * was promoted to public.
	 *
	 * @return description control or <code>null</code> if DESCRIPTION style
	 *         was not used to create the control and description control was
	 *         not set by the client.
	 * @see #setDescriptionControl(org.eclipse.swt.widgets.Control)
	 */
	@Override
	public Control getDescriptionControl() {
		return descriptionControl;
	}

	/**
	 * Sets the description control of this section. The control must not be
	 * <samp>null</samp> and must be a direct child of this container. If
	 * defined, control will be placed below the title text and the separator
	 * and will be hidden in the collapsed state.
	 * <p>
	 * This method and <code>DESCRIPTION</code> style are mutually exclusive.
	 * Use the method only if you want to create the description control
	 * yourself.
	 *
	 * @since 3.1
	 * @param descriptionControl
	 *            the control that will be placed below the title text.
	 */
	public void setDescriptionControl(Control descriptionControl) {
		Assert.isTrue((getExpansionStyle() & DESCRIPTION) == 0);
		Assert.isTrue(descriptionControl != null
				&& descriptionControl.getParent().equals(this));
		this.descriptionControl = descriptionControl;
	}

	/**
	 * Sets the color of the title bar border when TITLE_BAR style is used.
	 *
	 * @param color
	 *            the title bar border color
	 */
	public void setTitleBarBorderColor(Color color) {
		putTitleBarColor(COLOR_BORDER, color);
	}

	/**
	 * Sets the color of the title bar background when TITLE_BAR style is used.
	 * This color is used as a starting color for the vertical gradient.
	 *
	 * @param color
	 *            the title bar border background
	 */
	public void setTitleBarBackground(Color color) {
		putTitleBarColor(COLOR_BG, color);
	}

	/**
	 * Sets the color of the title bar gradient background when TITLE_BAR style
	 * is used. This color is used at the height where title controls end
	 * (toggle, tool bar).
	 *
	 * @param color
	 *            the title bar gradient background
	 */
	public void setTitleBarGradientBackground(Color color) {
		putTitleBarColor(COLOR_GBG, color);
	}

	/**
	 * Returns the title bar border color when TITLE_BAR style is used.
	 *
	 * @return the title bar border color
	 */
	public Color getTitleBarBorderColor() {
		if (titleColors == null)
			return null;
		return titleColors.get(COLOR_BORDER);
	}

	/**
	 * Returns the title bar gradient background color when TITLE_BAR style is
	 * used.
	 *
	 * @return the title bar gradient background
	 */
	public Color getTitleBarGradientBackground() {
		if (titleColors == null)
			return null;
		if ((getExpansionStyle() & SHORT_TITLE_BAR) != 0)
			return getBackground();
		return titleColors.get(COLOR_GBG);
	}

	/**
	 * Returns the title bar background when TITLE_BAR style is used.
	 *
	 * @return the title bar background
	 */
	public Color getTitleBarBackground() {
		if (titleColors == null)
			return null;
		return titleColors.get(COLOR_BG);
	}

	private void putTitleBarColor(String key, Color color) {
		if (color == null)
			return;
		if (titleColors == null)
			titleColors = new Hashtable<>();
		titleColors.put(key, color);
	}

	@Override
	protected void onPaint(PaintEvent e) {
		Color bg = null;
		Color fg = null;
		Color border = null;

		GC gc = e.gc;
		Image buffer = null;
		Rectangle bounds = getClientArea();

		if (bounds.width == 0 || bounds.height == 0) {
			return;
		}

		if ((getExpansionStyle() & TITLE_BAR) != 0) {
			buffer = new Image(getDisplay(), bounds.width, bounds.height);
			buffer.setBackground(getBackground());
			gc = new GC(buffer);
		}
		if (titleColors != null) {
			bg = titleColors.get(COLOR_BG);
			fg = getTitleBarForeground();
			border = titleColors.get(COLOR_BORDER);
		}
		if (bg == null)
			bg = getBackground();
		if (fg == null)
			fg = getForeground();
		if (border == null)
			border = fg;
		int theight = 0;
		int gradientheight = 0;
		int tvmargin = IGAP;
		if ((getExpansionStyle() & TITLE_BAR) != 0) {
			Point tsize = null;
			Point tcsize = null;
			if (toggle != null)
				tsize = toggle.getSize();
			if (getTextClient() != null)
				tcsize = getTextClient().getSize();
			Point size = textLabel == null ? new Point(0,0) : textLabel.getSize();
			if (tsize != null)
				theight += Math.max(theight, tsize.y);
			gradientheight = theight;
			if (tcsize != null) {
				theight = Math.max(theight, tcsize.y);
			}
			theight = Math.max(theight, size.y);
			gradientheight = Math.max(gradientheight, size.y);
			theight += tvmargin + tvmargin;
			gradientheight += tvmargin + tvmargin;
		} else {
			theight = 5;
		}
		if ((getExpansionStyle() & TITLE_BAR) != 0) {
			if (getBackgroundImage() == null)
				updateHeaderImage(bg, bounds, gradientheight, theight);
			gc.setBackground(getBackground());
			gc.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height);
			drawBackground(gc, bounds.x, bounds.y, bounds.width, theight - 2);
			if (marginWidth > 0) {
				// fix up margins
				gc.setBackground(getBackground());
				gc.fillRectangle(0, 0, marginWidth, theight);
				gc.fillRectangle(bounds.x + bounds.width - marginWidth, 0,
						marginWidth, theight);
			}
		} else if (isExpanded()) {
			gc.setForeground(bg);
			gc.setBackground(getBackground());
			gc.fillGradientRectangle(marginWidth, marginHeight, bounds.width
					- marginWidth - marginWidth, theight, true);
		}
		gc.setBackground(getBackground());
		FormUtil.setAntialias(gc, SWT.ON);
		// repair the upper left corner
		gc.fillPolygon(new int[] { marginWidth, marginHeight, marginWidth,
				marginHeight + 2, marginWidth + 2, marginHeight });
		// repair the upper right corner
		gc.fillPolygon(new int[] { bounds.width - marginWidth - 3,
				marginHeight, bounds.width - marginWidth, marginHeight,
				bounds.width - marginWidth, marginHeight + 3 });
		gc.setForeground(border);
		if (isExpanded() || (getExpansionStyle() & TITLE_BAR) != 0) {
			// top left curve
			gc.drawLine(marginWidth, marginHeight + 2, marginWidth + 2,
					marginHeight);
			// top edge
			gc.drawLine(marginWidth + 2, marginHeight, bounds.width
					- marginWidth - 3, marginHeight);
			// top right curve
			gc.drawLine(bounds.width - marginWidth - 3, marginHeight,
					bounds.width - marginWidth - 1, marginHeight + 2);
		} else {
			// collapsed short title bar
			// top edge
			gc.drawLine(marginWidth, marginHeight, bounds.width - 1,
					marginHeight);
		}
		if ((getExpansionStyle() & TITLE_BAR) != 0 || isExpanded()) {
			// left vertical edge gradient
			gc.fillGradientRectangle(marginWidth, marginHeight + 2, 1, theight + 2, true);
			// right vertical edge gradient
			gc.fillGradientRectangle(bounds.width - marginWidth - 1, marginHeight + 2, 1, theight + 2, true);
		}
		if ((getExpansionStyle() & TITLE_BAR) != 0) {
			// New in 3.3 - edge treatment
			gc.setForeground(getBackground());
			gc.drawPolyline(new int[] { marginWidth + 1, marginHeight + gradientheight + 4, marginWidth + 1,
					marginHeight + 2, marginWidth + 2, marginHeight + 2, marginWidth + 2, marginHeight + 1,
					bounds.width - marginWidth - 3, marginHeight + 1, bounds.width - marginWidth - 3, marginHeight + 2,
					bounds.width - marginWidth - 2, marginHeight + 2, bounds.width - marginWidth - 2,
					marginHeight + gradientheight + 4 });
		}
		if (buffer != null) {
			gc.dispose();
			e.gc.drawImage(buffer, 0, 0);
			buffer.dispose();
		}
	}

	private void updateHeaderImage(Color bg, Rectangle bounds, int theight, int realtheight) {
		Color gradient = getTitleBarGradientBackground() != null ? getTitleBarGradientBackground() : getBackground();
		Image image = FormImages.getInstance().getSectionGradientImage(gradient, bg, realtheight,
				theight, marginHeight, getDisplay());
		super.setBackgroundImage(image);
	}

	/**
	 * Background image is used for the title gradient - does nothing.
	 */
	@Override
	public final void setBackgroundImage(Image image) {
	}
}
