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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugModelProvider;
import org.eclipse.debug.core.model.IStackFrame;
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
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.contexts.ContextManagerEvent;
import org.eclipse.ui.contexts.EnabledSubmission;
import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IContextManager;
import org.eclipse.ui.contexts.IContextManagerListener;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.contexts.NotDefinedException;

/**
 * A context listener which automatically opens/closes/activates views in
 * response to debug context changes.
 * 
 * The context listener also updates for selection changes in the LaunchView,
 * enabling/disabling contexts and enabling activities based on the
 * org.eclipse.debug.ui.debugModelContextBindings and
 * org.eclipse.debug.ui.debugModelActivityBindings extension points.
 */
public class LaunchViewContextListener implements IPartListener2, IPageListener, IPerspectiveListener, IContextManagerListener {
	
	public static final String ATTR_ACTIVITY_ID = "activityId"; //$NON-NLS-1$
	public static final String ID_DEBUG_MODEL_ACTIVITY_BINDINGS = "debugModelActivityBindings"; //$NON-NLS-1$
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
	/**
	 * A mapping of debug models IDs (String) to a collection
	 * of context IDs (List<String>).
	 */
	private Map modelsToContexts= new HashMap();
	/**
	 * A mapping of debug model IDs (String) to a collection
	 * of activity IDs (List<String>).
	 */
	private Map modelsToActivities= new HashMap();
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
	/**
	 * Map of ILaunch objects to the List of EnabledSubmissions that were
	 * submitted for them.
	 * Key: ILaunch
	 * Value: List <EnabledSubmission>
	 */
	private Map fContextSubmissions= new HashMap();
	
