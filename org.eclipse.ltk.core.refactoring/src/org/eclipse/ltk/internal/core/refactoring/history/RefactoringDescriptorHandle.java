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

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.ltk.internal.core.refactoring.Assert;

/**
 * Handle to a refactoring descriptor.
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
	 * 
	 * @return the refactoring descriptor, or <code>null</code>
	 */
	public RefactoringDescriptor resolveDescriptor() {
		RefactoringDescriptor descriptor= null;
		final RefactoringHistory history= RefactoringHistory.getInstance();
		try {
			history.connect();
			descriptor= history.resolveDescriptor(this);
		} finally {
			history.disconnect();
		}
		return descriptor;
	}
}
