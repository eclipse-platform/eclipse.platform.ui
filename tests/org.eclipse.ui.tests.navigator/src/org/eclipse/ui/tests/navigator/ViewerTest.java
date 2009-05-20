/*******************************************************************************
 * Copyright (c) 2009 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.internal.help.WorkbenchHelpSystem;
import org.eclipse.ui.tests.harness.util.EditorTestHelper;
import org.eclipse.ui.tests.navigator.extension.TestContentProvider;

public class ViewerTest extends NavigatorTestBase {

	public ViewerTest() {
		_navigatorInstanceId = TEST_VIEWER_INHERITED;
	}

	// Bug 218127 [CommonNavigator] Common Navigator inherited viewer bindings
	public void testInheritedViewer() throws Exception {

		IStructuredSelection sel;
		sel = new StructuredSelection(
				((IContainer) _p2.members()[1]).members()[0]);
		_viewer.setSelection(sel);

		verifyMenu(sel, "Resource Mapping");
	}

	// Bug 198971[CommonNavigator] Provide extension schema for setting help ID
	public void testHelpId() throws Exception {
		String context = (String) _viewer.getControl().getData(WorkbenchHelpSystem.HELP_KEY);
		assertEquals(TEST_VIEWER_HELP_CONTEXT, context);
	}

	// Bug 275932 [CommonNavigator] Window close throws exception in
	// ObservableCollectionTreeContentProvider
	public void testDispose() throws Exception {
		refreshViewer();
		_viewer.expandAll();

		TestContentProvider._dieOnSetInput = true;
		EditorTestHelper.showView(_navigatorInstanceId, false);

		assertFalse(TestContentProvider._diedOnSetInput);
	}

}
