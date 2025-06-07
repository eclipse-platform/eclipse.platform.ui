/*******************************************************************************
 * Copyright (c) 2007, 2023 IBM Corporation and others.
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
 *     Ralf M Petter<ralf.petter@gmail.com> - Bug 510241, 510826, 510830
 *******************************************************************************/

package org.eclipse.ui.tests.forms.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.internal.forms.widgets.FormImages;
import org.junit.Test;

public class FormImagesTest {
	private static FormImages instance;
	@Test
	public void testSingleton() throws Exception {
		Display display = Display.getCurrent();
		FormImages instance = FormImages.getInstance();
		// ensure the singleton is returning the same instance
		assertEquals("getInstance() returned a different FormImages instance", instance, FormImages.getInstance());
		Image gradient = instance.getGradient(new Color(display, 1, 1, 1), new Color(display, 7, 7, 7), 21, 21, 0, display);
		instance.markFinished(gradient, display);
		// ensure the singleton is returning the same instance after creating and disposing one gradient
		assertEquals("getInstance() returned a different FormImages instance after creation and disposal of one image",
				instance, FormImages.getInstance());
	}

	@Test
	public void testDisposeOne() throws Exception {
		Display display = Display.getCurrent();
		Image gradient = getFormImagesInstance().getGradient(new Color(display, 255, 255, 255),
				new Color(display, 0, 0, 0), 21, 21, 0, display);
		getFormImagesInstance().markFinished(gradient, display);
		// ensure that getting a single gradient and marking it as finished disposed it
		assertTrue("markFinished(...) did not dispose an image after a single getGradient()", gradient.isDisposed());
		assertNull("descriptors map", getDescriptors(getFormImagesInstance()));
	}

	@Test
	public void testMultipleSimpleInstances() throws Exception {
		Display display = Display.getCurrent();
		Image gradient = getFormImagesInstance().getGradient(new Color(display, 200, 200, 200),
				new Color(display, 0, 0, 0), 30, 16, 3, display);
		int count;
		// ensure that the same image is returned for many calls with the same parameter
		for (count = 1; count < 20; count ++)
			assertEquals("getGradient(...) returned a different image for the same params on iteration " + count,
					gradient, getFormImagesInstance().getGradient(new Color(display, 200, 200, 200),
							new Color(display, 0, 0, 0), 30, 16, 3, display));
		for ( ;count > 0; count--) {
			getFormImagesInstance().markFinished(gradient, display);
			if (count != 1)
				// ensure that the gradient is not disposed early
				assertFalse("markFinished(...) disposed a shared image early on iteration " + count,
						gradient.isDisposed());
			else
				// ensure that the gradient is disposed on the last markFinished
				assertTrue("markFinished(...) did not dispose a shared image on the last call", gradient.isDisposed());
		}
		assertNull("descriptors map", getDescriptors(getFormImagesInstance()));
	}

	@Test
	public void testMultipleSectionGradientInstances() throws Exception {
		Display display = Display.getCurrent();
		Image gradient = getFormImagesInstance().getSectionGradientImage(new Color(display, 200, 200, 200),
				new Color(display, 0, 0, 0), 30, 16, 3, display);
		int count;
		// ensure that the same image is returned for many calls with the same
		// parameter
		for (count = 1; count < 20; count++)
			assertEquals(
					"getSectionGradientImage(...) returned a different image for the same params on iteration " + count,
					gradient, getFormImagesInstance().getSectionGradientImage(new Color(display, 200, 200, 200),
							new Color(display, 0, 0, 0), 30, 16, 3, display));
		for (; count > 0; count--) {
			getFormImagesInstance().markFinished(gradient, display);
			if (count != 1)
				// ensure that the gradient is not disposed early
				assertFalse("markFinished(...) disposed a shared image early on iteration " + count,
						gradient.isDisposed());
			else
				// ensure that the gradient is disposed on the last markFinished
				assertTrue("markFinished(...) did not dispose a shared image on the last call",
						gradient.isDisposed());
		}
		assertNull("descriptors map", getDescriptors(getFormImagesInstance()));
	}

	@Test
	public void testMultipleComplexInstances() throws Exception {
		Display display = Display.getCurrent();
		Image gradient = getFormImagesInstance().getGradient(
				new Color[] { new Color(display, 200, 200, 200), new Color(display, 0, 0, 0) },
				new int[] {100}, 31, true, null, display);
		int count;
		// ensure that the same image is returned for many calls with the same parameter
		for (count = 1; count < 20; count ++)
			assertEquals("getGradient(...) returned a different image for the same params on iteration " + count,
					gradient,
					getFormImagesInstance().getGradient(
							new Color[] { new Color(display, 200, 200, 200), new Color(display, 0, 0, 0) },
							new int[] {100}, 31, true, null, display));
		for ( ;count > 0; count--) {
			getFormImagesInstance().markFinished(gradient, display);
			if (count != 1)
				// ensure that the gradient is not disposed early
				assertFalse("markFinished(...) disposed a shared image early on iteration " + count,
						gradient.isDisposed());
			else
				// ensure that the gradient is disposed on the last markFinished
				assertTrue("markFinished(...) did not dispose a shared image on the last call", gradient.isDisposed());
		}
		assertNull("descriptors map", getDescriptors(getFormImagesInstance()));
	}

