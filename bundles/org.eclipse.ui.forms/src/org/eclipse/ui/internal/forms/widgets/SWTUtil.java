/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * Utility class to simplify access to some SWT resources. 
 */
public class SWTUtil {

	/**
	 * Returns the standard display to be used. The method first checks, if
	 * the thread calling this method has an associated disaply. If so, this
	 * display is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display;
		display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		return display;
	}

	/**
	 * Returns the shell for the given widget. If the widget doesn't represent
	 * a SWT object that manage a shell, <code>null</code> is returned.
	 * 
	 * @return the shell for the given widget
	 */
	public static Shell getShell(Widget widget) {
		if (widget instanceof Control)
			return ((Control) widget).getShell();
		if (widget instanceof Caret)
			return ((Caret) widget).getParent().getShell();
		if (widget instanceof DragSource)
			return ((DragSource) widget).getControl().getShell();
		if (widget instanceof DropTarget)
			return ((DropTarget) widget).getControl().getShell();
		if (widget instanceof Menu)
			return ((Menu) widget).getParent().getShell();
		if (widget instanceof ScrollBar)
			return ((ScrollBar) widget).getParent().getShell();

		return null;
	}

	public static Image [] loadProgressImages(Display display, InputStream is, ArrayList delays, Point size) {
		ImageLoader loader = new ImageLoader();
		try {
			ImageData[] imageDataArray = loader.load(is);
			Image [] images = new Image[imageDataArray.length];
			for (int i=0; i<imageDataArray.length; i++) {
				images[i] = new Image(display, imageDataArray[i]);
				Rectangle bounds = images[i].getBounds();
				size.x = Math.max(size.x, bounds.width);
				size.y = Math.max(size.y, bounds.height);
				int ms = imageDataArray[i].delayTime * 10;
				if (ms < 20) ms += 30;
				if (ms < 30) ms += 10;
				delays.add(new Integer(ms));
			}
			size.x += 4;
			size.y += 4;
			return images;
		}
		catch (IllegalArgumentException e) {
			return null;
		}
	}
}