/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.rules.TestName;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.util.Util;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;

import org.eclipse.ui.internal.findandreplace.FindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.IFindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.SearchOptions;

class DialogAccess implements IFindReplaceUIAccess {

	FindReplaceLogic findReplaceLogic;

	Combo findCombo;

	Combo replaceCombo;

	Button forwardRadioButton;

	Button globalRadioButton;

	Button searchInRangeRadioButton;

	Button caseCheckBox;

	Button wrapCheckBox;

	Button wholeWordCheckBox;

	Button incrementalCheckBox;

	Button regExCheckBox;

	Button findButton;

	Button replaceButton;

	Button replaceFindButton;

	Button replaceAllButton;

	private Supplier<Shell> shellRetriever;

	private Runnable closeOperation;

	Accessor dialogAccessor;

	DialogAccess(Accessor findReplaceDialogAccessor) {
		dialogAccessor= findReplaceDialogAccessor;
		findReplaceLogic= (FindReplaceLogic) findReplaceDialogAccessor.get("findReplaceLogic");
		findCombo= (Combo) findReplaceDialogAccessor.get("fFindField");
		replaceCombo= (Combo) findReplaceDialogAccessor.get("fReplaceField");
		forwardRadioButton= (Button) findReplaceDialogAccessor.get("fForwardRadioButton");
		globalRadioButton= (Button) findReplaceDialogAccessor.get("fGlobalRadioButton");
		searchInRangeRadioButton= (Button) findReplaceDialogAccessor.get("fSelectedRangeRadioButton");
		caseCheckBox= (Button) findReplaceDialogAccessor.get("fCaseCheckBox");
		wrapCheckBox= (Button) findReplaceDialogAccessor.get("fWrapCheckBox");
		wholeWordCheckBox= (Button) findReplaceDialogAccessor.get("fWholeWordCheckBox");
		incrementalCheckBox= (Button) findReplaceDialogAccessor.get("fIncrementalCheckBox");
		regExCheckBox= (Button) findReplaceDialogAccessor.get("fIsRegExCheckBox");
		shellRetriever= () -> ((Shell) findReplaceDialogAccessor.get("fActiveShell"));
		closeOperation= () -> findReplaceDialogAccessor.invoke("close", null);
		findButton= (Button) findReplaceDialogAccessor.get("fFindNextButton");
		replaceButton= (Button) findReplaceDialogAccessor.get("fReplaceSelectionButton");
		replaceFindButton= (Button) findReplaceDialogAccessor.get("fReplaceFindButton");
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
		if (option == SearchOptions.GLOBAL) {
			button= searchInRangeRadioButton;
			button.setSelection(true);
			globalRadioButton.setSelection(false);
		} else {
			button.setSelection(false);
		}
		button.notifyListeners(SWT.Selection, null);
	}


	@Override
	public void closeAndRestore() {
		restoreInitialConfiguration();
		assertInitialConfiguration();
		closeOperation.run();
	}

	@Override
	public void close() {
		closeOperation.run();
	}

	@Rule
	public TestName name= new TestName();

	@Override
	public void ensureHasFocusOnGTK() {
		if (Util.isGtk()) {
			// Ensure workbench has focus on GTK
			FindReplaceUITest.runEventQueue();
			if (shellRetriever.get() == null) {
				String screenshotPath= ScreenshotTest.takeScreenshot(FindReplaceUITest.class, name.getMethodName(), System.out);
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
	public void setFindText(String text) {
		findCombo.setText(text);
		findCombo.notifyListeners(SWT.Modify, null);
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
	public IFindReplaceLogic getFindReplaceLogic() {
		return findReplaceLogic;
	}

	@Override
	public void performReplaceAll() {
		replaceAllButton.notifyListeners(SWT.Selection, null);
	}

	@Override
	public void performReplace() {
		replaceButton.notifyListeners(SWT.Selection, null);
	}

	@Override
	public void performReplaceAndFind() {
		replaceFindButton.notifyListeners(SWT.Selection, null);
	}

	@Override
	public void assertInitialConfiguration() {
		assertTrue(findReplaceLogic.isActive(SearchOptions.FORWARD));
		assertFalse(findReplaceLogic.isActive(SearchOptions.CASE_SENSITIVE));
		assertTrue(findReplaceLogic.isActive(SearchOptions.WRAP));
		assertFalse(findReplaceLogic.isActive(SearchOptions.INCREMENTAL));
		assertFalse(findReplaceLogic.isActive(SearchOptions.REGEX));
		assertFalse(findReplaceLogic.isActive(SearchOptions.WHOLE_WORD));

		assertSelected(SearchOptions.FORWARD);
		if (!doesTextViewerHaveMultiLineSelection(findReplaceLogic.getTarget())) {
			assertSelected(SearchOptions.GLOBAL);
			assertTrue(findReplaceLogic.isActive(SearchOptions.GLOBAL));
		} else {
			assertUnselected(SearchOptions.GLOBAL);
			assertFalse(findReplaceLogic.isActive(SearchOptions.GLOBAL));
		}
		assertSelected(SearchOptions.WRAP);

		assertEnabled(SearchOptions.WRAP);
		assertEnabled(SearchOptions.CASE_SENSITIVE);
		assertEnabled(SearchOptions.REGEX);
		assertEnabled(SearchOptions.INCREMENTAL);

		assertUnselected(SearchOptions.CASE_SENSITIVE);
		assertUnselected(SearchOptions.REGEX);
		assertUnselected(SearchOptions.INCREMENTAL);

		String findString= getFindText();
		if (getEnabledOptions().contains(SearchOptions.WHOLE_WORD)) {
			assertFalse(findString.isEmpty());
			assertFalse(findString.contains(" "));
		}
	}

	private boolean doesTextViewerHaveMultiLineSelection(IFindReplaceTarget target) {
		if (target instanceof IFindReplaceTargetExtension scopeProvider) {
			return scopeProvider.getScope() != null; // null is returned for global scope
		}
		return false;
	}

	@Override
	public void assertEnabled(SearchOptions option) {
		Set<SearchOptions> enabled= getEnabledOptions();
		assertThat(enabled, hasItems(option));
	}

	@Override
	public void assertDisabled(SearchOptions option) {
		Set<SearchOptions> enabled= getEnabledOptions();
		assertThat(enabled, not(hasItems(option)));
	}

	@Override
	public void assertSelected(SearchOptions option) {
		Set<SearchOptions> enabled= getSelectedOptions();
		assertThat(enabled, hasItems(option));
	}

	@Override
	public void assertUnselected(SearchOptions option) {
		Set<SearchOptions> enabled= getSelectedOptions();
		assertThat(enabled, not(hasItems(option)));
	}

	private Set<SearchOptions> getEnabledOptions() {
		return Arrays.stream(SearchOptions.values())
				.filter(option -> getButtonForSearchOption(option).isEnabled())
				.collect(Collectors.toSet());
	}

	private Set<SearchOptions> getSelectedOptions() {
		return Arrays.stream(SearchOptions.values())
				.filter(option -> getButtonForSearchOption(option).getSelection())
				.collect(Collectors.toSet());
	}

}
