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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextProvider;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Context service for a specific window.
 * 
 * @since 3.2
 */
public class DebugWindowContextService implements IDebugContextService, IPartListener2, IDebugContextListener {
	
	private Map fListenersByPartId = new HashMap();
	private Map fProvidersByPartId = new HashMap();
	
	private IWorkbenchWindow fWindow;
	private List fProviders = new ArrayList();

	public DebugWindowContextService(IWorkbenchWindow window) {
		fWindow = window;
		fWindow.getPartService().addPartListener(this);
	}
	
	public void dispose() {
		fWindow.getPartService().removePartListener(this);
		fWindow = null;
	}
	
	protected synchronized void addProvider(IDebugContextProvider provider) {
		IWorkbenchPart part = provider.getPart();
		String id = null;
		if (part != null) {
			id = part.getSite().getId();
		}
		fProvidersByPartId.put(part, id);
		fProviders.add(provider);
		IWorkbenchPart active = null;
		IWorkbenchPage activePage = fWindow.getActivePage();
		if (activePage != null) {
			active = activePage.getActivePart();
		}
		if (fProviders.size() == 1 && (part == null || part.equals(active))) {
			notifyActivated();
		}
		provider.addDebugContextListener(this);
	}
	
	protected synchronized void removeProvider(IDebugContextProvider provider) {
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
				notifyActivated();
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextService#removeDebugContextListener(org.eclipse.debug.ui.contexts.IDebugContextListener)
	 */
	public void removeDebugContextListener(IDebugContextListener listener) {
		removeDebugContextListener(listener, null);
	}
	
	protected void notifyActivated() {
		if (fProviders.isEmpty()) {
			notifyActivated(null, null);
		} else {
			IDebugContextProvider provider = (IDebugContextProvider) fProviders.get(0);
			notifyActivated(provider.getActiveContext(), provider.getPart());
		}
	}
	
	protected void notifyActivated(ISelection context, IWorkbenchPart part) {
		notifyActivated(getListeners(null), context, part);
		if (part != null) {
			notifyActivated(getListeners(part), context, part);
		}
	}
	protected void notifyActivated(ListenerList list, final ISelection context, final IWorkbenchPart part) {
		if (list != null) {
			Object[] listeners = list.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				final IDebugContextListener listener = (IDebugContextListener) listeners[i];
				Platform.run(new ISafeRunnable() {
					public void run() throws Exception {
						listener.contextActivated(context, part);
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
		if (!fProviders.isEmpty()) {
			((IDebugContextProvider)fProviders.get(0)).getActiveContext();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partActivated(IWorkbenchPartReference partRef) {
		Object provider = fProvidersByPartId.get(partRef.getId());
		if (provider != null) {
			int index = fProviders.indexOf(provider);
			if (index > 0) {
				fProviders.remove(index);
				fProviders.add(0, provider);
				notifyActivated();
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
		IDebugContextProvider provider = (IDebugContextProvider) fProvidersByPartId.get(partRef.getId());
		if (provider != null) {
			removeProvider(provider);
		}
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
	 * @see org.eclipse.debug.ui.contexts.IDebugContextListener#contextActivated(java.lang.Object, org.eclipse.ui.IWorkbenchPart)
	 */
	public synchronized void contextActivated(ISelection context, IWorkbenchPart part) {
		if (!fProviders.isEmpty()) {
			IDebugContextProvider provider = (IDebugContextProvider) fProviders.get(0);
			if (provider.getPart() == part) {
				notifyActivated();
			}
		}
	}
	
}
