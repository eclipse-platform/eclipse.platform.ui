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
	                "org.eclipse.jdt.ui", "icons/full/obj16/class_obj.gif")
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
}
