/*******************************************************************************
 * Copyright (c) 2004, 2024 IBM Corporation and others.
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

package org.eclipse.ui.tests.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.ImageTests;
import org.junit.Test;

/**
 * Tests to ensure that various icon scenarios work. These are tested on editors
 * but should be applicable for any client of
 * {@link ResourceLocator#imageDescriptorFromBundle(String, String)}
 *
 * @since 3.0
 */
public class EditorIconTest {

	@Test
	public void testDependantBundleIcon() {
		Image i1 = null;
		Image i2 = null;

		try {
			i1 = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(
					"foo.icontest1").getImageDescriptor().createImage();
			i2 = ResourceLocator.imageDescriptorFromBundle("org.eclipse.ui", "icons/full/obj16/font.png")
					.orElseThrow(AssertionError::new).createImage();
			ImageTests.assertEquals(i1, i2);
		}
		finally {
			if (i1 != null) {
				i1.dispose();
			}
			if (i2 != null) {
				i2.dispose();
			}
		}
	}

	@Test
	public void testNonDependantBundleIcon() {
		Image i1 = null;
		Image i2 = null;
		try {
			i1 = PlatformUI.getWorkbench().getEditorRegistry()
					.getDefaultEditor(
					"foo.icontest2").getImageDescriptor().createImage();
			i2 = ResourceLocator.imageDescriptorFromBundle(
					"org.eclipse.debug.ui", "icons/full/obj16/file_obj.png") // layer breaker!
					.orElseThrow(AssertionError::new).createImage();
			ImageTests.assertEquals(i1, i2);
		}
		finally {
			if (i1 != null) {
				i1.dispose();
			}
			if (i2 != null) {
				i2.dispose();
			}
		}
	}

	@Test
	public void testBadIcon() {
		Image i1 = null;
		Image i2 = null;

		try {
			i1 = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(
					"foo.icontest3").getImageDescriptor().createImage();
			i2 = ResourceLocator.imageDescriptorFromBundle("org.eclipse.ui", "icons/full/obj16/file_obj.png")
					.orElseThrow(AssertionError::new).createImage();
			ImageTests.assertEquals(i1, i2);
		}
		finally {
			if (i1 != null) {
				i1.dispose();
			}
			if (i2 != null) {
				i2.dispose();
			}
		}
	}

	/**
	 * Tests undocumented support for platform:/plugin/... URLs.
	 */
	@Test
	public void testBug395126() {
		ImageDescriptor imageDescriptor = ResourceLocator.imageDescriptorFromBundle("org.eclipse.jface",
				"platform:/plugin/org.eclipse.jface/$nl$/icons/full/message_error.png")
				.orElseThrow(AssertionError::new);
		Image image = null;
		try {
			image = imageDescriptor.createImage(false);
			assertNotNull(image);
		} finally {
			if (image != null) {
				image.dispose();
			}
		}
	}

	/**
	 * Tests undocumented support for platform:/plugin/... URLs.
	 */
	@Test
	public void testBug395126_missing() {
		ImageDescriptor imageDescriptor = ResourceLocator.imageDescriptorFromBundle("org.eclipse.jface",
				"platform:/plugin/org.eclipse.jface/$nl$/icons/does-not-exist.gif").orElse(null);
		Image image = null;
		try {
			image = imageDescriptor.createImage(false);
			assertNull(image);
		} finally {
			if (image != null) {
				image.dispose();
			}
		}
	}

	/**
	 * Tests undocumented support for arbitrary URLs.
	 */
	@Test
	public void testBug474072() throws Exception {
		URL url = FileLocator.find(new URL("platform:/plugin/org.eclipse.jface/$nl$/icons/full/message_error.png"));
		ImageDescriptor imageDescriptor = ResourceLocator.imageDescriptorFromBundle("org.eclipse.jface", url.toString())
				.orElseThrow(AssertionError::new);
		Image image = null;
		try {
			image = imageDescriptor.createImage(false);
			assertNotNull(image);
		} finally {
			if (image != null) {
				image.dispose();
			}
		}
	}

	/**
	 * Tests undocumented support for arbitrary URLs.
	 */
	@Test
	public void testBug474072_missing() throws Exception {
		String url = FileLocator.find(new URL("platform:/plugin/org.eclipse.jface/$nl$/icons/full/message_error.png"))
				.toString();
		url += "does-not-exist";
		ImageDescriptor imageDescriptor = ResourceLocator.imageDescriptorFromBundle("org.eclipse.jface", url)
				.orElse(null);
		Image image = null;
		try {
			image = imageDescriptor.createImage(false);
			assertNull(image);
		} finally {
			if (image != null) {
				image.dispose();
			}
		}
	}
}
