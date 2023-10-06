/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;

import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;

/**
 * Proxy of a refactoring descriptor.
 * <p>
 * Refactoring descriptors are exposed by the refactoring history service as
 * lightweight proxy objects. Refactoring descriptor proxies have an efficient
 * memory representation and are therefore suited to model huge refactoring
 * histories which may be displayed in the user interface. The refactoring
 * history service may hand out any number of proxies for a given descriptor.
 * Proxies only offer direct access to the time stamp {@link #getTimeStamp()},
 * the related project {@link #getProject()} and description
 * {@link #getDescription()}. In order to access other information such as
 * arguments and comments, clients have to call
 * {@link #requestDescriptor(IProgressMonitor)} in order to obtain the actual
 * refactoring descriptor.
 * </p>
 * <p>
 * Refactoring descriptors are potentially heavy weight objects which should not
 * be held on to. Proxies which are retrieved from external sources (e.g. not
 * from the local refactoring history service) may encapsulate refactoring
 * descriptors and should not be held in memory as well.
 * </p>
 * <p>
 * All time stamps are measured as the milliseconds since January 1, 1970,
 * 00:00:00 GMT.
 * </p>
 * <p>
 * Note: this class is not intended to be subclassed by clients.
 * </p>
 *
 * @see IRefactoringHistoryService
 * @see RefactoringHistory
 *
 * @since 3.2
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class RefactoringDescriptorProxy extends PlatformObject implements Comparable<RefactoringDescriptorProxy> {

	/**
	 * {@inheritDoc}
	 * @since 3.7
	 */
	@Override
	public int compareTo(RefactoringDescriptorProxy proxy) {
		long delta= getTimeStamp() - proxy.getTimeStamp();
		if (delta > 0)
			return 1;
		else if (delta < 0)
			return -1;
		return 0;
	}

	@Override
	public final boolean equals(final Object object) {
		if (object instanceof RefactoringDescriptorProxy) {
			final RefactoringDescriptorProxy proxy= (RefactoringDescriptorProxy) object;
			return getTimeStamp() == proxy.getTimeStamp() && getDescription().equals(proxy.getDescription());
		}
		return false;
	}

	/**
	 * Returns a human-readable description of refactoring.
	 *
	 * @return a description of the refactoring
	 */
	public abstract String getDescription();

	/**
	 * Returns the name of the associated project.
	 *
	 * @return the non-empty name of the project, or <code>null</code>
	 */
	public abstract String getProject();

	/**
	 * Returns the time stamp of this refactoring.
	 *
	 * @return the time stamp, or <code>-1</code> if no time information is
	 *         available
	 */
	public abstract long getTimeStamp();

	@Override
	public final int hashCode() {
		int code= getDescription().hashCode();
		final long stamp= getTimeStamp();
		if (stamp >= 0)
			code+= (17 * stamp);
		return code;
	}

	/**
	 * Resolves this proxy and returns the associated refactoring descriptor.
	 * <p>
	 * Clients must connect to the refactoring history service first before
	 * calling this method.
	 * </p>
	 *
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 *
	 * @return the refactoring descriptor, or <code>null</code>
	 */
	public RefactoringDescriptor requestDescriptor(final IProgressMonitor monitor) {
		return RefactoringHistoryService.getInstance().requestDescriptor(this, monitor);
	}
}
