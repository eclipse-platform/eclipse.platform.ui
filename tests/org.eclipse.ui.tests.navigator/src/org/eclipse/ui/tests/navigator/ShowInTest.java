/*******************************************************************************
 * Copyright (c) 2010, 2018 Dawid Pakuła <zulus@w3des.net> and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dawid Pakuła <zulus@w3des.net> - Bug 536785
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.Test;

public class ShowInTest extends NavigatorTestBase {

	private static final int SLEEP_TIME = 800;

	public ShowInTest() {
		_navigatorInstanceId = TEST_VIEWER_SHOW_IN;
	}

	@Test
	public void testShowIn() throws Exception {

		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		IEditorPart editor1 = IDE.openEditor(activePage, _p2.getFile("file1.txt"));
		IEditorPart editor2 = IDE.openEditor(activePage, _p2.getFile("file2.txt"));

		_commonNavigator.setLinkingEnabled(false);
		_commonNavigator.show(new ShowInContext(editor1.getEditorInput(), null));

		DisplayHelper.sleep(SLEEP_TIME);

		_commonNavigator.getViewSite().getPage().activate(_commonNavigator);
		IStructuredSelection selection = (IStructuredSelection) _commonNavigator.getCommonViewer().getSelection();
		assertEquals(_p2.getFile("file1.txt"), selection.getFirstElement());

		_commonNavigator.show(new ShowInContext(editor2.getEditorInput(), new StructuredSelection(new Date())));
		DisplayHelper.sleep(SLEEP_TIME);

		activePage.activate(_commonNavigator);

		selection = (IStructuredSelection) _commonNavigator.getCommonViewer().getSelection();
		assertEquals(_p2.getFile("file2.txt"), selection.getFirstElement());
	}

}
