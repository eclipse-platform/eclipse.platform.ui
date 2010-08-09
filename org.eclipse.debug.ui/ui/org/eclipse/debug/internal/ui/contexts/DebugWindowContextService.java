/*******************************************************************************
 *  Copyright (c) 2005, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River - Pawel Piech - added an evaluation context source provider (bug 229219)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contexts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextProvider;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.services.IEvaluationService;

/**
 * Context service for a specific window.
 * 
 * @since 3.2
 */
public class DebugWindowContextService implements IDebugContextService, IPartListener2, IDebugContextListener {
	
	private Map fListenersByPartId = new HashMap();
	private Map fProvidersByPartId = new HashMap();
	private Map fPostListenersByPartId = new HashMap();
	
	private IWorkbenchWindow fWindow;
	private List fProviders = new ArrayList();
	
	private DebugContextSourceProvider fSourceProvider;

	public DebugWindowContextService(IWorkbenchWindow window) {
		fWindow = window;
		fWindow.getPartService().addPartListener(this);
		
		IEvaluationService evaluationService = (IEvaluationService)window.getService(IEvaluationService.class);
		fSourceProvider = new DebugContextSourceProvider(this, evaluationService);
	}
	
	public void dispose() {
		fSourceProvider.dispose();
		fWindow.getPartService().removePartListener(this);
		fWindow = null;
	}
	
	public synchronized void addDebugContextProvider(IDebugContextProvider provider) {
	    if (fWindow == null) return; // disposed
	    
		IWorkbenchPart part = provider.getPart();
		String id = null;
		if (part != null) {
			id = part.getSite().getId();
		}
		fProvidersByPartId.put(id, provider);
		fProviders.add(provider);
		IWorkbenchPart active = null;
		IWorkbenchPage activePage = fWindow.getActivePage();
		if (activePage != null) {
			active = activePage.getActivePart();
		}
		if (fProviders.size() == 1 && (part == null || part.equals(active))) {
			notify(provider);
		}
		provider.addDebugContextListener(this);
	}
	
