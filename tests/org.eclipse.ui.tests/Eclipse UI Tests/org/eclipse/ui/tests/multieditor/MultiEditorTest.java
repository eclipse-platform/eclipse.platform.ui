/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.ui.tests.multieditor;

import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPartReference;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.MultiEditor;
import org.eclipse.ui.part.MultiEditorInput;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.api.MockEditorPart;
import org.junit.Before;
import org.junit.Test;

/**
 * Test MultiEditor behaviour to highlight some of the broken functionality.
 *
 * @since 3.1
 */
public class MultiEditorTest {
	private static final String ACTION_TOOLTIP = "MultiEditorActionThing";

	private static final String PROJECT_NAME = "TiledEditorProject";

	private static final String CONTENT_OUTLINE = "org.eclipse.ui.views.ContentOutline";

	private static final String TESTEDITOR_COOLBAR = "org.eclipse.ui.tests.multieditor.actionSet";

	private static final String TILED_EDITOR_ID = "org.eclipse.ui.tests.multieditor.TiledEditor";

	// tiled editor test files
	private static final String DATA_FILES_DIR = "/data/org.eclipse.newMultiEditor/";

	private static final String TEST01_TXT = "test01.txt";

	private static final String TEST02_TXT = "test02.txt";

	private static final String TEST03_ETEST = "test03.etest";

	private static final String TEST04_PROPERTIES = "test04.properties";

	private static final String BUILD_XML = "build.xml";

	//
	// call trace for the editor open - setFocus - close test
	//
	private static String[] gEditorOpenTrace = { "setInitializationData",
			"init", "createPartControl", "createInnerPartControl",
			"createInnerPartControl", "setFocus", "updateGradient",
			"updateGradient", };

	private static String[] gEditorFocusTrace = { "setInitializationData",
			"init", "createPartControl", "createInnerPartControl",
			"createInnerPartControl", "setFocus", "updateGradient",
			"updateGradient", "updateGradient", "updateGradient", };

	private static String[] gEditorCloseTrace = { "setInitializationData",
			"init", "createPartControl", "createInnerPartControl",
			"createInnerPartControl", "setFocus", "updateGradient",
			"updateGradient", "updateGradient", "updateGradient",
			"widgetsDisposed", "dispose" };

	/**
	 * Can catch a MultiEditor unexpect Exception on init.
	 */
	private EditorErrorListener fErrorListener;

	/**
	 * Test that the test tiled editor can be opened with a basic
	 * MultiEditorInput with the same type of files.
	 *
	 * Test: Select a couple of files from navigator and use the TiledEditor
	 * menu to open the editor. It should open with the first selected file on
	 * top.
	 *
	 * @throws Throwable
	 *             on an error
	 */
	@Test
	public void testOpenBasicEditor() throws Throwable {
		final String[] simpleFiles = { TEST01_TXT, TEST02_TXT };

		IWorkbenchWindow window = openTestWindow();
		IWorkbenchPage page = window.getActivePage();

		IProject testProject = findOrCreateProject(PROJECT_NAME);

		MultiEditorInput input = generateEditorInput(simpleFiles, testProject);

		// validate there are no NullPointerExceptions during editor
		// initialization
		openAndValidateEditor(page, input);
	}

