/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * A special composite change used by a processor based refactoring
 * to track conflicts between changes provided by the processor and
 * participants.
 * <p>
 * This class is not intended to be used by normal clients.
 * </p>
 * @since 3.0 
 */
public class ProcessorChange extends CompositeChange {

	private List fSkippedChanges;
	
	/**
	 * Creates a new processor change
	 * 
	 * @param name the human reable name of the change
	 */
	public ProcessorChange(String name) {
		super(name);
		fSkippedChanges= new ArrayList();
	}
	
	/**
	 * Returns the list of changes that got skipped during change
	 * execution since their state wasn't valid anymore.
	 * 
	 * @return the list of skipped changes
	 */
	public Change[] getSkippedChanges() {
		return (Change[])fSkippedChanges.toArray(new Change[fSkippedChanges.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	protected boolean canPerformChange(Change child, IProgressMonitor pm) {
		try {
			RefactoringStatus status= child.isValid(pm);
			if (status.hasFatalError()) {
				fSkippedChanges.add(child);
				return false;
			} else {
				return true;
			}
		} catch (CoreException e) {
			fSkippedChanges.add(child);
			return false;
		}
	}
}
