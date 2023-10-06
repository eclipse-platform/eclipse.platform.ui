/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

/**
 * Refactoring entry of a refactoring history.
 *
 * @since 3.2
 */
public final class RefactoringHistoryEntry extends RefactoringHistoryNode {

	/** The refactoring descriptor proxy */
	private final RefactoringDescriptorProxy fDescriptorProxy;

	/** The parent node, or <code>null</code> */
	private final RefactoringHistoryNode fParent;

	/**
	 * Creates a new refactoring history entry.
	 *
	 * @param parent
	 *            the parent node, or <code>null</code>
	 * @param proxy
	 *            the refactoring descriptor proxy
	 */
	public RefactoringHistoryEntry(final RefactoringHistoryNode parent, final RefactoringDescriptorProxy proxy) {
		fParent= parent;
		fDescriptorProxy= proxy;
	}

	@Override
	public boolean equals(final Object object) {
		if (object instanceof RefactoringHistoryEntry) {
			final RefactoringHistoryEntry entry= (RefactoringHistoryEntry) object;
			return getDescriptor().equals(entry.getDescriptor());
		}
		return false;
	}

	/**
	 * Returns the refactoring descriptor.
	 *
	 * @return the refactoring descriptor
	 */
	public RefactoringDescriptorProxy getDescriptor() {
		return fDescriptorProxy;
	}

	@Override
	public int getKind() {
		return RefactoringHistoryNode.ENTRY;
	}

	@Override
	public RefactoringHistoryNode getParent() {
		return fParent;
	}

	@Override
	public int hashCode() {
		return getDescriptor().hashCode();
	}
}
