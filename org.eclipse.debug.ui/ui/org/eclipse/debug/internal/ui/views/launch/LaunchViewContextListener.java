/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.ContextEvent;
import org.eclipse.ui.contexts.ContextManagerFactory;
import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IContextListener;
import org.eclipse.ui.contexts.IMutableContextManager;
import org.eclipse.ui.contexts.NotDefinedException;

/**
 * A context listener which automatically opens/closes/activates views in
 * response to debug context changes.
 */
public class LaunchViewContextListener implements IContextListener, IPartListener, IPageListener, IPerspectiveListener {
	
	public static final String ID_CONTEXT_VIEW_BINDINGS= "contextViewBindings"; //$NON-NLS-1$
	public static final String ID_DEBUG_MODEL_CONTEXT_BINDINGS= "debugModelContextBindings"; //$NON-NLS-1$
	public static final String ATTR_CONTEXT_ID= "contextId"; //$NON-NLS-1$
	public static final String ATTR_VIEW_ID= "viewId"; //$NON-NLS-1$
	public static final String ATTR_DEBUG_MODEL_ID= "debugModelId"; //$NON-NLS-1$
	public static final String ATTR_AUTO_OPEN= "autoOpen"; //$NON-NLS-1$
	public static final String ATTR_AUTO_CLOSE= "autoClose"; //$NON-NLS-1$
	/**
	 * Attributes used to persist which views the user has manually opened/closed
	 */
	private static final String ATTR_VIEWS_TO_NOT_OPEN = "viewsNotToOpen"; //$NON-NLS-1$
	private static final String ATTR_VIEWS_TO_NOT_CLOSE = "viewsNotToClose"; //$NON-NLS-1$
	
	private Map modelsToContext= new HashMap();
	/**
	 * A mapping of context IDs (Strings) to a collection
	 * of context-view bindings (IConfigurationElements).
	 */
	private Map contextViews= new HashMap();
	private IMutableContextManager contextManager= ContextManagerFactory.getMutableContextManager();
	/**
	 * Collection of all views that might be opened or closed automatically.
	 * This collection starts out containing all views associated with a context.
	 * As views are manually opened and closed by the user, they're removed.
	 */
	private Set managedViewIds= new HashSet();
	private Set viewIdsToNotOpen= new HashSet();
	private Set viewIdsToNotClose= new HashSet();
	
