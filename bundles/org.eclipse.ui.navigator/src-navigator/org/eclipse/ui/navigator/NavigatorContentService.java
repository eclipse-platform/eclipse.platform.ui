/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.navigator.internal.extensions.IExtensionActivationListener;
import org.eclipse.ui.navigator.internal.extensions.INavigatorContentServiceListener;
import org.eclipse.ui.navigator.internal.extensions.NavigatorActivationService;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptorRegistry;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentExtension;
import org.eclipse.ui.navigator.internal.extensions.NavigatorViewerDescriptor;
import org.eclipse.ui.navigator.internal.extensions.NavigatorViewerDescriptorRegistry;
import org.eclipse.ui.navigator.internal.extensions.StructuredViewerManager;

/**
 * <p>
 * Provides centralized access to the information provided by NavigatorContentExtensions. Can be
 * instantiated as needed, but should be cached for active viewers. Information specific to a given
 * viewer will be cached by the NavigatorContentService, not including ContentProviders and Label
 * Providers created by {@link #createCommonContentProvider()}and
 * {@link #createCommonLabelProvider()}respectively.
 * </p>
 * 
 * <p>
 * The following class is experimental until fully documented.
 * </p>
 */
public class NavigatorContentService implements IExtensionActivationListener, IMementoAware {

	private static final NavigatorActivationService NAVIGATOR_ACTIVATION_SERVICE = NavigatorActivationService.getInstance();
	private static final NavigatorContentDescriptorRegistry CONTENT_DESCRIPTOR_REGISTRY = NavigatorContentDescriptorRegistry.getInstance();
	private static final NavigatorViewerDescriptorRegistry VIEWER_DESCRIPTOR_REGISTRY = NavigatorViewerDescriptorRegistry.getInstance();

	private static final NavigatorContentExtension[] NO_DESCRIPTOR_INSTANCES = new NavigatorContentExtension[0];
	private static final ITreeContentProvider[] NO_CONTENT_PROVIDERS = new ITreeContentProvider[0];
	private static final ILabelProvider[] NO_LABEL_PROVIDERS = new ILabelProvider[0];

	private final NavigatorViewerDescriptor viewerDescriptor;
	private final List listeners = new ArrayList();

	/*
	 * A map of (String-based-Navigator-Content-Extension-IDs,
	 * NavigatorContentExtension-objects)-pairs
	 */
	private final Map contentExtensions = new HashMap();
	private StructuredViewerManager structuredViewerManager;

	private ITreeContentProvider[] rootContentProviders;
	private Collection exclusions = new ArrayList();

	/**
	 *  
	 */
	public NavigatorContentService(String aViewerId) {
		super();
		aViewerId = aViewerId != null ? aViewerId : ""; //$NON-NLS-1$
		viewerDescriptor = VIEWER_DESCRIPTOR_REGISTRY.getNavigatorViewerDescriptor(aViewerId);
		NavigatorActivationService.getInstance().addExtensionActivationListener(viewerDescriptor.getViewerId(), this);
	}

	/**
	 *  
	 */
	public NavigatorContentService(String aViewerId, StructuredViewer aViewer) {
		this(aViewerId);
		structuredViewerManager = new StructuredViewerManager(aViewer);
	} 	

	public ITreeContentProvider createCommonContentProvider() {
		return new NavigatorContentServiceContentProvider(this);
	}

	/**
	 * @return
	 */
	public ILabelProvider createCommonLabelProvider() {
		return new NavigatorContentServiceLabelProvider(this);
	}

	/**
	 *  
	 */
	public void dispose() {
		for (Iterator contentItr = contentExtensions.values().iterator(); contentItr.hasNext();)
			((NavigatorContentExtension) contentItr.next()).dispose();
		NavigatorActivationService.getInstance().removeExtensionActivationListener(viewerDescriptor.getViewerId(), this);
	}

	protected void updateService(Viewer aViewer, Object anOldInput, Object aNewInput) {

		synchronized (this) {

			if (structuredViewerManager == null) {
				structuredViewerManager = new StructuredViewerManager(aViewer);
				structuredViewerManager.inputChanged(anOldInput, aNewInput);
			} else
				structuredViewerManager.inputChanged(aViewer, anOldInput, aNewInput);

			for (Iterator contentItr = contentExtensions.values().iterator(); contentItr.hasNext();)
				structuredViewerManager.initialize(((NavigatorContentExtension) contentItr.next()).getContentProvider());

			NavigatorContentExtension[] resultInstances = findRootContentDescriptors(aNewInput);
			rootContentProviders = extractContentProviders(resultInstances);
		}
	}
	