	/**
	 * Test that the public methods in TiledEditor (and MultiEditor) are called
	 * in the correct order from 3.0 to 3.1.
	 *
	 * Test: this test involves opening the tiled editor on 2 files, changing
	 * the focus from the first file to the second file, and closing the tiled
	 * editor.
	 */
	@Test
	public void testOpenTestFile() throws Throwable {
		final String[] simpleFiles = { TEST01_TXT, TEST03_ETEST };

		IWorkbenchWindow window = openTestWindow();
		WorkbenchPage page = (WorkbenchPage) window.getActivePage();

		IProject testProject = findOrCreateProject(PROJECT_NAME);

		MultiEditorInput input = generateEditorInput(simpleFiles, testProject);

		// catches the framework NPE
		IEditorPart editor = openAndValidateEditor(page, input);

		// did we get a multieditor back?
		assertTrue(editor instanceof MultiEditor);
		MultiEditor multiEditor = (MultiEditor) editor;

		chewUpEvents();

		// listHistory(((TiledEditor) multiEditor).callHistory);

		// check the public API called for opening the TiledEditor
		// ((TiledEditor) multiEditor).callHistory.printToConsole();
		assertTrue("The editor open trace was incorrect",
				((TiledEditor) multiEditor).callHistory
						.verifyOrder(gEditorOpenTrace));

		// swap focus to the last editor, which is the test editor
		// with the test coolbar contribution
		IEditorPart[] innerEditors = multiEditor.getInnerEditors();
		innerEditors[innerEditors.length - 1].setFocus();

		chewUpEvents();

		// ((TiledEditor) multiEditor).callHistory.printToConsole();
		assertTrue("Editor setFocus trace was incorrect",
				((TiledEditor) multiEditor).callHistory
						.verifyOrder(gEditorFocusTrace));

		page.closeEditor(multiEditor, false);

		chewUpEvents();

		// ((TiledEditor) multiEditor).callHistory.printToConsole();
		assertTrue("Editor close trace was incorrect",
				((TiledEditor) multiEditor).callHistory
						.verifyOrder(gEditorCloseTrace));
	}
	@Test
	public void testDirty() throws Throwable {
		IWorkbenchWindow window = openTestWindow();
		WorkbenchPage page = (WorkbenchPage) window.getActivePage();
		page.closeAllEditors(false);

		IProject testProject = findOrCreateProject(PROJECT_NAME);

		IFile fileA = createFile(testProject, TEST01_TXT);
		IFile fileB = createFile(testProject, TEST02_TXT);

		MultiEditorInput input = new MultiEditorInput(new String[] { MockEditorPart.ID1, MockEditorPart.ID1}, new IEditorInput[] { new FileEditorInput(fileA),new FileEditorInput(fileB)});

		// catches the framework NPE
		IEditorPart editor = openAndValidateEditor(page, input);

		// did we get a multieditor back?
		assertTrue(editor instanceof MultiEditor);
		MultiEditor multiEditor = (MultiEditor) editor;

		CTabFolder tabFolder = (CTabFolder) ((WorkbenchPartReference) page
				.getReference(multiEditor)).getModel().getParent().getWidget();
		// index is 0 since we want the editor that's behind in the editor
		// stack, 1 should be the second one that we activated
		CTabItem item = tabFolder.getItem(0);

		IEditorPart[] innerEditors = multiEditor.getInnerEditors();
		MockEditorPart editorA = (MockEditorPart) innerEditors[0];
		MockEditorPart editorB = (MockEditorPart) innerEditors[0];

		char firstChar = item.getText().charAt(0);
		assertFalse(firstChar == '*');

		try {
			editorA.setDirty(true);
			assertEquals('*', item.getText().charAt(0));

			editorA.setDirty(false);
			assertEquals(firstChar, item.getText().charAt(0));

			editorB.setDirty(true);
			assertEquals('*', item.getText().charAt(0));

			editorB.setDirty(false);
			assertEquals(firstChar, item.getText().charAt(0));
		} finally {
			page.closeAllEditors(false);
		}
	}

	/**
	 * Test that coolbar items in the workbench are updated when focus moves
	 * through the different inner editors ... this test as written is not 100%
	 * accurate, as the items are enabled.
	 *
	 * Test: Open two files where the first is a text file and the second is of
	 * type etest. Change focus to the etest file, and the coolbar should update
	 * with a new action icon and it should be enabled.
	 *
	 * @throws Throwable
	 *             on an error
	 */
	@Test
	public void testTrackCoolBar() throws Throwable {
		final String[] simpleFiles = { TEST01_TXT, TEST02_TXT,
				TEST04_PROPERTIES, BUILD_XML, TEST03_ETEST };

		IWorkbenchWindow window = openTestWindow();
		WorkbenchPage page = (WorkbenchPage) window.getActivePage();

		IProject testProject = findOrCreateProject(PROJECT_NAME);

		MultiEditorInput input = generateEditorInput(simpleFiles, testProject);

		// catches the framework NPE
		IEditorPart editor = openAndValidateEditor(page, input);

		// did we get a multieditor back?
		assertTrue(editor instanceof MultiEditor);
		MultiEditor multiEditor = (MultiEditor) editor;

		chewUpEvents();

		// get access to the appropriate coolbar
		IContributionItem contribution = findMyCoolBar(page);

		// our test editor contribution should not be visible
		// but it should be enabled
		validateIconState(contribution, ACTION_TOOLTIP, false);

		// swap focus to the last editor, which is the test editor
		// with the test coolbar contribution
		IEditorPart[] innerEditors = multiEditor.getInnerEditors();
		innerEditors[innerEditors.length - 1].setFocus();

		chewUpEvents();

		contribution = findMyCoolBar(page);
		assertNotNull("It should be available now", contribution);

		// our test editor contribution should now be visible and
		// enabled
		validateIconState(contribution, ACTION_TOOLTIP, true);

	}