	/**
	 * Creates a fully initialized context listener.
	 * @param view
	 */
	public LaunchViewContextListener(LaunchView view) {
		launchView= view;
		loadDebugModelContextExtensions();
		loadDebugModelActivityExtensions();
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
	 * 
	 * When a context associated with a debug model is enabled, we
	 * also activate all parent contexts. Since the context manager
	 * does not do this automatically, we cache all parent context
	 * identifiers in the modelToContexts map as well
	 */
	private void loadDebugModelContextExtensions() {
		loadDebugModelExtensions(ID_DEBUG_MODEL_CONTEXT_BINDINGS, ATTR_CONTEXT_ID, modelsToContexts);
		// add parent contexts
		Iterator modelIds = modelsToContexts.keySet().iterator();
		IContextManager manager = PlatformUI.getWorkbench().getContextSupport().getContextManager();
		while (modelIds.hasNext()) {
			String modelId = (String) modelIds.next();
			List contexts = (List) modelsToContexts.get(modelId);
			Set allContexts = new HashSet(contexts.size());
			Iterator iterator = contexts.iterator();
			while (iterator.hasNext()) {
				String contextId = (String) iterator.next();
				IContext context = manager.getContext(contextId);
				while (context != null && context.isDefined()) {
					allContexts.add(contextId);
					try {
						contextId = context.getParentId();
						context = null;
						if (contextId != null) {
							context = manager.getContext(contextId);
						}
					} catch (NotDefinedException e) {
						context = null;
					}
				}
			}
			List list = new ArrayList(allContexts.size());
			list.addAll(allContexts);
			modelsToContexts.put(modelId, list);
		}
	}
	
	/**
	 * Loads the extensions which map debug model identifiers
	 * to activity ids. This information is used to activate the
	 * appropriate activities when a debug element is selected.
	 */
	private void loadDebugModelActivityExtensions() {
		loadDebugModelExtensions(ID_DEBUG_MODEL_ACTIVITY_BINDINGS, ATTR_ACTIVITY_ID, modelsToActivities);
	}

	/**
	 * Loads extensions of the given extension point which map
	 * debug model identifiers to some value keyed to the given attributeId.
	 * A collection of these values is stored into the given map, keyed
	 * to the model ids.
	 */
	private void loadDebugModelExtensions(String extensionPointId, String attributeId, Map map) {
		IExtensionPoint extensionPoint = DebugUIPlugin.getDefault().getDescriptor().getExtensionPoint(extensionPointId);
		IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
		for (int i = 0; i < configurationElements.length; i++) {
			IConfigurationElement element = configurationElements[i];
			String modelIdentifier = element.getAttribute(ATTR_DEBUG_MODEL_ID);
			String value = element.getAttribute(attributeId);
			if (modelIdentifier != null && value != null) {
				List values = (List) map.get(modelIdentifier);
				if (values == null) {
					values = new ArrayList();
					map.put(modelIdentifier, values);
				}
				values.add(value);
			}
		}
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
		String[] enabled = getNewlyEnabledContexts(contextManagerEvent);
		String[] disabled = getNewlyDisabledContexts(contextManagerEvent);
		contextActivated(enabled);
		contextsDisabled(disabled);
	}
	
	private String[] getNewlyEnabledContexts(ContextManagerEvent contextManagerEvent) {
	    Set set = new HashSet(contextManagerEvent.getContextManager().getEnabledContextIds());
	    set.removeAll(contextManagerEvent.getPreviouslyEnabledContextIds());
	    return (String[]) set.toArray(new String[set.size()]);
	}

	private String[] getNewlyDisabledContexts(ContextManagerEvent contextManagerEvent) {
	    Set set = new HashSet(contextManagerEvent.getPreviouslyEnabledContextIds());
	    set.removeAll(contextManagerEvent.getContextManager().getEnabledContextIds());
	    return (String[]) set.toArray(new String[set.size()]);
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
	 * Determines the debug context associated with the selected
	 * stack frame's debug model (if any) and activates that
	 * context. This triggers this view's context listener
	 * to automatically open/close/activate views as appropriate.
	 */
	public void updateForSelection(Object selection) {
		ILaunch launch= LaunchView.getLaunch(selection);
		if (launch == null) {
			return;
		}
		String[] modelIds= getDebugModelIdsForSelection(selection);
		enableContexts(getContextsForModels(modelIds), launch);
		enableActivities(getActivitiesForModels(modelIds));
	}
	
	/**
	 * Returns the debug model identifiers associated with the given selection.
	 * 
	 * @param selection the selection
	 * @return the debug model identifiers associated with the given selection
	 */
	protected String[] getDebugModelIdsForSelection(Object selection) {
		if (selection instanceof IAdaptable) {
			IDebugModelProvider modelProvider= (IDebugModelProvider) Platform.getAdapterManager().getAdapter(selection, IDebugModelProvider.class);
			if (modelProvider != null) {
				String[] modelIds= modelProvider.getModelIdentifiers();
				if (modelIds != null) {
					return modelIds;
				}
			}
		}
		if (selection instanceof IStackFrame) {
			return new String[] { ((IStackFrame) selection).getModelIdentifier() };
		}
		return new String[0];
	}
	
	/**
	* Returns the activity identifiers associated with the
	* given model identifiers.
	* 
	* @param modelIds the model identifiers
	* @return the activities associated with the given model identifiers
	*/
	protected List getActivitiesForModels(String[] modelIds) {
		List activityIds= new ArrayList();
		for (int i = 0; i < modelIds.length; i++) {
			List ids= (List) modelsToActivities.get(modelIds[i]);
			if (ids != null) {
				activityIds.addAll(ids);
			}
		}
		return activityIds;
	}
	
	/**
	* Returns the context identifiers associated with the
	* given model identifiers.
	* 
	* @param modelIds the model identifiers
	* @return the contexts associated with the given model identifiers
	*/
	protected List getContextsForModels(String[] modelIds) {
		List contextIds= new ArrayList();
		for (int i = 0; i < modelIds.length; i++) {
			List ids= (List) modelsToContexts.get(modelIds[i]);
			if (ids == null) {
				// seed with base debug context
				ids = new ArrayList();
				ids.add("org.eclipse.debug.ui.debugging"); //$NON-NLS-1$
				modelsToContexts.put(modelIds[i], ids);
			}
			contextIds.addAll(ids);
		}
		return contextIds;
	}
	
	/**
	 * Enables the given activities in the workbench.
	 * 
	 * @param activityIds the activities to enable
	 */
	protected void enableActivities(List activityIds) {
		IWorkbenchActivitySupport activitySupport = PlatformUI.getWorkbench().getActivitySupport();
		Set enabledIds = activitySupport.getActivityManager().getEnabledActivityIds();
		Set idsToEnable= new HashSet();
		Iterator iter= activityIds.iterator();
		while (iter.hasNext()) {
			idsToEnable.add(iter.next());
		}
		if (!idsToEnable.isEmpty()) {
			idsToEnable.addAll(enabledIds);
			activitySupport.setEnabledActivityIds(idsToEnable);
		}
	}
	
	/**
	 * Enable the given contexts for the given launch. Context
	 * IDs which are not currently enabled in the workbench will be
	 * submitted to the workbench. Simulate a context enablement
	 * callback (by calling contextActivated) for contexts that are already enabled so that
	 * their views can be promoted.
	 * 
	 * @param contextIds the contexts to enable
	 * @param launch the launch for which the contexts are being enabled
	 */
	protected void enableContexts(List contextIds, ILaunch launch) {
		if (contextIds.isEmpty()) {
			return;
		}
		Set enabledContexts = PlatformUI.getWorkbench().getContextSupport().getContextManager().getEnabledContextIds();
		Set contextsAlreadyEnabled= new HashSet();
		Iterator iter= contextIds.iterator();
		while (iter.hasNext()) {
			String contextId= (String) iter.next();
			if (enabledContexts.contains(contextId)) {
				// If a context is already enabled, submitting it won't
				// generate a callback from the workbench. So we inform
				// our context listener ourselves.
				contextsAlreadyEnabled.add(contextId);
			}
		}
		submitContexts(contextIds, launch);
		contextActivated((String[]) contextsAlreadyEnabled.toArray(new String[0]));
	}

	/**
	 * Submits the given context IDs to the workbench context support
	 * on behalf of the given launch. When the launch terminates,
	 * the context submissions will be automatically removed.
	 *  
	 * @param contextIds the contexts to submit
	 * @param launch the launch for which the contexts are being submitted
	 */
	protected void submitContexts(List contextIds, ILaunch launch) {
		List submissions = (List) fContextSubmissions.get(launch);
		if (submissions == null) {
			submissions= new ArrayList();
			fContextSubmissions.put(launch, submissions);
		}
		List newSubmissions= new ArrayList();
		Iterator iter= contextIds.iterator();
		while (iter.hasNext()) {
			newSubmissions.add(new EnabledSubmission(null, null, null, (String) iter.next()));
		}
		IWorkbenchContextSupport contextSupport = PlatformUI.getWorkbench().getContextSupport();
		if (!newSubmissions.isEmpty()) {
			contextSupport.addEnabledSubmissions(newSubmissions);
			// After adding the new submissions, remove any old submissions
			// that exist for the same context IDs. This prevents us from
			// building up a ton of redundant submissions.
			List submissionsToRemove= new ArrayList();
			ListIterator oldSubmissions= submissions.listIterator();
			while (oldSubmissions.hasNext()) {
				EnabledSubmission oldSubmission= (EnabledSubmission) oldSubmissions.next();
				String contextId = oldSubmission.getContextId();
				if (contextIds.contains(contextId)) {
					oldSubmissions.remove();
					submissionsToRemove.add(oldSubmission);
				}
			}
			contextSupport.removeEnabledSubmissions(submissionsToRemove);
			submissions.addAll(newSubmissions);
		}
	}
	
	/**
	 * Notifies this view that the given launches have terminated. When a launch
	 * terminates, remove all context submissions associated with it.
	 * 
	 * @param launches the terminated launches
	 */
	protected void launchesTerminated(ILaunch[] launches) {
		List allSubmissions= new ArrayList();
		for (int i = 0; i < launches.length; i++) {
			List submissions= (List) fContextSubmissions.remove(launches[i]);
			if (submissions != null) {
				allSubmissions.addAll(submissions);
			}
		}
		PlatformUI.getWorkbench().getContextSupport().removeEnabledSubmissions(allSubmissions);
	}
	
	/**
	 * Removes all context submissions made by this view.
	 */
	protected void removeAllContextSubmissions() {
		IWorkbenchContextSupport contextSupport= PlatformUI.getWorkbench().getContextSupport();
		List submissions= new ArrayList();
		Iterator iterator = fContextSubmissions.values().iterator();
		while (iterator.hasNext()) {
			submissions.addAll((List) iterator.next());
		}
		contextSupport.removeEnabledSubmissions(submissions);
		fContextSubmissions.clear();
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
			removeAllContextSubmissions();
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