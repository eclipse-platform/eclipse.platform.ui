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
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugModelProvider;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IActivityPatternBinding;
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
 * response to debug context changes and automatically enables activities
 * as appropriate.
 * 
 * The context listener updates for selection changes in the LaunchView,
 * enabling/disabling contexts and enabling activities based on the
 * org.eclipse.debug.ui.debugModelContextBindings and
 * org.eclipse.ui.activities extension points.
 * 
 * Activity pattern bindings with patterns of the form:
 *   <debug model identifier>/debugModel
 * are treated as bindings between a debug model and an activity. When
 * an element with the specified debug model identifier is selected,
 * the specified activity will be enabled.
 */
public class LaunchViewContextListener implements IContextManagerListener {

	public static final String DEBUG_MODEL_ACTIVITY_SUFFIX = "debugModel"; //$NON-NLS-1$
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
	 */
	private Set managedViewIds= new HashSet();
	/**
	 * Collection of views which have been manually closed by the
	 * user. Views which are in this collection should not be
	 * automatically opened.
	 */
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
	public static final String DEBUG_CONTEXT= "org.eclipse.debug.ui.debugging"; //$NON-NLS-1$
	/**
	 * String preference specifying which views should not be
	 * automatically opened by the launch view.
	 * The value is a comma-separated list of view identifiers.
	 * 
	 * @since 3.0
	 */
	public static final String PREF_VIEWS_TO_NOT_OPEN= IDebugUIConstants.PLUGIN_ID + ".views_to_not_open"; //$NON-NLS-1$
	/**
	 * String preference specifying which views have been
	 * automatically opened by the launch view. Only views which
	 * have been automatically opened will be automatically closed.
	 * The value is a comma-separated list of view identifiers.
	 * 
	 * @since 3.0
	 */
	public static final String PREF_OPENED_VIEWS= IDebugUIConstants.PLUGIN_ID + ".opened_views"; //$NON-NLS-1$
	/**
	 * The collection of context ids which were most recently enabled. 
	 */
	private List lastEnabledIds= new ArrayList();
	/**
	 * Boolean flag controlling whether or not this listener is
	 * tracking part changes. This is necessary since this class
	 * doesn't implement its own listener, but it informed of
	 * perspective change events by the LaunchView.
	 */
	private boolean fIsTrackingPartChanges;
	/**
	 * Collection of perspectives in which views should be
	 * automatically opened and closed.
	 */
	private List fAutoManagePerspectives= new ArrayList();
	
	/**
	 * Creates a fully initialized context listener.
	 * 
	 * @param view a fully initialized launch view
	 */
	public LaunchViewContextListener(LaunchView view) {
		launchView= view;
		loadTrackViews();
		loadDebugModelContextExtensions();
		loadDebugModelActivityExtensions();
		loadContextToViewExtensions(true);
		loadOpenedViews();
		loadViewsToNotOpen();
		loadAutoManagePerspectives();
		PlatformUI.getWorkbench().getContextSupport().getContextManager().addContextManagerListener(this);
	}
	
