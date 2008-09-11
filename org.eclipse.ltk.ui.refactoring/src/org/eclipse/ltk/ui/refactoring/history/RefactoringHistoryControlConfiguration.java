/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.history;

import org.eclipse.core.resources.IProject;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.ui.refactoring.RefactoringUI;

/**
 * Configuration object for a refactoring history control.
 * <p>
 * Note: this class is intended to be subclassed by clients wishing to control
 * the configuration behavior of their refactoring history control.
 * </p>
 *
 * @see RefactoringUI#createRefactoringHistoryControl(org.eclipse.swt.widgets.Composite,
 *      RefactoringHistoryControlConfiguration)
 *
 * @see IRefactoringHistoryControl
 * @see RefactoringHistoryLabelProvider
 * @see RefactoringHistoryContentProvider
 *
 * @since 3.2
 */
public class RefactoringHistoryControlConfiguration {

	/** Should the refactorings be checkable? */
	protected final boolean fCheckable;

	/** The project, or <code>null</code> for the workspace */
	protected final IProject fProject;

	/** Should time information be displayed? */
	protected final boolean fTime;

	/**
	 * Creates a new refactoring history control configuration.
	 *
	 * @param project
	 *            the project, or <code>null</code> for the workspace
	 * @param time
	 *            <code>true</code> to display time information,
	 *            <code>false</code> otherwise
	 * @param checkable
	 *            <code>true</code> if the refactorings should be checkable,
	 *            <code>false</code> otherwise
	 */
	public RefactoringHistoryControlConfiguration(final IProject project, final boolean time, final boolean checkable) {
		fProject= project;
		fTime= time;
		fCheckable= checkable;
	}

	/**
	 * Returns the label of a collection of refactorings.
	 *
	 * @return the collection label
	 */
	public String getCollectionLabel() {
		return RefactoringUIMessages.RefactoringHistoryControlConfiguration_collection_label;
	}

	/**
	 * Returns the text of the comment field below the refactoring history tree,
	 * if the currently selected refactoring descriptor provides no comment.
	 *
	 * @return the comment caption
	 */
	public String getCommentCaption() {
		return RefactoringUIMessages.RefactoringHistoryControlConfiguration_comment_caption;
	}

	/**
	 * Returns the content provider to use.
	 *
	 * @return the content provider to use
	 */
	public RefactoringHistoryContentProvider getContentProvider() {
		return new RefactoringHistoryContentProvider(this);
	}

	/**
	 * Returns the message format pattern to use for days.
	 * <p>
	 * Arguments:
	 * <ul>
	 * <li> {0}: formatted date </li>
	 * </ul>
	 * </p>
	 *
	 * @return the day pattern
	 */
	public String getDayPattern() {
		return RefactoringUIMessages.RefactoringHistoryControlConfiguration_day_pattern;
	}

	/**
	 * Returns the label provider to use.
	 *
	 * @return the label provider to use
	 */
	public RefactoringHistoryLabelProvider getLabelProvider() {
		return new RefactoringHistoryLabelProvider(this);
	}

	/**
	 * Returns the message format pattern to use for last month.
	 * <p>
	 * Arguments:
	 * <ul>
	 * <li> {0}: month name </li>
	 * </ul>
	 * </p>
	 *
	 * @return the last month pattern
	 */
	public String getLastMonthPattern() {
		return RefactoringUIMessages.RefactoringHistoryControlConfiguration_last_month_label;
	}

	/**
	 * Returns the message format pattern to use for last week.
	 * <p>
	 * Arguments:
	 * <ul>
	 * <li> {0}: week number </li>
	 * </ul>
	 * </p>
	 *
	 * @return the last week pattern
	 */
	public String getLastWeekPattern() {
		return RefactoringUIMessages.RefactoringHistoryControlConfiguration_last_week_label;
	}

	/**
	 * Returns the message format pattern to use for months.
	 * <p>
	 * Arguments:
	 * <ul>
	 * <li> {0}: month name </li>
	 * </ul>
	 * </p>
	 *
	 * @return the month pattern
	 */
	public String getMonthPattern() {
		return RefactoringUIMessages.RefactoringHistoryControlConfiguration_month_pattern;
	}

