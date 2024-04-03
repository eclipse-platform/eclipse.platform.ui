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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ResourceBundle;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.util.Util;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.findandreplace.FindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.SearchOptions;

/**
 * Tests the FindReplaceDialog.
 *
 * @since 3.1
 */
public class FindReplaceDialogTest {

	@Rule
	public TestName testName= new TestName();

	private TextViewer fTextViewer;

	private static void runEventQueue() {
		Display display= PlatformUI.getWorkbench().getDisplay();
		for (int i= 0; i < 10; i++) { // workaround for https://bugs.eclipse.org/323272
			while (display.readAndDispatch()) {
				// do nothing
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}

	private DialogAccess dialog;

	private class DialogAccess {
		FindReplaceLogic findReplaceLogic;

		Combo findCombo;

		Button forwardRadioButton;

		Button globalRadioButton;

		Button caseCheckBox;

		Button wrapCheckBox;

		Button wholeWordCheckBox;

		Button incrementalCheckBox;

		Button regExCheckBox;

		Button replaceFindButton;

		private Supplier<Shell> shellRetriever;

		private Runnable closeOperation;


		DialogAccess(Accessor findReplaceDialogAccessor, boolean checkInitialConfiguration) {
			findReplaceLogic= (FindReplaceLogic) findReplaceDialogAccessor.get("findReplaceLogic");
			findCombo= (Combo) findReplaceDialogAccessor.get("fFindField");
			forwardRadioButton= (Button) findReplaceDialogAccessor.get("fForwardRadioButton");
			globalRadioButton= (Button) findReplaceDialogAccessor.get("fGlobalRadioButton");
			caseCheckBox= (Button) findReplaceDialogAccessor.get("fCaseCheckBox");
			wrapCheckBox= (Button) findReplaceDialogAccessor.get("fWrapCheckBox");
			wholeWordCheckBox= (Button) findReplaceDialogAccessor.get("fWholeWordCheckBox");
			incrementalCheckBox= (Button) findReplaceDialogAccessor.get("fIncrementalCheckBox");
			regExCheckBox= (Button) findReplaceDialogAccessor.get("fIsRegExCheckBox");
			replaceFindButton= (Button) findReplaceDialogAccessor.get("fReplaceFindButton");
			shellRetriever= () -> ((Shell) findReplaceDialogAccessor.get("fActiveShell"));
			closeOperation= () -> findReplaceDialogAccessor.invoke("close", null);
			if (checkInitialConfiguration) {
				assertInitialConfiguration();
			}
		}

		void restoreInitialConfiguration() {
			findCombo.setText("");
			select(forwardRadioButton);
			select(globalRadioButton);
			unselect(incrementalCheckBox);
			unselect(regExCheckBox);
			unselect(caseCheckBox);
			unselect(wholeWordCheckBox);
			select(wrapCheckBox);
		}

		private void assertInitialConfiguration() {
			assertTrue(findReplaceLogic.isActive(SearchOptions.FORWARD));
			assertTrue(forwardRadioButton.getSelection());
			assertTrue(findReplaceLogic.isActive(SearchOptions.GLOBAL));
			assertTrue(globalRadioButton.getSelection());
			assertFalse(findReplaceLogic.isActive(SearchOptions.CASE_SENSITIVE));
			assertTrue(caseCheckBox.isEnabled());
			assertFalse(caseCheckBox.getSelection());
			assertTrue(findReplaceLogic.isActive(SearchOptions.WRAP));
			assertTrue(wrapCheckBox.isEnabled());
			assertTrue(wrapCheckBox.getSelection());
			assertFalse(findReplaceLogic.isActive(SearchOptions.WHOLE_WORD));
			String searchString= findCombo.getText();
			assertEquals(wholeWordCheckBox.isEnabled(), !searchString.isEmpty() && !searchString.contains(" "));
			assertFalse(wholeWordCheckBox.getSelection());
			assertFalse(findReplaceLogic.isActive(SearchOptions.INCREMENTAL));
			assertTrue(incrementalCheckBox.isEnabled());
			assertFalse(incrementalCheckBox.getSelection());
			assertFalse(findReplaceLogic.isActive(SearchOptions.REGEX));
			assertTrue(regExCheckBox.isEnabled());
			assertFalse(regExCheckBox.getSelection());
		}

		void closeAndRestore() {
			restoreInitialConfiguration();
			assertInitialConfiguration();
			close();
		}

		void close() {
			closeOperation.run();
		}

		private void ensureHasFocusOnGTK() {
			if (Util.isGtk()) {
				// Ensure workbench has focus on GTK
				runEventQueue();
				if (shellRetriever.get() == null) {
					String screenshotPath= ScreenshotTest.takeScreenshot(FindReplaceDialogTest.class, testName.getMethodName(), System.out);
					fail("this test does not work on GTK unless the runtime workbench has focus. Screenshot: " + screenshotPath);
				}
			}
		}

	}

	private void openFindReplaceDialog() {
		Shell shell= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		Accessor dialogAccessor= new Accessor("org.eclipse.ui.texteditor.FindReplaceDialog", getClass().getClassLoader(), new Object[] { shell });
		dialogAccessor.invoke("create", null);
		dialog= new DialogAccess(dialogAccessor, true);
	}

	private void openTextViewerAndFindReplaceDialog() {
		openTextViewerAndFindReplaceDialog("line\nline\nline");
	}

	private void openTextViewerAndFindReplaceDialog(String content) {
		openTextViewer(content);
		openFindReplaceDialogForTextViewer();
	}

	private void openTextViewer(String content) {
		fTextViewer= new TextViewer(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		fTextViewer.setDocument(new Document(content));
		fTextViewer.getControl().setFocus();
	}

	private void openFindReplaceDialogForTextViewer() {
		openFindReplaceDialogForTextViewer(true);
	}

	private void openFindReplaceDialogForTextViewer(boolean checkInitialConfiguration) {
		Accessor fFindReplaceAction;
		fFindReplaceAction= new Accessor("org.eclipse.ui.texteditor.FindReplaceAction", getClass().getClassLoader(),
				new Class[] { ResourceBundle.class, String.class, Shell.class, IFindReplaceTarget.class },
				new Object[] { ResourceBundle.getBundle("org.eclipse.ui.texteditor.ConstructedEditorMessages"), "Editor.FindReplace.", fTextViewer.getControl().getShell(),
						fTextViewer.getFindReplaceTarget() });
		fFindReplaceAction.invoke("run", null);

		Object fFindReplaceDialogStub= fFindReplaceAction.get("fgFindReplaceDialogStub");
		if (fFindReplaceDialogStub == null)
			fFindReplaceDialogStub= fFindReplaceAction.get("fgFindReplaceDialogStubShell");
		Accessor fFindReplaceDialogStubAccessor= new Accessor(fFindReplaceDialogStub, "org.eclipse.ui.texteditor.FindReplaceAction$FindReplaceDialogStub", getClass().getClassLoader());

		Accessor dialogAccessor= new Accessor(fFindReplaceDialogStubAccessor.invoke("getDialog", null), "org.eclipse.ui.texteditor.FindReplaceDialog", getClass().getClassLoader());
		dialog= new DialogAccess(dialogAccessor, checkInitialConfiguration);
	}

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
		openFindReplaceDialog();
	}

	@Test
	public void testDisableWholeWordIfRegEx() {
		openFindReplaceDialog();
		fTextViewer= new TextViewer(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		fTextViewer.setDocument(new Document("line\nline\nline"));
		fTextViewer.getControl().setFocus();

		dialog.findCombo.setText("word");
		assertTrue(dialog.regExCheckBox.isEnabled());
		assertTrue(dialog.wholeWordCheckBox.isEnabled());

		dialog.findReplaceLogic.updateTarget(fTextViewer.getFindReplaceTarget(), false);
		select(dialog.wholeWordCheckBox);
		select(dialog.regExCheckBox);
		assertTrue(dialog.regExCheckBox.isEnabled());
		assertFalse(dialog.wholeWordCheckBox.isEnabled());
		assertTrue(dialog.wholeWordCheckBox.getSelection());
	}

	@Test
	public void testDisableWholeWordIfNotWord() {
		openFindReplaceDialog();
		select(dialog.wholeWordCheckBox);

		dialog.findCombo.setText("word");
		assertTrue(dialog.regExCheckBox.isEnabled());
		assertTrue(dialog.wholeWordCheckBox.isEnabled());
		assertTrue(dialog.wholeWordCheckBox.getSelection());

		dialog.findCombo.setText("no word");
		assertTrue(dialog.regExCheckBox.isEnabled());
		assertFalse(dialog.wholeWordCheckBox.isEnabled());
		assertTrue(dialog.wholeWordCheckBox.getSelection());
	}

	@Test
	public void testFocusNotChangedWhenEnterPressed() {
		openTextViewerAndFindReplaceDialog();

		dialog.findCombo.setFocus();
		dialog.findCombo.setText("line");
		simulateEnterInFindInputField(false);
		dialog.ensureHasFocusOnGTK();

		if (Util.isMac()) {
			/* On the Mac, checkboxes only take focus if "Full Keyboard Access" is enabled in the System Preferences.
			 * Let's not assume that someone pressed Ctrl+F7 on every test machine... */
			return;
		}

		assertTrue(dialog.findCombo.isFocusControl());

		dialog.wrapCheckBox.setFocus();
		simulateEnterInFindInputField(false);
		assertTrue(dialog.wrapCheckBox.isFocusControl());

		dialog.globalRadioButton.setFocus();
		simulateEnterInFindInputField(false);
		assertTrue(dialog.globalRadioButton.isFocusControl());
	}

	@Test
	public void testFocusNotChangedWhenButtonMnemonicPressed() {
		if (Util.isMac())
			return; // Mac doesn't support mnemonics.

		openTextViewerAndFindReplaceDialog();

		dialog.findCombo.setText("line");
		dialog.ensureHasFocusOnGTK();

		dialog.wrapCheckBox.setFocus();
		final Event event= new Event();
		event.detail= SWT.TRAVERSE_MNEMONIC;
		event.character= 'n';
		event.doit= false;
		dialog.wrapCheckBox.traverse(SWT.TRAVERSE_MNEMONIC, event);
		runEventQueue();
		assertTrue(dialog.wrapCheckBox.isFocusControl());

		dialog.globalRadioButton.setFocus();
		event.detail= SWT.TRAVERSE_MNEMONIC;
		event.doit= false;
		dialog.globalRadioButton.traverse(SWT.TRAVERSE_MNEMONIC, event);
		runEventQueue();
		assertTrue(dialog.globalRadioButton.isFocusControl());

		event.detail= SWT.TRAVERSE_MNEMONIC;
		event.character= 'r';
		event.doit= false;
		dialog.globalRadioButton.traverse(SWT.TRAVERSE_MNEMONIC, event);
		runEventQueue();
		assertTrue(dialog.globalRadioButton.isFocusControl());
	}

	@Test
	public void testShiftEnterReversesSearchDirection() {
		openTextViewerAndFindReplaceDialog();

		dialog.findCombo.setText("line");
		dialog.ensureHasFocusOnGTK();
		IFindReplaceTarget target= dialog.findReplaceLogic.getTarget();

		simulateEnterInFindInputField(false);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		simulateEnterInFindInputField(false);
		assertEquals(5, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		simulateEnterInFindInputField(true);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		unselect(dialog.forwardRadioButton);
		simulateEnterInFindInputField(true);
		assertEquals(5, (target.getSelection()).x);
	}

	@Test
	public void testChangeInputForIncrementalSearch() {
		openTextViewerAndFindReplaceDialog();
		select(dialog.incrementalCheckBox);

		dialog.findCombo.setText("lin");
		IFindReplaceTarget target= dialog.findReplaceLogic.getTarget();
		assertEquals(0, (target.getSelection()).x);
		assertEquals(dialog.findCombo.getText().length(), (target.getSelection()).y);

		dialog.findCombo.setText("line");
		assertEquals(0, (target.getSelection()).x);
		assertEquals(dialog.findCombo.getText().length(), (target.getSelection()).y);
	}

	@Test
	public void testFindWithWholeWordEnabledWithMultipleWords() {
		openTextViewerAndFindReplaceDialog("two words");
		dialog.findCombo.setText("two");
		select(dialog.wholeWordCheckBox);
		IFindReplaceTarget target= dialog.findReplaceLogic.getTarget();
		assertEquals(0, (target.getSelection()).x);
		assertEquals(0, (target.getSelection()).y);

		dialog.findCombo.setText("two wo");
		assertFalse(dialog.wholeWordCheckBox.getEnabled());
		assertTrue(dialog.wholeWordCheckBox.getSelection());

		simulateEnterInFindInputField(false);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(dialog.findCombo.getText().length(), (target.getSelection()).y);
	}

	@Test
	public void testReplaceAndFindAfterInitializingFindWithSelectedString() {
		openTextViewer("text text text");
		fTextViewer.setSelectedRange(0, 4);
		openFindReplaceDialogForTextViewer();

		IFindReplaceTarget target= dialog.findReplaceLogic.getTarget();
		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);
		select(dialog.replaceFindButton);

		assertEquals(" text text", fTextViewer.getDocument().get());
		assertEquals(1, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);
	}

	@Test
	public void testActivateWholeWordsAndSearchForTwoWords() {
		openTextViewer("text text text");
		openFindReplaceDialogForTextViewer();

		dialog.wholeWordCheckBox.setSelection(true);

		dialog.findCombo.setText("text text");
		assertTrue(dialog.wholeWordCheckBox.getSelection());
		assertFalse(dialog.wholeWordCheckBox.getEnabled());

		dialog.findCombo.setText("text");
		assertTrue(dialog.wholeWordCheckBox.getSelection());
		assertTrue(dialog.wholeWordCheckBox.getEnabled());

		dialog.regExCheckBox.setSelection(true);
		dialog.regExCheckBox.notifyListeners(SWT.Selection, null);
		runEventQueue();
		assertTrue(dialog.wholeWordCheckBox.getSelection());
		assertFalse(dialog.wholeWordCheckBox.getEnabled());
	}

	@Test
	public void testIncrementalSearchOnlyEnabledWhenAllowed() {
		openTextViewer("text text text");
		openFindReplaceDialogForTextViewer();

		dialog.incrementalCheckBox.setSelection(true);
		select(dialog.regExCheckBox);
		runEventQueue();
		assertTrue(dialog.incrementalCheckBox.getSelection());
		assertFalse(dialog.incrementalCheckBox.getEnabled());
	}

	/*
	 * Test for https://github.com/eclipse-platform/eclipse.platform.ui/pull/1805#pullrequestreview-1993772378
	 */
	@Test
	public void testIncrementalSearchOptionRecoveredCorrectly() {
		openTextViewer("text text text");
		openFindReplaceDialogForTextViewer();

		select(dialog.incrementalCheckBox);
		assertTrue(dialog.incrementalCheckBox.getSelection());
		assertTrue(dialog.incrementalCheckBox.getEnabled());

		dialog.close();
		openFindReplaceDialogForTextViewer(false);

		assertTrue(dialog.incrementalCheckBox.getSelection());
		assertTrue(dialog.incrementalCheckBox.getEnabled());

		select(dialog.incrementalCheckBox);
		select(dialog.regExCheckBox);
		assertTrue(dialog.incrementalCheckBox.getSelection());
		assertFalse(dialog.incrementalCheckBox.getEnabled());

		dialog.close();
		openFindReplaceDialogForTextViewer(false);

		assertTrue(dialog.incrementalCheckBox.getSelection());
		assertFalse(dialog.incrementalCheckBox.getEnabled());
	}

	private static void select(Button button) {
		button.setSelection(true);
		button.notifyListeners(SWT.Selection, null);
	}

	private static void unselect(Button button) {
		button.setSelection(false);
		button.notifyListeners(SWT.Selection, null);
	}

	private void simulateEnterInFindInputField(boolean shiftPressed) {
		final Event event= new Event();
		event.type= SWT.Traverse;
		event.detail= SWT.TRAVERSE_RETURN;
		event.character= SWT.CR;
		if (shiftPressed) {
			event.stateMask= SWT.SHIFT;
		}
		dialog.findCombo.traverse(SWT.TRAVERSE_RETURN, event);
		runEventQueue();
	}

}
