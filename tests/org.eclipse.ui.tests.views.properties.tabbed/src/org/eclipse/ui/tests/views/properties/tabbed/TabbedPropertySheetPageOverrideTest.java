/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.views.properties.tabbed;

import junit.framework.TestCase;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyComposite;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyList;
import org.eclipse.ui.tests.views.properties.tabbed.model.Error;
import org.eclipse.ui.tests.views.properties.tabbed.model.File;
import org.eclipse.ui.tests.views.properties.tabbed.model.Folder;
import org.eclipse.ui.tests.views.properties.tabbed.model.Information;
import org.eclipse.ui.tests.views.properties.tabbed.model.Warning;
import org.eclipse.ui.tests.views.properties.tabbed.override.OverrideTestsView;
import org.eclipse.ui.tests.views.properties.tabbed.views.TestsPerspective;

/**
 * Tests for the override tabs support.
 * 
 * @author Anthony Hunter
 * @since 3.4
 */
public class TabbedPropertySheetPageOverrideTest extends TestCase {

	private OverrideTestsView overrideTestsView;

	/**
	 * Get the list of tabs from the tabbed properties view.
	 * 
	 * @return the tab list.
	 */
	private TabbedPropertyList getTabbedPropertyList() {
		Control control = overrideTestsView.getTabbedPropertySheetPage()
				.getControl();
		assertTrue(control instanceof TabbedPropertyComposite);
		TabbedPropertyComposite tabbedPropertyComposite = (TabbedPropertyComposite) control;
		return tabbedPropertyComposite.getList();
	}

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		/**
		 * Close the existing perspectives.
		 */
		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		assertNotNull(workbenchWindow);
		IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
		assertNotNull(workbenchPage);
		workbenchPage.closeAllPerspectives(false, false);

		/**
		 * Open the tests perspective.
		 */
		PlatformUI.getWorkbench().showPerspective(
				TestsPerspective.TESTS_PERSPECTIVE_ID, workbenchWindow);

