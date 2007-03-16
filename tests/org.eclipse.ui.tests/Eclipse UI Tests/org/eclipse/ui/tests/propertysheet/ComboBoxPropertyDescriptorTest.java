/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.propertysheet;

import junit.framework.TestCase;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.views.properties.ComboBoxLabelProvider;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;

/**
 * Test for new functionality pertaining to Bug 21013.
 * 
 * @since 3.0
 */
public class ComboBoxPropertyDescriptorTest extends TestCase {

    private String ID = "ID"; //$NON-NLS-1$

    private String NAME = "NAME"; //$NON-NLS-1$

    private String[] values = { "One", "Two", "Three" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private ComboBoxPropertyDescriptor descriptor;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        descriptor = new ComboBoxPropertyDescriptor(ID, NAME, values);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Tests the case where the user does not set an ILabelProvider.
     */
    public void testGetDefaultLabelProvider() {
        ILabelProvider provider = descriptor.getLabelProvider();
        assertEquals("Default label provider is of the wrong type", //$NON-NLS-1$ 
                ComboBoxLabelProvider.class, provider.getClass());

        for (int i = 0; i < values.length; i++) {
            String expected = values[i];
            assertEquals("Wrong label provided", //$NON-NLS-1$
                    expected, provider.getText(new Integer(i)));

        }

        testWrongLabel(provider, new Object());
        testWrongLabel(provider, null);
        testWrongLabel(provider, new Integer(-1));
        testWrongLabel(provider, new Integer(values.length));
    }

    /**
     * Tests that a bad element object (an Integer outside the accepted range, 
     * null or an other Object) returns the empty String.
     * @param provider the provider to test against.
     * @param element the element to test.
     */
    public void testWrongLabel(ILabelProvider provider, Object element) {
        assertEquals("Wrong label provided in bad case", //$NON-NLS-1$
                "", //$NON-NLS-1$
                provider.getText(element));
    }

    /**
     * Tests the case where the user sets their own ILabelProvider.
     */
    public void testSetGetLabelProvider() {
        ILabelProvider provider = new LabelProvider();
        descriptor.setLabelProvider(provider);
        ILabelProvider descProvider = descriptor.getLabelProvider();
        assertSame("Wrong label provider", //$NON-NLS-1$
                provider, descProvider);
    }

}
