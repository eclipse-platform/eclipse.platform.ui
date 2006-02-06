/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.eclipse.ui.navigator.IExtensionActivationListener;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.IMementoAware;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.INavigatorContentServiceListener;
import org.eclipse.ui.navigator.INavigatorFilterService;
import org.eclipse.ui.navigator.INavigatorSorterService;
import org.eclipse.ui.navigator.INavigatorViewerDescriptor;
import org.eclipse.ui.navigator.NavigatorActivationService;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptorManager;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentExtension;
import org.eclipse.ui.navigator.internal.extensions.NavigatorViewerDescriptor;
import org.eclipse.ui.navigator.internal.extensions.NavigatorViewerDescriptorRegistry;
import org.eclipse.ui.navigator.internal.extensions.StructuredViewerManager;
import org.eclipse.ui.navigator.internal.sorters.NavigatorSorterService;

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

	private static final NavigatorContentDescriptorManager CONTENT_DESCRIPTOR_REGISTRY = NavigatorContentDescriptorManager
			.getInstance();

	private static final NavigatorViewerDescriptorRegistry VIEWER_DESCRIPTOR_REGISTRY = NavigatorViewerDescriptorRegistry
			.getInstance();

	private static final ITreeContentProvider[] NO_CONTENT_PROVIDERS = new ITreeContentProvider[0];

	private static final ILabelProvider[] NO_LABEL_PROVIDERS = new ILabelProvider[0];

	private static final INavigatorContentDescriptor[] NO_DESCRIPTORS = new INavigatorContentDescriptor[0];

	private static final String[] NO_EXTENSION_IDS = new String[0];

	private final NavigatorViewerDescriptor viewerDescriptor;

	private final List listeners = new ArrayList();

	/*
	 * A map of (String-based-Navigator-Content-Extension-IDs,
	 * NavigatorContentExtension-objects)-pairs
	 */
	private final Map contentExtensions = new HashMap();

	private StructuredViewerManager structuredViewerManager;

	private ITreeContentProvider[] rootContentProviders;

	private WeakHashMap contributionMemory;

	private ITreeContentProvider contentProvider;

	private ILabelProvider labelProvider;

	private final VisibilityAssistant assistant;

	private NavigatorFilterService navigatorFilterService;

	private INavigatorSorterService navigatorSorterService;

	private IDescriptionProvider descriptionProvider;

	/**
	 * 
	 */
	public NavigatorContentService(String aViewerId) {
		super();
		aViewerId = aViewerId != null ? aViewerId : ""; //$NON-NLS-1$
		viewerDescriptor = VIEWER_DESCRIPTOR_REGISTRY
				.getNavigatorViewerDescriptor(aViewerId);
		assistant = new VisibilityAssistant(viewerDescriptor);
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

	public String[] getVisibleExtensionIds() {

		List visibleExtensionIds = new ArrayList();

		NavigatorContentDescriptor[] descriptors = CONTENT_DESCRIPTOR_REGISTRY
				.getAllContentDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			if (assistant.isVisible(descriptors[i].getId()))
				visibleExtensionIds.add(descriptors[i].getId());
		}
		if (visibleExtensionIds.isEmpty())
			return NO_EXTENSION_IDS;
		return (String[]) visibleExtensionIds
				.toArray(new String[visibleExtensionIds.size()]);

	}

	public INavigatorContentDescriptor[] getVisibleExtensions() {
		List visibleDescriptors = new ArrayList();

		NavigatorContentDescriptor[] descriptors = CONTENT_DESCRIPTOR_REGISTRY
				.getAllContentDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			if (assistant.isVisible(descriptors[i].getId()))
				visibleDescriptors.add(descriptors[i]);
		}
		if (visibleDescriptors.isEmpty())
			return NO_DESCRIPTORS;
		return (INavigatorContentDescriptor[]) visibleDescriptors
				.toArray(new INavigatorContentDescriptor[visibleDescriptors
						.size()]);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.INavigatorContentService#bindExtensions(java.lang.String[],
	 *      boolean)
	 */
	public INavigatorContentDescriptor[] bindExtensions(String[] extensionIds,
			boolean isRoot) {
		if (extensionIds == null || extensionIds.length == 0)
			return NO_DESCRIPTORS;

		for (int i = 0; i < extensionIds.length; i++)
			assistant.bindExtensions(extensionIds, isRoot);
		Set boundDescriptors = new HashSet();
		INavigatorContentDescriptor descriptor;
		for (int i = 0; i < extensionIds.length; i++) {
			descriptor = CONTENT_DESCRIPTOR_REGISTRY
					.getContentDescriptor(extensionIds[i]);
			if (descriptor != null)
				boundDescriptors.add(descriptor);
		}

		if (boundDescriptors.size() == 0)
			return NO_DESCRIPTORS;
		return (INavigatorContentDescriptor[]) boundDescriptors
				.toArray(new INavigatorContentDescriptor[boundDescriptors
						.size()]);

	}

	public INavigatorContentDescriptor[] activateExtensions(
			String[] extensionIds, boolean toDeactivateAllOthers) {

		Set activatedDescriptors = new HashSet();
		final String viewerId = viewerDescriptor.getViewerId();
		NavigatorActivationService.getInstance().activateNavigatorExtension(
				viewerId, extensionIds, true);
		for (int extId = 0; extId < extensionIds.length; extId++) {
			activatedDescriptors.add(CONTENT_DESCRIPTOR_REGISTRY
					.getContentDescriptor(extensionIds[extId]));
		}

		if (toDeactivateAllOthers) {
			NavigatorContentDescriptor[] descriptors = CONTENT_DESCRIPTOR_REGISTRY
					.getAllContentDescriptors();
			List descriptorList = new ArrayList(Arrays.asList(descriptors));

			for (int descriptorIndx = 0; descriptorIndx < descriptors.length; descriptorIndx++)
				for (int extId = 0; extId < extensionIds.length; extId++)
					if (descriptors[descriptorIndx].getId().equals(
							extensionIds[extId]))
						descriptorList.remove(descriptors[descriptorIndx]);

			String[] deactivatedExtensions = new String[descriptorList.size()];
			for (int i = 0; i < descriptorList.size(); i++) {
				INavigatorContentDescriptor descriptor = (INavigatorContentDescriptor) descriptorList
						.get(i);
				deactivatedExtensions[i] = descriptor.getId();
			}
			NavigatorActivationService.getInstance()
					.activateNavigatorExtension(viewerId,
							deactivatedExtensions, false);
		}

		if (activatedDescriptors.size() == 0)
			return NO_DESCRIPTORS;
		return (INavigatorContentDescriptor[]) activatedDescriptors
				.toArray(new NavigatorContentDescriptor[activatedDescriptors
						.size()]);
	}

	public INavigatorContentDescriptor[] deactivateExtensions(
			String[] extensionIds, boolean toEnableAllOthers) {

		Set activatedDescriptors = new HashSet();
		final String viewerId = viewerDescriptor.getViewerId();
		NavigatorActivationService.getInstance().activateNavigatorExtension(
				viewerId, extensionIds, false);

		if (toEnableAllOthers) {
			NavigatorContentDescriptor[] descriptors = CONTENT_DESCRIPTOR_REGISTRY
					.getAllContentDescriptors();
			List descriptorList = new ArrayList(Arrays.asList(descriptors));

			for (int descriptorIndx = 0; descriptorIndx < descriptors.length; descriptorIndx++)
				for (int extId = 0; extId < extensionIds.length; extId++)
					if (descriptors[descriptorIndx].getId().equals(
							extensionIds[extId]))
						descriptorList.remove(descriptors[descriptorIndx]);

			String[] activatedExtensions = new String[descriptorList.size()];
			for (int i = 0; i < descriptorList.size(); i++) {
				NavigatorContentDescriptor descriptor = (NavigatorContentDescriptor) descriptorList
						.get(i);
				activatedExtensions[i] = descriptor.getId();
				activatedDescriptors.add(descriptor);
			}
			NavigatorActivationService.getInstance()
					.activateNavigatorExtension(viewerId, activatedExtensions,
							true);
		}
		if (activatedDescriptors.size() == 0)
			return NO_DESCRIPTORS;

		return (INavigatorContentDescriptor[]) activatedDescriptors
				.toArray(new NavigatorContentDescriptor[activatedDescriptors
						.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#createCommonContentProvider()
	 */
	public ITreeContentProvider createCommonContentProvider() {
		if (contentProvider != null)
			return contentProvider;
		synchronized (this) {
			if (contentProvider == null)
				contentProvider = new NavigatorContentServiceContentProvider(
						this);
		}
		return contentProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#createCommonLabelProvider()
	 */
	public ILabelProvider createCommonLabelProvider() {
		if (labelProvider != null)
			return labelProvider;
		synchronized (this) {
			if (labelProvider == null)
				labelProvider = new NavigatorContentServiceLabelProvider(this);
		}
		return labelProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.INavigatorContentService#createCommonDescriptionProvider()
	 */
	public IDescriptionProvider createCommonDescriptionProvider() {
		if (descriptionProvider != null)
			return descriptionProvider;
		synchronized (this) {
			if (descriptionProvider == null)
				descriptionProvider = new NavigatorContentServiceDescriptionProvider(
						this);
		}
		return descriptionProvider;
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
		assistant.dispose();
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

			rootContentProviders = extractContentProviders(findRootContentExtensions(aNewInput));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.INavigatorContentService#findEnabledContentDescriptors(java.lang.Object)
	 */
	public Set findEnabledContentDescriptors(Object anElement) {
		return findDescriptorsByTriggerPoint(anElement);
	}

	public IExtensionStateModel findStateModel(String anExtensionId) {
		return getExtension(
				CONTENT_DESCRIPTOR_REGISTRY.getContentDescriptor(anExtensionId))
				.getStateModel();
	}

	/**
	 * Return a set of content providers that could provide a parent for the
	 * given element. These content extensions are determined by consulting the
	 * <b>possibleChildren</b> expression in the <b>navigatorContent</b>
	 * extension.
	 * 
	 * <p>
	 * Clients that wish to tap into the link with editor support must describe
	 * all of their possible children in their <b>possibleChildren</b>
	 * expressions.
	 * </p>
	 * 
	 * @param anElement
	 *            An element from the tree (generally from a setSelection()
	 *            method).
	 * @return The set of content providers that may be able to provide a
	 *         parent.
	 */
	public ITreeContentProvider[] findParentContentProviders(Object anElement) {
		return extractContentProviders(findContentExtensionsByTriggerPoint(anElement));
	}

	/**
	 * <p>
	 * Return all of the content providers that are relevant for the viewer. The
	 * viewer is determined by the ID used to create the
	 * INavigatorContentService ({@link #getViewerId() }). See
	 * {@link #createCommonContentProvider() } for more information about how
	 * content providers are located for the root of the viewer. The root
	 * content providers are calculated once. If a new element is supplied, a
	 * client must call {@link #update() } prior in order to reset the
	 * calculated root providers.
	 * </p>
	 * 
	 * @param anElement
	 *            An element from the tree (generally the input of the viewer)
	 * @return The set of content providers that can provide root elements for a
	 *         viewer.
	 */
	public ITreeContentProvider[] findRootContentProviders(Object anElement) {
		if (rootContentProviders != null)
			return rootContentProviders;
		synchronized (this) {
			if (rootContentProviders == null) {
				rootContentProviders = extractContentProviders(findRootContentExtensions(anElement));

			}
		}
		return rootContentProviders;
	}

	/**
	 * Return all of the content providers that are enabled for the given
	 * element. An 'enabled' content provider is either the (1) source of the
	 * element (the element was returned as a child of its parent by the content
	 * provider) or (2) a content extension which describes the element in its
	 * <b>triggerPoints</b> expression.
	 * 
	 * @param anElement
	 *            An element from the tree (generally the element expanded by
	 *            the user)
	 * @return The set of content providers that can provide valid children for
	 *         the element.
	 */
	public ITreeContentProvider[] findRelevantContentProviders(Object anElement) {
		return extractContentProviders(findContentExtensionsByTriggerPoint(anElement));
	}

	/**
	 * 
	 * Return all of the label providers that are enabled for the given element.
	 * A label provider is 'enabled' if its corresponding content provider
	 * returned the element, or the element is described in the content
	 * extension's <b>triggerPoints</b> expression.
	 * 
	 * @param anElement
	 *            An element from the tree (any element contributed to the
	 *            tree).
	 * @return The set of label providers that may be able to provide a valid
	 *         (non-null) label.
	 */
	public ILabelProvider[] findRelevantLabelProviders(Object anElement) {
		return extractLabelProviders(findContentExtensionsWithPossibleChild(
				anElement, false));
	}

	/**
	 * 
	 * @param anElement
	 *            The element from the viewer or to be added to the viewer
	 * @return An array of content extensions that describe the object in their
	 *         <b>triggerPoints</b> expression.
	 */
	public Set findContentExtensionsByTriggerPoint(Object anElement) {
		return findContentExtensionsByTriggerPoint(anElement, true);
	}

	/**
	 * 
	 * @param anElement
	 *            The element from the viewer or to be added to the viewer
	 * @param toLoadIfNecessary
	 *            True will force the load of the extension, False will not
	 * @return An array of content extensions that describe the object in their
	 *         <b>triggerPoints</b> expression.
	 */
	public Set findContentExtensionsByTriggerPoint(Object anElement,
			boolean toLoadIfNecessary) {
		Set enabledDescriptors = findDescriptorsByTriggerPoint(anElement);
		return extractDescriptorInstances(enabledDescriptors, toLoadIfNecessary);
	}

	/**
	 * 
	 * @param anElement
	 *            The element from the viewer or to be added to the viewer *
	 * @return An array of content extensions that describe the object in their
	 *         <b>possibleChildren</b> expression.
	 */
	public Set findContentExtensionsWithPossibleChild(Object anElement) {
		return findContentExtensionsWithPossibleChild(anElement, true);
	}

	/**
	 * 
	 * @param anElement
	 *            The element from the viewer or to be added to the viewer
	 * @param toLoadIfNecessary
	 *            True will force the load of the extension, False will not
	 * @return An array of content extensions that describe the object in their
	 *         <b>possibleChildren</b> expression.
	 */
	public Set findContentExtensionsWithPossibleChild(Object anElement,
			boolean toLoadIfNecessary) {
		Set enabledDescriptors = findDescriptorsWithPossibleChild(anElement);
		return extractDescriptorInstances(enabledDescriptors, toLoadIfNecessary);
	}

	/**
	 * Remember that the elements in the given array came from the given source
	 * 
	 * @param source
	 *            The descriptor of the extension that contributed the set of
	 *            elements.
	 * @param elements
	 *            An array of elements from the given source.
	 */
	public synchronized void rememberContribution(
			NavigatorContentDescriptor source, Object[] elements) {

		if (source != null && elements != null)
			for (int i = 0; i < elements.length; i++)
				getContributionMemory().put(elements[i], source);
	}

	/**
	 * Remember that the elements in the given array came from the given source
	 * 
	 * @param source
	 *            The descriptor of the extension that contributed the set of
	 *            elements.
	 * @param element
	 *            An element from the given source.
	 */
	public synchronized void rememberContribution(
			NavigatorContentDescriptor source, Object element) {
		if (source != null && element != null)
			getContributionMemory().put(element, source);
	}

	/**
	 * 
	 * @param element
	 *            The element contributed by the descriptor to be returned
	 * @return The descriptor that contributed the element or null.
	 * @see #findContentExtensionsWithPossibleChild(Object)
	 */
	public NavigatorContentDescriptor getSourceOfContribution(Object element) {
		return (NavigatorContentDescriptor) getContributionMemory()
				.get(element);
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

	public Set findDescriptorsByTriggerPoint(Object anElement) {

		NavigatorContentDescriptor descriptor = getSourceOfContribution(anElement);
		Set result = new HashSet();
		if (descriptor != null)
			result.add(descriptor);
		result.addAll(CONTENT_DESCRIPTOR_REGISTRY
				.findDescriptorsForTriggerPoint(anElement, assistant));
		return result;
	}

	// update registry to use possibleChildren expression
	public Set findDescriptorsWithPossibleChild(Object anElement) {

		NavigatorContentDescriptor descriptor = getSourceOfContribution(anElement);
		Set result = new HashSet();
		if (descriptor != null)
			result.add(descriptor);
		result.addAll(CONTENT_DESCRIPTOR_REGISTRY
				.findDescriptorsForPossibleChild(anElement, assistant));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#onExtensionActivation(java.lang.String,
	 *      java.lang.String, boolean)
	 */
	public void onExtensionActivation(String aViewerId,
			String[] aNavigatorExtensionId, boolean toEnable) {
		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.INavigatorContentService#update()
	 */
	public void update() {
		rootContentProviders = null;
		if (structuredViewerManager != null)
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

	/**
	 * 
	 * @param aDescriptorKey
	 *            A descriptor
	 * @return The cached NavigatorContentExtension from the descriptor
	 */
	public final NavigatorContentExtension getExtension(
			INavigatorContentDescriptor aDescriptorKey) {
		return getExtension(aDescriptorKey, true);
	}

	public final NavigatorContentExtension getExtension(
			INavigatorContentDescriptor aDescriptorKey,
			boolean toLoadIfNecessary) {
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
				contentExtensions.put(aDescriptorKey,
						(extension = new NavigatorContentExtension(
								(NavigatorContentDescriptor) aDescriptorKey,
								this, structuredViewerManager)));
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
	public INavigatorViewerDescriptor getViewerDescriptor() {
		return viewerDescriptor;
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
						NavigatorPlugin.logError(0,
								"Could not restore state for Common Navigator content extension" //$NON-NLS-1$
										+ element.getId(), exception);

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

	protected final Set findRootContentExtensions(Object anElement) {
		return findRootContentExtensions(anElement, true);
	}

	protected final Set findRootContentExtensions(Object anElement,
			boolean toRespectViewerRoots) {

		SortedSet rootExtensions = new TreeSet(EXTENSION_COMPARATOR);
		if (toRespectViewerRoots
				&& viewerDescriptor.hasOverriddenRootExtensions()) {

			NavigatorContentDescriptor[] descriptors = CONTENT_DESCRIPTOR_REGISTRY
					.getAllContentDescriptors();

			NavigatorContentExtension extension = null;
			for (int i = 0; i < descriptors.length; i++) {
				if (isActive(descriptors[i].getId())
						&& isRootExtension(descriptors[i].getId())) {
					extension = getExtension(descriptors[i]);
					if (!extension.hasLoadingFailed())
						rootExtensions.add(extension);
				}
			}
		}
		if (rootExtensions.isEmpty())
			return findContentExtensionsByTriggerPoint(anElement);
		return rootExtensions;
	}

	public boolean isActive(String anExtensionId) {
		return assistant.isActive(anExtensionId);
	}

	public boolean isVisible(String anExtensionId) {
		return assistant.isVisible(anExtensionId);
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
	 * @see org.eclipse.ui.navigator.INavigatorContentService#getFilterService()
	 */
	public INavigatorFilterService getFilterService() {
		if (navigatorFilterService == null)
			navigatorFilterService = new NavigatorFilterService(this);
		return navigatorFilterService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.INavigatorContentService#getFilterService()
	 */
	public INavigatorSorterService getSorterService() {
		if (navigatorSorterService == null)
			navigatorSorterService = new NavigatorSorterService(this);
		return navigatorSorterService;
	}

	protected boolean isRootExtension(String anExtensionId) {
		return assistant.isRootExtension(anExtensionId);
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

	private ITreeContentProvider[] extractContentProviders(
			Set theDescriptorInstances) {
		if (theDescriptorInstances.size() == 0)
			return NO_CONTENT_PROVIDERS;
		List resultProvidersList = new ArrayList();
		for (Iterator itr = theDescriptorInstances.iterator(); itr.hasNext();)
			resultProvidersList.add(((NavigatorContentExtension) itr.next())
					.getContentProvider());
		return (ITreeContentProvider[]) resultProvidersList
				.toArray(new ITreeContentProvider[resultProvidersList.size()]);
	}

	private Set extractDescriptorInstances(Set theDescriptors,
			boolean toLoadAllIfNecessary) {
		if (theDescriptors.size() == 0)
			return Collections.EMPTY_SET;
		Set resultInstances = new TreeSet(EXTENSION_COMPARATOR);
		for (Iterator descriptorIter = theDescriptors.iterator(); descriptorIter
				.hasNext();) {
			NavigatorContentExtension extension = getExtension(
					(NavigatorContentDescriptor) descriptorIter.next(),
					toLoadAllIfNecessary);
			if (extension != null) {
				resultInstances.add(extension);

			}
		}
		return resultInstances;
	}

	private ILabelProvider[] extractLabelProviders(Set theDescriptorInstances) {
		if (theDescriptorInstances.size() == 0)
			return NO_LABEL_PROVIDERS;
		List resultProvidersList = new ArrayList();
		for (Iterator itr = theDescriptorInstances.iterator(); itr.hasNext();)
			resultProvidersList.add(((NavigatorContentExtension) itr.next())
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
			return ((NavigatorContentExtension) lvalue).getDescriptor()
					.getPriority()
					- ((NavigatorContentExtension) rvalue).getDescriptor()
							.getPriority();
		}
	};
}
