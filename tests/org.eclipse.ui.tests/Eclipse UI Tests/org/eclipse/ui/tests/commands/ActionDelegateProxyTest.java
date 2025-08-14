/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.commands;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.api.workbenchpart.MenuContributionHarness;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@Ignore("broke during e4 transition and still need adjustments")
public class ActionDelegateProxyTest {

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	private static final String DELEGATE_ACTION_SET_ID = "org.eclipse.ui.tests.delegateActionSet";
	private static final String INC_COMMAND = "org.eclipse.ui.tests.incMenuHarness";
	private static final String VIEW_ID = "org.eclipse.ui.tests.api.MenuTestHarness";

	private static final String GO_COMMAND = "org.eclipse.ui.tests.simplyGo";
	private static final String STAY_COMMAND = "org.eclipse.ui.tests.simplyStay";

	@Test
	public void testViewDelegate() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IWorkbenchPage page = window.getActivePage();
		assertNull(page.findView(VIEW_ID));
		IViewPart view = page.showView(VIEW_ID);
		assertNotNull(view);
		assertTrue(view instanceof MenuContributionHarness);
		MenuContributionHarness mch = (MenuContributionHarness) view;
		assertEquals(0, mch.getCount());
		IHandlerService service = window.getService(IHandlerService.class);
		service.executeCommand(INC_COMMAND, null);
		assertEquals(1, mch.getCount());
		service.executeCommand(INC_COMMAND, null);
		assertEquals(2, mch.getCount());

		page.hideView(view);
		IViewPart view2 = page.showView(VIEW_ID);
		assertFalse(view==view2);
		view = view2;
		assertNotNull(view);
		assertTrue(view instanceof MenuContributionHarness);
		mch = (MenuContributionHarness) view;
		assertEquals(0, mch.getCount());
		service.executeCommand(INC_COMMAND, null);
		assertEquals(1, mch.getCount());
		service.executeCommand(INC_COMMAND, null);
		assertEquals(2, mch.getCount());
	}

	@Test
	public void testWWActionDelegate() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		window.getActivePage().showActionSet(DELEGATE_ACTION_SET_ID);
		IHandlerService service = window.getService(IHandlerService.class);
		assertFalse(SimplyGoActionDelegate.executed);
		service.executeCommand(GO_COMMAND, null);
		assertTrue(SimplyGoActionDelegate.executed);
	}

	private static final String contents = "one\ntwo\nthree\n";

	@Test
	public void testEditorActionDelegate() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		window.getActivePage().closeAllEditors(false);
		IHandlerService service = window.getService(IHandlerService.class);
		assertFalse(EditorActionDelegate.executed);
		EditorActionDelegate.part = null;
		assertThrows(NotHandledException.class, () -> service.executeCommand(STAY_COMMAND, null));
		assertFalse(EditorActionDelegate.executed);
		assertNull(EditorActionDelegate.part);

		IProject proj = FileUtil.createProject(GO_COMMAND);
		IFile file = FileUtil.createFile("test.txt", proj);
		InputStream in = new ByteArrayInputStream(contents.getBytes());
		file.setContents(in, true, false, new NullProgressMonitor());
		IEditorPart editor1 = IDE.openEditor(window.getActivePage(), file);
		assertNotNull(editor1);
		assertEquals("org.eclipse.ui.DefaultTextEditor", editor1.getSite().getId());

		file = FileUtil.createFile("test2.txt", proj);
		in = new ByteArrayInputStream(contents.getBytes());
		file.setContents(in, true, false, new NullProgressMonitor());
		IEditorPart editor2 = IDE.openEditor(window.getActivePage(), file);
		assertNotNull(editor2);
		assertEquals("org.eclipse.ui.DefaultTextEditor", editor2.getSite().getId());

		service.executeCommand(STAY_COMMAND, null);
		assertTrue(EditorActionDelegate.executed);
		assertEquals(editor2, EditorActionDelegate.part);

		window.getActivePage().activate(editor1);
		processEvents();
		service.executeCommand(STAY_COMMAND, null);
		assertTrue(EditorActionDelegate.executed);
		assertEquals(editor1, EditorActionDelegate.part);

	}
}
