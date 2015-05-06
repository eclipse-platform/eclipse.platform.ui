/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *     Kai Toedter - added radial gradient support
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 461688
 *     Robert Roth <robert.roth.off@gmail.com> - Bug 283255
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 466646
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.e4.ui.css.core.dom.properties.Gradient;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class GradientBackgroundListener implements Listener {
	private static Map<Control, GradientBackgroundListener> handlers = new HashMap<Control, GradientBackgroundListener>();
	private static boolean isRadialSupported;

	private Gradient grad;
	private final Control control;
	private boolean radialGradient;
	Image gradientImage;

	private DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent e) {
			GradientBackgroundListener.remove(control);
		}
	};

	static {
		// The following code tries to instantiate a
		// java.awt.RadialGradientPaint that is only available in Java 6 and
		// higher. Since the BREE is set to J2SE-1.5, reflection is used.
		try {
			Class.forName("java.awt.RadialGradientPaint"); //$NON-NLS-1$
			isRadialSupported = true;
		} catch (Exception e) {
			//			System.err
			//					.println("Warning - radial gradients are only supported in Java 6 and higher, using linear gradient instead"); //$NON-NLS-1$
			isRadialSupported = false;
		}

	}

	private GradientBackgroundListener(Control control, Gradient grad) {
		this.grad = grad;
		this.control = control;
		control.addListener(SWT.Resize, this);
		control.addDisposeListener(disposeListener);
	}

	public void dispose() {
		grad = null;
		if (control != null && !control.isDisposed()) {
			control.removeListener(SWT.Resize, this);
			control.removeDisposeListener(disposeListener);
			if (control.getBackgroundImage() == gradientImage) {
				control.setBackgroundImage(null);
			}
		}
		if (gradientImage != null && !gradientImage.isDisposed()) {
			gradientImage.dispose();
		}
		gradientImage = null;
	}

	public static void handle(Control control, Gradient grad) {
		GradientBackgroundListener handler = handlers.get(control);
		if (handler == null) {
			handler = new GradientBackgroundListener(control, grad);
			handlers.put(control, handler);
			handler.handleEvent(null);
		} else {
			handler.grad = grad;
			handler.handleEvent(null);
		}
	}

	public static void remove(Control control) {
		GradientBackgroundListener handler = handlers.remove(control);
		if (handler != null) {
			handler.dispose();
		}
	}

	@Override
	public void handleEvent(Event event) {
		Point size = control.getSize();
		if (size.x <= 0 || size.y <= 0) {
			return;
		}

		// hold onto our old image for disposal, if necessary
		Image oldImage = control.getBackgroundImage();
		if(oldImage != gradientImage) {
			oldImage = null;
		}

		/*
		 * Draw the new background.  Radial backgrounds have to be generated
		 * for the full size of the control's size; linear backgrounds are
		 * just a slice for the control's height that is then repeated.
		 */

		// If Java 5 or lower is used, radial gradients are not supported yet
		// and they will be replaced by linear gradients
		if (grad.isRadial() && isRadialSupported) {
			List<java.awt.Color> colors = new ArrayList<java.awt.Color>();
			for (Object rgbObj : grad.getRGBs()) {
				if (rgbObj instanceof RGBA) {
					RGBA rgba = (RGBA) rgbObj;
					java.awt.Color color = new java.awt.Color(rgba.rgb.red, rgba.rgb.green, rgba.rgb.blue, rgba.alpha);
					colors.add(color);
				} else if (rgbObj instanceof RGB) {
					RGB rgb = (RGB) rgbObj;
					java.awt.Color color = new java.awt.Color(rgb.red, rgb.green, rgb.blue);
					colors.add(color);
				}
			}

			BufferedImage image = getBufferedImage(size.x, size.y, colors,
					CSSSWTColorHelper.getPercents(grad));
			// long startTime = System.currentTimeMillis();
			ImageData imagedata = convertToSWT(image);
			// System.out.println("Conversion took "
			// + (System.currentTimeMillis() - startTime) + " ms");
			gradientImage = new Image(control.getDisplay(), imagedata);
			radialGradient = true;
		} else if (oldImage == null || oldImage.isDisposed()
				|| oldImage.getBounds().height != size.y || radialGradient
				|| event == null) {
			radialGradient = false;
			boolean verticalGradient = grad.getVerticalGradient();
			int x = verticalGradient? 2 : size.x;
			int y = verticalGradient ? size.y : 2;
			gradientImage = new Image(control.getDisplay(), x, y);
			GC gc = new GC(gradientImage);
			List<Color> colors = new ArrayList<Color>();
			for (Object rgbObj : grad.getRGBs()) {
				if (rgbObj instanceof RGBA) {
					RGBA rgba = (RGBA) rgbObj;
					Color color = new Color(control.getDisplay(), rgba);
					colors.add(color);
				} else if (rgbObj instanceof RGB) {
					RGB rgb = (RGB) rgbObj;
					Color color = new Color(control.getDisplay(), rgb);
					colors.add(color);
				}
			}
			fillGradient(gc, new Rectangle(0, 0, x, y), colors,
					CSSSWTColorHelper.getPercents(grad), grad.getVerticalGradient());
			gc.dispose();
			for (Color c : colors) {
				c.dispose(); // Dispose colors too.
			}
		}
		if (gradientImage != null) {
			control.setBackgroundImage(gradientImage);
		}
		if (oldImage != null && oldImage != gradientImage) {
			oldImage.dispose();
		}
	}

	/*
	 * Fills a gradient rectangle in the specified gc with the specified colors
	 * and percentages.
	 *
	 * @param gc @param rect @param gradientColors @param gradientPercents
	 *
	 * @param gradientVertical
	 */
	private static void fillGradient(GC gc, Rectangle rect,
			List<Color> gradientColors, int[] gradientPercents,
			boolean gradientVertical) {
		Color background = gradientColors.get(gradientColors.size() - 1);
		if (gradientColors.size() == 1) {
			if (gradientColors.get(0) != null) {
				gc.setBackground(gradientColors.get(0));
			}
			gc.fillRectangle(rect.x, rect.y, rect.width, rect.height);
		} else {
			Color lastColor = gradientColors.get(0);
			int pos = (gradientVertical) ? rect.y : rect.x;
			int loopCount = Math.min(gradientColors.size() - 1,
					gradientPercents.length);
			for (int i = 0; i < loopCount; ++i) {
				gc.setForeground(lastColor);
				lastColor = gradientColors.get(i + 1);
				if (lastColor == null) {
					lastColor = background;
				}
				gc.setBackground(lastColor);
				int grpercent = ((Integer) gradientPercents[i]).intValue();
				if (gradientVertical) {
					final int gradientHeight = (grpercent * rect.height / 100)
							- (pos - rect.y);
					gc.fillGradientRectangle(rect.x, pos, rect.width,
							gradientHeight, true);
					pos += gradientHeight;
				} else {
					final int gradientWidth = (grpercent * rect.width / 100)
							- (pos - rect.x);
					gc.fillGradientRectangle(pos, rect.y, gradientWidth,
							rect.height, false);
					pos += gradientWidth;
				}
			}
			if (gradientVertical && pos < rect.height) {
				gc.setBackground(background);
				gc.fillRectangle(rect.x, pos, rect.width, rect.height - pos);
			}
			if (!gradientVertical && pos < rect.width) {
				gc.setBackground(background);
				gc.fillRectangle(pos, rect.y, rect.width - pos, rect.height);
			}
		}
	}

	/**
	 * Returns a BufferedImage that renders a radial gradient. This is a
	 * workaround since SWT does not support radial gradients yet.
	 *
	 * @param width
	 *            image width
	 * @param height
	 *            image height
	 * @param colors
	 *            a list of colors that define the gradients
	 * @param percents
	 *            a list of percents that define the percents of above colors
	 * @return the image
	 */
	private BufferedImage getBufferedImage(int width, int height,
			List<java.awt.Color> colors, int[] percents) {
		java.awt.Color[] colorArray = colors.toArray(new java.awt.Color[] {});
		float[] fractions = new float[percents.length + 1];
		fractions[0] = 0.0f;
		for (int i = 1; i <= percents.length; i++) {
			fractions[i] = percents[i - 1] / 100.0f;
		}
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D) image.getGraphics();

		// The following code tries to instantiate a
		// java.awt.RadialGradientPaint that is only available in Java 6 and
		// higher. Since the BREE is set to J2SE-1.5, reflection is used. If
		// this code is run with a Java version below 6, the radial gradient is
		// replaced by a flat background color (the first color in the color
		// array).
		try {
			Class<?> radialGradientPaintClass = Class
					.forName("java.awt.RadialGradientPaint"); //$NON-NLS-1$
			Class<?>[] classes = radialGradientPaintClass.getClasses();
			int i;
			for (i = 0; i < classes.length; i++) {
				if ("java.awt.MultipleGradientPaint.CycleMethod" //$NON-NLS-1$
						.equals(classes[i].getCanonicalName())) {
					break;
				}
			}
			Constructor<?> ctor = radialGradientPaintClass
					.getConstructor(new Class[] { java.awt.geom.Point2D.class,
							float.class, java.awt.geom.Point2D.class,
							float[].class, java.awt.Color[].class, classes[i] });

			final Object radialGradientPaint = ctor.newInstance(new Object[] {
					new Point2D.Double(width / 2.0, 0), width,
					new Point2D.Double(width / 2.0, 0.0), fractions,
					colorArray, classes[i].getEnumConstants()[0] });
			g2.setPaint((java.awt.Paint) radialGradientPaint);
		} catch (Exception e) {
			System.err
			.println("Warning - radial gradients are only supported in Java 6 and higher, using flat background color instead"); //$NON-NLS-1$
			g2.setColor(colorArray[0]);
		}

		g2.fillRect(0, 0, width, height);
		return image;
	}

	/**
	 * Converts a AWT BufferedImage to an SWT ImageData. This is a workaround
	 * since SWT does not support radial gradients yet.
	 *
	 * @param bufferedImage
	 *            the source AWT BufferedImage
	 * @return the converted SWT ImageData
	 */
	private ImageData convertToSWT(BufferedImage bufferedImage) {
		int[] bufferedImageData = ((DataBufferInt) bufferedImage.getData()
				.getDataBuffer()).getData();
		ImageData imageData = new ImageData(bufferedImage.getWidth(),
				bufferedImage.getHeight(), 24, new PaletteData(0xff0000,
						0x00ff00, 0x0000ff));
		imageData.setPixels(0, 0, bufferedImageData.length, bufferedImageData,
				0);
		return imageData;
	}
}
