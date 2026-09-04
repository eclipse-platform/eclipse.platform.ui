/*******************************************************************************
 * Copyright (c) 2026 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace.overlay;

import static org.eclipse.ui.internal.findandreplace.FindReplaceTestUtil.notifyKeyDown;
import static org.eclipse.ui.internal.findandreplace.FindReplaceTestUtil.processPendingEvents;
import static org.eclipse.ui.internal.findandreplace.FindReplaceTestUtil.runEventQueue;
import static org.eclipse.ui.internal.findandreplace.FindReplaceTestUtil.waitForFocus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ResourceBundle;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;

import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.StatusTextEditor;

/**
 * End-to-end test for whether a command acts on the Find/Replace overlay or on
 * the editor it is placed on.
 * <p>
 * The overlay's input fields sit inside the editor's widget tree, so the editor
 * remains the active part while a search term is typed, and its key bindings and
 * handlers would otherwise carry out keys meant for an input field. How that is
 * prevented is an implementation concern and deliberately not observed here; only
 * where a command ends up taking effect is.
 * <p>
 * Key strokes are delivered by notifying the widget rather than by posting native
 * events, which still runs the display filter the key binding dispatcher installs
 * but does not depend on the operating system, so the test also works headless.
 * The native editing inside the SWT text widget does not happen that way, so the
 * assertions say what must <em>not</em> reach the editor, plus one control
 * proving that keys are dispatched at all.
 * <p>
 * Unlike {@link FindReplaceOverlayTest}, which uses a bare viewer, this needs a
 * real editor part: without one there is nothing to arbitrate.
 */
public class FindReplaceOverlayInEditorTest {

	private static final String CONTENT = "word one word two"; //$NON-NLS-1$

	/** Tagged onto the widgets under {@link FindReplaceOverlay#ID_DATA_KEY}. */
	private static final String SEARCH_FIELD = "searchInput"; //$NON-NLS-1$

	private static final String REPLACE_FIELD = "replaceInput"; //$NON-NLS-1$

	private StatusTextEditor editor;

	private Text searchField;

	private String testName;

	@BeforeEach
	void openEditorWithOverlay(TestInfo testInfo) throws PartInitException {
		testName = testInfo.getTestMethod().get().getName();
		PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().forceActive();
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		editor = (StatusTextEditor) page.openEditor(new TestTextEditorInput(CONTENT), TestTextEditor.ID);
		runEventQueue();

		// Opening through the action rather than through its internals also asserts
		// that an editor of this kind gets the overlay rather than the dialog.
		new FindReplaceAction(ResourceBundle.getBundle("org.eclipse.ui.texteditor.ConstructedEditorMessages"), //$NON-NLS-1$
				"Editor.FindReplace.", editor).run(); //$NON-NLS-1$
		runEventQueue();

		searchField = focusedInputField(SEARCH_FIELD);
	}

	/**
	 * Where these tests run inside a full product rather than against the test
	 * bundle's own dependencies, the workbench opens its Welcome page over the whole
	 * window on a fresh workspace. It would cover the editor and keep the focus, so
	 * that the overlay never receives it. Nothing to close where no such page exists.
	 * <p>
	 * The page is put into standby before it is closed, because it is shown
	 * maximized: closing it right away leaves the window maximized on a part that is
	 * gone, and an editor opened afterwards is then never rendered into the window.
	 * It remains in the rendering engine's limbo instead, where it is invisible, and
	 * nothing inside an invisible widget tree can take the focus, so the overlay
	 * would never receive it. Standby restores the window's regular layout.
	 * <p>
	 * Closing once for the whole class is enough, since the page does not come back.
	 */
	@BeforeAll
	static void closeWelcomePage() {
		IIntroManager introManager = PlatformUI.getWorkbench().getIntroManager();
		IIntroPart welcomePage = introManager.getIntro();
		if (welcomePage != null) {
			introManager.setIntroStandby(welcomePage, true);
			runEventQueue();
			introManager.closeIntro(welcomePage);
			runEventQueue();
		}
	}

	@AfterEach
	void closeEditor() {
		searchField = null;
		if (editor != null) {
			editor.getSite().getPage().closeEditor(editor, false);
			editor = null;
		}
	}

	/**
	 * A key typed into an input field belongs to that field: it must never be
	 * carried out on the editor's document instead.
	 */
	@Test
	public void testKeysTypedIntoTheOverlayDoNotReachTheEditor() {
		focusSearchField();
		int selectionLengthBefore = editorSelectionLength();

		type(searchField, SWT.MOD1, 'a');

		assertEquals(selectionLengthBefore, editorSelectionLength(),
				"Select All's key binding must not select the editor's document"); //$NON-NLS-1$
		assertEquals(CONTENT, documentText(), "the document must not change"); //$NON-NLS-1$

		type(searchField, SWT.MOD1, SWT.DEL);

		assertEquals(CONTENT, documentText(),
				"delete-next-word's key binding must not delete from the document"); //$NON-NLS-1$

		// Control: a key the overlay does bind must take effect, otherwise the
		// assertions above would hold simply because no key was dispatched at all.
		assertTrue(searchField.isVisible(), "precondition: the overlay is open"); //$NON-NLS-1$
		type(searchField, SWT.NONE, SWT.ESC);
		assertFalse(searchField.isVisible(), "Escape in the search field must close the overlay"); //$NON-NLS-1$
	}

