/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands;

public final class Point implements Comparable {

	private final static int HASH_FACTOR = 87;
	private final static int HASH_INITIAL = 97;

	public static Point create(int x, int y) {
		return new Point(x, y);
	}

	private int x;
	private int y;

	private Point(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	public int compareTo(Object object) {
		Point point = (Point) object;
		int compareTo = x - point.x;

		if (compareTo == 0)
			compareTo = y - point.y;

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof Point))
			return false;

		Point point = (Point) object;
		return x == point.x && y == point.y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + x;
		result = result * HASH_FACTOR + y;
		return result;
	}
}
