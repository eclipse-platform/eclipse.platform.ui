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
package org.eclipse.debug.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * Optional enhancements to the {@link IBreakpointOrganizerDelegate} interface.
 * Supports operations on more than one breakpoint at a time.
 * <p>
 * Clients contributing a breakpoint organizer may optionally implement this
 * interface when implementing {@link IBreakpointOrganizerDelegate}.
 * </p>
 * @since 3.3
 */
public interface IBreakpointOrganizerDelegateExtension extends IBreakpointOrganizerDelegate {
    
    /**
     * Adds the specified breakpoints to the given category. Only called
     * if <code>canAdd(...)</code> returns <code>true</code> for the given
     * breakpoints and category.
     * 
     * @param breakpoints breakpoints add
     * @param category the breakpoints' new category
     */
    public void addBreakpoints(IBreakpoint[] breakpoints, IAdaptable category);
    
    /**
     * Removes the specified breakpoints from the given category. Only
     * called if <code>canRemove(...)</code> returns <code>true</code> for
     * the given breakpoints and category.
     * 
     * @param breakpoints breakpoints to remove
     * @param category the category the breakpoint is remove from
     */
    public void removeBreakpoints(IBreakpoint[] breakpoints, IAdaptable category);
        
    
}
