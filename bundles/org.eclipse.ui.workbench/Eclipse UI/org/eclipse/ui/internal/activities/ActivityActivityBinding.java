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

package org.eclipse.ui.internal.activities;

import org.eclipse.ui.activities.IActivityActivityBinding;

import org.eclipse.ui.internal.util.Util;

public final class ActivityActivityBinding implements IActivityActivityBinding {
	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL =
		ActivityActivityBinding.class.getName().hashCode();
	private String childActivityId;
	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private String parentActivityId;
	private transient String string;

	public ActivityActivityBinding(String childActivityId, String parentActivityId) {
		if (childActivityId == null || parentActivityId == null)
			throw new NullPointerException();

		this.childActivityId = childActivityId;
		this.parentActivityId = parentActivityId;
	}

	public int compareTo(Object object) {
		ActivityActivityBinding castedObject = (ActivityActivityBinding) object;
		int compareTo =
			Util.compare(childActivityId, castedObject.childActivityId);

		if (compareTo == 0)
			compareTo =
				Util.compare(parentActivityId, castedObject.parentActivityId);

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof ActivityActivityBinding))
			return false;

		ActivityActivityBinding castedObject = (ActivityActivityBinding) object;
		boolean equals = true;
		equals &= Util.equals(childActivityId, castedObject.childActivityId);
		equals &= Util.equals(parentActivityId, castedObject.parentActivityId);
		return equals;
	}

	public String getChildActivityId() {
		return childActivityId;
	}

	public String getParentActivityId() {
		return parentActivityId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(childActivityId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(parentActivityId);
			hashCodeComputed = true;
		}

		return hashCode;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(childActivityId);
			stringBuffer.append(',');
			stringBuffer.append(parentActivityId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
