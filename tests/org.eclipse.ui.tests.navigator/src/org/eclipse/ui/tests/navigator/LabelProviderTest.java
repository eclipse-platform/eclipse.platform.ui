/*******************************************************************************
 * Copyright (c) 2008, 2009 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 *.....IBM Corporation - fixed dead code warning
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.tests.navigator.extension.TestLabelProvider;
import org.eclipse.ui.tests.navigator.extension.TestLabelProviderBlank;
import org.eclipse.ui.tests.navigator.extension.TestLabelProviderCyan;
import org.eclipse.ui.tests.navigator.extension.TestLabelProviderGreen;

public class LabelProviderTest extends NavigatorTestBase {

	public LabelProviderTest() {
		_navigatorInstanceId = "org.eclipse.ui.tests.navigator.OverrideTestView";
	}

	// Backed out this test because the blank label provider was actually 
	// used to signify no label content  see bug 268250
	public void XXXtestBlankLabelProvider() throws Exception {

		TestLabelProvider._blankStatic = true;
		
		_contentService.bindExtensions(new String[] { TEST_CONTENT_OVERRIDDEN1,
				TEST_CONTENT_OVERRIDE1 }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_OVERRIDE1, TEST_CONTENT_OVERRIDDEN1 }, true);

		refreshViewer();

		TreeItem[] rootItems = _viewer.getTree().getItems();

		if (!rootItems[0].getText().equals(""))
			fail("Wrong text: " + rootItems[0].getText());
	}

	// bug 252293 [CommonNavigator] LabelProviders do not obey override rules
	public void testSimpleResFirst() throws Exception {

		_contentService.bindExtensions(new String[] { TEST_CONTENT_OVERRIDDEN1,
				TEST_CONTENT_OVERRIDE1 }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_OVERRIDE1, TEST_CONTENT_OVERRIDDEN1 }, true);

		refreshViewer();

		TreeItem[] rootItems = _viewer.getTree().getItems();


		if (!rootItems[0].getText().startsWith("Green"))
			fail("Wrong text: " + rootItems[0].getText());
		assertEquals(TestLabelProviderGreen.getTestColor(), rootItems[0]
				.getBackground(0));
	}

	
	// bug 252293 [CommonNavigator] LabelProviders do not obey override rules
	public void testSimpleResLast() throws Exception {
		_contentService.bindExtensions(new String[] { TEST_CONTENT_OVERRIDDEN2,
				TEST_CONTENT_OVERRIDE2 }, false);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_OVERRIDDEN2, TEST_CONTENT_OVERRIDE2 }, true);

		refreshViewer();

		TreeItem[] rootItems = _viewer.getTree().getItems();
		if (!rootItems[0].getText().startsWith("Cyan"))
			fail("Wrong text: " + rootItems[0].getText());
		assertEquals(TestLabelProviderCyan.getTestColor(), rootItems[0]
				.getBackground(0));
	}

	
	// Make sure that it finds label providers that are in overridden content extensions
	// if none of the label providers from the desired content extensions return anything
	public void testUsingOverriddenLabelProvider() throws Exception {

		_contentService.bindExtensions(new String[] { TEST_CONTENT_OVERRIDDEN2,
				TEST_CONTENT_OVERRIDE2_BLANK }, true);
		_contentService.getActivationService().activateExtensions(
				new String[] { TEST_CONTENT_OVERRIDDEN2, TEST_CONTENT_OVERRIDE2_BLANK }, true);

		refreshViewer();
		
		TreeItem[] rootItems = _viewer.getTree().getItems();
		
		//DisplayHelper.sleep(10000000);
		
		// But we get the text from the overridden label provider
		if (!rootItems[0].getText().startsWith("Blue"))
			fail("Wrong text: " + rootItems[0].getText());
		// We get the color from the blank label provider
		assertEquals(TestLabelProviderBlank.getTestColor(), rootItems[0]
				.getBackground(0));
	}
	
}
