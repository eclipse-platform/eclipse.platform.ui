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
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;

import org.eclipse.ui.internal.findandreplace.IFindReplaceUIAccess;
import org.eclipse.ui.internal.findandreplace.SearchOptions;
import org.eclipse.ui.internal.findandreplace.WidgetExtractor;

class OverlayAccess implements IFindReplaceUIAccess {
	private final IFindReplaceTarget findReplaceTarget;

	private final HistoryTextWrapper find;

	private final ToolItem inSelection;

	private final ToolItem caseSensitive;

	private final ToolItem wholeWord;

	private final ToolItem regEx;

	private final ToolItem searchForward;

	private final ToolItem searchBackward;

	private final ToolItem selectAll;

	private final ToolItem openReplaceDialog;

	private HistoryTextWrapper replace;

	private ToolItem replaceButton;

	private ToolItem replaceAllButton;

	private final FindReplaceOverlay overlay;

	OverlayAccess(IFindReplaceTarget findReplaceTarget, FindReplaceOverlay findReplaceOverlay) {
		this.findReplaceTarget= findReplaceTarget;
		overlay= findReplaceOverlay;
		WidgetExtractor widgetExtractor= new WidgetExtractor(FindReplaceOverlay.ID_DATA_KEY, findReplaceOverlay.getContainerControl());
		find= widgetExtractor.findHistoryTextWrapper("searchInput");
		caseSensitive= widgetExtractor.findToolItem("caseSensitiveSearch");
		wholeWord= widgetExtractor.findToolItem("wholeWordSearch");
		regEx= widgetExtractor.findToolItem("regExSearch");
		inSelection= widgetExtractor.findToolItem("searchInSelection");
		searchForward= widgetExtractor.findToolItem("searchForward");
		searchBackward= widgetExtractor.findToolItem("searchBackward");
		selectAll= widgetExtractor.findToolItem("selectAll");
		openReplaceDialog= widgetExtractor.findToolItem("replaceToggle");
		extractReplaceWidgets();
	}

	private void extractReplaceWidgets() {
		if (!isReplaceDialogOpen() && Objects.nonNull(openReplaceDialog)) {
			WidgetExtractor widgetExtractor= new WidgetExtractor(FindReplaceOverlay.ID_DATA_KEY, getContainerControl());
			replace= widgetExtractor.findHistoryTextWrapper("replaceInput");
			replaceButton= widgetExtractor.findToolItem("replaceOne");
			replaceAllButton= widgetExtractor.findToolItem("replaceAll");
		}
	}

	private Composite getContainerControl() {
		return overlay.getContainerControl();
	}

	private void restoreInitialConfiguration() {
		setFindText("");
		select(SearchOptions.GLOBAL);
		unselect(SearchOptions.REGEX);
		unselect(SearchOptions.CASE_SENSITIVE);
		unselect(SearchOptions.WHOLE_WORD);
	}

	@Override
	public void closeAndRestore() {
		restoreInitialConfiguration();
		assertInitialConfiguration();
		close();
	}

	@Override
	public void close() {
		overlay.close();
	}

	@Override
	public void select(SearchOptions option) {
		if (getSelectedOptions().contains(option)) {
			return;
		}
		ToolItem button= getButtonForSearchOption(option);
		if (button != null) {
			button.notifyListeners(SWT.Selection, null);
		}
	}

	@Override
	public void unselect(SearchOptions option) {
		if (!getSelectedOptions().contains(option)) {
			return;
		}
		ToolItem button= getButtonForSearchOption(option);
		if (button != null) {
			button.notifyListeners(SWT.Selection, null);
		}
	}

	@Override
	public void simulateKeyboardInteractionInFindInputField(int keyCode, boolean shiftPressed) {
		final Event event= new Event();
		event.type= SWT.KeyDown;
		if (shiftPressed) {
			event.stateMask= SWT.SHIFT;
		}
		event.keyCode= keyCode;
		find.getTextBar().notifyListeners(SWT.KeyDown, event);
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
			if (buttonForSearchOption == null) {
				return false;
			}
			if (option == SearchOptions.GLOBAL) {
				// The "Global" option is mapped to a button that selects whether to search in
				// the selection, thus inverting the semantic
				return !buttonForSearchOption.getSelection();
			}
			return buttonForSearchOption.getSelection();
		};
	}

	@Override
	public void selectFindHistoryEntry(int index) {
		// Clear the field directly (no SWT.Modify, so no incremental search fires),
		// then step down once per index position. From an empty field ARROW_DOWN
		// always lands on index 0 (newest); each additional press moves one step older.
		find.setText("");
		for (int i = 0; i <= index; i++) {
			simulateKeyboardInteractionInFindInputField(SWT.ARROW_DOWN, false);
		}
	}

	public void pressSelectAll() {
		selectAll.notifyListeners(SWT.Selection, null);
	}

	public void pressSearch(boolean forward) {
		if (forward) {
			searchForward.notifyListeners(SWT.Selection, null);
		} else {
			searchBackward.notifyListeners(SWT.Selection, null);
		}
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
		return replace != null;
	}

	public void openReplaceDialog() {
		if (!isReplaceDialogOpen() && Objects.nonNull(openReplaceDialog)) {
			openReplaceDialog.notifyListeners(SWT.Selection, null);
			extractReplaceWidgets();
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
		if (!doesTextViewerHaveMultiLineSelection()) {
			assertSelected(SearchOptions.GLOBAL);
		} else {
			assertUnselected(SearchOptions.GLOBAL);
		}
		assertEnabled(SearchOptions.GLOBAL);
		assertEnabled(SearchOptions.REGEX);
		assertEnabled(SearchOptions.CASE_SENSITIVE);
		if (!getFindText().contains(" ")) {
			assertEnabled(SearchOptions.WHOLE_WORD);
		} else {
			assertDisabled(SearchOptions.WHOLE_WORD);
		}
	}

	private boolean doesTextViewerHaveMultiLineSelection() {
		if (findReplaceTarget instanceof IFindReplaceTargetExtension scopeProvider) {
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
	public boolean isShown() {
		return getContainerControl().isVisible();
	}

	@Override
	public boolean hasFocus() {
		Control focusControl= getContainerControl().getDisplay().getFocusControl();
		while (focusControl != null) {
			if (getContainerControl() == focusControl) {
				return true;
			}
			focusControl= focusControl.getParent();
		}
		return false;
	}

}
