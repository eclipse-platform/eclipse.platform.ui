package org.eclipse.ui.internal.decorators;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

public class DecorationImageBuilder {

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

	static final int TOP_LEFT = LightweightDecoratorDefinition.TOP_LEFT;
	static final int TOP_RIGHT = LightweightDecoratorDefinition.TOP_RIGHT;
	static final int BOTTOM_LEFT = LightweightDecoratorDefinition.BOTTOM_LEFT;
	static final int BOTTOM_RIGHT = LightweightDecoratorDefinition.BOTTOM_RIGHT;
	static final int UNDERLAY = LightweightDecoratorDefinition.UNDERLAY;

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

	public static Image compositeImage(Image base, Image[] overlay) {
		if (base == null)
			return null;
		Device device = base.getDevice();
		ImageData baseData = base.getImageData();
		int maskDepth = getTransparencyDepth(baseData), baseMaskDepth = maskDepth;
		ImageData imageData = new ImageData(baseData.width, baseData.height,
				24, new PaletteData(0xff, 0xff00, 0xff00000));
		Image image = new Image(device, imageData);
		// Image image = new Image(device, baseData.width, baseData.height);
		GC gc = new GC(image);
		// gc.setBackground(device.getSystemColor(SWT.COLOR_BLACK));
		// gc.fillRectangle(0, 0, baseData.width, baseData.height);
		Image underlay = overlay.length > UNDERLAY ? overlay[UNDERLAY] : null;
		ImageData underlayData = null;
		if (underlay != null) {
			underlayData = underlay.getImageData();
			maskDepth = Math.max(maskDepth, getTransparencyDepth(underlayData));
			gc.drawImage(underlay, 0, 0);
		}
		gc.drawImage(base, 0, 0);
		ImageData topLeftData = null;
		if (overlay[TOP_LEFT] != null) {
			topLeftData = overlay[TOP_LEFT].getImageData();
			maskDepth = Math.max(maskDepth, getTransparencyDepth(topLeftData));
			gc.drawImage(overlay[TOP_LEFT], 0, 0);
		}
		ImageData topRightData = null;
		if (overlay[TOP_RIGHT] != null) {
			topRightData = overlay[TOP_RIGHT].getImageData();
			maskDepth = Math.max(maskDepth, getTransparencyDepth(topRightData));
			gc.drawImage(overlay[TOP_RIGHT], baseData.width
					- topRightData.width, 0);
		}
		ImageData bottomLeftData = null;
		if (overlay[BOTTOM_LEFT] != null) {
			bottomLeftData = overlay[BOTTOM_LEFT].getImageData();
			maskDepth = Math.max(maskDepth,
					getTransparencyDepth(bottomLeftData));
			gc.drawImage(overlay[BOTTOM_LEFT], 0, baseData.height
					- bottomLeftData.height);
		}
		ImageData bottomRightData = null;
		if (overlay[BOTTOM_RIGHT] != null) {
			bottomRightData = overlay[BOTTOM_RIGHT].getImageData();
			maskDepth = Math.max(maskDepth,
					getTransparencyDepth(bottomRightData));
			gc.drawImage(overlay[BOTTOM_RIGHT], baseData.width
					- bottomRightData.width, baseData.height
					- bottomRightData.height);
		}
		gc.dispose();
		if (baseMaskDepth > 0) {
			ImageData newData = image.getImageData();
			image.dispose();
			ImageData mask = null;
			switch (maskDepth) {
			case 1:
				mask = new ImageData(baseData.width, baseData.height,
						maskDepth, BW_PALETTE);
				break;
			case 8:
				mask = new ImageData(baseData.width, baseData.height,
						maskDepth, ALPHA_PALETTE, baseData.width,
						new byte[baseData.width * baseData.height]);
				break;
			}
			if (underlayData != null) {
				ImageData src = getTransparency(underlayData, maskDepth);
				composite(mask, src, 0, 0);
			}
			if (baseData != null) {
				ImageData src = getTransparency(baseData, maskDepth);
				composite(mask, src, 0, 0);
			}
			if (topLeftData != null) {
				ImageData src = getTransparency(topLeftData, maskDepth);
				composite(mask, src, 0, 0);
			}
			if (topRightData != null) {
				ImageData src = getTransparency(topRightData, maskDepth);
				composite(mask, src, mask.width - src.width, 0);
			}
			if (bottomLeftData != null) {
				ImageData src = getTransparency(bottomLeftData, maskDepth);
				composite(mask, src, 0, mask.height - src.height);
			}
			if (bottomRightData != null) {
				ImageData src = getTransparency(bottomRightData, maskDepth);
				composite(mask, src, mask.width - src.width, mask.height
						- src.height);
			}
			switch (maskDepth) {
			case 1:
				newData.maskData = mask.data;
				newData.maskPad = mask.scanlinePad;
				break;
			case 8:
				newData.alphaData = mask.data;
				break;
			}
			image = new Image(base.getDevice(), newData);
		}
		return image;
	}

	static final int SPACING = 5;

}