/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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

import static org.eclipse.ui.internal.findandreplace.FindReplaceTestUtil.runEventQueue;
import static org.eclipse.ui.internal.findandreplace.FindReplaceTestUtil.waitForFocus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.util.Util;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.ui.internal.findandreplace.FindReplaceUITest;
import org.eclipse.ui.internal.findandreplace.SearchOptions;

import org.eclipse.ui.texteditor.FindReplaceAction;

public class FindReplaceDialogTest extends FindReplaceUITest<DialogAccess> {
	@Override
	public DialogAccess openUIFromTextViewer(TextViewer viewer) {
		Accessor findActionAccessor= new Accessor(getFindReplaceAction(), FindReplaceAction.class);
		findActionAccessor.invoke("showDialog");

		Object fFindReplaceDialogStub= findActionAccessor.get("fgFindReplaceDialogStub");
		if (fFindReplaceDialogStub == null) {
			fFindReplaceDialogStub= findActionAccessor.get("fgFindReplaceDialogStubShell");
		}
		Accessor fFindReplaceDialogStubAccessor= new Accessor(fFindReplaceDialogStub, "org.eclipse.ui.texteditor.FindReplaceAction$FindReplaceDialogStub", getClass().getClassLoader());

		Dialog dialog= (Dialog) fFindReplaceDialogStubAccessor.invoke("getDialog");
		DialogAccess uiAccess= new DialogAccess(getFindReplaceTarget(), dialog);
		waitForFocus(uiAccess::hasFocus, testName.getMethodName());
		return uiAccess;
	}

	@Test
	public void testFocusNotChangedWhenEnterPressed() {
		assumeFalse("On Mac, checkboxes only take focus if 'Full Keyboard Access' is enabled in the system preferences", Util.isMac());

		initializeTextViewerWithFindReplaceUI("line\nline\nline");
		DialogAccess dialog= getDialog();

		dialog.getFindCombo().setFocus();
		dialog.setFindText("line");
		dialog.simulateKeyboardInteractionInFindInputField(SWT.CR, false);
		assertTrue(dialog.getFindCombo().isFocusControl());

		Button wrapCheckBox= dialog.getButtonForSearchOption(SearchOptions.WRAP);
		Button globalRadioButton= dialog.getButtonForSearchOption(SearchOptions.GLOBAL);
		wrapCheckBox.setFocus();
		dialog.simulateKeyboardInteractionInFindInputField(SWT.CR, false);
		assertTrue(wrapCheckBox.isFocusControl());

		globalRadioButton.setFocus();
		dialog.simulateKeyboardInteractionInFindInputField(SWT.CR, false);
		assertTrue(globalRadioButton.isFocusControl());
	}

	@Test
	public void testFocusNotChangedWhenButtonMnemonicPressed() {
		assumeFalse("Mac does not support mnemonics", Util.isMac());

		initializeTextViewerWithFindReplaceUI("");
		DialogAccess dialog= getDialog();
		dialog.setFindText("line");
		runEventQueue();

		Button wrapCheckBox= dialog.getButtonForSearchOption(SearchOptions.WRAP);
		wrapCheckBox.setFocus();
		final Event event= new Event();
		event.detail= SWT.TRAVERSE_MNEMONIC;
		event.character= 'n';
		event.doit= false;
		wrapCheckBox.traverse(SWT.TRAVERSE_MNEMONIC, event);
		runEventQueue();
		assertTrue(wrapCheckBox.isFocusControl());

		Button globalRadioButton= dialog.getButtonForSearchOption(SearchOptions.GLOBAL);
		globalRadioButton.setFocus();
		event.detail= SWT.TRAVERSE_MNEMONIC;
		event.doit= false;
		globalRadioButton.traverse(SWT.TRAVERSE_MNEMONIC, event);
		runEventQueue();
		assertTrue(globalRadioButton.isFocusControl());

		event.detail= SWT.TRAVERSE_MNEMONIC;
		event.character= 'r';
		event.doit= false;
		globalRadioButton.traverse(SWT.TRAVERSE_MNEMONIC, event);
		runEventQueue();
		assertTrue(globalRadioButton.isFocusControl());
	}

