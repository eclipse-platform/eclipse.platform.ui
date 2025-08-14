/*******************************************************************************
 * Copyright (c) 2000, 20019 IBM Corporation and others.
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
package org.eclipse.ui.tests.multipageeditor;

import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;

import java.io.ByteArrayInputStream;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * <p>
 * Test that the MultiPageEditorPart is acting on events and changes. These
 * tests are for making sure that selection events and page change events are
 * handled.
 * </p>
 * <p>
 * It also checks for changing Contexts.
 * </p>
 *
 * @since 3.2
 */
@RunWith(JUnit4.class)
public class MultiVariablePageTest extends UITestCase {

	private static final String FILE_CONTENTS = "#section01\nsection 1\n#section02\nsection 2\nwith info\n#section03\nLast page\n";

	private static final String MTEST01_FILE = "mtest01.multivar";

	private static final String MTEST02_FILE = "mtest02.multivar";

	private static final String MULTI_VARIABLE_PROJ = "MultiVariableTest";

	private int fPostCalled;

	public MultiVariablePageTest() {
		super(MultiVariablePageTest.class.getSimpleName());
	}

	/**
	 * Make sure that setting the active page programmatically calls
	 * pageChanged(int) on the way. This method is overridden in a lot of
	 * editors to provide their functionality on page changes.
	 */
	@Test
	public void testSetActivePage() throws Throwable {
		// Open a new test window.
		// Create and open a blurb file.
		IWorkbenchWindow window = openTestWindow();
		IEditorPart part = openMultivarFile(window, MTEST01_FILE);

		MultiVariablePageEditor editor = (MultiVariablePageEditor) part;

		editor.setPage(1);
		ISelection selection = editor.getEditorSite().getSelectionProvider()
				.getSelection();
		TextSelection text = (TextSelection) selection;
		// when we change to the second page, the selection should be
		// updated.
		assertEquals("#section02", text.getText());

		editor.setPage(0);
		selection = editor.getEditorSite().getSelectionProvider()
				.getSelection();
		text = (TextSelection) selection;
		// when we change back to the first page, the selection should be
		// updated.
		assertEquals("#section01", text.getText());
	}

	/**
	 * Make sure that removing a page that is a Control (instead of an editor)
	 * disposes of the Control immediately.
	 */
	@Test
	public void testRemovePage() throws Throwable {
		// Open a new test window.
		// Create and open a blurb file.
		IWorkbenchWindow window = openTestWindow();
		IEditorPart part = openMultivarFile(window, MTEST01_FILE);

		MultiVariablePageEditor editor = (MultiVariablePageEditor) part;
		editor.addLastPage();
		Control c = editor.getLastPage();
		assertFalse(c.isDisposed());
		editor.removeLastPage();
		assertTrue(c.isDisposed());

		c = editor.getTestControl(2);
		assertFalse(c.isDisposed());
		editor.removeLastPage();
		assertTrue(c.isDisposed());
		editor.setPage(0);
		editor.getSite().getPage().activate(editor);
	}

	/**
	 * Now the MPEP site's selection provider should by default support post
	 * selection listeners. Since the MVPE is based on Text editors, we should
	 * be getting the post selection events when we change pages.
	 *
	 * @throws Throwable
	 *             on error cases
	 */
	@Test
	public void testPostSelection() throws Throwable {
		// Open a new test window.
		IWorkbenchWindow window = openTestWindow();
		// Create and open a blurb file.
		IEditorPart part = openMultivarFile(window, MTEST01_FILE);

		MultiVariablePageEditor editor = (MultiVariablePageEditor) part;
		ISelectionProvider sp = editor.getEditorSite().getSelectionProvider();
		assertTrue(sp instanceof IPostSelectionProvider);

		IPostSelectionProvider postProvider = (IPostSelectionProvider) sp;

		fPostCalled = 0;
		ISelectionChangedListener listener = event -> fPostCalled += sp != event.getSelectionProvider() ? 1 : 0;

		try {
			postProvider.addPostSelectionChangedListener(listener);
			editor.setPage(1);
			assertEquals(1, fPostCalled);
			editor.setPage(0);
			assertEquals(2, fPostCalled);
		} finally {
			postProvider.removePostSelectionChangedListener(listener);
		}
	}

	private IEditorPart openMultivarFile(IWorkbenchWindow window,
			String filename) throws CoreException, PartInitException {
		IWorkbenchPage page = window.getActivePage();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject testProject = workspace.getRoot().getProject(
				MULTI_VARIABLE_PROJ);
		if (!testProject.exists()) {
			testProject.create(null);
		}
		testProject.open(null);
		IFile multiFile = testProject.getFile(filename);
		if (!multiFile.exists()) {
			multiFile.create(
					new ByteArrayInputStream(FILE_CONTENTS.getBytes()), true,
					null);
		}

		// I can't be bothered to use the ID, but this editor has an
		// extention registered against it.
		IEditorPart part = IDE.openEditor(page, multiFile);
		assertTrue("Should have opened our multi variable page editor",
				part instanceof MultiVariablePageEditor);
		return part;
	}

