/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.navigator;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PartInitException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests the Resource Navigator view.
 */
@RunWith(JUnit4.class)
public class NavigatorTest extends AbstractNavigatorTest {

	public NavigatorTest() {
		super(NavigatorTest.class.getSimpleName());
	}

	/**
	 * Tests that the Navigator is initially populated with
	 * the correct elements from the workspace.
	 */
	@Test
	public void testInitialPopulation() throws CoreException, PartInitException {
		createTestFile();
		showNav();

		// test its initial content by setting and getting selection on the file
		ISelectionProvider selProv = navigator.getSite().getSelectionProvider();
		StructuredSelection sel = new StructuredSelection(testFile);
		selProv.setSelection(sel);
		assertEquals(sel.size(), ((IStructuredSelection)selProv.getSelection()).size());
		assertEquals(sel.getFirstElement(), ((IStructuredSelection) selProv.getSelection()).getFirstElement());
	}

	/**
	 * Tests that the Navigator updates properly when a file is added to the workbench.
	 */
	@Test
	public void testFileAddition() throws CoreException, PartInitException {
		createTestFolder(); // create the project and folder before the Navigator is shown
		showNav();
		createTestFile(); // create the file after the Navigator is shown

		// test its initial content by setting and getting selection on the file
		ISelectionProvider selProv = navigator.getSite().getSelectionProvider();
		StructuredSelection sel = new StructuredSelection(testFile);
		selProv.setSelection(sel);
		assertEquals(sel.size(), ((IStructuredSelection)selProv.getSelection()).size());
		assertEquals(sel.getFirstElement(), ((IStructuredSelection)selProv.getSelection()).getFirstElement());
	}

}
