/*******************************************************************************
 * Copyright (c) 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.forms.util;

import java.lang.reflect.Field;

import junit.framework.TestCase;

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

public class ImageHyperlinkTest extends TestCase {

	private Display display;
	private Shell shell;
	private GC gc;
	private TestImageHyperlink imageHyperlink;

	@Override
	protected void setUp() throws Exception {
		display = PlatformUI.getWorkbench().getDisplay();
		shell = new Shell(display);
		shell.setSize(400, 300);
		shell.setLayout(new FillLayout());
		shell.open();
		Composite composite = new Composite(shell, SWT.V_SCROLL);
		imageHyperlink = new TestImageHyperlink(composite, SWT.NULL);
		gc = new GC(display);
	}

	@Override
	protected void tearDown() throws Exception {
		shell.dispose();
		gc.dispose();
	}

	public void testNoImageOnCreation() throws Exception {
		assertNull(imageHyperlink.getImage());
		assertNull(getDisabledImage(imageHyperlink));
	}

	public void testSetImageDoesNotCreateDisabledImage() throws Exception {
		Image image = createGradient();

		imageHyperlink.setImage(image);

		assertNotNull(imageHyperlink.getImage());
		assertNull(getDisabledImage(imageHyperlink));
	}

	public void testCreateDisabledImageOnPaint() throws Exception {
		Image image = createGradient();
		imageHyperlink.setImage(image);

		imageHyperlink.paintHyperlink(gc);

		assertNotNull(imageHyperlink.getImage());
		assertNull(getDisabledImage(imageHyperlink));
	}

	public void testCreateDisabledImageOnPaintWhenDisabled() throws Exception {
		Image image = createGradient();
		imageHyperlink.setImage(image);

		imageHyperlink.setEnabled(false);
		imageHyperlink.paintHyperlink(gc);

		assertNotNull(imageHyperlink.getImage());
		assertNotNull(getDisabledImage(imageHyperlink));
	}

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
