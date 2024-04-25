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
package org.eclipse.ui.workbench.texteditor.tests;

import static org.eclipse.ui.workbench.texteditor.tests.FindReplaceTestUtil.runEventQueue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import org.eclipse.swt.SWT;

import org.eclipse.jface.util.Util;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.findandreplace.SearchOptions;

public abstract class FindReplaceUITest<AccessType extends IFindReplaceUIAccess> {
	@Rule
	public TestName testName= new TestName();

	private TextViewer fTextViewer;

	private AccessType dialog;

	protected final void initializeTextViewerWithFindReplaceUI(String content) {
		openTextViewer(content);
		initializeFindReplaceUIForTextViewer();
	}

	private void openTextViewer(String content) {
		fTextViewer= new TextViewer(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		fTextViewer.setDocument(new Document(content));
		fTextViewer.getControl().setFocus();
	}

	private void initializeFindReplaceUIForTextViewer() {
		dialog= openUIFromTextViewer(fTextViewer);
		dialog.assertInitialConfiguration();
	}

	private void reopenFindReplaceUIForTextViewer() {
		dialog.close();
		dialog= openUIFromTextViewer(fTextViewer);
	}

	protected final void ensureHasFocusOnGTK() {
		if (Util.isGtk()) {
			runEventQueue();
			if (dialog.getActiveShell() == null) {
				String screenshotPath= ScreenshotTest.takeScreenshot(FindReplaceUITest.class, testName.getMethodName(), System.out);
				fail("this test does not work on GTK unless the runtime workbench has focus. Screenshot: " + screenshotPath);
			}
		}
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

		dialog.getFindReplaceLogic().updateTarget(fTextViewer.getFindReplaceTarget(), false);
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
		ensureHasFocusOnGTK();
		IFindReplaceTarget target= dialog.getTarget();

		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.simulateEnterInFindInputField(false);
		assertEquals(5, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.simulateEnterInFindInputField(true);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		// Keypad-Enter is also valid for navigating
		dialog.simulateKeyPressInFindInputField(SWT.KEYPAD_CR, false);
		assertEquals(5, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.simulateKeyPressInFindInputField(SWT.KEYPAD_CR, true);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);
	}

	@Test
	public void testChangeInputForIncrementalSearch() {
		initializeTextViewerWithFindReplaceUI("line\nline\nline");
		dialog.select(SearchOptions.INCREMENTAL);

		dialog.setFindText("lin");
		IFindReplaceTarget target= dialog.getTarget();
		assertEquals(0, (target.getSelection()).x);
		assertEquals(dialog.getFindText().length(), (target.getSelection()).y);

		dialog.setFindText("line");
		assertEquals(0, (target.getSelection()).x);
		assertEquals(dialog.getFindText().length(), (target.getSelection()).y);
	}

	@Test
	public void testFindWithWholeWordEnabledWithMultipleWords() {
		initializeTextViewerWithFindReplaceUI("two words two");
		dialog.setFindText("two");
		dialog.select(SearchOptions.WHOLE_WORD);
		dialog.select(SearchOptions.WRAP);
		IFindReplaceTarget target= dialog.getTarget();
		assertEquals(0, (target.getSelection()).x);
		assertEquals(0, (target.getSelection()).y);

		dialog.setFindText("two wo");
		dialog.assertDisabled(SearchOptions.WHOLE_WORD);
		dialog.assertSelected(SearchOptions.WHOLE_WORD);

		dialog.simulateEnterInFindInputField(false);
		assertThat(target.getSelectionText(), is(dialog.getFindText()));
		assertEquals(0, (target.getSelection()).x);
		assertEquals(dialog.getFindText().length(), (target.getSelection()).y);
	}

	@Test
	public void testReplaceAndFindAfterInitializingFindWithSelectedString() {
		openTextViewer("text text text");
		fTextViewer.setSelectedRange(0, 4);
		initializeFindReplaceUIForTextViewer();

		IFindReplaceTarget target= dialog.getTarget();
		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.performReplaceAndFind();

		assertEquals(" text text", fTextViewer.getDocument().get());
		assertEquals(1, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);
	}

	@Test
	public void testRegExSearch() {
		initializeTextViewerWithFindReplaceUI("abc");
		dialog.select(SearchOptions.REGEX);
		dialog.setFindText("(a|bc)");
		IFindReplaceTarget target= dialog.getTarget();
		dialog.simulateEnterInFindInputField(false);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(1, (target.getSelection()).y);

		dialog.simulateEnterInFindInputField(false);
		assertEquals(1, (target.getSelection()).x);
		assertEquals(2, (target.getSelection()).y);
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
	public void testIncrementalSearchOnlyEnabledWhenAllowed() {
		initializeTextViewerWithFindReplaceUI("text text text");

		dialog.select(SearchOptions.INCREMENTAL);
		dialog.select(SearchOptions.REGEX);

		dialog.assertSelected(SearchOptions.INCREMENTAL);
		dialog.assertDisabled(SearchOptions.INCREMENTAL);
	}

	/*
	 * Test for https://github.com/eclipse-platform/eclipse.platform.ui/pull/1805#pullrequestreview-1993772378
	 */
	@Test
	public void testIncrementalSearchOptionRecoveredCorrectly() {
		initializeTextViewerWithFindReplaceUI("text text text");

		dialog.select(SearchOptions.INCREMENTAL);
		dialog.assertSelected(SearchOptions.INCREMENTAL);
		dialog.assertEnabled(SearchOptions.INCREMENTAL);

		reopenFindReplaceUIForTextViewer();
		dialog.assertSelected(SearchOptions.INCREMENTAL);
		dialog.assertEnabled(SearchOptions.INCREMENTAL);

		dialog.select(SearchOptions.REGEX);
		dialog.assertSelected(SearchOptions.INCREMENTAL);
		dialog.assertDisabled(SearchOptions.INCREMENTAL);

		reopenFindReplaceUIForTextViewer();
		dialog.assertSelected(SearchOptions.INCREMENTAL);
		dialog.assertDisabled(SearchOptions.INCREMENTAL);
	}

	protected AccessType getDialog() {
		return dialog;
	}

	protected TextViewer getTextViewer() {
		return fTextViewer;
	}
}
