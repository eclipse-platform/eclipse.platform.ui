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

	/**
	 * Adds the specified refactoring history participant to this history.
	 * 
	 * @param participant
	 *            the participant to add
	 */
	public void addHistoryParticipant(final IRefactoringHistoryParticipant participant);

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
	public RefactoringDescriptorHandle[] getProjectHistory(final IProject project);

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
	public RefactoringDescriptorHandle[] getProjectHistory(final IProject project, final long start, final long end);

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
	public RefactoringDescriptorHandle[] getWorkspaceHistory(final long start, final long end);

	/**
	 * Removes the specified refactoring participant from this history.
	 * 
	 * @param participant
	 *            the participant to remove
	 */
	public void removeHistoryParticipant(final IRefactoringHistoryParticipant participant);
}
