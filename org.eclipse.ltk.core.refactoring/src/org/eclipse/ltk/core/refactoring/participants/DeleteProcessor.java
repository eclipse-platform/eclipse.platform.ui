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

public abstract class DeleteProcessor extends RefactoringProcessor {

	private SharableParticipants fSharedParticipants= new SharableParticipants();
	
	public abstract DeleteParticipant[] loadElementParticipants() throws CoreException;
	
	public void setArgumentsTo(DeleteParticipant participant) throws CoreException {
		participant.setArguments(getArguments());
	}
	
	protected DeleteArguments getArguments() {
		return new DeleteArguments();
	}
	
	protected SharableParticipants getSharedParticipants() {
		return fSharedParticipants;
	}
}