		/**
		 * Open the dynamic tests view.
		 */
		IViewPart view = workbenchPage
				.showView(OverrideTestsView.OVERRIDE_TESTS_VIEW_ID);
		assertNotNull(view);
		assertTrue(view instanceof OverrideTestsView);
		overrideTestsView = (OverrideTestsView) view;
	}

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();

		/**
		 * Bug 175070: Make sure the views have finished painting.
		 */
		while (Display.getCurrent().readAndDispatch()) {
			//
		}

	}

	/**
	 * When nothing is selected, there is one tab called empty.
	 * <p>
	 * Normally an empty structured selection shows "Properties are not
	 * available". The override tests provide a custom selection provider that
	 * allows for the display of a tab and section when the selection is empty.
	 */
	public void test_tabForEmpty() {
		/**
		 * select nothing
		 */
		overrideTestsView.setSelection(null);

		TabbedPropertyList tabbedPropertyList = getTabbedPropertyList();
		/**
		 * First tab is "Empty Item"
		 */
		assertEquals(tabbedPropertyList.getElementAt(0).toString(),
				"Empty Item");//$NON-NLS-1$
		/**
		 * No second tab
		 */
		assertNull(tabbedPropertyList.getElementAt(1));
	}

	/**
	 * Tests for the dynamic tabs and sections (items) when the Error element is
	 * selected.
	 */
	public void test_tabForError() {
		/**
		 * select "Error"
		 */
		overrideTestsView.setSelection(Error.class);

		TabbedPropertyList tabbedPropertyList = getTabbedPropertyList();
		/**
		 * First tab is "Information".
		 */
		assertEquals(tabbedPropertyList.getElementAt(0).toString(),
				"Information");//$NON-NLS-1$

		/**
		 * Second tab is "Warning".
		 */
		assertEquals(tabbedPropertyList.getElementAt(1).toString(), "Warning");//$NON-NLS-1$

		/**
		 * Third tab is "Error" and is selected.
		 */
		assertEquals(tabbedPropertyList.getElementAt(2).toString(), "Error");//$NON-NLS-1$
		assertTrue(tabbedPropertyList.getSelectionIndex() == 2);

		/**
		 * No fourth tab
		 */
		assertNull(tabbedPropertyList.getElementAt(3));

	}

	/**
	 * Tests for the dynamic tabs and sections (items) when the File element is
	 * selected.
	 */
	public void test_tabForFile() {
		/**
		 * select "File"
		 */
		overrideTestsView.setSelection(File.class);

		TabbedPropertyList tabbedPropertyList = getTabbedPropertyList();
		/**
		 * First tab is "File" and is selected.
		 */
		assertEquals(tabbedPropertyList.getElementAt(0).toString(), "File");//$NON-NLS-1$
		assertTrue(tabbedPropertyList.getSelectionIndex() == 0);

		/**
		 * Second tab is "Folder".
		 */
		assertEquals(tabbedPropertyList.getElementAt(1).toString(), "Folder");//$NON-NLS-1$

		/**
		 * No third tab
		 */
		assertNull(tabbedPropertyList.getElementAt(2));
	}

	/**
	 * Tests for the dynamic tabs and sections (items) when the Folder element
	 * is selected.
	 */
	public void test_tabForFolder() {
		/**
		 * select "Folder"
		 */
		overrideTestsView.setSelection(Folder.class);

		TabbedPropertyList tabbedPropertyList = getTabbedPropertyList();
		/**
		 * First tab is "File".
		 */
		assertEquals(tabbedPropertyList.getElementAt(0).toString(), "File");//$NON-NLS-1$

		/**
		 * Second tab is "Folder" and is selected.
		 */
		assertEquals(tabbedPropertyList.getElementAt(1).toString(), "Folder");//$NON-NLS-1$
		assertTrue(tabbedPropertyList.getSelectionIndex() == 1);

		/**
		 * No third tab
		 */
		assertNull(tabbedPropertyList.getElementAt(2));
	}

	/**
	 * Tests for the dynamic tabs and sections (items) when the Information
	 * element is selected.
	 */
	public void test_tabForInformation() {
		/**
		 * select "Information"
		 */
		overrideTestsView.setSelection(Information.class);

		TabbedPropertyList tabbedPropertyList = getTabbedPropertyList();
		/**
		 * First tab is "Information" and is selected.
		 */
		assertEquals(tabbedPropertyList.getElementAt(0).toString(),
				"Information");//$NON-NLS-1$
		assertTrue(tabbedPropertyList.getSelectionIndex() == 0);

		/**
		 * Second tab is "Warning".
		 */
		assertEquals(tabbedPropertyList.getElementAt(1).toString(), "Warning");//$NON-NLS-1$

		/**
		 * Third tab is "Error".
		 */
		assertEquals(tabbedPropertyList.getElementAt(2).toString(), "Error");//$NON-NLS-1$

		/**
		 * No fourth tab
		 */
		assertNull(tabbedPropertyList.getElementAt(3));
	}

	/**
	 * Tests for the dynamic tabs and sections (items) when the Warning element
	 * is selected.
	 */
	public void test_tabForWarning() {
		/**
		 * select "Warning"
		 */
		overrideTestsView.setSelection(Warning.class);

		TabbedPropertyList tabbedPropertyList = getTabbedPropertyList();
		/**
		 * First tab is "Information".
		 */
		assertEquals(tabbedPropertyList.getElementAt(0).toString(),
				"Information");//$NON-NLS-1$

		/**
		 * Second tab is "Warning" and is selected.
		 */
		assertEquals(tabbedPropertyList.getElementAt(1).toString(), "Warning");//$NON-NLS-1$
		assertTrue(tabbedPropertyList.getSelectionIndex() == 1);

		/**
		 * Third tab is "Error".
		 */
		assertEquals(tabbedPropertyList.getElementAt(2).toString(), "Error");//$NON-NLS-1$

		/**
		 * No fourth tab
		 */
		assertNull(tabbedPropertyList.getElementAt(3));

	}

}
