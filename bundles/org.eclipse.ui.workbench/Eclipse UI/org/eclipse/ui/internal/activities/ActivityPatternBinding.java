/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.activities;

import java.util.regex.Pattern;

import org.eclipse.ui.activities.IActivityPatternBinding;
import org.eclipse.ui.internal.util.Util;

public final class ActivityPatternBinding implements IActivityPatternBinding {
    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = ActivityPatternBinding.class
            .getName().hashCode();

    private String activityId;

    private transient int hashCode = HASH_INITIAL;

    private Pattern pattern;

    private transient String string;

    public ActivityPatternBinding(String activityId, String pattern) {
    	this(activityId, Pattern.compile(pattern));
    }

    public ActivityPatternBinding(String activityId, Pattern pattern) {
        if (pattern == null)
            throw new NullPointerException();

        this.activityId = activityId;
        this.pattern = pattern;
    }

    public int compareTo(Object object) {
        ActivityPatternBinding castedObject = (ActivityPatternBinding) object;
        int compareTo = Util.compare(activityId, castedObject.activityId);

        if (compareTo == 0)
            compareTo = Util.compare(pattern.pattern(), castedObject.pattern
                    .pattern());

        return compareTo;
    }

    public boolean equals(Object object) {
        if (!(object instanceof ActivityPatternBinding))
            return false;

        final ActivityPatternBinding castedObject = (ActivityPatternBinding) object;
        if (!Util.equals(activityId, castedObject.activityId)) {
            return false;
        }

        return Util.equals(pattern, castedObject.pattern);
    }

    public String getActivityId() {
        return activityId;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public int hashCode() {
        if (hashCode == HASH_INITIAL) {
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(activityId);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(pattern);
            if (hashCode == HASH_INITIAL)
                hashCode++;
        }

        return hashCode;
    }

    public String toString() {
        if (string == null) {
            final StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append('[');
            stringBuffer.append(activityId);
            stringBuffer.append(',');
            stringBuffer.append(pattern);
            stringBuffer.append(']');
            string = stringBuffer.toString();
        }

        return string;
    }

	/**
	 * Returns whether this binding's pattern matches the given string
	 * 
	 * @param toMatch the string to match
	 * @return <code>true</code> if it matches, <code>false</code> if not
     * @since 3.1
	 */
	public boolean isMatch(String toMatch) {
		return pattern.matcher(toMatch).matches();
	}
}
