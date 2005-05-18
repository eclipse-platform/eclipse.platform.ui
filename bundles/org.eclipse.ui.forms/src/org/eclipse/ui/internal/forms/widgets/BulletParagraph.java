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
package org.eclipse.ui.internal.forms.widgets;

import java.util.Hashtable;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class BulletParagraph extends Paragraph {
	public static final int CIRCLE = 1;

	public static final int TEXT = 2;

	public static final int IMAGE = 3;

	private int style = CIRCLE;

	private String text;

	private int CIRCLE_DIAM = 5;

	private int SPACING = 10;

	private int indent = -1;

	private int bindent = -1;

	private Rectangle bbounds;

	/**
	 * Constructor for BulletParagraph.
	 * 
	 * @param addVerticalSpace
	 */
	public BulletParagraph(boolean addVerticalSpace) {
		super(addVerticalSpace);
	}

	public int getIndent() {
		int ivalue = indent;
		if (ivalue != -1)
			return ivalue;
		switch (style) {
		case CIRCLE:
			ivalue = CIRCLE_DIAM + SPACING;
			break;
		default:
			ivalue = 20;
			break;
		}
		return getBulletIndent() + ivalue;
	}

	public int getBulletIndent() {
		if (bindent != -1)
			return bindent;
		return 0;
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

	public void setBulletIndent(int bindent) {
		this.bindent = bindent;
	}

	/*
	 * @see IBulletParagraph#getBulletText()
	 */
	public String getBulletText() {
		return text;
	}

	public void layout(GC gc, int width, Locator loc, int lineHeight,
			Hashtable resourceTable, IHyperlinkSegment selectedLink) {
		computeRowHeights(gc, width, loc, lineHeight, resourceTable);
		layoutBullet(gc, loc, lineHeight, resourceTable);
		super.layout(gc, width, loc, lineHeight, resourceTable, selectedLink);
	}

	public void paint(GC gc, Rectangle repaintRegion,
			Hashtable resourceTable, IHyperlinkSegment selectedLink,
			SelectionData selData) {
		paintBullet(gc, repaintRegion, resourceTable);
		super.paint(gc, repaintRegion, resourceTable, selectedLink, selData);
	}

	private void layoutBullet(GC gc, Locator loc, int lineHeight,
			Hashtable resourceTable) {
		int x = loc.x - getIndent() + getBulletIndent();
		int rowHeight = ((int[]) loc.heights.get(0))[0];
		if (style == CIRCLE) {
			int y = loc.y + rowHeight / 2 - CIRCLE_DIAM / 2;
			bbounds = new Rectangle(x, y, CIRCLE_DIAM, CIRCLE_DIAM);
		} else if (style == TEXT && text != null) {
			//int height = gc.getFontMetrics().getHeight();
			Point textSize = gc.textExtent(text);
			bbounds = new Rectangle(x, loc.y, textSize.x, textSize.y);
		} else if (style == IMAGE && text != null) {
			Image image = (Image) resourceTable.get(text);
			if (image != null) {
				Rectangle ibounds = image.getBounds();
				int y = loc.y + rowHeight / 2 - ibounds.height / 2;
				bbounds = new Rectangle(x, y, ibounds.width, ibounds.height);
			}
		}
	}

	public void paintBullet(GC gc, Rectangle repaintRegion,
			Hashtable resourceTable) {
		if (bbounds == null)
			return;
		int x = bbounds.x;
		int y = bbounds.y;
		if (repaintRegion != null) {
			x -= repaintRegion.x;
			y -= repaintRegion.y;
		}
		if (style == CIRCLE) {
			Color bg = gc.getBackground();
			Color fg = gc.getForeground();
			gc.setBackground(fg);
			gc.fillRectangle(x, y + 1, 5, 3);
			gc.fillRectangle(x + 1, y, 3, 5);
			gc.setBackground(bg);
		} else if (style == TEXT && text != null) {
			gc.drawText(text, x, y);
		} else if (style == IMAGE && text != null) {
			Image image = (Image) resourceTable.get(text);
			if (image != null)
				gc.drawImage(image, x, y);
		}
	}
}
