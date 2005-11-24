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
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

/**
 * Default implementation of a refactoring descriptor proxy.
 * 
 * @since 3.2
 */
public final class RefactoringDescriptorProxyImplementation extends RefactoringDescriptorProxy {

	/** The description of the refactoring */
	private final String fDescription;

	/** The non-empty name of the project, or <code>null</code> */
	private final String fProject;

	/** The time stamp of the refactoring */
	private final long fTimeStamp;

	/**
	 * Creates a new refactoring descriptor proxy implementation.
	 * 
	 * @param description
	 *            a non-empty human-readable description of the particular
	 *            refactoring instance
	 * @param project
	 *            the non-empty name of the project, or <code>null</code>
	 * @param stamp
	 *            the time stamp of the refactoring
	 */
	public RefactoringDescriptorProxyImplementation(final String description, final String project, final long stamp) {
		Assert.isTrue(project == null || !"".equals(project)); //$NON-NLS-1$
		Assert.isTrue(description != null && !"".equals(description)); //$NON-NLS-1$
		fDescription= description.intern();
		fProject= project != null ? project.intern() : null;
		fTimeStamp= stamp;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription() {
		return fDescription;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getProject() {
		return fProject;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getTimeStamp() {
		return fTimeStamp;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {

		final StringBuffer buffer= new StringBuffer(128);

		buffer.append(getClass().getName());
		buffer.append("[stamp="); //$NON-NLS-1$
		buffer.append(fTimeStamp);
		buffer.append(",project="); //$NON-NLS-1$
		buffer.append(fProject);
		buffer.append(",description="); //$NON-NLS-1$
		buffer.append(fDescription);
		buffer.append("]"); //$NON-NLS-1$

		return buffer.toString();
	}
}
