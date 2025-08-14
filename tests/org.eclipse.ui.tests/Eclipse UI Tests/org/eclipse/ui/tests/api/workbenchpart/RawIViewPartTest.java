/*******************************************************************************
 * Copyright (c) 2004, 2019 IBM Corporation and others.
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
package org.eclipse.ui.tests.api.workbenchpart;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;

import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


/**
 * @since 3.0
 */
@RunWith(JUnit4.class)
public class RawIViewPartTest extends UITestCase {

	public RawIViewPartTest() {
		super(RawIViewPartTest.class.getSimpleName());
	}

	IWorkbenchWindow window;

	IWorkbenchPage page;

	RawIViewPart view;

	IWorkbenchPartReference ref;

	boolean titleChangeEvent = false;

	boolean nameChangeEvent = false;

	boolean contentChangeEvent = false;

	private final IPropertyListener propertyListener = (source, propId) -> {
		switch (propId) {
		case IWorkbenchPartConstants.PROP_TITLE:
			titleChangeEvent = true;
			break;
		case IWorkbenchPartConstants.PROP_PART_NAME:
			nameChangeEvent = true;
			break;
		case IWorkbenchPartConstants.PROP_CONTENT_DESCRIPTION:
			contentChangeEvent = true;
			break;
		}
	};

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		window = openTestWindow();
		page = window.getActivePage();
		view = (RawIViewPart) page
				.showView("org.eclipse.ui.tests.workbenchpart.RawIViewPart");
		ref = page
				.findViewReference("org.eclipse.ui.tests.workbenchpart.RawIViewPart");
		ref.addPropertyListener(propertyListener);
		titleChangeEvent = false;
		nameChangeEvent = false;
		contentChangeEvent = false;
	}

	@Override
	protected void doTearDown() throws Exception {
		view.removePropertyListener(propertyListener);
		page.hideView(view);
		super.doTearDown();
	}

	private void verifySettings(IWorkbenchPart part, String expectedTitle,
			String expectedPartName, String expectedContentDescription)
			throws Exception {
		Assert.assertEquals("Incorrect view title", expectedTitle, part
				.getTitle());

		Assert.assertEquals("Incorrect title in view reference", expectedTitle,
				ref.getTitle());
		Assert.assertEquals("Incorrect part name in view reference",
				expectedPartName, ref.getPartName());
		Assert.assertEquals("Incorrect content description in view reference",
				expectedContentDescription, ref.getContentDescription());
	}

	private void verifySettings(String expectedTitle, String expectedPartName,
			String expectedContentDescription) throws Exception {
		verifySettings(view, expectedTitle, expectedPartName,
				expectedContentDescription);
	}

	/**
	 * Ensure that we've received the given property change events since the start of the test
	 *
	 * @param titleEvent PROP_TITLE
	 * @param nameEvent PROP_PART_NAME
	 * @param descriptionEvent PROP_CONTENT_DESCRIPTION
	 */
	private void verifyEvents(boolean titleEvent, boolean nameEvent,
			boolean descriptionEvent) {
		if (titleEvent) {
			Assert.assertEquals("Missing title change event", titleEvent,
					titleChangeEvent);
		}
		if (nameEvent) {
			Assert.assertEquals("Missing name change event", nameEvent,
					nameChangeEvent);
		}
		if (descriptionEvent) {
			Assert.assertEquals("Missing content description event",
					descriptionEvent, contentChangeEvent);
		}
	}

	@Test
	public void testDefaults() throws Throwable {
		verifySettings("SomeTitle", "RawIViewPart", "SomeTitle");
		verifyEvents(false, false, false);
	}

	@Test
	@Ignore
	public void XXXtestCustomTitle() throws Throwable {
		view.setTitle("CustomTitle");
		verifySettings("CustomTitle", "RawIViewPart", "CustomTitle");
		verifyEvents(true, false, true);
	}

	/**
	 * Ensures that the content description is empty when the title is the same
	 * as the default part name
	 */
	@Test
	@Ignore
	public void XXXtestEmptyContentDescription() throws Throwable {
		view.setTitle("RawIViewPart");
		verifySettings("RawIViewPart", "RawIViewPart", "");
		verifyEvents(true, false, true);
	}
}
