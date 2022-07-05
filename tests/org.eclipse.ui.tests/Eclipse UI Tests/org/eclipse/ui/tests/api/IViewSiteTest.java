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
package org.eclipse.ui.tests.api;

import static org.junit.Assert.assertNotNull;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class IViewSiteTest extends IWorkbenchPartSiteTest {

	/**
	 * Constructor for IViewPartSiteTest
	 */
	public IViewSiteTest() {
		super(IViewSiteTest.class.getSimpleName());
	}

	/**
	 * @see IWorkbenchPartSiteTest#getTestPartName()
	 */
	@Override
	protected String getTestPartName() throws Throwable {
		return MockViewPart.NAME;
	}

	/**
	 * @see IWorkbenchPartSiteTest#getTestPartId()
	 */
	@Override
	protected String getTestPartId() throws Throwable {
		return MockViewPart.ID;
	}

	/**
	 * @see IWorkbenchPartSiteTest#createTestPart(IWorkbenchPage)
	 */
	@Override
	protected IWorkbenchPart createTestPart(IWorkbenchPage page)
			throws Throwable {
		return page.showView(MockViewPart.ID);
	}

	@Test
	public void testGetActionBars() throws Throwable {
		// From Javadoc: "Returns the action bars for this part site."

		IViewPart view = (IViewPart) createTestPart(fPage);
		IViewSite site = view.getViewSite();
		assertNotNull(site.getActionBars());
	}

}