	@Test
	public void testShiftEnterReversesSearchDirectionDialogSpecific() {
		initializeTextViewerWithFindReplaceUI("line\nline\nline");
		DialogAccess dialog= getDialog();

		dialog.setFindText("line");
		IFindReplaceTarget target= getFindReplaceTarget();

		dialog.simulateKeyboardInteractionInFindInputField(SWT.CR, false);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.simulateKeyboardInteractionInFindInputField(SWT.CR, false);
		assertEquals(5, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.simulateKeyboardInteractionInFindInputField(SWT.CR, true);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		// This part only makes sense for the FindReplaceDialog since not every UI might have stored
		// the search direction as a state
		dialog.unselect(SearchOptions.FORWARD);
		dialog.simulateKeyboardInteractionInFindInputField(SWT.CR, true);
		assertEquals(5, (target.getSelection()).x);
	}

	@Test
	public void testReplaceAndFindAfterInitializingFindWithSelectedString() {
		openTextViewer("text text text");
		getTextViewer().setSelectedRange(0, 4);
		initializeFindReplaceUIForTextViewer();
		DialogAccess dialog= getDialog();

		assertThat(dialog.getFindText(), is("text"));

		IFindReplaceTarget target= getFindReplaceTarget();
		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.performReplaceAndFind();

		assertEquals(" text text", getTextViewer().getDocument().get());
		assertEquals(1, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);
	}

	/*
	 * Test for https://github.com/eclipse-platform/eclipse.platform.ui/pull/1805#pullrequestreview-1993772378
	 */
	@Test
	public void testIncrementalSearchOptionRecoveredCorrectly() {
		initializeTextViewerWithFindReplaceUI("text text text");
		DialogAccess dialog= getDialog();

		dialog.select(SearchOptions.INCREMENTAL);
		dialog.assertSelected(SearchOptions.INCREMENTAL);
		dialog.assertEnabled(SearchOptions.INCREMENTAL);

		reopenFindReplaceUIForTextViewer();
		dialog= getDialog();
		dialog.assertSelected(SearchOptions.INCREMENTAL);
		dialog.assertEnabled(SearchOptions.INCREMENTAL);
	}

	@Test
	public void testFindWithWholeWordEnabledWithMultipleWordsNotIncremental() {
		initializeTextViewerWithFindReplaceUI("two words two");
		DialogAccess dialog = getDialog();
		dialog.setFindText("two");
		dialog.select(SearchOptions.WHOLE_WORD);
		dialog.select(SearchOptions.WRAP);
		IFindReplaceTarget target= getFindReplaceTarget();

		dialog.simulateKeyboardInteractionInFindInputField(SWT.CR, false);
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
	public void testRegExSearch_nonIncremental() {
		initializeTextViewerWithFindReplaceUI("abc");
		DialogAccess dialog= getDialog();
		dialog.setFindText("(a|bc)");
		dialog.select(SearchOptions.REGEX);

		IFindReplaceTarget target= getFindReplaceTarget();
		dialog.simulateKeyboardInteractionInFindInputField(SWT.CR, false);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(1, (target.getSelection()).y);

		dialog.simulateKeyboardInteractionInFindInputField(SWT.CR, false);
		assertEquals(1, (target.getSelection()).x);
		assertEquals(2, (target.getSelection()).y);
	}

	@Test
	public void testReplaceButtonEnabledWithRegexSearched() {
		initializeTextViewerWithFindReplaceUI("one two three");

		DialogAccess dialog= getDialog();
		dialog.setFindText("two");
		dialog.select(SearchOptions.REGEX);
		dialog.setReplaceText("two2");
		dialog.performFindNext();

		assertTrue(dialog.getReplaceButton().isEnabled());
	}

}
