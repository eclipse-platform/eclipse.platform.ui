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

package org.eclipse.ui.internal.commands;

public final class XPoint implements Comparable {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = XPoint.class.getName().hashCode();

	private int x;
	private int y;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;

	XPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int compareTo(Object object) {
		XPoint point = (XPoint) object;
		int compareTo = x - point.x;

		if (compareTo == 0)
			compareTo = y - point.y;

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof XPoint))
			return false;

		XPoint point = (XPoint) object;
		boolean equals = true;
		equals &= x == point.x;
		equals &= y == point.y;
		return equals;		
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + x;
			hashCode = hashCode * HASH_FACTOR + y;			
			hashCodeComputed = true;
		}
			
		return hashCode;		
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(x);
			stringBuffer.append(',');
			stringBuffer.append(y);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;			
	}
}
