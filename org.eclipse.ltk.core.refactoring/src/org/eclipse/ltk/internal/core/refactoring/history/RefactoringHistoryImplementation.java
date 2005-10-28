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

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorHandle;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.core.refactoring.Assert;

/**
 * Default implementation of a refactoring history.
 * 
 * @since 3.2
 */
public final class RefactoringHistoryImplementation extends RefactoringHistory {

	/** The refactoring descriptor handles */
	private final RefactoringDescriptorHandle[] fHandles;

	/**
	 * Creates a new refactoring history implementation.
	 * 
	 * @param handles
	 *            the refactoring descriptor handles
	 */
	public RefactoringHistoryImplementation(final RefactoringDescriptorHandle[] handles) {
		Assert.isNotNull(handles);
		fHandles= handles;
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringDescriptorHandle[] getDescriptors() {
		final RefactoringDescriptorHandle[] handles= new RefactoringDescriptorHandle[fHandles.length];
		System.arraycopy(fHandles, 0, handles, 0, handles.length);
		Arrays.sort(handles, new Comparator() {

			public final int compare(final Object first, final Object second) {
				final RefactoringDescriptorHandle predecessor= (RefactoringDescriptorHandle) first;
				final RefactoringDescriptorHandle successor= (RefactoringDescriptorHandle) second;
				return (int) (successor.getTimeStamp() - predecessor.getTimeStamp());
			}
		});
		return handles;
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean isEmpty() {
		return fHandles.length == 0;
	}
}