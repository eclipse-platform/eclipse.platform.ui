/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions.tests;

import org.eclipse.core.runtime.IAdaptable;

/**
 * 
 */
public class AdaptableAdaptee implements IAdaptable {

    private Adapter fAdapter = new Adapter();
    
    public Object getAdapter(Class adapter) {
        if (adapter.isInstance(fAdapter)) {
            return fAdapter;
        }
        return null;
    }

}
