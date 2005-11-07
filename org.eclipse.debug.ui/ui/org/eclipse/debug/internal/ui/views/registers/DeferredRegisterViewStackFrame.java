/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.registers;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.elements.adapters.DeferredStackFrame;

public class DeferredRegisterViewStackFrame extends DeferredStackFrame {
    
	/* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parent) {
        try {
            return ((IStackFrame)parent).getRegisterGroups();
        } catch (DebugException e) {
        }
        return EMPTY;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.DeferredStackFrame#hasChildren(java.lang.Object)
	 */
	protected boolean hasChildren(Object child) {
		IRegisterGroup group = (IRegisterGroup) child;
		try {
			return group.hasRegisters();
		} catch (DebugException e) {
		}
		return false;
	}    
}
