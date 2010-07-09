/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Pawel Piech - Bug 154598: DebugModelContextBindingManager does not use IAdaptable.getAdapter() to retrieve IDebugModelProvider adapter
 *     Pawel Piech - Bug 298648:  [View Management] Race conditions and other issues make view management unreliable. 
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contexts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugModelProvider;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.activities.IActivityPatternBinding;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.progress.UIJob;

/**
 * Manages <code>debugModelContextBindings</code> extensions.
 * <p>
 * As debug contexts are activated, associated <code>org.eclipse.ui.contexts</code>
 * are activated. When a debug session (launch) terminates, the associated contexts
 * are disabled. Debug model activation also triggers assocaited activities.
 * </p>
 * @since 3.2
 */
public class DebugModelContextBindingManager implements IDebugContextListener, ILaunchesListener2, IActivityManagerListener {
	
	/**
	 * Map of debug model identifier to associated contexts as defined
	 * by <code>debugModelContextBindings</code> extensions.
	 */
	private Map fModelToContextIds = new HashMap();
	
	/**
	 * Map of launch objects to enabled model ids
	 */
	private Map fLaunchToModelIds = new HashMap();
	
	/**
	 * Map of launch objects to context activations
	 */
	private Map fLanuchToContextActivations = new HashMap();
	
	/**
	 * A list of activity pattern bindings for debug models. 
	 */
	private List fModelPatternBindings = new ArrayList();
	
	/**
	 * Map of debug model ids to assocaited activity ids.
	 */
	private Map fModelToActivities = new HashMap();
	
	/**
	 * A set of debug model ids for which activities have been enabled.
	 * Cleared when enabled activities change.
	 */
	private Set fModelsEnabledForActivities = new HashSet();
	
	// extension point
	public static final String ID_DEBUG_MODEL_CONTEXT_BINDINGS= "debugModelContextBindings"; //$NON-NLS-1$
	
	// extension point attributes
	public static final String ATTR_CONTEXT_ID= "contextId"; //$NON-NLS-1$
	public static final String ATTR_DEBUG_MODEL_ID= "debugModelId"; //$NON-NLS-1$
	
	// base debug context
	public static final String DEBUG_CONTEXT= "org.eclipse.debug.ui.debugging"; //$NON-NLS-1$
	
	// suffix for debug activities triggered by debug model context binding activation
	private static final String DEBUG_MODEL_ACTIVITY_SUFFIX = "/debugModel"; //$NON-NLS-1$
	
	// singleton manager
	private static DebugModelContextBindingManager fgManager;
	
	private static IContextService fgContextService = (IContextService) PlatformUI.getWorkbench().getAdapter(IContextService.class);
	
	public static DebugModelContextBindingManager getDefault() {
		if (fgManager == null) {
			fgManager = new DebugModelContextBindingManager();
		}
		return fgManager;
	}
	
	private DebugModelContextBindingManager() {
		loadDebugModelContextBindings();
		loadDebugModelActivityExtensions();
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		DebugUITools.getDebugContextManager().addDebugContextListener(this);
		IWorkbenchActivitySupport activitySupport = PlatformUI.getWorkbench().getActivitySupport();
		activitySupport.getActivityManager().addActivityManagerListener(this);
	}
	
