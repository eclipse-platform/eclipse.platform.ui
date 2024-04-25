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
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.util.ResourceBundle;

import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.util.Util;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.ui.internal.findandreplace.SearchOptions;

public class FindReplaceDialogTest extends FindReplaceUITest<DialogAccess> {

	@Override
	public DialogAccess openUIFromTextViewer(TextViewer viewer) {
		TextViewer textViewer= getTextViewer();
		Accessor fFindReplaceAction;

		fFindReplaceAction= new Accessor("org.eclipse.ui.texteditor.FindReplaceAction", getClass().getClassLoader(),
				new Class[] { ResourceBundle.class, String.class, Shell.class, IFindReplaceTarget.class },
				new Object[] { ResourceBundle.getBundle("org.eclipse.ui.texteditor.ConstructedEditorMessages"), "Editor.FindReplace.", textViewer.getControl().getShell(),
						textViewer.getFindReplaceTarget() });
		fFindReplaceAction.invoke("run", null);

		Object fFindReplaceDialogStub= fFindReplaceAction.get("fgFindReplaceDialogStub");
		if (fFindReplaceDialogStub == null)
			fFindReplaceDialogStub= fFindReplaceAction.get("fgFindReplaceDialogStubShell");
		Accessor fFindReplaceDialogStubAccessor= new Accessor(fFindReplaceDialogStub, "org.eclipse.ui.texteditor.FindReplaceAction$FindReplaceDialogStub", getClass().getClassLoader());

		Accessor dialogAccessor= new Accessor(fFindReplaceDialogStubAccessor.invoke("getDialog", null), "org.eclipse.ui.texteditor.FindReplaceDialog", getClass().getClassLoader());
		return new DialogAccess(dialogAccessor);
	}

	@Test
	public void testFocusNotChangedWhenEnterPressed() {
		assumeFalse("On Mac, checkboxes only take focus if 'Full Keyboard Access' is enabled in the system preferences", Util.isMac());

		initializeTextViewerWithFindReplaceUI("line\nline\nline");
		DialogAccess dialog= getDialog();

		dialog.findCombo.setFocus();
		dialog.setFindText("line");
		dialog.simulateEnterInFindInputField(false);
		dialog.ensureHasFocusOnGTK();

		assertTrue(dialog.getFindCombo().isFocusControl());

		Button wrapCheckBox= dialog.getButtonForSearchOption(SearchOptions.WRAP);
		Button globalRadioButton= dialog.getButtonForSearchOption(SearchOptions.GLOBAL);
		wrapCheckBox.setFocus();
		dialog.simulateEnterInFindInputField(false);
		assertTrue(wrapCheckBox.isFocusControl());

		globalRadioButton.setFocus();
		dialog.simulateEnterInFindInputField(false);
		assertTrue(globalRadioButton.isFocusControl());
	}

	@Test
	public void testFocusNotChangedWhenButtonMnemonicPressed() {
		assumeFalse("Mac does not support mnemonics", Util.isMac());

		initializeTextViewerWithFindReplaceUI("");
		DialogAccess dialog= getDialog();

		dialog.setFindText("line");
		dialog.ensureHasFocusOnGTK();

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
		dialog.ensureHasFocusOnGTK();
		IFindReplaceTarget target= dialog.getTarget();

		dialog.simulateEnterInFindInputField(false);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.simulateEnterInFindInputField(false);
		assertEquals(5, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		dialog.simulateEnterInFindInputField(true);
		assertEquals(0, (target.getSelection()).x);
		assertEquals(4, (target.getSelection()).y);

		// This part only makes sense for the FindReplaceDialog since not every UI might have stored
		// the search direction as a state
		dialog.unselect(SearchOptions.FORWARD);
		dialog.simulateEnterInFindInputField(true);
		assertEquals(5, (target.getSelection()).x);
	}
}
