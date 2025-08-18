/*******************************************************************************
 * Copyright (c) 2008, 2009 Versant Corp. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Alexander Kuppe (Versant Corp.) - https://bugs.eclipse.org/248103
 ******************************************************************************/

package org.eclipse.ui.tests.propertysheet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.PropertyShowInContext;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.4
 */
public class ShowInPropertySheetTest extends AbstractPropertySheetTest {

	private PropertySheet propertySheet;

	@Before
	public final void setUp() throws Exception {
		propertySheet = (PropertySheet) activePage.showView(IPageLayout.ID_PROP_SHEET);
	}

	/**
	 * Tries to get the IShowInTarget adapter
	 */
	@Test
	public void testGetIShowInTargetAdapter() {
		Object adapter = propertySheet.getAdapter(IShowInTarget.class);
		assertNotNull("No IShowInTarget adapter returned", adapter);
		assertTrue(adapter instanceof IShowInTarget);
	}

	/**
	 * Tests ShowIn PropertySheet with various inputs
	 */
	@Test
	public void testShowInPropertySheet() {
		IShowInTarget showInTarget = propertySheet
				.getAdapter(IShowInTarget.class);
		ShowInContext context = new PropertyShowInContext(activePage
				.getActivePart(), StructuredSelection.EMPTY);
		assertTrue(showInTarget.show(context));
	}

	/**
	 * Tests ShowIn PropertySheet with various inputs
	 */
	@Test
	public void testShowInPropertySheetWithNull() {
		IShowInTarget showInTarget = propertySheet
				.getAdapter(IShowInTarget.class);
		assertFalse(showInTarget.show(null));
	}

	/**
	 * Tests ShowIn PropertySheet with various inputs
	 */
	@Test
	public void testShowInPropertySheetWithNullContext() {
		IShowInTarget showInTarget = propertySheet
				.getAdapter(IShowInTarget.class);
		assertFalse(showInTarget.show(new ShowInContext(null, null)));
	}

	/**
	 * Tests ShowIn PropertySheet with various inputs
	 */
	@Test
	public void testShowInPropertySheetWithNullPart() {
		IShowInTarget showInTarget = propertySheet
				.getAdapter(IShowInTarget.class);
		assertFalse(showInTarget.show(new ShowInContext(new Object(),
				StructuredSelection.EMPTY)));
	}
}
