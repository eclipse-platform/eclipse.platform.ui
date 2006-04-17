/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.universal.util;



public class StringUtil {

    public static StringBuffer concat(String string1, String string2,
            String string3) {
        StringBuffer buffer = new StringBuffer(string1);
        buffer.append(string2);
        buffer.append(string3);
        return buffer;
    }

    public static StringBuffer concat(String string1, String string2,
            String string3, String string4) {
        StringBuffer buffer = concat(string1, string2, string3);
        buffer.append(string4);
        return buffer;
    }

    public static StringBuffer concat(String string1, String string2,
            String string3, String string4, String string5) {
        StringBuffer buffer = concat(string1, string2, string3, string4);
        buffer.append(string5);
        return buffer;
    }

    public static StringBuffer concat(String string1, String string2,
            String string3, String string4, String string5, String string6) {
        StringBuffer buffer = concat(string1, string2, string3, string4,
            string5);
        buffer.append(string6);
        return buffer;
    }



}
