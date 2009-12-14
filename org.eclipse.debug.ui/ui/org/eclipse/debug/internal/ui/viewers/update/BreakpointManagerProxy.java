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
package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.internal.ui.elements.adapters.AbstractBreakpointManagerInput;
import org.eclipse.debug.internal.ui.model.elements.AbstractBreakpointManagerContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.ViewerAdapterService;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.jface.viewers.Viewer;

/**
 * Breakpoint manager model proxy.
 * 
 * @since 3.6
 */
public class BreakpointManagerProxy extends AbstractModelProxy {
	/**
	 * The breakpoint manager content provider for this model proxy
	 */
	protected AbstractBreakpointManagerContentProvider fProvider;
	
	/**
	 * The breakpoint manager input for this model proxy
	 */
	protected AbstractBreakpointManagerInput fInput;
	
	/**
	 * Constructor.
	 * 
	 * @param input the breakpoint manager input
	 * @param context the presentation context for this model proxy
	 */
	public BreakpointManagerProxy(Object input, IPresentationContext context) {
		super();
				
		if (input instanceof AbstractBreakpointManagerInput) {
			fInput = (AbstractBreakpointManagerInput) input;
			
			// cache the required data and pass to the provider when this model is installed
			IElementContentProvider provider = ViewerAdapterService.getContentProvider(input);
			if (provider instanceof AbstractBreakpointManagerContentProvider) {
				fProvider = (AbstractBreakpointManagerContentProvider) provider;
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy#installed(org.eclipse.jface.viewers.Viewer)
	 */
	public void installed(Viewer viewer) {
		super.installed(viewer);
		if (fProvider != null) {
			fProvider.registerModelProxy(fInput, this);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy#dispose()
	 */
	public synchronized void dispose() {
		if (fProvider != null) {
			fProvider.unregisterModelProxy(fInput, this);
		}
		super.dispose();		
	}
}
