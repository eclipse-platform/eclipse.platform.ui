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
package org.eclipse.debug.ui;

import org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory;

/**
 * Abstract implementation of a breakpoint container factory delegate which can
 * return its associated factory.
 */
public abstract class AbstractBreakpointContainerFactoryDelegate implements IBreakpointContainerFactoryDelegate {
	
	protected IBreakpointContainerFactory fFactory;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IBreakpointContainerFactoryDelegate#getFactory()
	 */
	public IBreakpointContainerFactory getFactory() {
		return fFactory;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IBreakpointContainerFactoryDelegate#setFactory(org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory)
	 */
	public void setFactory(IBreakpointContainerFactory factory) {
		fFactory= factory;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IBreakpointContainerFactoryDelegate#dispose()
	 */
	public void dispose() {
		// Do nothing
	}

}
