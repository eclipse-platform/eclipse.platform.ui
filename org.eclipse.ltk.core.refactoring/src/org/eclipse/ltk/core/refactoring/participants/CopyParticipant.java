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
 * A participant to participate in refactorings that copy elements. A copy
 * participant can't assume that its associated refactoring processor is
 * a copy processor. A copy operation might be a side effect of another
 * refactoring operation.  
 * <p>
 * Copy participants are registered via the extension point <code>
 * org.eclipse.ltk.core.refactoring.copyParticipants</code>. Extensions to
 * this extension point must therefore extend this abstract class.
 * </p>
 * 
 * @since 3.0
 */
public abstract class CopyParticipant extends RefactoringParticipant {

	private CopyArguments fArguments;

	/**
	 * {@inheritDoc}
	 */
	protected final void initialize(RefactoringArguments arguments) {
		fArguments= (CopyArguments)arguments;
	}
	
	/**
	 * Returns the copy arguments.
	 * 
	 * @return the copy arguments
	 */
	public CopyArguments getArguments() {
		return fArguments;
	}
}
