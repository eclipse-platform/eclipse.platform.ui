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

package org.eclipse.ui.internal.csm.activities;

import org.eclipse.ui.internal.csm.activities.api.IPatternBinding;

final class ActivityPatternBinding implements IPatternBinding {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ActivityPatternBinding.class.getName().hashCode();

	private String pattern;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	
	ActivityPatternBinding(String pattern) {	
		if (pattern == null)
			throw new NullPointerException();

		this.pattern = pattern;
	}

	public int compareTo(Object object) {
		ActivityPatternBinding activityBinding = (ActivityPatternBinding) object;
		int compareTo = pattern.compareTo(activityBinding.pattern);			
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ActivityPatternBinding))
			return false;

		ActivityPatternBinding activityBinding = (ActivityPatternBinding) object;	
		boolean equals = true;
		equals &= pattern.equals(activityBinding.pattern);
		return equals;
	}

	public String getPattern() {
		return pattern;
	}
	
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + pattern.hashCode();
			hashCodeComputed = true;
		}
			
		return hashCode;		
	}

	public String toString() {
		return pattern;		
	}
}
