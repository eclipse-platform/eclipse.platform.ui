/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;

import java.util.Hashtable;

import org.eclipse.swt.graphics.*;

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
	/**
	 * Constructor for BulletParagraph.
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
			case CIRCLE :
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

	public void paint(
		GC gc,
		int width,
		Locator loc,
		int lineHeight,
		Hashtable resourceTable,
		HyperlinkSegment selectedLink) {
		computeRowHeights(gc, width, loc, lineHeight, resourceTable);
		paintBullet(gc, loc, lineHeight, resourceTable);
		super.paint(gc, width, loc, lineHeight, resourceTable, selectedLink);
	}

	public void paintBullet(
		GC gc,
		Locator loc,
		int lineHeight,
		Hashtable resourceTable) {
		int x = loc.x - getIndent() + getBulletIndent();
		int rowHeight = ((int[])loc.heights.get(0))[0];
		if (style == CIRCLE) {
			int y = loc.y + rowHeight / 2 - CIRCLE_DIAM / 2;
			Color bg = gc.getBackground();
			Color fg = gc.getForeground();
			gc.setBackground(fg);
			gc.fillRectangle(x, y + 1, 5, 3);
			gc.fillRectangle(x + 1, y, 3, 5);
			gc.setBackground(bg);
		} else if (style == TEXT && text != null) {
			gc.drawText(text, x, loc.y);
		} else if (style == IMAGE && text != null) {
			Image image = (Image) resourceTable.get(text);
			if (image != null) {
				int y = loc.y + rowHeight / 2 - image.getBounds().height / 2;
				gc.drawImage(image, x, y);
			}
		}
	}
}
