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
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.graphics.Point;

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

	private String findString = ""; //$NON-NLS-1$
	private String replaceString = ""; //$NON-NLS-1$

	@Override
	public void setFindString(String findString) {
		this.findString = Objects.requireNonNull(findString);
		if (isAvailableAndActive(SearchOptions.INCREMENTAL)) {
			performSearch(true);
		}
	}

	@Override
	public void setReplaceString(String replaceString) {
		this.replaceString = Objects.requireNonNull(replaceString);
	}

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
				resetIncrementalBaseLocation();
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
			resetIncrementalBaseLocation();
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
	public boolean isAvailable(SearchOptions searchOption) {
		switch (searchOption) {
		case REGEX:
			return isTargetSupportingRegEx;
		case WHOLE_WORD:
			return !isAvailableAndActive(SearchOptions.REGEX) && isWord(findString);
		case INCREMENTAL:
		case CASE_SENSITIVE:
		case FORWARD:
		case GLOBAL:
		case WRAP:
		default:
			return true;
		}
	}

	@Override
	public boolean isAvailableAndActive(SearchOptions searchOption) {
		return isAvailable(searchOption) && isActive(searchOption);
	}

	/**
	 * Tests whether each character in the given string is a letter.
	 *
	 * @param str the string to check
	 * @return <code>true</code> if the given string is a word
	 */
	private static boolean isWord(String str) {
		return str != null && str.chars().allMatch(Character::isJavaIdentifierPart);
	}

	@Override
	public void resetIncrementalBaseLocation() {
		if (target != null && shouldInitIncrementalBaseLocation()) {
			incrementalBaseLocation = target.getSelection();
		} else {
			incrementalBaseLocation = new Point(0, 0);
		}
	}

	public boolean shouldInitIncrementalBaseLocation() {
		return isActive(SearchOptions.INCREMENTAL);
	}

	/**
	 * Tells the dialog to perform searches only in the scope given by the actually
	 * selected lines.
	 */
	private void initializeSearchScope() {
		if (shouldInitIncrementalBaseLocation()) {
			resetIncrementalBaseLocation();
		}

		if (target == null || !(target instanceof IFindReplaceTargetExtension)) {
			return;
		}

		IFindReplaceTargetExtension extensionTarget = (IFindReplaceTargetExtension) target;

		IRegion scope;
		Point lineSelection = extensionTarget.getLineSelection();
		scope = new Region(lineSelection.x, lineSelection.y);

		int offset = isAvailableAndActive(SearchOptions.FORWARD) ? scope.getOffset()
				: scope.getOffset() + scope.getLength();

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
	public void performReplaceAll() {
		resetStatus();

		if (findString != null && !findString.isEmpty()) {
			try {
				int replaceCount = replaceAll();
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
	public void performSelectAll() {
		resetStatus();

		if (findString != null && !findString.isEmpty()) {
			try {
				int selectCount = selectAll();
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

	@Override
	public boolean performSearch() {
		boolean result = performSearch(false);
		resetIncrementalBaseLocation();
		return result;
	}

	private boolean performSearch(boolean updateFromIncrementalBaseLocation) {
		resetStatus();
		if (findString.isEmpty()) {
			return false;
		}

		boolean somethingFound = false;
		try {
			return somethingFound = findNext(updateFromIncrementalBaseLocation);
		} catch (PatternSyntaxException ex) {
			status = new InvalidRegExStatus(ex);
		} catch (IllegalStateException ex) {
			// we don't keep state in this dialog
		}
		return somethingFound;
	}

	/**
	 * Replaces all occurrences of the user's findString with the replace string.
	 * Returns the number of replacements that occur.
	 *
	 * @return the number of occurrences
	 *
	 */
	private int replaceAll() {
		if (!prepareTargetForEditing()) {
			return 0;
		}

		List<Point> replacements = new ArrayList<>();
		executeInForwardMode(() -> {
			executeWithReplaceAllEnabled(() -> {
				Point currentSelection = new Point(0, 0);
				while (findAndSelect(currentSelection.x + currentSelection.y) != -1) {
					currentSelection = replaceSelection();
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
	 * @return the number of selected elements
	 */
	private int selectAll() {
		List<Point> selections = new ArrayList<>();
		executeInForwardMode(() -> {
			Point currentSeletion = new Point(0, 0);
			while (findAndSelect(currentSeletion.x + currentSeletion.y) != -1) {
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
	 * @param startPosition the position at which to start the search
	 * @return the occurrence of the find string following the options or
	 *         <code>-1</code> if nothing found
	 */
	private int findIndex(int startPosition) {
		int index = 0;
		if (isAvailableAndActive(SearchOptions.FORWARD)) {
			index = findAndSelect(startPosition);
		} else {
			index = startPosition == 0 ? -1
					: findAndSelect(startPosition - 1);
		}

		if (index == -1) {

			if (isAvailableAndActive(SearchOptions.WRAP)) {
				statusLineMessage(FindReplaceMessages.FindReplace_Status_wrapped_label);
				status = new FindStatus(FindStatus.StatusCode.WRAPPED);
				index = findAndSelect(-1);
			} else {
				status = new FindStatus(FindStatus.StatusCode.NO_MATCH);
			}
		}
		return index;
	}

	@Override
	public int findAndSelect(int offset) {
		boolean wholeWordSearch = isAvailableAndActive(SearchOptions.WHOLE_WORD);
		boolean forwardSearch = isAvailableAndActive(SearchOptions.FORWARD);
		boolean caseSensitiveSearch = isAvailableAndActive(SearchOptions.CASE_SENSITIVE);
		boolean regexSearch = isAvailableAndActive(SearchOptions.REGEX);

		if (target instanceof IFindReplaceTargetExtension3 regexSupportingTarget) {
			return (regexSupportingTarget).findAndSelect(offset, findString,
					forwardSearch, caseSensitiveSearch,
					wholeWordSearch, regexSearch);
		}
		return target.findAndSelect(offset, findString, forwardSearch,
				caseSensitiveSearch, wholeWordSearch);
	}

	/**
	 * Replaces the selection with the current replace string. If the regex search
	 * option has been activated, the replace string is considered as regex replace
	 * pattern which will get expanded if the underlying target supports it. Returns
	 * the region of the inserted text; note that the returned selection covers the
	 * expanded pattern in case of regex replace.
	 *
	 * @return the selection after replacing, i.e. the inserted text
	 */
	private Point replaceSelection() {
		if (target instanceof IFindReplaceTargetExtension3)
			((IFindReplaceTargetExtension3) target).replaceSelection(replaceString,
					isAvailableAndActive(SearchOptions.REGEX));
		else
			target.replaceSelection(replaceString);

		return target.getSelection();
	}

	private boolean findNext(boolean updateFromIncrementalBaseLocation) {

		if (target == null) {
			return false;
		}

		int findReplacePosition = calculateFindBeginningOffset(updateFromIncrementalBaseLocation);

		int index = findIndex(findReplacePosition);

		if (index == -1) {
			String msg = NLSUtility.format(FindReplaceMessages.FindReplace_Status_noMatchWithValue_label, findString);
			statusLineMessage(false, msg);
			status = new FindStatus(FindStatus.StatusCode.NO_MATCH);
			return false;
		}

		if (isActive(SearchOptions.FORWARD) && index >= findReplacePosition
				|| !isActive(SearchOptions.FORWARD) && index <= findReplacePosition) {
			statusLineMessage(""); //$NON-NLS-1$
		}

		return true;
	}

	private int calculateFindBeginningOffset(boolean updateFromExistingBaseLocation) {
		Point r = null;
		if (updateFromExistingBaseLocation) {
			r = incrementalBaseLocation;
		} else {
			r = target.getSelection();
		}

		int findReplacePosition = r.x;
		if (!isActive(SearchOptions.FORWARD)) {
			findReplacePosition += r.y;
		}
		if (!updateFromExistingBaseLocation) {
			if (isActive(SearchOptions.FORWARD)) {
				findReplacePosition += r.y;
			} else {
				findReplacePosition -= r.y;
			}
		}
		return findReplacePosition;
	}

	@Override
	public boolean performReplaceAndFind() {
		resetStatus();
		if (performSelectAndReplace()) {
			performSearch();
			return true;
		}
		return false;
	}

	@Override
	public boolean performSelectAndReplace() {
		resetStatus();
		if (!isFindStringSelected()) {
			performSearch();
		}
		if (getStatus().wasSuccessful()) {
			if (!prepareTargetForEditing()) {
				return false;
			}
			try {
				replaceSelection();
				return true;
			} catch (PatternSyntaxException ex) {
				status = new InvalidRegExStatus(ex);
			} catch (IllegalStateException ex) {
			}
		}
		return false;
	}

	private boolean isFindStringSelected() {
		String selectedString = getCurrentSelection();
		if (isAvailableAndActive(SearchOptions.REGEX)) {
			int patternFlags = 0;
			if (!isAvailableAndActive(SearchOptions.CASE_SENSITIVE)) {
				patternFlags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
			}
			Pattern pattern = Pattern.compile(findString, patternFlags);
			return pattern.matcher(selectedString).find();
		} else {
			if (isAvailableAndActive(SearchOptions.CASE_SENSITIVE)) {
				return getCurrentSelection().equals(findString);
			}
			return getCurrentSelection().equalsIgnoreCase(findString);
		}
	}

	@Override
	public void updateTarget(IFindReplaceTarget newTarget, boolean canEditTarget) {
		resetStatus();
		this.isTargetEditable = canEditTarget;

		if (this.target != newTarget) {
			if (this.target instanceof IFindReplaceTargetExtension)
				((IFindReplaceTargetExtension) this.target).endSession();

			this.target = newTarget;
			isTargetSupportingRegEx = newTarget instanceof IFindReplaceTargetExtension3;

			if (newTarget instanceof IFindReplaceTargetExtension) {
				((IFindReplaceTargetExtension) newTarget).beginSession();

				activate(SearchOptions.GLOBAL);
			}
		}

		resetIncrementalBaseLocation();
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
	public IFindReplaceTarget getTarget() {
		return target;
	}

}
