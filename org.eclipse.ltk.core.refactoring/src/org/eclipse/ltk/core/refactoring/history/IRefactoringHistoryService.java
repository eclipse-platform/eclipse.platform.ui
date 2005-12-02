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
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.IProject;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

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
 * Note: this interface is not intended to be implemented by clients.
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
	 * history if necessary and increments an internal counter.
	 * <p>
	 * If the service is already connected, nothing happens.
	 * </p>
	 * <p>
	 * Every call to {@link #connect()} must be balanced with a corresponding
	 * call to {@link #disconnect()}.
	 * </p>
	 */
	public void connect();

	/**
	 * Disconnects the refactoring history service from the workbench's
	 * operation history if necessary and decrements an internal counter.
	 * <p>
	 * If the service is not connected, nothing happens. If the service is
	 * connected, all resources acquired since the corresponding call to
	 * {@link #connect()} are released.
	 * </p>
	 * <p>
	 * Every call to {@link #disconnect()} must be balanced with a corresponding
	 * call to {@link #connect()}.
	 * </p>
	 */
	public void disconnect();

	/**
	 * Returns a project refactoring history for the specified project.
	 * <p>
	 * Clients must connect to the refactoring history service first before
	 * calling this method.
	 * </p>
	 * 
	 * @param project
	 *            the project, which must exist
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 * @return the project refactoring history
	 */
	public RefactoringHistory getProjectHistory(IProject project, IProgressMonitor monitor);

	/**
	 * Returns a project refactoring history for the specified project.
	 * <p>
	 * Clients must connect to the refactoring history service first before
	 * calling this method.
	 * </p>
	 * 
	 * @param project
	 *            the project, which must exist
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp, inclusive
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 * @return the project refactoring history
	 */
	public RefactoringHistory getProjectHistory(IProject project, long start, long end, IProgressMonitor monitor);

	/**
	 * Returns the combined refactoring history for the specified projects.
	 * <p>
	 * Clients must connect to the refactoring history service first before
	 * calling this method.
	 * </p>
	 * 
	 * @param projects
	 *            the projects, which must exist
	 * @param monitor
	 *            the progress monitor to use
	 * @return the combined refactoring history
	 */
	public RefactoringHistory getRefactoringHistory(IProject[] projects, IProgressMonitor monitor);

	/**
	 * Returns the combined refactoring history for the specified projects.
	 * <p>
	 * Clients must connect to the refactoring history service first before
	 * calling this method.
	 * </p>
	 * 
	 * @param projects
	 *            the projects, which must exist
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp, inclusive
	 * @param monitor
	 *            the progress monitor to use
	 * @return the combined refactoring history
	 */
	public RefactoringHistory getRefactoringHistory(IProject[] projects, long start, long end, IProgressMonitor monitor);

	/**
	 * Returns the workspace refactoring history.
	 * <p>
	 * Clients must connect to the refactoring history service first before
	 * calling this method.
	 * </p>
	 * 
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 * @return the workspace refactoring history
	 */
	public RefactoringHistory getWorkspaceHistory(IProgressMonitor monitor);

	/**
	 * Returns the workspace refactoring history.
	 * <p>
	 * Clients must connect to the refactoring history service first before
	 * calling this method.
	 * </p>
	 * 
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp, inclusive
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 * @return the workspace refactoring history
	 */
	public RefactoringHistory getWorkspaceHistory(long start, long end, IProgressMonitor monitor);

	/**
	 * Returns whether a project has an explicit refactoring history.
	 * 
	 * @param project
	 *            the project to test
	 * @return <code>true</code> if the project contains an explicit project
	 *         history, <code>false</code> otherwise
	 */
	public boolean hasProjectHistory(IProject project);

	/**
	 * Reads a refactoring history from the input stream.
	 * 
	 * @param stream
	 *            the input stream
	 * @param filter
	 *            the flags which must be present in order to be read from the
	 *            input stream
	 * @return a refactoring history
	 * @throws CoreException
	 *             if an error occurs
	 */
	public RefactoringHistory readRefactoringHistory(InputStream stream, int filter) throws CoreException;

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
	 * Determines whether a project has an explicit refactoring history.
	 * <p>
	 * If an explicit refactoring history is enabled, refactorings executed on
	 * that particular project are stored in a <code>.refactorings</code>
	 * folder of the project folder. If no explicit refactoring history is
	 * enabled, all refactoring information is tracked as well, but persisted
	 * internally in a plugin-specific way without altering the project.
	 * </p>
	 * <p>
	 * Note: This API must not be called from outside the refactoring framework.
	 * </p>
	 * 
	 * @param project
	 *            the project to set
	 * @param enable
	 *            <code>true</code> to enable an explicit project history,
	 *            <code>false</code> otherwise
	 * @throws CoreException
	 *             if an error occurs
	 */
	public void setProjectHistory(IProject project, boolean enable) throws CoreException;

	/**
	 * Writes the specified refactoring descriptor proxies to the output stream.
	 * 
	 * @param proxies
	 *            the refactoring descriptor proxies
	 * @param stream
	 *            the output stream
	 * @param filter
	 *            the flags which must be present in order to be written to the
	 *            output stream
	 * @throws CoreException
	 *             if an error occurs
	 */
	public void writeRefactoringDescriptors(RefactoringDescriptorProxy[] proxies, OutputStream stream, int filter) throws CoreException;
}