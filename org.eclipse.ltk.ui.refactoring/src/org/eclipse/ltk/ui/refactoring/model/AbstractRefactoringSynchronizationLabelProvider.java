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
package org.eclipse.ltk.ui.refactoring.model;

import org.eclipse.team.core.diff.IDiffNode;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.ui.mapping.SynchronizationLabelProvider;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

import org.eclipse.ltk.internal.ui.refactoring.model.RefactoringDescriptorDiffNode;

/**
 * Partial implementation of a refactoring-aware synchronization label provider.
 * <p>
 * Note: this class is intended to be extended by clients who need refactoring
 * support in a synchronization label provider.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @see org.eclipse.team.ui.mapping.SynchronizationLabelProvider
 * 
 * @since 3.2
 */
public abstract class AbstractRefactoringSynchronizationLabelProvider extends SynchronizationLabelProvider {

	/**
	 * {@inheritDoc}
	 */
	protected String decorateText(final String base, final Object element) {
		if (element instanceof RefactoringDescriptorProxy)
			return base;
		return super.decorateText(base, element);
	}

	/**
	 * {@inheritDoc}
	 */
	protected IDiffNode getDiff(final Object element) {
		if (element instanceof RefactoringDescriptorProxy)
			return new RefactoringDescriptorDiffNode((RefactoringDescriptorProxy) element, getKind((RefactoringDescriptorProxy) element), getDirection((RefactoringDescriptorProxy) element));
		return super.getDiff(element);
	}

	/**
	 * Returns the direction of the difference of the specified refactoring
	 * descriptor proxy.
	 * <p>
	 * The result of this method is used to compose an icon which reflects the
	 * direction of the difference between the two or three versions of the
	 * refactoring descriptor.
	 * </p>
	 * 
	 * @param proxy
	 *            the refactoring descriptor proxy
	 * @return the direction of the difference
	 * 
	 * @see IThreeWayDiff#getDirection()
	 */
	protected abstract int getDirection(RefactoringDescriptorProxy proxy);

	/**
	 * Returns the kind of difference between the three sides ancestor, left and
	 * right of the specified refactoring descriptor proxy.
	 * <p>
	 * The result of this method is used to compose an icon which reflects the
	 * kind of difference between the two or three versions of the refactoring
	 * descriptor.
	 * </p>
	 * 
	 * @param proxy
	 *            the refactoring descriptor proxy
	 * @return the kind of difference
	 * 
	 * @see IDiffNode#getKind()
	 */
	protected abstract int getKind(RefactoringDescriptorProxy proxy);
}