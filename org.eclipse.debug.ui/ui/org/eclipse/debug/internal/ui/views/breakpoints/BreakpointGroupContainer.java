/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

import org.eclipse.debug.core.model.IBreakpoint;

/**
 * Subclass of breakpoint container which exists to allow action contributions to
 * breakpoint group containers
 */
public class BreakpointGroupContainer extends BreakpointContainer {

    /**
     * @param breakpoints
     * @param parentContainer
     * @param creatingFactory
     * @param name
     */
    public BreakpointGroupContainer(IBreakpoint[] breakpoints, IBreakpointContainer parentContainer, IBreakpointContainerFactory creatingFactory, String name) {
        super(breakpoints, parentContainer, creatingFactory, name);
    }

}