	@Test
	public void testMultipleUniqueInstances() throws Exception {
		Display display = Display.getCurrent();
		Image[] images = new Image[24];
		images[0] = getFormImagesInstance().getGradient(new Color(display, 1, 0, 0), new Color(display, 100, 100, 100),
				25, 23, 1, display);
		images[1] = getFormImagesInstance().getGradient(new Color(display, 0, 1, 0), new Color(display, 100, 100, 100),
				25, 23, 1, display);
		images[2] = getFormImagesInstance().getGradient(new Color(display, 0, 0, 1), new Color(display, 100, 100, 100),
				25, 23, 1, display);
		images[3] = getFormImagesInstance().getGradient(new Color(display, 0, 0, 0), new Color(display, 101, 100, 100),
				25, 23, 1, display);
		images[4] = getFormImagesInstance().getGradient(new Color(display, 0, 0, 0), new Color(display, 100, 101, 100),
				25, 23, 1, display);
		images[5] = getFormImagesInstance().getGradient(new Color(display, 0, 0, 0), new Color(display, 100, 100, 101),
				25, 23, 1, display);
		images[6] = getFormImagesInstance().getGradient(new Color(display, 0, 0, 0), new Color(display, 100, 100, 100),
				20, 23, 1, display);
		images[7] = getFormImagesInstance().getGradient(new Color(display, 0, 0, 0), new Color(display, 100, 100, 100),
				25, 10, 1, display);
		images[8] = getFormImagesInstance().getGradient(new Color(display, 0, 0, 0), new Color(display, 100, 100, 100),
				25, 23, 2, display);
		images[9] = getFormImagesInstance().getGradient(new Color(display, 1, 1, 1), new Color(display, 101, 101, 101),
				20, 10, 2, display);
		images[10] = getFormImagesInstance().getGradient(new Color[] { new Color(display, 0, 0, 0) }, new int[] {}, 31,
				true, null, display);
		images[11] = getFormImagesInstance().getGradient(
				new Color[] { new Color(display, 0, 0, 0), new Color(display, 1, 1, 1) },
				new int[] {80}, 31, true, new Color(display,255,255,255), display);
		images[12] = getFormImagesInstance().getGradient(
				new Color[] { new Color(display, 0, 0, 0), new Color(display, 1, 1, 1) },
				new int[] {80}, 31, true, new Color(display,0,0,0), display);
		images[13] = getFormImagesInstance().getGradient(
				new Color[] { new Color(display, 0, 0, 0), new Color(display, 100, 100, 100) },
				new int[] {100}, 31, true, null, display);
		images[14] = getFormImagesInstance().getGradient(
				new Color[] { new Color(display, 1, 0, 0), new Color(display, 100, 100, 100) },
				new int[] {100}, 31, true, null, display);
		images[15] = getFormImagesInstance().getGradient(
				new Color[] { new Color(display, 0, 1, 0), new Color(display, 100, 100, 100) },
				new int[] {100}, 31, true, null, display);
		images[16] = getFormImagesInstance().getGradient(
				new Color[] { new Color(display, 0, 0, 1), new Color(display, 100, 100, 100) },
				new int[] {100}, 31, true, null, display);
		images[17] = getFormImagesInstance().getGradient(
				new Color[] { new Color(display, 0, 0, 0), new Color(display, 101, 100, 100) },
				new int[] {100}, 31, true, null, display);
		images[18] = getFormImagesInstance().getGradient(
				new Color[] { new Color(display, 0, 0, 0), new Color(display, 100, 101, 100) },
				new int[] {100}, 31, true, null, display);
		images[19] = getFormImagesInstance().getGradient(
				new Color[] { new Color(display, 0, 0, 0), new Color(display, 100, 100, 101) },
				new int[] {100}, 31, true, null, display);
		images[20] = getFormImagesInstance().getGradient(
				new Color[] { new Color(display, 0, 0, 0), new Color(display, 100, 100, 100) },
				new int[] {100}, 20, true, null, display);
		images[21] = getFormImagesInstance().getGradient(
				new Color[] { new Color(display, 0, 0, 0), new Color(display, 100, 100, 100) },
				new int[] {100}, 31, false, null, display);
		images[22] = getFormImagesInstance().getGradient(
				new Color[] { new Color(display, 0, 0, 0), new Color(display, 100, 100, 100) },
				new int[] {50}, 31, true, new Color(display,1,1,1), display);
		images[23] = getFormImagesInstance().getGradient(
				new Color[] { new Color(display, 1, 1, 1), new Color(display, 101, 101, 101) },
				new int[] {50}, 20, false, new Color(display,1,1,1), display);
		// ensure none of the images are the same
		for (int i = 0; i < images.length - 1; i++) {
			for (int j = i+1; j < images.length; j++) {
				assertNotSame(
						"getGradient(...) returned the same image for different parameters: i = " + i + "; j = " + j,
						images[i], images[j]);
			}
		}
		// ensure all of the images are disposed with one call to markFinished
		for (int i = 0; i < images.length; i++) {
			getFormImagesInstance().markFinished(images[i], display);
			assertTrue("markFinished(...) did not dispose an image that was only requested once: i = " + i,
					images[i].isDisposed());
		}
		assertNull("descriptors map", getDescriptors(getFormImagesInstance()));
	}

