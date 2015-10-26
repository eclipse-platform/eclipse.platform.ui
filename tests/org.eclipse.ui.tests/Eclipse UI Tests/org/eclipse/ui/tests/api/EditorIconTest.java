/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.api;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.tests.harness.util.ImageTests;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Tests to ensure that various icon scenarios work.  These are tested on
 * editors but should be applicable for any client of
 * AbstractUIPlugin.imageDescriptorFromPlugin()
 *
 * @since 3.0
 */
public class EditorIconTest extends UITestCase {

    /**
     * @param testName
     */
    public EditorIconTest(String testName) {
        super(testName);
    }

    public void testDependantBundleIcon() {
        Image i1 = null;
        Image i2 = null;

        try {
	        i1 = fWorkbench.getEditorRegistry().getDefaultEditor(
	                "foo.icontest1").getImageDescriptor().createImage();
	        i2 = AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui",
	                "icons/full/obj16/font.png").createImage();
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

    public void testNonDependantBundleIcon() {
        Image i1 = null;
        Image i2 = null;
        try {
	        i1 = fWorkbench.getEditorRegistry().getDefaultEditor(
	                "foo.icontest2").getImageDescriptor().createImage();
	        i2 = AbstractUIPlugin.imageDescriptorFromPlugin(
	                "org.eclipse.jdt.ui", "icons/full/obj16/class_obj.png") // layer breaker!
	                .createImage();
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

    public void testBadIcon() {
        Image i1 = null;
        Image i2 = null;

        try {
	        i1 = fWorkbench.getEditorRegistry().getDefaultEditor(
	                "foo.icontest3").getImageDescriptor().createImage();
	        i2 = AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui",
	                "icons/full/obj16/file_obj.png").createImage();
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
	public void testBug395126() {
		ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.jface",
				"platform:/plugin/org.eclipse.jface/$nl$/icons/full/message_error.png");
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
	public void testBug395126_missing() {
		ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.jface",
				"platform:/plugin/org.eclipse.jface/$nl$/icons/does-not-exist.gif");
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
	public void testBug474072() throws Exception {
		URL url = FileLocator.find(new URL("platform:/plugin/org.eclipse.jface/$nl$/icons/full/message_error.png"));
		ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.jface",
				url.toString());
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
	public void testBug474072_missing() throws Exception {
		String url = FileLocator.find(new URL("platform:/plugin/org.eclipse.jface/$nl$/icons/full/message_error.png"))
				.toString();
		url += "does-not-exist";
		ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.jface", url);
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
