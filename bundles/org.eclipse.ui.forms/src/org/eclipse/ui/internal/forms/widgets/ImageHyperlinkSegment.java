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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.forms.widgets.IHyperlinkSegment#repaint(org.eclipse.swt.graphics.GC, boolean)
	 */
	public void repaint(GC gc, boolean hover) {
		/*
		FontMetrics fm = gc.getFontMetrics();
		int lineHeight = fm.getHeight();
		int descent = fm.getDescent();
		boolean rolloverMode = settings.getHyperlinkUnderlineMode()==HyperlinkSettings.UNDERLINE_HOVER;
		for (int i=0; i<areaRectangles.size(); i++) {
			AreaRectangle areaRectangle = (AreaRectangle)areaRectangles.get(i);
			Rectangle rect = areaRectangle.rect;
			String text = areaRectangle.getText();
			Point extent = gc.textExtent(text);
			int textX = rect.x + 1;
			gc.drawString(text, textX, rect.y, false);
			if (underline || hover || rolloverMode) {
				int lineY = rect.y + lineHeight - descent + 1;
				Color saved=null;
				if (rolloverMode && !hover) {
					saved = gc.getForeground();
					gc.setForeground(gc.getBackground());
				}
				gc.drawLine(textX, lineY, textX+extent.x, lineY);
				if (saved!=null)
					gc.setForeground(saved);
			}
		}
		*/
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
}