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

import org.eclipse.ltk.internal.core.refactoring.Assert;

public abstract class RenameParticipant extends RefactoringParticipant {

	private RenameArguments fArguments;
	
	public void setArguments(RenameArguments arguments) {
		Assert.isNotNull(arguments);
		fArguments= arguments;
	}
	
	public RenameArguments getArguments() {
		return fArguments;
	}
}
