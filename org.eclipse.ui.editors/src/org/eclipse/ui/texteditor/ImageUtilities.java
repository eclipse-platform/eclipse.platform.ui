/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

/**
 * @since 3.0
 * @deprecated use <code>org.eclipse.jface.text.source.ImageUtilities</code> instead
 */
public class ImageUtilities {
	
	/**
	 * @deprecated use <code>org.eclipse.jface.text.source.ImageUtilities</code> instead
	 */
	public static void drawImage(Image image, GC gc, Canvas canvas, Rectangle r, int halign, int valign) {
		org.eclipse.jface.text.source.ImageUtilities.drawImage(image, gc, canvas, r, halign, valign);
	}
	
	/**
	 * @deprecated use <code>org.eclipse.jface.text.source.ImageUtilities</code> instead
	 */
	public static void drawImage(Image image, GC gc, Canvas canvas, Rectangle r, int align) {
		org.eclipse.jface.text.source.ImageUtilities.drawImage(image, gc, canvas, r, align);
	}
}
