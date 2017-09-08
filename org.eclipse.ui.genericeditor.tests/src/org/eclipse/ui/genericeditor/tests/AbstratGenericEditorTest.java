/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sopot Cela (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import java.io.ByteArrayInputStream;

import org.junit.After;
import org.junit.Before;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

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
		file = project.getFile("foo.txt");
		file.create(new ByteArrayInputStream("bar 'bar'".getBytes()), true, null);
		editor = (ExtensionBasedTextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().openEditor(new FileEditorInput(this.file), "org.eclipse.ui.genericeditor.GenericEditor");
	 }

	@After
	public void tearDown() throws Exception {
		if (file != null) {
			file.delete(true, null);
		}
		if (project != null) {
			project.delete(true, null);
		}
	}

	private static void closeIntro() {
		IIntroPart intro = PlatformUI.getWorkbench().getIntroManager().getIntro();
		if (intro != null) {
			PlatformUI.getWorkbench().getIntroManager().closeIntro(intro);
		}
	}
	
	public static void waitAndDispatch(long milliseconds) {
		long timeout = milliseconds; //ms
		long start = System.currentTimeMillis();
		while (start + timeout > System.currentTimeMillis()) {
			Display.getDefault().readAndDispatch();
		}
	}

}
