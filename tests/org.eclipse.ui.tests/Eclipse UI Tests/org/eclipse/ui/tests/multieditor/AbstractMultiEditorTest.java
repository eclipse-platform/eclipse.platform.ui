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

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiEditorInput;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AbstractMultiEditorTest {

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	private static final String PROJECT_NAME = "TiledEditorProject";

	private static final String TILED_EDITOR_ID = "org.eclipse.ui.tests.multieditor.ConcreteAbstractMultiEditor";

	// tiled editor test files
	private static final String DATA_FILES_DIR = "/data/org.eclipse.newMultiEditor/";

	private static final String TEST01_TXT = "test01.txt";

	private static final String TEST03_ETEST = "test03.etest";

	private EditorErrorListener fErrorListener;

	@Test
	public void testBug317102() throws Throwable {
		final String[] simpleFiles = { TEST01_TXT, TEST03_ETEST };

		IWorkbenchWindow window = openTestWindow();
		WorkbenchPage page = (WorkbenchPage) window.getActivePage();

		IProject testProject = findOrCreateProject(PROJECT_NAME);

		MultiEditorInput input = generateEditorInput(simpleFiles, testProject);

		IEditorPart editor = page.openEditor(input,
				AbstractMultiEditorTest.TILED_EDITOR_ID);

		// did we get a multieditor back?
		assertTrue(editor instanceof ConcreteAbstractMultiEditor);
		ConcreteAbstractMultiEditor multiEditor = (ConcreteAbstractMultiEditor) editor;

		IEditorPart[] innerEditors = multiEditor.getInnerEditors();
		IEditorPart activeEditor = multiEditor.getActiveEditor();
		assertEquals(activeEditor, innerEditors[0]);
		multiEditor.activateEditor(innerEditors[1]);

		// activate another view
		IViewPart view = page.showView(IPageLayout.ID_PROBLEM_VIEW);
		assertEquals(view, page.getActivePart());

		fakeActivation(innerEditors[0]);
	}

	private void fakeActivation(IWorkbenchPart part) {
		try {
			setupErrorListener();
			Event event = new Event();
			event.type = SWT.Activate;
			((Control) ((PartSite) part.getSite()).getModel().getWidget())
					.setFocus();

			assertTrue("Nothing should have been logged",
					fErrorListener.messages.isEmpty());
		} finally {
			removeErrorListener();
		}
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
		IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();

		for (int f = 0; f < simpleFiles.length; ++f) {
			IFile f1 = createFile(testProject, simpleFiles[f]);
			ids[f] = registry.getDefaultEditor(f1.getName()).getId();
			inputs[f] = new FileEditorInput(f1);
		}

		return new MultiEditorInput(ids, inputs);
	}

	/**
	 * Create the project to work in. If it already exists, just open it.
	 *
	 * @param projectName
	 *            the name of the project to create
	 * @return the newly opened project
	 */
	private static IProject findOrCreateProject(String projectName)
			throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject testProject = workspace.getRoot().getProject(projectName);
		if (!testProject.exists()) {
			testProject.create(null);
		}
		testProject.open(null);
		return testProject;
	}

	private static IFile createFile(IProject testProject, String simpleFile)
			throws CoreException, IOException {
		IFile file = testProject.getFile(simpleFile);
		if (!file.exists()) {
			URL url = FileLocator.toFileURL(TestPlugin.getDefault().getBundle()
					.getEntry(DATA_FILES_DIR + simpleFile));
			file.create(url.openStream(), true, null);
		}
		return file;
	}

	/**
	 * Close any editors at the beginner of a test, so the test can be clean.
	 */
	@Before
	public void doSetUp() throws Exception {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
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
