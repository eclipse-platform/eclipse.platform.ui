/*
 * Created on Nov 26, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms.parts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Composite;

/**
 * Hyperlink is a concrete implementation of the
 * abstract base class that draws text in the client area.
 * Text can be wrapped and underlined.
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
		super(parent, style);
		initAccessible();
	}

	protected void initAccessible() {
		Accessible accessible = getAccessible();
		accessible.addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent e) {
				e.result = getText();
			}

			public void getHelp(AccessibleEvent e) {
				e.result = getToolTipText();
			}
		});

		accessible
			.addAccessibleControlListener(new AccessibleControlAdapter() {
			public void getChildAtPoint(AccessibleControlEvent e) {
				Point pt = toControl(new Point(e.x, e.y));
				e.childID =
					(getBounds().contains(pt))
						? ACC.CHILDID_SELF
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
				e.detail = ACC.ROLE_LABEL;
			}

			public void getState(AccessibleControlEvent e) {
				int state = ACC.STATE_NORMAL;
				if (Hyperlink.this.getSelection())
					state = ACC.STATE_SELECTED|ACC.STATE_FOCUSED;
				e.detail = state;
			}
		});
	}

	/**
	 * @param href
	 */
	public void setHref(Object href) {
		setData("href", href);
	}
	/**
	 * @return
	 */
	public Object getHref() {
		return getData("href");
	}

	public void setUnderlined(boolean underlined) {
		this.underlined = underlined;
		redraw();
	}

	public boolean isUnderlined() {
		return underlined;
	}

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

	private Point computeTextSize(int wHint, int hHint) {
		Point extent;
		GC gc = new GC(this);

		gc.setFont(getFont());
		if ((getStyle() & SWT.WRAP) != 0 && wHint != SWT.DEFAULT) {
			int height = FormUtil.computeWrapHeight(gc, text, wHint);
			extent = new Point(wHint, height);
		} else {
			extent = gc.textExtent(getText());
		}
		gc.dispose();
		return extent;
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		if (text != null)
			this.text = text;
		else
			text = "";
		redraw();
	}
	
	protected void paintHyperlink(PaintEvent e) {
		GC gc = e.gc;
		Point size = getSize();
		gc.setFont(getFont());
		gc.setForeground(getForeground());
		if ((getStyle() & SWT.WRAP) != 0) {
			FormUtil.paintWrapText(
				gc,
				size,
				text,
				marginWidth,
				marginHeight,
				underlined);
		} else {
			gc.drawText(getText(), marginWidth, marginHeight, true);
			if (underlined) {
				FontMetrics fm = gc.getFontMetrics();
				int descent = fm.getDescent();
				int lineY = size.y - marginHeight - descent + 1;
				gc.drawLine(marginWidth, lineY, size.x - marginWidth, lineY);
			}
		}
	}
}
