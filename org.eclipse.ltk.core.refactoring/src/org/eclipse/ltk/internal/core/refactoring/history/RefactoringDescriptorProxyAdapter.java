/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.history;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

/**
 * Adapter class which adapts refactoring descriptors to refactoring descriptor
 * proxies.
 *
 * @since 3.2
 */
public final class RefactoringDescriptorProxyAdapter extends RefactoringDescriptorProxy {

	/** The encapsulated descriptor */
	private final RefactoringDescriptor fDescriptor;

	/**
	 * Creates a new refactoring descriptor proxy adapter.
	 *
	 * @param descriptor
	 *            the descriptor to encapsulate
	 */
	public RefactoringDescriptorProxyAdapter(final RefactoringDescriptor descriptor) {
		Assert.isNotNull(descriptor);
		fDescriptor= descriptor;
	}

	@Override
	public String getDescription() {
		return fDescriptor.getDescription();
	}

	@Override
	public String getProject() {
		return fDescriptor.getProject();
	}

	@Override
	public long getTimeStamp() {
		return fDescriptor.getTimeStamp();
	}

	@Override
	public RefactoringDescriptor requestDescriptor(final IProgressMonitor monitor) {
		return fDescriptor;
	}

	@Override
	public String toString() {
		return fDescriptor.toString();
	}
}