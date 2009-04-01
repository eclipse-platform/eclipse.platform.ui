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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.SelectionProviderView;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.session.NonRestorableView;
import org.eclipse.ui.views.properties.NewPropertySheetHandler;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.PropertySheetPage;

/**
 * @since 3.4
 */
public class MultiInstancePropertySheetTest extends AbstractPropertySheetTest {

	/**
	 * TestPropertySheetPage exposes certain members for testability
	 */
	private TestPropertySheetPage testPropertySheetPage = new TestPropertySheetPage();
	private SelectionProviderView selectionProviderView;
	
	private IProject project;

	public MultiInstancePropertySheetTest(String testName) {
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
		// open the property sheet with the TestPropertySheetPage
		Platform.getAdapterManager().registerAdapters(testPropertySheetPage,
				PropertySheet.class);
		
		PropertySheetPerspectiveFactory.applyPerspective(activePage);
		
		propertySheet = (PropertySheet) activePage
				.showView(IPageLayout.ID_PROP_SHEET);

		selectionProviderView = (SelectionProviderView) activePage
				.showView(SelectionProviderView.ID);
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.harness.util.UITestCase#doTearDown()
	 */
	protected void doTearDown() throws Exception {
	    activePage.resetPerspective();         
		super.doTearDown();
		Platform.getAdapterManager().unregisterAdapters(testPropertySheetPage,
				PropertySheet.class);
		testPropertySheetPage = null;
		selectionProviderView = null;

        if (project != null) {
            FileUtil.deleteProject(project);
            project = null;
        }        
	}

	/**
	 * The if the registered {@link TestPropertySheetPage} is set as the default
	 * page of the PropertySheet
	 * 
	 * @throws PartInitException
	 */
	public void testDefaultPage() throws PartInitException {
		PropertySheet propertySheet = (PropertySheet) activePage
				.showView(IPageLayout.ID_PROP_SHEET);
		assertTrue(propertySheet.getCurrentPage() instanceof PropertySheetPage);
	}

	/**
	 * Test if the registered {@link TestPropertySheetPage} is set as the
	 * default page of the PropertyShecet
	 * 
	 * @throws PartInitException
	 */
	public void testDefaultPageAdapter() throws PartInitException {
		PropertySheet propertySheet = (PropertySheet) activePage
				.showView(IPageLayout.ID_PROP_SHEET);
		assertTrue(propertySheet.getCurrentPage() instanceof TestPropertySheetPage);
	}

	/**
	 * Test if the PropertySheet allows multiple instances
	 * 
	 * @throws PartInitException
	 */
	public void testAllowsMultiple() throws PartInitException {
		activePage.showView(IPageLayout.ID_PROP_SHEET);
		try {
			activePage.showView(IPageLayout.ID_PROP_SHEET, "aSecondaryId",
					IWorkbenchPage.VIEW_ACTIVATE);
		} catch (PartInitException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test if the PropertySheet follows selection
	 * 
	 * @throws Throwable
	 */
	public void testFollowsSelection() throws Throwable {
		// selection before selection changes
		TestPropertySheetPage firstPage = (TestPropertySheetPage) propertySheet
				.getCurrentPage();
		Object firstSelection = firstPage.getSelection();
		assertNotNull(firstSelection);

		// change the selection explicitly
		selectionProviderView.setSelection(new Object());
		TestPropertySheetPage secondPage = (TestPropertySheetPage) propertySheet
				.getCurrentPage();

		assertNotSame("PropertySheet hasn't changed selection", firstSelection,
				secondPage.getSelection());
	}

	/**
	 * Test if the PropertySheet follows part events
	 * 
	 * @throws Throwable
	 */
	public void testFollowsParts() throws Throwable {
		// selection before selection changes
		TestPropertySheetPage firstPage = (TestPropertySheetPage) propertySheet
				.getCurrentPage();
		Object firstPart = firstPage.getPart();
		assertNotNull(firstPart);

		// change the part explicitly (reusing the NonRestorableView here)
		TestPropertySheetPage testPropertySheetPage2 = new TestPropertySheetPage();
		Platform.getAdapterManager().registerAdapters(testPropertySheetPage2,
				org.eclipse.ui.tests.session.NonRestorableView.class);
		activePage.showView(NonRestorableView.ID);

		TestPropertySheetPage secondPage = (TestPropertySheetPage) propertySheet
				.getCurrentPage();

		assertEquals(testPropertySheetPage2, secondPage);
		assertNotSame("PropertySheet hasn't changed selection", firstPart,
				secondPage.getSelection());
		Platform.getAdapterManager().unregisterAdapters(testPropertySheetPage2,
				org.eclipse.ui.tests.session.NonRestorableView.class);
	}

	/**
	 * Test if pinning works in the PropertySheet
	 * 
	 * @throws Throwable
	 */
	public void testPinning() throws Throwable {
		// execute the pin action on the property sheet
		IAction action = getPinPropertySheetAction(propertySheet);
		action.setChecked(true);

		// get the content of the pinned property sheet for later comparison
		TestPropertySheetPage firstPage = (TestPropertySheetPage) propertySheet
				.getCurrentPage();
		assertNotNull(firstPage);
		Object firstSelection = firstPage.getSelection();
		assertNotNull(firstSelection);
		IWorkbenchPart firstPart = firstPage.getPart();
		assertNotNull(firstPart);

		// change the selection/part
		selectionProviderView.setSelection(new Object());
		TestPropertySheetPage testPropertySheetPage2 = new TestPropertySheetPage();
		Platform.getAdapterManager().registerAdapters(testPropertySheetPage2,
				org.eclipse.ui.tests.session.NonRestorableView.class);
		activePage.showView(NonRestorableView.ID);

		TestPropertySheetPage secondPage = (TestPropertySheetPage) propertySheet
				.getCurrentPage();
		assertEquals("PropertySheet has changed page", firstPage, secondPage);
		assertEquals("PropertySheetPage has changed selection", firstSelection,
				secondPage.getSelection());
		assertEquals("PropertySheetPage has changed part", firstPart,
				secondPage.getPart());
		Platform.getAdapterManager().unregisterAdapters(testPropertySheetPage2,
				org.eclipse.ui.tests.session.NonRestorableView.class);
	}

	/**
	 * Test if the PropertySheet unpinns if the contributing part is closed
	 * 
	 * @throws Throwable
	 */
	public void testUnpinningWhenPinnedPartIsClosed() throws Throwable {
		// execute the pin action on the property sheet
		IAction action = getPinPropertySheetAction(propertySheet);
		action.setChecked(true);

		// close the part the property sheet is pinned to
		activePage.hideView(selectionProviderView);

		// the action and therefore the property sheet should be unpinned
		assertFalse(action.isChecked());
	}

	/**
	 * Test if the PropertySheet's new handler creates a new instance
	 * 
	 * @throws NotHandledException
	 * @throws NotEnabledException
	 * @throws NotDefinedException
	 * @throws ExecutionException
	 */
	public void testNewPropertySheet() throws ExecutionException,
			NotDefinedException, NotEnabledException, NotHandledException {
		assertTrue(countPropertySheetViews() == 1);
		executeNewPropertySheetHandler();
		assertTrue(countPropertySheetViews() == 2);
	}

	/**
	 * @throws ExecutionException
	 * @throws NotDefinedException
	 * @throws NotEnabledException
	 * @throws NotHandledException
	 */
	private void executeNewPropertySheetHandler() throws ExecutionException,
			NotDefinedException, NotEnabledException, NotHandledException {

		// the propertysheet is the active part if its view toolbar command gets
		// pressed
		activePage.activate(propertySheet);

		IHandlerService handlerService = (IHandlerService) PlatformUI
				.getWorkbench().getService(IHandlerService.class);
		Event event = new Event();
		handlerService.executeCommand(NewPropertySheetHandler.ID, event);
	}

	/**
	 * Test if the PropertySheet pins the parent if a second instance is opened
	 * 
	 * @throws NotHandledException
	 * @throws NotEnabledException
	 * @throws NotDefinedException
	 * @throws ExecutionException
	 */
	public void testParentIsPinned() throws ExecutionException,
			NotDefinedException, NotEnabledException, NotHandledException {
		executeNewPropertySheetHandler();

		IAction pinAction = getPinPropertySheetAction(propertySheet);
		assertTrue("Parent property sheet isn't pinned", pinAction.isChecked());
	}

	/**
	 * Test if the PropertySheet pins the parent if a second instance is opened
	 * 
	 * @throws Throwable
	 */
	public void testPinningWithMultipleInstances() throws Throwable {
		executeNewPropertySheetHandler();
		testPinning();
	}

	/**
	 * Tests that pinning a property sheet ensures that the content continues to
	 * be rendered even if the original target part is not visible and is behind
	 * another part in the part stack. 
	 * 
	 * @throws CoreException
	 */
	public void testBug268676HidingPinnedTargetPart() throws CoreException {
		IPerspectiveDescriptor desc = activePage.getWorkbenchWindow().getWorkbench()
				.getPerspectiveRegistry().findPerspectiveWithId(IDE.RESOURCE_PERSPECTIVE_ID);
		// open the 'Resource' perspective
		activePage.setPerspective(desc);
		activePage.hideView(selectionProviderView);
		propertySheet = (PropertySheet) activePage.showView(IPageLayout.ID_PROP_SHEET);
		
		// create a project for properties rendering purposes
        project = FileUtil.createProject("projectToSelect");
        ISelection selection = new StructuredSelection(project);
		
		// show the 'Navigator'
		IViewPart navigator = activePage.showView(IPageLayout.ID_RES_NAV);        
        // have the 'Navigator' select it
        navigator.getSite().getSelectionProvider().setSelection(selection);

        // verify that the 'Navigator' uses a regular property sheet page
		assertTrue("The 'Properties' view should render the content of the 'Navigator' in a regular property sheet page",
				propertySheet.getCurrentPage() instanceof PropertySheetPage);

		// show the 'Project Explorer'
		IViewPart projectExplorer = activePage.showView(IPageLayout.ID_PROJECT_EXPLORER);        
        // have the 'Project Explorer' select it        
        projectExplorer.getSite().getSelectionProvider().setSelection(selection);
		
		assertFalse("The 'Navigator' should be hidden behind the 'Project Explorer'",
				activePage.isPartVisible(navigator));
		assertTrue("The 'Project Explorer' should be visible in front of the 'Navigator'",
				activePage.isPartVisible(projectExplorer));

        // verify that the 'Project Explorer' uses a non-standard property sheet page
		assertFalse("The 'Properties' view should be showing the content of the 'Project Explorer' in a tabbed property sheet, not a regular one",
				propertySheet.getCurrentPage() instanceof PropertySheetPage);
		
		// pin the tabbed property sheet page
		IAction action = getPinPropertySheetAction(propertySheet);
		action.setChecked(true);
		
		// now activate the 'Navigator' so a partHidden event is fired for the
		// 'Project Explorer', just because it is hidden should _not_ mean the
		// _pinned_ 'Properties' view should stop rendering its content though,
		// this is the bug being tested
		activePage.activate(navigator);

		// verify that the page is not a non-standard property sheet page
		assertFalse("The 'Properties' view should still be on the content of the 'Project Explorer' rendering a tabbed property sheet",
				propertySheet.getCurrentPage() instanceof PropertySheetPage);
	}
}