	public LaunchViewContextListener() {
		loadDebugModelContextExtensions();
		loadContextToViewExtensions(true);
		IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			window.addPageListener(this);
			window.addPerspectiveListener(this);
		}
	}
	
	/**
	 * Loads extensions which map context ids to views. This information
	 * is used to open the appropriate views when a context is activated. 
	 */
	private void loadContextToViewExtensions(boolean reloadContextMappings) {
		IExtensionPoint extensionPoint = DebugUIPlugin.getDefault().getDescriptor().getExtensionPoint(ID_CONTEXT_VIEW_BINDINGS);
		IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
		for (int i = 0; i < configurationElements.length; i++) {
			IConfigurationElement element = configurationElements[i];
			String viewId = element.getAttribute(ATTR_VIEW_ID);
			if (reloadContextMappings) {
				String contextId = element.getAttribute(ATTR_CONTEXT_ID);
				if (contextId == null || viewId == null) {
					continue;
				}
				IContext context= contextManager.getContext(contextId);
				context.addContextListener(this);
				List elements= (List) contextViews.get(contextId);
				if (elements == null) {
					elements= new ArrayList();
					contextViews.put(contextId, elements);
				}
				elements.add(element);
			}
			managedViewIds.add(viewId);
			String autoOpen= element.getAttribute(ATTR_AUTO_OPEN);
			if (autoOpen != null && !Boolean.valueOf(autoOpen).booleanValue()) {
				viewIdsToNotOpen.add(viewId);
			}
			String autoClose= element.getAttribute(ATTR_AUTO_CLOSE);
			if (autoClose != null && !Boolean.valueOf(autoClose).booleanValue()) {
				viewIdsToNotClose.add(viewId);
			}
		}
	}

	/**
	 * Loads the extensions which map debug model identifiers
	 * to context ids. This information is used to activate the
	 * appropriate context when a debug element is selected.
	 */
	private void loadDebugModelContextExtensions() {
		IExtensionPoint extensionPoint = DebugUIPlugin.getDefault().getDescriptor().getExtensionPoint(ID_DEBUG_MODEL_CONTEXT_BINDINGS);
		IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
		for (int i = 0; i < configurationElements.length; i++) {
			IConfigurationElement element = configurationElements[i];
			String modelIdentifier = element.getAttribute(ATTR_DEBUG_MODEL_ID);
			String context = element.getAttribute(ATTR_CONTEXT_ID);
			if (modelIdentifier != null && context != null) {
				modelsToContext.put(modelIdentifier, context);
			}
		}
	}
	
	/**
	 * Returns the context id associated with the given debug
	 * model identifier as specified via extension or <code>null</code>
	 * if none.
	 * @param debugModelIdentifier the debug model identifier or <code>null</code>.
	 * @return the context id associated with the given debug model
	 * 	identifier or <code>null</code> if none.
	 */
	public String getDebugModelContext(String debugModelIdentifier) {
		if (debugModelIdentifier == null) {
			return null;
		}
		return (String) modelsToContext.get(debugModelIdentifier);
	}
	
	/**
	 * Returns the context manager that this listener listens to.
	 * @return the context manager that this listener listens to
	 */
	public IMutableContextManager getContextManager() {
		return contextManager;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.contexts.IContextListener#contextChanged(org.eclipse.ui.contexts.ContextEvent)
	 */
	public void contextChanged(ContextEvent contextEvent) {
		if (contextEvent.hasEnabledChanged()) {
			IContext context = contextEvent.getContext();
			if (context.isEnabled()) {
				contextActivated(context.getId());
			}
		}
	}
	
	/**
	 * The context with the given ID has been activated.
	 * If the given context ID is the same as the current
	 * context, do nothing. Otherwise, activate the appropriate
	 * views.
	 * 
	 * @param contextId the ID of the context that has been
	 * 	activated
	 */
	public void contextActivated(String contextId) {
		List configurationElements= getConfigurationElements(contextId);
		if (configurationElements.isEmpty()) {
			return;
		}
		IWorkbenchPage page= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		page.removePartListener(this); // Stop listening before closing/opening/activating views
		List viewsToActivate= new ArrayList();
		List viewsToOpen= new ArrayList();
		Iterator iterator= configurationElements.iterator();
		while (iterator.hasNext()) {
			IConfigurationElement element = (IConfigurationElement) iterator.next();
			String viewId= element.getAttribute(ATTR_VIEW_ID);
			if (viewId == null) {
				continue;
			}
			IViewPart view = page.findView(viewId);
			if (view != null) {
				viewsToActivate.add(view);
			} else {
				// Don't open automatically if specified not to.
				if (!viewIdsToNotOpen.contains(viewId)) {
					viewsToOpen.add(viewId);
				}
			}
		}
		if (!viewsToActivate.isEmpty() || !viewsToOpen.isEmpty()) {
			IViewReference[] references = page.getViewReferences();
			for (int i = 0; i < references.length; i++) {
				IViewReference reference = references[i];
				String viewId= reference.getId();
				IViewPart view= reference.getView(true);
				if (view == null || !managedViewIds.contains(viewId)) {
					// Only close views that are associated with another context and which
					// the user hasn't manually opened.
					continue;
				}
				if (!viewsToActivate.contains(view) && !viewsToOpen.contains(viewId) &&
						!viewIdsToNotClose.contains(viewId)) {
					// Close all views that aren't applicable, unless specified not to
					page.hideView(view);
				}
			}
		}
		iterator= viewsToOpen.iterator();
		while (iterator.hasNext()) {
			String viewId = (String) iterator.next();
			try {
				viewsToActivate.add(page.showView(viewId, null, IWorkbenchPage.VIEW_CREATE));
			} catch (PartInitException e) {
				DebugUIPlugin.log(e.getStatus());
			}
		}
		// Until we have an API to open views "underneath" (bug 50618), first iterate
		// to remove views using the stack information, then open views, then activate.
		// When the "open underneath" API is provided, only iterate once.
		ListIterator listIterator= viewsToActivate.listIterator();
		while (listIterator.hasNext()) {
			boolean activate= true;
			IViewPart view = (IViewPart) listIterator.next();
			IViewPart[] stackedViews = page.getViewStack(view);
			if (stackedViews == null) {
				continue;
			}
			for (int i = 0; i < stackedViews.length; i++) {
				IViewPart stackedView= stackedViews[i];
				if (view != stackedView && viewsToActivate.contains(stackedView) && page.isPartVisible(stackedView)) {
					// If this view is currently obscured by an appropriate view that is already visible,
					// don't activate it (let the visible view stay visible).
					activate= false;
					break;
				}
			}
			if (activate) {
				page.bringToTop(view);
			}
		}
		page.addPartListener(this); // Start listening again for close/open
	}
	
	/**
	 * Lists the contextViews configuration elements for the
	 * given context ID and all its parent context IDs. The
	 * list only contains one configuration element per view
	 * such that if a child context provides a binding for a view
	 * it will override any bindings provided for that same view by
	 * parent contexts.
	 * 
	 * @param contextId the context ID
	 * @return the configuration elements for the given context ID and
	 * 	all parent context IDs. 
	 */
	private List getConfigurationElements(String contextId) {
		// Collection of view ids for which configuration
		// elements have been found.
		List configuredViewIds= new ArrayList();
		List allConfigurationElements= new ArrayList();
		while (contextId != null) {
			List configurationElements= (List) contextViews.get(contextId);
			if (configurationElements != null) {
				ListIterator iter= configurationElements.listIterator();
				while (iter.hasNext()) {
					// Remove any configuration elements for views that
					// are already "bound" by a configuration element.
					// This allows child contexts to override parent
					// bindings.
					IConfigurationElement element= (IConfigurationElement) iter.next();
					String viewId = element.getAttribute(ATTR_VIEW_ID);
					if (viewId != null) {
						if (configuredViewIds.contains(viewId)) {
							iter.remove();
						}
						configuredViewIds.add(viewId);
					}
				}
				allConfigurationElements.addAll(configurationElements);
			}
			IContext context = contextManager.getContext(contextId);
			if (context != null) {
				try {
					contextId= context.getParentId();
				} catch (NotDefinedException e) {
					contextId= null;
				}
			}
		}
		return allConfigurationElements;		 
	}

	/**
	 * When the user closes a view, do not automatically
	 * open that view in the future.
	 */
	public void partClosed(IWorkbenchPart part) {
		if (part instanceof IViewPart) {
			String id = ((IViewPart) part).getViewSite().getId();
			if (managedViewIds.remove(id)) {
				viewIdsToNotOpen.add(id);
			}
		}
	}
	/**
	 * When the user opens a view, do not automatically
	 * close that view in the future.
	 */
	public void partOpened(IWorkbenchPart part) {
		if (part instanceof IViewPart) {
			String id = ((IViewPart) part).getViewSite().getId();
			if (managedViewIds.remove(id)) {
				viewIdsToNotClose.add(id);
			}
		}
	}
	public void partActivated(IWorkbenchPart part) {
	}
	public void partBroughtToTop(IWorkbenchPart part) {
	}
	public void partDeactivated(IWorkbenchPart part) {
	}

	/**
	 * When a workbench page is opened, start listening to
	 * part notifications.
	 */
	public void pageActivated(IWorkbenchPage page) {
		page.addPartListener(this);
	}
	/**
	 * When a workbench page is closed, stop listening to
	 * part notifications.
	 */
	public void pageClosed(IWorkbenchPage page) {
		page.removePartListener(this);
	}
	public void pageOpened(IWorkbenchPage page) {
	}

	/**
	 * Persist the view ids that the user has manually
	 * opened/closed so that we continue to not automatically
	 * open/close them.
	 * @param memento a memento to save the state into
	 */
	public void saveState(IMemento memento) {
		StringBuffer views= new StringBuffer();
		Iterator iter= viewIdsToNotOpen.iterator();
		while(iter.hasNext()) {
			views.append((String) iter.next()).append(',');
		}
		if (views.length() > 0) {
			memento.putString(ATTR_VIEWS_TO_NOT_OPEN, views.toString());
			views= new StringBuffer();
		}
		iter= viewIdsToNotClose.iterator();
		while(iter.hasNext()) {
			views.append((String) iter.next()).append(',');
		}
		if (views.length() > 0) {
			memento.putString(ATTR_VIEWS_TO_NOT_CLOSE, views.toString());
		}
	}
	
	public void init(IMemento memento) {
		initViewCollection(memento, ATTR_VIEWS_TO_NOT_CLOSE);
		initViewCollection(memento, ATTR_VIEWS_TO_NOT_OPEN);
	}
	
	/**
	 * Loads a collection of view ids from the given memento keyed to
	 * the given attribute, and stores them in the given collection
	 * @param memento the memento
	 * @param attribute the attribute of the view ids
	 */
	private void initViewCollection(IMemento memento, String attribute) {
		String views = memento.getString(attribute);
		if (views == null) {
			return;
		}
		int startIndex= 0;
		int endIndex= views.indexOf(',');
		if (endIndex == -1) {
			endIndex= views.length();
		}
		while (startIndex < views.length() - 1) {
			String viewId= views.substring(startIndex, endIndex);
			if (viewId.length() > 0) {
				viewIdsToNotOpen.add(viewId);
			}
			startIndex= endIndex + 1;
			endIndex= views.indexOf(',', startIndex);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
	 */
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
		if (changeId.equals(IWorkbenchPage.CHANGE_RESET)) {
			//TODO: When the workbench adds a reset_end flag, remove
			// the part listener on reset, then add it back on reset_end.
			managedViewIds.clear();
			viewIdsToNotClose.clear();
			viewIdsToNotOpen.clear();
			loadContextToViewExtensions(false);
			contextManager.setEnabledContextIds(new HashSet());
		}
	}
}