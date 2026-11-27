/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

/**
 * Zoom state and zoom arithmetic of the image viewer, without any widget.
 * <p>
 * The viewer starts in <em>fit</em> mode: the image is shown as large as
 * possible without being enlarged beyond 100% and without scrollbars. The first
 * zoom step leaves fit mode and continues from the scale that was shown.
 * </p>
 */
public final class ImageZoomModel {

	/** The scales reached by the zoom in and zoom out steps. */
	private static final double[] LEVELS = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 1.5, 2, 3, 5, 7, 10, 15,
			20 };

	/** Smallest scale that a zoom step can produce. */
	public static final double MIN_SCALE = LEVELS[0];

	/** Largest scale that a zoom step can produce. */
	public static final double MAX_SCALE = LEVELS[LEVELS.length - 1];

	/** Relative scale change of one mouse wheel notch. */
	public static final double WHEEL_STEP = 0.075;

	/** Above this scale the image is drawn without smoothing. */
	public static final double PIXELATED_ABOVE = 3;

	private static final double EPSILON = 1e-6;

	private boolean fit = true;
	private double scale = 1;

	/**
	 * @return <code>true</code> if the image is fitted to the viewport
	 */
	public boolean isFit() {
		return fit;
	}

	/**
	 * Returns to fit mode.
	 */
	public void setFit() {
		fit = true;
	}

	/**
	 * @param fitScale the scale that fit mode shows right now
	 * @return the scale that is shown right now
	 */
	public double getScale(double fitScale) {
		return fit ? fitScale : scale;
	}

	/**
	 * Sets a scale that was chosen by a zoom out, by the mouse wheel or by the
	 * actual size. If it is the fit scale, the model returns to fit mode, so that
	 * the image follows the size of the viewport again.
	 */
	private void selectScale(double newScale, double fitScale) {
		scale = newScale;
		fit = Math.abs(newScale - fitScale) < EPSILON;
	}

	/**
	 * Zooms in to the next level above the current scale.
	 *
	 * @param fitScale the scale that fit mode shows right now
	 * @return <code>true</code> if the scale changed
	 */
	public boolean zoomIn(double fitScale) {
		double current = getScale(fitScale);
		for (double level : LEVELS) {
			if (level > current + EPSILON) {
				scale = level;
				fit = false;
				return true;
			}
		}
		return false;
	}

	/**
	 * Zooms out to the next level below the current scale.
	 *
	 * @param fitScale the scale that fit mode shows right now
	 * @return <code>true</code> if the scale changed
	 */
	public boolean zoomOut(double fitScale) {
		double current = getScale(fitScale);
		for (int i = LEVELS.length - 1; i >= 0; i--) {
			if (LEVELS[i] < current - EPSILON) {
				selectScale(LEVELS[i], fitScale);
				return true;
			}
		}
		return false;
	}

	/**
	 * Shows the image at 100%. If that is the fit scale, the model returns to fit
	 * mode.
	 *
	 * @param fitScale the scale that fit mode shows right now
	 * @return <code>true</code> if the scale changed
	 */
	public boolean actualSize(double fitScale) {
		if (Math.abs(getScale(fitScale) - 1) < EPSILON) {
			return false;
		}
		selectScale(1, fitScale);
		return true;
	}

	/**
	 * Zooms continuously, as done by the mouse wheel.
	 * <p>
	 * The smallest result is {@link #MIN_SCALE}, but never more than the scale that
	 * is shown or than the fit scale. A large image can be fitted below
	 * {@link #MIN_SCALE}, and zooming out must not enlarge it.
	 * </p>
	 *
	 * @param notches  number of wheel notches, positive to zoom in, negative to
	 *                 zoom out
	 * @param fitScale the scale that fit mode shows right now
	 * @return <code>true</code> if the scale changed
	 */
	public boolean zoomByWheel(double notches, double fitScale) {
		double factor = Math.max(0.01, 1 + notches * WHEEL_STEP);
		return zoomTo(getScale(fitScale) * factor, fitScale);
	}

	/**
	 * Zooms to the given scale, as done by a pinch gesture.
	 * <p>
	 * The limits are those of {@link #zoomByWheel(double, double)}: the smallest
	 * result is {@link #MIN_SCALE}, but never more than the scale that is shown or
	 * than the fit scale, and the largest is {@link #MAX_SCALE}.
	 * </p>
	 *
	 * @param target   the wanted scale
	 * @param fitScale the scale that fit mode shows right now
	 * @return <code>true</code> if the scale changed
	 */
	public boolean zoomTo(double target, double fitScale) {
		double current = getScale(fitScale);
		double minimum = Math.min(MIN_SCALE, Math.min(fitScale, current));
		double result = Math.max(minimum, Math.min(MAX_SCALE, target));
		if (Math.abs(result - current) < EPSILON) {
			return false;
		}
		selectScale(result, fitScale);
		return true;
	}

	/**
	 * Computes the scale at which the whole image is visible. An image that is
	 * smaller than the viewport is not enlarged.
	 *
	 * @return the fit scale, 1 if one of the sizes is not positive
	 */
	public static double fitScale(int imageWidth, int imageHeight, int viewWidth, int viewHeight) {
		if (imageWidth <= 0 || imageHeight <= 0 || viewWidth <= 0 || viewHeight <= 0) {
			return 1;
		}
		return Math.min(1, Math.min((double) viewWidth / imageWidth, (double) viewHeight / imageHeight));
	}

	/**
	 * @return the size of an image side at the given scale, in pixels
	 */
	public static int scaledSize(int imageSize, double scale) {
		if (imageSize <= 0) {
			return 0;
		}
		// Sides above 107,374,183 px at the largest scale (2000%) would overflow int.
		return Math.max(1, (int) Math.round(imageSize * scale));
	}

	/**
	 * Returns the position of the scaled image inside the viewport along one axis.
	 * An image that is smaller than the viewport is centered, a larger one is
	 * moved by the scroll offset.
	 *
	 * @param scaledSize size of the scaled image
	 * @param viewSize   size of the viewport
	 * @param scroll     scroll offset, ignored if the image is not larger
	 * @return the viewport coordinate of the left (or top) edge of the image
	 */
	public static int origin(int scaledSize, int viewSize, int scroll) {
		return scaledSize <= viewSize ? (viewSize - scaledSize) / 2 : -scroll;
	}

	/**
	 * Converts a viewport coordinate into an image coordinate.
	 *
	 * @param viewPos coordinate in the viewport
	 * @param origin  value of {@link #origin(int, int, int)}
	 * @param scale   the scale that was shown
	 * @return the image coordinate under <code>viewPos</code>
	 */
	public static double imageCoordinate(int viewPos, int origin, double scale) {
		return (viewPos - origin) / scale;
	}

	/**
	 * Computes the scroll offset that puts an image coordinate at a viewport
	 * coordinate. This keeps the point under the mouse while zooming. The result is
	 * not limited to the scrollable range.
	 *
	 * @param imageCoord the image coordinate that must stay in place
	 * @param viewPos    the viewport coordinate it must stay at
	 * @param scale      the new scale
	 * @return the scroll offset
	 */
	public static int scrollOffsetFor(double imageCoord, int viewPos, double scale) {
		return (int) Math.round(imageCoord * scale) - viewPos;
	}
}
