/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Christian Janz  - <christian.janz@gmail.com> Fix for Bug 385592
 *     Denis Zygann <d.zygann@web.de> - Bug 457390
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.eclipse.ui.tests.harness.util.UITestUtil.forceActive;
import static org.eclipse.ui.tests.harness.util.UITestUtil.getPageInput;
import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestPage;
import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.eclipse.ui.tests.harness.util.UITestUtil.waitForJobs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.tests.harness.util.CallHistory;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.junit.Assume;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IWorkbenchPageTest extends UITestCase {

	private IWorkbench fWorkbench;

	private IWorkbenchPage fActivePage;

	private IWorkbenchWindow fWin;

	private IProject proj;

	private int logCount;
	private IStatus logStatus;
	String getMessage() {
		return logStatus==null?"No message":logStatus.getMessage();
	}

	ILogListener openAndHideListener = (status, plugin) -> {
		logStatus = status;
		logCount++;
	};


	private int partHiddenCount = 0;
	private IWorkbenchPartReference partHiddenRef = null;
	private int partVisibleCount = 0;
	private IWorkbenchPartReference partVisibleRef = null;
	private int partActiveCount = 0;
	private IWorkbenchPartReference partActiveRef = null;
	IPartListener2 partListener2 = new IPartListener2() {
		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			partActiveCount++;
			partActiveRef = partRef;
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {
			partHiddenCount++;
			partHiddenRef = partRef;
		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
			partVisibleCount++;
			partVisibleRef = partRef;
		}
	};

	public IWorkbenchPageTest() {
		super(IWorkbenchPageTest.class.getName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		fWorkbench = PlatformUI.getWorkbench();
		fWin = openTestWindow();
		fActivePage = fWin.getActivePage();
		logStatus = null;
		logCount = 0;
		Platform.addLogListener(openAndHideListener);
	}

	@Override
	protected void doTearDown() throws Exception {
		Platform.removeLogListener(openAndHideListener);
		super.doTearDown();
		if (proj != null) {
			FileUtil.deleteProject(proj);
			proj = null;
		}
		fWorkbench = null;
	}

	/**
	 * Tests the new working set API.
	 *
	 * @since 3.2
	 */
	@Test
	public void testWorkingSets1() {
		IWorkbenchPage page = fActivePage;
		IWorkingSet[] sets = page.getWorkingSets();
		assertNotNull(sets);
		assertEquals(0, sets.length);

		IWorkingSetManager manager = page.getWorkbenchWindow().getWorkbench()
				.getWorkingSetManager();

		IWorkingSet set1 = null, set2 = null;
		try {
			set1 = manager.createWorkingSet("w1", new IAdaptable[0]);
			manager.addWorkingSet(set1);
			set2 = manager.createWorkingSet("w2", new IAdaptable[0]);
			manager.addWorkingSet(set2);

			page.setWorkingSets(new IWorkingSet[] { set1 });
			sets = page.getWorkingSets();

			assertNotNull(sets);
			assertEquals(1, sets.length);
			assertEquals(set1, sets[0]);

			page.setWorkingSets(new IWorkingSet[0]);
			sets = page.getWorkingSets();
			assertNotNull(sets);
			assertEquals(0, sets.length);

			page.setWorkingSets(new IWorkingSet[] { set1, set2 });
			sets = page.getWorkingSets();

			assertNotNull(sets);
			assertEquals(2, sets.length);
			Set<IWorkingSet> realSet = new HashSet<>(Arrays.asList(sets));
			assertTrue(realSet.contains(set1));
			assertTrue(realSet.contains(set2));

			page.setWorkingSets(new IWorkingSet[0]);
			sets = page.getWorkingSets();
			assertNotNull(sets);
			assertEquals(0, sets.length);
		} finally {
			if (set1 != null) {
				manager.removeWorkingSet(set1);
			}
			if (set2 != null) {
				manager.removeWorkingSet(set2);
			}
		}
	}

	/**
	 * Tests the new working set API.
	 *
	 * @since 3.2
	 */
	@Test
	public void testWorkingSets2() {
		fActivePage.setWorkingSets(null);
		IWorkingSet[] sets = fActivePage.getWorkingSets();
		assertNotNull(sets);
		assertEquals(0, sets.length);
	}

	/**
	 * Tests the working set listeners.
	 *
	 * @since 3.2
	 */
	@Test
	public void testWorkingSets3() {
		IWorkingSetManager manager = fActivePage.getWorkbenchWindow()
				.getWorkbench().getWorkingSetManager();

		IWorkingSet set1 = null;
		final IWorkingSet[][] sets = new IWorkingSet[1][];
		sets[0] = new IWorkingSet[0];
		IPropertyChangeListener listener = event -> {
			IWorkingSet[] oldSets = (IWorkingSet[]) event.getOldValue();
			assertTrue(Arrays.equals(sets[0], oldSets));
			sets[0] = (IWorkingSet[]) event.getNewValue();
		};
		try {
			set1 = manager.createWorkingSet("w1", new IAdaptable[0]);
			manager.addWorkingSet(set1);

			fActivePage.addPropertyChangeListener(listener);

			fActivePage.setWorkingSets(new IWorkingSet[] { set1 });
			fActivePage.setWorkingSets(new IWorkingSet[] {});
			fActivePage.setWorkingSets(new IWorkingSet[] { set1 });

			sets[0] = fActivePage.getWorkingSets();

			assertNotNull(sets[0]);
			assertEquals(1, sets[0].length);
			assertEquals(set1, sets[0][0]);

		} finally {
			fActivePage.removePropertyChangeListener(listener);
			if (set1 != null) {
				manager.removeWorkingSet(set1);
			}
		}
	}

	/**
	 * Test if the WorkingSet related settings are persisted across sessions.
	 */
	@Test
	public void testWorkingSetsPersisted_Bug385592() {
		IWorkingSetManager manager = fActivePage.getWorkbenchWindow()
				.getWorkbench().getWorkingSetManager();

		// get initial state and save it
		IWorkingSet[] workingSetsBeforeSave = fActivePage.getWorkingSets();
		String aggrWorkingSetIdBeforeSave = fActivePage
				.getAggregateWorkingSet().getName();
		((WorkbenchPage) fActivePage).saveWorkingSets();
		assertNotNull(workingSetsBeforeSave);
		assertNotNull(aggrWorkingSetIdBeforeSave);
		assertEquals(0, workingSetsBeforeSave.length);

		IWorkingSet set1 = null;
		try {
			set1 = manager.createWorkingSet("w1", new IAdaptable[0]);
			manager.addWorkingSet(set1);

			// change the working sets
			fActivePage.setWorkingSets(new IWorkingSet[] { set1 });
			assertNotNull(fActivePage.getWorkingSets());
			assertEquals(1, fActivePage.getWorkingSets().length);

			// restore the previous state
			((WorkbenchPage) fActivePage).restoreWorkingSets();
			assertEquals(aggrWorkingSetIdBeforeSave, fActivePage
					.getAggregateWorkingSet().getName());
			assertNotNull(fActivePage.getWorkingSets());
			assertEquals(workingSetsBeforeSave.length,
					fActivePage.getWorkingSets().length);

			// change again, save and restore the settings
			fActivePage.setWorkingSets(new IWorkingSet[] { set1 });
			((WorkbenchPage) fActivePage).saveWorkingSets();
			((WorkbenchPage) fActivePage).restoreWorkingSets();
			assertEquals(aggrWorkingSetIdBeforeSave, fActivePage
					.getAggregateWorkingSet().getName());
			assertEquals(1, fActivePage.getWorkingSets().length);
		} finally {
			if (set1 != null) {
				manager.removeWorkingSet(set1);
			}
		}
	}

	/**
	 * Test the VIEW_VISIBLE parameter for showView, opening the view in the
	 * stack that does not contain the active view. Ensures that the created
	 * view is not the active part but is the top part in its stack.
	 */
	@Test
	public void testView_VISIBLE2() throws PartInitException {
		fActivePage.setPerspective(WorkbenchPlugin.getDefault()
				.getPerspectiveRegistry().findPerspectiveWithId(
						ViewPerspective.ID));

		// create a part to be active
		IViewPart activePart = fActivePage.showView(MockViewPart.ID3);

		IViewPart createdPart = fActivePage.showView(MockViewPart.ID2, null,
				IWorkbenchPage.VIEW_VISIBLE);

		IViewPart[] stack = fActivePage.getViewStack(createdPart);
		assertEquals(2, stack.length);

		assertEquals(createdPart, stack[0]);
		assertEquals(fActivePage.findView(MockViewPart.ID), stack[1]);

		assertTrue(fActivePage.isPartVisible(createdPart));

		assertEquals(activePart, fActivePage.getActivePart());
	}

	/**
	 * Test the VIEW_ACTIVE parameter for showView, opening the view in the stack that does not
	 * contain the active view. Ensures that the created view is not the active part but is the top
	 * part in its stack.
	 */
	@Test
	public void testView_ACTIVE2() throws PartInitException {
		fActivePage.setPerspective(WorkbenchPlugin.getDefault().getPerspectiveRegistry().findPerspectiveWithId(ViewPerspective.ID));

		// create a part to be active
		fActivePage.showView(MockViewPart.ID3);

		IViewPart activePart= fActivePage.showView(MockViewPart.ID2, null, IWorkbenchPage.VIEW_ACTIVATE);

		IViewPart[] stack= fActivePage.getViewStack(activePart);
		assertEquals(2, stack.length);

		assertEquals(activePart, stack[0]);
		assertEquals(fActivePage.findView(MockViewPart.ID), stack[1]);

		assertTrue(fActivePage.isPartVisible(activePart));

		assertEquals(activePart, fActivePage.getActivePart());
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
		// Start with a view active in order to verify that the editor gets
		// activated
		fActivePage.showView(IPageLayout.ID_PROBLEM_VIEW, null,
				IWorkbenchPage.VIEW_ACTIVATE);
		callTrace.clear();
		IEditorPart editor = IDE.openEditor(fActivePage, input, id, true);
		assertEquals(editor.getEditorInput(), input);
		assertEquals(editor.getSite().getId(), id);
		assertEquals(hasEditor(editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);
		assertEquals(callTrace.contains("partActivated"), true);

		// we need another editor so that the editor under test can receive
		// events.
		// otherwise, events will be ignored.
		IEditorPart extra = IDE.openEditor(fActivePage, FileUtil.createFile(
				"aaaaa", proj));

		// close the first editor after the second has opened; necessary for
		// test to work with fix to PR 7743
		fActivePage.closeEditor(editor, false);

		// Start with a view active in order to verify that the editor does not
		// get activated
		// (note: regardless of the activate=false flag, the editor would always
		// be activated
		// if it is being opened in the same stack as the active editor. Making
		// a view active
		// initially tests the function of the activate flag)
		fActivePage.showView(IPageLayout.ID_PROBLEM_VIEW, null,
				IWorkbenchPage.VIEW_ACTIVATE);
		callTrace.clear();
		// open an editor without activation
		editor = IDE.openEditor(fActivePage, input, id, false);

		assertEquals(editor.getEditorInput(), input);
		assertEquals(editor.getSite().getId(), id);
		assertEquals(hasEditor(editor), true);
		assertEquals(callTrace.contains("partActivated"), false);
		assertEquals(callTrace.contains("partBroughtToTop"), true);

		fActivePage.activate(extra);

		/*
		 * javadoc: If this page already has an editor open on the target object
		 * that editor is brought to the front
		 */
		// open the editor under test second time without activation
		fActivePage.showView(IPageLayout.ID_PROBLEM_VIEW, null,
				IWorkbenchPage.VIEW_ACTIVATE);
		callTrace.clear();
		assertEquals(IDE.openEditor(fActivePage, input, id, false), editor);
		assertEquals(callTrace.contains("partBroughtToTop"), true);
		assertEquals(callTrace.contains("partActivated"), false);

		// activate the other editor
		fActivePage.activate(extra);

		/*
		 * javadoc: If activate == true the editor will be activated
		 */
		// open the editor under test second time with activation
		fActivePage.showView(IPageLayout.ID_PROBLEM_VIEW, null,
				IWorkbenchPage.VIEW_ACTIVATE);
		callTrace.clear();
		assertEquals(IDE.openEditor(fActivePage, input, id, true), editor);
		assertEquals(callTrace.contains("partBroughtToTop"), true);
		assertEquals(callTrace.contains("partActivated"), true);

		/*
		 * javadoc: If activate == false but another editor in the same stack
		 * was active, the new editor will be activated regardless.
		 */
		// Close the old editor
		fActivePage.closeEditor(editor, false);
		// Ensure another editor in the stack is active
		fActivePage.activate(extra);
		callTrace.clear();
		// Verify that the editor is still activated
		IDE.openEditor(fActivePage, input, id, false);
		assertEquals(callTrace.contains("partBroughtToTop"), true);
		assertEquals(callTrace.contains("partActivated"), true);
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

	/**
	 * openEditor(IWorkbenchPage page, IFile input)
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
		IEditorPart editor = IDE.openEditor(fActivePage, file);

		boolean foundEditor = hasEditor(editor);
		assertEquals(foundEditor, true);
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
		editor = IDE.openEditor(fActivePage, file);
		if (editor != null) {//If it opened an external editor skip it (Vista issue)
			assertEquals(hasEditor(editor), true);
			assertEquals(fActivePage.getActiveEditor(), editor);
			assertEquals(editor.getSite().getId(),
					"org.eclipse.ui.DefaultTextEditor");

			// open another editor to take the focus away from the first editor
			IDE
					.openEditor(fActivePage, FileUtil.createFile("test.mock2",
							proj));

			/*
			 * javadoc: If this page already has an editor open on the target
			 * object that editor is activated
			 */
			// open the editor second time.
			assertEquals(editor, IDE.openEditor(fActivePage, file));
			assertEquals(editor, fActivePage.getActiveEditor());
		}
	}

	/**
	 * openEditor(IWorkbenchPage page, IFile input, String editorID)
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
		IEditorPart editor = IDE.openEditor(fActivePage, file, id);
		assertEquals(editor.getSite().getId(), id);
		assertEquals(hasEditor(editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);

		// open another editor to take the focus away from the first editor
		IDE.openEditor(fActivePage, FileUtil.createFile("test.mock2", proj));

		/*
		 * javadoc: If this page already has an editor open on the target object
		 * that editor is activated
		 */
		// open the first editor second time.
		assertEquals(IDE.openEditor(fActivePage, file, id), editor);
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
		IEditorPart editor = IDE.openEditor(fActivePage, input, id);
		assertEquals(editor.getEditorInput(), input);
		assertEquals(editor.getSite().getId(), id);
		assertEquals(hasEditor(editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);

		// open another editor to take the focus away from the first editor
		IDE.openEditor(fActivePage, FileUtil.createFile("test.mock2", proj));

		/*
		 * javadoc: If this page already has an editor open on the target object
		 * that editor is activated
		 */
		// open the first editor second time.
		assertEquals(IDE.openEditor(fActivePage, input, id), editor);
		assertEquals(fActivePage.getActiveEditor(), editor);
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
		IEditorPart editor = IDE.openEditor(fActivePage, marker);
		callTrace = ((MockEditorPart) editor).getCallHistory();
		assertEquals(editor.getSite().getId(), MockEditorPart.ID2);
		assertEquals(hasEditor(editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);
		assertEquals(callTrace.contains("gotoMarker"), true);
		fActivePage.closeEditor(editor, false);

		/*
		 * javadoc: If the marker contains an EDITOR_ID_ATTR attribute the
		 * attribute value will be used to determine the editor type to be
		 * opened
		 */
		marker.setAttribute(IDE.EDITOR_ID_ATTR, MockEditorPart.ID1);
		editor = IDE.openEditor(fActivePage, marker);
		callTrace = ((MockEditorPart) editor).getCallHistory();
		assertEquals(editor.getSite().getId(), MockEditorPart.ID1);
		assertEquals(hasEditor(editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);
		assertEquals(callTrace.contains("gotoMarker"), true);
		// do not close the editor this time

		/*
		 * javdoc: If this page already has an editor open on the target object
		 * that editor is activated
		 */
		callTrace.clear();
		assertEquals(IDE.openEditor(fActivePage, marker), editor);
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
				"aaaaa", proj));

		/*
		 * javadoc: If activate == true the editor will be activated
		 */
		// open the registered editor for the marker resource with activation
		IEditorPart editor = IDE.openEditor(fActivePage, marker, true);
		editorCall = ((MockEditorPart) editor).getCallHistory();
		assertEquals(editor.getSite().getId(), MockEditorPart.ID2);
		assertEquals(hasEditor(editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);

		/*
		 * javadoc: the cursor and selection state of the editor is then updated
		 * from information recorded in the marker.
		 */
		assertEquals(editorCall.contains("gotoMarker"), true);
		fActivePage.closeEditor(editor, false);

		fActivePage.activate(extra);

		// open the registered editor for the marker resource without activation
		fActivePage.showView(IPageLayout.ID_PROBLEM_VIEW, null,
				IWorkbenchPage.VIEW_ACTIVATE);
		listenerCall.clear();
		editor = IDE.openEditor(fActivePage, marker, false);
		editorCall = ((MockEditorPart) editor).getCallHistory();
		assertEquals(editor.getSite().getId(), MockEditorPart.ID2);
		assertEquals(hasEditor(editor), true);
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
		marker.setAttribute(IDE.EDITOR_ID_ATTR, id);

		// open an editor with activation
		listenerCall.clear();

		editor = IDE.openEditor(fActivePage, marker, true);
		editorCall = ((MockEditorPart) editor).getCallHistory();
		assertEquals(editor.getSite().getId(), id);
		assertEquals(hasEditor(editor), true);
		assertEquals(fActivePage.getActiveEditor(), editor);
		assertEquals(editorCall.contains("gotoMarker"), true);
		fActivePage.closeEditor(editor, false);

		fActivePage.activate(extra);

		// open an editor without activation
		fActivePage.showView(IPageLayout.ID_PROBLEM_VIEW, null,
				IWorkbenchPage.VIEW_ACTIVATE);
		listenerCall.clear();
		editor = IDE.openEditor(fActivePage, marker, false);
		editorCall = ((MockEditorPart) editor).getCallHistory();
		assertEquals(editor.getSite().getId(), id);
		assertEquals(hasEditor(editor), true);
		assertEquals(editorCall.contains("gotoMarker"), true);
		assertEquals(listenerCall.contains("partActivated"), false);
		assertEquals(listenerCall.contains("partBroughtToTop"), true);
		// do not close the editor this time

		fActivePage.activate(extra);
		/*
		 * javadoc: If this page already has an editor open on the target object
		 * that editor is brought to front Note: we need to make a non-editor
		 * active first or bringing the editor to front would activate it
		 */
		// open the editor second time without activation
		fActivePage.showView(IPageLayout.ID_PROBLEM_VIEW, null,
				IWorkbenchPage.VIEW_ACTIVATE);
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

		/*
		 * javadoc: If activate == false but another editor in the same stack
		 * was active, the new editor will be activated regardless.
		 */
		// Close the old editor
		fActivePage.closeEditor(editor, false);
		// Ensure another editor in the stack is active
		fActivePage.activate(extra);
		listenerCall.clear();
		// Verify that the editor is still activated
		IDE.openEditor(fActivePage, marker, false);
		assertEquals(listenerCall.contains("partBroughtToTop"), true);
		assertEquals(listenerCall.contains("partActivated"), true);
	}

	/**
	 * Tests that the marker's value for the <code>IDE.EDITOR_ID_ATTR</code>
	 * attribute.
	 */
	@Test
	public void testOpenEditor7_Bug203640() throws Throwable {
		proj = FileUtil.createProject("testOpenEditor");
		IFile file = FileUtil.createFile("aa.mock2", proj);
		IMarker marker = file.createMarker(
				IMarker.TASK);
		marker.setAttribute(IDE.EDITOR_ID_ATTR, MockEditorPart.ID1);

		// open a regular text editor
		IEditorPart regularEditor = fActivePage.openEditor(new FileEditorInput(file), EditorsUI.DEFAULT_TEXT_EDITOR_ID);
		assertNotNull(regularEditor);
		assertTrue(regularEditor instanceof TextEditor);

		// open the registered editor for the marker resource
		IEditorPart markerEditor = IDE.openEditor(fActivePage, marker);
		assertNotNull(markerEditor);
		assertTrue(markerEditor instanceof MockEditorPart);

		// these shouldn't be the same, if they are it's a bug
		assertFalse(markerEditor == regularEditor);
		assertFalse(markerEditor.equals(regularEditor));
		assertEquals(2, fActivePage.getEditorReferences().length);
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
		IPerspectiveDescriptor per = getWorkbench()
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
				"a.mock1", proj));
		IEditorPart part2 = IDE.openEditor(fActivePage, FileUtil.createFile(
				"b.mock1", proj));

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

	/**
	 * Test to ensure that a minimized view can be brought to the top and
	 * consequently made visible.
	 *
	 * @param hasEditors whether there should be editors open or not
	 */
	private void testBringToTop_MinimizedViewBug292966(boolean hasEditors) throws Throwable {
		IPerspectiveRegistry reg = fWorkbench.getPerspectiveRegistry();
		IPerspectiveDescriptor resourcePersp = reg.findPerspectiveWithId(IDE.RESOURCE_PERSPECTIVE_ID);
		fActivePage.setPerspective(resourcePersp);
		processEvents();

		// first show the view we're going to test
		IViewPart propertiesView = fActivePage.showView(IPageLayout.ID_PROP_SHEET);
		assertNotNull(propertiesView);

		processEvents();

		proj = FileUtil.createProject("testOpenEditor");
		// open an editor
		IEditorPart editor = IDE.openEditor(fActivePage, FileUtil.createFile(
				"a.mock1", proj));
		assertNotNull("The editor could not be opened", editor); //$NON-NLS-1$
		assertTrue("The editor is not visible", fActivePage.isPartVisible(editor)); //$NON-NLS-1$

		if (!hasEditors) {
			// close editors if we don't want them opened for this test
			fActivePage.closeAllEditors(false);
			processEvents();
			assertEquals("All the editors should have been closed", 0, fActivePage.getEditorReferences().length); //$NON-NLS-1$
		}

		// minimize the view we're testing
		fActivePage.setPartState(fActivePage.getReference(propertiesView), IWorkbenchPage.STATE_MINIMIZED);
		assertFalse("A minimized view should not be visible", fActivePage.isPartVisible(propertiesView)); //$NON-NLS-1$
		processEvents();

		// open another view so that it now becomes the active part container
		IViewPart projectExplorer = fActivePage.showView(IPageLayout.ID_PROJECT_EXPLORER);
		// get the list of views that shares the stack with this other view
		IViewPart[] viewStack = fActivePage.getViewStack(projectExplorer);
		processEvents();
		// make sure that we didn't inadvertently bring back the test view by mistake
		for (IViewPart element : viewStack) {
			assertFalse("The properties view should not be on the same stack as the project explorer", //$NON-NLS-1$
					element.getSite().getId().equals(IPageLayout.ID_PROP_SHEET));
		}

		// bring the test view back from its minimized state
		fActivePage.bringToTop(propertiesView);
		// the view should be visible
		assertTrue("Invoking bringToTop(IWorkbenchPart) should cause the part to be visible", //$NON-NLS-1$
				fActivePage.isPartVisible(propertiesView));
	}

	@Test
	public void testBringToTop_MinimizedViewWithEditorsBug292966() throws Throwable {
		testBringToTop_MinimizedViewBug292966(false);
	}

	@Test
	public void testBringToTop_MinimizedViewWithoutEditorsBug292966() throws Throwable {
		testBringToTop_MinimizedViewBug292966(true);
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
	 * Tests showing multi-instance views (docked normally).
	 */
	@Test
	public void testShowViewMult() throws Throwable {
		/*
		 * javadoc: Shows the view identified by the given view id and secondary
		 * id in this page and gives it focus. This allows multiple instances of
		 * a particular view to be created. They are disambiguated using the
		 * secondary id.
		 */
		MockViewPart view = (MockViewPart) fActivePage
				.showView(MockViewPart.IDMULT);
		assertNotNull(view);
		assertTrue(view.getCallHistory().verifyOrder(
				new String[] { "init", "createPartControl", "setFocus" }));
		MockViewPart view2 = (MockViewPart) fActivePage.showView(
				MockViewPart.IDMULT, "2", IWorkbenchPage.VIEW_ACTIVATE);
		assertNotNull(view2);
		assertTrue(view2.getCallHistory().verifyOrder(
				new String[] { "init", "createPartControl", "setFocus" }));
		assertTrue(!view.equals(view2));
		MockViewPart view3 = (MockViewPart) fActivePage.showView(
				MockViewPart.IDMULT, "3", IWorkbenchPage.VIEW_ACTIVATE);
		assertNotNull(view3);
		assertTrue(view3.getCallHistory().verifyOrder(
				new String[] { "init", "createPartControl", "setFocus" }));
		assertTrue(!view.equals(view3));
		assertTrue(!view2.equals(view3));

		/*
		 * javadoc: If there is a view identified by the given view id and
		 * secondary id already open in this page, it is given focus.
		 */
		CallHistory callTrace = view.getCallHistory();
		callTrace.clear();
		assertEquals(fActivePage.showView(MockViewPart.IDMULT), view);
		assertEquals(callTrace.contains("setFocus"), true);
		CallHistory callTrace2 = view2.getCallHistory();
		callTrace.clear();
		callTrace2.clear();
		assertEquals(fActivePage.showView(MockViewPart.IDMULT, "2",
				IWorkbenchPage.VIEW_ACTIVATE), view2);
		assertEquals(callTrace2.contains("setFocus"), true);
		assertEquals(callTrace.contains("setFocus"), false);
		CallHistory callTrace3 = view3.getCallHistory();
		callTrace.clear();
		callTrace2.clear();
		callTrace3.clear();
		assertEquals(fActivePage.showView(MockViewPart.IDMULT, "3",
				IWorkbenchPage.VIEW_ACTIVATE), view3);
		assertEquals(callTrace3.contains("setFocus"), true);
		assertEquals(callTrace.contains("setFocus"), false);
		assertEquals(callTrace2.contains("setFocus"), false);

		// TODO expectations below do not work, exception is not thrown
		/*
		 * javadoc: If a secondary id is given, the view must allow multiple
		 * instances by having specified allowMultiple="true" in its extension.
		 */
//		boolean exceptionThrown = false;
//		try {
//			fActivePage.showView(MockViewPart.ID, "2",
//					IWorkbenchPage.VIEW_ACTIVATE);
//		} catch (PartInitException e) {
//			assertEquals(e.getMessage().indexOf("mult") != -1, true);
//			exceptionThrown = true;
//		}
//		assertEquals(exceptionThrown, true);
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

	/**
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=471782
	 */
	@Test
	public void testFindViewReference() throws Throwable {
		fWin.getWorkbench().showPerspective(ViewPerspective.ID, fWin);
		processEvents();
		assertNull(fActivePage.findView(MockViewPart.ID4));
		assertNull(fActivePage.findView(MockViewPart.ID2));
		assertNotNull(fActivePage.findView(MockViewPart.ID));

		assertNull(fActivePage.findViewReference(MockViewPart.ID4));
		assertNull(fActivePage.findViewReference(MockViewPart.ID2));
		assertNotNull(fActivePage.findViewReference(MockViewPart.ID));

		fActivePage.showView(MockViewPart.ID2);
		assertNotNull(fActivePage.findViewReference(MockViewPart.ID2));
		assertNotNull(fActivePage.findView(MockViewPart.ID2));

		fWin.getWorkbench().showPerspective(SessionPerspective.ID, fWin);
		processEvents();
		assertNull(fActivePage.findView(MockViewPart.ID4));
		assertNull(fActivePage.findView(MockViewPart.ID2));
		assertNotNull(fActivePage.findView(SessionView.VIEW_ID));

		assertNull(fActivePage.findViewReference(MockViewPart.ID4));
		assertNull(fActivePage.findViewReference(MockViewPart.ID2));
		assertNotNull(fActivePage.findViewReference(SessionView.VIEW_ID));

		fActivePage.showView(MockViewPart.ID2);
		assertNotNull(fActivePage.findView(MockViewPart.ID2));
		assertNotNull(fActivePage.findViewReference(MockViewPart.ID2));

		fActivePage.showView(MockViewPart.ID4);
		assertNotNull(fActivePage.findView(MockViewPart.ID4));
		assertNotNull(fActivePage.findViewReference(MockViewPart.ID4));
	}

	/**
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=471782
	 */
	@Test
	public void testFindViewReferenceAfterShowViewCommand() throws Throwable {
		boolean activeShell = forceActive(fWin.getShell());

		final AtomicBoolean shellIsActive = new AtomicBoolean(activeShell);
		Assume.assumeTrue(shellIsActive.get());

		ShellListener shellListener = new ShellStateListener(shellIsActive);
		fWin.getShell().addShellListener(shellListener);

		fWin.getWorkbench().showPerspective(ViewPerspective.ID, fWin);
		processEvents();
		assertNull(fActivePage.findView(MockViewPart.ID4));
		assertNull(fActivePage.findView(MockViewPart.ID2));
		assertNotNull(fActivePage.findView(MockViewPart.ID));

		assertNull(fActivePage.findViewReference(MockViewPart.ID4));
		assertNull(fActivePage.findViewReference(MockViewPart.ID2));
		assertNotNull(fActivePage.findViewReference(MockViewPart.ID));

		Assume.assumeTrue(forceActive(fWin.getShell()));

		showViewViaCommand(MockViewPart.ID2);

		Assume.assumeTrue(fWin.getShell().isVisible());
		Assume.assumeTrue(getWorkbench().getActiveWorkbenchWindow() == fWin);
		Assume.assumeTrue(shellIsActive.get());

		assertNotNull(fActivePage.findView(MockViewPart.ID2));
		assertNotNull(fActivePage.findViewReference(MockViewPart.ID2));

		fWin.getWorkbench().showPerspective(SessionPerspective.ID, fWin);
		processEvents();
		assertNull(fActivePage.findView(MockViewPart.ID2));
		assertNull(fActivePage.findView(MockViewPart.ID4));
		assertNotNull(fActivePage.findView(SessionView.VIEW_ID));

		assertNull(fActivePage.findViewReference(MockViewPart.ID2));
		assertNull(fActivePage.findViewReference(MockViewPart.ID4));
		assertNotNull(fActivePage.findViewReference(SessionView.VIEW_ID));

		showViewViaCommand(MockViewPart.ID2);
		assertNotNull(fActivePage.findView(MockViewPart.ID2));
		assertNotNull(fActivePage.findViewReference(MockViewPart.ID2));

		showViewViaCommand(MockViewPart.ID4);
		assertNotNull(fActivePage.findView(MockViewPart.ID4));
		assertNotNull(fActivePage.findViewReference(MockViewPart.ID4));
	}

	/**
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=471782
	 */
	@Test
	public void testFindHistoryViewReferenceAfterShowViewCommand() throws Throwable {
		boolean activeShell = forceActive(fWin.getShell());

		final AtomicBoolean shellIsActive = new AtomicBoolean(activeShell);
		Assume.assumeTrue(shellIsActive.get());

		ShellListener shellListener = new ShellStateListener(shellIsActive);
		fWin.getShell().addShellListener(shellListener);

		String historyView = "org.eclipse.team.ui.GenericHistoryView";
		fWin.getWorkbench().showPerspective(ViewPerspective.ID, fWin);
		processEvents();
		assertNull(fActivePage.findView(MockViewPart.ID4));
		assertNull(fActivePage.findView(MockViewPart.ID2));
		assertNotNull(fActivePage.findView(MockViewPart.ID));

		assertNull(fActivePage.findViewReference(MockViewPart.ID4));
		assertNull(fActivePage.findViewReference(MockViewPart.ID2));
		assertNull(fActivePage.findViewReference(historyView));
		assertNotNull(fActivePage.findViewReference(MockViewPart.ID));

		Assume.assumeTrue(forceActive(fWin.getShell()));
		showViewViaCommand(historyView);

		Assume.assumeTrue(fWin.getShell().isVisible());
		Assume.assumeTrue(getWorkbench().getActiveWorkbenchWindow() == fWin);
		Assume.assumeTrue(shellIsActive.get());

		assertNotNull(fActivePage.findView(historyView));
		assertNotNull(fActivePage.findViewReference(historyView));

		fWin.getWorkbench().showPerspective(SessionPerspective.ID, fWin);
		processEvents();
		assertNull(fActivePage.findView(MockViewPart.ID2));
		assertNull(fActivePage.findView(MockViewPart.ID4));
		assertNull(fActivePage.findView(historyView));
		assertNotNull(fActivePage.findView(SessionView.VIEW_ID));

		assertNull(fActivePage.findViewReference(MockViewPart.ID2));
		assertNull(fActivePage.findViewReference(MockViewPart.ID4));
		assertNull(fActivePage.findViewReference(historyView));
		assertNotNull(fActivePage.findViewReference(SessionView.VIEW_ID));

		showViewViaCommand(historyView);
		assertNotNull(fActivePage.findView(historyView));
		assertNotNull(fActivePage.findViewReference(historyView));
	}

	private void showViewViaCommand(String viewId) throws Throwable {
		waitForJobs(500, 3000);
		Map<String, String> parameters = new HashMap<>();
		parameters.put(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_ID, viewId);

		Command command = createCommand(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW);
		ExecutionEvent event = createEvent(command, parameters);

		// org.eclipse.ui.handlers.ShowViewHandler needs the *right* window!
		assertEquals(fWin, HandlerUtil.getActiveWorkbenchWindow(event));

		command.executeWithChecks(event);
		processEvents();
		waitForJobs(500, 3000);
	}

	private ExecutionEvent createEvent(Command command, Map<String, String> parameters) {
		IWorkbench workbench = getWorkbench();
		IHandlerService handlerService = workbench.getService(IHandlerService.class);
		IEvaluationContext contextSnapshot = handlerService.createContextSnapshot(true);
		return new ExecutionEvent(command, parameters, null, contextSnapshot);
	}

	private Command createCommand(String id) {
		ICommandService commandService = getWorkbench().getService(ICommandService.class);
		return commandService.getCommand(id);
	}

	@Test
	public void testFindSecondaryViewReference() throws Throwable {
		fActivePage.getWorkbenchWindow().getWorkbench().showPerspective(
				SessionPerspective.ID, fActivePage.getWorkbenchWindow());
		processEvents();
		assertNull(fActivePage.findViewReference(MockViewPart.IDMULT, "1"));

		fActivePage.showView(MockViewPart.IDMULT, "1", IWorkbenchPage.VIEW_ACTIVATE);
		assertNotNull(fActivePage.findViewReference(MockViewPart.IDMULT, "1"));
	}

	@Test
	public void testGetViews() throws Throwable {
		int totalBefore = fActivePage.getViewReferences().length;

		IViewPart view = fActivePage.showView(MockViewPart.ID2);
		assertEquals(hasView(view), true);
		assertEquals(fActivePage.getViewReferences().length, totalBefore + 1);

		fActivePage.hideView(view);
		assertEquals(hasView(view), false);
		assertEquals(fActivePage.getViewReferences().length, totalBefore);
	}

	@Test
	public void testHideViewWithPart() throws Throwable {
		// test that nothing bad happens with a null parameter
		fActivePage.hideView((IViewPart) null);

		IViewPart view = fActivePage.showView(MockViewPart.ID3);

		fActivePage.hideView(view);
		CallHistory callTrace = ((MockViewPart) view).getCallHistory();
		assertTrue(callTrace.contains("dispose"));
	}

	@Test
	public void testHideViewWithReference() throws Throwable {
		// test that nothing bad happens with a null parameter
		fActivePage.hideView((IViewReference) null);

		IViewPart view = fActivePage.showView(MockViewPart.ID4);
		IViewReference ref = fActivePage.findViewReference(MockViewPart.ID4);
		fActivePage.hideView(ref);
		CallHistory callTrace = ((MockViewPart) view).getCallHistory();
		assertTrue(callTrace.contains("dispose"));

	}

	@Test
	public void testHideSaveableView() throws Throwable {
		String viewId = SaveableMockViewPart.ID;
		SaveableMockViewPart view = (SaveableMockViewPart) fActivePage
				.showView(viewId);
		fActivePage.hideView(view);
		CallHistory callTrace = view.getCallHistory();
		assertTrue(callTrace.contains("isDirty"));
		assertTrue(callTrace.contains("dispose"));
		assertEquals(fActivePage.findView(viewId), null);

		try {
			APITestUtils.saveableHelperSetAutomatedResponse(1); // No
			view = (SaveableMockViewPart) fActivePage.showView(viewId);
			view.setDirty(true);
			fActivePage.hideView(view);
			callTrace = view.getCallHistory();
			assertTrue(callTrace.contains("isDirty"));
			assertFalse(callTrace.contains("doSave"));
			assertTrue(callTrace.contains("dispose"));
			assertEquals(fActivePage.findView(viewId), null);

			APITestUtils.saveableHelperSetAutomatedResponse(2); // Cancel
			view = (SaveableMockViewPart) fActivePage.showView(viewId);
			view.setDirty(true);
			fActivePage.hideView(view);
			callTrace = view.getCallHistory();
			assertTrue(callTrace.contains("isDirty"));
			assertFalse(callTrace.contains("doSave"));
			assertFalse(callTrace.contains("dispose"));
			assertEquals(fActivePage.findView(viewId), view);

			APITestUtils.saveableHelperSetAutomatedResponse(0); // Yes
			view = (SaveableMockViewPart) fActivePage.showView(viewId);
			view.setDirty(true);
			fActivePage.hideView(view);
			callTrace = view.getCallHistory();
			assertTrue(callTrace.contains("isDirty"));
			assertTrue(callTrace.contains("doSave"));
			assertTrue(callTrace.contains("dispose"));
			assertEquals(fActivePage.findView(viewId), null);

			// don't leave the view showing, or the UI will block on window
			// close
		} finally {
			view.setDirty(false);
			APITestUtils.saveableHelperSetAutomatedResponse(-1); // restore default
			// (prompt)
		}
	}

	/**
	 * Tests that a close will fall back to the default if the view returns
	 * ISaveable2.DEFAULT.
	 */
	@Test
	public void testCloseWithSaveNeeded() throws Throwable {
		String viewId = UserSaveableMockViewPart.ID;
		UserSaveableMockViewPart view = (UserSaveableMockViewPart) fActivePage
				.showView(viewId);
		fActivePage.hideView(view);

		UserSaveableMockViewPart view2 = null;

		CallHistory callTrace = view.getCallHistory();
		assertTrue(callTrace.contains("isDirty"));
		assertTrue(callTrace.contains("dispose"));
		assertEquals(fActivePage.findView(UserSaveableMockViewPart.ID), null);

		try {
			APITestUtils.saveableHelperSetAutomatedResponse(3); // DEFAULT
			view = (UserSaveableMockViewPart) fActivePage.showView(viewId);
			view.setDirty(true);
			view2 = (UserSaveableMockViewPart) fActivePage.showView(viewId,
					"2", IWorkbenchPage.VIEW_ACTIVATE);
			assertNotNull(view2);
			view2.setDirty(true);

			fActivePage.saveAllEditors(true);

			assertFalse(view.isDirty());
			assertFalse(view2.isDirty());

			callTrace = view.getCallHistory();
			fActivePage.hideView(view);
			fActivePage.hideView(view2);

			assertTrue(callTrace.contains("isDirty"));
			assertTrue(callTrace.contains("doSave"));
			assertEquals(fActivePage.findView(viewId), null);

			// don't leave the view showing, or the UI will block on window
			// close
		} finally {
			APITestUtils.saveableHelperSetAutomatedResponse(-1); // restore
			// default
			// (prompt)
		}
	}

	/**
	 * Tests that a close will fall back to the default if the view returns
	 * ISaveable2.DEFAULT.
	 */
	@Test
	public void testSaveEffectsSharedModel() throws Throwable {
		String viewId = UserSaveableSharedViewPart.ID;
		UserSaveableSharedViewPart view = null;
		UserSaveableSharedViewPart view2 = null;
		assertEquals(fActivePage.findView(UserSaveableSharedViewPart.ID), null);

		try {
			APITestUtils.saveableHelperSetAutomatedResponse(3); // DEFAULT
			UserSaveableSharedViewPart.SharedModel model = new UserSaveableSharedViewPart.SharedModel();
			view = (UserSaveableSharedViewPart) fActivePage.showView(viewId);
			view.setSharedModel(model);
			MPart part = ((WorkbenchPage) fActivePage).findPart(view);
			view2 = (UserSaveableSharedViewPart) fActivePage.showView(viewId,
					"2", IWorkbenchPage.VIEW_ACTIVATE);
			assertNotNull(view2);
			view2.setSharedModel(model);

			MPart part2 = ((WorkbenchPage) fActivePage).findPart(view2);
			fActivePage.saveAllEditors(true);

			assertFalse(view.isDirty());
			assertFalse(view2.isDirty());

			CallHistory callTrace = view.getCallHistory();
			CallHistory call2 = view2.getCallHistory();

			assertTrue(callTrace.contains("isDirty"));
			assertTrue(call2.contains("isDirty"));
			assertTrue("At least one should call doSave", callTrace
					.contains("doSave")
					|| call2.contains("doSave"));
			assertFalse("Both should not call doSave", callTrace
					.contains("doSave")
					&& call2.contains("doSave"));

			// The MPart pertaining to the view needs to be set to not dirty as only the
			// view is set as not dirty and the MPart associated is still dirty
			// and the UI will block on window close
			if (part.isDirty()) {
				part.setDirty(false);
			}
			if (part2.isDirty()) {
				part2.setDirty(false);
			}

			// don't leave the view showing, or the UI will block on window
			// close

		} finally {
			APITestUtils.saveableHelperSetAutomatedResponse(-1); // restore
			// default
			// (prompt)
			fActivePage.hideView(view);
			fActivePage.hideView(view2);
		}
	}

	@Test
	public void testClose() throws Throwable {
		IWorkbenchPage page = openTestPage(fWin);

		proj = FileUtil.createProject("testOpenEditor");
		final IFile file = FileUtil.createFile("aaa.mock1", proj);
		IEditorPart editor = IDE.openEditor(page, file);
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
		editor = IDE.openEditor(fActivePage, file);
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
		editor = IDE.openEditor(fActivePage, file);
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
	public void testCloseEditors() throws Throwable {
		int total = 5;
		final IFile[] files = new IFile[total];
		IEditorPart[] editors = new IEditorPart[total];
		IEditorReference[] editorRefs = new IEditorReference[total];
		CallHistory[] callTraces = new CallHistory[total];
		MockEditorPart[] mocks = new MockEditorPart[total];

		proj = FileUtil.createProject("testCloseEditors");
		for (int i = 0; i < total; i++) {
			files[i] = FileUtil.createFile(i + ".mock2", proj);
		}

		/*
		 * javadoc: If the page has open editors with unsaved content and save
		 * is true, the user will be given the opportunity to save them.
		 */
		// close all clean editors with confirmation
		for (int i = 0; i < total; i++) {
			editors[i] = IDE.openEditor(fActivePage, files[i]);
			callTraces[i] = ((MockEditorPart) editors[i]).getCallHistory();
		}

		editorRefs = fActivePage.getEditorReferences();
		assertEquals(fActivePage.closeEditors(editorRefs, true), true);
		for (int i = 0; i < total; i++) {
			assertEquals(callTraces[i].contains("isDirty"), true);
			assertEquals(callTraces[i].contains("doSave"), false);
			callTraces[i].clear();
		}

		// close all dirty editors with confirmation
		// can't be tested

		// close all dirty editors discarding them
		for (int i = 0; i < total; i++) {
			editors[i] = IDE.openEditor(fActivePage, files[i]);
			mocks[i] = (MockEditorPart) editors[i];
			mocks[i].setDirty(true);
			callTraces[i] = mocks[i].getCallHistory();
		}
		editorRefs = fActivePage.getEditorReferences();
		assertEquals(fActivePage.closeEditors(editorRefs, false), true);
		for (int i = 0; i < total; i++) {
			assertEquals(callTraces[i].contains("doSave"), false);
		}

		// close empty array of editors
		total = 1;
		for (int i = 0; i < total; i++) {
			editors[i] = IDE.openEditor(fActivePage, files[i]);
			mocks[i] = (MockEditorPart) editors[i];
			mocks[i].setDirty(true);
			callTraces[i] = mocks[i].getCallHistory();
		}
		// empty array test
		editorRefs = new IEditorReference[0];
		assertEquals(fActivePage.closeEditors(editorRefs, true), true);
		for (int i = 0; i < total; i++) {
			assertEquals(callTraces[i].contains("isDirty"), true);
			assertEquals(callTraces[i].contains("doSave"), false);
			callTraces[i].clear();
		}

		// close the last remaining editor, with save=false
		editorRefs = fActivePage.getEditorReferences();
		fActivePage.closeEditors(editorRefs, false);
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
			editors[i] = IDE.openEditor(fActivePage, files[i]);
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
			editors[i] = IDE.openEditor(fActivePage, files[i]);
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
		editor = IDE.openEditor(fActivePage, file);
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
	public void testIDESaveAllEditors() throws Throwable {
		int total = 3;

		final IFile[] files = new IFile[total];
		IEditorPart[] editors = new IEditorPart[total];
		CallHistory[] callTraces = new CallHistory[total];
		MockEditorPart[] mocks = new MockEditorPart[total];

		proj = FileUtil.createProject("testOpenEditor");
		for (int i = 0; i < total; i++) {
			files[i] = FileUtil.createFile(i + ".mock2", proj);
			editors[i] = IDE.openEditor(fActivePage, files[i]);
			mocks[i] = (MockEditorPart) editors[i];
			callTraces[i] = mocks[i].getCallHistory();
		}

		/*
		 * javadoc: If there are no dirty editors this method returns without
		 * effect. javadoc: If confirm is true the user is prompted to confirm
		 * the command
		 */
		// save all clean editors with confirmation
		assertEquals(IDE.saveAllEditors(new IResource[] { proj }, true), true);
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
		assertEquals(IDE.saveAllEditors(new IResource[] { proj }, false), true);
		for (int i = 0; i < total; i++) {
			assertEquals(callTraces[i].contains("isDirty"), true);
			assertEquals(callTraces[i].contains("doSave"), false);
			callTraces[i].clear();
		}

		// save all dirty editors with resource that IS NOT a parent
		// of the contents of the dirty editors without confirmation, this
		// should not
		// save any as they are not parented by the resource provided
		for (int i = 0; i < total; i++) {
			mocks[i].setDirty(true);
		}

		IResource emptyProj = FileUtil
				.createProject("testOpenEditorEmptyProject");
		assertEquals(IDE.saveAllEditors(new IResource[] { emptyProj }, false),
				true);
		for (int i = 0; i < total; i++) {
			// the editors were not in the empty project hence still dirty
			assertEquals(mocks[i].isDirty(), true);
			callTraces[i].clear();
		}

		// save all dirty editors with resource that IS a parent
		// of the contents of the editors without confirmation, this should
		// save them as they are parented by the resource provided
		assertEquals(IDE.saveAllEditors(new IResource[] { proj }, false), true);
		for (int i = 0; i < total; i++) {
			// the editors were not in the empty project hence still dirty
			assertEquals(mocks[i].isDirty(), false);
			assertEquals(callTraces[i].contains("isDirty"), true);
			assertEquals(callTraces[i].contains("doSave"), true);
			callTraces[i].clear();
		}

		// save all dirty editors with resource that IS NOT a parent
		// of the contents of the dirty editors without confirmation, this
		// should not
		// save any as they are not parented by the resource provided
		for (int i = 0; i < total; i++) {
			mocks[i].setDirty(true);
		}
		assertEquals(IDE.saveAllEditors(new IResource[] {}, false), true);
		for (int i = 0; i < total; i++) {
			// the editors were not in the empty project hence still dirty
			assertEquals(mocks[i].isDirty(), true);
			callTraces[i].clear();
		}

		// clear the dirty state so the tearDown does not open a confirm dialog.
		for (int i = 0; i < total; i++) {
			mocks[i].setDirty(false);
		}
	}

	@Test
	public void testSaveAllEditors() throws Throwable {
		int total = 3;

		final IFile[] files = new IFile[total];
		IEditorPart[] editors = new IEditorPart[total];
		CallHistory[] callTraces = new CallHistory[total];
		MockEditorPart[] mocks = new MockEditorPart[total];

		proj = FileUtil.createProject("testOpenEditor");
		for (int i = 0; i < total; i++) {
			files[i] = FileUtil.createFile(i + ".mock2", proj);
			editors[i] = IDE.openEditor(fActivePage, files[i]);
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
		int totalBefore = fActivePage.getEditorReferences().length;
		int num = 3;
		IEditorPart[] editors = new IEditorPart[num];

		for (int i = 0; i < num; i++) {
			editors[i] = IDE.openEditor(fActivePage, FileUtil.createFile(i
					+ ".mock2", proj));
			assertEquals(hasEditor(editors[i]), true);
		}
		assertEquals(fActivePage.getEditorReferences().length, totalBefore
				+ num);

		fActivePage.closeEditor(editors[0], false);
		assertEquals(hasEditor(editors[0]), false);
		assertEquals(fActivePage.getEditorReferences().length, totalBefore
				+ num - 1);

		fActivePage.closeAllEditors(false);
		assertEquals(fActivePage.getEditorReferences().length, 0);
	}

	@Test
	public void testShowActionSet() {
		String id = MockActionDelegate.ACTION_SET_ID;

		int totalBefore = ((WorkbenchPage) fActivePage).getActionSets().length;

		fActivePage.showActionSet(id);

		IActionSetDescriptor[] sets = ((WorkbenchPage) fActivePage).getActionSets();
		boolean found = false;
		for (int i = 0; i < sets.length && !found; i++) {
			if (id.equals(sets[i].getId())) {
				found = true;
			}
		}
		assertTrue("Failed for " + id,  found);


		// check that the method does not add an invalid action set to itself
		id = IConstants.FakeID;
		fActivePage.showActionSet(id);

		sets = ((WorkbenchPage) fActivePage).getActionSets();
		found = false;
		for (int i = 0; i < sets.length && !found; i++) {
			if (id.equals(sets[i].getId())) {
				found = true;
			}
		}
		assertFalse("Failed for " + id,  found);
		assertEquals(sets.length, totalBefore + 1);
	}

	@Test
	public void testHideActionSet() {
		int totalBefore = ((WorkbenchPage) fActivePage).getActionSets().length;

		String id = MockWorkbenchWindowActionDelegate.SET_ID;
		fActivePage.showActionSet(id);
		assertEquals(((WorkbenchPage) fActivePage).getActionSets().length, totalBefore + 1);

		fActivePage.hideActionSet(id);
		assertEquals(((WorkbenchPage) fActivePage).getActionSets().length, totalBefore);

		IActionSetDescriptor[] sets = ((WorkbenchPage) fActivePage).getActionSets();
		boolean found = false;
		for (int i = 0; i < sets.length && !found; i++) {
			if (id.equals(sets[i].getId())) {
				found = true;
			}
		}
		assertFalse("Failed for " + id,  found);

	}

	/**
	 * Return whether or not the editor exists in the current page.
	 *
	 * @return boolean
	 */
	private boolean hasEditor(IEditorPart editor) {
		IEditorReference[] references = fActivePage.getEditorReferences();
		for (IEditorReference reference : references) {
			if (reference.getEditor(false).equals(editor)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return whether or not the view exists in the current page.
	 *
	 * @return boolean
	 */
	private boolean hasView(IViewPart view) {
		IViewReference[] references = fActivePage.getViewReferences();
		for (IViewReference reference : references) {
			if (reference.getView(false).equals(view)) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void testStackOrder() throws PartInitException {
		IViewPart part1 = fActivePage.showView(MockViewPart.ID);
		IViewPart part2 = fActivePage.showView(MockViewPart.ID2);
		IViewPart part3 = fActivePage.showView(MockViewPart.ID3);
		IViewPart part4 = fActivePage.showView(MockViewPart.ID4);

		IViewPart[] stack = fActivePage.getViewStack(part1);
		assertTrue(stack.length == 4);
		assertTrue(stack[0] == part4);
		assertTrue(stack[1] == part3);
		assertTrue(stack[2] == part2);
		assertTrue(stack[3] == part1);

		fActivePage.activate(part2);
		stack = fActivePage.getViewStack(part1);
		assertTrue(stack.length == 4);
		assertTrue(stack[0] == part2);
		assertTrue(stack[1] == part4);
		assertTrue(stack[2] == part3);
		assertTrue(stack[3] == part1);

		fActivePage.activate(part1);
		stack = fActivePage.getViewStack(part1);
		assertTrue(stack.length == 4);
		assertTrue(stack[0] == part1);
		assertTrue(stack[1] == part2);
		assertTrue(stack[2] == part4);
		assertTrue(stack[3] == part3);

		fActivePage.activate(part3);
		stack = fActivePage.getViewStack(part1);
		assertTrue(stack.length == 4);
		assertTrue(stack[0] == part3);
		assertTrue(stack[1] == part1);
		assertTrue(stack[2] == part2);
		assertTrue(stack[3] == part4);
	}

	/**
	 * Test the VIEW_CREATE parameter for showView. Ensures that the created
	 * view is not the active part.
	 */
	@Test
	public void testView_CREATE1() throws PartInitException {
		fActivePage.setPerspective(fActivePage.getWorkbenchWindow().getWorkbench()
				.getPerspectiveRegistry().findPerspectiveWithId(
							ViewPerspective.ID));

		// create a part to be active
		IViewPart activePart = fActivePage.showView(MockViewPart.ID);
		IViewPart createdPart = fActivePage.showView(MockViewPart.ID2, null,
				IWorkbenchPage.VIEW_CREATE);

		IViewPart[] stack = fActivePage.getViewStack(activePart);
		assertEquals(2, stack.length);

		assertEquals(activePart, stack[0]);
		assertEquals(createdPart, stack[1]);

		assertFalse(fActivePage.isPartVisible(createdPart));

		assertEquals(activePart, fActivePage.getActivePart());
	}

	/**
	 * Test the VIEW_CREATE parameter for showView. Ensures that the created
	 * view is not the active part and is not visible
	 */
	@Test
	public void testView_CREATE2() throws PartInitException {
		fActivePage.setPerspective(fActivePage.getWorkbenchWindow().getWorkbench()
				.getPerspectiveRegistry().findPerspectiveWithId(
						ViewPerspective.ID));

		// create a part to be active
		IViewPart activePart = fActivePage.showView(MockViewPart.ID3);
		IViewPart createdPart = fActivePage.showView(MockViewPart.ID2, null,
				IWorkbenchPage.VIEW_CREATE);

		IViewPart[] stack = fActivePage.getViewStack(createdPart);
		assertEquals(2, stack.length);

		assertEquals(fActivePage.findView(MockViewPart.ID), stack[0]);
		assertEquals(createdPart, stack[1]);

		assertFalse(fActivePage.isPartVisible(createdPart));

		assertEquals(activePart, fActivePage.getActivePart());
	}

	/**
	 * Test the VIEW_CREATE parameter for showView. Ensures that the created
	 * view is not the active part and is visible.
	 */
	@Test
	public void testView_CREATE3() throws PartInitException {
		fActivePage.setPerspective(fActivePage.getWorkbenchWindow().getWorkbench()
				.getPerspectiveRegistry().findPerspectiveWithId(
						ViewPerspective.ID));

		// create a part to be active
		IViewPart activePart = fActivePage.showView(MockViewPart.ID3);
		IViewPart createdPart = fActivePage.showView(MockViewPart.ID4, null,
				IWorkbenchPage.VIEW_CREATE);

		IViewPart[] stack = fActivePage.getViewStack(createdPart);
		assertEquals(1, stack.length);

		assertEquals(createdPart, stack[0]);

		assertTrue(fActivePage.isPartVisible(createdPart));

		assertEquals(activePart, fActivePage.getActivePart());
	}

	/**
	 * Test the VIEW_VISIBLE parameter for showView, opening the view in the
	 * stack containing the active view. Ensures that the created view is not
	 * the active part and is not visible.
	 */
	@Test
	public void testView_VISIBLE1() throws PartInitException {
		fActivePage.setPerspective(fActivePage.getWorkbenchWindow().getWorkbench()
				.getPerspectiveRegistry().findPerspectiveWithId(
						ViewPerspective.ID));

		// create a part to be active
		IViewPart activePart = fActivePage.showView(MockViewPart.ID);
		IViewPart createdPart = fActivePage.showView(MockViewPart.ID2, null,
				IWorkbenchPage.VIEW_VISIBLE);
		IViewPart[] stack = fActivePage.getViewStack(activePart);
		assertEquals(2, stack.length);

		assertEquals(activePart, stack[0]);
		assertEquals(createdPart, stack[1]);

		assertFalse(fActivePage.isPartVisible(createdPart));

		assertEquals(activePart, fActivePage.getActivePart());
	}

	/**
	 * Test the VIEW_VISIBLE parameter for showView, opening the view in its own
	 * stack. Ensures that the created view is not active part but is the top
	 * part in its stack.
	 */
	@Test
	public void testView_VISIBLE3() throws PartInitException {
		fActivePage.setPerspective(fActivePage.getWorkbenchWindow().getWorkbench()
					.getPerspectiveRegistry().findPerspectiveWithId(
							ViewPerspective.ID));

		// create a part to be active
		IViewPart activePart = fActivePage.showView(MockViewPart.ID3);

		IViewPart createdPart = fActivePage.showView(MockViewPart.ID4, null,
				IWorkbenchPage.VIEW_VISIBLE);
		IViewPart[] stack = fActivePage.getViewStack(createdPart);
		assertEquals(1, stack.length);

		assertEquals(createdPart, stack[0]);

		assertTrue(fActivePage.isPartVisible(createdPart));

		assertEquals(activePart, fActivePage.getActivePart());
	}

	/**
	 * Test opening a perspective with placeholders for multi instance views. The
	 * placeholders are added at top level (not in any folder).
	 *
	 * @throws WorkbenchException
	 *
	 * @since 3.1
	 */
	@Test
	@Ignore
	public void testOpenPerspectiveWithMultiViewPlaceholdersAtTopLevel() throws WorkbenchException {

		fWin.getWorkbench().showPerspective(PerspectiveWithMultiViewPlaceholdersAtTopLevel.PERSP_ID, fWin);

//		ArrayList partIds = facade.getPerspectivePartIds(fActivePage, null);
		// FIXME: No implementation for facade.getPerspectivePartIds()

//		assertTrue(partIds.contains("*"));
//		assertTrue(partIds.contains(MockViewPart.IDMULT));
//		assertTrue(partIds.contains(MockViewPart.IDMULT + ":secondaryId"));
//		assertTrue(partIds.contains(MockViewPart.IDMULT + ":*"));
	}

	/**
	 * Test opening a perspective with placeholders for multi instance views. The
	 * placeholders are added in a placeholder folder. This is a regression test for
	 * bug 72383 [Perspectives] Placeholder folder error with multiple instance
	 * views
	 *
	 * @throws WorkbenchException
	 *
	 * @since 3.1
	 */
	@Test
	@Ignore
	public void testOpenPerspectiveWithMultiViewPlaceholdersInPlaceholderFolder() throws WorkbenchException {

		fWin.getWorkbench().showPerspective(PerspectiveWithMultiViewPlaceholdersInPlaceholderFolder.PERSP_ID, fWin);

//		ArrayList partIds = facade.getPerspectivePartIds(fActivePage,"placeholderFolder");

		// FIXME: No implementation for facade.getPerspectivePartIds()

//		assertTrue(partIds.contains("*"));
//		assertTrue(partIds.contains(MockViewPart.IDMULT));
//		assertTrue(partIds.contains(MockViewPart.IDMULT + ":secondaryId"));
//		assertTrue(partIds.contains(MockViewPart.IDMULT + ":*"));
	}

	/**
	 * Test opening a perspective with placeholders for multi instance views. The
	 * placeholders are added at top level (not in any folder).
	 *
	 * @throws WorkbenchException
	 *
	 * @since 3.1
	 */
	@Test
	@Ignore
	public void testOpenPerspectiveWithMultiViewPlaceholdersInFolder() throws WorkbenchException {
		fWin.getWorkbench().showPerspective(PerspectiveWithMultiViewPlaceholdersInFolder.PERSP_ID, fWin);

//		ArrayList partIds = facade.getPerspectivePartIds(fActivePage,"folder");

		// FIXME: No implementation for facade.getPerspectivePartIds()

//		assertTrue(partIds.contains("*"));
//		assertTrue(partIds.contains(MockViewPart.IDMULT));
//		assertTrue(partIds.contains(MockViewPart.IDMULT + ":secondaryId"));
//		assertTrue(partIds.contains(MockViewPart.IDMULT + ":*"));
	}

	/**
	 * Tests the getNewWizardShortcuts() method.
	 *
	 * @since 3.1
	 */
	@Test
	public void testGetNewWizardShortcuts() {
		String[] shortcuts = fActivePage.getNewWizardShortcuts();
		assertNotNull(shortcuts);
		assertEquals(0, shortcuts.length);

		IWorkbenchWindow win = openTestWindow(IDE.RESOURCE_PERSPECTIVE_ID);
		IWorkbenchPage page = win.getActivePage();
		shortcuts = page.getNewWizardShortcuts();
		List<String> shortcutList = Arrays.asList(shortcuts);
		assertTrue(shortcutList.contains("org.eclipse.ui.wizards.new.folder"));
		assertTrue(shortcutList.contains("org.eclipse.ui.wizards.new.file"));
	}

	/**
	 * Tests the getShowViewShortcuts() method.
	 *
	 * @since 3.1
	 */
	@Test
	public void testGetShowViewShortcuts() {
		String[] shortcuts = fActivePage.getShowViewShortcuts();
		assertNotNull(shortcuts);
		assertEquals(0, shortcuts.length);

		IWorkbenchWindow win = openTestWindow(IDE.RESOURCE_PERSPECTIVE_ID);
		IWorkbenchPage page = win.getActivePage();
		shortcuts = page.getShowViewShortcuts();
		List<String> shortcutList = Arrays.asList(shortcuts);
		assertTrue(shortcutList.contains(ProjectExplorer.VIEW_ID));
		assertTrue(shortcutList.contains(IPageLayout.ID_OUTLINE));
		assertTrue(shortcutList.contains(IPageLayout.ID_PROP_SHEET));
		assertTrue(shortcutList.contains(IPageLayout.ID_PROBLEM_VIEW));
	}

	/**
	 * Tests the getPerspectiveShortcuts() method.
	 *
	 * @since 3.1
	 */
	@Test
	public void testGetPerspectiveShortcuts() {
		String[] shortcuts = fActivePage.getPerspectiveShortcuts();
		assertNotNull(shortcuts);
		assertEquals(0, shortcuts.length);
		// not much of a test
	}

	/**
	 * Tests the getOpenPerspectives() method.
	 *
	 * @since 3.1
	 */
	@Test
	public void testGetOpenPerspectives() {
		IPerspectiveDescriptor[] openPersps = fActivePage.getOpenPerspectives();
		assertEquals(1, openPersps.length);
		assertEquals(EmptyPerspective.PERSP_ID, openPersps[0].getId());

		IPerspectiveRegistry reg = fWorkbench.getPerspectiveRegistry();
		IPerspectiveDescriptor resourcePersp = reg
				.findPerspectiveWithId(IDE.RESOURCE_PERSPECTIVE_ID);
		fActivePage.setPerspective(resourcePersp);
		openPersps = fActivePage.getOpenPerspectives();
		assertEquals(2, openPersps.length);
		assertEquals(EmptyPerspective.PERSP_ID, openPersps[0].getId());
		assertEquals(IDE.RESOURCE_PERSPECTIVE_ID, openPersps[1].getId());

		IPerspectiveDescriptor emptyPersp = reg
				.findPerspectiveWithId(EmptyPerspective.PERSP_ID);
		fActivePage.setPerspective(emptyPersp);
		openPersps = fActivePage.getOpenPerspectives();
		assertEquals(2, openPersps.length);
		assertEquals(EmptyPerspective.PERSP_ID, openPersps[0].getId());
		assertEquals(IDE.RESOURCE_PERSPECTIVE_ID, openPersps[1].getId());

		fActivePage.closeAllPerspectives(false, false);
		openPersps = fActivePage.getOpenPerspectives();
		assertEquals(0, openPersps.length);

		fActivePage.close();
		openPersps = fActivePage.getOpenPerspectives();
		assertEquals(0, openPersps.length);
	}

	/**
	 * Tests the getSortedPerspectives() method.
	 *
	 * @since 3.1
	 */
	@Test
	public void testGetSortedPerspectives() {
		IPerspectiveDescriptor[] openPersps = fActivePage
				.getSortedPerspectives();
		assertEquals(1, openPersps.length);
		assertEquals(EmptyPerspective.PERSP_ID, openPersps[0].getId());

		IPerspectiveRegistry reg = fWorkbench.getPerspectiveRegistry();
		IPerspectiveDescriptor resourcePersp = reg
				.findPerspectiveWithId(IDE.RESOURCE_PERSPECTIVE_ID);
		fActivePage.setPerspective(resourcePersp);
		openPersps = fActivePage.getSortedPerspectives();
		assertEquals(2, openPersps.length);
		assertEquals(EmptyPerspective.PERSP_ID, openPersps[0].getId());
		assertEquals(IDE.RESOURCE_PERSPECTIVE_ID, openPersps[1].getId());

		IPerspectiveDescriptor emptyPersp = reg
				.findPerspectiveWithId(EmptyPerspective.PERSP_ID);
		fActivePage.setPerspective(emptyPersp);
		openPersps = fActivePage.getSortedPerspectives();
		assertEquals(2, openPersps.length);
		assertEquals(IDE.RESOURCE_PERSPECTIVE_ID, openPersps[0].getId());
		assertEquals(EmptyPerspective.PERSP_ID, openPersps[1].getId());

		fActivePage.closeAllPerspectives(false, false);
		openPersps = fActivePage.getSortedPerspectives();
		assertEquals(0, openPersps.length);

		fActivePage.close();
		openPersps = fActivePage.getSortedPerspectives();
		assertEquals(0, openPersps.length);
	}

	/**
	 * Tests the closePerspective method.
	 *
	 * @since 3.1
	 */
	@Test
	public void testClosePerspective() {
		// TODO: Need to test variants with saveEditors==true

		IPerspectiveRegistry reg = fWorkbench.getPerspectiveRegistry();
		IPerspectiveDescriptor emptyPersp = reg
				.findPerspectiveWithId(EmptyPerspective.PERSP_ID);
		IPerspectiveDescriptor resourcePersp = reg
				.findPerspectiveWithId(IDE.RESOURCE_PERSPECTIVE_ID);

		fActivePage.setPerspective(resourcePersp);
		IPerspectiveDescriptor[] openPersps = fActivePage.getOpenPerspectives();
		assertEquals(2, openPersps.length);

		fActivePage.closePerspective(resourcePersp, false, false);
		openPersps = fActivePage.getOpenPerspectives();
		assertEquals(1, openPersps.length);
		assertEquals(EmptyPerspective.PERSP_ID, openPersps[0].getId());

		fActivePage.closePerspective(emptyPersp, false, false);
		assertEquals(fActivePage, fWin.getActivePage()); // page not closed
		openPersps = fActivePage.getOpenPerspectives();
		assertEquals(0, openPersps.length);

		fActivePage.setPerspective(emptyPersp);
		openPersps = fActivePage.getOpenPerspectives();
		assertEquals(1, openPersps.length);

		fActivePage.closePerspective(emptyPersp, false, true);
		openPersps = fActivePage.getOpenPerspectives();
		assertEquals(0, openPersps.length);
		assertNull(fWin.getActivePage()); // page closed
	}

	/**
	 * This tests that closing a perspective will not bring a prompt up for
	 * {@link org.eclipse.ui.ISaveablePart ISaveablePart} implementations that
	 * are returning false for their
	 * {@link org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
	 * isSaveOnCloseNeeded()} implementation.
	 *
	 * @see #testCloseAllPerspectivesDoesNotPromptBug272070()
	 */
	@Test
	public void testClosePerspectiveDoesNotPromptBug272070() throws Exception {
		try {
			APITestUtils.saveableHelperSetAutomatedResponse(2);
			proj = FileUtil
					.createProject("testClosePerspectiveDoesNotPromptBug272070");

			IPerspectiveRegistry reg = fWorkbench.getPerspectiveRegistry();
			IPerspectiveDescriptor resourcePersp = reg
					.findPerspectiveWithId(IDE.RESOURCE_PERSPECTIVE_ID);

			// close all perspectives so we start fresh
			fActivePage.closeAllPerspectives(false, false);
			// set the page to the 'Resource' perspective
			fActivePage.setPerspective(resourcePersp);

			// create a file and show an editor
			IEditorInput input = new FileEditorInput(FileUtil.createFile(
					"test.mock1", proj));
			MockEditorPart editor = (MockEditorPart) fActivePage.openEditor(
					input, MockEditorPart.ID1);

			// mark the editor as being dirty but not requiring saving when
			// closed
			editor.setDirty(true);
			editor.setSaveNeeded(false);

			// close the perspective
			fActivePage.closePerspective(resourcePersp, true, false);
			// mark the editor as not dirty, this is important because if the
			// editor is not closed, the test will fail and when JUnit tries to
			// tear down the workbench it will not shutdown because it will
			// prompt about the editor being dirty
			editor.setDirty(false);
			// the editor should have been closed when the perspective was
			// closed
			assertFalse("The editor should've been closed", fActivePage
					.isPartVisible(editor));

			// set the page to the 'Resource' perspective
			fActivePage.setPerspective(resourcePersp);

			// show a view
			SaveableMockViewPart view = (SaveableMockViewPart) fActivePage
					.showView(SaveableMockViewPart.ID);

			// mark the view as being dirty but not requiring saving when closed
			view.setDirty(true);
			view.setSaveNeeded(false);

			// close the perspective
			fActivePage.closePerspective(resourcePersp, true, false);
			// like the editor above, we need to mark the view as not being
			// dirty for the same reasons
			view.setDirty(false);
			// the view should have been hidden when the perspective was closed
			assertFalse("The view should be hidden", fActivePage
					.isPartVisible(view));
		} finally {
			APITestUtils.saveableHelperSetAutomatedResponse(-1);
		}
	}

	/**
	 * Tests the closeAllPerspectives method.
	 *
	 * @since 3.1
	 */
	@Test
	public void testCloseAllPerspectives() {
		// TODO: Need to test variants with saveEditors==true

		IPerspectiveRegistry reg = fWorkbench.getPerspectiveRegistry();
		IPerspectiveDescriptor emptyPersp = reg
				.findPerspectiveWithId(EmptyPerspective.PERSP_ID);
		IPerspectiveDescriptor resourcePersp = reg
				.findPerspectiveWithId(IDE.RESOURCE_PERSPECTIVE_ID);

		fActivePage.setPerspective(resourcePersp);
		IPerspectiveDescriptor[] openPersps = fActivePage.getOpenPerspectives();
		assertEquals(2, openPersps.length);

		fActivePage.closeAllPerspectives(false, false);
		assertEquals(fActivePage, fWin.getActivePage()); // page not closed
		openPersps = fActivePage.getOpenPerspectives();
		assertEquals(0, openPersps.length);

		fActivePage.setPerspective(emptyPersp);
		openPersps = fActivePage.getOpenPerspectives();
		assertEquals(1, openPersps.length);

		fActivePage.closeAllPerspectives(false, true);
		openPersps = fActivePage.getOpenPerspectives();
		assertEquals(0, openPersps.length);
		assertNull(fWin.getActivePage()); // page closed
	}

	/**
	 * This tests that closing all perspectives will not bring a prompt up for
	 * {@link org.eclipse.ui.ISaveablePart ISaveablePart} implementations that
	 * are returning false for their
	 * {@link org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
	 * isSaveOnCloseNeeded()} implementation.
	 *
	 * @see #testClosePerspectiveDoesNotPromptBug272070()
	 */
	@Test
	public void testCloseAllPerspectivesDoesNotPromptBug272070()
			throws Exception {
		try {
			APITestUtils.saveableHelperSetAutomatedResponse(2);
			proj = FileUtil
					.createProject("testCloseAllPerspectivesDoesNotPromptBug272070");

			IPerspectiveRegistry reg = fWorkbench.getPerspectiveRegistry();
			IPerspectiveDescriptor resourcePersp = reg
					.findPerspectiveWithId(IDE.RESOURCE_PERSPECTIVE_ID);

			// close all perspectives so we start fresh
			fActivePage.closeAllPerspectives(false, false);
			// set the page to the 'Resource' perspective
			fActivePage.setPerspective(resourcePersp);

			// create a file and show an editor
			IEditorInput input = new FileEditorInput(FileUtil.createFile(
					"test.mock1", proj));
			MockEditorPart editor = (MockEditorPart) fActivePage.openEditor(
					input, MockEditorPart.ID1);

			// mark the editor as being dirty but not requiring saving when
			// closed
			editor.setDirty(true);
			editor.setSaveNeeded(false);

			// close all perspectives
			fActivePage.closeAllPerspectives(true, false);
			// mark the editor as not dirty, this is important because if the
			// editor is not closed, the test will fail and when JUnit tries to
			// tear down the workbench it will not shutdown because it will
			// prompt about the editor being dirty
			editor.setDirty(false);
			// the editor should have been closed when the perspective was
			// closed
			assertFalse("The editor should've been closed", fActivePage
					.isPartVisible(editor));

			// set the page to the 'Resource' perspective
			fActivePage.setPerspective(resourcePersp);

			// show a view
			SaveableMockViewPart view = (SaveableMockViewPart) fActivePage
					.showView(SaveableMockViewPart.ID);

			// mark the view as being dirty but not requiring saving when closed
			view.setDirty(true);
			view.setSaveNeeded(false);

			// close all perspectives
			fActivePage.closeAllPerspectives(true, false);
			// like the editor above, we need to mark the view as not being
			// dirty for the same reasons
			view.setDirty(false);
			// the view should have been hidden when the perspective was closed
			assertFalse("The view should be hidden", fActivePage
					.isPartVisible(view));
		} finally {
			APITestUtils.saveableHelperSetAutomatedResponse(-1);
		}
	}

	/**
	 * Tests that relative view is correctly shown if visible parameter specified.
	 * See bug 538199 - perspectiveExtension: visible="false" not honored when
	 * relative view does not exist
	 *
	 * @throws WorkbenchException
	 */
	@Test
	public void testRelativeViewVisibility() throws WorkbenchException {
		processEvents();

		fActivePage.closeAllPerspectives(true, false);
		IPerspectiveRegistry reg = fWorkbench.getPerspectiveRegistry();
		IPerspectiveDescriptor testPerspective = reg.findPerspectiveWithId(PerspectiveViewsBug538199.ID);
		fActivePage.setPerspective(testPerspective);
		fWin.getWorkbench().showPerspective(PerspectiveViewsBug538199.ID, fWin);

		processEvents();
		IWorkbenchPage activePage = fWin.getActivePage();

		IViewPart view;

		// default view which should be shown
		view = activePage.findView(MockViewPart.ID);
		assertNotNull("View should be there", view);
		assertTrue("View should be visible", fActivePage.isPartVisible(view));

		// relative view with property 'visible=true' specified via xml
		view = activePage.findView("org.eclipse.ui.tests.api.MockViewPartVisibleByDefault");
		assertTrue("Relative 'visible' view should be visible even if the relative does not exist", //$NON-NLS-1$
				fActivePage.isPartVisible(view));

		// relative view with property 'visible=false' specified via xml
		view = activePage.findView("org.eclipse.ui.tests.api.MockViewPartInvisibleByDefault");
		assertNull("Relative 'invisible' part should not be visible if the relative does not exist", view);
	}

	/**
	 * Regression test for Bug 76285 [Presentations] Folder tab does not indicate
	 * current view. Tests that, when switching between perspectives, the remembered
	 * old part correctly handles multi-view instances.
	 *
	 * @throws PartInitException
	 */
	@Test
	public void testBug76285() throws PartInitException {
		IWorkbenchPage page = fActivePage;
		IPerspectiveDescriptor originalPersp = page.getPerspective();
		IPerspectiveDescriptor resourcePersp = getWorkbench()
				.getPerspectiveRegistry().findPerspectiveWithId(
						IDE.RESOURCE_PERSPECTIVE_ID);
		// test requires switching between two different perspectives
		assertNotSame(originalPersp, resourcePersp);

		int n = 5;
		IViewPart[] views = new IViewPart[n];
		for (int i = 0; i < n; ++i) {
			views[i] = page.showView(MockViewPart.IDMULT, Integer.toString(i), IWorkbenchPage.VIEW_CREATE);
		}
		assertEquals(5, page.getViews().length);
		for (int i = 0; i < n; ++i) {
			page.activate(views[i]);
			page.setPerspective(resourcePersp);
			assertFalse(page.getActivePart() instanceof MockViewPart);
			page.setPerspective(originalPersp);
			assertEquals(views[i], page.getActivePart());
		}
	}

	/**
	 * Tests that IShowEditorInput.showEditorInput is called when a matching
	 * editor is found during openEditor, and is not called when a new editor is
	 * opened.
	 *
	 * @since 3.1
	 */
	@Test
	public void testShowEditorInput() throws Exception {
		IWorkbenchPage page = fActivePage;
		proj = FileUtil.createProject("testShowEditorInput");
		IFile file = FileUtil.createFile("a.mock1", proj);
		MockEditorPart part1 = (MockEditorPart) IDE.openEditor(page, file);
		assertFalse(part1.getCallHistory().contains("showEditorInput"));

		MockEditorPart part2 = (MockEditorPart) IDE.openEditor(page, file);
		assertTrue(part1 == part2);
		assertTrue(part2.getCallHistory().contains("showEditorInput"));
	}

	/**
	 * Tests that the openEditor and findEditor variants that accepts match
	 * flags work as expected.
	 *
	 * @since 3.2
	 */
	@Test
	@Ignore
	public void testOpenAndFindEditorWithMatchFlags() throws Exception {
		IWorkbenchPage page = fActivePage;
		proj = FileUtil.createProject("testOpenEditorMatchFlags");
		IFile file1 = FileUtil.createFile("a.mock1", proj);
		IFile file2 = FileUtil.createFile("a.mock2", proj);
		FileEditorInput input1 = new FileEditorInput(file1);
		FileEditorInput input2 = new FileEditorInput(file2);
		String id1 = MockEditorPart.ID1;
		String id2 = MockEditorPart.ID2;

		// first editor (no match)
		MockEditorPart part1 = (MockEditorPart) page.openEditor(input1, id1,
				true, IWorkbenchPage.MATCH_INPUT);
		assertNotNull(part1);

		// same input, same id, matching input (should match part1)
		MockEditorPart part2 = (MockEditorPart) page.openEditor(input1, id1,
				true, IWorkbenchPage.MATCH_INPUT);
		assertTrue(part1 == part2);

		// same input, different id, matching input (should match part1)
		MockEditorPart part3 = (MockEditorPart) page.openEditor(input1, id2,
				true, IWorkbenchPage.MATCH_INPUT);
		assertTrue(part1 == part3);

		// same input, different id, matching input and id (no match)
		MockEditorPart part4 = (MockEditorPart) page.openEditor(input1, id2,
				true, IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
		assertNotNull(part4);
		assertTrue(part4 != part1);

		// same input, same id, matching nothing (no match)
		MockEditorPart part5 = (MockEditorPart) page.openEditor(input1, id1,
				true, IWorkbenchPage.MATCH_NONE);
		assertNotNull(part5);
		assertTrue(part5 != part1);
		assertTrue(part5 != part4);

		// different input, same id, matching id (should match part5 instead of
		// part1, because it was active)
		MockEditorPart part6 = (MockEditorPart) page.openEditor(input2, id1,
				true, IWorkbenchPage.MATCH_ID);
		assertTrue(part6 == part5);

		// different input, different id, matching input and id (no match)
		MockEditorPart part7 = (MockEditorPart) page.openEditor(input2, id2,
				true, IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
		assertNotNull(part7);
		assertTrue(part7 != part1);
		assertTrue(part7 != part4);
		assertTrue(part7 != part5);

		// At this point, there are 4 editors open:
		// part1 (input1, id1), part4 (input1, id2), part5 (input1, id1), and
		// part7 (input2, id2).
		// with part7 active.

		// find with MATCH_NONE is always empty
		IEditorReference[] refs = page.findEditors(input1, id1,
				IWorkbenchPage.MATCH_NONE);
		assertEquals(0, refs.length);

		// find input1 with MATCH_INPUT finds 3 editors: part1, part4 and part5
		// (in order)
		refs = page.findEditors(input1, null, IWorkbenchPage.MATCH_INPUT);
		assertEquals(3, refs.length);
		assertEquals(part1, refs[0].getPart(true));
		assertEquals(part4, refs[1].getPart(true));
		assertEquals(part5, refs[2].getPart(true));

		// find input2 with MATCH_INPUT finds 1 editor: part7
		refs = page.findEditors(input2, null, IWorkbenchPage.MATCH_INPUT);
		assertEquals(1, refs.length);
		assertEquals(part7, refs[0].getPart(true));

		// find id1 with MATCH_ID finds 2 editors: part1 and part5 (in order)
		refs = page.findEditors(null, id1, IWorkbenchPage.MATCH_ID);
		assertEquals(2, refs.length);
		assertEquals(part1, refs[0].getPart(true));
		assertEquals(part5, refs[1].getPart(true));

		// find id2 with MATCH_ID finds 2 editors: part4 and part7 (with part7
		// first because it was active)
		refs = page.findEditors(null, id2, IWorkbenchPage.MATCH_ID);
		assertEquals(2, refs.length);
		assertEquals(part7, refs[0].getPart(true));
		assertEquals(part4, refs[1].getPart(true));

		// find input1 and id1 with MATCH_INPUT and MATCH_ID finds 2 editors:
		// part1 and part5 (in order)
		refs = page.findEditors(input1, id1, IWorkbenchPage.MATCH_INPUT
				| IWorkbenchPage.MATCH_ID);
		assertEquals(2, refs.length);
		assertEquals(part1, refs[0].getPart(true));
		assertEquals(part5, refs[1].getPart(true));

		// find input1 and id2 with MATCH_INPUT and MATCH_ID finds 1 editors:
		// part4
		refs = page.findEditors(input1, id2, IWorkbenchPage.MATCH_INPUT
				| IWorkbenchPage.MATCH_ID);
		assertEquals(1, refs.length);
		assertEquals(part4, refs[0].getPart(true));
	}


	/**
	 * Create and hide a single editor, and check it is reflected in the
	 * editor references.  Check that close still works.
	 */
	@Test
	@Ignore
	public void testOpenAndHideEditor1() throws Exception {
		proj = FileUtil.createProject("testOpenAndHideEditor");
		IFile file1 = FileUtil.createFile("a.mock1", proj);
		IEditorPart editor = IDE.openEditor(fActivePage, file1);
		assertTrue(editor instanceof MockEditorPart);
		IEditorReference editorRef = (IEditorReference) fActivePage
				.getReference(editor);
		fActivePage.hideEditor(editorRef);
		assertEquals(0, fActivePage.getEditorReferences().length);
		fActivePage.showEditor(editorRef);
		assertEquals(1, fActivePage.getEditorReferences().length);
		fActivePage.closeAllEditors(true);
		assertEquals(0, fActivePage.getEditorReferences().length);
		assertEquals(getMessage(), 0, logCount);
	}

	/**
	 * Create and remove 2 editors.  Check that the removed editor
	 * is not returned in the list of references.  Check that
	 * close still works.
	 */
	@Test
	@Ignore
	public void testOpenAndHideEditor2() throws Exception {
		proj = FileUtil.createProject("testOpenAndHideEditor");
		IFile file1 = FileUtil.createFile("a.mock1", proj);
		IFile file2 = FileUtil.createFile("a.mock2", proj);
		IEditorPart editor = IDE.openEditor(fActivePage, file1);
		assertTrue(editor instanceof MockEditorPart);
		IEditorReference editorRef = (IEditorReference) fActivePage
				.getReference(editor);
		IEditorPart editor2 = IDE.openEditor(fActivePage, file2);
		assertTrue(editor2 instanceof MockEditorPart);
		IEditorReference editorRef2 = (IEditorReference) fActivePage.getReference(editor2);

		fActivePage.hideEditor(editorRef);
		IEditorReference[] refs = fActivePage.getEditorReferences();
		assertEquals(1, refs.length);
		assertEquals(editorRef2, refs[0]);
		fActivePage.showEditor(editorRef);
		refs = fActivePage.getEditorReferences();
		assertEquals(2, refs.length);

		fActivePage.hideEditor(editorRef2);
		refs = fActivePage.getEditorReferences();
		assertEquals(1, refs.length);
		fActivePage.hideEditor(editorRef);
		refs = fActivePage.getEditorReferences();
		assertEquals(0, refs.length);
		fActivePage.showEditor(editorRef);
		refs = fActivePage.getEditorReferences();
		assertEquals(editorRef, refs[0]);
		fActivePage.showEditor(editorRef2);
		refs = fActivePage.getEditorReferences();
		assertEquals(2, refs.length);

		fActivePage.closeAllEditors(true);
		refs = fActivePage.getEditorReferences();
		assertEquals(0, refs.length);
		assertEquals(getMessage(), 0, logCount);
	}

	/**
	 * Create 2 editors and hide one.  When added back and then closed, there
	 * should only be one editor.  Adding back the closed editor should
	 * generate a log message and not effect the list of editors.
	 */
	@Test
	@Ignore
	public void testOpenAndHideEditor3() throws Exception {
		proj = FileUtil.createProject("testOpenAndHideEditor");
		IFile file1 = FileUtil.createFile("a.mock1", proj);
		IFile file2 = FileUtil.createFile("a.mock2", proj);
		IEditorPart editor = IDE.openEditor(fActivePage, file1);
		assertTrue(editor instanceof MockEditorPart);
		IEditorReference editorRef = (IEditorReference) fActivePage
				.getReference(editor);
		IEditorPart editor2 = IDE.openEditor(fActivePage, file2);
		assertTrue(editor2 instanceof MockEditorPart);
		IEditorReference editorRef2 = (IEditorReference) fActivePage.getReference(editor2);

		fActivePage.hideEditor(editorRef2);
		IEditorReference[] refs = fActivePage.getEditorReferences();
		assertEquals(1, refs.length);
		assertEquals(editorRef, refs[0]);
		fActivePage.showEditor(editorRef2);
		fActivePage.closeEditors(new IEditorReference[] { editorRef2 }, true);
		refs = fActivePage.getEditorReferences();
		assertEquals(1, refs.length);
		fActivePage.showEditor(editorRef2);
		assertEquals(1, refs.length);
		assertEquals(getMessage(), 1, logCount);
		assertNotNull(getMessage());
		assertTrue(getMessage().startsWith("adding a disposed part"));
	}

	/**
	 * Create 2 editors, and remove and show one of them.  Trying to
	 * add it a second time should not effect the list of editor references.
	 */
	@Test
	@Ignore
	public void testOpenAndHideEditor4() throws Exception {
		proj = FileUtil.createProject("testOpenAndHideEditor");
		IFile file1 = FileUtil.createFile("a.mock1", proj);
		IFile file2 = FileUtil.createFile("a.mock2", proj);
		IEditorPart editor = IDE.openEditor(fActivePage, file1);
		assertTrue(editor instanceof MockEditorPart);
		IEditorReference editorRef = (IEditorReference) fActivePage
				.getReference(editor);
		IEditorPart editor2 = IDE.openEditor(fActivePage, file2);
		assertTrue(editor2 instanceof MockEditorPart);
		IEditorReference editorRef2 = (IEditorReference) fActivePage.getReference(editor2);

		fActivePage.hideEditor(editorRef2);
		IEditorReference[] refs = fActivePage.getEditorReferences();
		assertEquals(1, refs.length);
		assertEquals(editorRef, refs[0]);
		fActivePage.showEditor(editorRef2);
		refs = fActivePage.getEditorReferences();
		assertEquals(2, refs.length);
		fActivePage.showEditor(editorRef2);
		refs = fActivePage.getEditorReferences();
		assertEquals(2, refs.length);
		assertEquals(getMessage(), 0, logCount);
	}

	/**
	 * Create 2 editors that effect the Content Outline view.  Make
	 * sure that hiding and showing the active editor effects the
	 * outline view.
	 */
	@Test
	@Ignore
	public void testOpenAndHideEditor5() throws Exception {
		proj = FileUtil.createProject("testOpenAndHideEditor");
		IFile file1 = FileUtil.createFile("a1.java", proj);
		IFile file2 = FileUtil.createFile("a2.java", proj);
		IEditorPart editor = IDE.openEditor(fActivePage, file1);
		assertTrue(editor.getClass().getName().endsWith("CompilationUnitEditor"));
		IEditorReference editorRef = (IEditorReference) fActivePage
				.getReference(editor);
		IEditorPart editor2 = IDE.openEditor(fActivePage, file2);
		assertTrue(editor2.getClass().getName().endsWith("CompilationUnitEditor"));

		ContentOutline outline = (ContentOutline) fActivePage.showView(IPageLayout.ID_OUTLINE);
		IPage page2 = outline.getCurrentPage();
		fActivePage.activate(editor);
		processEvents();
		IPage page = outline.getCurrentPage();
		assertFalse(page2==page);

		assertEquals(getMessage(), 0, logCount);

		fActivePage.hideEditor(editorRef);
		assertEquals(page2, outline.getCurrentPage());
		assertEquals(getMessage(), 0, logCount);

		fActivePage.showEditor(editorRef);
		assertEquals(page2, outline.getCurrentPage());
		assertEquals(getMessage(), 0, logCount);

		fActivePage.activate(editor);
		assertEquals(page, outline.getCurrentPage());
		assertEquals(getMessage(), 0, logCount);
	}

	/**
	 * Create one editor.  Make sure hiding and showing it effects
	 * the outline view, and that when hidden the outline view
	 * reflects the default page.
	 */
	@Test
	@Ignore
	public void testOpenAndHideEditor6() throws Exception {
		proj = FileUtil.createProject("testOpenAndHideEditor");
		IFile file1 = FileUtil.createFile("a1.java", proj);
		IEditorPart editor = IDE.openEditor(fActivePage, file1);
		assertTrue(editor.getClass().getName().endsWith("CompilationUnitEditor"));
		IEditorReference editorRef = (IEditorReference) fActivePage
				.getReference(editor);

		ContentOutline outline = (ContentOutline) fActivePage.showView(IPageLayout.ID_OUTLINE);
		IPage defaultPage = outline.getDefaultPage();
		assertNotNull(defaultPage);

		processEvents();
		IPage page = outline.getCurrentPage();
		assertFalse(defaultPage==page);

		assertEquals(getMessage(), 0, logCount);
		assertEquals(0, partHiddenCount);
		fActivePage.addPartListener(partListener2);
		fActivePage.hideEditor(editorRef);
		processEvents();

		assertEquals(1, partHiddenCount);
		assertEquals(editorRef, partHiddenRef);

		assertEquals(defaultPage, outline.getCurrentPage());
		//assertEquals(page, outline.getCurrentPage());
		assertEquals(getMessage(), 0, logCount);

		assertEquals(0, partVisibleCount);
		fActivePage.showEditor(editorRef);
		processEvents();
		assertEquals(page, outline.getCurrentPage());
		assertEquals(getMessage(), 0, logCount);
		assertEquals(1, partVisibleCount);
		assertEquals(editorRef, partVisibleRef);

		fActivePage.activate(editor);
		assertEquals(page, outline.getCurrentPage());
		assertEquals(getMessage(), 0, logCount);
	}

	/**
	 * Create one editor.  Make sure hiding the editor updates
	 * the window title.
	 */
	@Test
	@Ignore
	public void testOpenAndHideEditor7() throws Exception {
		proj = FileUtil.createProject("testOpenAndHideEditor");
		IFile file1 = FileUtil.createFile("a1.java", proj);
		IEditorPart editor = IDE.openEditor(fActivePage, file1);
		assertTrue(editor.getClass().getName().endsWith("CompilationUnitEditor"));
		IEditorReference editorRef = (IEditorReference) fActivePage
				.getReference(editor);

		processEvents();

		String firstTitle = fWin.getShell().getText();

		assertEquals(getMessage(), 0, logCount);
		assertEquals(0, partHiddenCount);
		fActivePage.addPartListener(partListener2);
		fActivePage.hideEditor(editorRef);
		processEvents();

		assertEquals(1, partHiddenCount);
		assertEquals(editorRef, partHiddenRef);

		String nextTitle = fWin.getShell().getText();
		String tooltip = editor.getTitleToolTip();
		assertNotNull(tooltip);
		String[] split = Util.split(nextTitle, '-');
		assertEquals(2, split.length);
		String nextTitleRebuilt = split[0] + "- " + tooltip + " -" + split[1];
		assertEquals(firstTitle, nextTitleRebuilt);

		assertEquals(0, partVisibleCount);
		fActivePage.showEditor(editorRef);
		processEvents();
		assertEquals(getMessage(), 0, logCount);
		assertEquals(1, partVisibleCount);
		assertEquals(editorRef, partVisibleRef);
		nextTitle = fWin.getShell().getText();
		assertEquals(firstTitle, nextTitle);

		fActivePage.activate(editor);
		assertEquals(getMessage(), 0, logCount);
	}

	/**
	 * Create one editor.  Make sure hiding the editor that is the active part
	 * causes another part to become active.
	 */
	@Test
	@Ignore
	public void testOpenAndHideEditor8() throws Exception {
		proj = FileUtil.createProject("testOpenAndHideEditor");
		IFile file1 = FileUtil.createFile("a1.java", proj);
		IEditorPart editor = IDE.openEditor(fActivePage, file1);
		assertTrue(editor.getClass().getName().endsWith("CompilationUnitEditor"));
		IEditorReference editorRef = (IEditorReference) fActivePage
				.getReference(editor);

		ContentOutline outline = (ContentOutline) fActivePage.showView(IPageLayout.ID_OUTLINE);
		IPage defaultPage = outline.getDefaultPage();
		assertNotNull(defaultPage);
		fActivePage.activate(editor);

		processEvents();
		IPage page = outline.getCurrentPage();
		assertFalse(defaultPage==page);

		partActiveCount = 0;
		partActiveRef = null;
		assertEquals(getMessage(), 0, logCount);
		assertEquals(0, partHiddenCount);
		fActivePage.addPartListener(partListener2);
		fActivePage.hideEditor(editorRef);
		processEvents();

		assertEquals(1, partHiddenCount);
		assertEquals(editorRef, partHiddenRef);
		assertEquals(1, partActiveCount);
		assertFalse(partActiveRef == editorRef);

		fActivePage.showEditor(editorRef);

		assertEquals(getMessage(), 0, logCount);
	}

	/**
	 * Create a java editor.  Make a change.  Validate the enabled state
	 * of some commands.
	 */
	@Test
	@Ignore
	public void testOpenAndHideEditor9() throws Exception {
		proj = FileUtil.createProject("testOpenAndHideEditor");
		IFile file1 = FileUtil.createFile("a1.java", proj);
		IEditorPart editor = IDE.openEditor(fActivePage, file1);
		assertTrue(editor.getClass().getName()
				.endsWith("CompilationUnitEditor"));
		IEditorReference editorRef = (IEditorReference) fActivePage
				.getReference(editor);

		fActivePage.activate(editor);

		processEvents();
		ICommandService cs = fActivePage.getWorkbenchWindow()
				.getService(ICommandService.class);
		Command undo = cs.getCommand("org.eclipse.ui.edit.undo");
		assertTrue(undo.isDefined());

		assertFalse(undo.isEnabled());

		ITextEditor textEditor = (ITextEditor) editor;
		IDocument doc = textEditor.getDocumentProvider().getDocument(
				textEditor.getEditorInput());
		doc.replace(0, 1, "  ");
		fActivePage.saveEditor(editor, false);

		processEvents();
		assertTrue(undo.isEnabled());

		assertEquals(getMessage(), 0, logCount);
		fActivePage.hideEditor(editorRef);
		processEvents();

		assertFalse(undo.isEnabled());

		fActivePage.showEditor(editorRef);

		assertTrue(undo.isEnabled());

		assertEquals(getMessage(), 0, logCount);
	}

	/**
	 * Test opening multiple editors for an edge case: one input.
	 *
	 * openEditors(IWorkbenchPage page, IFile[] inputs)
	 */
	@Test
	public void testOpenEditors1() throws Throwable {
		proj = FileUtil.createProject("testOpenEditors");
		IFile[] inputs = new IFile[1];
		String fileName0 = "test0.txt";
		inputs[0] = FileUtil.createFile(fileName0, proj);

		// Check: editor references are returned for each file
		IEditorReference[] refs = IDE.openEditors(fActivePage, inputs);
		assertNotNull(refs);
		assertEquals(1, refs.length);
		assertNotNull(refs[0]);

		// Check: the editor is materialized
		IEditorPart editor0 = refs[0].getEditor(false);
		assertNotNull(editor0);

		// Check: the first file corresponds to the active editor
		assertEquals(fActivePage.getActiveEditor(), editor0);

		// Check: created editor match its input
		assertEquals(editor0.getSite().getId(), fWorkbench.getEditorRegistry()
				.getDefaultEditor(inputs[0].getName()).getId());

		// Check: reference's title matches the file name
		assertEquals(fileName0, refs[0].getTitle());
	}

	/**
	 * Test opening multiple editors for three inputs. Only first editor
	 * should be materialized; it also should be the active editor.
	 *
	 * openEditors(IWorkbenchPage page, IFile[] inputs)
	 */
	@Test
	public void testOpenEditors3() throws Throwable {
		proj = FileUtil.createProject("testOpenEditors");
		IFile[] inputs = new IFile[3];
		String fileName1 = "test1.txt";
		String fileName2 = "test2.txt";
		String fileName3 = "test3.txt";
		inputs[0] = FileUtil.createFile(fileName1, proj);
		inputs[1] = FileUtil.createFile(fileName2, proj);
		inputs[2] = FileUtil.createFile(fileName3, proj);

		// Check: editor references are returned for each file
		IEditorReference[] refs = IDE.openEditors(fActivePage, inputs);
		assertNotNull(refs);
		assertEquals(3, refs.length);
		assertNotNull(refs[0]);
		assertNotNull(refs[1]);
		assertNotNull(refs[2]);

		// Check: the first file got an editor materialized, rest of the files did not
		IEditorPart editor0 = refs[0].getEditor(false);
		assertNotNull(editor0);
		assertNull(refs[1].getEditor(false));
		assertNull(refs[2].getEditor(false));

		// Check: the first file corresponds to the active editor
		assertEquals(fActivePage.getActiveEditor(), editor0);

		// Check: created editors match their inputs
		assertEquals(editor0.getSite().getId(), fWorkbench.getEditorRegistry()
				.getDefaultEditor(inputs[0].getName()).getId());

		// Check: rest of the editors can be materialized
		IEditorPart editor1 = refs[1].getEditor(true);
		assertNotNull(editor1);

		// Check: those editors match their inputs too
		assertEquals(editor1.getSite().getId(), fWorkbench.getEditorRegistry()
				.getDefaultEditor(inputs[1].getName()).getId());

		// Check: reference's title matches the file name
		assertEquals(fileName1, refs[0].getTitle());
		assertEquals(fileName2, refs[1].getTitle());
		assertEquals(fileName3, refs[2].getTitle());
	}

	/**
	 * Test editor reuse when opening multiple editors. The internal editors
	 * with matching {id, input} should be reused.
	 *
	 * openEditors(IWorkbenchPage page, IFile[] inputs)
	 */
	@Test
	@Ignore
	public void testOpenEditorsReuse() throws Throwable {
		proj = FileUtil.createProject("testOpenEditors");

		String fileName1 = "test1.txt";
		String fileName2 = "test2.txt";
		String fileName3 = "test3.txt";
		int flag = IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID; // use both matches

		// open three files
		IFile[] inputs = new IFile[3];
		inputs[0] = FileUtil.createFile(fileName1, proj);
		inputs[1] = FileUtil.createFile(fileName2, proj);
		inputs[2] = FileUtil.createFile(fileName3, proj);
		IEditorReference[] refs = IDE.openEditors(fActivePage, inputs);

		// open two of the same files in mixed order, 1st (materialized) and 3rd (not materialized)
		String editorID = fWorkbench.getEditorRegistry().getDefaultEditor(inputs[0].getName()).getId();
		IEditorInput[] inputs2 = new IEditorInput[] {
				new FileEditorInput(inputs[1]),
				new FileEditorInput(inputs[0]) };
		String[] editorIDs2 = new String [] { editorID, editorID} ;

		IEditorReference[] refs2 = fActivePage.openEditors(inputs2, editorIDs2, flag);
		assertNotNull(refs2);
		assertEquals(2, refs2.length);

		// now input1 is materialized and has focus
		IEditorPart editor = refs2[0].getEditor(false);
		assertNotNull(editor);
		assertEquals(fActivePage.getActiveEditor(), editor);

		// check that the same editor was created
		assertEquals(refs2[0].getEditor(true), refs[1].getEditor(true));
		assertEquals(refs2[1].getEditor(true), refs[0].getEditor(true));

		// open a file with different editor IDs, materialized (input0) and non-materialzed (input3)
		String editorIDAlt = fWorkbench.getEditorRegistry().getDefaultEditor("abc.log").getId();
		IEditorInput[] inputs3 = new IEditorInput[] {
				new FileEditorInput(inputs[0]),
				new FileEditorInput(inputs[2]) };
		String[] editorIDs3 = new String [] { editorIDAlt, editorIDAlt} ;

		IEditorReference[] refs3 = fActivePage.openEditors(inputs3, editorIDs3, flag);
		assertNotNull(refs3);
		assertEquals(2, refs3.length);

		assertFalse(refs2[0].equals(refs[0]));
		assertFalse(refs2[1].equals(refs[2]));
	}

	/**
	 * A generic test to validate IWorkbenchPage's
	 * {@link IWorkbenchPage#setPartState(IWorkbenchPartReference, int)
	 * setPartState(IWorkbenchPartReference, int)} method which ensures the
	 * prevention of regressing on bug 209333.
	 */
	@Test
	public void testSetPartState() throws Exception {
		processEvents();
		// show a view
		IViewPart view = fActivePage.showView(MockViewPart.ID);
		processEvents();

		// now minimize it
		IViewReference reference = (IViewReference) fActivePage
				.getReference(view);
		fActivePage.setPartState(reference, IWorkbenchPage.STATE_MINIMIZED);
		processEvents();

		// since it's minimized
		assertTrue("This view should be minimized", APITestUtils.isViewMinimized(reference));
		// for whatever reason this view is still active, and active views
		// aren't recognized
		// as hidden even if they *are* physically invisible.
		assertTrue("Minimized but active view should be visible", fActivePage.isPartVisible(view));

		// try to restore it
		fActivePage.setPartState(reference, IWorkbenchPage.STATE_RESTORED);
		processEvents();

		// since it's maximized
		assertFalse("This view should not be restored", APITestUtils.isViewMinimized(reference));
		assertTrue("Restored view should be visible", fActivePage.isPartVisible(view));
	}

	/**
	 * Create and hide a single editor in a new window.  Close the window.
	 * Make sure there are no editor errors.
	 */
	@Test
	@Ignore
	public void testOpenAndHideEditor11() throws Exception {
		proj = FileUtil.createProject("testOpenAndHideEditor");
		IFile file1 = FileUtil.createFile("a.mock1", proj);

		IWorkbenchWindow window = openTestWindow();
		IWorkbenchPage page = window.getActivePage();

		IEditorPart editor = IDE.openEditor(page, file1);
		assertTrue(editor instanceof MockEditorPart);
		IEditorReference editorRef = (IEditorReference) page
				.getReference(editor);
		page.hideEditor(editorRef);
		assertEquals(0, page.getEditorReferences().length);
		page.showEditor(editorRef);
		assertEquals(1, page.getEditorReferences().length);
		page.hideEditor(editorRef);
		processEvents();
		window.close();
		processEvents();
		assertEquals(getMessage(), 0, logCount);
		assertEquals(0, page.getEditorReferences().length);
	}

	@Test
	public void testGetViewStackWithoutSecondaryId() throws PartInitException {
		IViewPart part = fActivePage.showView(MockViewPart.ID);
		assertEquals(MockViewPart.ID, part.getViewSite().getId());
		assertNull(part.getViewSite().getSecondaryId());

		IViewPart[] stack = fActivePage.getViewStack(part);
		assertEquals(1, stack.length);

		assertEquals(part, stack[0]);
	}

	@Test
	public void testGetViewStackWithSecondaryId() throws PartInitException {
		IViewPart part = fActivePage.showView(MockViewPart.ID, "1", IWorkbenchPage.VIEW_CREATE);
		assertEquals(MockViewPart.ID, part.getViewSite().getId());
		assertEquals("1", part.getViewSite().getSecondaryId());

		IViewPart[] stack = fActivePage.getViewStack(part);
		assertEquals(1, stack.length);

		assertEquals(part, stack[0]);
	}

	private static class ShellStateListener implements ShellListener {
		private final AtomicBoolean shellIsActive;

		public ShellStateListener(AtomicBoolean shellIsActive) {
			this.shellIsActive = shellIsActive;
		}

		@Override
		public void shellIconified(ShellEvent e) {
			shellIsActive.set(false);
		}

		@Override
		public void shellDeiconified(ShellEvent e) {
			shellIsActive.set(true);
		}

		@Override
		public void shellDeactivated(ShellEvent e) {
			shellIsActive.set(false);
		}

		@Override
		public void shellClosed(ShellEvent e) {
			shellIsActive.set(false);
		}

		@Override
		public void shellActivated(ShellEvent e) {
			shellIsActive.set(true);
		}
	}

}
