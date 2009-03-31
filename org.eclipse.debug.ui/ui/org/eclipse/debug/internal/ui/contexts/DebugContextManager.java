/*******************************************************************************
 *  Copyright (c) 2005, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contexts;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.views.ViewContextManager;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextManager;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

/**
 * @since 3.2
 */
public class DebugContextManager implements IDebugContextManager {
	
	private static DebugContextManager fgDefault;
	private Map fServices = new HashMap();
	private ListenerList fGlobalListeners = new ListenerList();
	
	private class WindowListener implements IWindowListener {

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
		 */
		public void windowActivated(IWorkbenchWindow window) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
		 */
		public void windowDeactivated(IWorkbenchWindow window) {			
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
		 */
		public void windowClosed(final IWorkbenchWindow window) {
		    // Use an async exec to dispose the debug context service for the 
			// closed window.  This will allow other window closed listeners 
			// to still use the context service before it is disposed.
			new UIJob(window.getShell().getDisplay(), "DebugContextManager windowClosed() handler") { //$NON-NLS-1$
				{
					setSystem(true);
				}
				
				public IStatus runInUIThread(IProgressMonitor monitor) {
					DebugWindowContextService service = (DebugWindowContextService) fServices.remove(window);
					if (service != null) {
						service.dispose();
					}
					return Status.OK_STATUS;
				}
			}.schedule();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
		 */
		public void windowOpened(IWorkbenchWindow window) {
		}
		
	}
	
	private DebugContextManager() {
		PlatformUI.getWorkbench().addWindowListener(new WindowListener());
	}
	
	public static IDebugContextManager getDefault() {
		if (fgDefault == null) {
			fgDefault = new DebugContextManager();
			// create the model context bindigg manager at the same time
			DebugModelContextBindingManager.getDefault();
			// create view manager
			ViewContextManager.getDefault();			
		}
		return fgDefault;
	}
	
	protected DebugWindowContextService createService(IWorkbenchWindow window) {
		DebugWindowContextService service = (DebugWindowContextService) fServices.get(window);
		if (service == null) {
			service = new DebugWindowContextService(window);
			fServices.put(window, service);
			// register global listeners
			Object[] listeners = fGlobalListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				IDebugContextListener listener = (IDebugContextListener) listeners[i];
				service.addDebugContextListener(listener);
			}
		}
		return service;
	}
	
	protected IDebugContextService getService(IWorkbenchWindow window) {
		return (DebugWindowContextService) fServices.get(window);
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.contexts.IDebugContextManager#addDebugContextListener(org.eclipse.debug.internal.ui.contexts.IDebugContextListener)
	 */
	public void addDebugContextListener(IDebugContextListener listener) {
		fGlobalListeners.add(listener);
		DebugWindowContextService[] services = getServices();
		for (int i = 0; i < services.length; i++) {
			DebugWindowContextService service = services[i];
			service.addDebugContextListener(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.contexts.IDebugContextManager#removeDebugContextListener(org.eclipse.debug.internal.ui.contexts.IDebugContextListener)
	 */
	public void removeDebugContextListener(IDebugContextListener listener) {
		fGlobalListeners.remove(listener);
		DebugWindowContextService[] services = getServices();
		for (int i = 0; i < services.length; i++) {
			DebugWindowContextService service = services[i];
			service.removeDebugContextListener(listener);
		}
	}
	
	/**
	 * Returns the existing context services.
	 * 
	 * @return existing context services
	 */
	private DebugWindowContextService[] getServices() {
		Collection sevices = fServices.values();
		return (DebugWindowContextService[]) sevices.toArray(new DebugWindowContextService[sevices.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextManager#getContextService(org.eclipse.ui.IWorkbenchWindow)
	 */
	public IDebugContextService getContextService(IWorkbenchWindow window) {
		return createService(window);
	}
	
}
