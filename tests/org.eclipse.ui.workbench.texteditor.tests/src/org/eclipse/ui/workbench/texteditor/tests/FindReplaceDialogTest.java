package org.eclipse.ui.workbench.texteditor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

import org.eclipse.ui.internal.findandreplace.IFindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.SearchOptions;

public class FindReplaceDialogTest extends FindReplaceUITest<DialogAccess> {

	@Override
	public DialogAccess openUIFromTextViewer(TextViewer viewer) {
		DialogAccess ret;
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
		ret= new DialogAccess(this, dialogAccessor);
		return ret;
	}

	@Test
	public void testFocusNotChangedWhenEnterPressed() {
		initializeFindReplaceUI("");

		dialog.setFindText("line", true);
		dialog.simulateEnterInFindInputField(false);
		dialog.ensureHasFocusOnGTK();

		if (Util.isMac())
			/* On the Mac, checkboxes only take focus if "Full Keyboard Access" is enabled in the System Preferences.
			 * Let's not assume that someone pressed Ctrl+F7 on every test machine... */
			return;



		assertTrue(dialog.getFindCombo().isFocusControl());

		Button wrapCheckBox = dialog.getButtonForSearchOption(SearchOptions.WRAP);
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
		if (Util.isMac())
			return; // Mac doesn't support mnemonics.

		initializeFindReplaceUI("");

		dialog.setFindText("line", false);
		dialog.ensureHasFocusOnGTK();

		Button btn= dialog.getButtonForSearchOption(SearchOptions.WRAP);
		btn.setFocus();
		final Event event= new Event();
		event.detail= SWT.TRAVERSE_MNEMONIC;
		event.character= 'n';
		event.doit= false;
		btn.traverse(SWT.TRAVERSE_MNEMONIC, event);
		runEventQueue();
		assertTrue(btn.isFocusControl());

		btn= dialog.getButtonForSearchOption(SearchOptions.GLOBAL);
		btn.setFocus();
		event.detail= SWT.TRAVERSE_MNEMONIC;
		event.doit= false;
		btn.traverse(SWT.TRAVERSE_MNEMONIC, event);
		runEventQueue();
		assertTrue(btn.isFocusControl());

		event.detail= SWT.TRAVERSE_MNEMONIC;
		event.character= 'r';
		event.doit= false;
		btn.traverse(SWT.TRAVERSE_MNEMONIC, event);
		runEventQueue();
		assertTrue(btn.isFocusControl());
	}

	@Override
	public void assertInitialConfiguration() {
		IFindReplaceLogic findReplaceLogic= dialog.getFindReplaceLogic();
		assertTrue(findReplaceLogic.isActive(SearchOptions.FORWARD));
		assertTrue(findReplaceLogic.isActive(SearchOptions.GLOBAL));
		assertFalse(findReplaceLogic.isActive(SearchOptions.CASE_SENSITIVE));
		assertTrue(findReplaceLogic.isActive(SearchOptions.WRAP));
		assertFalse(findReplaceLogic.isActive(SearchOptions.INCREMENTAL));
		assertFalse(findReplaceLogic.isActive(SearchOptions.REGEX));
		assertFalse(findReplaceLogic.isActive(SearchOptions.WHOLE_WORD));

		assertSelected(SearchOptions.FORWARD);
		assertSelected(SearchOptions.GLOBAL);
		assertSelected(SearchOptions.WRAP);

		assertEnabled(SearchOptions.WRAP);
		assertEnabled(SearchOptions.CASE_SENSITIVE);
		assertEnabled(SearchOptions.REGEX);
		assertEnabled(SearchOptions.INCREMENTAL);

		assertUnselected(SearchOptions.CASE_SENSITIVE);
		assertUnselected(SearchOptions.REGEX);
		assertUnselected(SearchOptions.INCREMENTAL);

		String findString= dialog.getFindText();
		if (dialog.getEnabledOptions().contains(SearchOptions.WHOLE_WORD)) {
			assertFalse(findString.isEmpty());
			assertFalse(findString.contains(" "));
		}
	}


	@Test
	public void testShiftEnterReversesSearchDirectionDialogSpecific() {
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

		// This part only makes sense for the FindReplaceDialog since not every UI might have stored
		// the search direction as a state
		dialog.unselect(SearchOptions.FORWARD);
		dialog.simulateEnterInFindInputField(true);
		assertEquals(5, (target.getSelection()).x);
	}
}