	public synchronized void removeDebugContextProvider(IDebugContextProvider provider) {
		int index = fProviders.indexOf(provider);
		if (index >= 0) {
			IWorkbenchPart part = provider.getPart();
			String id = null;
			if (part != null) {
				id = part.getSite().getId();
			}
			fProvidersByPartId.remove(id);
			fProviders.remove(index);
			if (index == 0) {
				IDebugContextProvider activeProvider = getActiveProvider();
				if (activeProvider != null) {
					notify(activeProvider);
				} else {
					notify(new DebugContextEvent(provider, new StructuredSelection(), DebugContextEvent.ACTIVATED));
				}
			}
		}
		provider.removeDebugContextListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextService#addDebugContextListener(org.eclipse.debug.ui.contexts.IDebugContextListener)
	 */
	public void addDebugContextListener(IDebugContextListener listener) {
		addDebugContextListener(listener, null);
	}
	
	public void addPostDebugContextListener(IDebugContextListener listener, String partId) {
		ListenerList list = (ListenerList) fPostListenersByPartId.get(partId);
		if (list == null) {
			list = new ListenerList();
			fPostListenersByPartId.put(partId, list);
		}
		list.add(listener);	
	}

	public void addPostDebugContextListener(IDebugContextListener listener) {
		addPostDebugContextListener(listener, null);
	}
	
	public void removePostDebugContextListener(IDebugContextListener listener, String partId) {
		ListenerList list = (ListenerList) fPostListenersByPartId.get(partId);
		if (list != null) {
			list.remove(listener);
		}
	}

	public void removePostDebugContextListener(IDebugContextListener listener) {
		removePostDebugContextListener(listener, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextService#removeDebugContextListener(org.eclipse.debug.ui.contexts.IDebugContextListener)
	 */
	public void removeDebugContextListener(IDebugContextListener listener) {
		removeDebugContextListener(listener, null);
	}
	
	/**
	 * Notifies listeners of the context in the specified provider.
	 * 
	 * @param provdier context provider
	 */
	protected void notify(IDebugContextProvider provdier) {
		ISelection activeContext = provdier.getActiveContext();
		if (activeContext == null) {
			activeContext = new StructuredSelection();
		}
		notify(new DebugContextEvent(provdier, activeContext, DebugContextEvent.ACTIVATED));
	}
	
	protected void notify(DebugContextEvent event) {
		notify(event, getListeners(null));
		IWorkbenchPart part = event.getDebugContextProvider().getPart();
		if (part != null) {
			notify(event, getListeners(part));
		}
		notify(event, getPostListeners(null));
		if (part != null) {
			notify(event, getPostListeners(part));
		}
	}
	
	protected void notify(final DebugContextEvent event, ListenerList list) {
		if (list != null) {
			Object[] listeners = list.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				final IDebugContextListener listener = (IDebugContextListener) listeners[i];
				SafeRunner.run(new ISafeRunnable() {
					public void run() throws Exception {
						listener.debugContextChanged(event);
					}
					public void handleException(Throwable exception) {
						DebugUIPlugin.log(exception);
					}
				});
			}
		}
	}
	
	protected ListenerList getListeners(IWorkbenchPart part) {
		String id = null;
		if (part != null) {
			id = part.getSite().getId();
		}
		return (ListenerList) fListenersByPartId.get(id);
	}
	
	protected ListenerList getPostListeners(IWorkbenchPart part) {
		String id = null;
		if (part != null) {
			id = part.getSite().getId();
		}
		return (ListenerList) fPostListenersByPartId.get(id);
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextService#addDebugContextListener(org.eclipse.debug.ui.contexts.IDebugContextListener, java.lang.String)
	 */
	public synchronized void addDebugContextListener(IDebugContextListener listener, String partId) {
		ListenerList list = (ListenerList) fListenersByPartId.get(partId);
		if (list == null) {
			list = new ListenerList();
			fListenersByPartId.put(partId, list);
		}
		list.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextService#removeDebugContextListener(org.eclipse.debug.ui.contexts.IDebugContextListener, java.lang.String)
	 */
	public void removeDebugContextListener(IDebugContextListener listener, String partId) {
		ListenerList list = (ListenerList) fListenersByPartId.get(partId);
		if (list != null) {
			list.remove(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextService#getActiveContext(java.lang.String)
	 */
	public ISelection getActiveContext(String partId) {
		IDebugContextProvider provider = (IDebugContextProvider) fProvidersByPartId.get(partId);
		if (provider != null) {
			return provider.getActiveContext();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextService#getActiveContext()
	 */
	public ISelection getActiveContext() {
		IDebugContextProvider activeProvider = getActiveProvider();
		if (activeProvider != null) {
			return activeProvider.getActiveContext();
		}
		return null;
	}
	
	/**
	 * Returns the active provider or <code>null</code>
	 * 
	 * @return active provider or <code>null</code>
	 */
	protected IDebugContextProvider getActiveProvider() {
		if (!fProviders.isEmpty()) {
			return (IDebugContextProvider)fProviders.get(0);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partActivated(IWorkbenchPartReference partRef) {
		IDebugContextProvider provider = (IDebugContextProvider) fProvidersByPartId.get(partRef.getId());
		if (provider != null) {
			int index = fProviders.indexOf(provider);
			if (index > 0) {
				fProviders.remove(index);
				fProviders.add(0, provider);
				notify(provider);
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partBroughtToTop(IWorkbenchPartReference partRef) {		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public synchronized void partClosed(IWorkbenchPartReference partRef) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partDeactivated(IWorkbenchPartReference partRef) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partOpened(IWorkbenchPartReference partRef) {		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partHidden(IWorkbenchPartReference partRef) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partVisible(IWorkbenchPartReference partRef) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partInputChanged(IWorkbenchPartReference partRef) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextEventListener#contextEvent(org.eclipse.debug.internal.ui.contexts.provisional.DebugContextEvent)
	 */
	public void debugContextChanged(DebugContextEvent event) {
		if (!fProviders.isEmpty()) {
			IDebugContextProvider provider = (IDebugContextProvider) fProviders.get(0);
			if (provider == event.getDebugContextProvider()) {
				notify(event);
			}
		}	
	}

}
