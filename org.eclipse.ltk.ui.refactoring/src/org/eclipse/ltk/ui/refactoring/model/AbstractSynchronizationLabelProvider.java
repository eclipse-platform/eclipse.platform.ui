/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.model;

import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.ui.mapping.SynchronizationLabelProvider;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.ui.refactoring.model.RefactoringDescriptorDiff;
import org.eclipse.ltk.internal.ui.refactoring.model.RefactoringDescriptorSynchronizationProxy;
import org.eclipse.ltk.internal.ui.refactoring.model.RefactoringHistoryDiff;

/**
 * Partial implementation of a refactoring-aware synchronization label provider.
 * <p>
 * This class overrides several methods from
 * {@link SynchronizationLabelProvider} to customize the rendering of
 * refactoring history objects in team synchronization views.
 * </p>
 * <p>
 * Note: this class is designed to be extended by clients. Programming language
 * implementers who need refactoring support in a synchronization label provider
 * used in team synchronization views may use this class as a basis for
 * refactoring-aware synchronization label providers.
 * </p>
 *
 * @see org.eclipse.team.ui.mapping.SynchronizationLabelProvider
 *
 * @since 3.2
 */
public abstract class AbstractSynchronizationLabelProvider extends SynchronizationLabelProvider {

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
	protected IDiff getDiff(final Object element) {
		if (element instanceof RefactoringDescriptorProxy)
			return new RefactoringDescriptorDiff((RefactoringDescriptorProxy) element, getKind(element), getDirection(element));
		else if (element instanceof RefactoringHistory)
			return new RefactoringHistoryDiff((RefactoringHistory) element, getKind(element), getDirection(element));
		return super.getDiff(element);
	}

	/**
	 * Returns the direction of the difference of the specified refactoring
	 * history object.
	 * <p>
	 * The result of this method is used to compose an icon which reflects the
	 * direction of the difference between the two or three versions of the
	 * refactoring history object.
	 * </p>
	 *
	 * @param element
	 *            the refactoring history object
	 * @return the direction of the difference
	 *
	 * @see IThreeWayDiff#getDirection()
	 */
	protected int getDirection(Object element) {
		if (element instanceof RefactoringHistory) {
			final RefactoringHistory history= (RefactoringHistory) element;
			final RefactoringDescriptorProxy[] descriptors= history.getDescriptors();
			int direction= 0;
			if (descriptors.length > 0) {
				if (descriptors[0] instanceof RefactoringDescriptorSynchronizationProxy) {
					final RefactoringDescriptorSynchronizationProxy proxy= (RefactoringDescriptorSynchronizationProxy) descriptors[0];
					direction= proxy.getDirection();
				}
			}
			for (int index= 1; index < descriptors.length; index++) {
				if (descriptors[index] instanceof RefactoringDescriptorSynchronizationProxy) {
					final RefactoringDescriptorSynchronizationProxy proxy= (RefactoringDescriptorSynchronizationProxy) descriptors[index];
					if (proxy.getDirection() != direction)
						return IThreeWayDiff.CONFLICTING;
				}
			}
			return IDiff.NO_CHANGE;
		} else if (element instanceof RefactoringDescriptorSynchronizationProxy) {
			final RefactoringDescriptorSynchronizationProxy proxy= (RefactoringDescriptorSynchronizationProxy) element;
			return proxy.getDirection();
		}
		return IThreeWayDiff.CONFLICTING;
	}

	/**
	 * Returns the kind of difference between the three sides ancestor, left and
	 * right of the specified refactoring history object.
	 * <p>
	 * The result of this method is used to compose an icon which reflects the
	 * kind of difference between the two or three versions of the refactoring
	 * history object.
	 * </p>
	 *
	 * @param element
	 *            the refactoring history object
	 * @return the kind of difference
	 *
	 * @see IDiff#getKind()
	 */
	protected int getKind(Object element) {
		if (element instanceof RefactoringHistory)
			return IDiff.CHANGE;
		return IDiff.ADD;
	}
}
