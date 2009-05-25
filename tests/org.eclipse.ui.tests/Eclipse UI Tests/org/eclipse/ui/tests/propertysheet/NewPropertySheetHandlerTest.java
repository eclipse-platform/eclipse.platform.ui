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

import java.util.HashMap;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.tests.SelectionProviderView;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.PropertyShowInContext;

/**
 * @since 3.5
 * 
 */
public class NewPropertySheetHandlerTest extends AbstractPropertySheetTest {

	private TestNewPropertySheetHandler testNewPropertySheetHandler;

	public NewPropertySheetHandlerTest(String testName) {
		super(testName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.tests.propertysheet.AbstractPropertySheetTest#doSetUp()
	 */
	protected void doSetUp() throws Exception {
		super.doSetUp();
		testNewPropertySheetHandler = new TestNewPropertySheetHandler();
	}

	private ExecutionEvent getExecutionEvent() {
		IHandlerService handlerService = (IHandlerService) PlatformUI
				.getWorkbench().getService(IHandlerService.class);
		ICommandService commandService = (ICommandService) PlatformUI
				.getWorkbench().getService(ICommandService.class);
		IEvaluationContext evalContext = handlerService.getCurrentState();
		Command command = commandService
				.getCommand(TestNewPropertySheetHandler.ID);
		ExecutionEvent executionEvent = new ExecutionEvent(command,
				new HashMap(), null, evalContext);
		return executionEvent;
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.tests.propertysheet.TestNewPropertySheetHandler#getShowInContext(org.eclipse.core.commands.ExecutionEvent)}
	 * .
	 * 
	 * @throws ExecutionException
	 * @throws PartInitException
	 *             StructuredSelection.EMPTY,
	 */
	public final void testGetShowInContextFromPropertySheet()
			throws ExecutionException, PartInitException {
		activePage.showView(IPageLayout.ID_PROP_SHEET);

		PropertyShowInContext context = testNewPropertySheetHandler
				.getShowInContext(getExecutionEvent());
		assertNotNull(context);
		assertNull(context.getSelection());
		assertNull(context.getPart());
		assertNull(context.getInput());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.tests.propertysheet.TestNewPropertySheetHandler#getShowInContext(org.eclipse.core.commands.ExecutionEvent)}
	 * .
	 * 
	 * @throws ExecutionException
	 * @throws PartInitException
	 */
	public final void testGetShowInContextFromAShowInSource()
			throws ExecutionException, PartInitException {
		IAdapterFactory factory = new IAdapterFactory() {
			public Object getAdapter(Object adaptableObject, Class adapterType) {
				return new IShowInSource() {
					public ShowInContext getShowInContext() {
						return new ShowInContext(StructuredSelection.EMPTY,
								StructuredSelection.EMPTY);
					}
				};
			}

			public Class[] getAdapterList() {
				return new Class[] { IShowInSource.class };
			}
		};
		try {
			SelectionProviderView selectionProviderView = (SelectionProviderView) activePage
					.showView(SelectionProviderView.ID);
			selectionProviderView.setSelection(StructuredSelection.EMPTY);
			Platform.getAdapterManager().registerAdapters(factory,
					SelectionProviderView.class);

			PropertyShowInContext context = testNewPropertySheetHandler
					.getShowInContext(getExecutionEvent());
			assertNotNull(context);
			assertEquals(StructuredSelection.EMPTY, context.getSelection());
			assertEquals(StructuredSelection.EMPTY, context.getInput());
			assertEquals(selectionProviderView, context.getPart());
		} finally {
			Platform.getAdapterManager().unregisterAdapters(factory);
		}
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.tests.propertysheet.TestNewPropertySheetHandler#getShowInContext(org.eclipse.core.commands.ExecutionEvent)}
	 * .
	 * 
	 * @throws ExecutionException
	 * @throws PartInitException
	 */
	public final void testGetShowInContextWithNoShowInSource()
			throws PartInitException, ExecutionException {
		SelectionProviderView selectionProviderView = (SelectionProviderView) activePage
				.showView(SelectionProviderView.ID);
		assertFalse(selectionProviderView instanceof IShowInSource);
		assertNull(selectionProviderView.getAdapter(IShowInSource.class));

		PropertyShowInContext context = testNewPropertySheetHandler
				.getShowInContext(getExecutionEvent());
		assertNotNull(context);
		assertNull(context.getSelection());
		assertNull(context.getInput());
		assertEquals(selectionProviderView, context.getPart());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.tests.propertysheet.TestNewPropertySheetHandler#getShowInContext(org.eclipse.core.commands.ExecutionEvent)}
	 * .
	 */
	public final void testGetShowInContextWithNoActivePart() {
		try {
			testNewPropertySheetHandler.getShowInContext(getExecutionEvent());
		} catch (ExecutionException e) {
			return;
		}
		fail("Expected ExecutionException due to no active part");
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.tests.propertysheet.TestNewPropertySheetHandler#findPropertySheet(org.eclipse.core.commands.ExecutionEvent, org.eclipse.ui.views.properties.PropertyShowInContext)}
	 * .
	 * 
	 * @throws ExecutionException
	 * @throws PartInitException
	 */
	public final void testFindPropertySheetWithoutActivePart()
			throws PartInitException, ExecutionException {
		assertNull(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActivePart());

		try {
			testNewPropertySheetHandler.findPropertySheet(getExecutionEvent(),
					new PropertyShowInContext(null, StructuredSelection.EMPTY));
		} catch (ExecutionException e) {
			return;
		}
		fail("Expected ExecutionException due to no active part");
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.tests.propertysheet.TestNewPropertySheetHandler#findPropertySheet(org.eclipse.core.commands.ExecutionEvent, org.eclipse.ui.views.properties.PropertyShowInContext)}
	 * .
	 * 
	 * @throws ExecutionException
	 * @throws PartInitException
	 */
	public final void testFindPropertySheetWithOtherSheetActive()
			throws PartInitException, ExecutionException {
		propertySheet = (PropertySheet) activePage
				.showView(IPageLayout.ID_PROP_SHEET);
		assertTrue(countPropertySheetViews() == 1);

		PropertySheet foundSheet = testNewPropertySheetHandler
				.findPropertySheet(getExecutionEvent(),
						new PropertyShowInContext(propertySheet,
								StructuredSelection.EMPTY));
		assertNotNull(foundSheet);
		assertNotSame(propertySheet, foundSheet);
		assertTrue(countPropertySheetViews() == 2);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.tests.propertysheet.TestNewPropertySheetHandler#findPropertySheet(org.eclipse.core.commands.ExecutionEvent, org.eclipse.ui.views.properties.PropertyShowInContext)}
	 * .
	 * 
	 * @throws ExecutionException
	 * @throws PartInitException
	 */
	public final void testFindPropertySheetWithSPVActive()
			throws PartInitException, ExecutionException {
		IViewPart showView = activePage.showView(IPageLayout.ID_PROP_SHEET);
		IViewPart spv = activePage.showView(SelectionProviderView.ID);
		assertTrue(countPropertySheetViews() == 1);

		PropertySheet foundSheet = testNewPropertySheetHandler
				.findPropertySheet(getExecutionEvent(),
						new PropertyShowInContext(spv,
								StructuredSelection.EMPTY));
		assertNotNull(foundSheet);
		assertEquals(showView, foundSheet);
		assertTrue(countPropertySheetViews() == 1);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.tests.propertysheet.TestNewPropertySheetHandler#findPropertySheet(org.eclipse.core.commands.ExecutionEvent, org.eclipse.ui.views.properties.PropertyShowInContext)}
	 * .
	 * 
	 * @throws ExecutionException
	 * @throws PartInitException
	 */
	public final void testFindPropertySheetWithPinnedPSandSPVActive()
			throws PartInitException, ExecutionException {
		PropertySheet sheet = (PropertySheet) activePage
				.showView(IPageLayout.ID_PROP_SHEET);
		sheet.setPinned(true);
		IViewPart spv = activePage.showView(SelectionProviderView.ID);
		assertTrue(countPropertySheetViews() == 1);

		PropertySheet foundSheet = testNewPropertySheetHandler
				.findPropertySheet(getExecutionEvent(),
						new PropertyShowInContext(spv,
								StructuredSelection.EMPTY));
		assertNotNull(foundSheet);
		assertNotSame(sheet, foundSheet);
		assertTrue(countPropertySheetViews() == 2);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.ui.tests.propertysheet.TestNewPropertySheetHandler#findPropertySheet(org.eclipse.core.commands.ExecutionEvent, org.eclipse.ui.views.properties.PropertyShowInContext)}
	 * .
	 * 
	 * @throws ExecutionException
	 * @throws PartInitException
	 */
	public final void testFindPropertySheetWithUnpinnedPSandSPVActive()
			throws PartInitException, ExecutionException {
	    PropertySheetPerspectiveFactory.applyPerspective(activePage);
	    
		PropertySheet sheet = (PropertySheet) activePage
				.showView(IPageLayout.ID_PROP_SHEET);
		IViewPart showView = activePage.showView(SelectionProviderView.ID);
		PropertyShowInContext context = new PropertyShowInContext(showView,
				StructuredSelection.EMPTY);
		assertTrue(sheet.show(context));
		sheet.setPinned(true);
		assertTrue(countPropertySheetViews() == 1);

		PropertySheet foundSheet = testNewPropertySheetHandler
				.findPropertySheet(getExecutionEvent(), context);
		assertNotNull(foundSheet);
		assertEquals(sheet, foundSheet);
		assertTrue(countPropertySheetViews() == 1);
	}
}
