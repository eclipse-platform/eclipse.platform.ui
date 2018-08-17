/*******************************************************************************
 * Copyright (c) 2015, 2017 Tasktop Technologies and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Ralf M Petter<ralf.petter@gmail.com> - Bug 510241
 *******************************************************************************/
package org.eclipse.ui.tests.forms.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.internal.forms.widgets.FormImages;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ImageHyperlinkTest {

	private Display display;
	private Shell shell;
	private GC gc;
	private TestImageHyperlink imageHyperlink;

	@Before
	public void setUp() throws Exception {
		display = PlatformUI.getWorkbench().getDisplay();
		shell = new Shell(display);
		shell.setSize(400, 300);
		shell.setLayout(new FillLayout());
		shell.open();
		Composite composite = new Composite(shell, SWT.V_SCROLL);
		imageHyperlink = new TestImageHyperlink(composite, SWT.NULL);
		gc = new GC(display);
	}

	@After
	public void tearDown() throws Exception {
		shell.dispose();
		gc.dispose();
	}

	@Test
	public void testNoImageOnCreation() throws Exception {
		assertNull(imageHyperlink.getImage());
		assertNull(getDisabledImage(imageHyperlink));
	}

	@Test
	public void testSetImageDoesNotCreateDisabledImage() throws Exception {
		Image image = createGradient();

		imageHyperlink.setImage(image);

		assertNotNull(imageHyperlink.getImage());
		assertNull(getDisabledImage(imageHyperlink));
	}

	@Test
	public void testCreateDisabledImageOnPaint() throws Exception {
		Image image = createGradient();
		imageHyperlink.setImage(image);

		imageHyperlink.paintHyperlink(gc);

		assertNotNull(imageHyperlink.getImage());
		assertNull(getDisabledImage(imageHyperlink));
	}

	@Test
	public void testCreateDisabledImageOnPaintWhenDisabled() throws Exception {
		Image image = createGradient();
		imageHyperlink.setImage(image);

		imageHyperlink.setEnabled(false);
		imageHyperlink.paintHyperlink(gc);

		assertNotNull(imageHyperlink.getImage());
		assertNotNull(getDisabledImage(imageHyperlink));
	}

	@Test
	public void testSetImageDisposesPreviousDisabledImage() throws Exception {
		Image prevImage = createGradient();
		imageHyperlink.setImage(prevImage);
		imageHyperlink.setEnabled(false);
		imageHyperlink.paintHyperlink(gc);
		Image prevDisabledImage = getDisabledImage(imageHyperlink);
		assertFalse(prevDisabledImage.isDisposed());
		Image image = createGradient();

		imageHyperlink.setImage(image);

		assertTrue(prevDisabledImage.isDisposed());
	}

	@Test
	public void testPaintHyperlinkDoesNotLeakDisabledImage() throws Exception {
		Image prevImage = createGradient();
		imageHyperlink.setImage(prevImage);
		imageHyperlink.setEnabled(false);
		imageHyperlink.paintHyperlink(gc);

		Image prevDisabledImage = getDisabledImage(imageHyperlink);
		imageHyperlink.paintHyperlink(gc);

		assertSame(prevDisabledImage, getDisabledImage(imageHyperlink));
	}
	@Test
	public void testSetImageNullClearsDisabledImage() throws Exception {
		Image image = createGradient();
		imageHyperlink.setImage(image);
		imageHyperlink.setEnabled(false);
		imageHyperlink.paintHyperlink(gc);
		assertNotNull(getDisabledImage(imageHyperlink));

		imageHyperlink.setImage(null);

		assertNull(getDisabledImage(imageHyperlink));
	}

	private Image createGradient() {
		return FormImages.getInstance().getGradient(new Color(display, 0, 0, 0), new Color(display, 100, 100, 100), 1,
				1, 1, display);
	}

	private static Image getDisabledImage(ImageHyperlink link) throws Exception {
		Field f = ImageHyperlink.class.getDeclaredField("disabledImage");
		f.setAccessible(true);
		return (Image) f.get(link);
	}

	private class TestImageHyperlink extends ImageHyperlink {

		public TestImageHyperlink(Composite parent, int style) {
			super(parent, style);
		}

		@Override
		public void paintHyperlink(GC gc) {
			super.paintHyperlink(gc);
		}
	}
}
