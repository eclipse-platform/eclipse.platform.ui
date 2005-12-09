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
package org.eclipse.ltk.internal.ui.refactoring.model;

import org.eclipse.core.runtime.IAdapterFactory;

import org.eclipse.core.resources.mapping.ResourceMapping;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.core.refactoring.model.RefactoringDescriptorResourceMapping;
import org.eclipse.ltk.core.refactoring.model.RefactoringHistoryResourceMapping;

/**
 * Adapter factory for refactoring history objects.
 * 
 * @since 3.2
 */
public final class RefactoringHistoryAdapterFactory implements IAdapterFactory {

	private static Class[] ADAPTERS= new Class[] { ResourceMapping.class};

	/**
	 * {@inheritDoc}
	 */
	public Object getAdapter(final Object adaptable, final Class adapter) {
		if (ResourceMapping.class.equals(adapter)) {
			if (adaptable instanceof RefactoringHistory)
				return new RefactoringHistoryResourceMapping((RefactoringHistory) adaptable);
			else if (adaptable instanceof RefactoringDescriptorProxy)
				return new RefactoringDescriptorResourceMapping((RefactoringDescriptorProxy) adaptable);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Class[] getAdapterList() {
		return ADAPTERS;
	}
}