/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
    
    private String patternString;
    
    private boolean isEqualityPattern;

    private transient String string;
    
    /**
     * @param activityId The id.
     * @param pattern A string that will be compiled to a pattern matcher.
     */
    public ActivityPatternBinding(String activityId, String pattern) {
    	this(activityId, Pattern.compile(pattern));
    }
    
    /**
     *  
     * @param activityId The id
     * @param pattern This string will be used as plain string, or as pattern-
     * 		  matcher pattern. The use depends on parameter <code>nonRegExp</code>.
     * @param isEqualityPattern If true the <code>pattern</code> string will be
     * 	      interpreted as normal string, not as pattern. 
     */
    public ActivityPatternBinding(String activityId, String pattern, boolean
    		isEqualityPattern) {
    	if (pattern == null) {
			throw new NullPointerException();
		}
    	
    	this.activityId = activityId;
    	this.isEqualityPattern = isEqualityPattern;
    	if (isEqualityPattern) {
    		this.patternString = pattern;    		
    		this.pattern = null;
    	} else {    		
    		this.patternString = null;
    		this.pattern = Pattern.compile(pattern);
    	}    
    }

    public ActivityPatternBinding(String activityId, Pattern pattern) {
        if (pattern == null) {
			throw new NullPointerException();
		}

        this.activityId = activityId;
        this.pattern = pattern;
        this.isEqualityPattern = false;
        this.patternString = null;
    }

    public int compareTo(Object object) {
        ActivityPatternBinding castedObject = (ActivityPatternBinding) object;
        int compareTo = Util.compare(activityId, castedObject.activityId);

        if (compareTo == 0) {        	
        	compareTo = Util.compare(isEqualityPattern,
					castedObject.isEqualityPattern);
        	
        	if (compareTo == 0)
				compareTo = Util.compare(getPattern().pattern(), castedObject
						.getPattern().pattern());
		}

        return compareTo;
    }

    public boolean equals(Object object) {
        if (!(object instanceof ActivityPatternBinding)) {
			return false;
		}

        final ActivityPatternBinding castedObject = (ActivityPatternBinding) object;
        if (!Util.equals(activityId, castedObject.activityId)) {
            return false;
        }
        
        if (!Util.equals(isEqualityPattern, castedObject.isEqualityPattern)) {
            return false;
        }

        return Util.equals(getPattern(), castedObject.getPattern());
    }

    public String getActivityId() {
        return activityId;
    }
    
    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.activities.IActivityPatternBinding#getPattern()
	 */
    public Pattern getPattern() {
    	if (pattern == null) {
    		pattern = Pattern.compile(PatternUtil.quotePattern(patternString));    		
    	}
    	return pattern;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.activities.IActivityPatternBinding#getString()
     */
    public String getString() {
    	if (isEqualityPattern) {
    		return patternString;
    	}
    	return getPattern().pattern();
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.ui.activities.IActivityPatternBinding#isEqualityPattern()
	 */
	public boolean isEqualityPattern() {
		return isEqualityPattern;
	}
    

    public int hashCode() {
        if (hashCode == HASH_INITIAL) {
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(activityId);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(pattern);
            if (hashCode == HASH_INITIAL) {
				hashCode++;
			}
        }

        return hashCode;
    }

    public String toString() {
        if (string == null) {
            final StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append('[');
            stringBuffer.append(activityId);
            stringBuffer.append(',');
            stringBuffer.append(isEqualityPattern());
            stringBuffer.append(',');
            stringBuffer.append(getString());
            stringBuffer.append(']');
            string = stringBuffer.toString();
        }

        return string;
    }

	/**
	 * Returns whether this binding's pattern matches the given string.
	 * 
	 * @param toMatch the string to match
	 * @return <code>true</code> if it matches, <code>false</code> if not
     * @since 3.1
	 */
	public boolean isMatch(String toMatch) {
		if (isEqualityPattern) {
			return patternString.equals(toMatch);
		}
		return pattern.matcher(toMatch).matches();		
	}	
}
