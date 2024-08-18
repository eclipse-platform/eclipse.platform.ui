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
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace.overlay;

import static org.eclipse.ui.internal.findandreplace.FindReplaceTestUtil.runEventQueue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;

import org.eclipse.ui.internal.findandreplace.FindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.IFindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.IFindReplaceUIAccess;
import org.eclipse.ui.internal.findandreplace.SearchOptions;

class OverlayAccess implements IFindReplaceUIAccess {
	FindReplaceLogic findReplaceLogic;

	HistoryTextWrapper find;

	HistoryTextWrapper replace;

	ToolItem inSelection;

	ToolItem caseSensitive;

	ToolItem wholeWord;

	ToolItem regEx;

	ToolItem searchForward;

	ToolItem searchBackward;

	Button openReplaceDialog;

	ToolItem replaceButton;

	ToolItem replaceAllButton;

	private Runnable closeOperation;

	Accessor dialogAccessor;

	private Supplier<Shell> shellRetriever;

	OverlayAccess(Accessor findReplaceOverlayAccessor) {
		dialogAccessor= findReplaceOverlayAccessor;
		findReplaceLogic= (FindReplaceLogic) findReplaceOverlayAccessor.get("findReplaceLogic");
		find= (HistoryTextWrapper) findReplaceOverlayAccessor.get("searchBar");
		replace= (HistoryTextWrapper) findReplaceOverlayAccessor.get("replaceBar");
		caseSensitive= (ToolItem) findReplaceOverlayAccessor.get("caseSensitiveSearchButton");
		wholeWord= (ToolItem) findReplaceOverlayAccessor.get("wholeWordSearchButton");
		regEx= (ToolItem) findReplaceOverlayAccessor.get("regexSearchButton");
		searchForward= (ToolItem) findReplaceOverlayAccessor.get("searchDownButton");
		searchBackward= (ToolItem) findReplaceOverlayAccessor.get("searchUpButton");
		closeOperation= () -> findReplaceOverlayAccessor.invoke("close", null);
		openReplaceDialog= (Button) findReplaceOverlayAccessor.get("replaceToggle");
		replaceButton= (ToolItem) findReplaceOverlayAccessor.get("replaceButton");
		replaceAllButton= (ToolItem) findReplaceOverlayAccessor.get("replaceAllButton");
		inSelection= (ToolItem) findReplaceOverlayAccessor.get("searchInSelectionButton");
		shellRetriever= () -> ((Shell) findReplaceOverlayAccessor.invoke("getShell", null));
	}

	@Override
	public IFindReplaceTarget getTarget() {
		return findReplaceLogic.getTarget();
	}

