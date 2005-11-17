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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

import org.eclipse.ltk.internal.core.refactoring.Assert;

/**
 * Factory class to create refactoring instances from refactoring descriptors.
 * 
 * @since 3.2
 */
public final class RefactoringInstanceFactory implements IRefactoringInstanceCreator {

	/** The singleton instance */
	private static RefactoringInstanceFactory fInstance= null;

	/**
	 * Returns the singleton instance of the refactoring instance factory.
	 * 
	 * @return the singleton instance
	 */
	public static RefactoringInstanceFactory getInstance() {
		if (fInstance == null)
			fInstance= new RefactoringInstanceFactory();
		return fInstance;
	}

	/**
	 * The creator registry (element type: &lt;String,
	 * IRefactoringInstanceCreator&gt;)
	 */
	private Map fCreatorRegistry= new HashMap();

	/**
	 * Creates a new refactoring instance factory.
	 */
	private RefactoringInstanceFactory() {
		// Not instantiatable
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringArguments createArguments(final RefactoringDescriptor descriptor) {
		Assert.isNotNull(descriptor);
		final String id= descriptor.getID();
		if (id != null) {
			final IRefactoringInstanceCreator creator= (IRefactoringInstanceCreator) fCreatorRegistry.get(id);
			if (creator != null)
				return creator.createArguments(descriptor);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Refactoring createRefactoring(final RefactoringDescriptor descriptor) throws CoreException {
		Assert.isNotNull(descriptor);
		final String id= descriptor.getID();
		if (id != null) {
			final IRefactoringInstanceCreator creator= (IRefactoringInstanceCreator) fCreatorRegistry.get(id);
			if (creator != null)
				return creator.createRefactoring(descriptor);
		}
		return null;
	}

	/**
	 * Registers the specified refactoring instance creator with the specified
	 * refactoring id.
	 * 
	 * @param id
	 *            the refactoring id
	 * @param creator
	 *            the refactoring instance creator
	 */
	public void registerCreator(final String id, final IRefactoringInstanceCreator creator) {
		Assert.isNotNull(id);
		Assert.isTrue(!"".equals(id)); //$NON-NLS-1$
		Assert.isNotNull(creator);
		fCreatorRegistry.put(id, creator);
	}

	/**
	 * Unregisters the refactoring instance creator registered with the
	 * specified id.
	 * 
	 * @param id
	 *            the refactoring id
	 */
	public void unregisterCreator(final String id) {
		Assert.isNotNull(id);
		Assert.isTrue(!"".equals(id)); //$NON-NLS-1$
		fCreatorRegistry.remove(id);
	}
}