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

public abstract class CopyParticipant extends RefactoringParticipant {

	private CopyArguments fArguments;
	
	/**
	 * Sets the copy arguments as provided by the corresponding
	 * refactoring processor.
	 * 
	 * @param arguments the copy arguments
	 */
	public void setArguments(CopyArguments arguments) {
		Assert.isNotNull(arguments);
		fArguments= arguments;
	}

	/**
	 * Returns the copy arguments or <code>null</code> if the arguments
	 * haven't been initialized yet.
	 * 
	 * @return the copy arguments or <code>null</code>
	 */
	public CopyArguments getArguments() {
		return fArguments;
	}
}