	@Test
	public void testComplexEquality() throws Exception {
		Display display = Display.getCurrent();
		Image image1 = getFormImagesInstance().getGradient(
				new Color[] { new Color(display, 0, 0, 0), new Color(display, 255, 255, 255) },
				new int[] {100}, 20, true, new Color(display,100,100,100), display);
		Image image2 = getFormImagesInstance().getGradient(
				new Color[] { new Color(display, 0, 0, 0), new Color(display, 255, 255, 255) },
				new int[] {100}, 20, true, new Color(display,0,0,0), display);
		assertEquals(
				"different images were created with only the background color differing when that difference is irrelevant",
				image1, image2);
		getFormImagesInstance().markFinished(image1, display);
		getFormImagesInstance().markFinished(image2, display);
		image1 = getFormImagesInstance().getGradient(
				new Color[] { new Color(display, 0, 0, 0), new Color(display, 255, 255, 255) },
				new int[] {80}, 20, true, new Color(display,100,100,100), display);
		image2 = getFormImagesInstance().getGradient(
				new Color[] { new Color(display, 0, 0, 0), new Color(display, 255, 255, 255) },
				new int[] {80}, 20, true, new Color(display,0,0,0), display);
		assertNotSame("the same image was used when different background colors were specified", image1, image2);
		getFormImagesInstance().markFinished(image1, display);
		getFormImagesInstance().markFinished(image2, display);
		assertNull("descriptors map", getDescriptors(getFormImagesInstance()));
	}

	@Test
	public void testToolkitColors() throws Exception {
		String blueKey = "blue";
		String redKey = "red";

		Display display = Display.getCurrent();
		FormToolkit kit1 = new FormToolkit(display);
		kit1.getColors().createColor(blueKey, new RGB(0,0,255));
		kit1.getColors().createColor(redKey, new RGB(255,0,0));
		FormToolkit kit2 = new FormToolkit(display);
		kit2.getColors().createColor(blueKey, new RGB(0,0,255));
		kit2.getColors().createColor(redKey, new RGB(255,0,0));
		Image image1 = getFormImagesInstance().getGradient(kit1.getColors().getColor(blueKey),
				kit1.getColors().getColor(redKey), 21, 21, 0, display);
		Image image2 = getFormImagesInstance().getGradient(kit2.getColors().getColor(blueKey),
				kit2.getColors().getColor(redKey), 21, 21, 0, display);
		assertEquals("different images were created for the same RGBs with different Color instances", image1, image2);
		Image image3 = getFormImagesInstance().getGradient(new Color(display, 0, 0, 255), new Color(display, 255, 0, 0),
				21, 21, 0, display);
		assertEquals("different images were created for the same RGBs with different Color instances", image1, image3);
		kit1.dispose();
		assertFalse("image was disposed after toolkits were disposed", image1.isDisposed());
		kit2.dispose();
		assertFalse("image was disposed after toolkits were disposed", image2.isDisposed());
		getFormImagesInstance().markFinished(image1, display);
		assertFalse("image was disposed early", image1.isDisposed());
		getFormImagesInstance().markFinished(image2, display);
		assertFalse("image was disposed early", image2.isDisposed());
		getFormImagesInstance().markFinished(image3, display);
		assertTrue("image was not disposed", image3.isDisposed());
		assertNull("descriptors map", getDescriptors(getFormImagesInstance()));
	}

	@Test
	public void testDisposeUnknown() throws Exception {
		Display display = Display.getCurrent();
		Image image = new Image(display, (gc, width, height) -> {}, 10, 10);
		getFormImagesInstance().markFinished(image, display);
		assertTrue("markFinished(...) did not dispose of an unknown image", image.isDisposed());
		assertNull("descriptors map", getDescriptors(getFormImagesInstance()));
	}

	private static HashMap<?, ?> getDescriptors(FormImages formImages) throws Exception {
		Field field = formImages.getClass().getDeclaredField("descriptors");
		field.setAccessible(true);
		return (HashMap<?, ?>) field.get(formImages);
	}

	private static FormImages getFormImagesInstance() throws Exception {
		if (instance == null) {
			Constructor<FormImages> constructor = FormImages.class.getDeclaredConstructor();
			constructor.setAccessible(true);
			instance = constructor.newInstance();
		}
		return instance;
	}
}
