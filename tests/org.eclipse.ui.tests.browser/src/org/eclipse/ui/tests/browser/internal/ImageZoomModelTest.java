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
package org.eclipse.ui.tests.browser.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.ui.internal.browser.ImageZoomModel;
import org.junit.jupiter.api.Test;

/**
 * Tests the zoom arithmetic of the image viewer. The widget itself is not
 * tested here.
 */
public class ImageZoomModelTest {

	private static final double DELTA = 1e-9;

	@Test
	public void testStartsInFitMode() {
		ImageZoomModel model = new ImageZoomModel();
		assertTrue(model.isFit());
		assertEquals(0.4, model.getScale(0.4), DELTA);
	}

	@Test
	public void testFirstZoomFromFitContinuesFromTheShownScale() {
		ImageZoomModel in = new ImageZoomModel();
		assertTrue(in.zoomIn(0.55));
		assertFalse(in.isFit());
		assertEquals(0.6, in.getScale(0.55), DELTA);

		ImageZoomModel out = new ImageZoomModel();
		assertTrue(out.zoomOut(0.55));
		assertEquals(0.5, out.getScale(0.55), DELTA);
	}

	@Test
	public void testEightZoomInStepsFromFullSizeReachTheMaximum() {
		ImageZoomModel model = new ImageZoomModel();
		double[] expected = { 1.5, 2, 3, 5, 7, 10, 15, 20 };
		for (double scale : expected) {
			assertTrue(model.zoomIn(1), "step to " + scale);
			assertEquals(scale, model.getScale(1), DELTA);
		}
		assertFalse(model.zoomIn(1), "no step above the maximum");
		assertEquals(ImageZoomModel.MAX_SCALE, model.getScale(1), DELTA);
	}

	@Test
	public void testNineZoomOutStepsFromFullSizeReachTheMinimum() {
		ImageZoomModel model = new ImageZoomModel();
		assertTrue(model.zoomOut(1)); // leaves fit mode at 0.9
		for (int i = 0; i < 8; i++) {
			assertTrue(model.zoomOut(1), "step " + (i + 2));
		}
		assertEquals(ImageZoomModel.MIN_SCALE, model.getScale(1), DELTA);
		assertFalse(model.zoomOut(1), "no step below the minimum");
	}

	@Test
	public void testSetFitReturnsToTheFitScale() {
		ImageZoomModel model = new ImageZoomModel();
		model.zoomIn(0.3);
		model.setFit();
		assertTrue(model.isFit());
		assertEquals(0.25, model.getScale(0.25), DELTA);
	}

	@Test
	public void testActualSizeShowsFullSizeAndLeavesFitMode() {
		ImageZoomModel model = new ImageZoomModel();
		assertTrue(model.actualSize(0.5));
		assertFalse(model.isFit());
		assertEquals(1, model.getScale(0.5), DELTA);
		assertFalse(model.actualSize(0.5), "already at full size");
	}

	@Test
	public void testActualSizeAtTheFitScaleStaysInFitMode() {
		ImageZoomModel model = new ImageZoomModel();
		assertFalse(model.actualSize(1), "a small image is fitted at full size");
		assertTrue(model.isFit());
		model.zoomIn(1);
		assertTrue(model.actualSize(1));
		assertTrue(model.isFit());
	}

	@Test
	public void testWheelZoomUsesSevenAndAHalfPercentPerNotch() {
		ImageZoomModel model = new ImageZoomModel();
		assertTrue(model.zoomByWheel(1, 1));
		assertEquals(1.075, model.getScale(1), DELTA);
		assertTrue(model.zoomByWheel(-1, 1));
		assertEquals(1.075 * 0.925, model.getScale(1), DELTA);
		assertFalse(model.isFit(), "above the fit scale the wheel stays in manual mode");
	}

	@Test
	public void testWheelZoomIsLimited() {
		ImageZoomModel model = new ImageZoomModel();
		for (int i = 0; i < 200; i++) {
			model.zoomByWheel(1, 1);
		}
		assertEquals(ImageZoomModel.MAX_SCALE, model.getScale(1), DELTA);
		assertFalse(model.zoomByWheel(1, 1));

		for (int i = 0; i < 200; i++) {
			model.zoomByWheel(-1, 1);
		}
		assertEquals(ImageZoomModel.MIN_SCALE, model.getScale(1), DELTA);
		assertFalse(model.zoomByWheel(-1, 1));
	}

	@Test
	public void testWheelZoomOutBelowMinimumDoesNotEnlargeFittedImage() {
		ImageZoomModel model = new ImageZoomModel();
		double fitScale = 0.05; // below MIN_SCALE, a very large image
		assertFalse(model.zoomByWheel(-1, fitScale));
		assertEquals(fitScale, model.getScale(fitScale), DELTA);
		assertTrue(model.isFit());
	}

	@Test
	public void testWheelZoomInFromBelowMinimumReturnsToTheFitScale() {
		ImageZoomModel model = new ImageZoomModel();
		double fitScale = 0.05;
		assertTrue(model.zoomByWheel(1, fitScale));
		assertEquals(fitScale * 1.075, model.getScale(fitScale), DELTA);
		assertFalse(model.isFit());
		assertTrue(model.zoomByWheel(-1, fitScale));
		assertEquals(fitScale, model.getScale(fitScale), DELTA);
		assertTrue(model.isFit());
		assertFalse(model.zoomByWheel(-1, fitScale));
		// the window was enlarged, a fitted image follows it
		assertEquals(0.08, model.getScale(0.08), DELTA);
	}

