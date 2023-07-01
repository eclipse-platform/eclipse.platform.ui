/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wang Yizhuo (wangyizhuo@gmail.com) - patch (see Bugzilla #239178)
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 430205, 458055
 *     Ralf M Petter <ralf.petter@gmail.com> - Bug 510826
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
	private Map<Integer, AbstractImageDescriptor> descriptors;

	private FormImages() {
	}

	private abstract static class AbstractImageDescriptor extends ImageDescriptor {
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

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof AbstractImageDescriptor) {
				AbstractImageDescriptor id = (AbstractImageDescriptor)obj;
				return fLength == id.fLength && Arrays.equals(fRGBs, id.fRGBs);
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hash = 0;
			for (RGB fRGB : fRGBs)
				hash = hash * 7 + fRGB.hashCode();
			return hash * 7 + fLength;
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

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SimpleImageDescriptor) {
				SimpleImageDescriptor id = (SimpleImageDescriptor) obj;
				if (super.equals(obj)  &&
						id.fTheight == fTheight && id.fMarginHeight == fMarginHeight)
					return true;
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hash = super.hashCode();
			hash = hash * 7 + Integer.valueOf(fTheight).hashCode();
			return hash * 7 + Integer.valueOf(fMarginHeight).hashCode();
		}

		@Override
		public ImageData getImageData() {
			return null;
		}

		@Override
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

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ComplexImageDescriptor) {
				ComplexImageDescriptor id = (ComplexImageDescriptor) obj;
				if (super.equals(obj)  &&
						id.fVertical == fVertical && Arrays.equals(id.fPercents, fPercents)) {
					if (Objects.equals(fBgRGB, id.fBgRGB)) {
						return true;
					}
					// if the only thing that isn't the same is the background color
					// still return true if it does not matter (percents add up to 100)
					int sum = 0;
					for (int fPercent : fPercents)
						sum += fPercent;
					if (sum >= 100)
						return true;
				}
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hash = super.hashCode();
			hash = hash * 7 + Boolean.hashCode(fVertical);
			for (int fPercent : fPercents) {
				hash = hash * 7 + Integer.hashCode(fPercent);
			}
			return hash;
		}

		@Override
		public ImageData getImageData() {
			return null;
		}

		@Override
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

	private class SimpleSectionImageDescriptor extends AbstractImageDescriptor {
		protected int fTheight;
		protected int fMarginHeight;

		SimpleSectionImageDescriptor(Color[] colors, int realtheight, int theight, int marginHeight) {
			super(colors, realtheight);
			fTheight = theight;
			fMarginHeight = marginHeight;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SimpleSectionImageDescriptor) {
				SimpleSectionImageDescriptor id = (SimpleSectionImageDescriptor) obj;
				if (super.equals(obj) && id.fTheight == fTheight && id.fMarginHeight == fMarginHeight)
					return true;
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hash = super.hashCode();
			hash = hash * 7 + Integer.hashCode(fTheight);
			return hash * 7 + Integer.hashCode(fMarginHeight);
		}

		@Override
		public ImageData getImageData() {
			return null;
		}

		@Override
		public Image createImage(boolean returnMissingImageOnError, Device device) {
			Image image = new Image(device, 1, fLength);
			Color originalBgColor = new Color(device, fRGBs[0]);
			Color color1 = new Color(device, fRGBs[1]);
			image.setBackground(originalBgColor);
			GC gc = new GC(image);
			gc.setBackground(color1);
			gc.fillRectangle(0, fMarginHeight + 2, 1, fTheight - fMarginHeight - 3);
			gc.setBackground(originalBgColor);
			gc.fillRectangle(0, fTheight - fMarginHeight - 4, 1, 4);
			gc.dispose();
			return image;
		}
	}

	private class SimpleSectionGradientImageDescriptor extends SimpleSectionImageDescriptor {

		SimpleSectionGradientImageDescriptor(Color color1, Color color2, int realtheight,
				int theight,
				int marginHeight) {
			super(new Color[] { color1, color2 }, realtheight, theight, marginHeight);
		}

		@Override
		public Image createImage(boolean returnMissingImageOnError, Device device) {
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

			return image;
		}

	}

	public Image getSectionGradientImage(Color color1, Color color2, int realtheight, int theight, int marginHeight,
			Display display) {
		if (color1 == null || color1.isDisposed())
			return null;
		AbstractImageDescriptor desc = new SimpleSectionGradientImageDescriptor(color1, color2,
				realtheight, theight, marginHeight);
		return getGradient(desc, display);
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
		for (Color color : colors)
			if (color == null || color.isDisposed())
				return null;
		if (bg != null && bg.isDisposed())
			return null;
		AbstractImageDescriptor desc = new ComplexImageDescriptor(colors, length, percents, vertical, bg);
		return getGradient(desc, display);
	}

	private synchronized Image getGradient(AbstractImageDescriptor desc, Display display) {
		checkHashMaps();
		Image result = manager.getResourceManager(display).create(desc);
		descriptors.put(Integer.valueOf(result.hashCode()), desc);
		return result;
	}

	public synchronized boolean markFinished(Image image, Display display) {
		checkHashMaps();
		Integer imageHashCode = Integer.valueOf(image.hashCode());
		AbstractImageDescriptor desc = descriptors.get(imageHashCode);
		if (desc != null) {
			LocalResourceManager resourceManager = manager.getResourceManager(display);
			resourceManager.destroy(desc);
			if (resourceManager.find(desc) == null) {
				descriptors.remove(imageHashCode);
				validateHashMaps();
			}
			return true;
		}
		// if the image was not found, dispose of it for the caller
		image.dispose();
		validateHashMaps();
		return false;
	}

	private void checkHashMaps() {
		if (descriptors == null)
			descriptors = new HashMap<>();
	}

	private void validateHashMaps() {
		if (descriptors.isEmpty())
			descriptors = null;
	}
}
