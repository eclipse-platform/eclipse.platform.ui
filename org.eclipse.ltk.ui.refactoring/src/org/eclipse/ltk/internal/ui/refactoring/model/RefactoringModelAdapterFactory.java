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

import org.eclipse.team.ui.mapping.IResourceMappingMerger;

import org.eclipse.core.runtime.IAdapterFactory;

import org.eclipse.core.resources.mapping.ModelProvider;

import org.eclipse.ltk.core.refactoring.model.AbstractRefactoringModelProvider;

/**
 * Adaptor factory for the resource mapping merger.
 * 
 * @since 3.2
 */
public final class RefactoringModelAdapterFactory implements IAdapterFactory {

	/**
	 * {@inheritDoc}
	 */
	public Object getAdapter(final Object adaptable, final Class adapter) {
		if (adaptable instanceof AbstractRefactoringModelProvider && adapter == IResourceMappingMerger.class)
			return new RefactoringModelMerger((ModelProvider) adaptable);
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Class[] getAdapterList() {
		return new Class[] { IResourceMappingMerger.class};
	}
}
