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
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.ltk.core.refactoring.participants.GenericRefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

import org.eclipse.ltk.internal.core.refactoring.Assert;

/**
 * Factory class to create refactoring components from refactoring descriptors.
 * 
 * @since 3.2
 */
public final class RefactoringComponentFactory {

	/** The singleton instance */
	private static RefactoringComponentFactory fInstance= null;

	/**
	 * Returns the singleton instance of the refactoring component factory.
	 * 
	 * @return the singleton instance
	 */
	public static RefactoringComponentFactory getInstance() {
		if (fInstance == null)
			fInstance= new RefactoringComponentFactory();
		return fInstance;
	}

	/**
	 * The refactoring map (element type:
	 * <code>&lt;String, Refactoring&gt;</code>)
	 * <p>
	 * TODO: replace by more general mechanism like extension point
	 * </p>
	 */
	private final Map fRefactoringMap= new HashMap();

	/**
	 * Creates a new refactoring component factory.
	 */
	private RefactoringComponentFactory() {
		// Not instantiatable
	}

	/**
	 * Creates the refactoring arguments for the specified refactoring
	 * descriptor.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor
	 * @return the refactoring arguments
	 */
	public RefactoringArguments createArguments(final RefactoringDescriptor descriptor) {
		Assert.isNotNull(descriptor);
		final GenericRefactoringArguments arguments= new GenericRefactoringArguments();
		final Map map= descriptor.getArguments();
		for (final Iterator iterator= map.keySet().iterator(); iterator.hasNext();) {
			final String name= (String) iterator.next();
			final String value= (String) map.get(name);
			if (value != null)
				arguments.setAttribute(name, value);
		}
		return arguments;
	}

	/**
	 * Creates the refactoring for the specified refactoring descriptor.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor
	 * @return the refactoring, or <code>null</code>
	 */
	public Refactoring createRefactoring(final RefactoringDescriptor descriptor) {
		Assert.isNotNull(descriptor);

		// TODO: implement

		return (Refactoring) fRefactoringMap.get(descriptor.getID());
	}

	/**
	 * This method is internally used.
	 * <p>
	 * TODO: replace by more general mechanism like extension point
	 * </p>
	 * 
	 * @param id
	 *            the id if the refactoring
	 * @param refactoring
	 *            the refactoring
	 */
	public void register(String id, Refactoring refactoring) {
		fRefactoringMap.put(id, refactoring);
	}
}
