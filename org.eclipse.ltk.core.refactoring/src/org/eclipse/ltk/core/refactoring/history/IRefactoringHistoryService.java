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
package org.eclipse.ltk.core.refactoring.history;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IProject;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorHandle;

/**
 * Interface for a refactoring history service. A refactoring history service
 * provides methods to register refactoring history listeners, refactoring
 * execution listeners and facilities to query the global refactoring history
 * index for specific refactoring histories. Additionally, methods are provided
 * which read or write refactoring information.
 * <p>
 * An instance of a refactoring history service may be obtained by calling
 * {@link RefactoringCore#getRefactoringHistoryService()}.
 * </p>
 * <p>
 * All time stamps are measured in UTC milliseconds from the epoch (see
 * {@link java.util#Calendar}).
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public interface IRefactoringHistoryService {

	/**
	 * Adds the specified refactoring execution listener to this service.
	 * <p>
	 * If the listener is already registered with the service, nothing happens.
	 * </p>
	 * 
	 * @param listener
	 *            the listener to add
	 */
	public void addExecutionListener(IRefactoringExecutionListener listener);

	/**
	 * Adds the specified refactoring history listener to this service.
	 * <p>
	 * If the listener is already registered with the service, nothing happens.
	 * </p>
	 * 
	 * @param listener
	 *            the participant to add
	 */
	public void addHistoryListener(IRefactoringHistoryListener listener);

	/**
	 * Connects the refactoring history service to the workbench's operation
	 * history.
	 * <p>
	 * If the service is already connected, nothing happens.
	 * </p>
	 */
	public void connect();

	/**
	 * Disconnects the refactoring history service from the workbench's
	 * operation history.
	 * <p>
	 * If the service is not connected, nothing happens.
	 * </p>
	 */
	public void disconnect();

	/**
	 * Returns a project refactoring history for the specified project.
	 * 
	 * @param project
	 *            the project, which must exist
	 * @return The project refactoring history
	 */
	public RefactoringHistory getProjectHistory(IProject project);

	/**
	 * Returns a project refactoring history for the specified project.
	 * 
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp, inclusive
	 * @param project
	 *            the project, which must exist
	 * @return The project refactoring history
	 */
	public RefactoringHistory getProjectHistory(IProject project, long start, long end);

	/**
	 * Returns the workspace refactoring history.
	 * 
	 * @return The workspace refactoring history
	 */
	public RefactoringHistory getWorkspaceHistory();

	/**
	 * Returns the workspace refactoring history.
	 * 
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp, inclusive
	 * @return The workspace refactoring history
	 */
	public RefactoringHistory getWorkspaceHistory(long start, long end);

	/**
	 * Reads a refactoring history from the input stream.
	 * 
	 * @param stream
	 *            the input stream
	 * @return A refactoring history
	 * @throws CoreException
	 *             if an error occurs
	 */
	public RefactoringHistory readRefactoringHistory(InputStream stream) throws CoreException;

	/**
	 * Removes the specified refactoring execution listener from this service.
	 * <p>
	 * If the listener is not registered with the service, nothing happens.
	 * </p>
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	public void removeExecutionListener(IRefactoringExecutionListener listener);

	/**
	 * Removes the specified refactoring history listener from this service.
	 * <p>
	 * If the listener is not registered with the service, nothing happens.
	 * </p>
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	public void removeHistoryListener(IRefactoringHistoryListener listener);

	/**
	 * Writes the specified refactoring history to the output stream.
	 * 
	 * @param handles
	 *            the refactoring descriptor handles
	 * @param stream
	 *            the output stream
	 * @throws CoreException
	 *             if an error occurs
	 */
	public void writeRefactoringHistory(RefactoringDescriptorHandle[] handles, OutputStream stream) throws CoreException;
}
