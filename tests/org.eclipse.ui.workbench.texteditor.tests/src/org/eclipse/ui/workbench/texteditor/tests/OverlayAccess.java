package org.eclipse.ui.workbench.texteditor.tests;

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.text.tests.Accessor;

import org.eclipse.jface.util.Util;

import org.eclipse.jface.text.IFindReplaceTarget;

import org.eclipse.ui.internal.findandreplace.FindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.IFindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.SearchOptions;

public class OverlayAccess implements IFindReplaceUIAccess {
	FindReplaceLogic findReplaceLogic;

	Text find;

	Text replace;

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

	private FindReplaceUITest<?> parentTest;

	Accessor dialogAccessor;

	Shell parentShell;

	OverlayAccess(FindReplaceUITest<?> parent, Accessor findReplaceOverlayAccessor) {
		dialogAccessor= findReplaceOverlayAccessor;
		parentTest= parent;
		parentShell= (Shell) findReplaceOverlayAccessor.invoke("getParentShell", null);
		findReplaceLogic= (FindReplaceLogic) findReplaceOverlayAccessor.get("findReplaceLogic");
		find= (Text) findReplaceOverlayAccessor.get("searchBar");
		replace= (Text) findReplaceOverlayAccessor.get("replaceBar");
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
			if (parentShell == null || !parentShell.isFocusControl()) {
				String screenshotPath= ScreenshotTest.takeScreenshot(FindReplaceUITest.class, parentTest.testName.getMethodName(), System.out);
				fail("this test does not work on GTK unless the runtime workbench has focus. Screenshot: " + screenshotPath);
			}
		}
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
	public void simulateEnterInFindInputField(boolean shiftPressed) {
		simulateKeyPressInFindInputField(SWT.CR, shiftPressed);
	}

	@Override
	public void simulateKeyPressInFindInputField(int keyCode, boolean shiftPressed) {
		final Event event= new Event();
		event.type= SWT.KeyDown;
		event.keyCode= keyCode;
		if (shiftPressed) {
			event.stateMask= SWT.SHIFT;
		}
		find.notifyListeners(SWT.KeyDown, event);
		find.traverse(SWT.TRAVERSE_RETURN, event);
		FindReplaceUITest.runEventQueue();
	}

	@Override
	public String getFindText() {
		return find.getText();
	}

	@Override
	public String getReplaceText() {
		return replace.getText();
	}

	@Override
	public void setFindText(String text, boolean grabFocus) {
		find.setText(text);
		find.notifyListeners(SWT.Modify, null);
		if (grabFocus) {
			find.forceFocus();
		}
	}

	@Override
	public void setReplaceText(String text) {
		openReplaceDialogIfNeeded();
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

	@Override
	public Set<SearchOptions> getEnabledOptions() {
		HashSet<SearchOptions> ret= new HashSet<>();

		for (SearchOptions option : SearchOptions.values()) {
			ToolItem optionButton= getButtonForSearchOption(option);
			if (option == SearchOptions.GLOBAL && !optionButton.isEnabled()) {
				ret.add(option); // The "Global" option is mapped to a button that
									// selects whether to search in the selection, thus inverting the semantic
			} else if (optionButton != null && optionButton.isEnabled()) {
				ret.add(option);
			}
		}

		return ret;
	}

	@Override
	public Set<SearchOptions> getSelectedOptions() {
		HashSet<SearchOptions> ret= new HashSet<>();

		for (SearchOptions option : SearchOptions.values()) {
			ToolItem optionButton= getButtonForSearchOption(option);
			if (option == SearchOptions.GLOBAL && !optionButton.getSelection()) {
				ret.add(option); // The "Global" option is mapped to a button that
									// selects whether to search in the selection, thus inverting the semantic
			} else if (optionButton != null && optionButton.getSelection()) {
				ret.add(option);
			}
		}

		return ret;
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
	public void pressReplaceAll() {
		openReplaceDialogIfNeeded();
		replaceAllButton.notifyListeners(SWT.Selection, null);
	}

	@Override
	public void performReplace() {
		openReplaceDialogIfNeeded();
		replaceButton.notifyListeners(SWT.Selection, null);
	}

	private void openReplaceDialogIfNeeded() {
		if (!dialogAccessor.getBoolean("replaceBarOpen")) {
			openReplaceDialog.notifyListeners(SWT.Selection, null);
			replace= (Text) dialogAccessor.get("replaceBar");
			replaceButton= (ToolItem) dialogAccessor.get("replaceButton");
			replaceAllButton= (ToolItem) dialogAccessor.get("replaceAllButton");
		}
	}

}
