/*******************************************************************************
 * Copyright (c) 2017, 2025 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Lucas Bullen (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import static org.eclipse.ui.tests.harness.util.DisplayHelper.runEventLoop;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import org.junit.Test;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.genericeditor.tests.contributions.EnabledPropertyTester;
import org.eclipse.ui.genericeditor.tests.contributions.ReconcilerStrategyFirst;
import org.eclipse.ui.genericeditor.tests.contributions.ReconcilerStrategySecond;
import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.DisplayHelper;

import org.eclipse.ui.texteditor.IDocumentProvider;

public class ReconcilerTest extends AbstratGenericEditorTest {

	protected ExtensionBasedTextEditor secondEditor;
	protected IFile secondFile;
	protected IProject secondProject;

	@Test
	public void testReconciler() throws Exception {
		performTestOnEditor(ReconcilerStrategyFirst.SEARCH_TERM, editor, ReconcilerStrategyFirst.REPLACEMENT);
	}

	@Test
	public void testEnabledWhenReconciler() throws Exception {
		EnabledPropertyTester.setEnabled(true);
		createAndOpenFile("enabledWhen.txt", "");
		performTestOnEditor(ReconcilerStrategyFirst.SEARCH_TERM, editor, ReconcilerStrategyFirst.REPLACEMENT);
		cleanFileAndEditor();

		EnabledPropertyTester.setEnabled(false);
		createAndOpenFile("enabledWhen.txt", "");
		performTestOnEditor(ReconcilerStrategySecond.SEARCH_TERM, editor, ReconcilerStrategySecond.REPLACEMENT);
	}

	@Test
	public void testMultipleEditors() throws Exception {
		secondProject= ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		secondProject.create(null);
		secondProject.open(null);
		secondFile= secondProject.getFile("foo.txt");
		secondFile.create(new ByteArrayInputStream("bar 'bar'".getBytes()), true, null);
		secondEditor = (ExtensionBasedTextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().openEditor(new FileEditorInput(secondFile), "org.eclipse.ui.genericeditor.GenericEditor");
		performTestOnEditor(ReconcilerStrategyFirst.SEARCH_TERM, editor, ReconcilerStrategyFirst.REPLACEMENT);
	}

	@Test
	public void testMultipleReconcilers() throws Exception {
		secondFile = project.getFile("bar.txt");
		secondFile.create(new ByteArrayInputStream("".getBytes()), true, null);
		secondEditor = (ExtensionBasedTextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().openEditor(new FileEditorInput(secondFile), "org.eclipse.ui.genericeditor.GenericEditor");
		performTestOnEditor(ReconcilerStrategyFirst.SEARCH_TERM, secondEditor, ReconcilerStrategySecond.REPLACEMENT);
	}

	private void performTestOnEditor(String startingText, ExtensionBasedTextEditor textEditor, String expectedText) throws Exception {
		IDocumentProvider dp = textEditor.getDocumentProvider();
		IDocument doc = dp.getDocument(textEditor.getEditorInput());

		doc.set(startingText);

		DisplayHelper.waitForCondition(window.getShell().getDisplay(), 2000, () -> {
			try {
				return doc.get(0, doc.getLineLength(0)).contains(expectedText);
			} catch (BadLocationException e) {
				return false;
			}
		});
		assertTrue("file was not affected by reconciler", doc.get().contains(expectedText));
	}

	@Override
	public void tearDown() throws Exception {
		if (secondEditor != null) {
			secondEditor.close(false);
			secondEditor = null;
			runEventLoop(PlatformUI.getWorkbench().getDisplay(),0);
		}
		if (secondFile != null) {
			secondFile.delete(true, new NullProgressMonitor());
			secondFile = null;
		}
		if (secondProject != null) {
			secondProject.delete(true, new NullProgressMonitor());
		}
		super.tearDown();
	}
}
