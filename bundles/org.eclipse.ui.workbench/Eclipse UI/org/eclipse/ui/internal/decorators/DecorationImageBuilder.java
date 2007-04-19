/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.decorators;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

/**
 * DecorationImageBuilder is a utility class for merging images without data
 * loss.
 * 
 * @since 3.3
 * 
 */
class DecorationImageBuilder {

	private static final int TOP_LEFT = LightweightDecoratorDefinition.TOP_LEFT;
	private static final int TOP_RIGHT = LightweightDecoratorDefinition.TOP_RIGHT;
	private static final int BOTTOM_LEFT = LightweightDecoratorDefinition.BOTTOM_LEFT;
	private static final int BOTTOM_RIGHT = LightweightDecoratorDefinition.BOTTOM_RIGHT;
	private static final int UNDERLAY = LightweightDecoratorDefinition.UNDERLAY;

	private static final PaletteData ALPHA_PALETTE, BW_PALETTE;
	static {
		RGB[] rgbs = new RGB[256];
		for (int i = 0; i < rgbs.length; i++) {
			rgbs[i] = new RGB(i, i, i);
		}
		ALPHA_PALETTE = new PaletteData(rgbs);
		BW_PALETTE = new PaletteData(new RGB[] { new RGB(0, 0, 0),
				new RGB(255, 255, 255) });
	}

	private static int getTransparencyDepth(ImageData data) {
		if (data.maskData != null && data.depth == 32) {
			for (int i = 0; i < data.data.length; i += 4) {
				if (data.data[i] != 0)
					return 8;
			}
		}
		if (data.maskData != null || data.transparentPixel != -1)
			return 1;
		if (data.alpha != -1 || data.alphaData != null)
			return 8;
		return 0;
	}

