/*******************************************************************************
 * Copyright (c) 2005 Tobias Widmer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Tobias Widmer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.history;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.ltk.core.refactoring.participants.GenericRefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

import org.eclipse.ltk.internal.core.refactoring.Assert;

/**
 * Partial implementation of a refactoring instance creator.
 * 
 * @since 3.2
 */
public abstract class RefactoringInstanceCreator implements IRefactoringInstanceCreator {

	/**
	 * {@inheritDoc}
	 */
	public RefactoringArguments createArguments(final RefactoringDescriptor descriptor) {
		Assert.isNotNull(descriptor);
		final GenericRefactoringArguments arguments= new GenericRefactoringArguments();
		final Map map= descriptor.getArguments();
		for (final Iterator iterator= map.keySet().iterator(); iterator.hasNext();) {
			final String name= (String) iterator.next();
			final String value= (String) map.get(name);
			if (name != null && !"".equals(name) && value != null) //$NON-NLS-1$
				arguments.setAttribute(name, value);
		}
		return arguments;
	}
}