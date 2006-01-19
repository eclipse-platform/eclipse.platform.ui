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

import org.eclipse.ltk.core.refactoring.IRefactoringCoreStatusCodes;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
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
 * @see RefactoringCore
 * @see IRefactoringHistoryListener
 * @see IRefactoringExecutionListener
 * 
 * @see RefactoringHistory
 * @see RefactoringDescriptorProxy
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
	 * Deletes the refactoring history of a project. Refactorings associated
	 * with the workspace are not deleted.
	 * <p>
	 * If a refactoring history is deleted, all files stored in the
	 * <code>.refactorings</code> folder of the project folder is removed. If
	 * no explicit refactoring history is enabled, the refactoring history
	 * information is removed internally.
	 * </p>
	 * <p>
	 * Note: This API must not be called from outside the refactoring framework.
	 * </p>
	 * 
	 * @param project
	 *            the project to delete its history
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 * @throws CoreException
	 *             if an error occurs while deleting the refactoring history.
	 *             Reasons include:
	 *             <ul>
	 *             <li>An I/O error occurs while deleting the refactoring
	 *             history.</li>
	 *             </ul>
	 */
	public void deleteProjectHistory(IProject project, IProgressMonitor monitor) throws CoreException;

	/**
	 * Deletes the specified refactoring descriptors from their associated
	 * refactoring histories.
	 * <p>
	 * Note: This API must not be called from outside the refactoring framework.
	 * </p>
	 * 
	 * @param proxies
	 *            the refactoring descriptor proxies
	 * @param query
	 *            the refactoring descriptor delete query to use
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 * @throws CoreException
	 *             if an error occurs while deleting the refactoring
	 *             descriptors. Reasons include:
	 *             <ul>
	 *             <li>The refactoring history has an illegal format, contains
	 *             illegal arguments or otherwise illegal information.</li>
	 *             <li>An I/O error occurs while deleting the refactoring
	 *             descriptors from the refactoring history.</li>
	 *             </ul>
	 * 
	 * @see IRefactoringCoreStatusCodes#REFACTORING_HISTORY_FORMAT_ERROR
	 * @see IRefactoringCoreStatusCodes#REFACTORING_HISTORY_IO_ERROR
	 */
	public void deleteRefactoringDescriptors(RefactoringDescriptorProxy[] proxies, IRefactoringDescriptorDeleteQuery query, IProgressMonitor monitor) throws CoreException;

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
	 * @param flags
	 *            the refactoring descriptor flags which must be present in
	 *            order to be returned in the refactoring history object, or
	 *            <code>RefactoringDescriptor#NONE</code>
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 * @return the project refactoring history
	 */
	public RefactoringHistory getProjectHistory(IProject project, long start, long end, int flags, IProgressMonitor monitor);

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
	 *            the progress monitor to use, or <code>null</code>
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
	 * @param flags
	 *            the refactoring descriptor flags which must be present in
	 *            order to be returned in the refactoring history object, or
	 *            <code>RefactoringDescriptor#NONE</code>
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 * @return the combined refactoring history
	 */
	public RefactoringHistory getRefactoringHistory(IProject[] projects, long start, long end, int flags, IProgressMonitor monitor);

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
	 * @param flags
	 *            the refactoring descriptor flags to filter the refactoring
	 *            descriptors
	 * @return a refactoring history containing the filtered refactoring
	 *         descriptors
	 * @throws CoreException
	 *             if an error occurs while reading form the input stream.
	 *             Reasons include:
	 *             <ul>
	 *             <li>The input stream contains no version information for the
	 *             refactoring history.</li>
	 *             <li>The input stream contains an unsupported version of a
	 *             refactoring history.</li>
	 *             <li>An I/O error occurs while reading the refactoring
	 *             history from the input stream.</li>
	 *             </ul>
	 * 
	 * @see RefactoringDescriptor#NONE
	 * @see RefactoringDescriptor#STRUCTURAL_CHANGE
	 * @see RefactoringDescriptor#BREAKING_CHANGE
	 * 
	 * @see IRefactoringCoreStatusCodes#REFACTORING_HISTORY_IO_ERROR
	 * @see IRefactoringCoreStatusCodes#UNSUPPORTED_REFACTORING_HISTORY_VERSION
	 * @see IRefactoringCoreStatusCodes#MISSING_REFACTORING_HISTORY_VERSION
	 */
	public RefactoringHistory readRefactoringHistory(InputStream stream, int flags) throws CoreException;

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
	 * enabled, all refactorings are tracked as well, but persisted internally
	 * in a plugin-specific way without altering the project.
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
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 * @throws CoreException
	 *             if an error occurs while changing the explicit refactoring
	 *             history property. Reasons include:
	 *             <ul>
	 *             <li>An I/O error occurs while changing the explicit
	 *             refactoring history property.</li>
	 *             </ul>
	 */
	public void setProjectHistory(IProject project, boolean enable, IProgressMonitor monitor) throws CoreException;

	/**
	 * Writes the specified refactoring descriptor proxies to the output stream.
	 * 
	 * @param proxies
	 *            the refactoring descriptor proxies
	 * @param stream
	 *            the output stream
	 * @param flags
	 *            the flags which must be present in order to be written to the
	 *            output stream
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 * @throws CoreException
	 *             if an error occurs while writing to the output stream.
	 *             Reasons include:
	 *             <ul>
	 *             <li>The refactoring descriptors have an illegal format,
	 *             contain illegal arguments or otherwise illegal information.</li>
	 *             <li>An I/O error occurs while writing the refactoring
	 *             descriptors to the output stream.</li>
	 *             </ul>
	 * 
	 * @see RefactoringDescriptor#NONE
	 * @see RefactoringDescriptor#STRUCTURAL_CHANGE
	 * @see RefactoringDescriptor#BREAKING_CHANGE
	 * 
	 * @see IRefactoringCoreStatusCodes#REFACTORING_HISTORY_FORMAT_ERROR
	 * @see IRefactoringCoreStatusCodes#REFACTORING_HISTORY_IO_ERROR
	 */
	public void writeRefactoringDescriptors(RefactoringDescriptorProxy[] proxies, OutputStream stream, int flags, IProgressMonitor monitor) throws CoreException;
}