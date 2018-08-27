/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.model;

import org.eclipse.team.core.diff.IThreeWayDiff;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

/**
 * Wrapper which wraps a refactoring descriptor proxy and adds synchronization
 * information to it.
 *
 * @since 3.2
 */
public final class RefactoringDescriptorSynchronizationProxy extends RefactoringDescriptorProxy {

	/** The direction of the difference */
	private final int fDirection;

	/** The non-empty name of the associated project */
	private final String fProject;

	/** The encapsulated descriptor proxy */
	private final RefactoringDescriptorProxy fProxy;

	/**
	 * Creates a new refactoring descriptor synchronization proxy.
	 * <p>
	 * The value of the direction argument is used to compose an icon which
	 * reflects the direction of the difference between the two or three
	 * versions of the refactoring descriptor.
	 * </p>
	 *
	 * @param proxy
	 *            the descriptor proxy to encapsulate
	 * @param project
	 *            the non-empty name of the project the refactoring is
	 *            associated with
	 * @param direction
	 *            the direction of the difference
	 *
	 * @see IThreeWayDiff#getDirection()
	 */
	public RefactoringDescriptorSynchronizationProxy(final RefactoringDescriptorProxy proxy, final String project, final int direction) {
		Assert.isNotNull(proxy);
		Assert.isNotNull(project);
		Assert.isTrue(!"".equals(project)); //$NON-NLS-1$
		fProxy= proxy;
		fProject= project;
		fDirection= direction;
	}

	@Override
	public int compareTo(final RefactoringDescriptorProxy object) {
		return fProxy.compareTo(object);
	}

	@Override
	public String getDescription() {
		return fProxy.getDescription();
	}

	/**
	 * Returns the direction of the difference of this refactoring descriptor.
	 * <p>
	 * The result of this method is used to compose an icon which reflects the
	 * direction of the difference between the two or three versions of the
	 * refactoring descriptor.
	 * </p>
	 *
	 * @return the direction of the difference
	 *
	 * @see IThreeWayDiff#getDirection()
	 */
	public int getDirection() {
		return fDirection;
	}

	@Override
	public String getProject() {
		return fProject;
	}

	@Override
	public long getTimeStamp() {
		return fProxy.getTimeStamp();
	}

	@Override
	public RefactoringDescriptor requestDescriptor(final IProgressMonitor monitor) {
		return fProxy.requestDescriptor(monitor);
	}

	@Override
	public String toString() {
		return fProxy.toString();
	}
}