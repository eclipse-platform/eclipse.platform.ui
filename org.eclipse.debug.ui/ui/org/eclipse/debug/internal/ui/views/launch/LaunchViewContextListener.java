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
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.ContextManagerEvent;
import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IContextManagerListener;
import org.eclipse.ui.contexts.NotDefinedException;

/**
 * A context listener which automatically opens/closes/activates views in
 * response to debug context changes.
 */
public class LaunchViewContextListener implements IPartListener2, IPageListener, IPerspectiveListener, IContextManagerListener {
	
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
	private static final String ATTR_OPENED_VIEWS = "viewsNotToClose"; //$NON-NLS-1$
	
	/**
	 * The launch view that this context listener works for
	 */
	private LaunchView launchView;
	
	private Map modelsToContext= new HashMap();
	/**
	 * A mapping of context IDs (Strings) to a collection
	 * of context-view bindings (IConfigurationElements).
	 */
	private Map contextViews= new HashMap();
	
	/**
	 * Collection of all views that might be opened or closed automatically.
	 * This collection starts out containing all views associated with a context.
	 * As views are manually opened and closed by the user, they're removed.
	 */
	private Set managedViewIds= new HashSet();
	private Set viewIdsToNotOpen= new HashSet();
	/**
	 * Collection of views which have been automatically opened.
	 * Only views which are in this collection should be automatically
	 * closed.
	 */
	private Set openedViewIds= new HashSet();
	
	public LaunchViewContextListener(LaunchView view) {
		launchView= view;
		loadDebugModelContextExtensions();
		loadContextToViewExtensions(true);
		IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getContextSupport().getContextManager().addContextManagerListener(this);
		IWorkbenchWindow window= workbench.getActiveWorkbenchWindow();
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
			String viewId = getViewId(element);
			if (reloadContextMappings) {
				String contextId = element.getAttribute(ATTR_CONTEXT_ID);
				if (contextId == null || viewId == null) {
					continue;
				}
				List elements= (List) contextViews.get(contextId);
				if (elements == null) {
					elements= new ArrayList();
					contextViews.put(contextId, elements);
				}
				elements.add(element);
			}
			managedViewIds.add(viewId);
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
		IContextManager contextManager = PlatformUI.getWorkbench().getContextSupport().getContextManager();
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
		iter= openedViewIds.iterator();
		while(iter.hasNext()) {
			views.append((String) iter.next()).append(',');
		}
		if (views.length() > 0) {
			memento.putString(ATTR_OPENED_VIEWS, views.toString());
		}
	}
	
	/**
	 * Restore the persisted collections of views to not close and
	 * views to not open
	 * 
	 * @param memento the memento containing the persisted view IDs
	 */
	public void init(IMemento memento) {
		initViewCollection(memento, ATTR_OPENED_VIEWS, openedViewIds);
		initViewCollection(memento, ATTR_VIEWS_TO_NOT_OPEN, viewIdsToNotOpen);
	}
	
	/**
	 * Loads a collection of view ids from the given memento keyed to
	 * the given attribute, and stores them in the given collection
	 * @param memento the memento
	 * @param attribute the attribute of the view ids
	 * @param collection the collection to store the view ids into.
	 */
	private void initViewCollection(IMemento memento, String attribute, Set collection) {
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
				collection.add(viewId);
			}
			startIndex= endIndex + 1;
			endIndex= views.indexOf(',', startIndex);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.contexts.IContextManagerListener#contextManagerChanged(org.eclipse.ui.contexts.ContextManagerEvent)
	 */
	public void contextManagerChanged(ContextManagerEvent contextManagerEvent) {
		contextActivated(contextManagerEvent.getEnabledContexts());
		contextsDisabled(contextManagerEvent.getDisabledContexts());
	}
	
