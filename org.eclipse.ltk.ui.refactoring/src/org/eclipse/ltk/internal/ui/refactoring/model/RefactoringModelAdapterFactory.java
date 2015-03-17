/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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

/**
 * Adaptor factory for refactoring model support.
 *
 * @since 3.2
 */
public final class RefactoringModelAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptable, Class<T> adapter) {
		if (adaptable instanceof RefactoringDescriptorCompareInput && ResourceMapping.class.equals(adapter)) {
			final RefactoringDescriptorCompareInput input= (RefactoringDescriptorCompareInput) adaptable;
			final RefactoringDescriptorProxy descriptor= input.getDescriptor();
			if (descriptor != null) {
				return (T) descriptor.getAdapter(ResourceMapping.class);
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { ResourceMapping.class};
	}
}
