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

import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.ui.mapping.AbstractCompareAdapter;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

import org.eclipse.ltk.internal.ui.refactoring.model.RefactoringDescriptorCompareInput;
import org.eclipse.ltk.internal.ui.refactoring.model.RefactoringDescriptorCompareViewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.Viewer;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.structuremergeviewer.ICompareInput;

/**
 * Partial implementation of a refactoring-aware compare adapter.
 * <p>
 * The refactoring compare adapter provides compare support for the refactoring
 * history objects associated with the refactoring model provider.
 * </p>
 * <p>
 * Note: this class is intended to be extended by clients who need refactoring
 * support in a synchronization viewer.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @see AbstractCompareAdapter
 * 
 * @since 3.2
 */
public abstract class AbstractRefactoringCompareAdapter extends AbstractCompareAdapter {

	/**
	 * {@inheritDoc}
	 */
	public ICompareInput asCompareInput(final ISynchronizationContext context, final Object element) {
		if (element instanceof RefactoringDescriptorProxy)
			return new RefactoringDescriptorCompareInput((RefactoringDescriptorProxy) element, getKind(context, (RefactoringDescriptorProxy) element));
		return super.asCompareInput(context, element);
	}

	/**
	 * {@inheritDoc}
	 */
	public Viewer findContentViewer(final Composite parent, final Viewer viewer, final ICompareInput input, final CompareConfiguration configuration) {
		if (input instanceof RefactoringDescriptorCompareInput)
			return new RefactoringDescriptorCompareViewer(parent, SWT.NONE);
		return super.findContentViewer(parent, viewer, input, configuration);
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
	protected abstract int getKind(ISynchronizationContext context, RefactoringDescriptorProxy proxy);
}