	/**
	 * Returns the project of the refactoring history being displayed.
	 *
	 * @return the project, or <code>null</code> for the workspace
	 */
	public final IProject getProject() {
		return fProject;
	}

	/**
	 * Returns the message format pattern to use if refactorings of exactly one
	 * project are displayed.
	 * <p>
	 * Arguments:
	 * <ul>
	 * <li> {0}: name of project </li>
	 * </ul>
	 * </p>
	 *
	 * @return the project pattern
	 */
	public String getProjectPattern() {
		return RefactoringUIMessages.RefactoringHistoryControlConfiguration_project_pattern;
	}

	/**
	 * Returns the message format pattern to use for refactorings.
	 * <p>
	 * Arguments:
	 * <ul>
	 * <li> {0}: formatted date </li>
	 * <li> {0}: description of refactoring</li>
	 * </ul>
	 * </p>
	 *
	 * @return the refactoring pattern
	 */
	public String getRefactoringPattern() {
		return RefactoringUIMessages.RefactoringHistoryControlConfiguration_refactoring_pattern;
	}

	/**
	 * Returns the message format pattern to use for this month.
	 * <p>
	 * Arguments:
	 * <ul>
	 * <li> {0}: month name </li>
	 * </ul>
	 * </p>
	 *
	 * @return the this month pattern
	 */
	public String getThisMonthPattern() {
		return RefactoringUIMessages.RefactoringHistoryControlConfiguration_this_month_pattern;
	}

	/**
	 * Returns the message format pattern to use for this week.
	 * <p>
	 * Arguments:
	 * <ul>
	 * <li> {0}: week number </li>
	 * </ul>
	 * </p>
	 *
	 * @return the this week pattern
	 */
	public String getThisWeekPattern() {
		return RefactoringUIMessages.RefactoringHistoryControlConfiguration_this_week_pattern;
	}

	/**
	 * Returns the message format pattern to use for today.
	 * <p>
	 * Arguments:
	 * <ul>
	 * <li> {0}: formatted date </li>
	 * </ul>
	 * </p>
	 *
	 * @return the today pattern
	 */
	public String getTodayPattern() {
		return RefactoringUIMessages.RefactoringHistoryControlConfiguration_today_pattern;
	}

	/**
	 * Returns the message format pattern to use for weeks.
	 * <p>
	 * Arguments:
	 * <ul>
	 * <li> {0}: week number </li>
	 * </ul>
	 * </p>
	 *
	 * @return the week pattern
	 */
	public String getWeekPattern() {
		return RefactoringUIMessages.RefactoringHistoryControlConfiguration_week_pattern;
	}

	/**
	 * Returns the caption of the refactoring history tree if refactorings of
	 * more than one project are displayed.
	 *
	 * @return the workspace caption
	 */
	public String getWorkspaceCaption() {
		return RefactoringUIMessages.RefactoringHistoryControlConfiguration_workspace_caption;
	}

	/**
	 * Returns the message format pattern to use for years.
	 * <p>
	 * Arguments:
	 * <ul>
	 * <li> {0}: year number </li>
	 * </ul>
	 * </p>
	 *
	 * @return the year pattern
	 */
	public String getYearPattern() {
		return RefactoringUIMessages.RefactoringHistoryControlConfiguration_year_pattern;
	}

	/**
	 * Returns the message format pattern to use for yesterday.
	 * <p>
	 * Arguments:
	 * <ul>
	 * <li> {0}: formatted date </li>
	 * </ul>
	 * </p>
	 *
	 * @return the yesterday pattern
	 */
	public String getYesterdayPattern() {
		return RefactoringUIMessages.RefactoringHistoryControlConfiguration_yesterday_pattern;
	}

	/**
	 * Returns whether the control should make the refactorings checkable.
	 *
	 * @return <code>true</code> if the control should make refactorings
	 *         checkable, <code>false</code> otherwise
	 */
	public final boolean isCheckableViewer() {
		return fCheckable;
	}

	/**
	 * Returns whether the control should display time information.
	 *
	 * @return <code>true</code> to display time information,
	 *         <code>false</code> otherwise
	 */
	public final boolean isTimeDisplayed() {
		return fTime;
	}
}
