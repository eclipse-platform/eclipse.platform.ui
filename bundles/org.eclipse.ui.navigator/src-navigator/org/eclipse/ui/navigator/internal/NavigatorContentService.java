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
package org.eclipse.ui.navigator.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.IMementoAware;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.NavigatorActivationService;
import org.eclipse.ui.navigator.internal.extensions.IExtensionActivationListener;
import org.eclipse.ui.navigator.internal.extensions.INavigatorContentServiceListener;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptorRegistry;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentExtension;
import org.eclipse.ui.navigator.internal.extensions.NavigatorViewerDescriptor;
import org.eclipse.ui.navigator.internal.extensions.NavigatorViewerDescriptorRegistry;
import org.eclipse.ui.navigator.internal.extensions.StructuredViewerManager;

/**
 * <p>
 * Provides centralized access to the information provided by
 * NavigatorContentExtensions. Can be instantiated as needed, but should be
 * cached for active viewers. Information specific to a given viewer will be
 * cached by the NavigatorContentService, not including ContentProviders and
 * Label Providers created by {@link #createCommonContentProvider()}and
 * {@link #createCommonLabelProvider()}respectively.
 * </p>
 * 
 * <p>
 * The following class is experimental until fully documented.
 * </p>
 */
public class NavigatorContentService implements IExtensionActivationListener,
		IMementoAware, INavigatorContentService {

	private static final NavigatorActivationService NAVIGATOR_ACTIVATION_SERVICE = NavigatorActivationService
			.getInstance();

	private static final NavigatorContentDescriptorRegistry CONTENT_DESCRIPTOR_REGISTRY = NavigatorContentDescriptorRegistry
			.getInstance();

	private static final NavigatorViewerDescriptorRegistry VIEWER_DESCRIPTOR_REGISTRY = NavigatorViewerDescriptorRegistry
			.getInstance();

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

	private WeakHashMap contributionMemory;

	/**
	 * 
	 */
	public NavigatorContentService(String aViewerId) {
		super();
		aViewerId = aViewerId != null ? aViewerId : ""; //$NON-NLS-1$
		viewerDescriptor = VIEWER_DESCRIPTOR_REGISTRY
				.getNavigatorViewerDescriptor(aViewerId);
		NavigatorActivationService.getInstance()
				.addExtensionActivationListener(viewerDescriptor.getViewerId(),
						this);
	}

	/**
	 * 
	 */
	public NavigatorContentService(String aViewerId, StructuredViewer aViewer) {
		this(aViewerId);
		structuredViewerManager = new StructuredViewerManager(aViewer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#createCommonContentProvider()
	 */
	public ITreeContentProvider createCommonContentProvider() {
		return new NavigatorContentServiceContentProvider(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#createCommonLabelProvider()
	 */
	public ILabelProvider createCommonLabelProvider() {
		return new NavigatorContentServiceLabelProvider(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#dispose()
	 */
	public void dispose() {
		for (Iterator contentItr = contentExtensions.values().iterator(); contentItr
				.hasNext();)
			((NavigatorContentExtension) contentItr.next()).dispose();
		NavigatorActivationService.getInstance()
				.removeExtensionActivationListener(
						viewerDescriptor.getViewerId(), this);
	}

	protected void updateService(Viewer aViewer, Object anOldInput,
			Object aNewInput) {

		synchronized (this) {

			if (structuredViewerManager == null) {
				structuredViewerManager = new StructuredViewerManager(aViewer);
				structuredViewerManager.inputChanged(anOldInput, aNewInput);
			} else
				structuredViewerManager.inputChanged(aViewer, anOldInput,
						aNewInput);

			for (Iterator contentItr = contentExtensions.values().iterator(); contentItr
					.hasNext();)
				structuredViewerManager
						.initialize(((NavigatorContentExtension) contentItr
								.next()).getContentProvider());

			NavigatorContentExtension[] resultInstances = findRootContentDescriptors(aNewInput);
			rootContentProviders = extractContentProviders(resultInstances);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#findStateModel(java.lang.String)
	 */
	public IExtensionStateModel findStateModel(String anExtensionId) {
		return getExtension(
				CONTENT_DESCRIPTOR_REGISTRY.getContentDescriptor(anExtensionId))
				.getStateModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#findParentContentProviders(java.lang.Object)
	 */
	public ITreeContentProvider[] findParentContentProviders(Object anElement) {
		NavigatorContentExtension[] resultInstances = findRelevantContentExtensions(anElement);
		return extractContentProviders(resultInstances);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#findRootContentProviders(java.lang.Object)
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
					resultInstances = findRootContentDescriptors(anElement,
							false);
					rootContentProviders = extractContentProviders(resultInstances);
				}
			}
		}
		return rootContentProviders;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#findRelevantContentProviders(java.lang.Object)
	 */
	public ITreeContentProvider[] findRelevantContentProviders(Object anElement) {
		NavigatorContentExtension[] resultInstances = findRelevantContentExtensions(anElement);
		return extractContentProviders(resultInstances);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#findRelevantLabelProviders(java.lang.Object)
	 */

	public ILabelProvider[] findRelevantLabelProviders(Object anElement) { 
		NavigatorContentExtension[] resultInstances = findRelevantContentExtensions(
				anElement, false);
		return extractLabelProviders(resultInstances);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#findRelevantContentExtensions(java.lang.Object)
	 */
	public NavigatorContentExtension[] findRelevantContentExtensions(
			Object anElement) {
		return findRelevantContentExtensions(anElement, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#findRelevantContentExtensions(java.lang.Object,
	 *      boolean)
	 */
	public NavigatorContentExtension[] findRelevantContentExtensions(
			Object anElement, boolean toLoadIfNecessary) {
		Set enabledDescriptors = getEnabledDescriptors(anElement);
		return extractDescriptorInstances(enabledDescriptors, toLoadIfNecessary);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#findRelevantContentExtensions(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public NavigatorContentExtension[] findRelevantContentExtensions(
			IStructuredSelection aSelection) {
		Set contentDescriptors = getEnabledDescriptors(aSelection);
		if (contentDescriptors.size() == 0)
			return NO_DESCRIPTOR_INSTANCES; 
		return extractDescriptorInstances(contentDescriptors);

	}

	public synchronized void rememberContribution(
			NavigatorContentDescriptor source, Object[] elements) {

		if (source != null && elements != null)
			for (int i = 0; i < elements.length; i++)
				getContributionMemory().put(elements[i], source);
	}

	public synchronized void rememberContribution(
			NavigatorContentDescriptor source, Object element) {
		if (source != null && element != null)
			getContributionMemory().put(element, source);
	}

	public NavigatorContentDescriptor getSourceOfContribution(Object element) {
		return (NavigatorContentDescriptor) getContributionMemory().get(element);
	}

	/**
	 * @return Returns the contributionMemory.
	 */
	public Map getContributionMemory() {
		if (contributionMemory != null)
			return contributionMemory;
		synchronized (this) {
			if (contributionMemory == null)
				contributionMemory = new WeakHashMap();
		}

		return contributionMemory;
	}

	/**
	 * @param anElement
	 * @return
	 */
	private Set getEnabledDescriptors(Object anElement) {
		Set enabledByExpression = CONTENT_DESCRIPTOR_REGISTRY
				.getEnabledContentDescriptors(anElement, viewerDescriptor);
		NavigatorContentDescriptor descriptor = getSourceOfContribution(anElement);
		if(descriptor != null)
			enabledByExpression.add(descriptor);
		return filterDescriptors(enabledByExpression);
	}

	/**
	 * @param aSelection
	 * @return
	 */
	private Set getEnabledDescriptors(IStructuredSelection aSelection) {
		return filterDescriptors(CONTENT_DESCRIPTOR_REGISTRY
				.getEnabledContentDescriptors(aSelection));
	}

	private Set filterDescriptors(Set contentDescriptors) {
		Set result = new HashSet();
		for (Iterator contentDescriptorsItr = contentDescriptors.iterator(); contentDescriptorsItr.hasNext(); ) {
			NavigatorContentDescriptor descriptor = (NavigatorContentDescriptor) contentDescriptorsItr.next(); 
			if (!exclusions.contains(descriptor.getId()))
				result.add(descriptor);
		}
		return result;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.internal.views.extensions.IInitializationManager#initialize(org.eclipse.jface.viewers.IStructuredContentProvider)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#initialize(org.eclipse.jface.viewers.IStructuredContentProvider)
	 */
	public boolean initialize(IStructuredContentProvider aContentProvider) {
		return structuredViewerManager.initialize(aContentProvider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#onExtensionActivation(java.lang.String,
	 *      java.lang.String, boolean)
	 */
	public void onExtensionActivation(String aViewerId,
			String aNavigatorExtensionId, boolean toEnable) {
		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#update()
	 */
	public void update() {
		rootContentProviders = null;
		structuredViewerManager.safeRefresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#getViewerId()
	 */
	public final String getViewerId() {
		return viewerDescriptor.getViewerId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#getExtension(org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptor)
	 */
	public final NavigatorContentExtension getExtension(
			NavigatorContentDescriptor aDescriptorKey) {
		return getExtension(aDescriptorKey, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#getExtension(org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptor,
	 *      boolean)
	 */
	public final NavigatorContentExtension getExtension(
			NavigatorContentDescriptor aDescriptorKey, boolean toLoadIfNecessary) {
		/* Query and return the relevant descriptor instance */
		NavigatorContentExtension extension = (NavigatorContentExtension) contentExtensions
				.get(aDescriptorKey);
		if (extension != null || !toLoadIfNecessary)
			return extension;

		/*
		 * If the descriptor instance hasn't been created yet, then we need to
		 * (1) verify that it wasn't added by another thread, (2) create and add
		 * the result into the map
		 */
		synchronized (this) {
			extension = (NavigatorContentExtension) contentExtensions
					.get(aDescriptorKey);
			if (extension == null) {
				contentExtensions
						.put(aDescriptorKey,
								(extension = new NavigatorContentExtension(
										aDescriptorKey, this,
										structuredViewerManager)));
				notifyListeners(extension);
			}
		}
		return extension;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#getViewerDescriptor()
	 */
	public NavigatorViewerDescriptor getViewerDescriptor() {
		return viewerDescriptor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#addExclusion(java.lang.String)
	 */
	public void addExclusion(String anExtensionId) {
		exclusions.add(anExtensionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#removeExclusion(java.lang.String)
	 */
	public void removeExclusion(String anExtensionId) {
		exclusions.remove(anExtensionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#restoreState(org.eclipse.ui.IMemento)
	 */
	public void restoreState(final IMemento aMemento) {
		synchronized (this) {
			for (Iterator extensionItr = getExtensions().iterator(); extensionItr
					.hasNext();) {
				final NavigatorContentExtension element = (NavigatorContentExtension) extensionItr
						.next();
				ISafeRunnable runnable = new ISafeRunnable() {
					public void run() throws Exception {
						element.restoreState(aMemento);

					}

					public void handleException(Throwable exception) {
						NavigatorPlugin
								.logError(
										0,
										CommonNavigatorMessages.NavigatorContentService_0
												+ element.getId()
												+ CommonNavigatorMessages.NavigatorContentService_1,
										exception);

					}
				};
				Platform.run(runnable);

			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento aMemento) {
		synchronized (this) {
			for (Iterator extensionItr = getExtensions().iterator(); extensionItr
					.hasNext();) {
				NavigatorContentExtension element = (NavigatorContentExtension) extensionItr
						.next();
				element.saveState(aMemento);
			}
		}
	}

	protected final NavigatorContentExtension[] findRootContentDescriptors(
			Object anElement) {
		return findRootContentDescriptors(anElement, true);
	}

	protected final NavigatorContentExtension[] findRootContentDescriptors(
			Object anElement, boolean toRespectViewerRoots) {
		String[] rootDescriptorIds = viewerDescriptor
				.getRootContentExtensionIds();
		NavigatorContentExtension[] resultInstances = null;

		if (toRespectViewerRoots && rootDescriptorIds.length > 0) {
			List resultInstancesList = new ArrayList();

			NavigatorContentDescriptor descriptor = null;
			NavigatorContentExtension extension = null;
			for (int i = 0; i < rootDescriptorIds.length; i++) {
				if (isActive(rootDescriptorIds[i])) {
					descriptor = CONTENT_DESCRIPTOR_REGISTRY
							.getContentDescriptor(rootDescriptorIds[i]);
					extension = getExtension(descriptor);
					if (!extension.hasLoadingFailed())
						resultInstancesList.add(extension);
				}
			}
			resultInstancesList
					.toArray((resultInstances = new NavigatorContentExtension[resultInstancesList
							.size()]));
		} else
			resultInstances = findRelevantContentExtensions(anElement);
		return resultInstances;
	}

	protected boolean isActive(String anExtensionId) {
		return !exclusions.contains(anExtensionId)
				&& NAVIGATOR_ACTIVATION_SERVICE.isNavigatorExtensionActive(
						getViewerId(), anExtensionId);
	}

	protected final Collection getExtensions() {
		return (contentExtensions.size() > 0) ? Collections
				.unmodifiableCollection(contentExtensions.values())
				: Collections.EMPTY_LIST;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#addListener(org.eclipse.ui.navigator.internal.extensions.INavigatorContentServiceListener)
	 */
	public void addListener(INavigatorContentServiceListener aListener) {
		listeners.add(aListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#removeListener(org.eclipse.ui.navigator.internal.extensions.INavigatorContentServiceListener)
	 */
	public void removeListener(INavigatorContentServiceListener aListener) {
		listeners.remove(aListener);
	}

	private void notifyListeners(NavigatorContentExtension aDescriptorInstance) {

		if (listeners.size() == 0)
			return;
		INavigatorContentServiceListener listener = null;
		List failedListeners = null;
		for (Iterator listenersItr = listeners.iterator(); listenersItr
				.hasNext();) {
			try {
				listener = (INavigatorContentServiceListener) listenersItr
						.next();
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
	private ITreeContentProvider[] extractContentProviders(
			NavigatorContentExtension[] theDescriptorInstances) {
		if (theDescriptorInstances.length == 0)
			return NO_CONTENT_PROVIDERS;
		List resultProvidersList = new ArrayList();
		for (int i = 0; i < theDescriptorInstances.length; i++)
			if (theDescriptorInstances[i].getContentProvider() != null)
				resultProvidersList.add(theDescriptorInstances[i]
						.getContentProvider());
		return (ITreeContentProvider[]) resultProvidersList
				.toArray(new ITreeContentProvider[resultProvidersList.size()]);
	}

	/**
	 * @param theDescriptors
	 *            a List of NavigatorContentDescriptor objects
	 * @return
	 */
	private NavigatorContentExtension[] extractDescriptorInstances(
			Set theDescriptors) {
		return extractDescriptorInstances(theDescriptors, true);
	}

	/**
	 * @param theDescriptors
	 *            a List of NavigatorContentDescriptor objects
	 * @return
	 */
	private NavigatorContentExtension[] extractDescriptorInstances(
			Set theDescriptors, boolean toLoadAllIfNecessary) {
		if (theDescriptors.size() == 0)
			return NO_DESCRIPTOR_INSTANCES;
		Set resultInstances = new HashSet();
		for (Iterator descriptorIter = theDescriptors.iterator(); descriptorIter.hasNext(); ) {
			NavigatorContentExtension extension = getExtension(
					(NavigatorContentDescriptor) descriptorIter.next(),
					toLoadAllIfNecessary);
			if (extension != null) {
				resultInstances.add(extension);

			}
		}
		NavigatorContentExtension[] extensions = (NavigatorContentExtension[]) resultInstances.toArray(new NavigatorContentExtension[resultInstances.size()]);
		Arrays.sort(extensions, EXTENSION_COMPARATOR);
		return extensions;
	}

	/**
	 * @param theDescriptorInstances
	 * @return
	 */
	private ILabelProvider[] extractLabelProviders(
			NavigatorContentExtension[] theDescriptorInstances) {
		if (theDescriptorInstances.length == 0)
			return NO_LABEL_PROVIDERS;
		List resultProvidersList = new ArrayList();
		for (int i = 0; i < theDescriptorInstances.length; i++)
			if (theDescriptorInstances[i].getLabelProvider() != null)
				resultProvidersList.add(theDescriptorInstances[i]
						.getLabelProvider());
		return (ILabelProvider[]) resultProvidersList
				.toArray(new ILabelProvider[resultProvidersList.size()]);
	}
	

	private static final Comparator EXTENSION_COMPARATOR = new Comparator() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object lvalue, Object rvalue) {
			return ((NavigatorContentDescriptor) lvalue).getPriority() - ((NavigatorContentDescriptor) rvalue).getPriority();
		}
	};
}