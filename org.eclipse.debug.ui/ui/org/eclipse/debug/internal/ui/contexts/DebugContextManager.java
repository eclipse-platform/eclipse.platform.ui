/*******************************************************************************
 *  Copyright (c) 2005, 2016 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Patrick Chuong (Texas Instruments) - Allow multiple debug views and
 *     		multiple debug context providers (Bug 327263)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contexts;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.internal.ui.views.ViewContextManager;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextManager;
import org.eclipse.debug.ui.contexts.IDebugContextProvider;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;

/**
 * @since 3.2
 */
public class DebugContextManager implements IDebugContextManager {

	private static DebugContextManager fgDefault;
	private Map<IWorkbenchWindow, DebugWindowContextService> fServices = new HashMap<IWorkbenchWindow, DebugWindowContextService>();
	private ListenerList<IDebugContextListener> fGlobalListeners = new ListenerList<>();

	/**
	 * A debug context service that does nothing (used for windows that have been closed)
	 */
	private static IDebugContextService NULL_SERVICE = new IDebugContextService() {
		@Override
		public void removePostDebugContextListener(IDebugContextListener listener, String partId) {
		}
		@Override
		public void removePostDebugContextListener(IDebugContextListener listener) {
		}
		@Override
		public void removeDebugContextProvider(IDebugContextProvider provider) {
		}
		@Override
		public void removeDebugContextListener(IDebugContextListener listener, String partId) {
		}
		@Override
		public void removeDebugContextListener(IDebugContextListener listener) {
		}
		@Override
		public ISelection getActiveContext(String partId) {
			return null;
		}
		@Override
		public ISelection getActiveContext() {
			return null;
		}
		@Override
		public void addPostDebugContextListener(IDebugContextListener listener, String partId) {
		}
		@Override
		public void addPostDebugContextListener(IDebugContextListener listener) {
		}
		@Override
		public void addDebugContextProvider(IDebugContextProvider provider) {
		}
		@Override
		public void addDebugContextListener(IDebugContextListener listener, String partId) {
		}
		@Override
		public void addDebugContextListener(IDebugContextListener listener) {
		}
		@Override
		public void addDebugContextListener(IDebugContextListener listener, String partId, String partSecondaryId) {

		}
		@Override
		public void removeDebugContextListener(IDebugContextListener listener, String partId, String partSecondaryId) {
		}

		@Override
		public ISelection getActiveContext(String partId, String partSecondaryId) {
			return null;
		}
		@Override
		public void addPostDebugContextListener(IDebugContextListener listener, String partId, String partSecondaryId) {
		}

		@Override
		public void removePostDebugContextListener(IDebugContextListener listener, String partId, String partSecondaryId) {
		}
	};

	private class WindowListener implements IWindowListener {

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
		 */
		@Override
		public void windowActivated(IWorkbenchWindow window) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
		 */
		@Override
		public void windowDeactivated(IWorkbenchWindow window) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
		 */
		@Override
		public void windowClosed(final IWorkbenchWindow window) {
            DebugWindowContextService service = fServices.get(window);
            if (service != null) {
            	fServices.remove(window);
                service.dispose();
            }
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
		 */
		@Override
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

	protected IDebugContextService createService(IWorkbenchWindow window) {
		DebugWindowContextService service = fServices.get(window);
		if (service == null) {
			IEvaluationService evaluationService = window.getService(IEvaluationService.class);
			if (window.getShell() == null || evaluationService == null) {
				// the window has been closed - return a dummy service
				return NULL_SERVICE;
			} else {
				service = new DebugWindowContextService(window, evaluationService);
				fServices.put(window, service);
				// register global listeners
				for (IDebugContextListener iDebugContextListener : fGlobalListeners) {
					service.addDebugContextListener(iDebugContextListener);
				}
			}
		}
		return service;
	}

	protected IDebugContextService getService(IWorkbenchWindow window) {
		return fServices.get(window);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.contexts.IDebugContextManager#addDebugContextListener(org.eclipse.debug.internal.ui.contexts.IDebugContextListener)
	 */
	@Override
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
	@Override
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
		Collection<DebugWindowContextService> sevices = fServices.values();
		return sevices.toArray(new DebugWindowContextService[sevices.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextManager#getContextService(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public IDebugContextService getContextService(IWorkbenchWindow window) {
		return createService(window);
	}

}
