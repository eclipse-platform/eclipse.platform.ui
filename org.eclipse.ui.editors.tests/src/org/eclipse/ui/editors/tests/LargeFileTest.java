/*******************************************************************************
 * Copyright (c) 2019 Thomas Wolf and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.editors.tests;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.filebuffers.tests.ResourceHelper;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.intro.IIntroPart;

import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Tests opening large files in a text editor.
 */
public class LargeFileTest {

	private IFile fLargeFile;

	private IPreferenceStore preferenceStore;

	boolean initialWordWrap;

	@Before
	public void setUp() throws Exception {
		IIntroPart intro = PlatformUI.getWorkbench().getIntroManager().getIntro();
		if (intro != null) {
			PlatformUI.getWorkbench().getIntroManager().closeIntro(intro);
		}
		IFolder folder = ResourceHelper.createFolder("LargeFileTestProject/test/");
		fLargeFile = ResourceHelper.createFile(folder, "large.txt", "");
		IFileStore store = EFS.getLocalFileSystem().getStore(fLargeFile.getLocationURI());
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(store.openOutputStream(EFS.NONE, null), StandardCharsets.UTF_8))) {
			for (int i = 1; i <= 40000; i++) {
				writer.write("This is line # " + i);
				writer.newLine();
			}
		}
		fLargeFile.refreshLocal(IResource.DEPTH_ZERO, null);
		preferenceStore = EditorsPlugin.getDefault().getPreferenceStore();
		initialWordWrap = preferenceStore.getBoolean(AbstractTextEditor.PREFERENCE_WORD_WRAP_ENABLED);
	}

	@After
	public void tearDown() throws Exception {
		fLargeFile.deleteMarkers(IMarker.BOOKMARK, true, IResource.DEPTH_INFINITE);
		ResourceHelper.deleteProject("LargeFileTestProject");
		fLargeFile= null;
		preferenceStore.setValue(AbstractTextEditor.PREFERENCE_WORD_WRAP_ENABLED, initialWordWrap);
		TestUtil.cleanUp();
	}

	@Test
	public void openLargeFileWithAnnotation() throws Exception {
		IWorkbench workbench= PlatformUI.getWorkbench();
		Display display = workbench.getDisplay();
		IWorkbenchPage page= workbench.getActiveWorkbenchWindow().getActivePage();
		// Set word-wrap (makes the StyledText have variable line height)
		preferenceStore.setValue(AbstractTextEditor.PREFERENCE_WORD_WRAP_ENABLED, true);
		// Let's get an idea how long it takes to open the file at all. Open the
		// file twice; the first time takes longer.
		long[] baseline = new long[1];
		for (int i = 0; i < 2; i++) {
			baseline[0] = System.nanoTime();
			IEditorPart part = IDE.openEditor(page, fLargeFile);
			display.asyncExec(() -> {
				baseline[0] = System.nanoTime() - baseline[0];
			});
			TestUtil.runEventLoop();
			assertTrue("Expected a text editor", part instanceof ITextEditor);
			page.closeEditor(part, false);
			TestUtil.runEventLoop();
		}
		// Create a marker on the file
		Map<String, Object> attributes = Map.of(IMarker.LOCATION, "line 1", //
				IMarker.MESSAGE, "Just a test marker", //
				IMarker.LINE_NUMBER, 1, //
				IMarker.CHAR_START, 8, //
				IMarker.CHAR_END, 12 ,//
				IMarker.USER_EDITABLE, false
				);

		fLargeFile.createMarker(IMarker.BOOKMARK, attributes);
		TestUtil.runEventLoop();
		// Open it again
		long[] withMarker = { System.nanoTime() };
		IEditorPart part = IDE.openEditor(page, fLargeFile);
		display.asyncExec(() -> {
			withMarker[0] = System.nanoTime() - withMarker[0];
		});
		TestUtil.runEventLoop();
		assertTrue("Expected a text editor", part instanceof ITextEditor);
		page.closeEditor(part, false);
		TestUtil.runEventLoop();
		// Be generous here; all this timing is approximate. Fail if the attempt
		// with the marker takes more than twice as long. If the OverviewRuler
		// is badly implemented, opening with the marker will take much longer.
		assertTrue("Opening large file took too long: " + (withMarker[0] / 1000000.0f) + "ms with marker vs. "
				+ (baseline[0] / 1000000.0f) + "ms without",
				withMarker[0] / 2 <= baseline[0]);
	}
}