	/**
	 * Loads extensions which map context ids to views. This information
	 * is used to open the appropriate views when a context is activated. 
	 */
	private void loadContextToViewExtensions(boolean reloadContextMappings) {
		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), ID_CONTEXT_VIEW_BINDINGS);
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
		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), ID_DEBUG_MODEL_CONTEXT_BINDINGS);
		IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
		for (int i = 0; i < configurationElements.length; i++) {
			IConfigurationElement element = configurationElements[i];
			String modelIdentifier = element.getAttribute(ATTR_DEBUG_MODEL_ID);
			String contextId = element.getAttribute(ATTR_CONTEXT_ID);
			if (modelIdentifier != null && contextId != null) {
				List contextIds = (List) modelsToContexts.get(modelIdentifier);
				if (contextIds == null) {
					contextIds = new ArrayList();
					modelsToContexts.put(modelIdentifier, contextIds);
				}
				contextIds.add(contextId);
			}
		}
	}
	
	/**
	 * Loads the extensions which map debug model identifiers
	 * to activity ids. This information is used to activate the
	 * appropriate activities when a debug element is selected.
	 */
	private void loadDebugModelActivityExtensions() {
		IActivityManager activityManager = PlatformUI.getWorkbench().getActivitySupport().getActivityManager();
		Set activityIds = activityManager.getDefinedActivityIds();
		Iterator activityIterator = activityIds.iterator();
		while (activityIterator.hasNext()) {
			String activityId= (String) activityIterator.next();
			IActivity activity = activityManager.getActivity(activityId);
			if (activity != null) {
				Set patternBindings = activity.getActivityPatternBindings();
				Iterator patternIterator= patternBindings.iterator();
				while (patternIterator.hasNext()) {
					IActivityPatternBinding patternBinding= (IActivityPatternBinding) patternIterator.next();
					String pattern = patternBinding.getPattern().pattern();
					int index = pattern.lastIndexOf(DEBUG_MODEL_ACTIVITY_SUFFIX);
					if (index > 0) {
						String debugModel= pattern.substring(0, index - 1);
						List ids = (List)modelsToActivities.get(debugModel);
						if (ids == null) {
							ids = new ArrayList();
							modelsToActivities.put(debugModel, ids);
						}
						ids.add(activityId);
					}
				}
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
	 * Persist the collection of views to not automatically open.
	 */
	private void saveViewsToNotOpen() {
		saveViewCollection(LaunchViewContextListener.PREF_VIEWS_TO_NOT_OPEN, viewIdsToNotOpen);
	}
	
	/**
	 * Persist the collection of views which have been automatically
	 * opened.
	 */
	private void saveOpenedViews() {
		saveViewCollection(LaunchViewContextListener.PREF_OPENED_VIEWS, openedViewIds);
	}

	/**
	 * Persist the view identifiers that the user has manually
	 * opened/closed so that we continue to not automatically
	 * open/close them.
	 * @param attribute the preference key in which to store the
	 *  view id collection
	 * @param collection the view identifier collection
	 */
	public void saveViewCollection(String attribute, Set collection) {
		StringBuffer views= new StringBuffer();
		Iterator iter= collection.iterator();
		while (iter.hasNext()) {
			views.append((String) iter.next()).append(',');
		}
		if (views.length() > 0) {
			IPreferenceStore preferenceStore = DebugUITools.getPreferenceStore();
			preferenceStore.removePropertyChangeListener(launchView);
			preferenceStore.setValue(attribute, views.toString());
			preferenceStore.addPropertyChangeListener(launchView);
		}
	}
	
	/**
	 * Load the collection of views to not open.
	 */
	public void loadViewsToNotOpen() {
		loadViewCollection(ATTR_VIEWS_TO_NOT_OPEN, viewIdsToNotOpen);
	}
	
	/**
	 * Load the collection of views that have been automatically
	 * opened.
	 */
	public void loadOpenedViews() {
		loadViewCollection(ATTR_OPENED_VIEWS, openedViewIds);
	}
	
	/**
	 * Loads a collection of view ids from the preferences keyed to
	 * the given attribute, and stores them in the given collection
	 * 
	 * @param attribute the attribute of the view ids
	 * @param collection the collection to store the view ids into.
	 */
	public void loadViewCollection(String attribute, Set collection) {
		collection.clear();
		String views = DebugUITools.getPreferenceStore().getString(attribute);
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
		Set enabled = getNewlyEnabledContexts(contextManagerEvent);
		Set disabled = getNewlyDisabledContexts(contextManagerEvent);
		contextEnabled(enabled);
		contextsDisabled(disabled);
	}
	
	private Set getNewlyEnabledContexts(ContextManagerEvent contextManagerEvent) {
	    Set set = new HashSet(contextManagerEvent.getContextManager().getEnabledContextIds());
	    set.removeAll(contextManagerEvent.getPreviouslyEnabledContextIds());
	    return set;
	}

	private Set getNewlyDisabledContexts(ContextManagerEvent contextManagerEvent) {
	    Set set = new HashSet(contextManagerEvent.getPreviouslyEnabledContextIds());
	    set.removeAll(contextManagerEvent.getContextManager().getEnabledContextIds());
	    return set;
	}

	/**
	 * The context with the given ID has been enabled.
	 * Activate the appropriate views.
	 * 
	 * @param contextId the ID of the context that has been
	 * 	enabled
	 */
	public void contextEnabled(Set contextIds) {
		if (!isAutoManageViews()) {
			return;
		}
		IWorkbenchPage page= getPage();
		// We ignore the "Debugging" context since we use it
		// to provide a base set of views for other context
		// bindings to inherit. If we don't ignore it, we'll
		// end up opening those views whenever a debug session
		// starts, which is not the desired behavior.
		contextIds.remove(DEBUG_CONTEXT);
		if (page == null || contextIds.size() == 0) {
			return;
		}
		Set viewsToShow= new HashSet();
		Set viewsToOpen= new HashSet();
		computeViewActivation(contextIds, viewsToOpen, viewsToShow);
		fIsTrackingPartChanges= false;
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
		if (!viewsToOpen.isEmpty()) {
			saveOpenedViews();
		}
		iterator= viewsToShow.iterator();
		while (iterator.hasNext()) {
			boolean activate= true;
			IViewPart view = (IViewPart) iterator.next();
			IViewPart[] stackedViews = page.getViewStack(view);
			if (stackedViews == null) {
				continue;
			}
			// For each applicable view, iterate through the view stack.
			// If we find that view before any other applicable views,
			// show it. Otherwise, don't.
			for (int i = 0; i < stackedViews.length; i++) {
				IViewPart stackedView= stackedViews[i];
				if (view == stackedView) {
					break;
				} else if (viewsToShow.contains(stackedView)) {
					// If this view is below an appropriate view, don't show it
					activate= false;
					break;
				}
			}
			if (activate) {
				page.bringToTop(view);
			}
		}
		loadTrackViews();
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
	private void computeViewActivation(Set contextIds, Set viewIdsToOpen, Set viewIdsShow) {
		IWorkbenchPage page = getPage();
		if (page == null) {
			return;
		}
		Iterator contexts = contextIds.iterator();
		while (contexts.hasNext()) {
			String contextId = (String) contexts.next();
			Iterator configurationElements= getConfigurationElements(contextId).iterator();
			while (configurationElements.hasNext()) {
				IConfigurationElement element = (IConfigurationElement) configurationElements.next();
				String viewId= getViewId(element);
				if (viewId == null) {
					continue;
				}
				IViewReference reference = page.findViewReference(viewId);
				if (reference != null && reference.isFastView()) {
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
	public void contextsDisabled(Set contexts) {
		IWorkbenchPage page= getPage();
		if (page == null || contexts.size() == 0 || !isAutoManageViews()) {
			return;
		}
		Set viewsToClose= getViewIdsToClose(contexts);
		if (viewsToClose.isEmpty()) { 
			return;
		}
		fIsTrackingPartChanges= false;
		Iterator iter= viewsToClose.iterator();
		while (iter.hasNext()) {
			String viewId= (String) iter.next();
			IViewReference view = page.findViewReference(viewId);
			if (view != null) {
				page.hideView(view);
				openedViewIds.remove(viewId);
			}
		}
		saveOpenedViews();
		loadTrackViews();
	}
	
	/**
	 * Returns a collection of view IDs which should be closed
	 * when the given context IDs are disabled.
	 * 
	 * @param contextIds the context identifiers
	 * @return the identifiers of the views which should be closed
	 *  when the given contexts disable 
	 */
	public Set getViewIdsToClose(Set contextIds) {
		Set viewIdsToClose= new HashSet();
		Set viewIdsToKeepOpen= getViewIdsForEnabledContexts();
		Iterator contexts = contextIds.iterator();
		while (contexts.hasNext()) {
			String contextId = (String) contexts.next();
			List list = getConfigurationElements(contextId);
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
			if (contextId.equals(DEBUG_CONTEXT)) {
				// Ignore the "Debugging" context. See comment in contextEnabled(...)
				continue;
			}
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
		ILaunch launch= getLaunch(selection);
		if (launch == null) {
			return;
		}
		String[] modelIds= getDebugModelIdsForSelection(selection);
		enableContexts(getContextsForModels(modelIds), launch);
		enableActivities(getActivitiesForModels(modelIds));
	}

	/**
	 * Returns the ILaunch associated with the given selection or
	 * <code>null</code> if none can be determined.
	 * 
	 * @param selection the selection or <code>null</code>
	 * @return the ILaunch associated with the given selection or <code>null</code>
	 */
	protected static ILaunch getLaunch(Object selection) {
		ILaunch launch= null;
		if (selection instanceof ILaunch) {
			launch= (ILaunch) selection;
		} else if (selection instanceof IDebugElement) {
			launch= ((IDebugElement) selection).getLaunch();
		} else if (selection instanceof IProcess) {
			launch= ((IProcess) selection).getLaunch();
		}
		return launch;
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
				ids.add(DEBUG_CONTEXT);
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
			if (enabledContexts.contains(contextId) && !lastEnabledIds.contains(contextId)) {
				// If a context is already enabled, submitting it won't
				// generate a callback from the workbench. So we inform
				// our context listener ourselves.
				// This covers the case where the user is selecting
				// among elements from several enabled contexts.
				contextsAlreadyEnabled.add(contextId);
			}
		}
		lastEnabledIds.clear();
		lastEnabledIds.addAll(contextIds);
		submitContexts(contextIds, launch);
		contextEnabled(contextsAlreadyEnabled);
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
	 * Returns whether this view automatically opens and closes
	 * views based on contexts
	 * @return whether or not this view automatically manages
	 * views based on contexts
	 */
	private boolean isAutoManageViews() {
		IWorkbenchPage page = launchView.getViewSite().getWorkbenchWindow().getActivePage();
		if (page != null) {
			IPerspectiveDescriptor descriptor = page.getPerspective();
			if (descriptor != null) {
				return fAutoManagePerspectives.contains(descriptor.getId());
			}
		}
		return false;
	}
	
	/**
	 * Returns the workbench page containing the launch view.
	 * 
	 * @return the workbench page containing the launch view
	 */
	public IWorkbenchPage getPage() {
		return launchView.getSite().getPage();
	}
	
	/**
	 * Notifies this listener that the given perspective change
	 * has occurred.
	 * 
	 * Don't listen to part open/close notifications during reset.
	 */
	public void perspectiveChanged(IWorkbenchPage page, String changeId) {
		if (changeId.equals(IWorkbenchPage.CHANGE_RESET)) {
			fIsTrackingPartChanges= false;
		} else if (changeId.equals(IWorkbenchPage.CHANGE_RESET_COMPLETE)) {
			loadTrackViews();
		}
	}
	
	/**
	 * Notifies this listener that the given perspective change
	 * has occurred.
	 * 
	 * When a part is opened/closed, do not close/open it automatically. 
	 */
	public void perspectiveChanged(IWorkbenchPage page, IWorkbenchPartReference ref, String changeId) {
		if (!fIsTrackingPartChanges) {
			return;
		}
		if (IWorkbenchPage.CHANGE_VIEW_HIDE.equals(changeId) && (ref instanceof IViewReference)) {
			String id = ((IViewReference) ref).getId();
			if (managedViewIds.contains(id)) {
				viewIdsToNotOpen.add(id);
				saveViewsToNotOpen();
			}
			openedViewIds.remove(id);
			saveOpenedViews();
		} else if (IWorkbenchPage.CHANGE_VIEW_SHOW.equals(changeId) && ref instanceof IViewReference) {
			String id = ((IViewReference) ref).getId();
			openedViewIds.remove(id);
			saveOpenedViews();
		}
	}
	
	/**
	 * Reads the preference specifying whether this view automatically
	 * tracks views being opened and closed for the purpose of not
	 * automatically managing those views once they've been opened/closed
	 * manually.
	 */
	public void loadTrackViews() {
		fIsTrackingPartChanges= DebugUITools.getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_TRACK_VIEWS);
	}
	
	/**
	 * Load the collection of perspectives in which view
	 * management will occur from the preference store.
	 */
	private void loadAutoManagePerspectives() {
		String prefString = DebugUIPlugin.getDefault().getPreferenceStore().getString(IDebugUIConstants.PREF_MANAGE_VIEW_PERSPECTIVES);
		fAutoManagePerspectives= parseList(prefString);
	}
	
	/**
	 * Reloaded the collection of view management perspectives
	 * and updates (potentially opening views) for the given
	 * selection.
	 */
	public void reloadAutoManagePerspectives(Object selection) {
		// Remove the context ids associated with the current selection
		// so that updateForSelection(...) will open views
		// as appropriate given the new view management settings.
		String[] modelIds = getDebugModelIdsForSelection(selection);
		List contextIds = getContextsForModels(modelIds);
		lastEnabledIds.removeAll(contextIds);
		
		loadAutoManagePerspectives();
		updateForSelection(selection);
	}
	
	/**
	 * Parses the comma separated string into a list of strings
	 * 
	 * @return list
	 */
	public static List parseList(String listString) {
		List list = new ArrayList(10);
		StringTokenizer tokenizer = new StringTokenizer(listString, ","); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			list.add(token);
		}
		return list;
	}

	/**
	 * The launch view associated with this context listener has
	 * been disposed. Remove as a context listener.
	 *
	 */
	public void dispose() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getContextSupport().getContextManager().removeContextManagerListener(this);
	}
}