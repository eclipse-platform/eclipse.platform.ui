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

import org.eclipse.ui.activities.IPatternBinding;
import org.eclipse.ui.internal.util.Util;

final class PatternBinding implements IPatternBinding {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = PatternBinding.class.getName().hashCode();

	private boolean inclusive;
	private String pattern;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	
	PatternBinding(boolean inclusive, String pattern) {	
		if (pattern == null)
			throw new NullPointerException();

		this.inclusive = inclusive;
		this.pattern = pattern;
	}

	public int compareTo(Object object) {
		PatternBinding patternBinding = (PatternBinding) object;
		int compareTo = Util.compare(inclusive, patternBinding.inclusive);			
		
		if (compareTo == 0)			
			compareTo = pattern.compareTo(patternBinding.pattern);
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof PatternBinding))
			return false;

		PatternBinding patternBinding = (PatternBinding) object;	
		boolean equals = true;
		equals &= inclusive == patternBinding.inclusive;
		equals &= pattern.equals(patternBinding.pattern);
		return equals;
	}

	public String getPattern() {
		return pattern;
	}

	public boolean isInclusive() {
		return inclusive;
	}
	
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(inclusive);
			hashCode = hashCode * HASH_FACTOR + pattern.hashCode();
			hashCodeComputed = true;
		}
			
		return hashCode;		
	}

	public String toString() {
		return pattern;		
	}
}
