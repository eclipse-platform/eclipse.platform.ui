/*
 * Created on Jan 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.internal.forms.widgets;

import org.eclipse.swt.graphics.*;
import org.eclipse.ui.forms.HyperlinkSettings;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ImageHyperlinkSegment extends ImageSegment implements
		IHyperlinkSegment {
	private String href;
	private HyperlinkSettings settings;
	private String tooltipText;
	
	public ImageHyperlinkSegment(HyperlinkSettings settings) {
		this.settings = settings;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.forms.widgets.IHyperlinkSegment#setHref(java.lang.String)
	 */
	public void setHref(String href) {
		this.href = href;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.forms.widgets.IHyperlinkSegment#getHref()
	 */
	public String getHref() {
		return href;
	}

	public void paintFocus(GC gc, Color bg, Color fg, boolean selected) {
		Rectangle bounds = getBounds();
		if (bounds == null)
			return;
		if (selected) {
			gc.setBackground(bg);
			gc.setForeground(fg);
			gc.drawFocus(bounds.x, bounds.y, bounds.width, bounds.height);
		} else {
			gc.setForeground(bg);
			gc.drawRectangle(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.forms.widgets.IHyperlinkSegment#isWordWrapAllowed()
	 */
	public boolean isWordWrapAllowed() {
		return !isNowrap();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.forms.widgets.IHyperlinkSegment#setWordWrapAllowed(boolean)
	 */
	public void setWordWrapAllowed(boolean value) {
		setNowrap(!value);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.forms.widgets.IHyperlinkSegment#getText()
	 */
	public String getText() {
		return "";
	}
	/**
	 * @return Returns the tooltipText.
	 */
	public String getTooltipText() {
		return tooltipText;
	}
	/**
	 * @param tooltipText The tooltipText to set.
	 */
	public void setTooltipText(String tooltipText) {
		this.tooltipText = tooltipText;
	}
}