	/**
	 * Test that the outline view is updated when focus moves from an editor to
	 * the ant editor.
	 *
	 * Test: Open 2 files where the first is a text file and the second is an
	 * ant file. Set focus on the ant file, and the outline should be updated to
	 * reflect the buildfile outline.
	 *
	 * @throws Throwable
	 *             on an error
	 */
	public void xtestTrackOutline() throws Throwable {
		final String[] simpleFiles = { TEST01_TXT, TEST02_TXT,
				TEST04_PROPERTIES, BUILD_XML, TEST03_ETEST };

		IWorkbenchWindow window = openTestWindow();
		WorkbenchPage page = (WorkbenchPage) window.getActivePage();

		IProject testProject = findOrCreateProject(PROJECT_NAME);

		MultiEditorInput input = generateEditorInput(simpleFiles, testProject);

		// catches the framework NPE
		IEditorPart editor = openAndValidateEditor(page, input);

		// did we get a multieditor back?
		assertTrue(editor instanceof MultiEditor);
		MultiEditor multiEditor = (MultiEditor) editor;

		chewUpEvents();

		// Swap to the second last editor, which should be the ant
		// build editor.
		IEditorPart[] innerEditors = multiEditor.getInnerEditors();
		innerEditors[innerEditors.length - 2].setFocus();
		chewUpEvents();

		// get the outline view part
		IViewPart outline = window.getActivePage().showView(CONTENT_OUTLINE);
		assertNotNull(outline);

		// find out who is contributing the outline view.
		IContributedContentsView view = outline.getAdapter(IContributedContentsView.class);
		IWorkbenchPart part = view.getContributingPart();
		assertNotNull("The Outline view has not been updated by the editor",
				part);
		assertTrue("The Outline view is not talking to an editor",
				part instanceof IEditorPart);

		IEditorPart outlineEditor = (IEditorPart) part;

		// the active inner editor (the ant editor) should also
		// be the outline editor contributor ... this works in
		// 3.0, fails in 3.1
		assertEquals("The Outline view is not talking to the correct editor",
				multiEditor.getActiveEditor(), outlineEditor);

		page.closeEditor(editor, false);
		chewUpEvents();

		view = outline.getAdapter(IContributedContentsView.class);
		assertNull(view.getContributingPart());
	}

	/**
	 * Return the test editor coolbar.
	 *
	 * @param page
	 *            the workbench page
	 * @return the IContributionItem for the test editor cool bar.
	 */
	private IContributionItem findMyCoolBar(WorkbenchPage page) {
		return ((IActionBars2) page.getActionBars())
				.getCoolBarManager().find(TESTEDITOR_COOLBAR);
	}

	/**
	 * Validate the state of an icon in the toolbar.
	 *
	 * @param contribution
	 *            the high level contribution from the coolbar to look through
	 * @param tooltip
	 *            the string that matches the action's tooltip
	 * @param state
	 *            should it be true or false
	 */
	private void validateIconState(IContributionItem contribution,
			String tooltip, boolean state) {
		assertTrue("We might not have the contribution or expect it",
				contribution != null || !state);
		if (contribution == null) {
			return;
		}

		ToolBarManager toolBarManager = (ToolBarManager) ((ToolBarContributionItem) contribution)
				.getToolBarManager();
		ToolBar bar = toolBarManager.getControl();

		assertTrue("It's OK for bar to be null if we expect state to be false",
				bar != null || !state);
		if (bar == null) {
			return;
		}

		ToolItem[] items = bar.getItems();
		for (ToolItem item : items) {
			// System.err.println("Item: " + items[i].getToolTipText());
			if (tooltip.equals(item.getToolTipText())) {
				assertEquals("Invalid icon state for " + tooltip, state,
						item.getEnabled());
				return;
			}
		}
		assertFalse("We haven't found our item", state);
	}

