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
package org.eclipse.jface.tests.viewers;

/**
 * Test with missing images.
 */
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class CheckboxTreeViewerMissingTest extends CheckboxTreeViewerTest {
    private static Image testImage;

    public static Image getMissingImage() {
        if (testImage == null)
            testImage = ImageDescriptor.createFromFile(TestLabelProvider.class,
                    "images/missing.gif").createImage();
        return testImage;
    }

    public static class CheckboxMissingTableTestLabelProvider extends
            CheckboxTreeViewerTest.CheckboxTableTestLabelProvider implements
            ITableLabelProvider {
        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.tests.viewers.StructuredViewerTest.TestLabelProvider#getImage(java.lang.Object)
         */
        public Image getImage(Object element) {
            return getMissingImage();
        }

    }

    public CheckboxTreeViewerMissingTest(String name) {
        super(name);
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.tests.viewers.StructuredViewerTest#getTestLabelProvider()
     */
    public IBaseLabelProvider getTestLabelProvider() {
        return new CheckboxMissingTableTestLabelProvider();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.tests.viewers.StructuredViewerTest#tearDown()
     */
    public void tearDown() {
        super.tearDown();
        if (testImage != null) {
            testImage.dispose();
            testImage = null;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.tests.viewers.StructuredViewerTest#testLabelProvider()
     */
    public void testLabelProvider() {
        super.testLabelProvider();
    }

}
