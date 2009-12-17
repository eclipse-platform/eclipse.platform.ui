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

package org.eclipse.ui.activities;

import java.util.regex.Pattern;

/**
 * An instance of this interface represents a binding between an activity and a
 * regular expression pattern.  It's typically unnecessary to use this interface 
 * directly.  Rather, clients wishing to test strings against activity patterns
 * should use instances of <code>IIdentifier</code>.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @see IActivity
 * @see IIdentifier
 * @see IActivityManager#getIdentifier(String)
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IActivityPatternBinding extends Comparable {

    /**
     * Returns the identifier of the activity represented in this binding.
     * 
     * @return the identifier of the activity represented in this binding.
     *         Guaranteed not to be <code>null</code>.
     */
    String getActivityId();

    /**
	 * Returns the pattern represented in this binding. This pattern should
	 * conform to the regular expression syntax described by the
	 * <code>java.util.regex.Pattern</code> class. If
	 * {@link #isEqualityPattern()} is <code>true</code> a Pattern will be
	 * generated based on the escaped version of the String returned by
	 * {@link #getString()}.
	 * 
	 * @return the pattern. Guaranteed not to be <code>null</code>.
	 */
	Pattern getPattern();
    
    /**
	 * If {@link #isEqualityPattern()} is <code>true</code> this will return
	 * the raw <em>pattern</em> string. Otherwise it will return the string
	 * version of the compiled pattern.
	 * 
	 * @return The raw <em>pattern</em> string, or the string version of the
	 *         compiled pattern, depending on {@link #isEqualityPattern()}.
	 * @since 3.4
	 */
	String getString();

	/**
	 * Answers whether or not the pattern should be treated as a regular string
	 * or a regular expression. If <code>true</code>, this pattern binding
	 * will represent an equality match between the pattern and a target ID
	 * rather than a regular expression match.
	 * 
	 * @return whether the pattern should be treated as regular string
	 * @since 3.4
	 */
	boolean isEqualityPattern();
}
