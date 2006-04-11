/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.SaveablesLifecycleEvent;
import org.eclipse.ui.internal.navigator.VisibilityAssistant.VisibilityListener;
import org.eclipse.ui.internal.navigator.extensions.ExtensionPriorityComparator;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentExtension;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorSaveablesService;
import org.eclipse.ui.navigator.ISaveablesSourceHelper;
import org.eclipse.ui.navigator.SaveablesProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;

/**
 * Implementation of INavigatorSaveablesService.
 * <p>
 * Implementation note: all externally callable methods are synchronized. The
 * private helper methods are not synchronized since they can only be called
 * from methods that already hold the lock.
 * </p>
 * @since 3.2
 * 
 */
public class NavigatorSaveablesService implements INavigatorSaveablesService, VisibilityListener {

	private NavigatorContentService contentService;

	private static List instances = new ArrayList();

	/**
	 * @param contentService
	 */
	public NavigatorSaveablesService(NavigatorContentService contentService) {
		this.contentService = contentService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.INavigatorSaveablesService#createHelper(org.eclipse.ui.ISaveablesSource,
	 *      org.eclipse.jface.viewers.StructuredViewer,
	 *      org.eclipse.ui.ISaveablesLifecycleListener)
	 */
	public ISaveablesSourceHelper createHelper(ISaveablesSource source,
			StructuredViewer viewer, ISaveablesLifecycleListener listener) {
		SaveablesSourceHelper saveablesSourceHelper = new SaveablesSourceHelper();
		return saveablesSourceHelper;
	}

	private static void addInstance(NavigatorSaveablesService saveablesService) {
		synchronized (instances) {
			instances.add(saveablesService);
		}
	}

	private static void removeInstance(
			NavigatorSaveablesService saveablesService) {
		synchronized (instances) {
			instances.remove(saveablesService);
		}
	}

	/**
	 * @param event
	 */
	/* package */ static void bundleChanged(BundleEvent event) {
		synchronized(instances) {
			if (event.getType() == BundleEvent.STARTED) {
				// System.out.println("bundle started: " + event.getBundle().getSymbolicName()); //$NON-NLS-1$
				for (Iterator it = instances.iterator(); it.hasNext();) {
					NavigatorSaveablesService instance = (NavigatorSaveablesService) it
							.next();
					instance.handleBundleStarted(event.getBundle()
							.getSymbolicName());
				}
			} else if (event.getType() == BundleEvent.STOPPED) {
				// System.out.println("bundle stopped: " + event.getBundle().getSymbolicName()); //$NON-NLS-1$
				for (Iterator it = instances.iterator(); it.hasNext();) {
					NavigatorSaveablesService instance = (NavigatorSaveablesService) it
							.next();
					instance.handleBundleStopped(event.getBundle()
							.getSymbolicName());
				}
			}
		}
	}

	private class LifecycleListener implements ISaveablesLifecycleListener {
		public void handleLifecycleEvent(SaveablesLifecycleEvent event) {
			Saveable[] saveables = event.getSaveables();
			switch (event.getEventType()) {
			case SaveablesLifecycleEvent.POST_OPEN:
				recomputeSaveablesAndNotify(false, null);
				break;
			case SaveablesLifecycleEvent.POST_CLOSE:
				recomputeSaveablesAndNotify(false, null);
				break;
			case SaveablesLifecycleEvent.DIRTY_CHANGED:
				Saveable[] shownSaveables = getShownSaveables(saveables);
				if (shownSaveables.length > 0) {
					outsideListener
							.handleLifecycleEvent(new SaveablesLifecycleEvent(
									saveablesSource,
									SaveablesLifecycleEvent.DIRTY_CHANGED,
									shownSaveables, false));
				}
				break;
			}
		}
	}

	private Saveable[] currentSaveables;

	private ISaveablesLifecycleListener outsideListener;

	private ISaveablesLifecycleListener saveablesLifecycleListener = new LifecycleListener();

	private ISaveablesSource saveablesSource;

	private StructuredViewer viewer;

	private SaveablesProvider[] saveablesProviders;

	private DisposeListener disposeListener = new DisposeListener() {

		public void widgetDisposed(DisposeEvent e) {
			if (saveablesProviders != null) {
				for (int i = 0; i < saveablesProviders.length; i++) {
					saveablesProviders[i].dispose();
				}
			}
			removeInstance(NavigatorSaveablesService.this);
			contentService = null;
			currentSaveables = null;
			outsideListener = null;
			saveablesLifecycleListener = null;
			saveablesSource = null;
			viewer = null;
			saveablesProviders = null;
			disposeListener = null;
		}
	};

	private Map inactivePluginsWithSaveablesProviders;

    /**
	 * a TreeMap (NavigatorContentDescriptor->SaveablesProvider) which uses
	 * ExtensionPriorityComparator.INSTANCE as its Comparator
	 */
	private Map saveablesProviderMap;

