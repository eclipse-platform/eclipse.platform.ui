/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class StringUtil {

    /*
     * Helper method for String#split to handle the case where we
     * might be running on Foundation class libraries instead of 1.4.
     */
    public static String[] split(String string, String delimiters) {
    	try {
    		return string.split(delimiters);
    	} catch (NoSuchMethodError e) {
    		// not running 1.4 so try a string tokenizer
    		List result = new ArrayList();
    		for (StringTokenizer tokenizer = new StringTokenizer(string, delimiters); tokenizer.hasMoreTokens(); )
    			result.add(tokenizer.nextToken());
    		return (String[]) result.toArray(new String[result.size()]);
    	}
    }
}
