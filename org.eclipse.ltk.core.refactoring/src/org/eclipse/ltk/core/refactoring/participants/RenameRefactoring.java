/*******************************************************************************
 * Copyright (c) 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.internal.core.refactoring.Assert;

/**
 * A generic rename refactoring. The actual refactoring is done
 * by the rename processor passed to the constructor.
 * 
 * @since 3.0
 */
public class RenameRefactoring extends ProcessorBasedRefactoring {

	private RenameProcessor fProcessor;
	private RenameParticipant[] fElementParticipants;
	
	/**
	 * Creates a new rename refactoring with the given rename processor.
	 * 
	 * @param processor the rename processor
	 */
	public RenameRefactoring(RenameProcessor processor) {
		Assert.isNotNull(processor);
		fProcessor= processor;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public RefactoringProcessor getProcessor() {
		return fProcessor;
	}

	/**
	 * {@inheritDoc}
	 */
	protected RefactoringParticipant[] getElementParticipants(boolean setArguments) throws CoreException {
		if (fElementParticipants == null)
			fElementParticipants= fProcessor.loadElementParticipants();
		if (setArguments) {
			for (int i= 0; i < fElementParticipants.length; i++) {
				fProcessor.setArgumentsTo(fElementParticipants[i]);
			}
		}
		RefactoringParticipant[]result= new RefactoringParticipant[fElementParticipants.length];
		for (int i= 0; i < result.length; i++) {
			result[i]= fElementParticipants[i];
		}
		return result;
	}
}
