/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;

import java.util.HashMap;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class FormImages {
	private static FormImages instance;

	public static FormImages getInstance() {
		if (instance == null)
			instance = new FormImages();
		return instance;
	}

	private HashMap images;
	private HashMap ids;
	
	private FormImages() {
	}
	
	private class ImageIdentifier {
		private Display fDisplay;
		private Color fColor1;
		private Color fColor2;
		private int fRealtheight;
		private int fTheight;
		private int fMarginHeight;
		
		ImageIdentifier (Display display, Color color1, Color color2,
				int realtheight, int theight, int marginHeight) {
			fDisplay = display;
			fColor1 = color1;
			fColor2 = color2;
			fRealtheight = realtheight;
			fTheight = theight;
			fMarginHeight = marginHeight;
		}
		
		public boolean equals(Object obj) {
			if (obj instanceof ImageIdentifier) {
				ImageIdentifier id = (ImageIdentifier) obj;
				if (id.fDisplay.equals(fDisplay) && id.fColor1.equals(fColor1) &&
						id.fColor2.equals(fColor2) && id.fRealtheight == fRealtheight &&
						id.fTheight == fTheight && id.fMarginHeight == fMarginHeight)
					return true;
			}
			return false;
		}
		
		public int hashCode() {
			int hash = fDisplay.hashCode();
			hash = hash * 7 + fColor1.hashCode();
			hash = hash * 7 + fColor2.hashCode();
			hash = hash * 7 + new Integer(fRealtheight).hashCode();
			hash = hash * 7 + new Integer(fTheight).hashCode();
			hash = hash * 7 + new Integer(fMarginHeight).hashCode();
			return hash;
		}
	}
	
	private class ImageReference {
		private Image fImage;
		private int fCount;
		
		public ImageReference(Image image) {
			fImage = image;
			fCount = 1;
		}

		public Image getImage() {
			return fImage;
		}
		// returns a boolean indicating if all clients of this image are finished
		// a true result indicates the underlying image should be disposed
		public boolean decCount() {
			return --fCount == 0;
		}
		public void incCount() {
			fCount++;
		}
	}
	
	public Image getGradient(Display display, Color color1, Color color2,
			int realtheight, int theight, int marginHeight) {
		checkHashMaps();
		ImageIdentifier id = new ImageIdentifier(display, color1, color2, realtheight, theight, marginHeight);
		ImageReference result = (ImageReference) images.get(id);
		if (result != null && !result.getImage().isDisposed()) {
			result.incCount();
			return result.getImage();
		}
		Image image = createGradient(display, color1, color2, realtheight, theight, marginHeight);
		images.put(id, new ImageReference(image));
		ids.put(image, id);
		return image;
	}
	
	public boolean markFinished(Image image) {
		checkHashMaps();
		ImageIdentifier id = (ImageIdentifier)ids.get(image);
		if (id != null) {
			ImageReference ref = (ImageReference) images.get(id);
			if (ref != null) {
				if (ref.decCount()) {
					images.remove(id);
					ids.remove(ref.getImage());
					ref.getImage().dispose();
					validateHashMaps();
				}
				return true;
			}
		}
		return false;
	}

	private void checkHashMaps() {
		if (images == null)
			images = new HashMap();
		if (ids == null)
			ids = new HashMap();
	}
	
	private void validateHashMaps() {
		if (images.size() == 0)
			images = null;
		if (ids.size() == 0)
			ids = null;
	}
	
	private Image createGradient(Display display, Color color1, Color color2,
			int realtheight, int theight, int marginHeight) {
		Image image = new Image(display, 1, realtheight);
		image.setBackground(color1);
		GC gc = new GC(image);
		gc.setBackground(color1);
		gc.fillRectangle(0, 0, 1, realtheight);
		gc.setForeground(color2);
		gc.setBackground(color1);
		gc.fillGradientRectangle(0, marginHeight + 2, 1, theight - 2, true);
		gc.dispose();
		return image;
	}
}
