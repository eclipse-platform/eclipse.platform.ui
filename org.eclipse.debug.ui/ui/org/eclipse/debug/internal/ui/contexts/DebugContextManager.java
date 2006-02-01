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
package org.eclipse.debug.internal.ui.contexts;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextListener;
import org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextManager;
import org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * @since 3.2
 */
public class DebugContextManager implements IDebugContextManager {
	
	private static DebugContextManager fgDefault;
	private Map fServices = new HashMap();
	
	private DebugContextManager() {
	}
	
	public static IDebugContextManager getDefault() {
		if (fgDefault == null) {
			fgDefault = new DebugContextManager();
		}
		return fgDefault;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextManager#addDebugContextProvider(org.eclipse.debug.ui.contexts.IDebugContextProvider)
	 */
	public void addDebugContextProvider(IDebugContextProvider provider) {
		IWorkbenchPart part = provider.getPart();
		IWorkbenchWindow window = part.getSite().getWorkbenchWindow();
		DebugWindowContextService service = createService(window);
		service.addProvider(provider);
	}
	
	protected DebugWindowContextService createService(IWorkbenchWindow window) {
		DebugWindowContextService service = (DebugWindowContextService) fServices.get(window);
		if (service == null) {
			service = new DebugWindowContextService(window);
			fServices.put(window, service);
			// TODO: register 'null' provider (global)
		}
		return service;
	}
	
	protected IDebugContextService getService(IWorkbenchWindow window) {
		return (DebugWindowContextService) fServices.get(window);
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextManager#removeDebugContextProvider(org.eclipse.debug.ui.contexts.IDebugContextProvider)
	 */
	public void removeDebugContextProvider(IDebugContextProvider provider) {
		IWorkbenchPart part = provider.getPart();
		IWorkbenchWindow window = part.getSite().getWorkbenchWindow();
		DebugWindowContextService service = (DebugWindowContextService) fServices.get(window);
		if (service != null) {
			service.removeProvider(provider);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextManager#addDebugContextListener(org.eclipse.debug.ui.contexts.IDebugContextListener, org.eclipse.ui.IWorkbenchWindow)
	 */
	public void addDebugContextListener(IDebugContextListener listener, IWorkbenchWindow window) {
		IDebugContextService service = createService(window);
		service.addDebugContextListener(listener);			
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextManager#removeDebugContextListener(org.eclipse.debug.ui.contexts.IDebugContextListener, org.eclipse.ui.IWorkbenchWindow)
	 */
	public void removeDebugContextListener(IDebugContextListener listener, IWorkbenchWindow window) {
		IDebugContextService service = getService(window);
		if (service != null) {
			service.removeDebugContextListener(listener);
		}	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextManager#addDebugContextListener(org.eclipse.debug.ui.contexts.IDebugContextListener, org.eclipse.ui.IWorkbenchWindow, java.lang.String)
	 */
	public void addDebugContextListener(IDebugContextListener listener, IWorkbenchWindow window, String partId) {
		DebugWindowContextService service = createService(window);
		service.addDebugContextListener(listener, partId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextManager#removeDebugContextListener(org.eclipse.debug.ui.contexts.IDebugContextListener, org.eclipse.ui.IWorkbenchWindow, java.lang.String)
	 */
	public void removeDebugContextListener(IDebugContextListener listener, IWorkbenchWindow window, String partId) {
		IDebugContextService service = getService(window);
		if (service != null) {
			service.removeDebugContextListener(listener, partId);
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextManager#getActiveContext(org.eclipse.ui.IWorkbenchWindow)
	 */
	public ISelection getActiveContext(IWorkbenchWindow window) {
		IDebugContextService service = getService(window);
		if (service != null) {
			return service.getActiveContext();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextManager#getActiveContext(org.eclipse.ui.IWorkbenchWindow, java.lang.String)
	 */
	public ISelection getActiveContext(IWorkbenchWindow window, String partId) {
		IDebugContextService service = getService(window);
		if (service != null) {
			return service.getActiveContext(partId);
		}
		return null;
	}
	
}
