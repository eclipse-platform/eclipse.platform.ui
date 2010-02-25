/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wang Yizhuo (wangyizhuo@gmail.com) - patch (see Bugzilla #239178) 
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;

import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class FormImages {
	private static FormImages instance;

	public static FormImages getInstance() {
		if (instance == null)
			instance = new FormImages();
		return instance;
	}

	private ResourceManagerManger manager = new ResourceManagerManger();
	private HashMap descriptors;
	
	private FormImages() {
	}
	
	private abstract class AbstractImageDescriptor extends ImageDescriptor {
		RGB[] fRGBs;
		int fLength;
		
		AbstractImageDescriptor(Color[] colors, int length) {
			fRGBs = new RGB[colors.length];
			for (int i = 0; i < colors.length; i++) {
				Color color = colors[i];
				fRGBs[i] = color == null ? null : color.getRGB();
			}
			fLength = length;
		}
		
		public boolean equals(Object obj) {
			if (obj instanceof AbstractImageDescriptor) {
				AbstractImageDescriptor id = (AbstractImageDescriptor)obj;
				if (id.fRGBs.length == fRGBs.length) {
					boolean result = id.fLength == fLength;
					for (int i = 0; i < fRGBs.length && result; i++) {
						result = result && id.fRGBs[i].equals(fRGBs[i]);
					}
					return result;
				}
			}
			return false;
		}
		
		public int hashCode() {
			int hash = 0;
			for (int i = 0; i < fRGBs.length; i++)
				hash = hash * 7 + fRGBs[i].hashCode();
			hash = hash * 7 + fLength;
			return hash;
		}
	}
	
	private class SimpleImageDescriptor extends AbstractImageDescriptor{
		private int fTheight;
		private int fMarginHeight;
		
		SimpleImageDescriptor (Color color1, Color color2,
				int realtheight, int theight, int marginHeight) {
			super(new Color[] {color1, color2}, realtheight);
			fTheight = theight;
			fMarginHeight = marginHeight;
		}
		
		public boolean equals(Object obj) {
			if (obj instanceof SimpleImageDescriptor) {
				SimpleImageDescriptor id = (SimpleImageDescriptor) obj;
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

		public ImageData getImageData() {
			return null;
		}
		
		public Image createImage(boolean returnMissingImageOnError,	Device device) {
			Image image = new Image(device, 1, fLength);
			Color color1 = new Color(device, fRGBs[0]);
			Color color2 = new Color(device, fRGBs[1]);
			image.setBackground(color1);
			GC gc = new GC(image);
			gc.setBackground(color1);
			gc.fillRectangle(0, 0, 1, fLength);
			gc.setForeground(color2);
			gc.setBackground(color1);
			gc.fillGradientRectangle(0, fMarginHeight + 2, 1, fTheight - 2, true);
			gc.dispose();
			color1.dispose();
			color2.dispose();
			return image;
		}
	}
	
	private class ComplexImageDescriptor extends AbstractImageDescriptor {
		RGB fBgRGB;
		boolean fVertical;
		int[] fPercents;
		
		public ComplexImageDescriptor(Color[] colors, int length,
				int[] percents, boolean vertical, Color bg) {
			super(colors, length);
			fBgRGB = bg == null ? null : bg.getRGB();
			fVertical = vertical;
			fPercents = percents;
		}
		
		public boolean equals(Object obj) {
			if (obj instanceof ComplexImageDescriptor) {
				ComplexImageDescriptor id = (ComplexImageDescriptor) obj;
				if (super.equals(obj)  &&
						id.fVertical == fVertical && Arrays.equals(id.fPercents, fPercents)) {
					if ((id.fBgRGB == null && fBgRGB == null) ||
							(id.fBgRGB != null && id.fBgRGB.equals(fBgRGB)))
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

		public ImageData getImageData() {
			return null;
		}
		
		public Image createImage(boolean returnMissingImageOnError,	Device device) {
			int width = fVertical ? 1 : fLength;
			int height = fVertical ? fLength : 1;
			Image gradient = new Image(device, Math.max(width, 1), Math
					.max(height, 1));
			GC gc = new GC(gradient);
			Color[] colors = new Color[fRGBs.length];
			for (int i = 0; i < colors.length; i++)
				colors[i] = new Color(device, fRGBs[i]);
			Color bg = fBgRGB == null ? null : new Color(device, fBgRGB);
			drawTextGradient(gc, width, height, colors, fPercents, fVertical, bg);
			gc.dispose();
			for (int i = 0; i < colors.length; i++)
				colors[i].dispose();
			if (bg != null)
				bg.dispose();
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
						int gradientWidth = percents[i] * width / 100;
						
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
	
	public Image getGradient(Color color1, Color color2,
			int realtheight, int theight, int marginHeight, Display display) {
		if (color1 == null || color1.isDisposed() || color2 == null || color2.isDisposed())
			return null;
		AbstractImageDescriptor desc = new SimpleImageDescriptor(color1, color2, realtheight, theight, marginHeight);
		return getGradient(desc, display);
	}
	
	public Image getGradient(Color[] colors, int[] percents,
			int length, boolean vertical, Color bg, Display display) {
		if (colors.length == 0)
			return null;
		for (int i = 0; i < colors.length; i++)
			if (colors[i] == null || colors[i].isDisposed())
				return null;
		if (bg != null && bg.isDisposed())
			return null;
		AbstractImageDescriptor desc = new ComplexImageDescriptor(colors, length, percents, vertical, bg);
		return getGradient(desc, display);
	}
	
	private synchronized Image getGradient(AbstractImageDescriptor desc, Display display) {
		checkHashMaps();
		Image result = manager.getResourceManager(display).createImage(desc);
		descriptors.put(result, desc);
		return result;
	}
	
	public synchronized boolean markFinished(Image image, Display display) {
		checkHashMaps();
		AbstractImageDescriptor desc = (AbstractImageDescriptor)descriptors.get(image);
		if (desc != null) {
			LocalResourceManager resourceManager = manager.getResourceManager(display);
			resourceManager.destroyImage(desc);
			if (resourceManager.find(desc) == null) {
				descriptors.remove(image);
				validateHashMaps();
			}
			return true;
		}
		// if the image was not found, dispose of it for the caller
		image.dispose();
		return false;
	}

	private void checkHashMaps() {
		if (descriptors == null)
			descriptors = new HashMap();
	}
	
	private void validateHashMaps() {
		if (descriptors.size() == 0)
			descriptors = null;
	}
}
