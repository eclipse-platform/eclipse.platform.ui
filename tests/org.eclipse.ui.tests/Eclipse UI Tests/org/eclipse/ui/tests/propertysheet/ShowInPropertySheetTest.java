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

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.PropertyShowInContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.4
 */
@RunWith(JUnit4.class)
public class ShowInPropertySheetTest extends AbstractPropertySheetTest {

	public ShowInPropertySheetTest() {
		super(ShowInPropertySheetTest.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();

		propertySheet = (PropertySheet) activePage
				.showView(IPageLayout.ID_PROP_SHEET);
	}

	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();
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
