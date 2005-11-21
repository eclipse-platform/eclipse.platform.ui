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
package org.eclipse.debug.internal.ui.sourcelookup;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.contexts.DebugContextManager;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.ISourceDisplayAdapter;
import org.eclipse.debug.ui.sourcelookup.ISourceLookupResult;
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
public class SourceLookupService implements IDebugContextListener {
	
	private IWorkbenchWindow fWindow;
	
	public SourceLookupService(IWorkbenchWindow window) {
		fWindow = window;
		DebugContextManager.getDefault().addDebugContextListener(this, window);
	}
	
	public void dispose() {
		DebugContextManager.getDefault().removeDebugContextListener(this, fWindow);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextListener#contextActivated(java.lang.Object, org.eclipse.ui.IWorkbenchPart)
	 */
	public synchronized void contextActivated(ISelection selection, IWorkbenchPart part) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection)selection;
			if (structuredSelection.size() == 1) {
				Object context = (structuredSelection).getFirstElement();
				if (context instanceof IAdaptable) {
					IAdaptable adaptable = (IAdaptable) context;
					ISourceDisplayAdapter adapter = (ISourceDisplayAdapter) adaptable.getAdapter(ISourceDisplayAdapter.class);
					if (adapter != null) {
						IWorkbenchPage page = null;
						if (part == null) {
							page = fWindow.getActivePage();
						} else {
							page = part.getSite().getPage();
						} 
						adapter.displaySource(context, page);
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextListener#contextChanged(org.eclipse.jface.viewers.ISelection, org.eclipse.ui.IWorkbenchPart)
	 */
	public void contextChanged(ISelection selection, IWorkbenchPart part) {		
	}	
}