	public IExtensionStateModel findStateModel(String anExtensionId) {
		return getExtension(CONTENT_DESCRIPTOR_REGISTRY.getContentDescriptor(anExtensionId)).getStateModel();		
	}

	/**
	 * @param element
	 * @return
	 */
	public ITreeContentProvider[] findParentContentProviders(Object anElement) {
		NavigatorContentExtension[] resultInstances = findRelevantContentExtensions(anElement);
		return extractContentProviders(resultInstances);
	}

	/**
	 * <p>
	 * Return all of the content providers that are relevant for the viewer. The viewer is
	 * determined by the ID used to create the NavigatorContentService.
	 * </p>
	 * 
	 * @return
	 */
	public ITreeContentProvider[] findRootContentProviders(Object anElement) {
		if (rootContentProviders != null)
			return rootContentProviders;
		synchronized (this) {
			if (rootContentProviders == null) {
				NavigatorContentExtension[] resultInstances = findRootContentDescriptors(anElement);
				if (resultInstances.length > 0)
					rootContentProviders = extractContentProviders(resultInstances);
				else {
					resultInstances = findRootContentDescriptors(anElement, false);
					rootContentProviders = extractContentProviders(resultInstances);
				}
			}
		}
		return rootContentProviders;
	}

	/**
	 * <p>
	 * Return all of the content providers that are enabled for the given parameter 'element'.
	 * 
	 * @param anElement
	 * @return
	 */
	public ITreeContentProvider[] findRelevantContentProviders(Object anElement) {
		NavigatorContentExtension[] resultInstances = findRelevantContentExtensions(anElement);
		return extractContentProviders(resultInstances);
	}


	/**
	 * <p>
	 * Return all of the label providers that are enabled for the given parameter 'element'.
	 * 
	 * @param anElement
	 * @return
	 */
	
	public ILabelProvider[] findRelevantLabelProviders(Object anElement) {
		NavigatorContentExtension[] resultInstances = findRelevantContentExtensions(anElement,false);
		return extractLabelProviders(resultInstances);
	}
	
	public NavigatorContentExtension[] findRelevantContentExtensions(Object anElement) {
		List enabledDescriptors = getEnabledDescriptors(anElement);
		return extractDescriptorInstances(enabledDescriptors);		
	}
	
	public NavigatorContentExtension[] findRelevantContentExtensions(Object anElement, boolean toLoadIfNecessary) {
		List enabledDescriptors = getEnabledDescriptors(anElement);
		return extractDescriptorInstances(enabledDescriptors,toLoadIfNecessary);		
	}
	
	

	public NavigatorContentExtension[] findRelevantContentExtensions(IStructuredSelection aSelection) {
		List contentDescriptors = getEnabledDescriptors(aSelection);
		if(contentDescriptors.size() == 0)
			return NO_DESCRIPTOR_INSTANCES;
		NavigatorContentExtension[] contentDescriptorInstances = new NavigatorContentExtension[contentDescriptors.size()];
		NavigatorContentDescriptor descriptor; 		
		for(int i=0; i<contentDescriptors.size(); i++ ) {
			descriptor = (NavigatorContentDescriptor) contentDescriptors.get(i);
			contentDescriptorInstances[i] = getExtension(descriptor);
		}
		return contentDescriptorInstances;
		
	}


	/**
	 * @param anElement
	 * @return
	 */
	private List getEnabledDescriptors(Object anElement) {
		return filterDescriptors(CONTENT_DESCRIPTOR_REGISTRY.getEnabledContentDescriptors(anElement, viewerDescriptor)); 
	}

	/**
	 * @param aSelection
	 * @return
	 */
	private List getEnabledDescriptors(IStructuredSelection aSelection) {
		return filterDescriptors(CONTENT_DESCRIPTOR_REGISTRY.getEnabledContentDescriptors(aSelection));
	}
	 
