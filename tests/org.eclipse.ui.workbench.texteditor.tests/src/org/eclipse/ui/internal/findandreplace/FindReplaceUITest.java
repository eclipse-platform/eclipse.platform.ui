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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import org.eclipse.swt.SWT;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.texteditor.FindReplaceAction;

public abstract class FindReplaceUITest<AccessType extends IFindReplaceUIAccess> {
	@Rule
	public TestName testName= new TestName();

	private TextViewer fTextViewer;

	private FindReplaceAction findReplaceAction;

	private AccessType dialog;

	@Before
	public final void ensureWorkbenchWindowIsActive() {
		PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().forceActive();
	}

	protected FindReplaceAction getFindReplaceAction() {
		return findReplaceAction;
	}

	protected final void initializeTextViewerWithFindReplaceUI(String content) {
		openTextViewer(content);
		initializeFindReplaceUIForTextViewer();
	}

	protected void openTextViewer(String content) {
		fTextViewer= new TextViewer(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		fTextViewer.setDocument(new Document(content));
		fTextViewer.getControl().setFocus();
		findReplaceAction= new FindReplaceAction(ResourceBundle.getBundle("org.eclipse.ui.texteditor.ConstructedEditorMessages"), "Editor.FindReplace.", fTextViewer.getControl().getShell(),
				fTextViewer.getFindReplaceTarget());
	}

	protected void initializeFindReplaceUIForTextViewer() {
		dialog= openUIFromTextViewer(fTextViewer);
		dialog.assertInitialConfiguration();
	}

	protected void reopenFindReplaceUIForTextViewer() {
		dialog.close();
		dialog= openUIFromTextViewer(fTextViewer);
	}

	protected abstract AccessType openUIFromTextViewer(TextViewer viewer);

	@After
	public void tearDown() throws Exception {
		if (dialog != null) {
			dialog.closeAndRestore();
			dialog= null;
		}

		if (fTextViewer != null) {
			fTextViewer.getControl().dispose();
			fTextViewer= null;
		}
	}

	@Test
	public void testInitialButtonState() {
		initializeTextViewerWithFindReplaceUI("");
	}

	@Test
	public void testDisableWholeWordIfRegEx() {
		initializeTextViewerWithFindReplaceUI("");
		fTextViewer= new TextViewer(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		fTextViewer.setDocument(new Document("line" + System.lineSeparator() + "line" + System.lineSeparator() + "line"));
		fTextViewer.getControl().setFocus();

		dialog.setFindText("word");
		dialog.assertEnabled(SearchOptions.REGEX);
		dialog.assertEnabled(SearchOptions.WHOLE_WORD);

		dialog.select(SearchOptions.WHOLE_WORD);
		dialog.select(SearchOptions.REGEX);
		dialog.assertEnabled(SearchOptions.REGEX);
		dialog.assertDisabled(SearchOptions.WHOLE_WORD);
		dialog.assertSelected(SearchOptions.WHOLE_WORD);

		dialog.unselect(SearchOptions.REGEX);
		dialog.assertEnabled(SearchOptions.REGEX);
		dialog.assertUnselected(SearchOptions.REGEX);
		dialog.assertEnabled(SearchOptions.WHOLE_WORD);
		dialog.assertSelected(SearchOptions.WHOLE_WORD);
	}

	@Test
	public void testDisableWholeWordIfNotWord() {
		initializeTextViewerWithFindReplaceUI("");
		dialog.select(SearchOptions.WHOLE_WORD);

		dialog.setFindText("word");
		dialog.assertEnabled(SearchOptions.REGEX);
		dialog.assertEnabled(SearchOptions.WHOLE_WORD);
		dialog.assertSelected(SearchOptions.WHOLE_WORD);

		dialog.setFindText("no word");
		dialog.assertEnabled(SearchOptions.REGEX);
		dialog.assertDisabled(SearchOptions.WHOLE_WORD);
		dialog.assertSelected(SearchOptions.WHOLE_WORD);

		dialog.setFindText("word_again");
		dialog.assertEnabled(SearchOptions.REGEX);
		dialog.assertEnabled(SearchOptions.WHOLE_WORD);
		dialog.assertSelected(SearchOptions.WHOLE_WORD);
	}

	@Test
	public void testShiftEnterReversesSearchDirection() {
		initializeTextViewerWithFindReplaceUI("line\nline\nline");

		dialog.select(SearchOptions.INCREMENTAL);
		dialog.setFindText("line");
		IFindReplaceTarget target= getFindReplaceTarget();

		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.simulateKeyboardInteractionInFindInputField(SWT.CR, false);
		assertEquals(5, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.simulateKeyboardInteractionInFindInputField(SWT.CR, true);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		// Keypad-Enter is also valid for navigating
		dialog.simulateKeyboardInteractionInFindInputField(SWT.KEYPAD_CR, false);
		assertEquals(5, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.simulateKeyboardInteractionInFindInputField(SWT.KEYPAD_CR, true);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);
	}

	@Test
	public void testChangeInputForIncrementalSearch() {
		initializeTextViewerWithFindReplaceUI("line\nline\nline");
		dialog.select(SearchOptions.INCREMENTAL);

		dialog.setFindText("lin");
		IFindReplaceTarget target= getFindReplaceTarget();
		assertEquals(0, (target.getSelection()).x);
		assertEquals(dialog.getFindText().length(), (target.getSelection()).y);

		dialog.setFindText("line");
		assertEquals(0, (target.getSelection()).x);
		assertEquals(dialog.getFindText().length(), (target.getSelection()).y);
	}

	@Test
	public void testFindWithWholeWordEnabledWithMultipleWords() {
		initializeTextViewerWithFindReplaceUI("two words two");
		dialog.select(SearchOptions.INCREMENTAL);
		dialog.select(SearchOptions.WHOLE_WORD);
		dialog.select(SearchOptions.WRAP);
		dialog.setFindText("two");
		IFindReplaceTarget target= getFindReplaceTarget();
		assertEquals(0, (target.getSelection()).x);
		assertEquals(3, (target.getSelection()).y);

		dialog.setFindText("two wo");
		dialog.assertDisabled(SearchOptions.WHOLE_WORD);
		dialog.assertSelected(SearchOptions.WHOLE_WORD);

		dialog.simulateKeyboardInteractionInFindInputField(SWT.CR, false);
		assertThat(target.getSelectionText(), is(dialog.getFindText()));

		assertEquals(0, (target.getSelection()).x);
		assertEquals(dialog.getFindText().length(), (target.getSelection()).y);
	}

	@Test
	public void testRegExSearch() {
		initializeTextViewerWithFindReplaceUI("abc");
		dialog.select(SearchOptions.INCREMENTAL);
		dialog.setFindText("(a|bc)");
		dialog.select(SearchOptions.REGEX);

		IFindReplaceTarget target= getFindReplaceTarget();
		assertEquals(0, (target.getSelection()).x);
		assertEquals(1, (target.getSelection()).y);

		dialog.simulateKeyboardInteractionInFindInputField(SWT.CR, false);
		assertEquals(1, (target.getSelection()).x);
		assertEquals(2, (target.getSelection()).y);

		dialog.simulateKeyboardInteractionInFindInputField(SWT.CR, false);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(1, (target.getSelection()).y);

		dialog.setFindText("b|c");
		assertEquals(1, (target.getSelection()).x);
		assertEquals(1, (target.getSelection()).y);
	}

	@Test
	public void testSimpleReplace() {
		initializeTextViewerWithFindReplaceUI("ABCD ABCD ABCD");
		dialog.setFindText("ABCD");
		dialog.setReplaceText("abcd");
		dialog.performReplace();
		assertThat(fTextViewer.getDocument().get(), is("abcd ABCD ABCD"));

		dialog.performReplaceAll();
		assertThat(fTextViewer.getDocument().get(), is("abcd abcd abcd"));

		dialog.select(SearchOptions.REGEX);
		dialog.setFindText("(ab|cd)");
		dialog.setReplaceText("o");
		dialog.performReplaceAll();
		assertThat(fTextViewer.getDocument().get(), is("oo oo oo"));
	}

	@Test
	public void testReplaceAllInSelection() {
		openTextViewer("line" + System.lineSeparator() + "line" + System.lineSeparator() + "line");
		fTextViewer.getControl().setFocus();
		fTextViewer.setSelection(new TextSelection(4 + System.lineSeparator().length(), 8 + System.lineSeparator().length()));
		initializeFindReplaceUIForTextViewer();

		dialog.unselect(SearchOptions.GLOBAL);
		dialog.unselect(SearchOptions.WHOLE_WORD);

		dialog.setFindText("line");
		dialog.setReplaceText("");
		dialog.performReplaceAll();

		assertThat(fTextViewer.getDocument().get(), is("line" + System.lineSeparator() + System.lineSeparator()));
	}

	@Test
	public void testActivateWholeWordsAndSearchForTwoWords() {
		initializeTextViewerWithFindReplaceUI("text text text");
		dialog.select(SearchOptions.WHOLE_WORD);

		dialog.setFindText("text text");
		dialog.assertDisabled(SearchOptions.WHOLE_WORD);
		dialog.assertSelected(SearchOptions.WHOLE_WORD);

		dialog.setFindText("text");
		dialog.assertEnabled(SearchOptions.WHOLE_WORD);
		dialog.assertSelected(SearchOptions.WHOLE_WORD);

		dialog.select(SearchOptions.REGEX);
		dialog.assertDisabled(SearchOptions.WHOLE_WORD);
		dialog.assertSelected(SearchOptions.WHOLE_WORD);
	}

	@Test
	public void testActivateDialogWithSelectionActive() {
		openTextViewer("text" + System.lineSeparator() + "text" + System.lineSeparator() + "text");
		fTextViewer.setSelection(new TextSelection(4 + System.lineSeparator().length(), 8 + System.lineSeparator().length()));
		initializeFindReplaceUIForTextViewer();

		dialog.assertUnselected(SearchOptions.GLOBAL);
		dialog.setFindText("text");
		dialog.performReplaceAll();

		assertThat(fTextViewer.getDocument().get(), is("text" + System.lineSeparator() + System.lineSeparator()));
	}

	@Test
	public void testSearchTextSelectedWhenOpeningDialog() {
		openTextViewer("test");

		fTextViewer.setSelection(new TextSelection(0, 4));
		initializeFindReplaceUIForTextViewer();

		assertEquals("test", dialog.getFindText());
		assertEquals(dialog.getSelectedFindText(), dialog.getFindText());
	}

	@Test
	public void testSearchTextSelectedWhenSwitchingFocusToDialog() {
		openTextViewer("");
		initializeFindReplaceUIForTextViewer();

		dialog.setFindText("text");
		initializeFindReplaceUIForTextViewer();

		assertEquals("text", dialog.getFindText());
		assertEquals(dialog.getSelectedFindText(), dialog.getFindText());
	}

	private void assertScopeActivationOnTextInput(String input) {
		openTextViewer(input);
		fTextViewer.setSelection(new TextSelection(0, fTextViewer.getDocument().toString().length()));
		initializeFindReplaceUIForTextViewer();

		dialog.assertUnselected(SearchOptions.GLOBAL);
	}

	@Test
	public void testSelectionOnOpenSetsScopedMode() {
		assertScopeActivationOnTextInput("hello\r\nworld\r\nthis\r\nhas_many_lines");
		assertScopeActivationOnTextInput("hello\nworld");
	}

	@Test
	public void testActivateDialogSelectionActive_withoutRegExOptionActivated() {
		openTextViewer("test text.*;");
		initializeFindReplaceUIForTextViewer();

		fTextViewer.setSelection(new TextSelection("test ".length(), "text.*".length()));
		reopenFindReplaceUIForTextViewer();

		dialog.assertUnselected(SearchOptions.REGEX);
		assertEquals("text.*", dialog.getFindText());
	}

	@Test
	public void testActivateDialogSelectionActive_withRegExOptionActivated() {
		openTextViewer("test text.*;");
		initializeFindReplaceUIForTextViewer();
		dialog.select(SearchOptions.REGEX);
		fTextViewer.setSelection(new TextSelection("test ".length(), "text.*".length()));
		reopenFindReplaceUIForTextViewer();
		dialog.assertSelected(SearchOptions.REGEX);
		assertEquals("text\\.\\*", dialog.getFindText());

		dialog.performReplaceAll();
		assertThat(fTextViewer.getDocument().get(), is("test ;"));
	}

	@Test
	public void testReplaceIfSelectedOnStart() {
		openTextViewer("abcdefg");
		fTextViewer.setSelection(new TextSelection(2, 2));
		initializeFindReplaceUIForTextViewer();

		dialog.setReplaceText("aa");
		dialog.performReplace();

		assertThat(fTextViewer.getDocument().get(), is("abaaefg"));
	}

	protected AccessType getDialog() {
		return dialog;
	}

	protected TextViewer getTextViewer() {
		return fTextViewer;
	}

	protected final IFindReplaceTarget getFindReplaceTarget() {
		return fTextViewer.getFindReplaceTarget();
	}

}
