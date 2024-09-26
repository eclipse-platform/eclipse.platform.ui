/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.ResourceBundle;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextSelection;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.findandreplace.HistoryStore;

import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.FindNextAction;
import org.eclipse.ui.texteditor.FindReplaceAction;

public class FindNextActionTest {
	private static final String TEST_PROJECT_NAME = "TestProject";

	private static final String BUNDLE_FOR_CONSTRUCTED_KEYS_NAME = "org.eclipse.ui.texteditor.ConstructedEditorMessages";//$NON-NLS-1$

	private static ResourceBundle bundleForConstructedKeys = ResourceBundle.getBundle(BUNDLE_FOR_CONSTRUCTED_KEYS_NAME);

	private AbstractTextEditor editor;

	private IProject project;

	private FindNextAction action;

	private static enum Direction {
		FORWARD, BACKWARD
	}

	@Before
	public void createTestProject() throws CoreException {
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(TEST_PROJECT_NAME);
		project.create(null);
		project.open(null);
	}

	public void openEditorAndFindNextAction(String content, Direction direction) throws CoreException {
		IFile file = createTestFile(content);
		editor = openEditor(file);
		action = new FindNextAction(bundleForConstructedKeys, "findNext", editor, direction == Direction.FORWARD);
	}

	private IFile createTestFile(String content) throws CoreException {
		IFile file = project.getFile("file.txt");
		file.create(content.getBytes(), IResource.FORCE, null);
		file.setCharset(null, null);
		return file;
	}

	private static AbstractTextEditor openEditor(IFile file) throws PartInitException {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
		IEditorPart editorPart = IDE.openEditor(page, file);
		assertThat(editorPart, Matchers.instanceOf(AbstractTextEditor.class));
		return (AbstractTextEditor) editorPart;
	}

	@After
	public void tearDown() throws Exception {
		resetInitialSearchSettings();
		closeEditor(editor);
		editor = null;
		project.delete(true, null);
		project = null;
		TestUtil.cleanUp();
	}

	private void resetInitialSearchSettings() {
		IDialogSettings settings = getActionSettings();
		settings.put("isRegEx", false);
		settings.put("casesensitive", false);
		settings.put("wrap", true);
		settings.put("wholeword", false);
	}

	private static void closeEditor(IEditorPart editor) {
		IWorkbenchPartSite site;
		IWorkbenchPage page;
		if (editor != null && (site = editor.getSite()) != null && (page = site.getPage()) != null) {
			page.closeEditor(editor, false);
		}
	}

	private void setEditorSelection(int offset, int length) {
		Document document = (Document) editor.getDocumentProvider().getDocument(editor.getEditorInput());
		TextSelection selection = new TextSelection(document, offset, length);
		ISelectionProvider selectionProvider = editor.getSelectionProvider();
		selectionProvider.setSelection(selection);
	}

	private void assertSelectionIs(int offset, int length) {
		assertEquals(offset, getEditorSelection().getRegions()[0].getOffset());
		assertEquals(length, getEditorSelection().getRegions()[0].getLength());
	}

	private TextSelection getEditorSelection() {
		ISelectionProvider selectionProvider = editor.getSelectionProvider();
		if (selectionProvider.getSelection() instanceof TextSelection) {
			return (TextSelection) selectionProvider.getSelection();
		}
		return null;
	}

	private IDialogSettings getActionSettings() {
		IDialogSettings settings = PlatformUI.getDialogSettingsProvider(FrameworkUtil.getBundle(FindNextAction.class))
				.getDialogSettings();
		IDialogSettings fDialogSettings = settings.getSection(FindReplaceAction.class.getClass().getName());
		if (fDialogSettings == null)
			fDialogSettings = settings.addNewSection(FindReplaceAction.class.getClass().getName());
		return fDialogSettings;
	}

	@Test
	public void testFindNextForward() throws CoreException {
		openEditorAndFindNextAction("testtesttest", Direction.FORWARD);
		setEditorSelection(0, 4);
		action.run();
		assertSelectionIs(4, 4);
		action.run();
		assertSelectionIs(8, 4);
		action.run();
		assertSelectionIs(0, 4);
	}

	@Test
	public void testFindNextBackwards() throws CoreException {
		openEditorAndFindNextAction("testtesttest", Direction.BACKWARD);
		setEditorSelection(4, 4);
		action.run();
		assertSelectionIs(0, 4);
		action.run();
		assertSelectionIs(8, 4);
	}

	@Test
	public void testFindNextFromHistory() throws CoreException {
		openEditorAndFindNextAction("word-abcd-text", Direction.FORWARD);
		IDialogSettings settings = getActionSettings();
		HistoryStore historyStore = new HistoryStore(settings, "findhistory", 15);
		historyStore.add("abcd");
		setEditorSelection(0, 0);
		action.run();
		assertSelectionIs(5, 4);
		setEditorSelection(3, 0);
		action.run();
		assertSelectionIs(5, 4);
	}

	@Test
	public void testFindNextStoresCorrectHistory() throws CoreException {
		openEditorAndFindNextAction("history", Direction.FORWARD);
		setEditorSelection(0, "history".length());
		action.run();
		IDialogSettings settings = getActionSettings();
		HistoryStore historyStore = new HistoryStore(settings, "findhistory", 15);
		assertThat(historyStore.get(0), is("history"));
	}

	@Test
	public void testFindNextWithRegExEscapedCorrectly() throws CoreException {
		openEditorAndFindNextAction("wo+rd-woord", Direction.FORWARD);
		IDialogSettings settings = getActionSettings();
		setEditorSelection(0, 5);
		settings.put("isRegEx", true);
		action.run();
		assertSelectionIs(0, 5);
	}

	@Test
	public void testCaseSensitiveFindNext() throws CoreException {
		openEditorAndFindNextAction("wordWORD", Direction.FORWARD);
		IDialogSettings settings = getActionSettings();
		settings.put("casesensitive", true);
		setEditorSelection(0, 4);
		action.run();
		assertSelectionIs(0, 4);
	}

	@Test
	public void testFindNextMultilineSelection() throws CoreException {
		openEditorAndFindNextAction("line\n\rnext\n\rnext\r\nline", Direction.FORWARD);
		// we expect the search string to only contain the first line
		setEditorSelection(0, 10);
		action.run();
		assertSelectionIs(18, 4);
	}

	@Test
	public void testFindNextNoWrap() throws CoreException {
		openEditorAndFindNextAction("wordword", Direction.FORWARD);
		IDialogSettings settings = getActionSettings();
		settings.put("wrap", false);
		setEditorSelection(0, 4);
		action.run();
		assertSelectionIs(4, 4);
		action.run();
		assertSelectionIs(4, 4);
	}

	@Test
	public void testFindWholeWords() throws CoreException {
		openEditorAndFindNextAction("word longerword word", Direction.FORWARD);
		IDialogSettings settings = getActionSettings();
		settings.put("wholeword", true);
		setEditorSelection(0, 4);
		action.run();
		assertSelectionIs(16, 4);
	}

	@Test
	public void testFindWholeWordsIsNotWord() throws CoreException {
		openEditorAndFindNextAction("w ord longerw ordinner w ord", Direction.FORWARD);
		IDialogSettings settings = getActionSettings();
		settings.put("wholeword", true);
		setEditorSelection(0, 5);
		action.run();
		assertSelectionIs(12, 5);
		action.run();
		assertSelectionIs(23, 5);
	}

}