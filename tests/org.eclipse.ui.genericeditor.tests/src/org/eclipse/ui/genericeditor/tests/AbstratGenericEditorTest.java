/*******************************************************************************
 * Copyright (c) 2016, 2025 Red Hat Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Sopot Cela (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import static org.eclipse.ui.tests.harness.util.DisplayHelper.runEventLoop;
import static org.eclipse.ui.tests.harness.util.UITestUtil.forceActive;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.eclipse.ui.tests.harness.util.UITestUtil.waitForJobs;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextEditor;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Closes intro, create {@link #project}, create {@link #file} and open {@link #editor}; and clean up.
 * Also contains additional utility methods
 * @since 1.0
 */
public class AbstratGenericEditorTest {

	protected IProject project;
	protected IFile file;
	protected ExtensionBasedTextEditor editor;
	protected IWorkbenchWindow window;

	/**
	 * Closes intro, create {@link #project}, create {@link #file} and open {@link #editor}
	 * @throws Exception ex
	 */
	@Before
	public void setUp() throws Exception {
		closeIntro();
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		project.create(null);
		project.open(null);
		project.setDefaultCharset(StandardCharsets.UTF_8.name(), null);
		waitForJobs(100, 5000);
		window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		forceActive(window.getShell());
		createAndOpenFile();
	 }

	protected void createAndOpenFile() throws Exception {
		createAndOpenFile("foo.txt", "bar 'bar'");
	}

	/**
	 * Creates a new file in the project, opens it, and associate that file with the test state
	 * @param name name of the file in the project
	 * @param contents content of the file
	 * @throws Exception ex
	 * @since 1.1
	 */
	protected void createAndOpenFile(String name, String contents) throws Exception {
		createAndOpenFile(name, contents, () -> new FileEditorInput(file));
	}

	/**
	 * Creates a new file in the project, opens it, and associate that file with the test state
	 * @param name name of the file in the project
	 * @param contents content of the file
	 * @param inputCreator creates the input for the editor
	 * @throws Exception ex
	 */
	protected void createAndOpenFile(String name, String contents, Supplier<? extends IEditorInput> inputCreator) throws Exception {
		this.file = project.getFile(name);
		this.file.create(new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8)), true, null);
		this.file.setCharset(StandardCharsets.UTF_8.name(), null);
		this.editor = (ExtensionBasedTextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().openEditor(inputCreator.get(), "org.eclipse.ui.genericeditor.GenericEditor");
		runEventLoop(PlatformUI.getWorkbench().getDisplay(),0);
	}

	/**
	 * Closes editor and delete file. Keeps project open.
	 * @throws Exception ex
	 * @since 1.1
	 */
	protected void cleanFileAndEditor() throws Exception {
		if (editor != null) {
			editor.close(false);
			editor = null;
		}
		runEventLoop(PlatformUI.getWorkbench().getDisplay(),0);
		if (file != null) {
			file.delete(true, new NullProgressMonitor());
			file = null;
		}
	}

	@After
	public void tearDown() throws Exception {
		cleanFileAndEditor();
		if (project != null) {
			project.delete(true, null);
		}
	}

	private static void closeIntro() {
		IIntroPart intro = PlatformUI.getWorkbench().getIntroManager().getIntro();
		if (intro != null) {
			PlatformUI.getWorkbench().getIntroManager().closeIntro(intro);
			runEventLoop(PlatformUI.getWorkbench().getDisplay(),0);
		}
	}

	public static void waitAndDispatch(long milliseconds) {
		long timeout = milliseconds; //ms
		long start = System.currentTimeMillis();
		while (start + timeout > System.currentTimeMillis()) {
			processEvents();
		}
	}

}