	@Test
	public void testZoomToSetsTheScale() {
		ImageZoomModel model = new ImageZoomModel();
		assertTrue(model.zoomTo(2.5, 1));
		assertFalse(model.isFit());
		assertEquals(2.5, model.getScale(1), DELTA);
		assertFalse(model.zoomTo(2.5, 1), "same scale");
	}

	@Test
	public void testZoomToIsLimited() {
		ImageZoomModel model = new ImageZoomModel();
		assertTrue(model.zoomTo(1000, 1));
		assertEquals(ImageZoomModel.MAX_SCALE, model.getScale(1), DELTA);
		assertFalse(model.zoomTo(1000, 1));

		assertTrue(model.zoomTo(0.0001, 1));
		assertEquals(ImageZoomModel.MIN_SCALE, model.getScale(1), DELTA);
		assertFalse(model.zoomTo(0.0001, 1));
	}

	@Test
	public void testZoomToBelowMinimumDoesNotEnlargeFittedImage() {
		ImageZoomModel model = new ImageZoomModel();
		double fitScale = 0.05; // below MIN_SCALE, a very large image
		assertFalse(model.zoomTo(0.01, fitScale));
		assertEquals(fitScale, model.getScale(fitScale), DELTA);
		assertTrue(model.isFit());
	}

	@Test
	public void testZoomToTheFitScaleReturnsToFitMode() {
		ImageZoomModel model = new ImageZoomModel();
		assertTrue(model.zoomTo(3, 0.5));
		assertFalse(model.isFit());
		assertTrue(model.zoomTo(0.5, 0.5));
		assertTrue(model.isFit());
		assertEquals(0.3, model.getScale(0.3), DELTA);
	}

	@Test
	public void testStepZoomOutToTheFitScaleReturnsToFitMode() {
		ImageZoomModel model = new ImageZoomModel();
		assertTrue(model.zoomIn(0.5)); // 0.6
		assertTrue(model.zoomOut(0.5)); // 0.5 is a level and the fit scale
		assertTrue(model.isFit());
		assertEquals(0.3, model.getScale(0.3), DELTA);
	}

	@Test
	public void testWheelZoomOutDoesNotEnlargeAfterTheFitScaleGrew() {
		ImageZoomModel model = new ImageZoomModel();
		model.zoomByWheel(1, 0.05); // 0.05375, below MIN_SCALE
		double shown = model.getScale(0.5);
		// the window was enlarged, the fit scale is now larger than the shown scale
		assertFalse(model.zoomByWheel(-1, 0.5));
		assertEquals(shown, model.getScale(0.5), DELTA);
	}

	@Test
	public void testStepZoomOutFromBelowMinimumDoesNotEnlarge() {
		ImageZoomModel model = new ImageZoomModel();
		assertFalse(model.zoomOut(0.05));
		assertEquals(0.05, model.getScale(0.05), DELTA);
		assertTrue(model.zoomIn(0.05));
		assertEquals(ImageZoomModel.MIN_SCALE, model.getScale(0.05), DELTA);
	}

	@Test
	public void testStepAfterWheelZoomGoesToTheNextLevel() {
		ImageZoomModel model = new ImageZoomModel();
		model.zoomByWheel(1, 1); // 1.075
		model.zoomIn(1);
		assertEquals(1.5, model.getScale(1), DELTA);

		model.zoomByWheel(-1, 1); // 1.3875
		model.zoomOut(1);
		assertEquals(1, model.getScale(1), DELTA);
	}

	@Test
	public void testFitScaleDoesNotEnlarge() {
		assertEquals(1, ImageZoomModel.fitScale(100, 50, 400, 400), DELTA);
		assertEquals(0.5, ImageZoomModel.fitScale(800, 400, 400, 400), DELTA);
		assertEquals(0.25, ImageZoomModel.fitScale(400, 1600, 400, 400), DELTA);
		assertEquals(1, ImageZoomModel.fitScale(0, 50, 400, 400), DELTA, "empty image");
		assertEquals(1, ImageZoomModel.fitScale(100, 50, 0, 400), DELTA, "empty viewport");
	}

	@Test
	public void testOriginCentersSmallImagesAndScrollsLargeOnes() {
		assertEquals(100, ImageZoomModel.origin(200, 400, 77));
		assertEquals(-77, ImageZoomModel.origin(1000, 400, 77));
		assertEquals(0, ImageZoomModel.origin(400, 400, 0));
	}

	@Test
	public void testPointUnderTheMouseStaysWhenZoomingIn() {
		// image 1000 wide, viewport 500, scrolled by 200, mouse at 100, scale 1
		double scale = 1;
		int origin = ImageZoomModel.origin(1000, 500, 200);
		double imageX = ImageZoomModel.imageCoordinate(100, origin, scale);
		assertEquals(300, imageX, DELTA);

		double newScale = 2;
		int scroll = ImageZoomModel.scrollOffsetFor(imageX, 100, newScale);
		assertEquals(500, scroll);
		int newOrigin = ImageZoomModel.origin(2000, 500, scroll);
		assertEquals(imageX, ImageZoomModel.imageCoordinate(100, newOrigin, newScale), DELTA);
	}

	@Test
	public void testScaledSizeKeepsPositiveDimensionsAtLeastOnePixel() {
		assertEquals(0, ImageZoomModel.scaledSize(0, 0.5));
		assertEquals(1, ImageZoomModel.scaledSize(1, 0.01));
		assertEquals(1, ImageZoomModel.scaledSize(1, 0.1));
		assertEquals(10, ImageZoomModel.scaledSize(100, 0.1));
	}
}
