/*******************************************************************************
 * Copyright (c) 2004, 2005 Richard Hoefter and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Richard Hoefter (richard.hoefter@web.de) - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.datatransfer;

/**
 * String utilities.
 */
public class StringUtil {

    public static final String NEWLINE = System.getProperty("line.separator"); //$NON-NLS-1$

    public static String removePrefix(String s, String prefix)
    {
        if (s == null)
        {
            return null;
        }
        if (s.startsWith(prefix))
        {
            return s.substring(prefix.length());
        }
        return s;
    }

    public static String removeSuffix(String s, String suffix)
    {
        if (s == null)
        {
            return null;
        }
        if (s.endsWith(suffix))
        {
            return s.substring(0, s.length() - suffix.length());
        }
        return s;
    }

    public static String removePrefixAndSuffix(String s, String prefix, String suffix)
    {
        return removePrefix(removeSuffix(s, suffix), prefix);
    }
}