	private List filterDescriptors(List contentDescriptors) {
		List result = new ArrayList();
		for (int x=0; x< contentDescriptors.size(); ++x) {
			NavigatorContentDescriptor descriptor = (NavigatorContentDescriptor) contentDescriptors.get(x);
			if(!exclusions.contains(descriptor.getId()))
				result.add(descriptor);			
		}
		return result;
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.internal.views.extensions.IInitializationManager#initialize(org.eclipse.jface.viewers.IStructuredContentProvider)
	 */
	public boolean initialize(IStructuredContentProvider aContentProvider) {
		return structuredViewerManager.initialize(aContentProvider);
	}

	/**
	 * @param viewerId
	 * @param navigatorExtensionId
	 * @param toEnable
	 */
	public void onExtensionActivation(String aViewerId, String aNavigatorExtensionId, boolean toEnable) {
		update();
	}

	public void update() {
		rootContentProviders = null;
		structuredViewerManager.safeRefresh();
	}

	/**
	 * @return Returns the viewerId.
	 */
	public final String getViewerId() {
		return viewerDescriptor.getViewerId();
	}
	/**
	 * @param object
	 * @return
	 */
	public final NavigatorContentExtension getExtension(NavigatorContentDescriptor aDescriptorKey) {
		return getExtension(aDescriptorKey,true);
	}
	
	/**
	 * @param object
	 * @return
	 */
	public final NavigatorContentExtension getExtension(NavigatorContentDescriptor aDescriptorKey , boolean toLoadIfNecessary) {
		/* Query and return the relevant descriptor instance */
		NavigatorContentExtension extension = (NavigatorContentExtension) contentExtensions.get(aDescriptorKey);
		if (extension != null || !toLoadIfNecessary)
			return extension;

		/*
		 * If the descriptor instance hasn't been created yet, then we need to (1) verify that it
		 * wasn't added by another thread, (2) create and add the result into the map
		 */
		synchronized (this) {
			extension = (NavigatorContentExtension) contentExtensions.get(aDescriptorKey);
			if (extension == null) {
				contentExtensions.put(aDescriptorKey, (extension = new NavigatorContentExtension(aDescriptorKey, this, structuredViewerManager)));
				notifyListeners(extension);
			}
		}
		return extension;
		
	}

	/**
	 * 
	 * @return The ViewerDescriptor for tihs Content Service instance. 
	 */
	public NavigatorViewerDescriptor getViewerDescriptor() {
		return viewerDescriptor;
	}
	
	
	public void addExclusion(String anExtensionId) {
		exclusions.add(anExtensionId);
	}
	
	public void removeExclusion(String anExtensionId) {
		exclusions.remove(anExtensionId);
	}
	
	public void restoreState(final IMemento aMemento) { 
		synchronized (this) { 
			for (Iterator extensionItr = getExtensions().iterator(); extensionItr.hasNext();) {
				final NavigatorContentExtension element = (NavigatorContentExtension) extensionItr.next();
				ISafeRunnable runnable = new ISafeRunnable() {
					public void run() throws Exception {
						element.restoreState(aMemento);	
						
					}
					public void handleException(Throwable exception) {
						NavigatorPlugin.logError(0, "Could not restore state for Common Navigator content extension \""+element.getId()+"\".", exception);
						
					}
				};
				Platform.run(runnable);				
				
			}
		}		
	}
	
	public void saveState(IMemento aMemento) { 
		synchronized (this) { 
			for (Iterator extensionItr = getExtensions().iterator(); extensionItr.hasNext();) {
				NavigatorContentExtension element = (NavigatorContentExtension) extensionItr.next();
				element.saveState(aMemento);
			}
		}		
	}

	protected final NavigatorContentExtension[] findRootContentDescriptors(Object anElement) {
		return findRootContentDescriptors(anElement, true);
	}

	protected final NavigatorContentExtension[] findRootContentDescriptors(Object anElement, boolean toRespectViewerRoots) {
		String[] rootDescriptorIds = viewerDescriptor.getRootContentExtensionIds();
		NavigatorContentExtension[] resultInstances = null;

		if (toRespectViewerRoots && rootDescriptorIds.length > 0) {
			List resultInstancesList = new ArrayList();

			NavigatorContentDescriptor descriptor = null;
			NavigatorContentExtension extension = null;
			for (int i = 0; i < rootDescriptorIds.length; i++) {
				if (isActive(rootDescriptorIds[i])) {
					descriptor = CONTENT_DESCRIPTOR_REGISTRY.getContentDescriptor(rootDescriptorIds[i]);
					extension = getExtension(descriptor);
					if (!extension.hasLoadingFailed())
						resultInstancesList.add(extension);
				}
			}
			resultInstancesList.toArray((resultInstances = new NavigatorContentExtension[resultInstancesList.size()]));
		} else
			resultInstances = findRelevantContentExtensions(anElement);
		return resultInstances;
	}

	protected boolean isActive(String anExtensionId) {
		return !exclusions.contains(anExtensionId) && NAVIGATOR_ACTIVATION_SERVICE.isNavigatorExtensionActive(getViewerId(), anExtensionId);
	}	

	protected final Collection getExtensions() {
		return (contentExtensions.size() > 0) ? Collections.unmodifiableCollection(contentExtensions.values()) : Collections.EMPTY_LIST;
	}

	public void addListener(INavigatorContentServiceListener aListener) {
		listeners.add(aListener);
	}

	public void removeListener(INavigatorContentServiceListener aListener) {
		listeners.remove(aListener);
	}

	private void notifyListeners(NavigatorContentExtension aDescriptorInstance) {
		
		if (listeners.size() == 0)
			return;
		INavigatorContentServiceListener listener = null;
		List failedListeners = null;
		for (Iterator listenersItr = listeners.iterator(); listenersItr.hasNext();) {
			try {
				listener = (INavigatorContentServiceListener) listenersItr.next();
				listener.onLoad(aDescriptorInstance);
			} catch (RuntimeException re) {
				if (failedListeners == null)
					failedListeners = new ArrayList();
				failedListeners.add(listener);				
			}
		}
		if (failedListeners != null) {
			listeners.removeAll(failedListeners);
		}
	}

	/**
	 * @param theDescriptorInstances
	 * @return
	 */
	private ITreeContentProvider[] extractContentProviders(NavigatorContentExtension[] theDescriptorInstances) {
		if (theDescriptorInstances.length == 0)
			return NO_CONTENT_PROVIDERS;
		List resultProvidersList = new ArrayList();
		for (int i = 0; i < theDescriptorInstances.length; i++)
			if (theDescriptorInstances[i].getContentProvider() != null)
				resultProvidersList.add(theDescriptorInstances[i].getContentProvider());
		return (ITreeContentProvider[]) resultProvidersList.toArray(new ITreeContentProvider[resultProvidersList.size()]);
	}

	/**
	 * @param theDescriptors
	 *            a List of NavigatorContentDescriptor objects
	 * @return
	 */
	private NavigatorContentExtension[] extractDescriptorInstances(List theDescriptors) {
		return extractDescriptorInstances(theDescriptors,true);
	}
	
	
	/**
	 * @param theDescriptors
	 *            a List of NavigatorContentDescriptor objects
	 * @return
	 */
	private NavigatorContentExtension[] extractDescriptorInstances(List theDescriptors, boolean toLoadAllIfNecessary) {
		if (theDescriptors.size() == 0)
			return NO_DESCRIPTOR_INSTANCES;
		List  resultInstances = new ArrayList();
		for (int i = 0; i < theDescriptors.size(); i++) {
			NavigatorContentExtension extension = getExtension((NavigatorContentDescriptor) theDescriptors.get(i), toLoadAllIfNecessary);
			if (extension != null) {
				resultInstances.add(extension);
				
			}
		}
		return (NavigatorContentExtension[]) resultInstances.toArray(new NavigatorContentExtension[resultInstances.size()]);
	}

	/**
	 * @param theDescriptorInstances
	 * @return
	 */
	private ILabelProvider[] extractLabelProviders(NavigatorContentExtension[] theDescriptorInstances) {
		if (theDescriptorInstances.length == 0)
			return NO_LABEL_PROVIDERS;
		List resultProvidersList = new ArrayList();
		for (int i = 0; i < theDescriptorInstances.length; i++)
			if (theDescriptorInstances[i].getLabelProvider() != null)
				resultProvidersList.add(theDescriptorInstances[i].getLabelProvider());
		return (ILabelProvider[]) resultProvidersList.toArray(new ILabelProvider[resultProvidersList.size()]);
	}
}