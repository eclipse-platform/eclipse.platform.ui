/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.csm.commands;

import org.eclipse.ui.commands.IContextBinding;

final class ActivityBinding implements IContextBinding {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ActivityBinding.class.getName().hashCode();

	private String contextId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	
	ActivityBinding(String contextId) {	
		if (contextId == null)
			throw new NullPointerException();

		this.contextId = contextId;
	}

	public int compareTo(Object object) {
		ActivityBinding contextBinding = (ActivityBinding) object;
		int compareTo = contextId.compareTo(contextBinding.contextId);			
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ActivityBinding))
			return false;

		ActivityBinding contextBinding = (ActivityBinding) object;	
		boolean equals = true;
		equals &= contextId.equals(contextBinding.contextId);
		return equals;
	}

	public String getContextId() {
		return contextId;
	}
	
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + contextId.hashCode();
			hashCodeComputed = true;
		}
			
		return hashCode;		
	}

	public String toString() {
		return contextId;		
	}
}
