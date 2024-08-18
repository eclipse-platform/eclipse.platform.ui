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

import org.eclipse.jface.text.IFindReplaceTarget;

import org.eclipse.ui.internal.findandreplace.status.IFindReplaceStatus;

/**
 * Implements a generalized logic operator for in-file Find/Replace-Operations.
 * Requires a target that inherits from {@link IFindReplaceTarget} to operate.
 * Allows enabling or disabling different {@link SearchOptions} which will be
 * applied to subsequent operations.
 */
public interface IFindReplaceLogic {

	/**
	 * Sets the string to be used for searching in find and replace operations.
	 *
	 * @param findString the find string to use, must not be null
	 */
	public void setFindString(String findString);

	/**
	 * Sets the string to be used as a replacement in replace operations.
	 *
	 * @param replaceString the replace string to use, must not be null
	 */
	public void setReplaceString(String replaceString);

	/**
	 * Activate a search option
	 *
	 * @param searchOption option
	 */
	public void activate(SearchOptions searchOption);

	/**
	 * Deactivate a search option
	 *
	 * @param searchOption option
	 */
	public void deactivate(SearchOptions searchOption);

	/**
	 * @param searchOption option
	 * @return whether the option is active
	 */
	public boolean isActive(SearchOptions searchOption);

	/**
	 * Returns whether the given search options is currently available. This
	 * includes validation of whether the target supports a specific option (such as
	 * {@link SearchOptions#REGEX}) and the compatibility of search options (such as
	 * {@link SearchOptions#INCREMENTAL} not being available when
	 * {@link SearchOptions#REGEX} is active).
	 *
	 * @param searchOption the search option to check for availability
	 *
	 * @return whether the search option is currently available
	 */
	public boolean isAvailable(SearchOptions searchOption);

	/**
	 * Returns whether the given search options is currently available and active.
	 * Combines {@link #isActive(SearchOptions)} and
	 * {@link #isAvailable(SearchOptions)}.
	 *
	 * @param searchOption the search option to check
	 * @return whether the search option is currently available and active
	 */
	public boolean isAvailableAndActive(SearchOptions searchOption);

	/**
	 * Returns the current status of FindReplaceLogic. The Status can inform about
	 * events such as an error happening, a warning happening (e.g.: the
	 * search-string wasn't found) and brings a method to retrieve a message that
	 * can directly be displayed to the user.
	 *
	 * @return FindAndReplaceMessageStatus
	 */
	public IFindReplaceStatus getStatus();

	/**
	 * Replaces all occurrences of the current find string with the replace string.
	 * Indicate to the user the number of replacements that occur.
	 */
	public void performReplaceAll();

	/**
	 * Selects all occurrences of the current find string.
	 */
	public void performSelectAll();

	/**
	 * Locates the current find string in the target. If incremental search is
	 * activated, the search will be performed starting from an incremental search
	 * position, which can be reset using {@link #resetIncrementalBaseLocation()}.
	 * If incremental search is activated and RegEx search is activated, nothing
	 * happens.
	 *
	 * @return Whether the string was found in the target
	 */
	public boolean performSearch();

	/**
	 * Searches for the current find string starting at the given offset and using
	 * the specified search directives. If a string has been found it is selected
	 * and its start offset is returned.
	 *
	 * @param offset     the offset at which searching starts
	 * @return the position of the specified string, or -1 if the string has not
	 *         been found
	 */
	public int findAndSelect(int offset);

	/**
	 * Replaces the current selection if it matches the find string or performs a
	 * search and does a replacement. It then performs another search for the
	 * current find string. Will not fail in case the selection is invalidated,
	 * e.g., after a replace operation or after the target was updated.
	 *
	 * @return whether a replacement has been performed
	 */
	public boolean performReplaceAndFind();

	/**
	 * Selects first and then replaces the next occurrence.
	 *
	 * @return whether a replacement has been performed
	 */
	public boolean performSelectAndReplace();

	/**
	 * Updates the target on which to perform Find/Replace-operations on.
	 *
	 * @param newTarget     the new target for the FindReplaceLogic
	 * @param canEditTarget whether the target is editable - and thus eligible for
	 *                      replacing. Used, for example, for targets containing
	 *                      read-only-files, where the target is an editable
	 *                      component but the file should not be edited.
	 */
	public void updateTarget(IFindReplaceTarget newTarget, boolean canEditTarget);

	/**
	 * dispose of the FindReplaceLogic, ends the Find/Replace-Session in the
	 * FindReplaceTarget.
	 */
	public void dispose();

	/*
	 * @return the Target that FindReplaceLogic operates on
	 */
	public IFindReplaceTarget getTarget();

	/**
	 * Initializes the anchor used as the starting point for incremental searching.
	 * Subsequent incremental searches will start from the first letter of the
	 * currently selected range in the FindReplaceTarget.
	 *
	 * <p>
	 * The "current selection" refers to the range of text that is currently
	 * highlighted or selected within the FindReplaceTarget. This selection can be
	 * either a single position (if no range is selected) or a range of text.
	 *
	 * <p>
	 * When handling range selections:
	 * <ul>
	 * <li>Forward search operations will use the beginning of the selection as the
	 * starting point.</li>
	 * <li>Backward search operations will use the end of the selection as the
	 * starting point.</li>
	 * </ul>
	 */
	void resetIncrementalBaseLocation();

}