	/**
	 * Loads the extensions which map debug model identifiers
	 * to context ids.
	 */
	private void loadDebugModelContextBindings() {
		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), ID_DEBUG_MODEL_CONTEXT_BINDINGS);
		IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
		for (int i = 0; i < configurationElements.length; i++) {
			IConfigurationElement element = configurationElements[i];
			String modelIdentifier = element.getAttribute(ATTR_DEBUG_MODEL_ID);
			String contextId = element.getAttribute(ATTR_CONTEXT_ID);
			synchronized (this) {
    			if (modelIdentifier != null && contextId != null) {
    				List contextIds = (List) fModelToContextIds.get(modelIdentifier);
    				if (contextIds == null) {
    					contextIds = new ArrayList();
    					fModelToContextIds.put(modelIdentifier, contextIds);
    				}
    				contextIds.add(contextId);
    			}
			}
		}
	}	
	
	/**
	 * Loads the extensions which map debug model patterns
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
					if (pattern.endsWith(DEBUG_MODEL_ACTIVITY_SUFFIX)) {
						fModelPatternBindings.add(patternBinding);
					}
				}
			}
		}
	}	

	public void debugContextChanged(DebugContextEvent event) {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) > 0) {
			ISelection selection = event.getContext();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				Iterator iterator = ss.iterator();
				while (iterator.hasNext()) {
					activated(iterator.next()); 
				}
			}
		}
	}
	
	/**
	 * The specified object has been activated. Activate contexts and activities as
	 * required for the object.
	 * 
	 * @param object object that has been activated
	 */
	private void activated(Object object) {
		String[] modelIds = getDebugModelIds(object);
		if (modelIds == null) {
			return;
		}
		ILaunch launch = getLaunch(object);
		if (launch == null || launch.isTerminated()) {
			return;
		}
		List toEnable = new ArrayList(modelIds.length);
		synchronized (this) {
		    Set alreadyEnabled = (Set) fLaunchToModelIds.get(launch);
    		if (alreadyEnabled == null) {
    			alreadyEnabled = new HashSet();
    			fLaunchToModelIds.put(launch, alreadyEnabled);
    		}
    		for (int i = 0; i < modelIds.length; i++) {
    			String id = modelIds[i];
    			if (!alreadyEnabled.contains(id)) {
    				alreadyEnabled.add(id);
    				toEnable.add(id);
    			}
    		}
		}
		for (int i = 0; i < toEnable.size(); i++) {
		    activateModel((String)toEnable.get(i), launch);
		}

		enableActivitiesFor(modelIds);
	}
	
	/**
	 * Activates the given model identifier for the specified launch. This activates
	 * associated contexts and all parent contexts for the model.
	 * 
	 * @param modelId model to be enabled
	 * @param launch the launch the model is being enabled for
	 */
	private void activateModel(String modelId, ILaunch launch) {
	    List contextIds = null; 
	    synchronized (this) {
    		contextIds = (List) fModelToContextIds.get(modelId);
    		if (contextIds == null) {
    			// if there are no contexts for a model, the base debug context should 
    			// be activated (i.e. a debug model with no org.eclipse.ui.contexts and
    			// associated org.eclipse.debug.ui.modelContextBindings)
    			contextIds = new ArrayList();
    			contextIds.add(DEBUG_CONTEXT);
    			fModelToContextIds.put(modelId, contextIds);
    		}
	    }
	    
		Iterator iterator = contextIds.iterator();
		while (iterator.hasNext()) {
			activateContext((String) iterator.next(), launch);
		}
	}
	
	/**
	 * Activates the given context and all its parent contexts.
	 * 
	 * @param contextId
	 * @param launch
	 */
	private void activateContext(String contextId, ILaunch launch) {
		while (contextId != null) {
			Context context = fgContextService.getContext(contextId);
			IContextActivation activation = fgContextService.activateContext(contextId);
			addActivation(launch, activation);
			try {
				if (contextId.equals(DEBUG_CONTEXT)) {
					// don't enable windows contexts and higher
					break;
				}
				contextId = context.getParentId();
			} catch (NotDefinedException e) {
				contextId = null;
				DebugUIPlugin.log(e);
			}
		}
	}
	
	/**
	 * Notes the activation for a context and launch so we can de-activate later.
	 * 
	 * @param launch
	 * @param activation
	 */
	private synchronized void addActivation(ILaunch launch, IContextActivation activation) {
		List activations = (List) fLanuchToContextActivations.get(launch);
		if (activations == null) {
			activations = new ArrayList();
			fLanuchToContextActivations.put(launch, activations);
		}
		activations.add(activation);
	}

	/**
	 * Returns the debug model identifiers associated with the given object or <code>null</code>
	 * if none.
	 * 
	 * @param object 
	 * @return debug model identifiers associated with the given object or <code>null</code>
	 */
	private String[] getDebugModelIds(Object object) {
		if (object instanceof IAdaptable) {
            IDebugModelProvider modelProvider= (IDebugModelProvider)((IAdaptable)object).getAdapter(IDebugModelProvider.class);
			if (modelProvider != null) {
				String[] modelIds= modelProvider.getModelIdentifiers();
				if (modelIds != null) {
					return modelIds;
				}
			}
		}
		if (object instanceof IStackFrame) {
			return new String[] { ((IStackFrame) object).getModelIdentifier() };
		}
		return null;
	}	
	
	/**
	 * Returns the ILaunch associated with the given object or
	 * <code>null</code> if none.
	 * 
	 * @param object object for which launch is required
	 * @return the ILaunch associated with the given object or <code>null</code>
	 */
	public static ILaunch getLaunch(Object object) {
		ILaunch launch = null;
		if (object instanceof IAdaptable) {
			launch = (ILaunch) ((IAdaptable)object).getAdapter(ILaunch.class);
		}
		if (launch == null && object instanceof IDebugElement) {
			launch = ((IDebugElement) object).getLaunch();
		}
		return launch;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener2#launchesTerminated(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesTerminated(ILaunch[] launches) {
		// disable activated contexts
		for (int i = 0; i < launches.length; i++) {
			ILaunch launch = launches[i];
			List activations;
			synchronized(this) {
    			activations = (List) fLanuchToContextActivations.remove(launch);
    			fLaunchToModelIds.remove(launch);
			}
			if (activations != null) {
			    final List _activations = activations; 
				UIJob job = new UIJob("Deactivate debug contexts") { //$NON-NLS-1$
					public IStatus runInUIThread(IProgressMonitor monitor) {
						Iterator iterator = _activations.iterator();
						while (iterator.hasNext()) {
							IContextActivation activation = (IContextActivation) iterator.next();
							activation.getContextService().deactivateContext(activation);
						}
						return Status.OK_STATUS;
					}
				};
				job.setSystem(true);
				job.schedule();
			}
			
		}
		// TODO: Terminated notification
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesRemoved(ILaunch[] launches) {}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesAdded(ILaunch[] launches) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesChanged(ILaunch[] launches) {
	}	
	
	/**
	 * Returns the workbench contexts associated with a debug context
	 * 
	 * @param target debug context
	 * @return associated workbench contexts
	 */
	public List getWorkbenchContextsForDebugContext(Object target) {
		List workbenchContexts = new ArrayList();
		String[] modelIds = getDebugModelIds(target);
		if (modelIds != null) {
			for (int i = 0; i < modelIds.length; i++) {
				String modelId = modelIds[i];
				synchronized (this) {
    				List contextIds = (List) fModelToContextIds.get(modelId);
    				if (contextIds != null) {
    					Iterator contextIterator = contextIds.iterator();
    					while (contextIterator.hasNext()) {
    						String contextId = (String) contextIterator.next();
    						if (!workbenchContexts.contains(contextId)) {
    							workbenchContexts.add(contextId);
    						}
    					}
    				}
				}
			}
		}
		return workbenchContexts;
	}	
	
	/**
	 * Enables activities in the workbench associated with the given debug 
	 * model ids that have been activated.
	 * 
	 * @param debug model ids for which to enable activities
	 */
	private void enableActivitiesFor(String[] modelIds) {
		Set activities = null;
		for (int i = 0; i < modelIds.length; i++) {
			String id = modelIds[i];
			if (!fModelsEnabledForActivities.contains(id)) {
				Set ids= (Set) fModelToActivities.get(id);
				if (ids == null) {
					// first time the model has been seen, perform pattern matching
					ids = new HashSet();
					fModelToActivities.put(id, ids);
					Iterator bindings = fModelPatternBindings.iterator();
					while (bindings.hasNext()) {
						IActivityPatternBinding binding = (IActivityPatternBinding) bindings.next();
						String regex = binding.getPattern().pattern();
						regex = regex.substring(0, regex.length() - DEBUG_MODEL_ACTIVITY_SUFFIX.length());
						if (Pattern.matches(regex, id)) {
							ids.add(binding.getActivityId());
						}
					}
				}
				if (!ids.isEmpty()) {
					if (activities == null) {
						activities = new HashSet();
					}
					activities.addAll(ids);
				}
				fModelsEnabledForActivities.add(id);
			}
		}
		if (activities != null) {
			IWorkbenchActivitySupport activitySupport = PlatformUI.getWorkbench().getActivitySupport();
			Set enabledActivityIds = activitySupport.getActivityManager().getEnabledActivityIds();
			if (!enabledActivityIds.containsAll(activities)) {
				enabledActivityIds = new HashSet(enabledActivityIds);
				enabledActivityIds.addAll(activities);
				activitySupport.setEnabledActivityIds(activities);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.activities.IActivityManagerListener#activityManagerChanged(org.eclipse.ui.activities.ActivityManagerEvent)
	 */
	public void activityManagerChanged(ActivityManagerEvent activityManagerEvent) {
		if (activityManagerEvent.haveEnabledActivityIdsChanged()) {
			fModelsEnabledForActivities.clear();
		}
	}

}
