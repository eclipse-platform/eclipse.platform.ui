/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Fair Issac Corp - bug 287103 - NCSLabelProvider does not properly handle overrides
 *******************************************************************************/
package org.eclipse.ui.internal.navigator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.navigator.dnd.NavigatorDnDService;
import org.eclipse.ui.internal.navigator.extensions.ExtensionSequenceNumberComparator;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentDescriptorManager;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentExtension;
import org.eclipse.ui.internal.navigator.extensions.NavigatorViewerDescriptor;
import org.eclipse.ui.internal.navigator.extensions.NavigatorViewerDescriptorManager;
import org.eclipse.ui.internal.navigator.extensions.SafeDelegateTreeContentProvider;
import org.eclipse.ui.internal.navigator.extensions.StructuredViewerManager;
import org.eclipse.ui.internal.navigator.sorters.NavigatorSorterService;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.eclipse.ui.navigator.IExtensionActivationListener;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.IMementoAware;
import org.eclipse.ui.navigator.INavigatorActivationService;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentExtension;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.INavigatorContentServiceListener;
import org.eclipse.ui.navigator.INavigatorDnDService;
import org.eclipse.ui.navigator.INavigatorFilterService;
import org.eclipse.ui.navigator.INavigatorPipelineService;
import org.eclipse.ui.navigator.INavigatorSaveablesService;
import org.eclipse.ui.navigator.INavigatorSorterService;
import org.eclipse.ui.navigator.INavigatorViewerDescriptor;
import org.osgi.service.prefs.BackingStoreException;

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

	/**
	 *
	 */
	public static final String WIDGET_KEY = "org.eclipse.ui.navigator"; //$NON-NLS-1$

	private static final NavigatorContentDescriptorManager CONTENT_DESCRIPTOR_REGISTRY = NavigatorContentDescriptorManager
			.getInstance();

	private static final NavigatorViewerDescriptorManager VIEWER_DESCRIPTOR_REGISTRY = NavigatorViewerDescriptorManager
			.getInstance();

	private static final ITreeContentProvider[] NO_CONTENT_PROVIDERS = new ITreeContentProvider[0];

	private static final ILabelProvider[] NO_LABEL_PROVIDERS = new ILabelProvider[0];

	private static final INavigatorContentDescriptor[] NO_DESCRIPTORS = new INavigatorContentDescriptor[0];

	private static final String[] NO_EXTENSION_IDS = new String[0];

	private final NavigatorViewerDescriptor viewerDescriptor;

	private final List<INavigatorContentServiceListener> listeners = new ArrayList<INavigatorContentServiceListener>();

	/*
	 * A map of (String-based-Navigator-Content-Extension-IDs,
	 * NavigatorContentExtension-objects)-pairs
	 */
	private final Map<INavigatorContentDescriptor, NavigatorContentExtension> contentExtensions = new HashMap<INavigatorContentDescriptor, NavigatorContentExtension>();

	private StructuredViewerManager structuredViewerManager;

	private ITreeContentProvider[] rootContentProviders;

	private ITreeContentProvider contentProvider;

	/*
	 * Used when providing objects to the CommonViewer by the contentProvider
	 * to record the object/description associations which are when stored
	 * in the Tree associated with the viewer.
	 */
	private Map<Object, INavigatorContentDescriptor> contributionMemory;
	private Map<Object, INavigatorContentDescriptor> contributionMemoryFirstClass;

	private ILabelProvider labelProvider;

	private final VisibilityAssistant assistant;

	private NavigatorFilterService navigatorFilterService;

	private NavigatorSorterService navigatorSorterService;

	private INavigatorPipelineService navigatorPipelineService;

	private INavigatorDnDService navigatorDnDService;

	private INavigatorActivationService navigatorActivationService;

	private NavigatorSaveablesService navigatorSaveablesService;

	private NavigatorExtensionStateService navigatorExtensionStateService;

	private IDescriptionProvider descriptionProvider;

	private boolean contentProviderInitialized;

	private boolean labelProviderInitialized;

	private boolean isDisposed;

	/**
	 * @param aViewerId
	 *            The viewer id for this content service; normally from the
	 *            <b>org.eclipse.ui.views</b> extension.
	 */
	public NavigatorContentService(String aViewerId) {
		super();
		aViewerId = aViewerId != null ? aViewerId : ""; //$NON-NLS-1$
		viewerDescriptor = VIEWER_DESCRIPTOR_REGISTRY
				.getNavigatorViewerDescriptor(aViewerId);
		assistant = new VisibilityAssistant(viewerDescriptor,
				getActivationService());
		getActivationService().addExtensionActivationListener(this);
		contributionMemory = new HashMap<Object, INavigatorContentDescriptor>();
		contributionMemoryFirstClass = new HashMap<Object, INavigatorContentDescriptor>();
	}

	/**
	 * @param aViewerId
	 *            The viewer id for this content service; normally from the
	 *            <b>org.eclipse.ui.views</b> extension.
	 * @param aViewer
	 *            The viewer that this content service will be associated with.
	 */
	public NavigatorContentService(String aViewerId, StructuredViewer aViewer) {
		this(aViewerId);
		structuredViewerManager = new StructuredViewerManager(aViewer, this);
	}

	@Override
	public String[] getVisibleExtensionIds() {

		List<String> visibleExtensionIds = new ArrayList<String>();

		NavigatorContentDescriptor[] descriptors = CONTENT_DESCRIPTOR_REGISTRY
				.getAllContentDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			if (assistant.isVisible(descriptors[i].getId())) {
				visibleExtensionIds.add(descriptors[i].getId());
			}
		}
		if (visibleExtensionIds.isEmpty()) {
			return NO_EXTENSION_IDS;
		}
		return visibleExtensionIds
				.toArray(new String[visibleExtensionIds.size()]);

	}

	@Override
	public INavigatorContentDescriptor[] getVisibleExtensions() {
		List<NavigatorContentDescriptor> visibleDescriptors = new ArrayList<NavigatorContentDescriptor>();

		NavigatorContentDescriptor[] descriptors = CONTENT_DESCRIPTOR_REGISTRY
				.getAllContentDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			if (assistant.isVisible(descriptors[i].getId())) {
				visibleDescriptors.add(descriptors[i]);
			}
		}
		if (visibleDescriptors.isEmpty()) {
			return NO_DESCRIPTORS;
		}
		return visibleDescriptors
				.toArray(new INavigatorContentDescriptor[visibleDescriptors
						.size()]);

	}

	/* package */INavigatorContentDescriptor[] getActiveDescriptorsWithSaveables() {
		List<NavigatorContentDescriptor> result = new ArrayList<NavigatorContentDescriptor>();

		NavigatorContentDescriptor[] descriptors = CONTENT_DESCRIPTOR_REGISTRY
				.getContentDescriptorsWithSaveables();
		for (int i = 0; i < descriptors.length; i++) {
			if (assistant.isVisible(descriptors[i].getId())
					&& assistant.isActive(descriptors[i])) {
				result.add(descriptors[i]);
			}
		}
		if (result.isEmpty()) {
			return NO_DESCRIPTORS;
		}
		return result
				.toArray(new INavigatorContentDescriptor[result.size()]);

	}

	@Override
	public INavigatorContentDescriptor[] bindExtensions(String[] extensionIds,
			boolean isRoot) {
		if (extensionIds == null || extensionIds.length == 0) {
			return NO_DESCRIPTORS;
		}

		for (int i = 0; i < extensionIds.length; i++) {
			assistant.bindExtensions(extensionIds, isRoot);
		}
		Set<INavigatorContentDescriptor> boundDescriptors = new HashSet<INavigatorContentDescriptor>();
		INavigatorContentDescriptor descriptor;
		for (int i = 0; i < extensionIds.length; i++) {
			descriptor = CONTENT_DESCRIPTOR_REGISTRY
					.getContentDescriptor(extensionIds[i]);
			if (descriptor != null) {
				boundDescriptors.add(descriptor);
			}
		}

		if (boundDescriptors.size() == 0) {
			return NO_DESCRIPTORS;
		}

		if (Policy.DEBUG_EXTENSION_SETUP) {
			System.out.println("bindExtensions: " + //$NON-NLS-1$
					boundDescriptors);
		}
		return boundDescriptors
				.toArray(new INavigatorContentDescriptor[boundDescriptors
						.size()]);

	}

	@Override
	public ITreeContentProvider createCommonContentProvider() {
		if (contentProviderInitialized) {
			return contentProvider;
		}
		synchronized (this) {
			if (contentProvider == null) {
				contentProvider = new NavigatorContentServiceContentProvider(
						this);
			}
			contentProviderInitialized = true;
		}
		return contentProvider;
	}

	@Override
	public ILabelProvider createCommonLabelProvider() {
		if (labelProviderInitialized) {
			return labelProvider;
		}
		synchronized (this) {
			if (labelProvider == null) {
				labelProvider = new NavigatorContentServiceLabelProvider(this);
			}
			labelProviderInitialized = true;
		}
		return labelProvider;
	}

	@Override
	public IDescriptionProvider createCommonDescriptionProvider() {
		if (descriptionProvider != null) {
			return descriptionProvider;
		}
		synchronized (this) {
			if (descriptionProvider == null) {
				descriptionProvider = new NavigatorContentServiceDescriptionProvider(
						this);
			}
		}
		return descriptionProvider;
	}

	@Override
	public void dispose() {
		if (navigatorSaveablesService != null) {
			assistant.removeListener(navigatorSaveablesService);
		}
		if (navigatorSorterService != null) {
			assistant.removeListener(navigatorSorterService);
		}
		synchronized (this) {
			for (Iterator<NavigatorContentExtension> contentItr = contentExtensions.values().iterator(); contentItr
					.hasNext();) {
				contentItr.next().dispose();
			}
		}
		getActivationService().removeExtensionActivationListener(this);
		assistant.dispose();
		isDisposed = true;
	}

	protected void updateService(Viewer aViewer, Object anOldInput,
			Object aNewInput) {

		// Prevents the world from being started again once we have been disposed.  In
		// the dispose process, the ContentViewer will call setInput() on the
		// NavigatorContentServiceContentProvider, which gets us here
		if (isDisposed)
			return;
		synchronized (this) {

			if (structuredViewerManager == null) {
				structuredViewerManager = new StructuredViewerManager((StructuredViewer) aViewer, this);
				structuredViewerManager.inputChanged(anOldInput, aNewInput);
			} else {
				structuredViewerManager.inputChanged(aViewer, anOldInput,
						aNewInput);
			}

			for (Iterator<NavigatorContentExtension> contentItr = contentExtensions.values().iterator(); contentItr
					.hasNext();) {
				NavigatorContentExtension ext = contentItr
						.next();
				if (ext.isLoaded()) {
					structuredViewerManager
							.initialize(ext.internalGetContentProvider());
				}
			}

			rootContentProviders = extractContentProviders(findRootContentExtensions(aNewInput));
		}
	}

	@Override
	public IExtensionStateModel findStateModel(String anExtensionId) {
		if (anExtensionId == null) {
			return null;
		}
		INavigatorContentDescriptor desc = CONTENT_DESCRIPTOR_REGISTRY
				.getContentDescriptor(anExtensionId);
		if (desc == null) {
			return null;
		}
		INavigatorContentExtension ext = getExtension(desc);
		if (ext == null) {
			return null;
		}
		return ext.getStateModel();
	}

	/**
	 * <p>
	 * Return all of the content providers that are relevant for the viewer. The
	 * viewer is determined by the ID used to create the
	 * INavigatorContentService ({@link #getViewerId() }). See
	 * {@link #createCommonContentProvider() } for more information about how
	 * content providers are located for the root of the viewer. The root
	 * content providers are calculated once. If a new element is supplied, a
	 * client must call {@link #update() } prior in order to reset the calculated
	 * root providers.
	 * </p>
	 *
	 * @param anElement
	 *            An element from the tree (generally the input of the viewer)
	 * @return The set of content providers that can provide root elements for a
	 *         viewer.
	 */
	public ITreeContentProvider[] findRootContentProviders(Object anElement) {
		if (rootContentProviders != null) {
			return rootContentProviders;
		}
		synchronized (this) {
			if (rootContentProviders == null) {
				rootContentProviders = extractContentProviders(findRootContentExtensions(anElement));

			}
		}
		return rootContentProviders;
	}

	/**
	 * Returns the list of extensions that should be considered as possible
	 * contributors of CNF artifacts (labels, sorters, ...). The algorithm
	 * first considers the source of contribution and its overrides, and then
	 * any possibleChildren and their overrides in order.
	 *
	 * @param anElement
	 *            An element from the tree (any element contributed to the
	 *            tree).
	 * @return A Collection of NCEs sorted in the correct order for label provider application
	 */
	public Collection<NavigatorContentExtension> findPossibleLabelExtensions(Object anElement) {
		LinkedHashSet<NavigatorContentExtension> contributors = new LinkedHashSet<NavigatorContentExtension>();
		INavigatorContentDescriptor sourceDescriptor = getSourceOfContribution(anElement);

		// This is a TreeSet sorted ascending
		Set<INavigatorContentDescriptor> possibleChildDescriptors = findDescriptorsWithPossibleChild(anElement, false);

		// Add the source so that it gets sorted into the right place
		if (sourceDescriptor != null) {
			possibleChildDescriptors.add(sourceDescriptor);
		}

		for (Iterator<INavigatorContentDescriptor> iter = possibleChildDescriptors.iterator(); iter.hasNext();) {
			NavigatorContentDescriptor ncd = (NavigatorContentDescriptor) iter.next();
			findOverridingLabelExtensions(anElement, ncd, contributors);
		}

		return contributors;
	}

	private void findOverridingLabelExtensions(Object anElement,
			INavigatorContentDescriptor descriptor, LinkedHashSet<NavigatorContentExtension> contributors) {
		ListIterator iter = ((NavigatorContentDescriptor) descriptor).getOverridingExtensionsListIterator(false);
		while (iter.hasPrevious()) {
			INavigatorContentDescriptor child = (INavigatorContentDescriptor) iter.previous();
			if (assistant.isVisibleAndActive(child) && child.isPossibleChild(anElement)) {
				findOverridingLabelExtensions(anElement, child, contributors);
			}
		}
		contributors.add(getExtension(descriptor));
	}

	/**
	 *
	 * The label provider that is are enabled for the given element.
	 * A label provider is 'enabled' if its corresponding content provider
	 * returned the element.
	 *
	 * @param anElement
	 *            An element from the tree (any element contributed to the
	 *            tree).
	 * @return The label provider
	 */
	public ILabelProvider[] findRelevantLabelProviders(Object anElement) {
		Collection<NavigatorContentExtension> extensions = findPossibleLabelExtensions(anElement);

		if (extensions.size() == 0) {
			return NO_LABEL_PROVIDERS;
		}
		List<ICommonLabelProvider> resultProvidersList = new ArrayList<ICommonLabelProvider>();
		for (Iterator<NavigatorContentExtension> itr = extensions.iterator(); itr.hasNext();) {
			resultProvidersList.add(itr.next().getLabelProvider());
		}
		return resultProvidersList.toArray(new ILabelProvider[resultProvidersList.size()]);
	}

	/**
	 * Search for extensions that declare the given element in their
	 * <b>triggerPoints</b> expression.
	 *
	 * @param anElement
	 *            The element to use in the query
	 * @return The set of {@link INavigatorContentExtension}s that are
	 *         <i>visible</i> and <i>active</i> for this content service and
	 *         either declared through a
	 *         <b>org.eclipse.ui.navigator.viewer/viewerContentBinding</b> to be
	 *         a root element or have a <b>triggerPoints</b> expression that is
	 *         <i>enabled</i> for the given element.
	 */
	@Override
	public Set<NavigatorContentExtension> findRootContentExtensions(Object anElement) {
		return findRootContentExtensions(anElement, true);
	}

	/**
	 * Search for extensions that declare the given element in their
	 * <b>triggerPoints</b> expression.
	 *
	 * @param anElement
	 *            The element to use in the query
	 * @param toRespectViewerRoots
	 *            True respect the <b>viewerContentBinding</b>s, False will look
	 *            only for matching <b>triggerPoints</b> expressions.
	 * @return The set of {@link INavigatorContentExtension}s that are
	 *         <i>visible</i> and <i>active</i> for this content service and
	 *         either declared through a
	 *         <b>org.eclipse.ui.navigator.viewer/viewerContentBinding</b> to be
	 *         a root element or have a <b>triggerPoints</b> expression that is
	 *         <i>enabled</i> for the given element.
	 */
	public Set<NavigatorContentExtension> findRootContentExtensions(Object anElement,
			boolean toRespectViewerRoots) {

		SortedSet<NavigatorContentExtension> rootExtensions = new TreeSet<NavigatorContentExtension>(
				ExtensionSequenceNumberComparator.INSTANCE);
		if (toRespectViewerRoots
				/*&& viewerDescriptor.hasOverriddenRootExtensions()*/) {

			NavigatorContentDescriptor[] descriptors = CONTENT_DESCRIPTOR_REGISTRY
					.getAllContentDescriptors();

			NavigatorContentExtension extension = null;
			for (int i = 0; i < descriptors.length; i++) {
				if (isActive(descriptors[i].getId())
						&& isRootExtension(descriptors[i].getId())) {
					extension = getExtension(descriptors[i]);
					if (!extension.hasLoadingFailed()) {
						rootExtensions.add(extension);
					}
				}
			}
		}
		if (rootExtensions.isEmpty()) {
			return findContentExtensionsByTriggerPoint(anElement);
		}
		return rootExtensions;
	}

	/**
	 * Search for extensions that declare the given element in their
	 * <b>possibleChildren</b> expression.
	 *
	 * @param anElement
	 *            The element to use in the query
	 * @return The set of {@link INavigatorContentExtension}s that are
	 *         <i>visible</i> and <i>active</i> for this content service and
	 *         have a <b>possibleChildren</b> expression that is <i>enabled</i>
	 *         for the given element.
	 */
	public Set<NavigatorContentExtension> findOverrideableContentExtensionsForPossibleChild(
			Object anElement) {
		Set<NavigatorContentExtension> overrideableExtensions = new TreeSet<NavigatorContentExtension>(
				ExtensionSequenceNumberComparator.INSTANCE);
		Set<INavigatorContentDescriptor> descriptors = findDescriptorsWithPossibleChild(anElement, false);
		for (Iterator<INavigatorContentDescriptor> iter = descriptors.iterator(); iter.hasNext();) {
			INavigatorContentDescriptor descriptor = iter
					.next();
			if (descriptor.hasOverridingExtensions()) {
				overrideableExtensions.add(getExtension(descriptor));
			}
		}
		return overrideableExtensions;
	}

	@Override
	public INavigatorContentDescriptor getContentDescriptorById(
			String anExtensionId) {
		return CONTENT_DESCRIPTOR_REGISTRY.getContentDescriptor(anExtensionId);
	}

	/**
	 *
	 * @param anExtensionId
	 *            The id used to define the
	 *            <b>org.eclipse.ui.navigator.navigatorContent
	 *            /navigatorContent</b> extension.
	 * @return An instance of the content extension for the given extension id.
	 *         May return <b>null</b> if the id is invalid.
	 */
	@Override
	public INavigatorContentExtension getContentExtensionById(
			String anExtensionId) {
		NavigatorContentDescriptor descriptor = CONTENT_DESCRIPTOR_REGISTRY
				.getContentDescriptor(anExtensionId);
		if (descriptor != null)
			return getExtension(descriptor);
		return null;
	}

	/**
	 * Search for extensions that declare the given element in their
	 * <b>triggerPoints</b> expression.
	 *
	 * @param anElement
	 *            The element to use in the query
	 * @return The set of {@link INavigatorContentExtension}s that are
	 *         <i>visible</i> and <i>active</i> for this content service and
	 *         have a <b>triggerPoints</b> expression that is <i>enabled</i> for
	 *         the given element.
	 */
	@Override
	public Set<NavigatorContentExtension> findContentExtensionsByTriggerPoint(Object anElement) {
		return findContentExtensionsByTriggerPoint(anElement, true, !CONSIDER_OVERRIDES);
	}

	/**
	 * Search for extensions that declare the given element in their
	 * <b>triggerPoints</b> expression.
	 *
	 * @param anElement
	 *            The element to use in the query
	 * @param toLoadIfNecessary
	 *            True will force the load of the extension, False will not
	 * @param computeOverrides
	 * @return The set of {@link INavigatorContentExtension}s that are
	 *         <i>visible</i> and <i>active</i> for this content service and
	 *         have a <b>triggerPoints</b> expression that is <i>enabled</i> for
	 *         the given element.
	 */
	public Set<NavigatorContentExtension> findContentExtensionsByTriggerPoint(Object anElement,
			boolean toLoadIfNecessary, boolean computeOverrides) {
		Set<INavigatorContentDescriptor> enabledDescriptors = findDescriptorsByTriggerPoint(anElement, computeOverrides);
		return extractDescriptorInstances(enabledDescriptors, toLoadIfNecessary);
	}

	/**
	 * Search for extensions that declare the given element in their
	 * <b>possibleChildren</b> expression.
	 *
	 * @param anElement
	 *            The element to use in the query
	 * @return The set of {@link INavigatorContentExtension}s that are
	 *         <i>visible</i> and <i>active</i> for this content service and
	 *         have a <b>possibleChildren</b> expression that is <i>enabled</i>
	 *         for the given element.
	 */
	@Override
	public Set<NavigatorContentExtension> findContentExtensionsWithPossibleChild(Object anElement) {
		return findContentExtensionsWithPossibleChild(anElement, true);
	}

	/**
	 * Search for extensions that declare the given element in their
	 * <b>possibleChildren</b> expression.
	 *
	 * @param anElement
	 *            The element to use in the query
	 * @param toLoadIfNecessary
	 *            True will force the load of the extension, False will not
	 * @return The set of {@link INavigatorContentExtension}s that are
	 *         <i>visible</i> and <i>active</i> for this content service and
	 *         have a <b>possibleChildren</b> expression that is <i>enabled</i>
	 *         for the given element.
	 */
	public Set<NavigatorContentExtension> findContentExtensionsWithPossibleChild(Object anElement,
			boolean toLoadIfNecessary) {
		Set<INavigatorContentDescriptor> enabledDescriptors = findDescriptorsWithPossibleChild(anElement);
		return extractDescriptorInstances(enabledDescriptors, toLoadIfNecessary);
	}

	/**
	 *
	 *
	 * @param firstClassSource
	 * @param source
	 * @param element
	 */
	public void rememberContribution(INavigatorContentDescriptor source,
			INavigatorContentDescriptor firstClassSource, Object element) {
		/*
		 * We want to write to (overwrite) the contributionMemory only if we
		 * have never heard of the element before, or if the element is coming
		 * from the same first class NCE, which means that the subsequent NCE is
		 * an override. The override will take precedence over the originally
		 * contributing NCE. However in the case of different first class NCEs,
		 * the first one wins, so we don't update the contribution memory.
		 */
		synchronized (this) {
			if (contributionMemory.get(element) == null
					|| contributionMemoryFirstClass.get(element) == firstClassSource) {
				if (Policy.DEBUG_RESOLUTION)
					System.out
							.println("rememberContribution: " + Policy.getObjectString(element) + " source: " + source); //$NON-NLS-1$//$NON-NLS-2$
				contributionMemory.put(element, source);
				contributionMemoryFirstClass.put(element, firstClassSource);
			}
		}
	}

	/**
	 * Forget about the specified element
	 *
	 * @param element
	 *            The element to forget.
	 */
	public void forgetContribution(Object element) {
		synchronized (this) {
			contributionMemory.remove(element);
			contributionMemoryFirstClass.remove(element);
		}
	}

	/**
	 * @param element
	 * @return the remembered NavigatorContentDescriptor
	 */
	public NavigatorContentDescriptor getContribution(Object element)
	{
		NavigatorContentDescriptor desc;
		synchronized (this) {
			desc = (NavigatorContentDescriptor) contributionMemory.get(element);
		}
		return desc;
	}

	/**
	 * Used only for the tests
	 * @return the size of the contribution memory
	 */
	public int getContributionMemorySize() {
		synchronized (this) {
			return contributionMemory.size();
		}
	}

	/**
	 *
	 * @param element
	 *            The element contributed by the descriptor to be returned
	 * @return The descriptor that contributed the element or null.
	 * @see #findContentExtensionsByTriggerPoint(Object)
	 */
	public synchronized NavigatorContentDescriptor getSourceOfContribution(Object element) {
		if (element == null)
			return null;
		if (structuredViewerManager == null)
			return null;
		// Try here first because it might not yet be in the tree
		NavigatorContentDescriptor src;
		synchronized (this) {
			src = (NavigatorContentDescriptor) contributionMemory.get(element);
		}
		if (src != null)
			return src;
		return (NavigatorContentDescriptor) structuredViewerManager.getData(element);
	}
	/**
	 *
	 */
	public static final boolean CONSIDER_OVERRIDES = true;

	/**
	 * Search for extensions that declare the given element in their
	 * <b>triggerPoints</b> expression.
	 *
	 * @param anElement
	 *            The element to use in the query
	 * @param considerOverrides
	 *
	 * @return The set of {@link INavigatorContentDescriptor}s that are
	 *         <i>visible</i> and <i>active</i> for this content service and
	 *         have a <b>triggerPoints</b> expression that is <i>enabled</i> for
	 *         the given element.
	 */
	public Set<INavigatorContentDescriptor> findDescriptorsByTriggerPoint(Object anElement, boolean considerOverrides) {
		// Here we use the cache, since objects are inserted into the
		// cache in response to the trigger point
		NavigatorContentDescriptor descriptor = getSourceOfContribution(anElement);
		Set<INavigatorContentDescriptor> result = new TreeSet<INavigatorContentDescriptor>(ExtensionSequenceNumberComparator.INSTANCE);
		if (descriptor != null) {
			result.add(descriptor);
		}
		result.addAll(CONTENT_DESCRIPTOR_REGISTRY
				.findDescriptorsForTriggerPoint(anElement, assistant, considerOverrides));
		return result;
	}

	/**
	 * Search for extensions that declare the given element in their
	 * <b>possibleChildren</b> expression.
	 *
	 * @param anElement
	 *            The element to use in the query
	 * @return The set of {@link INavigatorContentDescriptor}s that are
	 *         <i>visible</i> and <i>active</i> for this content service and
	 *         have a <b>possibleChildren</b> expression that is <i>enabled</i>
	 *         for the given element.
	 */
	public Set<INavigatorContentDescriptor> findDescriptorsWithPossibleChild(Object anElement) {
		return findDescriptorsWithPossibleChild(anElement, true);
	}

	/**
	 * Search for extensions that declare the given element in their
	 * <b>possibleChildren</b> expression.
	 *
	 * @param anElement
	 *            The element to use in the query
	 * @param toComputeOverrides
	 *            True indicates the overridden tree should be traversed.
	 * @return The set of {@link INavigatorContentDescriptor}s that are
	 *         <i>visible</i> and <i>active</i> for this content service and
	 *         have a <b>possibleChildren</b> expression that is <i>enabled</i>
	 *         for the given element.
	 */
	public Set<INavigatorContentDescriptor> findDescriptorsWithPossibleChild(Object anElement,
			boolean toComputeOverrides) {
		// Don't use the cache which is only used for triggerPoints
		Set<INavigatorContentDescriptor> result = new TreeSet<INavigatorContentDescriptor>(ExtensionSequenceNumberComparator.INSTANCE);
		result.addAll(CONTENT_DESCRIPTOR_REGISTRY
				.findDescriptorsForPossibleChild(anElement, assistant,
						toComputeOverrides));
		return result;
	}

	@Override
	public void onExtensionActivation(String aViewerId,
			String[] aNavigatorExtensionId, boolean toEnable) {
		synchronized (this) {
			SafeRunner.run(new NavigatorSafeRunnable() {
				@Override
				public void run() throws Exception {
					NavigatorContentDescriptor key;
					NavigatorContentExtension extension;
					for (Iterator<INavigatorContentDescriptor> iter = contentExtensions.keySet().iterator(); iter
							.hasNext();) {
						key = (NavigatorContentDescriptor) iter.next();
						INavigatorActivationService activation = getActivationService();
						if (!activation.isNavigatorExtensionActive(key.getId())) {
							extension = contentExtensions
									.get(key);
							iter.remove();
							extension.dispose();
						}
					}
				}
			});
		}
		if (structuredViewerManager != null) {
			structuredViewerManager.resetViewerData();
		}
		update();
	}

	@Override
	public void update() {
		rootContentProviders = null;
		if (structuredViewerManager != null) {
			structuredViewerManager.safeRefresh();
		}
	}

	@Override
	public final String getViewerId() {
		return viewerDescriptor.getViewerId();
	}

	/**
	 * Returns the remembered data (the NavigatorContentDescriptor) associated with
	 * an object in the viewer. This can be used to test an object's presence in the viewer.
	 * @param element
	 * @return the object stored as data in the viewer
	 */
	public Object getViewerElementData(Object element) {
		if (structuredViewerManager != null) {
			return structuredViewerManager.getData(element);
		}
		return null;
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

	/**
	 *
	 * @param aDescriptorKey
	 * @param toLoadIfNecessary
	 *            True if the extension should be loaded if it is not already.
	 * @return The instance of the extension for the given descriptor key.
	 */
	public final NavigatorContentExtension getExtension(
			INavigatorContentDescriptor aDescriptorKey,
			boolean toLoadIfNecessary) {
		/* Query and return the relevant descriptor instance */
		NavigatorContentExtension extension = contentExtensions
				.get(aDescriptorKey);
		if (extension != null || !toLoadIfNecessary) {
			return extension;
		}

		/*
		 * If the descriptor instance hasn't been created yet, then we need to
		 * (1) verify that it wasn't added by another thread, (2) create and add
		 * the result into the map
		 */
		synchronized (this) {
			extension = contentExtensions
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

	@Override
	public INavigatorViewerDescriptor getViewerDescriptor() {
		return viewerDescriptor;
	}

	@Override
	public void restoreState(final IMemento aMemento) {
		synchronized (this) {
			for (Iterator extensionItr = getExtensions().iterator(); extensionItr.hasNext();) {
				final NavigatorContentExtension element = (NavigatorContentExtension) extensionItr
						.next();
				SafeRunner.run(new NavigatorSafeRunnable(((NavigatorContentDescriptor) element
						.getDescriptor()).getConfigElement()) {
					@Override
					public void run() throws Exception {
						element.restoreState(aMemento);
					}
				});
			}
		}
	}

	@Override
	public void saveState(final IMemento aMemento) {
		synchronized (this) {
			for (Iterator extensionItr = getExtensions().iterator(); extensionItr.hasNext();) {
				final NavigatorContentExtension element = (NavigatorContentExtension) extensionItr
						.next();
				SafeRunner.run(new NavigatorSafeRunnable(((NavigatorContentDescriptor) element
						.getDescriptor()).getConfigElement()) {
					@Override
					public void run() throws Exception {
						element.saveState(aMemento);
					}
				});
			}
		}
	}

	@Override
	public boolean isActive(String anExtensionId) {
		return assistant.isActive(anExtensionId);
	}

	@Override
	public boolean isVisible(String anExtensionId) {
		return assistant.isVisible(anExtensionId);
	}

	protected final Collection getExtensions() {
		return (contentExtensions.size() > 0) ? Collections
				.unmodifiableCollection(contentExtensions.values())
				: Collections.EMPTY_LIST;
	}

	@Override
	public void addListener(INavigatorContentServiceListener aListener) {
		listeners.add(aListener);
	}

	@Override
	public INavigatorFilterService getFilterService() {
		if (navigatorFilterService == null) {
			navigatorFilterService = new NavigatorFilterService(this);
		}
		return navigatorFilterService;
	}

	@Override
	public INavigatorSorterService getSorterService() {
		if (navigatorSorterService == null) {
			navigatorSorterService = new NavigatorSorterService(this);
			assistant.addListener(navigatorSorterService);
		}
		return navigatorSorterService;
	}

	@Override
	public INavigatorPipelineService getPipelineService() {
		if (navigatorPipelineService == null) {
			navigatorPipelineService = new NavigatorPipelineService(this);
		}
		return navigatorPipelineService;
	}

	@Override
	public INavigatorDnDService getDnDService() {
		if (navigatorDnDService == null) {
			navigatorDnDService = new NavigatorDnDService(this);
		}
		return navigatorDnDService;
	}

	@Override
	public INavigatorActivationService getActivationService() {

		if (navigatorActivationService == null) {
			navigatorActivationService = new NavigatorActivationService(this);
		}
		return navigatorActivationService;
	}

	@Override
	public INavigatorSaveablesService getSaveablesService() {
		synchronized (this) {
			if (navigatorSaveablesService == null) {
				navigatorSaveablesService = new NavigatorSaveablesService(this);
				assistant.addListener(navigatorSaveablesService);
			}
			return navigatorSaveablesService;
		}
	}

	/**
	 * Not API as of 3.3.
	 *
	 * @return The extension state service for this content service.
	 *
	 */
	public NavigatorExtensionStateService getExtensionStateService() {
		if (navigatorExtensionStateService == null) {
			navigatorExtensionStateService = new NavigatorExtensionStateService(
					this);
		}
		return navigatorExtensionStateService;
	}

	/**
	 * Non-API method to return a shell.
	 *
	 * @return A shell associated with the current viewer (if any) or
	 *         <b>null</b>.
	 */
	public Shell getShell() {
		if (structuredViewerManager != null
				&& structuredViewerManager.getViewer() != null) {
			return structuredViewerManager.getViewer().getControl().getShell();
		}
		return null;
	}

	protected boolean isRootExtension(String anExtensionId) {
		return assistant.isRootExtension(anExtensionId);
	}

	@Override
	public void removeListener(INavigatorContentServiceListener aListener) {
		listeners.remove(aListener);
	}

	@Override
	public String toString() {
		return "ContentService[" + viewerDescriptor.getViewerId() + "]"; //$NON-NLS-1$//$NON-NLS-2$
	}

	private void notifyListeners(final NavigatorContentExtension aDescriptorInstance) {

		if (listeners.size() == 0) {
			return;
		}

		final List<INavigatorContentServiceListener> failedListeners = new ArrayList<INavigatorContentServiceListener>();

		for (Iterator<INavigatorContentServiceListener> listenersItr = listeners.iterator(); listenersItr.hasNext();) {
			final INavigatorContentServiceListener listener = listenersItr
					.next();
			SafeRunner.run(new NavigatorSafeRunnable() {

				@Override
				public void run() throws Exception {
					listener.onLoad(aDescriptorInstance);
				}

				@Override
				public void handleException(Throwable e) {
					super.handleException(e);
					failedListeners.add(listener);
				}
			});
		}

		if (failedListeners.size() > 0) {
			listeners.removeAll(failedListeners);
		}
	}

	private ITreeContentProvider[] extractContentProviders(
			Set<NavigatorContentExtension> theDescriptorInstances) {
		if (theDescriptorInstances.size() == 0) {
			return NO_CONTENT_PROVIDERS;
		}
		List<SafeDelegateTreeContentProvider> resultProvidersList = new ArrayList<SafeDelegateTreeContentProvider>();
		for (Iterator<NavigatorContentExtension> itr = theDescriptorInstances.iterator(); itr.hasNext();) {
			resultProvidersList.add(itr.next()
					.internalGetContentProvider());
		}
		return resultProvidersList
				.toArray(new ITreeContentProvider[resultProvidersList.size()]);
	}

	private Set<NavigatorContentExtension> extractDescriptorInstances(Set<INavigatorContentDescriptor> theDescriptors,
			boolean toLoadAllIfNecessary) {
		if (theDescriptors.size() == 0) {
			return Collections.EMPTY_SET;
		}
		Set<NavigatorContentExtension> resultInstances = new TreeSet<NavigatorContentExtension>(ExtensionSequenceNumberComparator.INSTANCE);
		for (Iterator<INavigatorContentDescriptor> descriptorIter = theDescriptors.iterator(); descriptorIter
				.hasNext();) {
			NavigatorContentExtension extension = getExtension(
					descriptorIter.next(),
					toLoadAllIfNecessary);
			if (extension != null) {
				resultInstances.add(extension);

			}
		}
		return resultInstances;
	}

	/**
	 * @return the viewer
	 */
	public Viewer getViewer() {
		return structuredViewerManager.getViewer();
	}


	/**
	 * Get our preferences
	 */
	static IEclipsePreferences getPreferencesRoot() {
		IEclipsePreferences root = (IEclipsePreferences) Platform.getPreferencesService().getRootNode().node(
				InstanceScope.SCOPE);
		return (IEclipsePreferences) root.node(NavigatorPlugin.PLUGIN_ID);
	}


	static void flushPreferences(IEclipsePreferences prefs) {
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR,
					CommonNavigatorMessages.NavigatorContentService_problemSavingPreferences, e);
			Platform.getLog(Platform.getBundle(NavigatorPlugin.PLUGIN_ID)).log(status);
		}
	}

}