	/**
	 * @param saveablesSource
	 * @param viewer
	 * @param outsideListener
	 */
	public synchronized void init(final ISaveablesSource saveablesSource,
			final StructuredViewer viewer,
			ISaveablesLifecycleListener outsideListener) {
		this.saveablesSource = saveablesSource;
		this.viewer = viewer;
		this.outsideListener = outsideListener;
		currentSaveables = computeSaveables();
		viewer.getControl().addDisposeListener(disposeListener);
		addInstance(this);
	}

	/** helper to compute the saveables for which elements are part of the tree.
	 * Must be called from a synchronized method.
	 * 
	 * @return the saveables
	 */ 
	private Saveable[] computeSaveables() {
		ITreeContentProvider contentProvider = (ITreeContentProvider) viewer
				.getContentProvider();
		Object viewerInput = viewer.getInput();
		List result = new ArrayList();
		Set roots = new HashSet(Arrays.asList(contentProvider
				.getElements(viewerInput)));
		SaveablesProvider[] saveablesProviders = getSaveablesProviders();
		for (int i = 0; i < saveablesProviders.length; i++) {
			SaveablesProvider saveablesProvider = saveablesProviders[i];
			Saveable[] saveables = saveablesProvider.getSaveables();
			for (int j = 0; j < saveables.length; j++) {
				Saveable saveable = saveables[j];
				Object[] elements = saveablesProvider.getElements(saveable);
				// the saveable is added to the result if at least one of the
				// elements representing the saveable appears in the tree, i.e.
				// if its parent chain leads to a root node.
				boolean foundRoot = false;
				for (int k = 0; !foundRoot && k < elements.length; k++) {
					Object element = elements[k];
					while (!foundRoot && element != null) {
						if (roots.contains(element)) {
							// found a parent chain leading to a root. The
							// saveable is part of the tree.
							result.add(saveable);
							foundRoot = true;
						} else {
							element = contentProvider.getParent(element);
						}
					}
				}
			}
		}
		return (Saveable[]) result.toArray(new Saveable[result.size()]);
	}

	public synchronized Saveable[] getActiveSaveables() {
		ITreeContentProvider contentProvider = (ITreeContentProvider) viewer
				.getContentProvider();
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		Set result = new HashSet();
		for (Iterator it = selection.iterator(); it.hasNext();) {
			Object element = it.next();
			// try to find a saveable that contains the selected element
			while (element != null) {
				Saveable saveable = getSaveable(element);
				if (saveable != null) {
					result.add(saveable);
					break;
				}
				element = contentProvider.getParent(element);
			}
		}
		return (Saveable[]) result.toArray(new Saveable[result.size()]);
	}

	/**
	 * @param element
	 * @return the saveable associated with the given element
	 */
	private Saveable getSaveable(Object element) {
		if (saveablesProviderMap==null) {
			// has the side effect of recomputing saveablesProviderMap:
			getSaveablesProviders();
		}
        for(Iterator sItr = saveablesProviderMap.keySet().iterator(); sItr.hasNext();) {
        	NavigatorContentDescriptor descriptor = (NavigatorContentDescriptor) sItr.next();
                if(descriptor.isTriggerPoint(element) || descriptor.isPossibleChild(element)) {
                	SaveablesProvider provider = (SaveablesProvider) saveablesProviderMap.get(descriptor);
                	Saveable  saveable = provider.getSaveable(element);
                        if(saveable != null) {
                                return saveable;
                        }
                }
        }
        return null;
	}

	/**
	 * @return the saveables
	 */
	public Saveable[] getSaveables() {
		return currentSaveables;
	}

	/**
	 * @return all SaveablesProvider objects
	 */
	private SaveablesProvider[] getSaveablesProviders() {
		// TODO optimize this
		if (saveablesProviders == null) {
			inactivePluginsWithSaveablesProviders = new HashMap();
			saveablesProviderMap = new TreeMap(ExtensionPriorityComparator.INSTANCE);
			INavigatorContentDescriptor[] descriptors = contentService
					.getActiveDescriptorsWithSaveables();
			List result = new ArrayList();
			for (int i = 0; i < descriptors.length; i++) {
				NavigatorContentDescriptor descriptor = (NavigatorContentDescriptor) descriptors[i];
				String pluginId = ((NavigatorContentDescriptor) descriptor)
						.getContribution().getPluginId();
				if (Platform.getBundle(pluginId).getState() != Bundle.ACTIVE) {
					List inactiveDescriptors = (List) inactivePluginsWithSaveablesProviders
							.get(pluginId);
					if (inactiveDescriptors == null) {
						inactiveDescriptors = new ArrayList();
						inactivePluginsWithSaveablesProviders.put(pluginId,
								inactiveDescriptors);
					}
					inactiveDescriptors.add(descriptor);
				} else {
					SaveablesProvider saveablesProvider = createSaveablesProvider(descriptor);
					if (saveablesProvider != null) {
						saveablesProvider.init(saveablesLifecycleListener);
						result.add(saveablesProvider);
						saveablesProviderMap.put(descriptor, saveablesProvider);
					}
				}
			}
			saveablesProviders = (SaveablesProvider[]) result
					.toArray(new SaveablesProvider[result.size()]);
		}
		return saveablesProviders;
	}

