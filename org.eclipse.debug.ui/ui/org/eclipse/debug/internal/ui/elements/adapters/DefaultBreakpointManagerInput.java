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
 * Default breakpoint manager input.
 * 
 * @since 3.6
 */
public class DefaultBreakpointManagerInput extends AbstractBreakpointManagerInput {
		
	/**
	 * Constructor - Default breakpoint manager input.
	 * 
	 * @param context the presentation context.
	 */
	public DefaultBreakpointManagerInput(IPresentationContext context) {
		super(context);
	}
	

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (getContext() != null) {
			return getContext().hashCode();
		} else {
			return 1;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0) {
		if ( (arg0 != null) && arg0.getClass().equals(this.getClass()) ) {

			IPresentationContext context = ((DefaultBreakpointManagerInput) arg0).getContext();
			if (getContext() != null && context != null)
				return getContext().equals(context);
		} 
		
		return super.equals(arg0);				
	}
}
