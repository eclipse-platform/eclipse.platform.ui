/*******************************************************************************
 * Copyright (c) 2016, 2020 Red Hat Inc. and others
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.13
 */
public class DecorationOverlayIconTest {

	private ImageDescriptor baseDescriptor1;
	private Image baseImage1;
	private ImageDescriptor baseDescriptor2;
	private Image baseImage2;
	private ImageDescriptor overlayDescriptor1;
	private ImageDescriptor overlayDescriptor2;

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

	@Test
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

	private static class SimpleImageDescriptor extends ImageDescriptor {
		private final String pretendFileName;

		public SimpleImageDescriptor(String pretendFileName) {
			this.pretendFileName = pretendFileName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((pretendFileName == null) ? 0 : pretendFileName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			SimpleImageDescriptor other = (SimpleImageDescriptor) obj;
			if (pretendFileName == null) {
				if (other.pretendFileName != null) {
					return false;
				}
			} else if (!pretendFileName.equals(other.pretendFileName)) {
				return false;
			}
			return true;
		}

	}

	@Test
	public void testEqualsAndHashCode2() {
		// what is true about the underlying image descriptors should be true about
		// the DecorationOverlayIcon too.

		// first verify image descriptor's equals/hashcode
		SimpleImageDescriptor equalButDifferent1 = new SimpleImageDescriptor("pretend_file_name");
		SimpleImageDescriptor equalButDifferent2 = new SimpleImageDescriptor("pretend_file_name");
		assertTrue(equalButDifferent1.equals(equalButDifferent2));
		assertEquals(equalButDifferent1.hashCode(), equalButDifferent2.hashCode());

		// second verify overlay's equals/hashcode still maintain contract when wrapping
		// the above descriptors
		DecorationOverlayIcon equalButDifferentIcon1 = new DecorationOverlayIcon(equalButDifferent1, overlayDescriptor1,
				IDecoration.TOP_LEFT);
		DecorationOverlayIcon equalButDifferentIcon2 = new DecorationOverlayIcon(equalButDifferent2, overlayDescriptor1,
				IDecoration.TOP_LEFT);
		assertTrue(equalButDifferentIcon1.equals(equalButDifferentIcon2));
		assertEquals(equalButDifferentIcon1.hashCode(), equalButDifferentIcon2.hashCode());
	}
}
