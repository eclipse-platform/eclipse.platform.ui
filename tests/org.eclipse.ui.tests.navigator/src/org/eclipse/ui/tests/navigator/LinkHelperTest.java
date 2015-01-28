/*******************************************************************************
 * Copyright (c) 2010, 2015 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 457870
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.tests.navigator.extension.TestLinkHelper;
import org.junit.Test;

public class LinkHelperTest extends NavigatorTestBase {

	private static final int SLEEP_TIME = 800;
	private static final boolean SLEEP_LONG = false;

	public LinkHelperTest() {
		_navigatorInstanceId = TEST_VIEWER_LINK_HELPER;
	}


	@Test
	public void testLinkHelperSelectionChange() throws Exception {

		System.out.println("SelectionChange start");
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage();

		IDE.openEditor(activePage, _p2.getFile("file1.txt"));
		IDE.openEditor(activePage, _p2.getFile("file2.txt"));

		_commonNavigator.setLinkingEnabled(true);

		_viewer.setSelection(new StructuredSelection(_p2.getFile("file1.txt"))); //$NON-NLS-1$
		DisplayHelper.sleep(SLEEP_TIME);

		_commonNavigator.getViewSite().getPage().activate(_commonNavigator);

		System.out.println("before set 2");
		_viewer.setSelection(new StructuredSelection(_p2.getFile("file2.txt"))); //$NON-NLS-1$
		DisplayHelper.sleep(SLEEP_TIME);

		activePage.activate(_commonNavigator);

		TestLinkHelper.instance.resetTest();

		System.out.println("before set 3");
		_viewer.setSelection(new StructuredSelection(_p2.getFile("file1.txt"))); //$NON-NLS-1$
		DisplayHelper.sleep(SLEEP_TIME);

		System.out.println("SelectionChange Done: " + TestLinkHelper.instance);
		assertEquals(0, TestLinkHelper.instance.findSelectionCount);
		assertEquals(1, TestLinkHelper.instance.activateEditorCount);

		if (SLEEP_LONG)
			DisplayHelper.sleep(100000000);
	}

	@Test
	public void testLinkHelperEditorActivation() throws Exception {
		System.out.println("EditorActivation start");

		_commonNavigator.setLinkingEnabled(false);
		DisplayHelper.sleep(SLEEP_TIME);

		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage();

		IEditorPart file1Editor = IDE.openEditor(activePage, _p2.getFile("file1.txt"));
		IEditorPart file2Editor = IDE.openEditor(activePage, _p2.getFile("file2.txt"));

		_commonNavigator.setLinkingEnabled(true);
		DisplayHelper.sleep(SLEEP_TIME);

		TestLinkHelper.instance.resetTest();

		System.out.println("before activate 1");
		activePage.activate(file1Editor);
		DisplayHelper.sleep(SLEEP_TIME);
		System.out.println("EditorActivation 1 Done: " + TestLinkHelper.instance);
		assertEquals(1, TestLinkHelper.instance.findSelectionCount);
		assertEquals(0, TestLinkHelper.instance.activateEditorCount);

		TestLinkHelper.instance.resetTest();

		System.out.println("before activate 2");
		activePage.activate(file2Editor);
		DisplayHelper.sleep(SLEEP_TIME);
		System.out.println("EditorActivation 2 Done: " + TestLinkHelper.instance);
		assertEquals(1, TestLinkHelper.instance.findSelectionCount);
		assertEquals(0, TestLinkHelper.instance.activateEditorCount);


	}

}