	/**
	 * Create the project to work in. If it already exists, just open it.
	 *
	 * @param projectName
	 *            the name of the project to create
	 * @return the newly opened project
	 */
	private IProject findOrCreateProject(String projectName)
			throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject testProject = workspace.getRoot().getProject(projectName);
		if (!testProject.exists()) {
			testProject.create(null);
		}
		testProject.open(null);
		return testProject;
	}

	/**
	 * After an internal action, see if there are any outstanding SWT events.
	 */
	private void chewUpEvents() throws InterruptedException {
		Thread.sleep(500);
		Display display = Display.getCurrent();
		while (display.readAndDispatch()) {
		}
	}

	/**
	 * Open the test editor. It does basic validation that there is no
	 * NullPointerException during initialization.
	 *
	 * @param page
	 *            the workbench page
	 * @param input
	 *            the editor input with multiple files
	 * @return the MultiEditor
	 */
	private IEditorPart openAndValidateEditor(IWorkbenchPage page,
			MultiEditorInput input) throws PartInitException {

		IEditorPart editorPart = null;
		try {
			setupErrorListener();
			editorPart = page
					.openEditor(input, MultiEditorTest.TILED_EDITOR_ID);
			assertNotNull(editorPart);

			// 3.1.0 only
			// assertFalse("The editor never actualized",
			// editorPart instanceof ErrorEditorPart);

			if (!fErrorListener.messages.isEmpty()) {
				String[] msgs = fErrorListener.messages
						.toArray(new String[fErrorListener.messages.size()]);
				for (String msg : msgs) {
					if (!msg.contains("The proxied handler for")
							&& !msg.contains("Conflict for \'")
							&& !msg.contains("Keybinding conflicts occurred")
							&& !msg.contains("A handler conflict occurred")) {
						fail("Failed with: " + msg);
					}
				}
			}
		} finally {
			removeErrorListener();
		}
		return editorPart;
	}

	/**
	 * Set up to catch any editor initialization exceptions.
	 */
	private void setupErrorListener() {
		final ILog log = WorkbenchPlugin.getDefault().getLog();
		fErrorListener = new EditorErrorListener();
		log.addLogListener(fErrorListener);
	}

	/**
	 * Remove the editor error listener.
	 */
	private void removeErrorListener() {
		final ILog log = WorkbenchPlugin.getDefault().getLog();
		if (fErrorListener != null) {
			log.removeLogListener(fErrorListener);
			fErrorListener = null;
		}
	}

	/**
	 * Create the multi editor input in the given project. Creates the files in
	 * the project from template files in the classpath if they don't already
	 * exist.
	 *
	 * @param simpleFiles
	 *            the array of filenames to copy over
	 * @param testProject
	 *            the project to create the files in
	 * @return the editor input used to open the multieditor
	 */
	private MultiEditorInput generateEditorInput(String[] simpleFiles,
			IProject testProject) throws CoreException, IOException {
		String[] ids = new String[simpleFiles.length];
		IEditorInput[] inputs = new IEditorInput[simpleFiles.length];
		IEditorRegistry registry = getWorkbench().getEditorRegistry();

		for (int f = 0; f < simpleFiles.length; ++f) {
			IFile f1 = createFile(testProject, simpleFiles[f]);
			ids[f] = registry.getDefaultEditor(f1.getName()).getId();
			inputs[f] = new FileEditorInput(f1);
		}

		return new MultiEditorInput(ids, inputs);
	}

	private IFile createFile(IProject testProject, String simpleFile) throws CoreException, IOException {
		IFile file = testProject.getFile(simpleFile);
		if (!file.exists()) {
			URL url = FileLocator.toFileURL(TestPlugin.getDefault()
					.getBundle().getEntry(DATA_FILES_DIR + simpleFile));
			file.create(url.openStream(), true, null);
		}
		return file;
	}

	/**
	 * Close any editors at the beginner of a test, so the test can be clean.
	 */
	@Before
	public void doSetUp() throws Exception {
		IWorkbenchPage page = getWorkbench().getActiveWorkbenchWindow()
				.getActivePage();
		page.closeAllEditors(false);

	}

	/**
	 * Listens for the standard message that indicates the MultiEditor failed
	 * ... usually caused by incorrect framework initialization that doesn't set
	 * the innerChildren.
	 *
	 * @since 3.1
	 */
	public static class EditorErrorListener implements ILogListener {

		public ArrayList<String> messages = new ArrayList<>();

		@Override
		public void logging(IStatus status, String plugin) {
			String msg = status.getMessage();
			Throwable ex = status.getException();
			if (ex != null) {
				msg += ": " + ex.getMessage();
			}
			messages.add(msg);
		}
	}
}
