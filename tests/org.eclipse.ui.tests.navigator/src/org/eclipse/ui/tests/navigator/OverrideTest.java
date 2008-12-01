/*******************************************************************************
p * Copyright (c) 2008 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.tests.navigator.extension.TestLabelProviderCyan;
import org.eclipse.ui.tests.navigator.extension.TestLabelProviderGreen;

public class OverrideTest extends NavigatorTestBase {

	public OverrideTest() {
		_navigatorInstanceId = "org.eclipse.ui.tests.navigator.OverrideTestView";
	}

	// bug 252293 [CommonNavigator] LabelProviders do not obey override rules
	public void NOTYETtestSimpleResFirst() throws Exception {

		if (!false) {
			contentService.bindExtensions(new String[] { TEST_CONTENT1,
					TEST_OVERRIDE1 }, false);
			contentService.getActivationService().activateExtensions(
					new String[] { TEST_OVERRIDE1, TEST_CONTENT1 }, true);
		}

		viewer.refresh();

		TreeItem[] rootItems = viewer.getTree().getItems();

		if (false) {
			DisplayHelper.sleep(Display.getCurrent(), 10000000);
		}

		if (!rootItems[0].getText().startsWith("Green"))
			fail("Wrong text: " + rootItems[0].getText());
		assertEquals(TestLabelProviderGreen.getTestColor(), rootItems[0]
				.getBackground(0));
	}

	// bug 252293 [CommonNavigator] LabelProviders do not obey override rules
	public void NOTYETtestSimpleResLast() throws Exception {

		if (!false) {
			contentService.bindExtensions(new String[] { TEST_CONTENT2,
					TEST_OVERRIDE2 }, false);
			contentService.getActivationService().activateExtensions(
					new String[] { TEST_CONTENT2, TEST_OVERRIDE2 }, true);
		}

		viewer.refresh();
		TreeItem[] rootItems = viewer.getTree().getItems();
		assertEquals(TestLabelProviderCyan.getTestColor(), rootItems[0]
				.getBackground(0));
	}

}
