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

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.CallHistory;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * This is a test for IEditorPart. Since IEditorPart is an interface this test
 * verifies the IEditorPart lifecycle rather than the implementation.
 */
@RunWith(JUnit4.class)
public class IEditorPartTest extends IWorkbenchPartTest {

	/**
	 * Constructor for IEditorPartTest
	 */
	public IEditorPartTest() {
		super(IEditorPartTest.class.getSimpleName());
	}

	/**
	 * @see IWorkbenchPartTest#openPart(IWorkbenchPage)
	 */
	@Override
	protected MockPart openPart(IWorkbenchPage page) throws Throwable {
		IProject proj = FileUtil.createProject("IEditorPartTest");
		IFile file = FileUtil.createFile("IEditorPartTest.txt", proj);
		return (MockWorkbenchPart) page.openEditor(new FileEditorInput(file),
				MockEditorPart.ID1);
	}

	/**
	 * @see IWorkbenchPartTest#closePart(IWorkbenchPage, MockWorkbenchPart)
	 */
	@Override
	protected void closePart(IWorkbenchPage page, MockPart part)
			throws Throwable {
		page.closeEditor((IEditorPart) part, true);
	}

	/**
	 * Tests that the editor is closed without saving if isSaveOnCloseNeeded()
	 * returns false.
	 *
	 * @see ISaveablePart#isSaveOnCloseNeeded()
	 */
	@Test
	public void testOpenAndCloseSaveNotNeeded() throws Throwable {
		// Open a part.
		MockEditorPart part = (MockEditorPart) openPart(fPage);
		part.setDirty(true);
		part.setSaveNeeded(false);
		closePart(fPage, part);

		CallHistory history = part.getCallHistory();
		assertTrue(history.verifyOrder(new String[] { "setInitializationData",
				"init", "createPartControl", "setFocus", "isSaveOnCloseNeeded",
				"widgetDisposed", "dispose" }));
		assertFalse(history.contains("doSave"));
	}

	@Test
	public void testOpenAndCloseWithNoMemento() throws Throwable {
		IProject proj = FileUtil.createProject("IEditorPartTest");
		IFile file = FileUtil.createFile("IEditorPartTest.txt", proj);
		MockEditorWithState editor = (MockEditorWithState) fPage.openEditor(
				new FileEditorInput(file), MockEditorWithState.ID);
		closePart(fPage, editor);

		CallHistory history = editor.getCallHistory();
		assertFalse(history.contains("saveState"));
		assertFalse(history.contains("restoreState"));
	}

	@Test
	public void testGetShellAfterClose() throws Throwable {
		IProject proj = FileUtil.createProject("IEditorPartTest");
		IFile file = FileUtil.createFile("IEditorPartTest.txt", proj);
		MockEditorWithState editor = (MockEditorWithState) fPage.openEditor(new FileEditorInput(file),
				MockEditorWithState.ID);

		assertNotNull(editor.getSite().getShell());

		closePart(fPage, editor);
		processEvents();

		Map<String, List<IStatus>> errors = new LinkedHashMap<>();
		ILogListener listener = (status, plugin) -> {
			List<IStatus> list = errors.get(plugin);
			if (list == null) {
				list = new ArrayList<>();
				errors.put(status.getPlugin(), list);
			}
			list.add(status);
		};

		try {
			Platform.addLogListener(listener);

			// Will dispose the PartSite after close()
			closePart(fPage, editor);
			processEvents();

			// Should log error
			editor.getSite().getShell();
		} finally {
			Platform.removeLogListener(listener);
		}

		List<IStatus> list = errors.get("org.eclipse.ui.workbench");
		assertFalse("No error reported on accessing shell after part disposal", list == null || list.isEmpty());
		assertEquals(1, list.size());
		Throwable ex = list.get(0).getException();
		assertTrue("Unexpected exception: " + ex, ex instanceof IllegalStateException);
		assertTrue("Unexpected exception message: " + ex.getMessage(),
				ex.getMessage().contains("IWorkbenchSite.getShell() was called after part disposal"));
	}

}