	/**
	 * @param descriptor
	 * @return the SaveablesProvider, or null
	 */
	private SaveablesProvider createSaveablesProvider(NavigatorContentDescriptor descriptor) {
		NavigatorContentExtension extension = contentService
				.getExtension(descriptor, true);
		ITreeContentProvider contentProvider = extension
				.getContentProvider();
		if (contentProvider instanceof IAdaptable) {
			return (SaveablesProvider) ((IAdaptable) contentProvider)
					.getAdapter(SaveablesProvider.class);
		}
		return null;
	}

	private Saveable[] getShownSaveables(Saveable[] saveables) {
		Set result = new HashSet(Arrays.asList(currentSaveables));
		result.retainAll(Arrays.asList(saveables));
		return (Saveable[]) result.toArray(new Saveable[result.size()]);
	}

	/* package */synchronized void recomputeSaveablesAndNotify(boolean recomputeProviders,
			String startedBundleIdOrNull) {
		synchronized (this) {
			if (recomputeProviders && startedBundleIdOrNull == null
					&& saveablesProviders != null) {
				// a bundle was stopped, dispose of all saveablesProviders and
				// recompute
				// TODO optimize this
				for (int i = 0; i < saveablesProviders.length; i++) {
					saveablesProviders[i].dispose();
				}
				saveablesProviders = null;
			} else if (startedBundleIdOrNull != null){
				if(inactivePluginsWithSaveablesProviders.containsKey(startedBundleIdOrNull)) {
					updateSaveablesProviders(startedBundleIdOrNull);
				}
			}
			Set oldSaveables = new HashSet(Arrays.asList(currentSaveables));
			currentSaveables = computeSaveables();
			Set newSaveables = new HashSet(Arrays.asList(currentSaveables));
			final Set removedSaveables = new HashSet(oldSaveables);
			removedSaveables.removeAll(newSaveables);
			final Set addedSaveables = new HashSet(newSaveables);
			addedSaveables.removeAll(oldSaveables);
			if (addedSaveables.size() > 0) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						outsideListener.handleLifecycleEvent(new SaveablesLifecycleEvent(
								saveablesSource, SaveablesLifecycleEvent.POST_OPEN,
								(Saveable[]) addedSaveables
								.toArray(new Saveable[addedSaveables.size()]),
								false));
					}
				});
			}
			// TODO this will make the closing of saveables non-cancelable.
			// Ideally, we should react to PRE_CLOSE events and fire
			// an appropriate PRE_CLOSE
			if (removedSaveables.size() > 0) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						outsideListener
								.handleLifecycleEvent(new SaveablesLifecycleEvent(
										saveablesSource,
										SaveablesLifecycleEvent.PRE_CLOSE,
										(Saveable[]) removedSaveables
												.toArray(new Saveable[removedSaveables
														.size()]), true));
						outsideListener
								.handleLifecycleEvent(new SaveablesLifecycleEvent(
										saveablesSource,
										SaveablesLifecycleEvent.POST_CLOSE,
										(Saveable[]) removedSaveables
												.toArray(new Saveable[removedSaveables
														.size()]), false));
					}
				});
			}
		}
	}

	/**
	 * @param startedBundleId
	 */
	private void updateSaveablesProviders(String startedBundleId) {
		List result = new ArrayList(Arrays.asList(saveablesProviders));
		List descriptors = (List) inactivePluginsWithSaveablesProviders
				.get(startedBundleId);
		for (Iterator it = descriptors.iterator(); it.hasNext();) {
			NavigatorContentDescriptor descriptor = (NavigatorContentDescriptor) it
					.next();
			SaveablesProvider saveablesProvider = createSaveablesProvider(descriptor);
			if (saveablesProvider != null) {
				saveablesProvider.init(saveablesLifecycleListener);
				result.add(saveablesProvider);
				saveablesProviderMap.put(descriptor, saveablesProvider);
			}
		}
		saveablesProviders = (SaveablesProvider[]) result
				.toArray(new SaveablesProvider[result.size()]);
	}

	/**
	 * @param symbolicName
	 */
	private synchronized void handleBundleStarted(String symbolicName) {
		if (inactivePluginsWithSaveablesProviders.containsKey(symbolicName)) {
			recomputeSaveablesAndNotify(true, symbolicName);
		}
	}

	/**
	 * @param symbolicName
	 */
	private synchronized void handleBundleStopped(String symbolicName) {
		recomputeSaveablesAndNotify(true, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.navigator.VisibilityAssistant.VisibilityListener#onVisibilityOrActivationChange()
	 */
	public void onVisibilityOrActivationChange() {
		recomputeSaveablesAndNotify(true, null);
	}

}
