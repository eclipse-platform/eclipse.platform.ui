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

/**
 * TODO Write Java doc
 * @since 3.0
 */
public abstract class DeleteParticipant extends RefactoringParticipant {

	private DeleteArguments fArguments;
	
	/**
	 * {@inheritDoc}
	 */
	protected void initialize(RefactoringArguments arguments) {
		fArguments= (DeleteArguments)arguments;
	}
	
	/**
	 * Returns the delete arguments.
	 * 
	 * @return the delete arguments
	 */
	public DeleteArguments getArguments() {
		return fArguments;
	}
}
