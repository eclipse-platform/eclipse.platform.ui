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
 * Copy arguments describes the data that a processor
 * provides to its copy participants.
 * 
 * @since 3.0
 */
public class CopyArguments {
	
	private Object fTarget;
	
	/**
	 * Creates new copy arguments.
	 * 
	 * @param target the target location of the copy operation
	 */
	public CopyArguments(Object target) {
		Assert.isNotNull(target);
		fTarget= target;
	}
	
	/**
	 * Returns the target location of the copy operation
	 * 
	 * @return the copy's target location
	 */
	public Object getTargetLocation() {
		return fTarget;
	}
}
