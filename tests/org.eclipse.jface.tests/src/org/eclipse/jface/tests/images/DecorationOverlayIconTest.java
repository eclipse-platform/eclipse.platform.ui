/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickael Istria (Red Hat Inc,)
 *******************************************************************************/
package org.eclipse.jface.tests.images;

import static org.junit.Assert.assertNotEquals;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.junit.Before;

import junit.framework.TestCase;

/**
 * @since 3.13
 */
public class DecorationOverlayIconTest extends TestCase {

	private ImageDescriptor baseDescriptor1;
	private Image baseImage1;
	private ImageDescriptor baseDescriptor2;
	private Image baseImage2;
	private ImageDescriptor overlayDescriptor1;
	private ImageDescriptor overlayDescriptor2;

	@Override
	@Before
	public void setUp() {
		ImageRegistry imageRegistry = JFaceResources.getImageRegistry();
		baseImage1 = imageRegistry.get(Dialog.DLG_IMG_HELP);
		assertNotNull(baseImage1);
		baseDescriptor1 = imageRegistry.getDescriptor(Dialog.DLG_IMG_HELP);
		assertNotNull(baseDescriptor1);
		baseImage2 = imageRegistry.get(Dialog.DLG_IMG_MESSAGE_ERROR);
		assertNotNull(baseImage2);
		baseDescriptor2 = imageRegistry.getDescriptor(Dialog.DLG_IMG_MESSAGE_ERROR);
		assertNotNull(baseDescriptor2);
		overlayDescriptor1 = imageRegistry.getDescriptor(Dialog.DLG_IMG_MESSAGE_INFO);
		assertNotNull(overlayDescriptor1);
		overlayDescriptor2 = imageRegistry.getDescriptor(Dialog.DLG_IMG_MESSAGE_WARNING);
		assertNotNull(overlayDescriptor2);
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
