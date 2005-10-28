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

import org.eclipse.ltk.internal.core.refactoring.Assert;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;

/**
 * Handle to a refactoring descriptor.
 * <p>
 * Refactoring descriptors are exposed by the refactoring history service as
 * lightweight handle objects. The refactoring history service may hand out any
 * number of handles for a given descriptor. Handles only offer direct access to
 * the time stamp {@link #getTimeStamp()} and description
 * {@link #getDescription()}. In order to access other information such as
 * arguments and comments, clients have to resolve the handle by calling
 * {@link #resolveDescriptor()} to obtain the refactoring descriptor.
 * </p>
 * <p>
 * Refactoring descriptors are potentially heavyweight objects which should not
 * be held on to. Handles which are retrieved from external sources may
 * encapsulate refactoring descriptors and should not be held in memory as well.
 * </p>
 * <p>
 * This class is not intended to be subclassed and instantiated by clients.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public class RefactoringDescriptorHandle {

	/** The description of the refactoring */
	private final String fDescription;

	/** The time stamp of the refactoring */
	private final long fTimeStamp;

	/**
	 * Creates a new refactoring descriptor handle.
	 * 
	 * @param description
	 *            a non-empty human-readable description of the particular
	 *            refactoring instance
	 * @param stamp
	 *            the time stamp of the refactoring
	 */
	public RefactoringDescriptorHandle(final String description, final long stamp) {
		Assert.isTrue(description != null && !"".equals(description)); //$NON-NLS-1$
		fDescription= description;
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
	 * Returns the time stamp of this refactoring.
	 * 
	 * @return the time stamp, or <code>-1</code> if no time information is
	 *         available
	 */
	public long getTimeStamp() {
		return fTimeStamp;
	}

	/**
	 * Resolves this handle and returns the associated refactoring descriptor.
	 * <p>
	 * This method is not intended to be overridden outside the refactoring
	 * framework.
	 * </p>
	 * 
	 * @return the refactoring descriptor, or <code>null</code>
	 */
	public RefactoringDescriptor resolveDescriptor() {
		RefactoringDescriptor descriptor= null;
		final RefactoringHistoryService history= RefactoringHistoryService.getInstance();
		try {
			history.connect();
			descriptor= history.resolveDescriptor(this);
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