	/**
	 * The context with the given ID has been submitted for
	 * enablement. Activate the appropriate views.
	 * 
	 * @param contextId the ID of the context that has been
	 * 	submitted for enablement
	 */
	public void contextActivated(String[] contextIds) {
		IWorkbenchPage page= getActiveWorkbenchPage();
		if (page == null || contextIds.length == 0) {
			return;
		}
		Set viewsToShow= new HashSet();
		Set viewsToOpen= new HashSet();
		computeViewActivation(contextIds, viewsToOpen, viewsToShow);
		page.removePartListener(this); // Stop listening before opening/activating views
		Iterator iterator= viewsToOpen.iterator();
		while (iterator.hasNext()) {
			String viewId = (String) iterator.next();
			try {
				IViewPart view = page.showView(viewId, null, IWorkbenchPage.VIEW_CREATE);
				openedViewIds.add(view.getViewSite().getId());
				viewsToShow.add(view);
			} catch (PartInitException e) {
				DebugUIPlugin.log(e.getStatus());
			}
		}
		iterator= viewsToShow.iterator();
		while (iterator.hasNext()) {
			boolean activate= true;
			IViewPart view = (IViewPart) iterator.next();
			IViewPart[] stackedViews = page.getViewStack(view);
			if (stackedViews == null) {
				continue;
			}
			for (int i = 0; i < stackedViews.length; i++) {
				IViewPart stackedView= stackedViews[i];
				if (view != stackedView && viewsToShow.contains(stackedView) && page.isPartVisible(stackedView)) {
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
	 * Compute which views should be automatically opened and which should be
	 * automatically brought to top when the given contexts are enabled.
	 * 
	 * @param contextIds the contexts that have been enabled
	 * @param viewIdsToOpen a Set into which this method can store the
	 *  collection of view identifiers (String) that should be opened
	 * @param viewIdsShow a Set into which this method can store the
	 *  collection of view identifiers (String) that should be brought to top
	 */
	private void computeViewActivation(String[] contextIds, Set viewIdsToOpen, Set viewIdsShow) {
		IWorkbenchPage page = getActiveWorkbenchPage();
		if (page == null) {
			return;
		}
		for (int i = 0; i < contextIds.length; i++) {
			String contextId = contextIds[i];
			Iterator configurationElements= getConfigurationElements(contextId).iterator();
			while (configurationElements.hasNext()) {
				IConfigurationElement element = (IConfigurationElement) configurationElements.next();
				String viewId= getViewId(element);
				if (viewId == null) {
					continue;
				}
				IViewPart view = page.findView(viewId);
				if (view != null) {
					viewIdsShow.add(view);
				} else if (isAutoOpen(element) && !viewIdsToNotOpen.contains(viewId)) {
					// Don't open automatically if specified not to.
					viewIdsToOpen.add(viewId);
				}
			}
		}
	}
	
	/**
	 * The given contexts have been disabled. Close all views
	 * associated with these contexts that aren't associated
	 * with other active contexts.
	 * 
	 * @param contexts
	 */
	public void contextsDisabled(String[] contexts) {
		IWorkbenchPage page= getActiveWorkbenchPage();
		if (page == null || contexts.length == 0) {
			return;
		}
		page.removePartListener(this);
		Set viewsToClose= getViewIdsToClose(contexts);
		Iterator iter= viewsToClose.iterator();
		while (iter.hasNext()) {
			String viewId= (String) iter.next();
			IViewReference view = page.findViewReference(viewId);
			if (view != null) {
				page.hideView(view);
			}
		}
		page.addPartListener(this);
	}
	
	/**
	 * Returns a collection of view IDs which should be closed
	 * when the given context IDs are disabled.
	 * 
	 * @param contextIds the context identifiers
	 * @return the identifiers of the views which should be closed
	 *  when the given contexts disable 
	 */
	public Set getViewIdsToClose(String[] contextIds) {
		Set viewIdsToClose= new HashSet();
		Set viewIdsToKeepOpen= getViewIdsForEnabledContexts();
		for (int i = 0; i < contextIds.length; i++) {
			List list = getConfigurationElements(contextIds[i]);
			Iterator iter = list.iterator();
			while (iter.hasNext()) {
				IConfigurationElement element = (IConfigurationElement) iter.next();
				if (!isAutoClose(element)) {
					continue;
				}
				String viewId = getViewId(element);
				if (viewId == null || !openedViewIds.contains(viewId) || viewIdsToKeepOpen.contains(viewId)) {
					// Don't close views that the user has manually opened or views
					// which are associated with contexts that are still enabled.
					continue;
				}
				viewIdsToClose.add(viewId);
			}
		}
		return viewIdsToClose;
	}
	
	/**
	 * Returns the set of view identifiers that are bound to
	 * contexts which are enabled in the workbench.
	 * 
	 * @return the set of view identifiers bound to enabled contexts
	 */
	protected Set getViewIdsForEnabledContexts() {
		Set viewIds= new HashSet();
		Iterator enabledContexts = PlatformUI.getWorkbench().getContextSupport().getContextManager().getEnabledContextIds().iterator();
		while (enabledContexts.hasNext()) {
			String contextId = (String) enabledContexts.next();
			viewIds.addAll(getApplicableViewIds(contextId));
		}
		return viewIds;
	}
	
	/**
	 * Returns the set of view identifiers that are bound to the
	 * given context.
	 * 
	 * @param contextId the context identifier
	 * @return the set of view identifiers bound to the given context
	 */
	public Set getApplicableViewIds(String contextId) {
		Set viewIds= new HashSet();
		Iterator elements = getConfigurationElements(contextId).iterator();
		while (elements.hasNext()) {
			String viewId = getViewId((IConfigurationElement) elements.next());
			if (viewId != null) {
				viewIds.add(viewId);
			}
		}
		return viewIds;
	}
	
	/**
	 * Returns the view identifier associated with the given extension
	 * element or <code>null</code> if none.
	 * 
	 * @param element the contextViewBinding extension element
	 * @return the view identifier associated with the given element or <code>null</code>
	 */
	public static String getViewId(IConfigurationElement element) {
		return element.getAttribute(ATTR_VIEW_ID);
	}
	
	/**
	 * Returns whether the given configuration element is configured
	 * for automatic view opening. The element's view should be automatically
	 * opened if the autoOpen element is specified as true or if the autoOpen
	 * element is unspecified.
	 * 
	 * @param element the contextViewBinding extension element
	 * @return whether or not given given configuration element's view
	 *  should be automatically opened
	 */
	public static boolean isAutoOpen(IConfigurationElement element) {
		String autoOpen = element.getAttribute(ATTR_AUTO_OPEN);
		return autoOpen == null || Boolean.valueOf(autoOpen).booleanValue();
	}
	
	/**
	 * Returns whether the given configuration element is configured
	 * for automatic view closure. The element's view should be automatically
	 * close if the autoClose element is specified as true or if the autoClose
	 * element is unspecified.
	 * 
	 * @param element the contextViewBinding extension element
	 * @return whether or not given given configuration element's view
	 *  should be automatically closed
	 */
	public static boolean isAutoClose(IConfigurationElement element) {
		String autoClose = element.getAttribute(ATTR_AUTO_CLOSE);
		return autoClose == null || Boolean.valueOf(autoClose).booleanValue();
	}
	
	/**
	 * Returns the active workbench page or <code>null</code>
	 * if none.
	 * 
	 * @return the active workbench page or <code>null</code>
	 */
	public IWorkbenchPage getActiveWorkbenchPage() {
		IWorkbenchWindow window = launchView.getViewSite().getWorkbenchWindow();
		IWorkbenchPage page= null;
		if (window != null) {
			page= window.getActivePage();
		}
		return page;
	}
	
	/**
	 * Reset context state when the perspective is reset
	 */
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
		if (changeId.equals(IWorkbenchPage.CHANGE_RESET)) {
			page.removePartListener(this);
			managedViewIds.clear();
			openedViewIds.clear();
			viewIdsToNotOpen.clear();
			loadContextToViewExtensions(false);
			launchView.removeAllContextSubmissions();
		} else if (changeId.equals(IWorkbenchPage.CHANGE_RESET_COMPLETE)) {
			page.addPartListener(this);
		}
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
	
	/**
	 * When the user opens a view, do not automatically
	 * close that view in the future.
	 */
	public void partOpened(IWorkbenchPartReference ref) {
		if (ref instanceof IViewReference) {
			String id = ((IViewReference) ref).getId();
			managedViewIds.remove(id);
		}
	}
	
	/**
	 * When the user closes a view, do not automatically
	 * open that view in the future.
	 */
	public void partHidden(IWorkbenchPartReference ref) {
		if (ref instanceof IViewReference) {
			String id = ((IViewReference) ref).getId();
			// partHidden is sent whenever the view is made not
			// visible. To tell that the view has been "closed",
			// try to find it.
			if (getActiveWorkbenchPage().findView(id) == null) {
				if (managedViewIds.remove(id)) {
					viewIdsToNotOpen.add(id);
				}
				openedViewIds.remove(id);
			}
		}
	}
	
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
	}
	public void pageOpened(IWorkbenchPage page) {
	}
	public void partActivated(IWorkbenchPartReference ref) {
	}
	public void partBroughtToTop(IWorkbenchPartReference ref) {
	}
	public void partClosed(IWorkbenchPartReference ref) {
	}
	public void partDeactivated(IWorkbenchPartReference ref) {
	}
	public void partVisible(IWorkbenchPartReference ref) {
	}
	public void partInputChanged(IWorkbenchPartReference ref) {
	}
}