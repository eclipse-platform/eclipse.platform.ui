/*******************************************************************************
 * Copyright (c) 2004, 2005 Richard Hoefter and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Richard Hoefter (richard.hoefter@web.de) - initial API and implementation
 *     IBM Corporation - nlsing and incorporating into Eclipse
 *******************************************************************************/

package org.eclipse.ant.internal.ui.datatransfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Collection utilities.
 */
public class CollectionUtil
{
    private CollectionUtil() {}

    /**
     * Remove duplicates preserving original order.
     * @param l list to remove duplicates from
     * @return new list without duplicates 
     */
    public static List removeDuplicates(List l)
    {
        List res = new ArrayList();
        for (Iterator iter = l.iterator(); iter.hasNext();)
        {
            Object element = iter.next();
            if (!res.contains(element))
            {
                res.add(element);
            }
        }
        return res;
    }
    
    public static String toString(Collection c, String separator)
    {
        StringBuffer b = new StringBuffer();
        for (Iterator iter = c.iterator(); iter.hasNext();)
        {
            String element = (String) iter.next();
            b.append(element);
            b.append(separator);
        }
        String result = b.toString();
        return StringUtil.removeSuffix(result, separator);
    }
}