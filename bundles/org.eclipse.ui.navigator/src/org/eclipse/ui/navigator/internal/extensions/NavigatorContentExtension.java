/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.extensions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.IMementoAware;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentExtension;
import org.eclipse.ui.navigator.internal.NavigatorContentService;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class NavigatorContentExtension implements IMementoAware,
		INavigatorContentExtension {

	private static final NavigatorContentExtension[] NO_EXTENSIONS = new NavigatorContentExtension[0];

	private NavigatorContentService contentService;

	private NavigatorContentDescriptor descriptor;

	private final String viewerId;

	private ICommonContentProvider contentProvider;

	private ICommonLabelProvider labelProvider;

	private IExtensionStateModel stateModel;

	private boolean labelProviderInitializationFailed = false;

	private boolean contentProviderInitializationFailed = false;

	private boolean isDisposed = false;

	private IMemento appliedMemento;

	private StructuredViewerManager viewerManager; 

	/**
	 * Create an object to manage the instantiated elements from the extension.
	 * 
	 * @param aDescriptor
	 *            The descriptor that knows how to create elements and knows the
	 *            id of the extension
	 * @param aContentService
	 *            The content service that will manage this extension
	 * @param aViewerManager
	 *            The viewer manager that knows how to initialize the content
	 *            provider created by this extension.
	 */
	public NavigatorContentExtension(NavigatorContentDescriptor aDescriptor,
			NavigatorContentService aContentService,
			StructuredViewerManager aViewerManager) {
		super();
		Assert.isNotNull(aDescriptor);

		descriptor = aDescriptor;
		contentService = aContentService;
		viewerId = contentService.getViewerId();
		viewerManager = aViewerManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentExtension#getId()
	 */
	public String getId() {
		return descriptor.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentExtension#getDescriptor()
	 */
	public INavigatorContentDescriptor getDescriptor() {
		return descriptor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentExtension#getContentProvider()
	 */
	public ITreeContentProvider getContentProvider() {
		if (contentProvider != null || contentProviderInitializationFailed)
			return contentProvider;
		synchronized (this) {
			try {
				if (contentProvider == null) {
					ITreeContentProvider treeContentProvider = descriptor
							.createContentProvider();
					if (treeContentProvider != null) {
						contentProvider = new NavigatorContentProvider(
								treeContentProvider, descriptor, contentService);
						contentProvider.init(getStateModel(), appliedMemento);
						viewerManager.initialize(contentProvider);
					} else
						contentProvider = SkeletonTreeContentProvider.INSTANCE;
				}
			} catch (CoreException e) {
				contentProviderInitializationFailed = true;
				e.printStackTrace();
			} catch (RuntimeException e) {
				contentProviderInitializationFailed = true;
				e.printStackTrace();
			}
			if (contentProviderInitializationFailed)
				contentProvider = SkeletonTreeContentProvider.INSTANCE;
		}
		return contentProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentExtension#getLabelProvider()
	 */
	public ICommonLabelProvider getLabelProvider() {
		if (labelProvider != null || labelProviderInitializationFailed)
			return labelProvider;
		synchronized (this) {
			try {

				if (labelProvider == null) {
					ILabelProvider tempLabelProvider = descriptor
							.createLabelProvider();

					if (tempLabelProvider instanceof ICommonLabelProvider) {
						labelProvider = (ICommonLabelProvider) tempLabelProvider;
						if (getContentProvider() instanceof NavigatorContentProvider)
							labelProvider
									.init(
											getStateModel(),
											((NavigatorContentProvider) getContentProvider())
													.getDelegateContentProvider());
						else
							labelProvider.init(getStateModel(),
									SkeletonTreeContentProvider.INSTANCE);
					} else {
						labelProvider = new DelegateCommonLabelProvider(
								tempLabelProvider);
					}
				}
			} catch (CoreException e) {
				labelProviderInitializationFailed = true;
				e.printStackTrace();
			} catch (RuntimeException e) {
				labelProviderInitializationFailed = true;
				e.printStackTrace();
			}

			if (labelProviderInitializationFailed)
				labelProvider = SkeletonLabelProvider.INSTANCE;
		}
		return labelProvider;
	}
 
	/**
	 * Dispose of any resources acquired during the lifecycle of the extension.
	 * 
	 */
	public void dispose() {
		try {
			synchronized (this) {

				if (contentProvider != null)
					contentProvider.dispose();
				if (labelProvider != null)
					labelProvider.dispose();
			}
		} finally {
			isDisposed = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentExtension#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	/**
	 * @return Returns the contentProviderInitializationFailed.
	 */
	public boolean hasContentProviderInitializationFailed() {
		return contentProviderInitializationFailed;
	}

	/**
	 * @return Returns the labelProviderInitializationFailed.
	 */
	public boolean hasLabelProviderInitializationFailed() {
		return labelProviderInitializationFailed;
	}

	/**
	 * 
	 * @return True if the loading of the content provider has failed.
	 */
	public boolean hasLoadingFailed() {
		return contentProviderInitializationFailed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentExtension#isLoaded()
	 */
	public boolean isLoaded() {
		return contentProvider != null;
	}

	public void restoreState(IMemento aMemento) {
		synchronized (this) {
			appliedMemento = aMemento;
			applyMemento(contentProvider);
			applyMemento(labelProvider);

		}
	}

	public void saveState(IMemento aMemento) {
		synchronized (this) {
		}
	}

	private void applyMemento(IMementoAware target) {
		if (target != null)
			target.restoreState(appliedMemento);

	}

	protected final void complainDisposedIfNecessary() {
		if (isDisposed)
			throw new IllegalStateException("INavigatorContentExtension " //$NON-NLS-1$
					+ descriptor.getId() + " is disposed!"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorContentExtension#getStateModel()
	 */
	public IExtensionStateModel getStateModel() {
		if (stateModel == null)
			stateModel = new ExtensionStateModel(descriptor.getId(), viewerId);
		return stateModel;
	}

	/**
	 * @return Returns the overridingExtensions.
	 */
	public NavigatorContentExtension[] getOverridingExtensionsForTriggerPoint(
			Object anElement) {
		if (!descriptor.hasOverridingExtensions())
			return NO_EXTENSIONS;

		NavigatorContentDescriptor overriddingDescriptor;
		Set overridingExtensions = new HashSet();
		for (Iterator contentDescriptorsItr = descriptor
				.getOverriddingExtensions().iterator(); contentDescriptorsItr
				.hasNext();) {
			overriddingDescriptor = (NavigatorContentDescriptor) contentDescriptorsItr
					.next();

			if (contentService.isActive(overriddingDescriptor.getId())
					&& contentService.isVisible(overriddingDescriptor.getId())
					&& overriddingDescriptor.isTriggerPoint(anElement)) {
				overridingExtensions.add(contentService
						.getExtension(overriddingDescriptor));
			}
		}
		if (overridingExtensions.size() == 0)
			return NO_EXTENSIONS;
		return (NavigatorContentExtension[]) overridingExtensions
				.toArray(new NavigatorContentExtension[overridingExtensions
						.size()]);
	}

	/**
	 * @return Returns the overridingExtensions.
	 */
	public NavigatorContentExtension[] getOverridingExtensionsForPossibleChild(
			Object anElement) {
		if (!descriptor.hasOverridingExtensions())
			return NO_EXTENSIONS;

		Set overridingExtensions = new HashSet();
		for (Iterator contentDescriptorsItr = descriptor
				.getOverriddingExtensions().iterator(); contentDescriptorsItr
				.hasNext();) {
			descriptor = (NavigatorContentDescriptor) contentDescriptorsItr
					.next();

			if (contentService.isActive(descriptor.getId())
					&& contentService.isVisible(descriptor.getId())
					&& descriptor.isPossibleChild(anElement)) {
				overridingExtensions.add(contentService
						.getExtension(descriptor));
			}
		}
		if (overridingExtensions.size() == 0)
			return NO_EXTENSIONS;
		return (NavigatorContentExtension[]) overridingExtensions
				.toArray(new NavigatorContentExtension[overridingExtensions
						.size()]);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return descriptor.toString() + " Instance"; //$NON-NLS-1$
	}
}
