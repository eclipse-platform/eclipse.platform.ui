/*******************************************************************************
 * Copyright (c) 2023 Vector Informatik GmbH and others.
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

package org.eclipse.ui.internal.findandreplace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.text.IFindReplaceTargetExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.findandreplace.status.FindAllStatus;
import org.eclipse.ui.internal.findandreplace.status.FindStatus;
import org.eclipse.ui.internal.findandreplace.status.IFindReplaceStatus;
import org.eclipse.ui.internal.findandreplace.status.InvalidRegExStatus;
import org.eclipse.ui.internal.findandreplace.status.NoStatus;
import org.eclipse.ui.internal.findandreplace.status.ReplaceAllStatus;
import org.eclipse.ui.internal.texteditor.NLSUtility;

import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.IFindReplaceTargetExtension2;

public class FindReplaceLogic implements IFindReplaceLogic {
	private IFindReplaceStatus status;
	private IFindReplaceTarget target;
	private Point incrementalBaseLocation;

	private boolean isTargetSupportingRegEx;
	private boolean isTargetEditable;
	private Set<SearchOptions> searchOptions = new HashSet<>();

	@Override
	public void activate(SearchOptions searchOption) {
		if (!searchOptions.add(searchOption)) {
			return;
		}

		switch (searchOption) {
		case GLOBAL:
			unsetSearchScope();
			break;
		case FORWARD:
		case INCREMENTAL:
			if (shouldInitIncrementalBaseLocation()) {
				initIncrementalBaseLocation();
			}
			break;
		// $CASES-OMITTED$
		default:
			break;
		}
	}

	@Override
	public void deactivate(SearchOptions searchOption) {
		if (!searchOptions.remove(searchOption)) {
			return;
		}

		if (searchOption == SearchOptions.GLOBAL) {
			initializeSearchScope();
		}

		if (searchOption == SearchOptions.FORWARD && shouldInitIncrementalBaseLocation()) {
			initIncrementalBaseLocation();
		}
	}

	@Override
	public boolean isActive(SearchOptions searchOption) {
		return searchOptions.contains(searchOption);
	}

	@Override
	public IFindReplaceStatus getStatus() {
		if (status == null) {
			return new NoStatus();
		}
		return status;
	}

	/**
	 * Call before running an operation of FindReplaceLogic. Resets the internal
	 * status.
	 */
	private void resetStatus() {
		status = null;
	}

	@Override
	public boolean isIncrementalSearchAvailable() {
		return !isRegExSearchAvailableAndActive();
	}

	@Override
	public boolean isWholeWordSearchAvailable(String findString) {
		return !isRegExSearchAvailableAndActive() && isWord(findString);
	}
	/**
	 * Tests whether each character in the given string is a letter.
	 *
	 * @param str the string to check
	 * @return <code>true</code> if the given string is a word
	 */
	private static boolean isWord(String str) {
		return str != null && !str.isEmpty() && str.chars().allMatch(Character::isJavaIdentifierPart);
	}

	@Override
	public boolean isRegExSearchAvailableAndActive() {
		return isActive(SearchOptions.REGEX) && isTargetSupportingRegEx;
	}


	/**
	 * Initializes the anchor used as starting point for incremental searching.
	 *
	 */
	private void initIncrementalBaseLocation() {
		if (target != null && isActive(SearchOptions.INCREMENTAL) && !isRegExSearchAvailableAndActive()) {
			incrementalBaseLocation = target.getSelection();
		} else {
			incrementalBaseLocation = new Point(0, 0);
		}
	}

	public boolean shouldInitIncrementalBaseLocation() {
		return isActive(SearchOptions.INCREMENTAL) && !isActive(SearchOptions.REGEX);
	}

	/**
	 * Tells the dialog to perform searches only in the scope given by the actually
	 * selected lines.
	 */
	private void initializeSearchScope() {
		if (shouldInitIncrementalBaseLocation()) {
			initIncrementalBaseLocation();
		}

		if (target == null || !(target instanceof IFindReplaceTargetExtension)) {
			return;
		}

		IFindReplaceTargetExtension extensionTarget = (IFindReplaceTargetExtension) target;

		IRegion scope;
		Point lineSelection = extensionTarget.getLineSelection();
		scope = new Region(lineSelection.x, lineSelection.y);

		int offset = isActive(SearchOptions.FORWARD) ? scope.getOffset() : scope.getOffset() + scope.getLength();

		extensionTarget.setSelection(offset, 0);
		extensionTarget.setScope(scope);
	}

	/**
	 * Unsets the search scope for a "Scoped"-Search.
	 */
	private void unsetSearchScope() {
		if (target == null || !(target instanceof IFindReplaceTargetExtension)) {
			return;
		}

		IFindReplaceTargetExtension extensionTarget = (IFindReplaceTargetExtension) target;

		extensionTarget.setScope(null);
	}

	/**
	 * Returns the status line manager of the active editor or <code>null</code> if
	 * there is no such editor.
	 *
	 * @return the status line manager of the active editor
	 */
	private IEditorStatusLine getStatusLineManager() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}

		IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			return null;
		}

		IEditorPart editor = page.getActiveEditor();
		if (editor == null) {
			return null;
		}

		return editor.getAdapter(IEditorStatusLine.class);
	}

	@Override
	public void performReplaceAll(String findString, String replaceString, Display display) {
		resetStatus();

		int replaceCount = 0;

		if (findString != null && !findString.isEmpty()) {

			class ReplaceAllRunnable implements Runnable {
				public int numberOfOccurrences;

				@Override
				public void run() {
					numberOfOccurrences = replaceAll(findString, replaceString == null ? "" : replaceString); //$NON-NLS-1$
				}
			}

			try {
				ReplaceAllRunnable runnable = new ReplaceAllRunnable();
				BusyIndicator.showWhile(display, runnable);
				replaceCount = runnable.numberOfOccurrences;

				if (replaceCount != 0) {
					if (replaceCount == 1) { // not plural
						statusLineMessage(FindReplaceMessages.FindReplace_Status_replacement_label);
					} else {
						String msg = FindReplaceMessages.FindReplace_Status_replacements_label;
						msg = NLSUtility.format(msg, String.valueOf(replaceCount));
						statusLineMessage(msg);
					}
					status = new ReplaceAllStatus(replaceCount);
				} else {
					String msg = NLSUtility.format(FindReplaceMessages.FindReplace_Status_noMatchWithValue_label,
							findString);
					statusLineMessage(false, msg);
					status = new FindStatus(FindStatus.StatusCode.NO_MATCH);
				}
			} catch (PatternSyntaxException ex) {
				status = new InvalidRegExStatus(ex);
			} catch (IllegalStateException ex) {
				// we don't keep state in this dialog
			}
		}
	}

	@Override
	public void performSelectAll(String findString, Display display) {
		resetStatus();

		int selectCount = 0;

		if (findString != null && !findString.isEmpty()) {

			class SelectAllRunnable implements Runnable {
				public int numberOfOccurrences;

				@Override
				public void run() {
					numberOfOccurrences = selectAll(findString);
				}
			}

			try {
				SelectAllRunnable runnable = new SelectAllRunnable();
				BusyIndicator.showWhile(display, runnable);
				selectCount = runnable.numberOfOccurrences;

				if (selectCount != 0) {
					if (selectCount == 1) { // not plural
						statusLineMessage(FindReplaceMessages.FindReplace_Status_selection_label);
					} else {
						String msg = FindReplaceMessages.FindReplace_Status_selections_label;
						msg = NLSUtility.format(msg, String.valueOf(selectCount));
						statusLineMessage(msg);
					}
					status = new FindAllStatus(selectCount);
				} else {
					String msg = NLSUtility.format(FindReplaceMessages.FindReplace_Status_noMatchWithValue_label,
							findString);
					statusLineMessage(false, msg);
					status = new FindStatus(FindStatus.StatusCode.NO_MATCH);
				}
			} catch (PatternSyntaxException ex) {
				status = new InvalidRegExStatus(ex);
			} catch (IllegalStateException ex) {
				// we don't keep state
			}
		}
	}

	/**
	 * Validates the state of the find/replace target. Validates the state of this
	 * target. The predominate intent of this method is to take any action probably
	 * necessary to ensure that the target can persistently be changed. Regardless
	 * of whether there is something to do or not, returns whether the target can be
	 * edited.
	 *
	 * @return <code>true</code> if target can be changed, <code>false</code>
	 *         otherwise
	 */
	private boolean prepareTargetForEditing() {
		if (target instanceof IFindReplaceTargetExtension2) {
			IFindReplaceTargetExtension2 extension = (IFindReplaceTargetExtension2) target;
			if (!extension.validateTargetState()) {
				status = new FindStatus(FindStatus.StatusCode.READONLY);
				return false;
			}
		}
		return isEditable();
	}

	/**
	 * Replaces the current selection of the target with the user's replace string.
	 *
	 * @param replaceString the String to replace the selection with
	 *
	 * @return <code>true</code> if the operation was successful
	 */
	private boolean replaceSelection(String replaceString) {

		if (!prepareTargetForEditing()) {
			return false;
		}

		if (replaceString == null) {
			replaceString = ""; //$NON-NLS-1$
		}

		boolean replaced;
		try {
			replaceSelection(replaceString, isRegExSearchAvailableAndActive());
			replaced = true;
		} catch (PatternSyntaxException ex) {
			status = new InvalidRegExStatus(ex);
			replaced = false;
		} catch (IllegalStateException ex) {
			replaced = false;
		}

		return replaced;
	}

	@Override
	public boolean performSearch(String searchString) {
		return performSearch(shouldInitIncrementalBaseLocation(), searchString);
	}

	/**
	 * Locates the user's findString in the text of the target.
	 *
	 * @param mustInitIncrementalBaseLocation <code>true</code> if base location
	 *                                        must be initialized
	 * @param findString                      the String to search for
	 * @return Whether the string was found in the target
	 */
	private boolean performSearch(boolean mustInitIncrementalBaseLocation, String findString) {
		if (mustInitIncrementalBaseLocation) {
			initIncrementalBaseLocation();
		}
		resetStatus();

		boolean somethingFound = false;

		if (findString != null && !findString.isEmpty()) {

			try {
				somethingFound = findNext(findString, isActive(SearchOptions.FORWARD));
			} catch (PatternSyntaxException ex) {
				status = new InvalidRegExStatus(ex);
			} catch (IllegalStateException ex) {
				// we don't keep state in this dialog
			}
		}
		return somethingFound;
	}

	/**
	 * Replaces all occurrences of the user's findString with the replace string.
	 * Returns the number of replacements that occur.
	 *
	 * @param findString    the string to search for
	 * @param replaceString the replacement string
	 *                      expression
	 * @return the number of occurrences
	 *
	 */
	private int replaceAll(String findString, String replaceString) {
		if (!prepareTargetForEditing()) {
			return 0;
		}

		List<Point> replacements = new ArrayList<>();
		executeInForwardMode(() -> {
			executeWithReplaceAllEnabled(() -> {
				Point currentSelection = new Point(0, 0);
				while (findAndSelect(currentSelection.x + currentSelection.y, findString) != -1) {
					currentSelection = replaceSelection(replaceString, isRegExSearchAvailableAndActive());
					replacements.add(currentSelection);
				}
			});
		});
		return replacements.size();
	}

	private void executeInForwardMode(Runnable runnable) {
		if (isActive(SearchOptions.FORWARD)) {
			runnable.run();
		} else {
			activate(SearchOptions.FORWARD);
			try {
				runnable.run();
			} finally {
				deactivate(SearchOptions.FORWARD);
			}
		}
	}

	private void executeWithReplaceAllEnabled(Runnable runnable) {
		if (target instanceof IFindReplaceTargetExtension selectableTarget) {
			selectableTarget.setReplaceAllMode(true);
			try {
				runnable.run();
			} finally {
				selectableTarget.setReplaceAllMode(false);
			}
		} else {
			runnable.run();
		}
	}

	/**
	 * @param findString the string to select as part of the search
	 * @return the number of selected elements
	 */
	private int selectAll(String findString) {
		List<Point> selections = new ArrayList<>();
		executeInForwardMode(() -> {
			Point currentSeletion = new Point(0, 0);
			while (findAndSelect(currentSeletion.x + currentSeletion.y, findString) != -1) {
				currentSeletion = target.getSelection();
				selections.add(currentSeletion);
			}
			if (target instanceof IFindReplaceTargetExtension4 selectableTarget) {
				IRegion[] selectedRegions = selections.stream().map(selection -> new Region(selection.x, selection.y))
						.toArray(IRegion[]::new);
				selectableTarget.setSelection(selectedRegions);
			}
		});
		return selections.size();
	}

	/**
	 * Returns the position of the specified search string, or <code>-1</code> if
	 * the string can not be found when searching using the given options.
	 *
	 * @param findString    the string to search for
	 * @param startPosition the position at which to start the search
	 * @return the occurrence of the find string following the options or
	 *         <code>-1</code> if nothing found
	 */
	private int findIndex(String findString, int startPosition) {
		int index = 0;
		if (isActive(SearchOptions.FORWARD)) {
			index = findAndSelect(startPosition, findString);
		} else {
			index = startPosition == 0 ? -1
				: findAndSelect(startPosition - 1, findString);
		}

		if (index == -1) {

			if (isActive(SearchOptions.WRAP)) {
				statusLineMessage(FindReplaceMessages.FindReplace_Status_wrapped_label);
				status = new FindStatus(FindStatus.StatusCode.WRAPPED);
				index = findAndSelect(-1, findString);
			} else {
				status = new FindStatus(FindStatus.StatusCode.NO_MATCH);
			}
		}
		return index;
	}

	@Override
	public int findAndSelect(int offset, String findString) {
		boolean wholeWordSearch = isActive(SearchOptions.WHOLE_WORD) && isWholeWordSearchAvailable(findString);
		boolean forwardSearch = isActive(SearchOptions.FORWARD);
		boolean caseSensitiveSearch = isActive(SearchOptions.CASE_SENSITIVE);
		boolean regexSearch = isActive(SearchOptions.REGEX);

		if (target instanceof IFindReplaceTargetExtension3 regexSupportingTarget) {
			return (regexSupportingTarget).findAndSelect(offset, findString,
					forwardSearch, caseSensitiveSearch,
					wholeWordSearch, regexSearch);
		}
		return target.findAndSelect(offset, findString, forwardSearch,
				caseSensitiveSearch, wholeWordSearch);
	}

	/**
	 * Replaces the selection with <code>replaceString</code>. If
	 * <code>regExReplace</code> is <code>true</code>, <code>replaceString</code> is
	 * a regex replace pattern which will get expanded if the underlying target
	 * supports it. Returns the region of the inserted text; note that the returned
	 * selection covers the expanded pattern in case of regex replace.
	 *
	 * @param replaceString the replace string (or a regex pattern)
	 * @param regExReplace  <code>true</code> if <code>replaceString</code> is a
	 *                      pattern
	 * @return the selection after replacing, i.e. the inserted text
	 */
	private Point replaceSelection(String replaceString, boolean regExReplace) {
		if (target instanceof IFindReplaceTargetExtension3)
			((IFindReplaceTargetExtension3) target).replaceSelection(replaceString, regExReplace);
		else
			target.replaceSelection(replaceString);

		return target.getSelection();
	}

	/**
	 * Returns whether the specified search string can be found using the given
	 * options.
	 *
	 * @param findString             the string to search for
	 * @param forwardSearch          the direction of the search
	 * @return <code>true</code> if the search string can be found using the given
	 *         options
	 *
	 */
	private boolean findNext(String findString, boolean forwardSearch) {

		if (target == null) {
			return false;
		}

		Point r = null;
		if (isActive(SearchOptions.INCREMENTAL)) {
			r = incrementalBaseLocation;
		} else {
			r = target.getSelection();
		}

		int findReplacePosition = r.x;
		if (forwardSearch) {
			findReplacePosition += r.y;
		}

		int index = findIndex(findString, findReplacePosition);

		if (index == -1) {
			String msg = NLSUtility.format(FindReplaceMessages.FindReplace_Status_noMatchWithValue_label, findString);
			statusLineMessage(false, msg);
			status = new FindStatus(FindStatus.StatusCode.NO_MATCH);
			return false;
		}

		if (forwardSearch && index >= findReplacePosition || !forwardSearch && index <= findReplacePosition) {
			statusLineMessage(""); //$NON-NLS-1$
		}

		return true;
	}

	@Override
	public boolean performReplaceAndFind(String findString, String replaceString) {
		resetStatus();
		if (performSelectAndReplace(findString, replaceString)) {
			performSearch(findString);
			return true;
		}
		return false;
	}

	@Override
	public boolean performSelectAndReplace(String findString, String replaceString) {
		resetStatus();
		if (!isFindStringSelected(findString)) {
			performSearch(findString);
		}
		return replaceSelection(replaceString);
	}

	private boolean isFindStringSelected(String findString) {
		String selectedString = getCurrentSelection();
		if (isRegExSearchAvailableAndActive()) {
			int patternFlags = 0;
			if (!isActive(SearchOptions.CASE_SENSITIVE)) {
				patternFlags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
			}
			Pattern pattern = Pattern.compile(findString, patternFlags);
			return pattern.matcher(selectedString).find();
		} else {
			return getCurrentSelection().equals(findString);
		}
	}

	@Override
	public void updateTarget(IFindReplaceTarget newTarget, boolean canEditTarget) {
		resetStatus();
		this.isTargetEditable = canEditTarget;

		if (this.target != newTarget) {
			if (newTarget != null && newTarget instanceof IFindReplaceTargetExtension)
				((IFindReplaceTargetExtension) newTarget).endSession();

			this.target = newTarget;
			if (newTarget != null)
				isTargetSupportingRegEx = newTarget instanceof IFindReplaceTargetExtension3;

			if (newTarget instanceof IFindReplaceTargetExtension) {
				((IFindReplaceTargetExtension) newTarget).beginSession();

				activate(SearchOptions.GLOBAL);
			}
		}

		initIncrementalBaseLocation();
	}

	@Override
	public void dispose() {
		if (target != null && target instanceof IFindReplaceTargetExtension) {
			((IFindReplaceTargetExtension) target).endSession();
		}

		target = null;
	}

	private String getCurrentSelection() {
		if (target == null) {
			return null;
		}

		return target.getSelectionText();
	}

	/**
	 * Returns whether the target is editable.
	 *
	 * @return <code>true</code> if target is editable
	 */
	private boolean isEditable() {
		boolean isEditable = (target == null ? false : target.isEditable());
		return isTargetEditable && isEditable;
	}

	/**
	 * Sets the given status message in the status line.
	 *
	 * @param error         <code>true</code> if it is an error
	 * @param editorMessage the message to display in the editor's status line
	 */
	private void statusLineMessage(boolean error, String editorMessage) {
		IEditorStatusLine statusLine = getStatusLineManager();
		if (statusLine != null) {
			statusLine.setMessage(error, editorMessage, null);
		}
	}

	/**
	 * Sets the given message in the status line.
	 *
	 * @param message the message
	 */
	private void statusLineMessage(String message) {
		statusLineMessage(false, message);
	}

	@Override
	public void performIncrementalSearch(String searchString) {
		resetStatus();

		if (isActive(SearchOptions.INCREMENTAL) && isIncrementalSearchAvailable()) {
			if (searchString.equals("") && target != null) { //$NON-NLS-1$
				// empty selection at base location
				int offset = incrementalBaseLocation.x;

				if (isActive(SearchOptions.FORWARD)) {
					offset = offset + incrementalBaseLocation.y;
				}

				findAndSelect(offset, ""); //$NON-NLS-1$
			} else {
				performSearch(false, searchString);
			}
		}
	}

	@Override
	public IFindReplaceTarget getTarget() {
		return target;
	}

}
