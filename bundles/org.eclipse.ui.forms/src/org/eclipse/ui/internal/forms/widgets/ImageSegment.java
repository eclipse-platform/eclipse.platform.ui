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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;

/**
 * @version 	1.0
 * @author
 */
public class ImageSegment extends ParagraphSegment {
	public static final int TOP = 1;
	public static final int MIDDLE = 2;
	public static final int BOTTOM = 3;
	
	private int alignment = BOTTOM;
	private String imageId;
	
	public int getVerticalAlignment() {
		return alignment;
	}
	
	void setVerticalAlignment(int alignment) {
		this.alignment = alignment;
	}
	
	public Image getImage(Hashtable objectTable) {
		if (imageId==null) return null;
		Object obj = objectTable.get(imageId);
		if (obj==null) return null;
		if (obj instanceof Image) return (Image)obj;
		return null;
	}
	
	public String getObjectId() {
		return imageId;
	}
	
	void setObjectId(String imageId) {
		this.imageId = imageId;
	}
	
	public boolean advanceLocator(GC gc, int wHint, Locator loc, Hashtable objectTable, boolean computeHeightOnly) {
		Image image = getImage(objectTable);
		int iwidth = 0;
		int iheight = 0;
		boolean newLine=false;
		
		if (image!=null) {
			Rectangle rect = image.getBounds();
			iwidth = rect.width;
			iheight = rect.height;
		}
		if (wHint != SWT.DEFAULT && loc.x + iwidth > wHint) {
			// new line
			loc.x = loc.indent + iwidth;
			loc.width = loc.x;
			loc.y += loc.rowHeight;
			if (computeHeightOnly)
				loc.collectHeights(true);
			loc.rowHeight = iheight;
			loc.leading = 0;
			newLine=true;
		}
		else {
			loc.x += iwidth;
			loc.rowHeight = Math.max(loc.rowHeight, iheight);
		}
		return newLine;
	}

	public void paint(GC gc, int width, Locator loc, Hashtable resourceTable, boolean selected) {
		Image image = getImage(resourceTable);
		int iwidth = 0;
		int iheight = 0;
		if (image!=null) {
			Rectangle rect = image.getBounds();
			iwidth = rect.width;
			iheight = rect.height;
		}
		else return;
		loc.width = iwidth;
		loc.height = iheight;
		
		if (loc.x + iwidth > width) {
			// new row
			loc.x = loc.indent + loc.marginWidth;
			loc.y += loc.rowHeight;
			loc.rowHeight = 0;
			loc.rowCounter++;
		}
		int ix = loc.x;
		int iy = loc.getBaseline(iheight, false);		
		gc.drawImage(image, ix, iy);
		loc.x += iwidth;
		loc.rowHeight = Math.max(loc.rowHeight, iheight);
	}
}
