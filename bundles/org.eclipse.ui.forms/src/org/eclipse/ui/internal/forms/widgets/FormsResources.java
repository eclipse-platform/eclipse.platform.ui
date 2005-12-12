/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

/**
 * Utility methods to access shared form-specific resources.
 * <p>
 * All methods declared on this class are static. This
 * class cannot be instantiated.
 * </p>
 * <p>
 * </p>
 */
public class FormsResources {
	private static Cursor busyCursor;
	private static Cursor handCursor;
	private static Cursor textCursor;
	private static Point progressSize;
	private static Image[] progressImages;
	private static int [] progressDelays;
	
	public static Cursor getBusyCursor() {
		if (busyCursor==null)
			busyCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_WAIT);
		return busyCursor;
	}
	public static Cursor getHandCursor() {
		if (handCursor==null)
			handCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
		return handCursor;
	}
	public static Cursor getTextCursor() {
		if (textCursor==null)
			textCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_IBEAM);
		return textCursor;
	}
	
	public static Image [] getProgressImages(Display display) {
		if (progressImages==null) {
			InputStream is = FormsResources.class.getResourceAsStream("progress.gif"); //$NON-NLS-1$
			if (is!=null) {
				ArrayList delays = new ArrayList();
				progressSize = new Point(0, 0);
				progressImages = SWTUtil.loadProgressImages(display, is, delays, progressSize);
				try {
					is.close();
				}
				catch (IOException e) {
				}
				if (progressImages!=null) {
					progressDelays = new int[delays.size()];
					for (int i=0; i<delays.size(); i++) {
						progressDelays[i]=((Integer)delays.get(i)).intValue();
					}
				}
			}
		}
		return progressImages;
	}
	
	public static Point getProgressSize() {
		return progressSize;
	}
	
	public static int getProgressDelay(int index) {
		/*
		if (progressDelays==null)
			return 0;
		return progressDelays[index];
		*/
		return 100;
	}
	
	public static void shutdown() {
		if (busyCursor!=null)
			busyCursor.dispose();
		if (handCursor!=null)
			handCursor.dispose();
		if (textCursor!=null)
			textCursor.dispose();
		if (progressImages!=null) {
			for (int i=0; i<progressImages.length; i++) {
				progressImages[i].dispose();
			}
			progressImages=null;
		}
		busyCursor=null;
		handCursor=null;
		textCursor=null;
	}
}
