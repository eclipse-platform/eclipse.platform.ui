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

package org.eclipse.ui.internal.contexts;

import org.eclipse.ui.contexts.IContextBinding;
import org.eclipse.ui.internal.util.Util;

final class ContextBinding implements IContextBinding {
	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL =
		ContextBinding.class.getName().hashCode();
	private String childContextId;
	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private String parentContextId;
	private transient String string;

	ContextBinding(String childContextId, String parentContextId) {
		if (childContextId == null || parentContextId == null)
			throw new NullPointerException();

		this.childContextId = childContextId;
		this.parentContextId = parentContextId;
	}

	public int compareTo(Object object) {
		ContextBinding castedObject = (ContextBinding) object;
		int compareTo =
			Util.compare(childContextId, castedObject.childContextId);

		if (compareTo == 0)
			compareTo =
				Util.compare(parentContextId, castedObject.parentContextId);

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof ContextBinding))
			return false;

		ContextBinding castedObject = (ContextBinding) object;
		boolean equals = true;
		equals &= Util.equals(childContextId, castedObject.childContextId);
		equals &= Util.equals(parentContextId, castedObject.parentContextId);
		return equals;
	}

	public String getContextId() {
		return childContextId;
	}

	public String getParentContextId() {
		return parentContextId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(childContextId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(parentContextId);
			hashCodeComputed = true;
		}

		return hashCode;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(childContextId);
			stringBuffer.append(',');
			stringBuffer.append(parentContextId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
