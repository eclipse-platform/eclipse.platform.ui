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
package org.eclipse.debug.examples.ui.pda.views;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.examples.core.pda.model.PDADebugElement;
import org.eclipse.debug.examples.core.pda.model.PDADebugTarget;

/**
 * Property tester for use with standard expressions to determine whether 
 * the given debug target can perform a push operation.
 */
public class CanPushTester extends PropertyTester {
   
    private static final String CAN_PUSH_PROPERTY = "canPush";
    
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (CAN_PUSH_PROPERTY.equals(property)) {
            if (receiver instanceof IAdaptable) {
                PDADebugElement element = (PDADebugElement) 
                    ((IAdaptable)receiver).getAdapter(PDADebugElement.class);
                if (element != null) {
                    PDADebugTarget target = (PDADebugTarget)element.getDebugTarget();
                    return target.canPush();
                }
            }
        }
        return false;
    }
}