	private void restoreInitialConfiguration() {
		find.setText("");
		select(SearchOptions.GLOBAL);
		unselect(SearchOptions.REGEX);
		unselect(SearchOptions.CASE_SENSITIVE);
		unselect(SearchOptions.WHOLE_WORD);
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

	@Override
	public void select(SearchOptions option) {
		ToolItem button= getButtonForSearchOption(option);
		if (button == null) {
			return;
		}
		button.setSelection(true);
		if (option == SearchOptions.GLOBAL) {
			button.setSelection(false);
		}
		button.notifyListeners(SWT.Selection, null);
	}

	@Override
	public void unselect(SearchOptions option) {
		ToolItem button= getButtonForSearchOption(option);
		if (button == null) {
			return;
		}
		button.setSelection(false);
		if (option == SearchOptions.GLOBAL) {
			button.setSelection(true);
		}
		button.notifyListeners(SWT.Selection, null);
	}

	@Override
	public void simulateKeyboardInteractionInFindInputField(int keyCode, boolean shiftPressed) {
		final Event event= new Event();
		event.type= SWT.KeyDown;
		if (shiftPressed) {
			event.stateMask= SWT.SHIFT;
		}
		event.keyCode= keyCode;
		find.notifyListeners(SWT.KeyDown, event);
		runEventQueue();
	}

	@Override
	public String getFindText() {
		return find.getText();
	}

	@Override
	public String getSelectedFindText() {
		return find.getSelectionText();
	}

	@Override
	public String getReplaceText() {
		return replace.getText();
	}

	@Override
	public void setFindText(String text) {
		find.setText(text);
		find.notifyListeners(SWT.Modify, null);
	}

	@Override
	public void setReplaceText(String text) {
		openReplaceDialog();
		replace.setText(text);
	}

	@Override
	public ToolItem getButtonForSearchOption(SearchOptions option) {
		switch (option) {
			case CASE_SENSITIVE:
				return caseSensitive;
			case REGEX:
				return regEx;
			case WHOLE_WORD:
				return wholeWord;
			case GLOBAL:
				return inSelection;
			//$CASES-OMITTED$
			default:
				return null;
		}
	}

	private Set<SearchOptions> getEnabledOptions() {
		return Arrays.stream(SearchOptions.values())
				.filter(option -> (getButtonForSearchOption(option) != null && getButtonForSearchOption(option).getEnabled()))
				.collect(Collectors.toSet());
	}

	private Set<SearchOptions> getSelectedOptions() {
		return Arrays.stream(SearchOptions.values())
				.filter(isOptionSelected())
				.collect(Collectors.toSet());
	}

	private Predicate<? super SearchOptions> isOptionSelected() {
		return option -> {
			ToolItem buttonForSearchOption= getButtonForSearchOption(option);
			if (option == SearchOptions.GLOBAL) {
				return !buttonForSearchOption.getSelection();// The "Global" option is mapped to a button that
				// selects whether to search in the selection, thus inverting the semantic
			}
			return buttonForSearchOption != null && buttonForSearchOption.getSelection();
		};
	}

	public void pressSearch(boolean forward) {
		if (forward) {
			searchForward.notifyListeners(SWT.Selection, null);
		} else {
			searchBackward.notifyListeners(SWT.Selection, null);
		}
	}

	@Override
	public IFindReplaceLogic getFindReplaceLogic() {
		return findReplaceLogic;
	}

	@Override
	public void performReplaceAll() {
		openReplaceDialog();
		replaceAllButton.notifyListeners(SWT.Selection, null);
	}

	@Override
	public void performReplace() {
		openReplaceDialog();
		replaceButton.notifyListeners(SWT.Selection, null);
	}

	public boolean isReplaceDialogOpen() {
		return dialogAccessor.getBoolean("replaceBarOpen");
	}

	public void openReplaceDialog() {
		if (!isReplaceDialogOpen() && Objects.nonNull(openReplaceDialog)) {
			openReplaceDialog.notifyListeners(SWT.Selection, null);
			replace= (HistoryTextWrapper) dialogAccessor.get("replaceBar");
			replaceButton= (ToolItem) dialogAccessor.get("replaceButton");
			replaceAllButton= (ToolItem) dialogAccessor.get("replaceAllButton");
		}
	}

	public void closeReplaceDialog() {
		if (isReplaceDialogOpen() && Objects.nonNull(openReplaceDialog)) {
			openReplaceDialog.notifyListeners(SWT.Selection, null);
			replace= null;
			replaceButton= null;
			replaceAllButton= null;
		}
	}

	@Override
	public void performReplaceAndFind() {
		performReplace();
	}

	@Override
	public void assertInitialConfiguration() {
		assertUnselected(SearchOptions.REGEX);
		assertUnselected(SearchOptions.WHOLE_WORD);
		assertUnselected(SearchOptions.CASE_SENSITIVE);
		if (!doesTextViewerHaveMultiLineSelection(findReplaceLogic.getTarget())) {
			assertSelected(SearchOptions.GLOBAL);
			assertTrue(findReplaceLogic.isActive(SearchOptions.GLOBAL));
		} else {
			assertUnselected(SearchOptions.GLOBAL);
			assertFalse(findReplaceLogic.isActive(SearchOptions.GLOBAL));
		}
		assertEnabled(SearchOptions.GLOBAL);
		assertEnabled(SearchOptions.REGEX);
		assertEnabled(SearchOptions.CASE_SENSITIVE);
		if (getFindText().equals("") || findReplaceLogic.isAvailable(SearchOptions.WHOLE_WORD)) {
			assertEnabled(SearchOptions.WHOLE_WORD);
		} else {
			assertDisabled(SearchOptions.WHOLE_WORD);
		}
	}

	private boolean doesTextViewerHaveMultiLineSelection(IFindReplaceTarget target) {
		if (target instanceof IFindReplaceTargetExtension scopeProvider) {
			return scopeProvider.getScope() != null; // null is returned for global scope
		}
		return false;
	}

	@Override
	public void assertUnselected(SearchOptions option) {
		assertFalse(getSelectedOptions().contains(option));
	}

	@Override
	public void assertSelected(SearchOptions option) {
		assertTrue(getSelectedOptions().contains(option));
	}

	@Override
	public void assertDisabled(SearchOptions option) {
		assertFalse(getEnabledOptions().contains(option));
	}

	@Override
	public void assertEnabled(SearchOptions option) {
		assertTrue(getEnabledOptions().contains(option));
	}

	@Override
	public Shell getActiveShell() {
		return shellRetriever.get();
	}

}