	/**
	 * The retargetable global actions must act on the focused input field, so that
	 * Edit &gt; Select All does what the user expects while typing a search term.
	 */
	@Test
	public void testSelectAllActsOnTheFocusedInputField() throws Exception {
		focusSearchField();
		searchField.setText("abc"); //$NON-NLS-1$
		searchField.setSelection(0, 0);
		int editorSelectionBefore = editorSelectionLength();

		executeCommand("org.eclipse.ui.edit.selectAll"); //$NON-NLS-1$

		assertEquals(3, searchField.getSelectionCount(),
				"Select All must select the text of the focused input field"); //$NON-NLS-1$
		assertEquals(editorSelectionBefore, editorSelectionLength(),
				"Select All must not select the editor's document"); //$NON-NLS-1$
	}

	/**
	 * Enter finds the next match in the search field and replaces in the replace
	 * field, so the two must be told apart although the key is the same.
	 */
	@Test
	public void testEnterMeansSomethingElseInEachInputField() throws Exception {
		focusSearchField();
		searchField.setText("word"); //$NON-NLS-1$
		processPendingEvents();
		int firstMatch = editorSelectionOffset();

		type(searchField, SWT.NONE, SWT.CR);

		assertNotEquals(firstMatch, editorSelectionOffset(),
				"Enter in the search field must move on to the next match"); //$NON-NLS-1$
		assertEquals(CONTENT, documentText(), "finding must not change the document"); //$NON-NLS-1$

		Text replaceField = showReplaceField();
		replaceField.setText("X"); //$NON-NLS-1$
		processPendingEvents();

		type(replaceField, SWT.NONE, SWT.CR);

		assertNotEquals(CONTENT, documentText(),
				"Enter in the replace field must replace the current match in the document"); //$NON-NLS-1$
	}

	/**
	 * Commands of the surrounding window, Save among them, must stay executable
	 * while an input field has focus: only the editor's own are out of place there.
	 */
	@Test
	public void testWindowCommandsStillWorkWhileTheOverlayHasFocus() throws Exception {
		document().set("edited"); //$NON-NLS-1$
		processPendingEvents();
		assertTrue(editor.isDirty(), "precondition: the editor has unsaved changes"); //$NON-NLS-1$
		focusSearchField();

		executeCommand("org.eclipse.ui.file.save"); //$NON-NLS-1$

		assertFalse(editor.isDirty(), "Save must still reach the editor while the overlay has focus"); //$NON-NLS-1$
	}

	/**
	 * The overlay is not a part of its own as far as the workbench is concerned, so
	 * views tracking the active part or the selection must see nothing change.
	 */
	@Test
	public void testTheEditorStaysTheActivePartWhileTheOverlayHasFocus() {
		focusSearchField();
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		assertEquals(editor, page.getActiveEditor(), "active editor"); //$NON-NLS-1$
		assertEquals(editor, page.getActivePart(), "active part"); //$NON-NLS-1$
		assertNotNull(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection(),
				"the selection service must keep reporting the editor's selection"); //$NON-NLS-1$
	}

	/**
	 * The editor acts on its keys again once the overlay no longer has focus. Only
	 * asserting the other direction would be satisfied by an overlay that disabled
	 * the editor's keys for good.
	 */
	@Test
	public void testTheEditorActsOnKeysAgainOnceTheOverlayLostFocus() {
		focusSearchField();
		editor.setFocus();
		waitForFocus(editorWidget()::isFocusControl, testName);

		type(editorWidget(), SWT.MOD1, 'a');

		assertEquals(CONTENT.length(), editorSelectionLength(),
				"Select All must apply to the document again once the editor has focus"); //$NON-NLS-1$
	}

	private void focusSearchField() {
		// Focus the editor first, so that focusing the search field is a real
		// transition rather than a no-op on an already focused control.
		editor.setFocus();
		processPendingEvents();
		searchField.forceFocus();
		waitForFocus(searchField::isFocusControl, testName);
	}

	/** Reveals the replace field through the overlay's own command, which focuses it. */
	private Text showReplaceField() throws Exception {
		executeCommand(FindReplaceOverlayCommandSupport.CMD_TOGGLE_REPLACE);
		return focusedInputField(REPLACE_FIELD);
	}

	/**
	 * The overlay's input field of the given kind, once it has focus. Waiting rather
	 * than asserting straight away is what makes this survive a workbench that hands
	 * focus over more slowly, and yields a screenshot rather than a bare mismatch when
	 * the focus ends up somewhere else entirely.
	 */
	private Text focusedInputField(String expectedId) {
		waitForFocus(() -> isInputField(Display.getCurrent().getFocusControl(), expectedId), testName);
		return (Text) Display.getCurrent().getFocusControl();
	}

	private static boolean isInputField(Control focused, String expectedId) {
		return focused instanceof Text field
				&& expectedId.equals(field.getParent().getData(FindReplaceOverlay.ID_DATA_KEY));
	}

	private static void executeCommand(String commandId) throws Exception {
		PlatformUI.getWorkbench().getService(IHandlerService.class).executeCommand(commandId, null);
		processPendingEvents();
	}

	/**
	 * Types a key and lets what it triggered run, but without waiting on top: the
	 * stroke itself is carried out while it is delivered, and waiting after every one
	 * of them would dominate the runtime of this class.
	 */
	private static void type(Control target, int stateMask, int keyCode) {
		notifyKeyDown(target, stateMask, keyCode);
		processPendingEvents();
	}

	private Control editorWidget() {
		return editor.getAdapter(org.eclipse.jface.text.ITextViewer.class).getTextWidget();
	}

	private IDocument document() {
		return editor.getDocumentProvider().getDocument(editor.getEditorInput());
	}

	private String documentText() {
		return document().get();
	}

	private ITextSelection editorSelection() {
		return (ITextSelection) editor.getSelectionProvider().getSelection();
	}

	private int editorSelectionLength() {
		return editorSelection().getLength();
	}

	private int editorSelectionOffset() {
		return editorSelection().getOffset();
	}

}
