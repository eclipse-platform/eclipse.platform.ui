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

package org.eclipse.ui.internal.csm.roles;

import org.eclipse.ui.internal.csm.roles.api.IActivityBinding;

final class ActivityBinding implements IActivityBinding {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ActivityBinding.class.getName().hashCode();

	private String activityId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	
	ActivityBinding(String activityId) {	
		if (activityId == null)
			throw new NullPointerException();

		this.activityId = activityId;
	}

	public int compareTo(Object object) {
		ActivityBinding activityBinding = (ActivityBinding) object;
		int compareTo = activityId.compareTo(activityBinding.activityId);			
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ActivityBinding))
			return false;

		ActivityBinding activityBinding = (ActivityBinding) object;	
		boolean equals = true;
		equals &= activityId.equals(activityBinding.activityId);
		return equals;
	}

	public String getActivityId() {
		return activityId;
	}
	
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + activityId.hashCode();
			hashCodeComputed = true;
		}
			
		return hashCode;		
	}

	public String toString() {
		return activityId;		
	}
}
