
package org.eclipse.update.ui.forms.internal.engine;

import org.eclipse.swt.graphics.GC;
import java.util.Hashtable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Color;

public class BulletParagraph extends Paragraph implements IBulletParagraph {
	private int style = CIRCLE;
	private String text;
	private int CIRCLE_DIAM = 5;
	private int SPACING = 10;
	private int indent = -1;
	/**
	 * Constructor for BulletParagraph.
	 * @param addVerticalSpace
	 */
	public BulletParagraph(boolean addVerticalSpace) {
		super(addVerticalSpace);
	}
	
	public int getIndent() {
		if (indent != -1) return indent;
		switch (style) {
			case CIRCLE:
				return CIRCLE_DIAM + SPACING;
		}
		return 20;
	}

	/*
	 * @see IBulletParagraph#getBulletStyle()
	 */
	public int getBulletStyle() {
		return style;
	}
	
	public void setBulletStyle(int style) {
		this.style = style;
	}
	
	public void setBulletText(String text) {
		this.text = text;
	}
	
	public void setIndent(int indent) {
		this.indent = indent;
	}

	/*
	 * @see IBulletParagraph#getBulletText()
	 */
	public String getBulletText() {
		return text;
	}
	
	public void paintBullet(GC gc, Locator loc, int lineHeight, Hashtable objectTable) {
		int x = loc.x - getIndent();
		if (style==CIRCLE) {
			int y = loc.y + lineHeight/2 - CIRCLE_DIAM/2;
			Color bg = gc.getBackground();
			Color fg = gc.getForeground();
			gc.setBackground(fg);
			gc.fillRectangle(x, y+1, 5, 3);
			gc.fillRectangle(x+1, y, 3, 5);
			gc.setBackground(bg);
		}
		else if (style==TEXT && text!=null) {
			gc.drawText(text, x, loc.y);
		}
		else if (style==IMAGE && text!=null) {
			Image image = (Image)objectTable.get(text);
			if (image!=null) {
				int y = loc.y + lineHeight/2 - image.getBounds().height/2;
				gc.drawImage(image, x, y);
			}
		}
	}
}
