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
import static org.junit.Assert.fail;

import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import org.eclipse.core.filebuffers.tests.ResourceHelper;

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


public class FindNextActionTest {
	private IFile fFile;

	private IEditorPart fEditor;

	private int fCount;

	private FindNextAction action;

	private static final String BUNDLE_FOR_CONSTRUCTED_KEYS= "org.eclipse.ui.texteditor.ConstructedEditorMessages";//$NON-NLS-1$

	private static ResourceBundle fgBundleForConstructedKeys= ResourceBundle.getBundle(BUNDLE_FOR_CONSTRUCTED_KEYS);

	public void openEditorAndFindNextAction(String content, boolean direction) {
		try {
			IFolder folder= ResourceHelper.createFolder("EncodingChangeTestProject/EncodingChangeTests/");
			fFile= ResourceHelper.createFile(folder, "file" + fCount + ".txt", content);
			fFile.setCharset(null, null);
			fCount++;
		} catch (CoreException e) {
			fail();
		}
		IWorkbench workbench= PlatformUI.getWorkbench();
		IWorkbenchPage page= workbench.getActiveWorkbenchWindow().getActivePage();
		try {
			fEditor= IDE.openEditor(page, fFile);
		} catch (PartInitException e) {
			fail();
		}

		action= new FindNextAction(fgBundleForConstructedKeys, "findNext", fEditor, direction);
	}

	public static void closeEditor(IEditorPart editor) {
		IWorkbenchPartSite site;
		IWorkbenchPage page;
		if (editor != null && (site= editor.getSite()) != null && (page= site.getPage()) != null) {
			page.closeEditor(editor, false);
		}
	}

	@After
	public void tearDown() throws Exception {
		resetInitialSearchSettings();
		closeEditor(fEditor);
		fEditor= null;
		fFile= null;
		ResourceHelper.deleteProject("EncodingChangeTestProject");
		TestUtil.cleanUp();
	}

	private void resetInitialSearchSettings() {
		IDialogSettings settings= getActionSettings();
		settings.put("isRegEx", false);
		settings.put("casesensitive", false);
		settings.put("wrap", true);
		settings.put("wholeword", false);
	}

	public void setEditorSelection(int offset, int length) {
		if (fEditor instanceof AbstractTextEditor textEditor) {
			Document document= (Document) textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
			TextSelection selection= new TextSelection(document, offset, length);
			ISelectionProvider selectionProvider= textEditor.getSelectionProvider();
			selectionProvider.setSelection(selection);
		}
	}

	public TextSelection getEditorSelection() {
		if (fEditor instanceof AbstractTextEditor textEditor) {
			ISelectionProvider selectionProvider= textEditor.getSelectionProvider();
			if (selectionProvider.getSelection() instanceof TextSelection) {
				return (TextSelection) selectionProvider.getSelection();
			}
		}
		return null;
	}

	public void assertSelectionIs(int offset, int length) {
		assertThat(getEditorSelection().getRegions()[0].getOffset(), is(offset));
		assertThat(getEditorSelection().getRegions()[0].getLength(), is(length));
	}

	private IDialogSettings getActionSettings() {
		IDialogSettings settings= PlatformUI.getDialogSettingsProvider(FrameworkUtil.getBundle(FindNextAction.class))
				.getDialogSettings();
		IDialogSettings fDialogSettings= settings.getSection("org.eclipse.ui.texteditor.FindReplaceDialog");
		if (fDialogSettings == null)
			fDialogSettings= settings.addNewSection("org.eclipse.ui.texteditor.FindReplaceDialog");
		return fDialogSettings;
	}

	@Test
	public void testFindNextForward() {
		openEditorAndFindNextAction("testtesttest", true);
		setEditorSelection(0, 4);
		action.run();
		assertSelectionIs(4, 4);
		action.run();
		assertSelectionIs(8, 4);
		action.run();
		assertSelectionIs(0, 4);
	}

	@Test
	public void testFindNextBackwards() {
		openEditorAndFindNextAction("testtesttest", false);
		setEditorSelection(4, 4);
		action.run();
		assertSelectionIs(0, 4);
		action.run();
		assertSelectionIs(8, 4);
	}

	@Test
	public void testFindNextFromHistory() {
		openEditorAndFindNextAction("word-abcd-text", true);
		IDialogSettings settings= getActionSettings();
		HistoryStore historyStore= new HistoryStore(settings, "findhistory", 15);
		historyStore.add("abcd");
		setEditorSelection(0, 0);
		action.run();
		assertSelectionIs(5, 4);
		setEditorSelection(3, 0);
		action.run();
		assertSelectionIs(5, 4);
	}

	@Test
	public void testFindNextStoresCorrectHistory() {
		openEditorAndFindNextAction("history", true);
		setEditorSelection(0, "history".length());
		action.run();
		IDialogSettings settings= getActionSettings();
		HistoryStore historyStore= new HistoryStore(settings, "findhistory", 15);
		assertThat(historyStore.get(0), is("history"));
	}

	@Test
	public void testFindNextWithRegExEscapedCorrectly() {
		openEditorAndFindNextAction("wo+rd-woord", true);
		IDialogSettings settings= getActionSettings();
		setEditorSelection(0, 5);
		settings.put("isRegEx", true);
		action.run();
		assertSelectionIs(0, 5);
	}

	@Test
	public void testCaseSensitiveFindNext() {
		openEditorAndFindNextAction("wordWORD", true);
		IDialogSettings settings= getActionSettings();
		settings.put("casesensitive", true);
		setEditorSelection(0, 4);
		action.run();
		assertSelectionIs(0, 4);
	}

	@Test
	public void testFindNextMultilineSelection() {
		openEditorAndFindNextAction("line\n\rnext\n\rnext\r\nline", true);
		// we expect the search string to only contain the first line
		setEditorSelection(0, 10);
		action.run();
		assertSelectionIs(18, 4);
	}

	@Test
	public void testFindNextNoWrap() {
		openEditorAndFindNextAction("wordword", true);
		IDialogSettings settings= getActionSettings();
		settings.put("wrap", false);
		setEditorSelection(0, 4);
		action.run();
		assertSelectionIs(4, 4);
		action.run();
		assertSelectionIs(4, 4);
	}

	@Test
	public void testFindWholeWords() {
		openEditorAndFindNextAction("word longerword word", true);
		IDialogSettings settings= getActionSettings();
		settings.put("wholeword", true);
		setEditorSelection(0, 4);
		action.run();
		assertSelectionIs(16, 4);
	}

	@Test
	public void testFindWholeWordsIsNotWord() {
		openEditorAndFindNextAction("w ord longerw ordinner w ord", true);
		IDialogSettings settings= getActionSettings();
		settings.put("wholeword", true);
		setEditorSelection(0, 5);
		action.run();
		assertSelectionIs(12, 5);
		action.run();
		assertSelectionIs(23, 5);
	}
}