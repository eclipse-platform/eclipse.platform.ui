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

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.navigator.filters.CommonFilterSelectionDialog;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.tests.harness.util.DisplayHelper;

public class ExtensionsTest extends NavigatorTestBase {

	private static final boolean DEBUG = false;

	public ExtensionsTest() {
		_navigatorInstanceId = TEST_VIEWER_HIDE_EXTENSIONS;
	}

	class CFDialog extends CommonFilterSelectionDialog {
		public CFDialog(CommonViewer aCommonViewer) {
			super(aCommonViewer);
		}

		public void finish() {
			okPressed();
			close();
		}

	}

	// Bug 185561 when hideAvailableExtensionsTab is true, everything gone
	public void testHideAvailableExtensions() throws Exception {
		assertEquals(3, _commonNavigator.getCommonViewer().getTree()
				.getItemCount());

		// Just showing the filters dialog upsets the apple cart
		CFDialog cfDialog = new CFDialog(_commonNavigator.getCommonViewer());
		cfDialog.create();
		cfDialog.finish();

		assertEquals(_projectCount, _commonNavigator.getCommonViewer().getTree()
				.getItemCount());

		if (DEBUG)
			DisplayHelper.sleep(Display.getCurrent(), 10000000);

	}

}
