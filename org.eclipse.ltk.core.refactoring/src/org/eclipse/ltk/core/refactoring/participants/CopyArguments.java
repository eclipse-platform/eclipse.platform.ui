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

import org.eclipse.ltk.internal.core.refactoring.Assert;

/**
 * Copy arguments describe the data that a processor
 * provides to its copy participants.
 * 
 * @since 3.0
 */
public class CopyArguments extends RefactoringArguments {
	
	private Object fDestination;
	
	/**
	 * Creates new copy arguments.
	 * 
	 * @param destination the destination of the copy operation
	 */
	public CopyArguments(Object destination) {
		Assert.isNotNull(destination);
		fDestination= destination;
	}
	
	/**
	 * Returns the destination of the copy operation
	 * 
	 * @return the copy's destination
	 */
	public Object getDestination() {
		return fDestination;
	}
}
