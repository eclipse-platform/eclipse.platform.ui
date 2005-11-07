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
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ltk.internal.core.refactoring.Assert;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;

/**
 * Proxy to a refactoring descriptor.
 * <p>
 * Refactoring descriptors are exposed by the refactoring history service as
 * lightweight proxy objects. The refactoring history service may hand out any
 * number of proxies for a given descriptor. Proxies only offer direct access to
 * the time stamp {@link #getTimeStamp()}, the related project
 * {@link #getProject()} and description {@link #getDescription()}. In order to
 * access other information such as arguments and comments, clients have to call
 * {@link #requestDescriptor(IProgressMonitor)} in order to obtain the
 * refactoring descriptor.
 * </p>
 * <p>
 * Refactoring descriptors are potentially heavyweight objects which should not
 * be held on to. Proxies which are retrieved from external sources may
 * encapsulate refactoring descriptors and should not be held in memory as well.
 * </p>
 * <p>
 * All time stamps are measured in UTC milliseconds from the epoch (see
 * {@link java.util#Calendar}).
 * </p>
 * <p>
 * Note: this class is not intended to be subclassed and instantiated by
 * clients.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public class RefactoringDescriptorProxy {

	/** The description of the refactoring */
	private final String fDescription;

	/** The non-empty name of the project, or <code>null</code> */
	private final String fProject;

	/** The time stamp of the refactoring */
	private final long fTimeStamp;

	/**
	 * Creates a new refactoring descriptor proxy.
	 * 
	 * @param description
	 *            a non-empty human-readable description of the particular
	 *            refactoring instance
	 * @param project
	 *            the non-empty name of the project, or <code>null</code>
	 * @param stamp
	 *            the time stamp of the refactoring
	 */
	public RefactoringDescriptorProxy(final String description, final String project, final long stamp) {
		Assert.isTrue(project == null || !"".equals(project)); //$NON-NLS-1$
		Assert.isTrue(description != null && !"".equals(description)); //$NON-NLS-1$
		fDescription= description.intern();
		fProject= project != null ? project.intern() : null;
		fTimeStamp= stamp;
	}

	/**
	 * Returns a human-readable description of the particular refactoring
	 * instance.
	 * 
	 * @return a description of the refactoring
	 */
	public String getDescription() {
		return fDescription;
	}

	/**
	 * Returns the name of the project.
	 * 
	 * @return the non-empty name, or <code>null</code>
	 */
	public String getProject() {
		return fProject;
	}

	/**
	 * Returns the time stamp of this refactoring.
	 * 
	 * @return the time stamp, or <code>-1</code> if no time information is
	 *         available
	 */
	public long getTimeStamp() {
		return fTimeStamp;
	}

	/**
	 * Resolves this proxy and returns the associated refactoring descriptor.
	 * <p>
	 * This method is not intended to be overridden outside the refactoring
	 * framework.
	 * </p>
	 * 
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 * 
	 * @return the refactoring descriptor, or <code>null</code>
	 */
	public RefactoringDescriptor requestDescriptor(final IProgressMonitor monitor) {
		RefactoringDescriptor descriptor= null;
		final RefactoringHistoryService history= RefactoringHistoryService.getInstance();
		try {
			history.connect();
			descriptor= history.requestDescriptor(this, monitor);
		} finally {
			history.disconnect();
		}
		return descriptor;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {

		final StringBuffer buffer= new StringBuffer(128);

		buffer.append(getClass().getName());
		buffer.append("[timeStamp="); //$NON-NLS-1$
		buffer.append(fTimeStamp);
		buffer.append(",description="); //$NON-NLS-1$
		buffer.append(fDescription);
		buffer.append("]"); //$NON-NLS-1$

		return buffer.toString();
	}
}
