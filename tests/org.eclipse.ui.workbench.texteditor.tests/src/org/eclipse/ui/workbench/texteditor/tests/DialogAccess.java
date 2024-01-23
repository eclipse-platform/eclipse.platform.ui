package org.eclipse.ui.workbench.texteditor.tests;

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.util.Util;

import org.eclipse.jface.text.IFindReplaceTarget;

import org.eclipse.ui.internal.findandreplace.FindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.IFindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.SearchOptions;

class DialogAccess implements IFindReplaceUIAccess {

	FindReplaceLogic findReplaceLogic;

	Combo findCombo;

	Combo replaceCombo;

	Button forwardRadioButton;

	Button globalRadioButton;

	Button caseCheckBox;

	Button wrapCheckBox;

	Button wholeWordCheckBox;

	Button incrementalCheckBox;

	Button regExCheckBox;

	Button findButton;

	Button replaceButton;

	Button replaceAllButton;

	private Supplier<Shell> shellRetriever;

	private Runnable closeOperation;

	private FindReplaceUITest<DialogAccess> parentTest;

	Accessor dialogAccessor;

	DialogAccess(FindReplaceUITest<DialogAccess> parent, Accessor findReplaceDialogAccessor) {
		dialogAccessor= findReplaceDialogAccessor;
		parentTest= parent;
		findReplaceLogic= (FindReplaceLogic) findReplaceDialogAccessor.get("findReplaceLogic");
		findCombo= (Combo) findReplaceDialogAccessor.get("fFindField");
		replaceCombo= (Combo) findReplaceDialogAccessor.get("fReplaceField");
		forwardRadioButton= (Button) findReplaceDialogAccessor.get("fForwardRadioButton");
		globalRadioButton= (Button) findReplaceDialogAccessor.get("fGlobalRadioButton");
		caseCheckBox= (Button) findReplaceDialogAccessor.get("fCaseCheckBox");
		wrapCheckBox= (Button) findReplaceDialogAccessor.get("fWrapCheckBox");
		wholeWordCheckBox= (Button) findReplaceDialogAccessor.get("fWholeWordCheckBox");
		incrementalCheckBox= (Button) findReplaceDialogAccessor.get("fIncrementalCheckBox");
		regExCheckBox= (Button) findReplaceDialogAccessor.get("fIsRegExCheckBox");
		shellRetriever= () -> ((Shell) findReplaceDialogAccessor.get("fActiveShell"));
		closeOperation= () -> findReplaceDialogAccessor.invoke("close", null);
		findButton= (Button) findReplaceDialogAccessor.get("fFindNextButton");
		replaceButton= (Button) findReplaceDialogAccessor.get("fReplaceSelectionButton");
		replaceAllButton= (Button) findReplaceDialogAccessor.get("fReplaceAllButton");
	}

	void restoreInitialConfiguration() {
		findCombo.setText("");
		select(SearchOptions.FORWARD);
		select(SearchOptions.GLOBAL);
		select(SearchOptions.WRAP);
		unselect(SearchOptions.INCREMENTAL);
		unselect(SearchOptions.REGEX);
		unselect(SearchOptions.CASE_SENSITIVE);
		unselect(SearchOptions.WHOLE_WORD);
	}

	@Override
	public Button getButtonForSearchOption(SearchOptions option) {
		switch (option) {
			case CASE_SENSITIVE:
				return caseCheckBox;
			case FORWARD:
				return forwardRadioButton;
			case GLOBAL:
				return globalRadioButton;
			case INCREMENTAL:
				return incrementalCheckBox;
			case REGEX:
				return regExCheckBox;
			case WHOLE_WORD:
				return wholeWordCheckBox;
			case WRAP:
				return wrapCheckBox;
			default:
				return null;
		}
	}

	@Override
	public void select(SearchOptions option) {
		Button button= getButtonForSearchOption(option);
		button.setSelection(true);
		button.notifyListeners(SWT.Selection, null);
	}

	@Override
	public void unselect(SearchOptions option) {
		Button button= getButtonForSearchOption(option);
		button.setSelection(false);
		button.notifyListeners(SWT.Selection, null);
	}


	@Override
	public void close() {
		restoreInitialConfiguration();
		parentTest.assertInitialConfiguration();
		closeOperation.run();
	}

	@Override
	public void ensureHasFocusOnGTK() {
		if (Util.isGtk()) {
			// Ensure workbench has focus on GTK
			FindReplaceUITest.runEventQueue();
			if (shellRetriever.get() == null) {
				String screenshotPath= ScreenshotTest.takeScreenshot(FindReplaceUITest.class, parentTest.testName.getMethodName(), System.out);
				fail("this test does not work on GTK unless the runtime workbench has focus. Screenshot: " + screenshotPath);
			}
		}
	}

	@Override
	public void simulateEnterInFindInputField(boolean shiftPressed) {
		simulateKeyPressInFindInputField(SWT.CR, shiftPressed);
	}

	@Override
	public void simulateKeyPressInFindInputField(int keyCode, boolean shiftPressed) {
		final Event event= new Event();
		event.type= SWT.Traverse;
		event.detail= SWT.TRAVERSE_RETURN;
		event.character= (char) keyCode;
		if (shiftPressed) {
			event.stateMask= SWT.SHIFT;
		}
		findCombo.traverse(SWT.TRAVERSE_RETURN, event);
		FindReplaceUITest.runEventQueue();
	}


	@Override
	public String getReplaceText() {
		return replaceCombo.getText();
	}

	@Override
	public void setFindText(String text, boolean grabFocus) {
		findCombo.setText(text);
		findCombo.notifyListeners(SWT.Modify, null);
		if (grabFocus) {
			findCombo.setFocus();
		}
	}

	@Override
	public void setReplaceText(String text) {
		replaceCombo.setText(text);
	}

	@Override
	public IFindReplaceTarget getTarget() {
		return findReplaceLogic.getTarget();
	}

	@Override
	public String getFindText() {
		return findCombo.getText();
	}

	public Combo getFindCombo() {
		return findCombo;
	}

	@Override
	public Set<SearchOptions> getEnabledOptions() {
		HashSet<SearchOptions> ret= new HashSet<>();

		for (SearchOptions option : SearchOptions.values()) {
			if (getButtonForSearchOption(option).isEnabled()) {
				ret.add(option);
			}
		}

		return ret;
	}

	@Override
	public Set<SearchOptions> getSelectedOptions() {
		HashSet<SearchOptions> ret= new HashSet<>();

		for (SearchOptions option : SearchOptions.values()) {
			if (getButtonForSearchOption(option).getSelection()) {
				ret.add(option);
			}
		}

		return ret;
	}

	@Override
	public IFindReplaceLogic getFindReplaceLogic() {
		return findReplaceLogic;
	}

	@Override
	public void pressReplaceAll() {
		replaceAllButton.notifyListeners(SWT.Selection, null);
	}

	@Override
	public void performReplace() {
		findButton.notifyListeners(SWT.Selection, null);
		replaceButton.notifyListeners(SWT.Selection, null);
	}



}