/*******************************************************************************
 * Copyright (c) 2021 Red Hat Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.editors.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IMultiTextSelection;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.MultiTextSelection;
import org.eclipse.jface.text.Region;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.part.FileEditorInput;

import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

public class CaseActionTest {

	private static IProject project;
	private static IFile file;
	private AbstractTextEditor editor;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		project = ResourcesPlugin.getWorkspace().getRoot().getProject("test");
		project.create(new NullProgressMonitor());
		project.open(new NullProgressMonitor());
		file = project.getFile("foo.txt");
		file.create(new ByteArrayInputStream("bar".getBytes()), true, new NullProgressMonitor());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		file.delete(true, new NullProgressMonitor());
		project.delete(true, new NullProgressMonitor());
		TestUtil.cleanUp();
	}

	@Before
	public void setUp() throws Exception {
		IIntroPart intro = PlatformUI.getWorkbench().getIntroManager().getIntro();
		if (intro != null) {
			PlatformUI.getWorkbench().getIntroManager().closeIntro(intro);
		}

		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
		editor = (AbstractTextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().openEditor(new FileEditorInput(file), desc.getId());
		editor.setFocus();
		// make sure we start from a clean state
	}

	@After
	public void tearDown() throws Exception {
		editor.close(false);
		editor= null;
	}

	@Test
	public void testMultiSelectionCase() {
		IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		doc.set("foo bar foo");
		IRegion[] initialSelection = { new Region(0,3), new Region(8, 3) };
		editor.getSelectionProvider().setSelection(new MultiTextSelection(doc, initialSelection));
		editor.getAction(ITextEditorActionConstants.UPPER_CASE).run();
		assertEquals("FOO bar FOO", doc.get());
		assertArrayEquals(initialSelection,
				((IMultiTextSelection) editor.getSelectionProvider().getSelection()).getRegions());
		//
		doc.set("foß bar fßo bar ßoo");
		editor.getSelectionProvider().setSelection(
				new MultiTextSelection(doc, new IRegion[] { new Region(0, 3), new Region(8, 3), new Region(16, 3) }));
		editor.getAction(ITextEditorActionConstants.UPPER_CASE).run();
		assertEquals("FOSS bar FSSO bar SSOO", doc.get());
		assertArrayEquals(new IRegion[] { //
				new Region(0, 4), //
				new Region(9, 4), //
				new Region(18, 4) //
		}, ((IMultiTextSelection) editor.getSelectionProvider().getSelection()).getRegions());
	}

}
