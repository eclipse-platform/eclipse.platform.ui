/*
 * Created on Jun 19, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.welcome.internal.portal;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.update.ui.forms.internal.FormLabel;
import org.eclipse.welcome.internal.WelcomePortalImages;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WelcomeTitleArea extends FormLabel {
	private Image topImage;
	private Image bottomImage;

	public WelcomeTitleArea(Composite parent, int style) {
		super(parent, style);
		topImage = WelcomePortalImages.get(WelcomePortalImages.IMG_FORM_BANNER);
		bottomImage = WelcomePortalImages.get(WelcomePortalImages.IMG_FORM_UNDERLINE);
	}
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Point textSize = super.computeSize(wHint, hHint, changed);
		int width = textSize.x;
		int height = Math.max(textSize.y, topImage.getBounds().height);
		height += bottomImage.getBounds().height;
		return new Point(width, height);
	}
	protected void paint(PaintEvent e) {
		GC gc = e.gc;
		Rectangle bounds = getBounds();
		gc.drawImage(topImage, bounds.x, bounds.y);
		int y =	bounds.y + bounds.height - bottomImage.getBounds().height;
		gc.drawImage(bottomImage, 0, y);
		super.paint(e);
	}
}
