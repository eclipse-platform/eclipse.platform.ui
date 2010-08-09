/*******************************************************************************
  * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River - Pawel Piech - Fixed debug context service usage (Bug 258189)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.internal.ui.views.launch.DebugElementAdapterFactory;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Performs source lookup in a window.
 * 
 * @since 3.2
 */
public class SourceLookupService implements IDebugContextListener, ISourceDisplay {
	
	private IWorkbenchWindow fWindow;
	private IDebugContextService fDebugContextService;
	
	public SourceLookupService(IWorkbenchWindow window) {
		fWindow = window;
		fDebugContextService = DebugUITools.getDebugContextManager().getContextService(window); 
		fDebugContextService.addDebugContextListener(this);
	}
	
	public void dispose() {
		fDebugContextService.removeDebugContextListener(this);
		fWindow = null;
	}

	public synchronized void debugContextChanged(DebugContextEvent event) {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) > 0) {
			displaySource(event.getContext(), event.getDebugContextProvider().getPart(), false);
		}
	}
		
	/**
	 * Displays source for the given selection and part, optionally forcing
	 * a source lookup.
	 * 
	 * @param selection
	 * @param part
	 * @param force
	 */
	protected synchronized void displaySource(ISelection selection, IWorkbenchPart part, boolean force) {
	    if (fWindow == null) return; // disposed
	    
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection)selection;
			if (structuredSelection.size() == 1) {
				Object context = (structuredSelection).getFirstElement();
				IWorkbenchPage page = null;
				if (part == null) {
					page = fWindow.getActivePage();
				} else {
					page = part.getSite().getPage();
				} 
				displaySource(context, page, force);
			}
		}
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.contexts.ISourceDisplayAdapter#displaySource(java.lang.Object, org.eclipse.ui.IWorkbenchPage, boolean)
	 */
	public void displaySource(Object context, IWorkbenchPage page, boolean forceSourceLookup) {
		if (context instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) context;
			ISourceDisplay adapter = (ISourceDisplay) adaptable.getAdapter(ISourceDisplay.class);
			if (adapter == null && !(context instanceof PlatformObject)) {
	        	// for objects that don't properly subclass PlatformObject to inherit default
	        	// adapters, just delegate to the adapter factory
	        	adapter = (ISourceDisplay) new DebugElementAdapterFactory().getAdapter(context, ISourceDisplay.class);
	        }
			if (adapter != null) {						
				adapter.displaySource(context, page, forceSourceLookup);
			}
		}
	}	
}
