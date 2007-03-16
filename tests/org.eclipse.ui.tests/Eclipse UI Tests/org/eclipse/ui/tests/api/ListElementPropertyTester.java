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
package org.eclipse.ui.tests.api;

import org.eclipse.core.expressions.PropertyTester;

public class ListElementPropertyTester extends PropertyTester {

    public static final String ATTR_NAME = "name";

    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
        ListElement le = (ListElement) receiver;
        if (property.equals(ATTR_NAME)) {
            return expectedValue.equals(le.getName());
        }
        return false;
    }
}

