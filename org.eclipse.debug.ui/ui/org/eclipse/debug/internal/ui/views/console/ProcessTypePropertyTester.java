/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.console;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.console.TextConsole;

/**
 * Tests if a process type matches the expected value.
 * 
 * @since 3.1
 */
public class ProcessTypePropertyTester extends PropertyTester {

    /* (non-Javadoc)
     * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
     */
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (receiver instanceof TextConsole) {
            TextConsole console = (TextConsole) receiver;
            IProcess process = (IProcess) console.getAttribute(IDebugUIConstants.ATTR_CONSOLE_PROCESS);
            if (process != null) {
                String type = process.getAttribute(IProcess.ATTR_PROCESS_TYPE);
                return (type != null && type.equals(expectedValue));
            }//end if
        }//end if
        return false;
    }//end test

}//end class
