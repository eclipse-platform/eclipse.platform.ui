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

import static org.eclipse.ui.internal.findandreplace.FindReplaceTestUtil.runEventQueue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;

import org.eclipse.ui.internal.findandreplace.IFindReplaceUIAccess;
import org.eclipse.ui.internal.findandreplace.SearchOptions;
import org.eclipse.ui.internal.findandreplace.WidgetExtractor;

class DialogAccess implements IFindReplaceUIAccess {

	private static final String DATA_ID = "org.eclipse.ui.texteditor.FindReplaceDialog.id";

	private final IFindReplaceTarget findReplaceTarget;

	private final Dialog findReplaceDialog;

	private final Combo findCombo;

	private final Combo replaceCombo;

	private final Button forwardRadioButton;

	private final Button globalRadioButton;

	private final Button searchInRangeRadioButton;

	private final Button caseCheckBox;

	private final Button wrapCheckBox;

	private final Button wholeWordCheckBox;

	private final Button incrementalCheckBox;

	private final Button regExCheckBox;

	private final Button replaceButton;

	private final Button replaceFindButton;

	private final Button replaceAllButton;

	private final Button findNextButton;

	DialogAccess(IFindReplaceTarget findReplaceTarget, Dialog findReplaceDialog) {
		this.findReplaceTarget= findReplaceTarget;
		this.findReplaceDialog= findReplaceDialog;
		WidgetExtractor widgetExtractor= new WidgetExtractor(DATA_ID, findReplaceDialog.getShell());
		findCombo= widgetExtractor.findCombo("searchInput");
		replaceCombo= widgetExtractor.findCombo("replaceInput");
		forwardRadioButton= widgetExtractor.findButton("searchForward");
		globalRadioButton= widgetExtractor.findButton("globalSearch");
		searchInRangeRadioButton= widgetExtractor.findButton("searchInSelection");
		caseCheckBox= widgetExtractor.findButton("caseSensitiveSearch");
		wrapCheckBox= widgetExtractor.findButton("wrappedSearch");
		wholeWordCheckBox= widgetExtractor.findButton("wholeWordSearch");
		incrementalCheckBox= widgetExtractor.findButton("incrementalSearch");
		regExCheckBox= widgetExtractor.findButton("regExSearch");

		replaceButton= widgetExtractor.findButton("replaceOne");
		replaceFindButton= widgetExtractor.findButton("replaceFindOne");
		replaceAllButton= widgetExtractor.findButton("replaceAll");
		findNextButton= widgetExtractor.findButton("findNext");
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
		findReplaceDialog.close();
	}

	@Override
	public void close() {
		findReplaceDialog.close();
	}

	@Override
	public boolean hasFocus() {
		Control focusControl= findReplaceDialog.getShell().getDisplay().getFocusControl();
		Shell focusControlShell= focusControl != null ? focusControl.getShell() : null;
		return focusControlShell == findReplaceDialog.getShell();
	}

	@Override
	public void simulateKeyboardInteractionInFindInputField(int keyCode, boolean shiftPressed) {
		final Event event= new Event();
		event.detail= SWT.TRAVERSE_RETURN;
		event.type= SWT.KeyDown;
		if (shiftPressed) {
			event.stateMask= SWT.SHIFT;
		}
		event.keyCode= keyCode;
		findCombo.traverse(SWT.TRAVERSE_RETURN, event);
		runEventQueue();
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
		replaceCombo.notifyListeners(SWT.Modify, null);
	}

	@Override
	public String getFindText() {
		return findCombo.getText();
	}

	@Override
	public String getSelectedFindText() {
		Point selection = findCombo.getSelection();
		return findCombo.getText().substring(selection.x, selection.y);
	}

	public Button getReplaceButton() {
		return replaceButton;
	}

	public Combo getFindCombo() {
		return findCombo;
	}

	@Override
	public void performReplaceAll() {
		replaceAllButton.notifyListeners(SWT.Selection, null);
	}

	@Override
	public void performReplace() {
		replaceButton.notifyListeners(SWT.Selection, null);
	}

	public void performFindNext() {
		findNextButton.notifyListeners(SWT.Selection, null);
	}

	@Override
	public void performReplaceAndFind() {
		replaceFindButton.notifyListeners(SWT.Selection, null);
	}

	@Override
	public void assertInitialConfiguration() {
		assertSelected(SearchOptions.FORWARD);
		if (!doesTextViewerHaveMultiLineSelection(findReplaceTarget)) {
			assertSelected(SearchOptions.GLOBAL);
		} else {
			assertUnselected(SearchOptions.GLOBAL);
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

	@Override
	public boolean isShown() {
		return findReplaceDialog.getShell().isVisible();
	}

}
