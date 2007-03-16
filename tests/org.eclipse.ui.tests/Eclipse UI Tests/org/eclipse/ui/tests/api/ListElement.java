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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IActionFilter;

public class ListElement implements IAdaptable {

    private String name;

    private boolean flag;

    public ListElement(String name) {
        this(name, false);
    }

    public ListElement(String name, boolean flag) {
        this.name = name;
        this.flag = flag;
    }

    public String toString() {
        return name + ':' + flag;
    }

    public String getName() {
        return name;
    }

    public boolean getFlag() {
        return flag;
    }

    public Object getAdapter(Class adapter) {
        if (adapter == IActionFilter.class) {
            return ListElementActionFilter.getSingleton();
        }
        return null;
    }

}

