/*******************************************************************************
 * Copyright (c) 2005 Tobias Widmer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Tobias Widmer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.history;

import org.eclipse.core.resources.IProject;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;

/**
 * Configuration object for a refactoring history control.
 * <p>
 * Note: this class is intended to be subclassed outside the refactoring
 * framework.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public class RefactoringHistoryControlConfiguration {

	/** The information message, or <code>null</code> */
	protected final String fMessage;

	/** The project, or <code>null</code> */
	protected final IProject fProject;

	/** Should time information be displayed? */
	protected final boolean fTime;

	/**
	 * Creates a new refactoring history control configuration.
	 * 
	 * @param project
	 *            the project, or <code>null</code>
	 * @param message
	 *            the information message, or <code>null</code>
	 * @param time
	 *            <code>true</code> to display time information,
	 *            <code>false</code> otherwise
	 */
	public RefactoringHistoryControlConfiguration(final IProject project, final String message, final boolean time) {
		fProject= project;
		fMessage= message;
		fTime= time;
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
	 * Returns the caption of the comment field below the refactoring history
	 * tree.
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
	 * Returns the last month label.
	 * 
	 * @return the last month label
	 */
	public String getLastMonthLabel() {
		return RefactoringUIMessages.RefactoringHistoryControlConfiguration_last_month_label;
	}

	/**
	 * Returns the last week label.
	 * 
	 * @return the last week label
	 */
	public String getLastWeekLabel() {
		return RefactoringUIMessages.RefactoringHistoryControlConfiguration_last_week_label;
	}

	/**
	 * Returns the information message of the control.
	 * 
	 * @return the information message
	 */
	public String getMessage() {
		return fMessage;
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
	 * @return the project, or <code>null</code>
	 */
	public IProject getProject() {
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
	 * Returns the this month label.
	 * 
	 * @return the this month label
	 */
	public String getThisMonthLabel() {
		return RefactoringUIMessages.RefactoringHistoryControlConfiguration_this_month_pattern;
	}

	/**
	 * Returns the this week label.
	 * 
	 * @return the this week label
	 */
	public String getThisWeekLabel() {
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
	 * Returns whether the control should display time information.
	 * 
	 * @return <code>true</code> to display time information,
	 *         <code>false</code> otherwise
	 */
	public boolean isTimeDisplayed() {
		return fTime;
	}
}