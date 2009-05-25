/*******************************************************************************
 * Copyright (c) 2008, 2009 Versant Corp. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (Versant Corp.) - https://bugs.eclipse.org/248103
 ******************************************************************************/

package org.eclipse.ui.tests.propertysheet;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.views.properties.PropertyShowInContext;

/**
 * @since 3.5
 * 
 */
public class PropertyShowInContextTest extends AbstractPropertySheetTest {

	public PropertyShowInContextTest(String testName) {
		super(testName);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.views.properties.PropertyShowInContext#hashCode()}.
	 */
	public final void testHashCode() {
		ShowInContext psc1 = new PropertyShowInContext(null, (ISelection) null);
		ShowInContext psc2 = new PropertyShowInContext(null, (ISelection) null);
		assertEquals(psc1.hashCode(), psc2.hashCode());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.views.properties.PropertyShowInContext#hashCode()}.
	 */
	public final void testHashCode2() {
		ShowInContext psc1 = new PropertyShowInContext(null,
				StructuredSelection.EMPTY);
		ShowInContext psc2 = new PropertyShowInContext(null,
				StructuredSelection.EMPTY);
		assertEquals(psc1.hashCode(), psc2.hashCode());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.views.properties.PropertyShowInContext#hashCode()}.
	 * 
	 * @throws PartInitException
	 */
	public final void testHashCode3() throws PartInitException {
		IViewPart showView = activePage.showView(IPageLayout.ID_PROP_SHEET);
		ShowInContext psc1 = new PropertyShowInContext(showView,
				StructuredSelection.EMPTY);
		ShowInContext psc2 = new PropertyShowInContext(showView,
				StructuredSelection.EMPTY);
		assertEquals(psc1.hashCode(), psc2.hashCode());
		psc2.setSelection(new StructuredSelection(new Object()));
		assertFalse(psc1.hashCode() == psc2.hashCode());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.views.properties.PropertyShowInContext#hashCode()}.
	 * 
	 * @throws PartInitException
	 */
	public final void testHashCode4() throws PartInitException {
		IViewPart showView = activePage.showView(IPageLayout.ID_PROP_SHEET);
		ShowInContext psc1 = new PropertyShowInContext(showView,
				new ShowInContext(null, null));
		PropertyShowInContext psc2 = new PropertyShowInContext(showView,
				new ShowInContext(null, null));
		assertEquals(psc1.hashCode(), psc2.hashCode());

		psc2.setPart(null);
		assertFalse(psc1.hashCode() == psc2.hashCode());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.views.properties.PropertyShowInContext#hashCode()}.
	 * 
	 * @throws PartInitException
	 */
	public final void testHashCode5() throws PartInitException {
		IViewPart showView = activePage.showView(IPageLayout.ID_PROP_SHEET);
		ShowInContext showInContext = new ShowInContext(null, null);
		ShowInContext psc1 = new PropertyShowInContext(showView, showInContext);
		ShowInContext psc2 = new PropertyShowInContext(showView, showInContext);
		assertEquals(psc1.hashCode(), psc2.hashCode());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.views.properties.PropertyShowInContext#hashCode()}.
	 * 
	 * @throws PartInitException
	 */
	public final void testHashCode6() throws PartInitException {
		IViewPart showView = activePage.showView(IPageLayout.ID_PROP_SHEET);
		ShowInContext psc1 = new PropertyShowInContext(showView,
				new ShowInContext(new Object(), null));
		ShowInContext psc2 = new PropertyShowInContext(showView,
				new ShowInContext(null, null));
		assertFalse(psc1.hashCode() == psc2.hashCode());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.views.properties.PropertyShowInContext#hashCode()}.
	 * 
	 * @throws PartInitException
	 */
	public final void testHashCode7() throws PartInitException {
		IViewPart showView = activePage.showView(IPageLayout.ID_PROP_SHEET);
		ShowInContext psc1 = new PropertyShowInContext(showView,
				new ShowInContext(null, null));
		ShowInContext psc2 = new PropertyShowInContext(showView,
				new ShowInContext(null, StructuredSelection.EMPTY));
		assertFalse(psc1.hashCode() == psc2.hashCode());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.views.properties.PropertyShowInContext#equals(Object)}
	 * .
	 */
	public final void testEquals() {
		ShowInContext psc1 = new PropertyShowInContext(null,
				StructuredSelection.EMPTY);
		psc1.setSelection(null);
		ShowInContext psc2 = new PropertyShowInContext(null,
				StructuredSelection.EMPTY);
		psc2.setSelection(null);
		assertEquals(psc1, psc2);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.views.properties.PropertyShowInContext#equals(Object)}
	 * .
	 */
	public final void testEquals2() {
		ShowInContext psc1 = new PropertyShowInContext(null,
				StructuredSelection.EMPTY);
		ShowInContext psc2 = new PropertyShowInContext(null,
				StructuredSelection.EMPTY);
		assertEquals(psc1, psc2);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.views.properties.PropertyShowInContext#equals(Object)}
	 * .
	 * 
	 * @throws PartInitException
	 */
	public final void testEquals3() throws PartInitException {
		IViewPart showView = activePage.showView(IPageLayout.ID_PROP_SHEET);
		ShowInContext psc1 = new PropertyShowInContext(showView,
				StructuredSelection.EMPTY);
		ShowInContext psc2 = new PropertyShowInContext(showView,
				StructuredSelection.EMPTY);
		assertEquals(psc1, psc2);
		psc2.setSelection(new StructuredSelection(new Object()));
		assertFalse(psc1.equals(psc2));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.views.properties.PropertyShowInContext#equals(Object)}
	 * .
	 * 
	 * @throws PartInitException
	 */
	public final void testEquals4() throws PartInitException {
		IViewPart showView = activePage.showView(IPageLayout.ID_PROP_SHEET);
		ShowInContext psc1 = new PropertyShowInContext(showView,
				new ShowInContext(null, null));
		PropertyShowInContext psc2 = new PropertyShowInContext(showView,
				new ShowInContext(null, null));
		assertEquals(psc1, psc2);

		psc2.setPart(null);
		assertFalse(psc1.equals(psc2));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.views.properties.PropertyShowInContext#equals(Object)}
	 * .
	 * 
	 * @throws PartInitException
	 */
	public final void testEquals5() throws PartInitException {
		IViewPart showView = activePage.showView(IPageLayout.ID_PROP_SHEET);
		ShowInContext showInContext = new ShowInContext(null, null);
		ShowInContext psc1 = new PropertyShowInContext(showView, showInContext);
		ShowInContext psc2 = new PropertyShowInContext(showView, showInContext);
		assertEquals(psc1, psc2);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.views.properties.PropertyShowInContext#equals(Object)}
	 * .
	 * 
	 * @throws PartInitException
	 */
	public final void testEqualsNullInput() throws PartInitException {
		IViewPart showView = activePage.showView(IPageLayout.ID_PROP_SHEET);
		ShowInContext psc1 = new PropertyShowInContext(showView,
				new ShowInContext(new Object(), null));
		ShowInContext psc2 = new PropertyShowInContext(showView,
				new ShowInContext(null, null));
		assertTrue(psc1.equals(psc2));
		assertTrue(psc2.equals(psc1));
	}
	
	/**
	 * Test method for
	 * {@link org.eclipse.ui.views.properties.PropertyShowInContext#equals(Object)}
	 * .
	 * 
	 * @throws PartInitException
	 */
	public final void testEqualsNullInputBoth() throws PartInitException {
		IViewPart showView = activePage.showView(IPageLayout.ID_PROP_SHEET);
		ShowInContext psc1 = new PropertyShowInContext(showView,
				new ShowInContext(null, null));
		ShowInContext psc2 = new PropertyShowInContext(showView,
				new ShowInContext(null, null));
		assertTrue(psc2.equals(psc1));
		assertTrue(psc1.equals(psc2));
	}
	
	/**
	 * Test method for
	 * {@link org.eclipse.ui.views.properties.PropertyShowInContext#equals(Object)}
	 * .
	 * 
	 * @throws PartInitException
	 */
	public final void testEqualsNonNullInput() throws PartInitException {
		IViewPart showView = activePage.showView(IPageLayout.ID_PROP_SHEET);
		ShowInContext psc1 = new PropertyShowInContext(showView,
				new ShowInContext(new Object(), null));
		ShowInContext psc2 = new PropertyShowInContext(showView,
				new ShowInContext(new Object(), null));
		assertFalse(psc1.equals(psc2));
		assertFalse(psc2.equals(psc1));
	}
	
	/**
	 * Test method for
	 * {@link org.eclipse.ui.views.properties.PropertyShowInContext#equals(Object)}
	 * .
	 * 
	 * @throws PartInitException
	 */
	public final void testEquals7() throws PartInitException {
		IViewPart showView = activePage.showView(IPageLayout.ID_PROP_SHEET);
		ShowInContext psc1 = new PropertyShowInContext(showView,
				new ShowInContext(null, null));
		ShowInContext psc2 = new PropertyShowInContext(showView,
				new ShowInContext(null, StructuredSelection.EMPTY));
		assertFalse(psc1.equals(psc2));
	}
}
