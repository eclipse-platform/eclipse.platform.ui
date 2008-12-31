/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text.revisions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

import org.eclipse.core.runtime.Assert;

/**
 * Utility for color operations.
 *
 * @since 3.3
 */
public final class Colors {
	/*
	 * Implementation note: Color computation assumes sRGB, which is probably not true, and does not
	 * always give good results. CIE based algorithms would be better, see
	 * http://www.w3.org/TR/PNG-ColorAppendix.html and http://en.wikipedia.org/wiki/Lab_color_space
	 */

	/**
	 * Returns the human-perceived brightness of a color as float in [0.0, 1.0]. The used RGB
	 * weights come from http://www.poynton.com/notes/colour_and_gamma/ColorFAQ.html#RTFToC9.
	 *
	 * @param rgb the color
	 * @return the gray-scale value
	 */
	public static float brightness(RGB rgb) {
		return Math.min(1f, (0.2126f * rgb.red + 0.7152f * rgb.green + 0.0722f * rgb.blue + 0.5f) / 255f);
	}

	/**
	 * Normalizes a color in its perceived brightness. Yellows are darkened, while blues and reds
	 * are lightened. Depending on the hue, the brightness range within the RGB gamut may be
	 * different, outside values are clipped. Note that this is an approximation; the returned RGB
	 * is not guaranteed to have the requested {@link #brightness(RGB) brightness}.
	 *
	 * @param color the color to normalize
	 * @param brightness the requested brightness, in [0,&nbsp;1]
	 * @return a normalized version of <code>color</code>
	 * @see #brightness(RGB)
	 */
	public static RGB adjustBrightness(RGB color, float brightness) {
		float[] hsi= toHSI(color);
		float psychoFactor= brightness - brightness(color);
		float weight= 0.5f; // found by trial and error
		hsi[2]= Math.max(0, Math.min(1.0f, hsi[2] + psychoFactor * weight));
		color= fromHSI(hsi);
		return color;
	}

	/**
	 * Converts an {@link RGB} to an <a href="http://en.wikipedia.org/wiki/HSL_color_space">HSI</a>
	 * triplet.
	 *
	 * @param color the color to convert
	 * @return the HSI float array of length 3
	 */
	private static float[] toHSI(RGB color) {
		float r = color.red / 255f;
		float g = color.green / 255f;
		float b = color.blue / 255f;
		float max = Math.max(Math.max(r, g), b);
		float min = Math.min(Math.min(r, g), b);
		float delta = max - min;
		float maxPlusMin= max + min;
		float intensity = maxPlusMin / 2;
		float saturation= intensity < 0.5 ? delta / maxPlusMin : delta / (2 - maxPlusMin);

		float hue = 0;
		if (delta != 0) {
			if (r == max) {
				hue = (g  - b) / delta;
			} else {
				if (g == max) {
					hue = 2 + (b - r) / delta;
				} else {
					hue = 4 + (r - g) / delta;
				}
			}
			hue *= 60;
			if (hue < 0) hue += 360;
		}
		return new float[] {hue, saturation, intensity};
	}

	/**
	 * Converts a <a href="http://en.wikipedia.org/wiki/HSL_color_space">HSI</a> triplet to an RGB.
	 *
	 * @param hsi the HSI values
	 * @return the RGB corresponding to the HSI spec
	 */
	private static RGB fromHSI(float[] hsi) {
		float r, g, b;
		float hue= hsi[0];
		float saturation= hsi[1];
		float intensity= hsi[2];
		if (saturation == 0) {
			r = g = b = intensity;
		} else {
			float temp2= intensity < 0.5f ? intensity * (1.0f + saturation) : (intensity + saturation) - (intensity * saturation);
			float temp1= 2f * intensity - temp2;
			if (hue == 360) hue = 0;
			hue /= 360;

			r= hue2RGB(temp1, temp2, hue + 1f/3f);
			g= hue2RGB(temp1, temp2, hue);
			b= hue2RGB(temp1, temp2, hue - 1f/3f);
		}

		int red = (int)(r * 255 + 0.5);
		int green = (int)(g * 255 + 0.5);
		int blue = (int)(b * 255 + 0.5);
		return new RGB(red, green, blue);
	}

	private static float hue2RGB(float t1, float t2, float hue) {
		if (hue < 0)
			hue += 1;
		else if (hue > 1)
			hue -= 1;
		if (6f * hue < 1)
			return t1 +(t2 - t1) * 6f * hue;
		if (2f * hue < 1)
			return t2;
		if (3f * hue < 2)
			return t1 + (t2 - t1) * (2f/3f - hue) * 6f;
		return t1;
	}

