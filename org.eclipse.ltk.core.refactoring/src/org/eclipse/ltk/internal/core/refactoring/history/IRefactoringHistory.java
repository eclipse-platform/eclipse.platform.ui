/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.history;

import org.eclipse.core.resources.IProject;

/**
 * Interface for a workspace refactoring history. An instance of a refactoring
 * history may be obtained by calling
 * {@link org.eclipse.ltk.core.refactoring.RefactoringCore#getRefactoringHistory()}.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public interface IRefactoringHistory {

	/** The refactoring history file */
	public static final String NAME_REFACTORING_HISTORY= "refactorings.history"; //$NON-NLS-1$

	/** The refactoring history folder */
	public static final String NAME_REFACTORINGS_FOLDER= ".refactorings"; //$NON-NLS-1$

	/**
	 * Adds the specified refactoring history listener to this history.
	 * <p>
	 * If the listener is already registered with the history, nothing happens.
	 * </p>
	 * 
	 * @param listener
	 *            the listener to add
	 */
	public void addHistoryListener(IRefactoringHistoryListener listener);

	/**
	 * Adds the specified refactoring history participant to this history.
	 * <p>
	 * If the participant is already registered with the history, nothing
	 * happens.
	 * </p>
	 * 
	 * @param participant
	 *            the participant to add
	 */
	public void addHistoryParticipant(IRefactoringHistoryParticipant participant);

	/**
	 * Connects the refactoring history to the workbench's operation history.
	 * <p>
	 * If the history is already connected, nothing happens.
	 * </p>
	 */
	public void connect();

	/**
	 * Disconnects the refactoring history from the workbench's operation
	 * history.
	 * <p>
	 * If the history is not connected, nothing happens.
	 * </p>
	 */
	public void disconnect();

	/**
	 * Returns a project refactoring history for the specified project.
	 * 
	 * @param project
	 *            the project, which must exist
	 * @return An array of refactoring descriptor handles, in no particular
	 *         order
	 */
	public RefactoringDescriptorHandle[] getProjectHistory(IProject project);

	/**
	 * Returns a project refactoring history for the specified project.
	 * 
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp, inclusive
	 * @param project
	 *            the project, which must exist
	 * @return An array of refactoring descriptor handles, in no particular
	 *         order
	 */
	public RefactoringDescriptorHandle[] getProjectHistory(IProject project, long start, long end);

	/**
	 * Returns the workspace refactoring history.
	 * 
	 * @return An array of refactoring descriptor handles, in no particular
	 *         order
	 */
	public RefactoringDescriptorHandle[] getWorkspaceHistory();

	/**
	 * Returns the workspace refactoring history.
	 * 
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp, inclusive
	 * @return An array of refactoring descriptor handles, in no particular
	 *         order
	 */
	public RefactoringDescriptorHandle[] getWorkspaceHistory(long start, long end);

	/**
	 * Removes the specified refactoring history listener from this history.
	 * <p>
	 * If the listener is not registered with the history, nothing happens.
	 * </p>
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	public void removeHistoryListener(IRefactoringHistoryListener listener);

	/**
	 * Removes the specified refactoring history participant from this history.
	 * <p>
	 * If the participant is not registered with the history, nothing happens.
	 * </p>
	 * 
	 * @param participant
	 *            the participant to remove
	 */
	public void removeHistoryParticipant(IRefactoringHistoryParticipant participant);
}