	/**
	 * Make sure that contexts are activated-deactivated by pages changes and
	 * other editors.
	 *
	 * @throws Throwable
	 *             on error
	 */
	@Test
	public void testContextActivation() throws Throwable {
		IContextService globalService = getWorkbench()
				.getService(IContextService.class);

		// Open a new test window.
		// Create and open a blurb file.
		IWorkbenchWindow window = openTestWindow();
		IEditorPart part = openMultivarFile(window, MTEST01_FILE);

		MultiVariablePageEditor editor = (MultiVariablePageEditor) part;
		checkActiveContext(globalService, ContextTextEditor.CONTEXT_ID, false);
		checkActiveContext(globalService, ContextTextEditor.TEXT_CONTEXT_ID,
				true);

		editor.setPage(1);
		checkActiveContext(globalService, ContextTextEditor.CONTEXT_ID, false);
		checkActiveContext(globalService, ContextTextEditor.TEXT_CONTEXT_ID,
				true);

		editor.setPage(2);
		checkActiveContext(globalService, ContextTextEditor.CONTEXT_ID, true);
		checkActiveContext(globalService, ContextTextEditor.TEXT_CONTEXT_ID,
				true);

		editor.setPage(1);
		checkActiveContext(globalService, ContextTextEditor.CONTEXT_ID, false);
		checkActiveContext(globalService, ContextTextEditor.TEXT_CONTEXT_ID,
				true);

		editor.setPage(2);
		checkActiveContext(globalService, ContextTextEditor.CONTEXT_ID, true);
		editor.removeLastPage();
		checkActiveContext(globalService, ContextTextEditor.CONTEXT_ID, false);
		checkActiveContext(globalService, ContextTextEditor.TEXT_CONTEXT_ID,
				true);
	}

	/**
	 * Assert if the contextId is active in the contextService.
	 */
	private void checkActiveContext(IContextService contextService,
			String contextId, boolean isActive) {
		Collection<?> activeContexts = contextService.getActiveContextIds();
		assertEquals(contextId, isActive, activeContexts.contains(contextId));
	}

	private static class PageChange implements IPageChangedListener {
		MultiPageEditorPart editor;
		Object page;

		@Override
		public void pageChanged(PageChangedEvent event) {
			editor = (MultiPageEditorPart) event.getSource();
			page = event.getSelectedPage();
		}

	}

	@Test
	public void testPageChangeListeners() throws Throwable {
		// Open a new test window.
		// Create and open a blurb file.
		IWorkbenchWindow window = openTestWindow();
		IEditorPart part = openMultivarFile(window, MTEST01_FILE);

		MultiVariablePageEditor editor = (MultiVariablePageEditor) part;
		PageChange listener = new PageChange();
		editor.addPageChangedListener(listener);

		editor.setPage(1);
		processEvents();
		assertEquals(editor, listener.editor);
		IEditorPart page = editor.getEditor(1);
		assertEquals(page, listener.page);

		editor.setPage(0);
		processEvents();
		assertEquals(editor, listener.editor);
		page = editor.getEditor(0);
		assertEquals(page, listener.page);
		Control control = editor
				.getTestControl(listener.editor.getActivePage());
		assertNotNull(control);
	}

	private void testOneEditor(IWorkbenchWindow window, IPartService partService)
			throws CoreException, PartInitException {
		IEditorPart part = openMultivarFile(window, MTEST01_FILE);
		assertNotNull(part);
		MultiVariablePageEditor editor = (MultiVariablePageEditor) part;
		PartPageListener listener = new PartPageListener();
		partService.addPartListener(listener);

		editor.setPage(1);
		processEvents();
		assertEquals(1, listener.pageChangeCount);
		assertEquals(editor, listener.currentChangeEvent.getSource());
		IEditorPart pageEditor = editor.getEditor(1);
		assertEquals(pageEditor, listener.currentChangeEvent.getSelectedPage());

		editor.setPage(0);
		processEvents();
		assertEquals(2, listener.pageChangeCount);
		assertEquals(editor, listener.currentChangeEvent.getSource());
		pageEditor = editor.getEditor(0);
		assertEquals(pageEditor, listener.currentChangeEvent.getSelectedPage());
		partService.removePartListener(listener);

		editor.setPage(1);
		processEvents();
		assertEquals(2, listener.pageChangeCount);
	}

	private void testTwoEditors(IWorkbenchWindow window,
			IPartService partService) throws CoreException, PartInitException {
		IEditorPart part = openMultivarFile(window, MTEST01_FILE);
		assertNotNull(part);
		MultiVariablePageEditor editor = (MultiVariablePageEditor) part;
		IEditorPart part2 = openMultivarFile(window, MTEST02_FILE);
		assertNotNull(part2);
		MultiVariablePageEditor editor2 = (MultiVariablePageEditor) part2;

		PartPageListener listener = new PartPageListener();
		partService.addPartListener(listener);

		editor.setPage(1);
		processEvents();
		assertEquals(1, listener.pageChangeCount);
		assertEquals(editor, listener.currentChangeEvent.getSource());
		IEditorPart pageEditor = editor.getEditor(1);
		assertEquals(pageEditor, listener.currentChangeEvent.getSelectedPage());

		editor2.setPage(2);
		assertEquals(2, listener.pageChangeCount);
		assertEquals(editor2, listener.currentChangeEvent.getSource());

		editor.setPage(0);
		processEvents();
		assertEquals(3, listener.pageChangeCount);
		assertEquals(editor, listener.currentChangeEvent.getSource());
		pageEditor = editor.getEditor(0);
		assertEquals(pageEditor, listener.currentChangeEvent.getSelectedPage());

		editor2.setPage(0);
		assertEquals(4, listener.pageChangeCount);
		assertEquals(editor2, listener.currentChangeEvent.getSource());

		partService.removePartListener(listener);

		editor.setPage(1);
		processEvents();
		assertEquals(4, listener.pageChangeCount);
	}

	@Test
	public void testPagePartListener() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IWorkbenchPage page = window.getActivePage();
		testOneEditor(window, page);
	}

	@Test
	public void testPagePartListener2() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IWorkbenchPage page = window.getActivePage();
		testTwoEditors(window, page);
	}

	@Test
	public void testPageWindowListener() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		testOneEditor(window, window.getPartService());
	}

	@Test
	public void testPageWindowListener2() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		testTwoEditors(window, window.getPartService());
	}
}
