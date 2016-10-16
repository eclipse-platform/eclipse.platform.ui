/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc,)
 *******************************************************************************/
package org.eclipse.jface.tests.images;

import static org.junit.Assert.assertNotEquals;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;

import junit.framework.TestCase;

/**
 * @since 3.13
 */
public class DecorationOverlayIconTest extends TestCase {

	private static ImageDescriptor baseDescriptor1;
	private static Image baseImage1;
	private static ImageDescriptor baseDescriptor2;
	private static Image baseImage2;
	private static ImageDescriptor overlayDescriptor1;
	private static ImageDescriptor overlayDescriptor2;

	@Override
	@Before
	public void setUp() {
		baseImage1 = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
		assertNotNull(baseImage1);
		baseDescriptor1 = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
		assertNotNull(baseDescriptor1);
		baseImage2 = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		assertNotNull(baseImage2);
		baseDescriptor2 = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
		assertNotNull(baseDescriptor2);
		overlayDescriptor1 = PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_DEC_FIELD_ERROR);
		assertNotNull(overlayDescriptor1);
		overlayDescriptor2 = PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_DEC_FIELD_WARNING);
		assertNotNull(overlayDescriptor2);
	}

	@Override
	@After
	public void tearDown() {
		baseImage1.dispose();
		baseImage2.dispose();
	}

	public void testEqualsAndHashCode() {
		// same base and overlay
		DecorationOverlayIcon icon1 = new DecorationOverlayIcon(baseImage1,
				new ImageDescriptor[] { overlayDescriptor1, overlayDescriptor2 });
		DecorationOverlayIcon icon2 = new DecorationOverlayIcon(baseImage1,
				new ImageDescriptor[] { overlayDescriptor1, overlayDescriptor2 });
		assertTrue(icon1.equals(icon2));
		assertEquals(icon1.hashCode(), icon2.hashCode());
		//
		icon1 = new DecorationOverlayIcon(baseDescriptor1, overlayDescriptor1, IDecoration.TOP_LEFT);
		icon2 = new DecorationOverlayIcon(baseDescriptor1, overlayDescriptor1, IDecoration.TOP_LEFT);
		assertTrue(icon1.equals(icon2));
		assertEquals(icon1.hashCode(), icon2.hashCode());
		// same base, different overlays
		icon1 = new DecorationOverlayIcon(baseImage1, new ImageDescriptor[] { overlayDescriptor2 });
		icon2 = new DecorationOverlayIcon(baseImage1, new ImageDescriptor[] { overlayDescriptor1 });
		assertFalse(icon1.equals(icon2));
		assertNotEquals(icon1.hashCode(), icon2.hashCode());
		//
		icon1 = new DecorationOverlayIcon(baseDescriptor1, overlayDescriptor1, IDecoration.TOP_LEFT);
		icon2 = new DecorationOverlayIcon(baseDescriptor1, overlayDescriptor2, IDecoration.TOP_LEFT);
		assertFalse(icon1.equals(icon2));
		assertNotEquals(icon1.hashCode(), icon2.hashCode());
		// same overaly, different bases
		icon1 = new DecorationOverlayIcon(baseImage1, new ImageDescriptor[] { overlayDescriptor2 });
		icon2 = new DecorationOverlayIcon(baseImage2, new ImageDescriptor[] { overlayDescriptor2 });
		assertFalse(icon1.equals(icon2));
		assertNotEquals(icon1.hashCode(), icon2.hashCode());
		//
		icon1 = new DecorationOverlayIcon(baseDescriptor1, overlayDescriptor2, IDecoration.TOP_LEFT);
		icon2 = new DecorationOverlayIcon(baseDescriptor2, overlayDescriptor2, IDecoration.TOP_LEFT);
		assertFalse(icon1.equals(icon2));
		assertNotEquals(icon1.hashCode(), icon2.hashCode());
		// one descriptor, other image
		icon1 = new DecorationOverlayIcon(baseImage1, overlayDescriptor2, IDecoration.TOP_LEFT);
		icon2 = new DecorationOverlayIcon(baseDescriptor1, overlayDescriptor2, IDecoration.TOP_LEFT);
		assertFalse(icon1.equals(icon2));
		assertNotEquals(icon1.hashCode(), icon2.hashCode());
	}
}
