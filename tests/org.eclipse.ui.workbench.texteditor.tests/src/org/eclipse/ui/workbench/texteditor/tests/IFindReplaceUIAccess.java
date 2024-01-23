package org.eclipse.ui.workbench.texteditor.tests;

import java.util.Set;

import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.text.IFindReplaceTarget;

import org.eclipse.ui.internal.findandreplace.IFindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.SearchOptions;


/**
 * Wraps UI-Access for different Find/Replace-UIs
 */
public interface IFindReplaceUIAccess {

	IFindReplaceTarget getTarget();

	void close();

	void ensureHasFocusOnGTK();

	void unselect(SearchOptions option);

	void select(SearchOptions option);

	void simulateEnterInFindInputField(boolean shiftPressed);

	void simulateKeyPressInFindInputField(int keyCode, boolean shiftPressed);

	String getFindText();

	String getReplaceText();

	void setFindText(String text, boolean grabFocus);

	void setReplaceText(String text);

	Widget getButtonForSearchOption(SearchOptions option);

	Set<SearchOptions> getEnabledOptions();

	Set<SearchOptions> getSelectedOptions();

	IFindReplaceLogic getFindReplaceLogic();

	void pressReplaceAll();

	/**
	 * Some UIs require a "find" before allowing a Replace. This method handles that, ensuring that
	 * if "findstring" is contained in the target's text, it will definitely be replaced.
	 *
	 * To keep parity between tests, this method should only be called if no text is currently
	 * selected on the target.
	 */
	void performReplace();

}
