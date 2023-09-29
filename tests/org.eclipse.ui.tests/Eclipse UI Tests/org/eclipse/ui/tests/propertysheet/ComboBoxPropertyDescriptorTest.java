/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.propertysheet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.views.properties.ComboBoxLabelProvider;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for new functionality pertaining to Bug 21013.
 *
 * @since 3.0
 */
public class ComboBoxPropertyDescriptorTest {

	private final String ID = "ID"; //$NON-NLS-1$

	private final String NAME = "NAME"; //$NON-NLS-1$

	private final String[] values = { "One", "Two", "Three" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	private ComboBoxPropertyDescriptor descriptor;

	@Before
	public void setUp() throws Exception {
		descriptor = new ComboBoxPropertyDescriptor(ID, NAME, values);
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Tests the case where the user does not set an ILabelProvider.
	 */
	@Test
	public void testGetDefaultLabelProvider() {
		ILabelProvider provider = descriptor.getLabelProvider();
		assertEquals("Default label provider is of the wrong type", //$NON-NLS-1$
				ComboBoxLabelProvider.class, provider.getClass());

		for (int i = 0; i < values.length; i++) {
			String expected = values[i];
			assertEquals("Wrong label provided", //$NON-NLS-1$
					expected, provider.getText(Integer.valueOf(i)));

		}

		testWrongLabel(provider, new Object());
		testWrongLabel(provider, null);
		testWrongLabel(provider, Integer.valueOf(-1));
		testWrongLabel(provider, Integer.valueOf(values.length));
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
	@Test
	public void testSetGetLabelProvider() {
		ILabelProvider provider = new LabelProvider();
		descriptor.setLabelProvider(provider);
		ILabelProvider descProvider = descriptor.getLabelProvider();
		assertSame("Wrong label provider", //$NON-NLS-1$
				provider, descProvider);
	}

}
