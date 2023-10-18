/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.internal.core.refactoring.history;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

/**
 * Default implementation of a refactoring history.
 *
 * @since 3.2
 */
public final class RefactoringHistoryImplementation extends RefactoringHistory {

	/**
	 * Returns a hash code value for the array
	 *
	 * @param array
	 *            the array to create a hash code value for
	 * @return a hash code value for the array
	 */
	private static int hashCode(final Object[] array) {
		if (array == null)
			return 0;
		int result= 1;
		for (Object a : array) {
			result= 31 * result + (a == null ? 0 : a.hashCode());
		}
		return result;
	}

	/** The refactoring descriptor proxies */
	private final RefactoringDescriptorProxy[] fDescriptorProxies;

	/** Is the refactoring history already sorted? */
	private boolean fSorted= false;

	/**
	 * Creates a new refactoring history implementation.
	 *
	 * @param proxies
	 *            the refactoring descriptor proxies
	 */
	public RefactoringHistoryImplementation(final RefactoringDescriptorProxy[] proxies) {
		Assert.isNotNull(proxies);
		fDescriptorProxies= new RefactoringDescriptorProxy[proxies.length];
		System.arraycopy(proxies, 0, fDescriptorProxies, 0, proxies.length);
	}

	@Override
	public boolean equals(final Object object) {
		if (this == object)
			return true;
		if (object == null)
			return false;
		if (getClass() != object.getClass())
			return false;
		final RefactoringHistoryImplementation other= (RefactoringHistoryImplementation) object;
		if (!Arrays.equals(getDescriptors(), other.getDescriptors()))
			return false;
		return true;
	}

	/**
	 * Returns the descriptor proxies, in no particular order.
	 *
	 * @return the descriptor proxies
	 */
	RefactoringDescriptorProxy[] getDescriptorProxies() {
		return fDescriptorProxies;
	}

	@Override
	public RefactoringDescriptorProxy[] getDescriptors() {
		if (!fSorted && fDescriptorProxies.length > 1)
			RefactoringHistoryManager.sortRefactoringDescriptorsDescending(fDescriptorProxies);
		fSorted= true;
		final RefactoringDescriptorProxy[] proxies= new RefactoringDescriptorProxy[fDescriptorProxies.length];
		System.arraycopy(fDescriptorProxies, 0, proxies, 0, fDescriptorProxies.length);
		return proxies;
	}

	@Override
	public int hashCode() {
		return 31 * RefactoringHistoryImplementation.hashCode(getDescriptors());
	}

	@Override
	public boolean isEmpty() {
		return fDescriptorProxies.length == 0;
	}

	@Override
	public RefactoringHistory removeAll(final RefactoringHistory history) {
		final Set<RefactoringDescriptorProxy> existing= new LinkedHashSet<>(Arrays.asList(fDescriptorProxies));
		final Set<RefactoringDescriptorProxy> other= new HashSet<>(Arrays.asList(history.getDescriptors()));
		existing.removeAll(other);
		final RefactoringDescriptorProxy[] proxies= new RefactoringDescriptorProxy[existing.size()];
		existing.toArray(proxies);
		return new RefactoringHistoryImplementation(proxies);
	}

	@Override
	public String toString() {
		final StringBuilder buffer= new StringBuilder(256);
		buffer.append(getClass().getName());
		buffer.append("[descriptors="); //$NON-NLS-1$
		buffer.append(Arrays.toString(getDescriptors()));
		buffer.append(']');
		return buffer.toString();
	}
}