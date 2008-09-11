/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.model;

import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.ui.mapping.SynchronizationCompareAdapter;

import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.internal.ui.refactoring.model.RefactoringDescriptorCompareInput;
import org.eclipse.ltk.internal.ui.refactoring.model.RefactoringDescriptorSynchronizationProxy;

/**
 * Partial implementation of a refactoring-aware synchronization compare adapter.
 * <p>
 * This class provides compare support for the refactoring history objects
 * associated with a refactoring model provider.
 * </p>
 * <p>
 * Note: this class is designed to be extended by clients. Programming language
 * implementers which need a refactoring-aware synchronization compare adapter to contribute to
 * team synchronization views may extend this class to provide specific compare
 * inputs for their model elements.
 * </p>
 *
 * @see SynchronizationCompareAdapter
 *
 * @since 3.2
 */
public abstract class AbstractSynchronizationCompareAdapter extends SynchronizationCompareAdapter {

	/**
	 * {@inheritDoc}
	 */
	public ICompareInput asCompareInput(final ISynchronizationContext context, final Object element) {
		if (element instanceof RefactoringDescriptorProxy)
			return new RefactoringDescriptorCompareInput((RefactoringDescriptorProxy) element, getKind(context, (RefactoringDescriptorProxy) element));
		return super.asCompareInput(context, element);
	}

	/**
	 * Returns the kind of difference between the three sides ancestor, left and
	 * right of the specified refactoring descriptor proxy.
	 * <p>
	 * The result of this method is used to compose an icon which reflects the
	 * kind of difference between the two or three versions of the refactoring
	 * descriptor.
	 * </p>
	 *
	 * @param context
	 *            the synchronization context
	 * @param proxy
	 *            the refactoring descriptor proxy
	 * @return the kind of difference
	 *
	 * @see ICompareInput#getKind()
	 */
	protected int getKind(final ISynchronizationContext context, final RefactoringDescriptorProxy proxy) {
		int kind= Differencer.ADDITION;
		if (proxy instanceof RefactoringDescriptorSynchronizationProxy) {
			final RefactoringDescriptorSynchronizationProxy extended= (RefactoringDescriptorSynchronizationProxy) proxy;
			final int direction= extended.getDirection();
			if (direction == IThreeWayDiff.OUTGOING)
				kind|= Differencer.LEFT;
			else if (direction == IThreeWayDiff.INCOMING)
				kind|= Differencer.RIGHT;
		}
		return kind;
	}
}
