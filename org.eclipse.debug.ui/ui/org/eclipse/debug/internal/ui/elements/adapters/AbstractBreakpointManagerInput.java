/*****************************************************************
 * Copyright (c) 2009 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Initial API and implementation (Bug 238956)
 *****************************************************************/
package org.eclipse.debug.internal.ui.elements.adapters;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * A breakpoint manager input allows the breakpoints view to link the active debug context
 * per workbench window.
 * 
 * @since 3.6
 */
public abstract class AbstractBreakpointManagerInput {
	
	/**
	 * The presentation context of the breakpoints view.
	 */
	final private IPresentationContext fContext;
	
	/**
	 * Constructor.
	 * 
	 * @param context the presentation context for this input
	 */
	protected AbstractBreakpointManagerInput(IPresentationContext context) {
		fContext = context;
	}
		
	/**
	 * Returns the presentation context for this input.
	 * 
	 * @return the presentation context
	 */
	public IPresentationContext getContext() {
		return fContext;
	}
}