	/**
	 * Returns an RGB that lies between the given foreground and background
	 * colors using the given mixing factor. A <code>factor</code> of 1.0 will produce a
	 * color equal to <code>fg</code>, while a <code>factor</code> of 0.0 will produce one
	 * equal to <code>bg</code>.
	 * @param bg the background color
	 * @param fg the foreground color
	 * @param factor the mixing factor, must be in [0,&nbsp;1]
	 *
	 * @return the interpolated color
	 */
	public static RGB blend(RGB bg, RGB fg, float factor) {
		Assert.isLegal(bg != null);
		Assert.isLegal(fg != null);
		Assert.isLegal(factor >= 0f && factor <= 1f);

		float complement= 1f - factor;
		return new RGB(
				(int) (complement * bg.red + factor * fg.red),
				(int) (complement * bg.green + factor * fg.green),
				(int) (complement * bg.blue + factor * fg.blue)
		);
	}

	/**
	 * Returns an array of colors in a smooth palette from <code>start</code> to <code>end</code>.
	 * <p>
	 * The returned array has size <code>steps</code>, and the color at index 0 is <code>start</code>, the color
	 * at index <code>steps&nbsp;-&nbsp;1</code> is <code>end</code>.
	 *
	 * @param start the start color of the palette
	 * @param end the end color of the palette
	 * @param steps the requested size, must be &gt; 0
	 * @return an array of <code>steps</code> colors in the palette from <code>start</code> to <code>end</code>
	 */
	public static RGB[] palette(RGB start, RGB end, int steps) {
		Assert.isLegal(start != null);
		Assert.isLegal(end != null);
		Assert.isLegal(steps > 0);

		if (steps == 1)
			return new RGB[] { start };

		float step= 1.0f / (steps - 1);
		RGB[] gradient= new RGB[steps];
		for (int i= 0; i < steps; i++)
			gradient[i]= blend(start, end, step * i);

		return gradient;
	}

	/**
	 * Returns an array of colors with hues evenly distributed on the hue wheel defined by the <a
	 * href="http://en.wikipedia.org/wiki/HSV_color_space">HSB color space</a>. The returned array
	 * has size <code>steps</code>. The distance <var>d</var> between two successive colors is
	 * in [120&#176;,&nbsp;180&#176;].
	 * <p>
	 * The color at a given <code>index</code> has the hue returned by
	 * {@linkplain #computeHue(int) computeHue(index)}; i.e. the computed hues are not equidistant,
	 * but adaptively distributed on the color wheel.
	 * </p>
	 * <p>
	 * The first six colors returned correspond to the following {@link SWT} color constants:
	 * {@link SWT#COLOR_RED red}, {@link SWT#COLOR_GREEN green}, {@link SWT#COLOR_BLUE blue},
	 * {@link SWT#COLOR_YELLOW yellow}, {@link SWT#COLOR_CYAN cyan},
	 * {@link SWT#COLOR_MAGENTA magenta}.
	 * </p>
	 *
	 * @param steps the requested size, must be &gt;= 2
	 * @return an array of <code>steps</code> colors evenly distributed on the color wheel
	 */
	public static RGB[] rainbow(int steps) {
		Assert.isLegal(steps >= 2);

		RGB[] rainbow= new RGB[steps];
		for (int i= 0; i < steps; i++)
			rainbow[i]= new RGB(computeHue(i), 1f, 1f);

		return rainbow;
	}

	/**
	 * Returns an indexed hue in [0&#176;,&nbsp;360&#176;), distributing the hues evenly on the hue wheel
	 * defined by the <a href="http://en.wikipedia.org/wiki/HSV_color_space">HSB (or HSV) color
	 * space</a>. The distance <var>d</var> between two successive colors is in [120&#176;,&nbsp;180&#176;].
	 * <p>
	 * The first six colors returned correspond to the following {@link SWT} color constants:
	 * {@link SWT#COLOR_RED red}, {@link SWT#COLOR_GREEN green}, {@link SWT#COLOR_BLUE blue},
	 * {@link SWT#COLOR_YELLOW yellow}, {@link SWT#COLOR_CYAN cyan},
	 * {@link SWT#COLOR_MAGENTA magenta}.
	 * </p>
	 *
	 * @param index the index of the color, must be &gt;= 0
	 * @return a color hue in [0&#176;,&nbsp;360&#176;)
	 * @see RGB#RGB(float, float, float)
	 */
	public static float computeHue(final int index) {
		Assert.isLegal(index >= 0);
		/*
		 * Base 3 gives a nice partitioning for RGB colors with red, green, blue being the colors
		 * 0,1,2, and yellow, cyan, magenta colors 3,4,5.
		 */
		final int base= 3;
		final float range= 360f;

		// partition the baseRange by using the least significant bit to select one half of the
		// partitioning
		int baseIndex= index / base;
		float baseRange= range / base;
		float baseOffset= 0f;
		while (baseIndex > 0) {
			baseRange /= 2;
			int lsb= baseIndex % 2;
			baseOffset += lsb * baseRange;
			baseIndex >>= 1;
		}

		final int baseMod= index % base;
		final float hue= baseOffset + baseMod * range / base;
		Assert.isTrue(hue >= 0 && hue < 360);
		return hue;
	}

	private Colors() {
		// not instantiatable
	}

}
