/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.activities;

import java.util.Set;


/**
 * 
 * <em>EXPERIMENTAL</em>
 * @since 3.1
 */
public class BasicTriggerPointAdvisor implements ITriggerPointAdvisor {

	/**
	 * Create a new instance of this class.
	 */
	public BasicTriggerPointAdvisor() {
		super();
	}
	
	/**
	 * Default implementation allows all activation.
	 */
	public Set allow(ITriggerPoint triggerPoint, IIdentifier identifier) {
		return identifier.getActivityIds();
	}
}
