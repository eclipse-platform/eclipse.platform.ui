/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.findandreplace.SearchOptions;

/**
 * Tests the FindReplaceDialog.
 *
 * @since 3.1
 */
public abstract class FindReplaceUITest<AccessType extends IFindReplaceUIAccess> {

	@Rule
	public TestName testName= new TestName();

	protected TextViewer fTextViewer;

	static void runEventQueue() {
		Display display= PlatformUI.getWorkbench().getDisplay();
		for (int i= 0; i < 10; i++) { // workaround for https://bugs.eclipse.org/323272
			while (display.readAndDispatch()) {
				// do nothing
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}

	protected AccessType dialog;

	public void initializeFindReplaceUI(String content) {
		fTextViewer= new TextViewer(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		fTextViewer.setDocument(new Document(content));
		fTextViewer.getControl().setFocus();

		dialog= openUIFromTextViewer(fTextViewer);
		assertInitialConfiguration();
	}

	public abstract AccessType openUIFromTextViewer(TextViewer viewer);

	public void assertEnabled(SearchOptions option) {
		Set<SearchOptions> enabled= dialog.getEnabledOptions();
		assertThat(enabled, hasItems(option));
	}

	public void assertDisabled(SearchOptions option) {
		Set<SearchOptions> enabled= dialog.getEnabledOptions();
		assertThat(enabled, not(hasItems(option)));
	}

	public void assertSelected(SearchOptions option) {
		Set<SearchOptions> enabled= dialog.getSelectedOptions();
		assertThat(enabled, hasItems(option));
	}

	protected void assertUnselected(SearchOptions option) {
		Set<SearchOptions> enabled= dialog.getSelectedOptions();
		assertThat(enabled, not(hasItems(option)));

	}

	@After
	public void tearDown() throws Exception {
		if (dialog != null) {
			dialog.close();
			dialog= null;
		}

		if (fTextViewer != null) {
			fTextViewer.getControl().dispose();
			fTextViewer= null;
		}
	}

	@Test
	public void testInitialButtonState() {
		initializeFindReplaceUI("");
	}

	@Test
	public void testDisableWholeWordIfRegEx() {
		initializeFindReplaceUI("");
		fTextViewer= new TextViewer(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		fTextViewer.setDocument(new Document("line\nline\nline"));
		fTextViewer.getControl().setFocus();

		dialog.setFindText("word", false);
		assertEnabled(SearchOptions.REGEX);
		assertEnabled(SearchOptions.WHOLE_WORD);

		dialog.getFindReplaceLogic().updateTarget(fTextViewer.getFindReplaceTarget(), false);
		dialog.select(SearchOptions.WHOLE_WORD);
		dialog.select(SearchOptions.REGEX);
		assertEnabled(SearchOptions.REGEX);
		assertDisabled(SearchOptions.WHOLE_WORD);
		assertSelected(SearchOptions.WHOLE_WORD);

		dialog.unselect(SearchOptions.REGEX);
		assertEnabled(SearchOptions.REGEX);
		assertUnselected(SearchOptions.REGEX);
		assertEnabled(SearchOptions.WHOLE_WORD);
		assertSelected(SearchOptions.WHOLE_WORD);
	}

	@Test
	public void testDisableWholeWordIfNotWord() {
		initializeFindReplaceUI("");
		dialog.select(SearchOptions.WHOLE_WORD);

		dialog.setFindText("word", false);
		assertEnabled(SearchOptions.REGEX);
		assertEnabled(SearchOptions.WHOLE_WORD);
		assertSelected(SearchOptions.WHOLE_WORD);

		dialog.setFindText("no word", false);
		assertEnabled(SearchOptions.REGEX);
		assertDisabled(SearchOptions.WHOLE_WORD);
		assertSelected(SearchOptions.WHOLE_WORD);

		dialog.setFindText("word_again", false);
		assertEnabled(SearchOptions.REGEX);
		assertEnabled(SearchOptions.WHOLE_WORD);
		assertSelected(SearchOptions.WHOLE_WORD);
	}

	@Test
	public void testShiftEnterReversesSearchDirection() {
		initializeFindReplaceUI("line\nline\nline");

		dialog.select(SearchOptions.INCREMENTAL);
		dialog.setFindText("line", false);
		dialog.ensureHasFocusOnGTK();
		IFindReplaceTarget target= dialog.getTarget();

		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.simulateEnterInFindInputField(false);
		assertEquals(5, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.simulateEnterInFindInputField(true);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		// Keypad-Enter is also be valid for navigating
		dialog.simulateKeyPressInFindInputField(SWT.KEYPAD_CR, false);
		assertEquals(5, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.simulateKeyPressInFindInputField(SWT.KEYPAD_CR, true);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);
	}



	@Test
	public void testFindWithWholeWordEnabledWithMultipleWords() {
		initializeFindReplaceUI("two words two");
		dialog.select(SearchOptions.INCREMENTAL);
		dialog.setFindText("two", false);
		dialog.select(SearchOptions.WHOLE_WORD);
		dialog.select(SearchOptions.WRAP);
		IFindReplaceTarget target= dialog.getTarget();
		assertEquals(0, (target.getSelection()).x);
		assertEquals(dialog.getFindText().length(), (target.getSelection()).y);

		dialog.setFindText("two wo", false);
		dialog.simulateEnterInFindInputField(false);
		assertDisabled(SearchOptions.WHOLE_WORD);
		assertSelected(SearchOptions.WHOLE_WORD);
		assertThat(target.getSelectionText(), is(dialog.getFindText()));

		dialog.setFindText("two ", false);
		dialog.simulateEnterInFindInputField(false);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(dialog.getFindText().length(), (target.getSelection()).y);
	}



	@Test
	public void testRegExSearch() {
		initializeFindReplaceUI("abc");
		dialog.select(SearchOptions.INCREMENTAL);
		dialog.select(SearchOptions.REGEX);
		dialog.setFindText("(a|bc)", false);
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
		initializeFindReplaceUI("ABCD ABCD ABCD");
		dialog.setFindText("ABCD", false);
		dialog.setReplaceText("abcd");
		dialog.performReplace();

		assertThat(fTextViewer.getDocument().get(), is("abcd ABCD ABCD"));

		dialog.pressReplaceAll();
		assertThat(fTextViewer.getDocument().get(), is("abcd abcd abcd"));

		dialog.select(SearchOptions.REGEX);
		dialog.setFindText("(ab|cd)", false);
		dialog.setReplaceText("o");
		dialog.pressReplaceAll();
		assertThat(fTextViewer.getDocument().get(), is("oo oo oo"));
	}

	@Test
	public void testReplaceAllInSelection() {
		fTextViewer= new TextViewer(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		fTextViewer.setDocument(new Document("line\nline\nline"));
		fTextViewer.getControl().setFocus();
		fTextViewer.setSelection(new TextSelection(5, 9));

		dialog= openUIFromTextViewer(fTextViewer);
		dialog.unselect(SearchOptions.GLOBAL);
		dialog.setFindText("line", false);
		dialog.setReplaceText("");
		dialog.pressReplaceAll();
		assertThat(fTextViewer.getDocument().get(), is("line\n\n"));
	}


	@Test
	public void testChangeInputForIncrementalSearch() {
		initializeFindReplaceUI("line");
		dialog.select(SearchOptions.INCREMENTAL);

		dialog.setFindText("lin", false);
		IFindReplaceTarget target= dialog.getTarget();
		assertEquals(0, (target.getSelection()).x);
		assertEquals(dialog.getFindText().length(), (target.getSelection()).y);

		dialog.setFindText("line", false);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(dialog.getFindText().length(), (target.getSelection()).y);
	}

	public abstract void assertInitialConfiguration();


}