	private static ImageData getTransparency(ImageData data,
			int transparencyDepth) {
		if (data == null)
			return null;
		if (transparencyDepth == 1)
			return data.getTransparencyMask();
		ImageData mask = null;
		if (data.maskData != null && data.depth == 32) {
			ImageData m = data.getTransparencyMask();
			mask = new ImageData(data.width, data.height, 8, ALPHA_PALETTE,
					data.width, new byte[data.width * data.height]);
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int alpha = data.getPixel(x, y) & 0xFF;
					if (alpha == 0) {
						if (m.getPixel(x, y) != 0)
							alpha = 255;
					}
					mask.setPixel(x, y, alpha);
				}
			}
		} else if (data.maskData != null || data.transparentPixel != -1) {
			ImageData m = data.getTransparencyMask();
			mask = new ImageData(data.width, data.height, 8, ALPHA_PALETTE,
					data.width, new byte[data.width * data.height]);
			for (int y = 0; y < mask.height; y++) {
				for (int x = 0; x < mask.width; x++) {
					mask.setPixel(x, y, m.getPixel(x, y) != 0 ? (byte) 255 : 0);
				}
			}
		} else if (data.alpha != -1) {
			mask = new ImageData(data.width, data.height, 8, ALPHA_PALETTE,
					data.width, new byte[data.width * data.height]);
			for (int i = 0; i < mask.data.length; i++) {
				mask.data[i] = (byte) data.alpha;
			}
		} else if (data.alphaData != null) {
			mask = new ImageData(data.width, data.height, 8, ALPHA_PALETTE,
					data.width, data.alphaData);
		} else {
			mask = new ImageData(data.width, data.height, 8, ALPHA_PALETTE,
					data.width, new byte[data.width * data.height]);
			for (int i = 0; i < mask.data.length; i++) {
				mask.data[i] = (byte) 255;
			}
		}
		return mask;
	}

	private static void composite(ImageData dst, ImageData src, int xOffset,
			int yOffset) {
		if (dst.depth == 1) {
			for (int y = 0, dstY = y + yOffset; y < src.height; y++, dstY++) {
				for (int x = 0, dstX = x + xOffset; x < src.width; x++, dstX++) {
					if (0 <= dstX && dstX < dst.width && 0 <= dstY
							&& dstY < dst.height) {
						if (src.getPixel(x, y) != 0) {
							dst.setPixel(dstX, dstY, 1);
						}
					}
				}
			}
		} else if (dst.depth == 8) {
			for (int y = 0, dstY = y + yOffset; y < src.height; y++, dstY++) {
				for (int x = 0, dstX = x + xOffset; x < src.width; x++, dstX++) {
					if (0 <= dstX && dstX < dst.width && 0 <= dstY
							&& dstY < dst.height) {
						int srcAlpha = src.getPixel(x, y);
						int dstAlpha = dst.getPixel(dstX, dstY);
						dstAlpha += (srcAlpha - dstAlpha) * srcAlpha / 255;
						dst.setPixel(dstX, dstY, dstAlpha);
					}
				}
			}
		}
	}

	/**
	 * Create a composite image by underlaying and overlaying the base image.
	 * @param device
	 * @param base
	 * @param overlay
	 * @return Image
	 */
	static Image compositeImage(Device device, ImageData base,
			ImageData[] overlay) {
		if (base == null)
			return null;
		Image image = new Image(device, new ImageData(base.width, base.height,
				24, new PaletteData(0xff, 0xff00, 0xff00000)));
		GC gc = new GC(image);
		ImageData src;
		int maskDepth = 0, baseMaskDepth = 0;
		ImageData underlay = src = overlay.length > UNDERLAY ? overlay[UNDERLAY]
				: null;
		if (src != null) {
			maskDepth = Math.max(maskDepth, getTransparencyDepth(src));
			Image img = new Image(device, src);
			gc.drawImage(img, 0, 0);
			img.dispose();
		}
		src = base;
		if (base != null) {
			maskDepth = Math.max(maskDepth,
					baseMaskDepth = getTransparencyDepth(src));
			Image img = new Image(device, src);
			gc.drawImage(img, 0, 0);
			img.dispose();
		}
		ImageData topLeft = src = overlay[TOP_LEFT];
		if (src != null) {
			maskDepth = Math.max(maskDepth, getTransparencyDepth(src));
			Image img = new Image(device, src);
			gc.drawImage(img, 0, 0);
			img.dispose();
		}
		ImageData topRight = src = overlay[TOP_RIGHT];
		if (src != null) {
			maskDepth = Math.max(maskDepth, getTransparencyDepth(src));
			Image img = new Image(device, src);
			gc.drawImage(img, base.width - src.width, 0);
			img.dispose();
		}
		ImageData bottomLeft = src = overlay[BOTTOM_LEFT];
		if (src != null) {
			maskDepth = Math.max(maskDepth, getTransparencyDepth(src));
			Image img = new Image(device, src);
			gc.drawImage(img, 0, base.height - src.height);
			img.dispose();
		}
		ImageData bottomRight = src = overlay[BOTTOM_RIGHT];
		if (src != null) {
			maskDepth = Math.max(maskDepth, getTransparencyDepth(src));
			Image img = new Image(device, src);
			gc.drawImage(img, base.width - src.width, base.height - src.height);
			img.dispose();
		}
		gc.dispose();
		if (baseMaskDepth > 0) {
			ImageData newData = image.getImageData();
			image.dispose();
			ImageData mask = null;
			switch (maskDepth) {
			case 1:
				mask = new ImageData(base.width, base.height, maskDepth,
						BW_PALETTE);
				break;
			case 8:
				mask = new ImageData(base.width, base.height, maskDepth,
						ALPHA_PALETTE, base.width, new byte[base.width
								* base.height]);
				break;
			}
			src = getTransparency(underlay, maskDepth);
			if (src != null)
				composite(mask, src, 0, 0);
			src = getTransparency(base, maskDepth);
			if (src != null)
				composite(mask, src, 0, 0);
			src = getTransparency(topLeft, maskDepth);
			if (src != null)
				composite(mask, src, 0, 0);
			src = getTransparency(topRight, maskDepth);
			if (src != null)
				composite(mask, src, mask.width - src.width, 0);
			src = getTransparency(bottomLeft, maskDepth);
			if (src != null)
				composite(mask, src, 0, mask.height - src.height);
			src = getTransparency(bottomRight, maskDepth);
			if (src != null)
				composite(mask, src, mask.width - src.width, mask.height
						- src.height);
			switch (maskDepth) {
			case 1:
				newData.maskData = mask.data;
				newData.maskPad = mask.scanlinePad;
				break;
			case 8:
				newData.alphaData = mask.data;
				break;
			}
			image = new Image(device, newData);
		}
		return image;
	}

}