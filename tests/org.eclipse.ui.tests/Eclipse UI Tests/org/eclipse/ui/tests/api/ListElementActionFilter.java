/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IActionFilter;

public class ListElementActionFilter implements IActionFilter {

    private boolean called = false;

    public static final String ATTR_NAME = "name";

    public static final String ATTR_FLAG = "flag";

    public static final String VAL_TRUE = "true";

    public static final String VAL_FALSE = "false";

    private static ListElementActionFilter singleton;

    public static ListElementActionFilter getSingleton() {
        if (singleton == null)
            singleton = new ListElementActionFilter();
        return singleton;
    }

    private ListElementActionFilter() {
        super();
    }

    /**
     * @see IActionFilter#testAttribute(Object, String, String)
     */
    public boolean testAttribute(Object target, String name, String value) {
        called = true;
        ListElement le = (ListElement) target;
        if (name.equals(ATTR_NAME)) {
            return value.equals(le.getName());
        } else if (name.equals(ATTR_FLAG)) {
            boolean flag = le.getFlag();
            if (flag)
                return value.equals(VAL_TRUE);
            else
                return value.equals(VAL_FALSE);
        }
        return false;
    }

    public void clearCalled() {
        called = false;
    }

    public boolean getCalled() {
        return called;
    }

}

