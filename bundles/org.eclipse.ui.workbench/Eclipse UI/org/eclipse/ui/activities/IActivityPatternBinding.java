/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
     * Returns the pattern represented in this binding.  This pattern should 
     * conform to the regular expression syntax described by the 
     * <code>java.util.regex.Pattern</code> class.
     * 
     * @return the pattern. Guaranteed not to be <code>null</code>.
     */
    Pattern getPattern();
}