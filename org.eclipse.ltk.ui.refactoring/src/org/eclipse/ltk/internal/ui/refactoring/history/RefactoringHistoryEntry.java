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
package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

/**
 * Refactoring entry of a refactoring history.
 * 
 * @since 3.2
 */
public final class RefactoringHistoryEntry extends RefactoringHistoryNode {

	/** The refactoring descriptor proxy */
	private final RefactoringDescriptorProxy fRefactoringDescriptor;

	/**
	 * Creates a new refactoring history entry.
	 * 
	 * @param parent
	 *            the parent node
	 * @param proxy
	 *            the refactoring descriptor proxy
	 */
	public RefactoringHistoryEntry(final RefactoringHistoryNode parent, final RefactoringDescriptorProxy proxy) {
		super(parent, RefactoringHistoryNode.ENTRY);
		fRefactoringDescriptor= proxy;
	}

	/**
	 * Returns the refactoring descriptor.
	 * 
	 * @return the refactoring descriptor
	 */
	public RefactoringDescriptorProxy getDescriptor() {
		return fRefactoringDescriptor;
	}
}
