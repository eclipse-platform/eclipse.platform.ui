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

import java.util.Arrays;
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
	
	private abstract class ImageIdentifier {
		Display fDisplay;
		Color[] fColors;
		int fLength;
		
		ImageIdentifier(Display display, Color[] colors, int length) {
			fDisplay = display;
			fColors = colors;
			fLength = length;
		}
		
		public boolean equals(Object obj) {
			if (obj instanceof ImageIdentifier) {
				ImageIdentifier id = (ImageIdentifier)obj;
				if (id.fColors.length == fColors.length) {
					boolean result = id.fDisplay.equals(fDisplay) && id.fLength == fLength;
					for (int i = 0; i < fColors.length && result; i++) {
						result = result && id.fColors[i].equals(fColors[i]);
					}
					return result;
				}
			}
			return false;
		}
		
		public int hashCode() {
			int hash = fDisplay.hashCode();
			for (int i = 0; i < fColors.length; i++)
				hash = hash * 7 + fColors[i].hashCode();
			hash = hash * 7 + fLength;
			return hash;
		}
	}
	
	private class SimpleImageIdentifier extends ImageIdentifier{
		private int fTheight;
		private int fMarginHeight;
		
		SimpleImageIdentifier (Display display, Color color1, Color color2,
				int realtheight, int theight, int marginHeight) {
			super(display, new Color[] {color1, color2}, realtheight);
			fTheight = theight;
			fMarginHeight = marginHeight;
		}
		
		public boolean equals(Object obj) {
			if (obj instanceof SimpleImageIdentifier) {
				SimpleImageIdentifier id = (SimpleImageIdentifier) obj;
				if (super.equals(obj)  &&
						id.fTheight == fTheight && id.fMarginHeight == fMarginHeight)
					return true;
			}
			return false;
		}
		
		public int hashCode() {
			int hash = super.hashCode();
			hash = hash * 7 + new Integer(fTheight).hashCode();
			hash = hash * 7 + new Integer(fMarginHeight).hashCode();
			return hash;
		}
	}
	
	private class ComplexImageIdentifier extends ImageIdentifier {
		Color fBg;
		boolean fVertical;
		int[] fPercents;
		
		public ComplexImageIdentifier(Display display, Color[] colors, int length,
				int[] percents, boolean vertical, Color bg) {
			super(display, colors, length);
			fBg = bg;
			fVertical = vertical;
			fPercents = percents;
		}
		
		public boolean equals(Object obj) {
			if (obj instanceof ComplexImageIdentifier) {
				ComplexImageIdentifier id = (ComplexImageIdentifier) obj;
				if (super.equals(obj)  &&
						id.fVertical == fVertical && Arrays.equals(id.fPercents, fPercents)) {
					if ((id.fBg == null && fBg == null) ||
							(id.fBg != null && id.fBg.equals(fBg)))
						return true;
					// if the only thing that isn't the same is the background color
					// still return true if it does not matter (percents add up to 100)
					int sum = 0;
					for (int i = 0; i < fPercents.length; i++)
						sum += fPercents[i];
					if (sum >= 100)
						return true;
				}
			}
			return false;
		}
		
		public int hashCode() {
			int hash = super.hashCode();
			hash = hash * 7 + new Boolean(fVertical).hashCode();
			for (int i = 0; i < fPercents.length; i++)
				hash = hash * 7 + new Integer(fPercents[i]).hashCode();
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
		ImageIdentifier id = new SimpleImageIdentifier(display, color1, color2, realtheight, theight, marginHeight);
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
	
	public Image getGradient(Display display, Color[] colors, int[] percents,
			int length, boolean vertical, Color bg) {
		checkHashMaps();
		ImageIdentifier id = new ComplexImageIdentifier(display, colors, length, percents, vertical, bg);
		ImageReference result = (ImageReference) images.get(id);
		if (result != null && !result.getImage().isDisposed()) {
			result.incCount();
			return result.getImage();
		}
		Image image = createGradient(display, colors, percents, length, vertical, bg);
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
		// if the image was not found, dispose of it for the caller
		image.dispose();
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
	
	private Image createGradient(Display display, Color[] colors, int[] percents,
			int length, boolean vertical, Color bg) {
		int width = vertical ? 1 : length;
		int height = vertical ? length : 1;
		Image gradient = new Image(display, Math.max(width, 1), Math
				.max(height, 1));
		GC gc = new GC(gradient);
		drawTextGradient(gc, width, height, colors, percents, vertical, bg);
		gc.dispose();
		return gradient;
	}

	private void drawTextGradient(GC gc, int width, int height, Color[] colors,
			int[] percents, boolean vertical, Color bg) {
		final Color oldBackground = gc.getBackground();
		if (colors.length == 1) {
			if (colors[0] != null)
				gc.setBackground(colors[0]);
			gc.fillRectangle(0, 0, width, height);
		} else {
			final Color oldForeground = gc.getForeground();
			Color lastColor = colors[0];
			if (lastColor == null)
				lastColor = oldBackground;
			int pos = 0;
			for (int i = 0; i < percents.length; ++i) {
				gc.setForeground(lastColor);
				lastColor = colors[i + 1];
				if (lastColor == null)
					lastColor = oldBackground;
				gc.setBackground(lastColor);
				if (vertical) {
					int gradientHeight = percents[i] * height / 100;
					
					gc.fillGradientRectangle(0, pos, width, gradientHeight,
							true);
					pos += gradientHeight;
				} else {
					int gradientWidth = percents[i] * height / 100;
					
					gc.fillGradientRectangle(pos, 0, gradientWidth, height,
							false);
					pos += gradientWidth;
				}
			}
			if (vertical && pos < height) {
				if (bg != null)
					gc.setBackground(bg);
				gc.fillRectangle(0, pos, width, height - pos);
			}
			if (!vertical && pos < width) {
				if (bg != null)
					gc.setBackground(bg);
				gc.fillRectangle(pos, 0, width - pos, height);
			}
			gc.setForeground(oldForeground);
		}
	}
}
