/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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

import static org.eclipse.ui.tests.harness.util.UITestUtil.getPageInput;
import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestPage;
import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.ArrayUtil;
import org.eclipse.ui.tests.harness.util.CallHistory;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class IDeprecatedWorkbenchPageTest extends UITestCase {

	private IWorkbench fWorkbench;

	private IWorkbenchPage fActivePage;

	private IWorkbenchWindow fWin;

	private IProject proj;

	public IDeprecatedWorkbenchPageTest() {
		super(IDeprecatedWorkbenchPageTest.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		fWorkbench = PlatformUI.getWorkbench();
		fWin = openTestWindow();
		fActivePage = fWin.getActivePage();
	}

	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();
		if (proj != null) {
			FileUtil.deleteProject(proj);
			proj = null;
		}
		fWorkbench = null;
	}

	/**
	 * tests both of the following: setEditorAreaVisible() isEditorAreaVisible()
	 */
	@Test
	public void testGet_SetEditorAreaVisible() throws Throwable {
		fActivePage.setEditorAreaVisible(true);
		assertTrue(fActivePage.isEditorAreaVisible());

		fActivePage.setEditorAreaVisible(false);
		assertFalse(fActivePage.isEditorAreaVisible());
	}

	@Test
	public void testGetPerspective() throws Throwable {
		assertNotNull(fActivePage.getPerspective());

		IWorkbenchPage page = fWin.openPage(EmptyPerspective.PERSP_ID,
				getPageInput());
		assertEquals(EmptyPerspective.PERSP_ID, page.getPerspective().getId());
	}

	@Test
	public void testSetPerspective() throws Throwable {
		IPerspectiveDescriptor per = PlatformUI.getWorkbench()
				.getPerspectiveRegistry().findPerspectiveWithId(
						EmptyPerspective.PERSP_ID);
		fActivePage.setPerspective(per);
		assertEquals(per, fActivePage.getPerspective());
	}

	@Test
	public void testGetLabel() {
		assertNotNull(fActivePage.getLabel());
	}

	@Test
	public void testGetInput() throws Throwable {
		IAdaptable input = getPageInput();
		IWorkbenchPage page = fWin.openPage(input);
		assertEquals(input, page.getInput());
	}

	@Test
	public void testActivate() throws Throwable {
		MockViewPart part = (MockViewPart) fActivePage
				.showView(MockViewPart.ID);
		MockViewPart part2 = (MockViewPart) fActivePage
				.showView(MockViewPart.ID2);

		MockPartListener listener = new MockPartListener();
		fActivePage.addPartListener(listener);
		fActivePage.activate(part);

		CallHistory callTrace;

		callTrace = part2.getCallHistory();
		callTrace.clear();
		fActivePage.activate(part2);
		assertTrue(callTrace.contains("setFocus"));
		assertTrue(listener.getCallHistory().contains("partActivated"));

		callTrace = part.getCallHistory();
		callTrace.clear();
		fActivePage.activate(part);
		assertTrue(callTrace.contains("setFocus"));
		assertTrue(listener.getCallHistory().contains("partActivated"));
	}

	@Test
	public void testBringToTop() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");
		IEditorPart part = IDE.openEditor(fActivePage, FileUtil.createFile(
				"a.mock1", proj), true);
		IEditorPart part2 = IDE.openEditor(fActivePage, FileUtil.createFile(
				"b.mock1", proj), true);

		MockPartListener listener = new MockPartListener();
		fActivePage.addPartListener(listener);
		CallHistory callTrace = listener.getCallHistory();

		// at this point, part2 is active
		fActivePage.bringToTop(part);
		assertEquals(callTrace.contains("partBroughtToTop"), true);

		callTrace.clear();
		fActivePage.bringToTop(part2);
		assertEquals(callTrace.contains("partBroughtToTop"), true);
	}

	@Test
	public void testGetWorkbenchWindow() {
		/*
		 * Commented out because until test case can be updated to work with new
		 * window/page/perspective implementation
		 *
		 * assertEquals(fActivePage.getWorkbenchWindow(), fWin); IWorkbenchPage
		 * page = openTestPage(fWin); assertEquals(page.getWorkbenchWindow(),
		 * fWin);
		 */
	}

	@Test
	public void testShowView() throws Throwable {
		/*
		 * javadoc: Shows a view in this page and give it focus
		 */
		MockViewPart view = (MockViewPart) fActivePage
				.showView(MockViewPart.ID);
		assertNotNull(view);
		assertTrue(view.getCallHistory().verifyOrder(
				new String[] { "init", "createPartControl", "setFocus" }));

		fActivePage.showView(MockViewPart.ID2);

		/*
		 * javadoc: If the view is already visible, it is given focus
		 */
		CallHistory callTrace = view.getCallHistory();
		callTrace.clear();
		assertEquals(fActivePage.showView(MockViewPart.ID), view);
		assertEquals(callTrace.contains("setFocus"), true);
	}

	/**
	 * openEditor(IFile input)
	 */
	@Test
	public void testOpenEditor() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");

		/*
		 * javadoc: 1. The workbench editor registry is consulted to determine
		 * if an editor extension has been registered for the file type. If so,
		 * an instance of the editor extension is opened on the file
		 */
		IFile file = FileUtil.createFile("test.mock1", proj);
		IEditorPart editor = IDE.openEditor(fActivePage, file, true);
		assertEquals(ArrayUtil.contains(fActivePage.getEditors(), editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);
		assertEquals(editor.getSite().getId(), fWorkbench.getEditorRegistry()
				.getDefaultEditor(file.getName()).getId());

		/*
		 * javadoc: 2. Next, the native operating system will be consulted to
		 * determine if a native editor exists for the file type. If so, a new
		 * process is started and the native editor is opened on the file.
		 */
		// can not be tested
		/*
		 * javadoc: 3. If all else fails the file will be opened in a default
		 * text editor.
		 */
		file = FileUtil.createFile("a.null and void", proj);
		editor = IDE.openEditor(fActivePage, file, true);
		if (editor != null) {//Editor may be external
			assertEquals(ArrayUtil.contains(fActivePage.getEditors(), editor),
					true);
			assertEquals(fActivePage.getActiveEditor(), editor);
			assertEquals(editor.getSite().getId(),
					"org.eclipse.ui.DefaultTextEditor");

			// open another editor to take the focus away from the first editor
			IDE.openEditor(fActivePage,
					FileUtil.createFile("test.mock2", proj), true);

			/*
			 * javadoc: If this page already has an editor open on the target
			 * object that editor is activated
			 */
			// open the editor second time.
			assertEquals(editor, IDE.openEditor(fActivePage, file, true));
			assertEquals(editor, fActivePage.getActiveEditor());
		}
	}

	/**
	 * openEditor(IFile input, String editorID)
	 */
	@Test
	public void testOpenEditor2() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");
		final IFile file = FileUtil.createFile("asfasdasdf", proj);
		final String id = MockEditorPart.ID1;

		/*
		 * javadoc: The editor type is determined by mapping editorId to an
		 * editor extension registered with the workbench.
		 */
		IEditorPart editor = fActivePage.openEditor(new FileEditorInput(file),
				id);
		assertEquals(editor.getSite().getId(), id);
		assertEquals(ArrayUtil.contains(fActivePage.getEditors(), editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);

		// open another editor to take the focus away from the first editor
		IDE.openEditor(fActivePage, FileUtil.createFile("test.mock2", proj),
				true);

		/*
		 * javadoc: If this page already has an editor open on the target object
		 * that editor is activated
		 */
		// open the first editor second time.
		assertEquals(fActivePage.openEditor(new FileEditorInput(file), id),
				editor);
		assertEquals(fActivePage.getActiveEditor(), editor);
	}

	/**
	 * openEditor(IEditorInput input,String editorId)
	 */
	@Test
	public void testOpenEditor3() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");
		final String id = MockEditorPart.ID1;
		IEditorInput input = new FileEditorInput(FileUtil.createFile(
				"test.mock1", proj));

		/*
		 * javadoc: The editor type is determined by mapping editorId to an
		 * editor extension registered with the workbench
		 */
		IEditorPart editor = fActivePage.openEditor(input, id);
		assertEquals(editor.getEditorInput(), input);
		assertEquals(editor.getSite().getId(), id);
		assertEquals(ArrayUtil.contains(fActivePage.getEditors(), editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);

		// open another editor to take the focus away from the first editor
		IDE.openEditor(fActivePage, FileUtil.createFile("test.mock2", proj),
				true);

		/*
		 * javadoc: If this page already has an editor open on the target object
		 * that editor is activated
		 */
		// open the first editor second time.
		assertEquals(fActivePage.openEditor(input, id), editor);
		assertEquals(fActivePage.getActiveEditor(), editor);
	}

	/**
	 * openEditor(IEditorInput input, String editorId, boolean activate)
	 */
	@Test
	public void testOpenEditor4() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");
		final String id = MockEditorPart.ID1;
		IEditorInput input = new FileEditorInput(FileUtil.createFile(
				"test.mock1", proj));
		MockPartListener listener = new MockPartListener();
		fActivePage.addPartListener(listener);
		CallHistory callTrace = listener.getCallHistory();

		/*
		 * javadoc: The editor type is determined by mapping editorId to an
		 * editor extension registered with the workbench. javadoc: If activate ==
		 * true the editor will be activated
		 */
		// open an editor with activation
		IEditorPart editor = fActivePage.openEditor(input, id, true);
		assertEquals(editor.getEditorInput(), input);
		assertEquals(editor.getSite().getId(), id);
		assertEquals(ArrayUtil.contains(fActivePage.getEditors(), editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);
		assertEquals(callTrace.contains("partActivated"), true);

		// we need another editor so that the editor under test can receive
		// events.
		// otherwise, events will be ignored.
		IEditorPart extra = IDE.openEditor(fActivePage, FileUtil.createFile(
				"aaaaa", proj), true);

		// close the first editor after the second has opened; necessary for
		// test to work with fix to PR 7743
		fActivePage.closeEditor(editor, false);

		// Activate something in a different stack, or the editor will end up
		// activated regardless of
		// the activate flag.
		fActivePage.showView(IPageLayout.ID_PROBLEM_VIEW, null,
				IWorkbenchPage.VIEW_ACTIVATE);
		// open an editor without activation
		callTrace.clear();
		editor = fActivePage.openEditor(input, id, false);
		assertEquals(editor.getEditorInput(), input);
		assertEquals(editor.getSite().getId(), id);
		assertEquals(ArrayUtil.contains(fActivePage.getEditors(), editor), true);
		assertEquals(callTrace.contains("partActivated"), false);
		assertEquals(callTrace.contains("partBroughtToTop"), true);

		fActivePage.activate(extra);

		/*
		 * javadoc: If this page already has an editor open on the target object
		 * that editor is brought to the front
		 */
		// Activate something in a different stack, or the editor will end up
		// activated regardless of
		// the activate flag.
		fActivePage.showView(IPageLayout.ID_PROBLEM_VIEW, null,
				IWorkbenchPage.VIEW_ACTIVATE);
		// open the editor under test second time without activation
		callTrace.clear();
		assertEquals(fActivePage.openEditor(input, id, false), editor);
		assertEquals(callTrace.contains("partBroughtToTop"), true);
		assertEquals(callTrace.contains("partActivated"), false);

		// activate the other editor
		fActivePage.activate(extra);

		/*
		 * javadoc: If activate == true the editor will be activated
		 */
		// open the editor under test second time with activation
		callTrace.clear();
		assertEquals(fActivePage.openEditor(input, id, true), editor);
		assertEquals(callTrace.contains("partBroughtToTop"), true);
		assertEquals(callTrace.contains("partActivated"), true);
	}

	/**
	 * openEditor(IMarker marker)
	 */
	@Test
	public void testOpenEditor5() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");
		IMarker marker = FileUtil.createFile("aa.mock2", proj).createMarker(
				IMarker.TASK);
		CallHistory callTrace;

		/*
		 * javadoc: the cursor and selection state of the editor is then updated
		 * from information recorded in the marker.
		 */
		// open the registered editor for the marker resource
		IEditorPart editor = IDE.openEditor(fActivePage, marker, true);
		callTrace = ((MockEditorPart) editor).getCallHistory();
		assertEquals(editor.getSite().getId(), MockEditorPart.ID2);
		assertEquals(ArrayUtil.contains(fActivePage.getEditors(), editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);
		assertEquals(callTrace.contains("gotoMarker"), true);
		fActivePage.closeEditor(editor, false);

		/*
		 * javadoc: If the marker contains an EDITOR_ID_ATTR attribute the
		 * attribute value will be used to determine the editor type to be
		 * opened
		 */
		marker.setAttribute(IWorkbenchPage.EDITOR_ID_ATTR, MockEditorPart.ID1);
		editor = IDE.openEditor(fActivePage, marker, true);
		callTrace = ((MockEditorPart) editor).getCallHistory();
		assertEquals(editor.getSite().getId(), MockEditorPart.ID1);
		assertEquals(ArrayUtil.contains(fActivePage.getEditors(), editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);
		assertEquals(callTrace.contains("gotoMarker"), true);
		// do not close the editor this time

		/*
		 * javdoc: If this page already has an editor open on the target object
		 * that editor is activated
		 */
		callTrace.clear();
		assertEquals(IDE.openEditor(fActivePage, marker, true), editor);
		assertEquals(fActivePage.getActiveEditor(), editor);
		assertEquals(callTrace.contains("gotoMarker"), true);
		fActivePage.closeEditor(editor, false);
	}

	/**
	 * openEditor(IMarker marker, boolean activate)
	 */
	@Test
	public void testOpenEditor6() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");
		IMarker marker = FileUtil.createFile("aa.mock2", proj).createMarker(
				IMarker.TASK);
		MockPartListener listener = new MockPartListener();
		fActivePage.addPartListener(listener);
		CallHistory listenerCall = listener.getCallHistory();
		CallHistory editorCall;

		// we need another editor so that the editor under test can receive
		// events.
		// otherwise, events will be ignored.
		IEditorPart extra = IDE.openEditor(fActivePage, FileUtil.createFile(
				"aaaaa", proj), true);

		/*
		 * javadoc: If activate == true the editor will be activated
		 */
		// open the registered editor for the marker resource with activation
		IEditorPart editor = IDE.openEditor(fActivePage, marker, true);
		editorCall = ((MockEditorPart) editor).getCallHistory();
		assertEquals(editor.getSite().getId(), MockEditorPart.ID2);
		assertEquals(ArrayUtil.contains(fActivePage.getEditors(), editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);

		/*
		 * javadoc: the cursor and selection state of the editor is then updated
		 * from information recorded in the marker.
		 */
		assertEquals(editorCall.contains("gotoMarker"), true);
		fActivePage.closeEditor(editor, false);

		fActivePage.activate(extra);

		// Activate something in a different stack, or the editor will end up
		// activated regardless of
		// the activate flag.
		fActivePage.showView(IPageLayout.ID_PROBLEM_VIEW, null,
				IWorkbenchPage.VIEW_ACTIVATE);

		// open the registered editor for the marker resource without activation
		listenerCall.clear();
		editor = IDE.openEditor(fActivePage, marker, false);
		editorCall = ((MockEditorPart) editor).getCallHistory();
		assertEquals(editor.getSite().getId(), MockEditorPart.ID2);
		assertEquals(ArrayUtil.contains(fActivePage.getEditors(), editor), true);
		assertEquals(listenerCall.contains("partBroughtToTop"), true);
		assertEquals(listenerCall.contains("partActivated"), false);
		assertEquals(editorCall.contains("gotoMarker"), true);
		fActivePage.closeEditor(editor, false);

		/*
		 * javadoc: If the marker contains an EDITOR_ID_ATTR attribute the
		 * attribute value will be used to determine the editor type to be
		 * opened
		 */
		String id = MockEditorPart.ID1;
		marker.setAttribute(IWorkbenchPage.EDITOR_ID_ATTR, id);

		// open an editor with activation
		listenerCall.clear();

		editor = IDE.openEditor(fActivePage, marker, true);
		editorCall = ((MockEditorPart) editor).getCallHistory();
		assertEquals(editor.getSite().getId(), id);
		assertEquals(ArrayUtil.contains(fActivePage.getEditors(), editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);
		assertEquals(editorCall.contains("gotoMarker"), true);
		fActivePage.closeEditor(editor, false);

		fActivePage.activate(extra);

		// Activate something in a different stack, or the editor will end up
		// activated regardless of
		// the activate flag.
		fActivePage.showView(IPageLayout.ID_PROBLEM_VIEW, null,
				IWorkbenchPage.VIEW_ACTIVATE);

		// open an editor without activation
		listenerCall.clear();
		editor = IDE.openEditor(fActivePage, marker, false);
		editorCall = ((MockEditorPart) editor).getCallHistory();
		assertEquals(editor.getSite().getId(), id);
		assertEquals(ArrayUtil.contains(fActivePage.getEditors(), editor), true);
		assertEquals(editorCall.contains("gotoMarker"), true);
		assertEquals(listenerCall.contains("partActivated"), false);
		assertEquals(listenerCall.contains("partBroughtToTop"), true);
		// do not close the editor this time

		fActivePage.activate(extra);

		/*
		 * javadoc: If this page already has an editor open on the target object
		 * that editor is brought to front
		 */
		// Activate something in a different stack, or the editor will end up
		// activated regardless of
		// the activate flag.
		fActivePage.showView(IPageLayout.ID_PROBLEM_VIEW, null,
				IWorkbenchPage.VIEW_ACTIVATE);
		// open the editor second time without activation
		listenerCall.clear();
		assertEquals(IDE.openEditor(fActivePage, marker, false), editor);
		assertEquals(listenerCall.contains("partBroughtToTop"), true);
		assertEquals(listenerCall.contains("partActivated"), false);

		fActivePage.activate(extra);

		/*
		 * javdoc: If activate == true the editor will be activated
		 */
		// open the editor second time with activation
		listenerCall.clear();
		assertEquals(IDE.openEditor(fActivePage, marker, true), editor);
		assertEquals(editorCall.contains("gotoMarker"), true);
		assertEquals(listenerCall.contains("partBroughtToTop"), true);
		assertEquals(listenerCall.contains("partActivated"), true);
	}

	@Test
	public void testFindView() throws Throwable {
		String id = MockViewPart.ID3;
		// id of valid, but not open view
		assertNull(fActivePage.findView(id));

		IViewPart view = fActivePage.showView(id);
		assertEquals(fActivePage.findView(id), view);

		// close view
		fActivePage.hideView(view);
		assertNull(fActivePage.findView(id));
	}

	@Test
	public void testGetViews() throws Throwable {
		int totalBefore = fActivePage.getViews().length;

		IViewPart view = fActivePage.showView(MockViewPart.ID2);
		assertEquals(ArrayUtil.contains(fActivePage.getViews(), view), true);
		assertEquals(fActivePage.getViews().length, totalBefore + 1);

		fActivePage.hideView(view);
		assertEquals(ArrayUtil.contains(fActivePage.getViews(), view), false);
		assertEquals(fActivePage.getViews().length, totalBefore);
	}

	@Test
	public void testHideView() throws Throwable {
		IViewPart view = fActivePage.showView(MockViewPart.ID3);

		fActivePage.hideView(view);
		CallHistory callTrace = ((MockViewPart) view).getCallHistory();
		assertTrue(callTrace.contains("dispose"));
	}

	@Test
	@Ignore
	public void XXXtestClose() throws Throwable {
		IWorkbenchPage page = openTestPage(fWin);

		proj = FileUtil.createProject("testOpenEditor");
		final IFile file = FileUtil.createFile("aaa.mock1", proj);
		IEditorPart editor = IDE.openEditor(page, file, true);
		CallHistory callTrace = ((MockEditorPart) editor).getCallHistory();
		callTrace.clear();

		/*
		 * javadoc: If the page has open editors with unsaved content and save
		 * is true, the user will be given the opportunity to save them
		 */
		assertEquals(page.close(), true);
		assertEquals(callTrace
				.verifyOrder(new String[] { "isDirty", "dispose" }), true);
		assertEquals(fWin.getActivePage(), fActivePage);
	}

	@Test
	public void testCloseEditor() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");
		final IFile file = FileUtil.createFile("test.mock1", proj);
		IEditorPart editor;
		CallHistory callTrace;
		MockEditorPart mock;

		/*
		 * javadoc: Parameters: save - true to save the editor contents if
		 * required (recommended)
		 */
		// create a clean editor that needs to be saved on closing
		editor = IDE.openEditor(fActivePage, file, true);
		mock = (MockEditorPart) editor;
		mock.setSaveNeeded(true);
		callTrace = mock.getCallHistory();
		callTrace.clear();
		// close the editor with save confirmation
		assertEquals(fActivePage.closeEditor(editor, true), true);
		assertEquals(callTrace
				.verifyOrder(new String[] { "isDirty", "dispose" }), true);

		/*
		 * javadoc: If the editor has unsaved content and save is true, the user
		 * will be given the opportunity to save it.
		 */
		// can't be tested
		/*
		 * javadoc: Parameters: save - false to discard any unsaved changes
		 */
		// create a dirty editor
		editor = IDE.openEditor(fActivePage, file, true);
		mock = (MockEditorPart) editor;
		mock.setDirty(true);
		mock.setSaveNeeded(true);
		callTrace = mock.getCallHistory();
		callTrace.clear();
		// close the editor and discard changes
		assertEquals(fActivePage.closeEditor(editor, false), true);
		assertEquals(callTrace.contains("isSaveOnCloseNeeded"), false);
		/*
		 * It is possible that some action may query the isDirty value of the
		 * editor to update its enabled state. There is nothing wrong in doing
		 * that, so do not test for no isDirty call here.
		 *
		 * assertEquals(callTrace.contains( "isDirty"), false);
		 */
		assertEquals(callTrace.contains("doSave"), false);
		assertEquals(callTrace.contains("dispose"), true);
	}

	@Test
	public void testCloseAllEditors() throws Throwable {
		int total = 5;
		final IFile[] files = new IFile[total];
		IEditorPart[] editors = new IEditorPart[total];
		CallHistory[] callTraces = new CallHistory[total];
		MockEditorPart[] mocks = new MockEditorPart[total];

		proj = FileUtil.createProject("testOpenEditor");
		for (int i = 0; i < total; i++) {
			files[i] = FileUtil.createFile(i + ".mock2", proj);
		}

		/*
		 * javadoc: If the page has open editors with unsaved content and save
		 * is true, the user will be given the opportunity to save them.
		 */
		// close all clean editors with confirmation
		for (int i = 0; i < total; i++) {
			editors[i] = IDE.openEditor(fActivePage, files[i], true);
			callTraces[i] = ((MockEditorPart) editors[i]).getCallHistory();
		}
		assertEquals(fActivePage.closeAllEditors(true), true);
		for (int i = 0; i < total; i++) {
			assertEquals(callTraces[i].contains("isDirty"), true);
			assertEquals(callTraces[i].contains("doSave"), false);
			callTraces[i].clear();
		}

		// close all dirty editors with confirmation
		// can't be tested

		// close all dirty editors discarding them
		for (int i = 0; i < total; i++) {
			editors[i] = IDE.openEditor(fActivePage, files[i], true);
			mocks[i] = (MockEditorPart) editors[i];
			mocks[i].setDirty(true);
			callTraces[i] = mocks[i].getCallHistory();
		}
		assertEquals(fActivePage.closeAllEditors(false), true);
		for (int i = 0; i < total; i++) {
			assertEquals(callTraces[i].contains("doSave"), false);
		}
	}

	@Test
	public void testSaveEditor() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");
		final IFile file = FileUtil.createFile("test.mock1", proj);
		IEditorPart editor;
		CallHistory callTrace;
		MockEditorPart mock;

		// create a clean editor
		editor = IDE.openEditor(fActivePage, file, true);
		mock = (MockEditorPart) editor;
		callTrace = mock.getCallHistory();
		callTrace.clear();

		/*
		 * javadoc: Saves the contents of the given editor if dirty. If not,
		 * this method returns without effect
		 */
		// save the clean editor with confirmation
		assertEquals(fActivePage.saveEditor(editor, true), true);
		assertEquals(callTrace.contains("isDirty"), true);
		assertEquals(callTrace.contains("doSave"), false);

		/*
		 * javadoc: If confirm is true the user is prompted to confirm the
		 * command.
		 */
		// can't be tested
		/*
		 * javadoc: Otherwise, the save happens without prompt.
		 */
		// save the clean editor without confirmation
		assertEquals(fActivePage.saveEditor(editor, false), true);
		assertEquals(callTrace.contains("isDirty"), true);
		assertEquals(callTrace.contains("doSave"), false);

		// save the dirty editor without confirmation
		mock.setDirty(true);
		callTrace.clear();
		assertEquals(fActivePage.saveEditor(editor, false), true);
		assertEquals(callTrace
				.verifyOrder(new String[] { "isDirty", "doSave" }), true);
	}

	@Test
	@Ignore
	public void XXXtestSaveAllEditors() throws Throwable {
		int total = 3;

		final IFile[] files = new IFile[total];
		IEditorPart[] editors = new IEditorPart[total];
		CallHistory[] callTraces = new CallHistory[total];
		MockEditorPart[] mocks = new MockEditorPart[total];

		proj = FileUtil.createProject("testOpenEditor");
		for (int i = 0; i < total; i++) {
			files[i] = FileUtil.createFile(i + ".mock2", proj);
			editors[i] = IDE.openEditor(fActivePage, files[i], true);
			mocks[i] = (MockEditorPart) editors[i];
			callTraces[i] = mocks[i].getCallHistory();
		}

		/*
		 * javadoc: If there are no dirty editors this method returns without
		 * effect. javadoc: If confirm is true the user is prompted to confirm
		 * the command
		 */
		// save all clean editors with confirmation
		assertEquals(fActivePage.saveAllEditors(true), true);
		for (int i = 0; i < total; i++) {
			assertEquals(callTraces[i].contains("isDirty"), true);
			assertEquals(callTraces[i].contains("doSave"), false);
			callTraces[i].clear();
		}

		// save all dirty editors with confirmation can't be tested

		/*
		 * javadoc: Parameters: confirm - false to save unsaved changes without
		 * asking
		 */
		// save all clean editors without confirmation
		assertEquals(fActivePage.saveAllEditors(false), true);
		for (int i = 0; i < total; i++) {
			assertEquals(callTraces[i].contains("isDirty"), true);
			assertEquals(callTraces[i].contains("doSave"), false);
			callTraces[i].clear();
		}

		// save all dirty editors without confirmation
		for (int i = 0; i < total; i++) {
			mocks[i].setDirty(true);
		}
		assertEquals(fActivePage.saveAllEditors(false), true);
		for (int i = 0; i < total; i++) {
			assertEquals(callTraces[i].verifyOrder(new String[] { "isDirty",
					"doSave" }), true);
		}
	}

	@Test
	public void testGetEditors() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");
		int totalBefore = fActivePage.getEditors().length;
		int num = 3;
		IEditorPart[] editors = new IEditorPart[num];

		for (int i = 0; i < num; i++) {
			editors[i] = IDE.openEditor(fActivePage, FileUtil.createFile(i
					+ ".mock2", proj), true);
			assertEquals(ArrayUtil.contains(fActivePage.getEditors(),
					editors[i]), true);
		}
		assertEquals(fActivePage.getEditors().length, totalBefore + num);

		fActivePage.closeEditor(editors[0], false);
		assertEquals(ArrayUtil.contains(fActivePage.getEditors(), editors[0]),
				false);
		assertEquals(fActivePage.getEditors().length, totalBefore + num - 1);

		fActivePage.closeAllEditors(false);
		assertEquals(fActivePage.getEditors().length, 0);
	}

	@Test
	@Ignore
	public void XXXtestShowActionSet() {
		String id = MockActionDelegate.ACTION_SET_ID;

//		int totalBefore = facade.getActionSetCount(fActivePage);
		// FIXME: No implementation for facade.getActionSetCount()

		fActivePage.showActionSet(id);

//		facade.assertActionSetId(fActivePage, id, true);

		// check that the method does not add an invalid action set to itself
		id = IConstants.FakeID;
		fActivePage.showActionSet(id);

//		facade.assertActionSetId(fActivePage, id, false);
		// FIXME: No implementation for facade.assertActionSetId()

//		assertEquals(facade.getActionSetCount(fActivePage), totalBefore + 1);
	}

	@Test
	@Ignore
	public void XXXtestHideActionSet() {
//		int totalBefore = facade.getActionSetCount(fActivePage);
		// FIXME: No implementation for facade.getActionSetCount()

		String id = MockWorkbenchWindowActionDelegate.SET_ID;
		fActivePage.showActionSet(id);
//		assertEquals(facade.getActionSetCount(fActivePage), totalBefore + 1);

		fActivePage.hideActionSet(id);
//		assertEquals(facade.getActionSetCount(fActivePage), totalBefore);

//		facade.assertActionSetId(fActivePage, id, false);
		// FIXME: No implementation for facade.assertActionSetId()
	}
}
