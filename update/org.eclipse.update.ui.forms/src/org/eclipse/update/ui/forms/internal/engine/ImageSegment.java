/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.ui.forms.internal.engine;

import org.eclipse.swt.graphics.*;
import java.util.Hashtable;
import org.eclipse.swt.SWT;

/**
 * @version 	1.0
 * @author
 */
public class ImageSegment extends ParagraphSegment implements IImageSegment {
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
	
	public void advanceLocator(GC gc, int wHint, Locator loc, Hashtable objectTable) {
		Image image = getImage(objectTable);
		int iwidth = 0;
		int iheight = 0;
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
			loc.rowHeight = iheight;
		}
		else {
			loc.x += iwidth;
			loc.rowHeight = Math.max(loc.rowHeight, iheight);
		}
	}

	public void paint(GC gc, int width, Locator loc, Hashtable objectTable, boolean selected) {
		Image image = getImage(objectTable);
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
		
		int ix = loc.x;
		int iy = loc.y;
		
		if (ix + iwidth > width) {
			// new row
			ix = loc.indent + loc.marginWidth;
			iy += loc.rowHeight;
			loc.rowHeight = 0;
		}
		
		gc.drawImage(image, ix, iy);
		loc.x = ix + iwidth;
		loc.y = iy;
		loc.rowHeight = Math.max(loc.rowHeight, iheight);
	}
}
