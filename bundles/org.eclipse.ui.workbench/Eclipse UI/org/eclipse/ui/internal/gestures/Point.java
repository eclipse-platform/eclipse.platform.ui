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

package org.eclipse.ui.internal.gestures;

public final class Point implements Comparable {

    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = Point.class.getName().hashCode();

    private transient int hashCode;

    private transient boolean hashCodeComputed;

    private transient String string;

    private int x;

    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int compareTo(Object object) {
        Point castedObject = (Point) object;
        int compareTo = x - castedObject.x;

        if (compareTo == 0)
            compareTo = y - castedObject.y;

        return compareTo;
    }

    public boolean equals(Object object) {
        if (!(object instanceof Point))
            return false;

        Point castedObject = (Point) object;
        boolean equals = true;
        equals &= x == castedObject.x;
        equals &= y == castedObject.y;
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