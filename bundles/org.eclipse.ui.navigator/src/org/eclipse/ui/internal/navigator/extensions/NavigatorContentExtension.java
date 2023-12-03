/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.extensions;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.internal.navigator.NavigatorSafeRunnable;
import org.eclipse.ui.internal.navigator.Policy;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.IMementoAware;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentExtension;

/**
 *
 * @since 3.2
 */
public class NavigatorContentExtension implements IMementoAware,
		INavigatorContentExtension {

	private static final NavigatorContentExtension[] NO_EXTENSIONS = new NavigatorContentExtension[0];

	private NavigatorContentService contentService;

	private NavigatorContentDescriptor descriptor;

	private SafeDelegateTreeContentProvider contentProvider;

	private ICommonLabelProvider labelProvider;

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
		viewerManager = aViewerManager;
	}

	@Override
	public String getId() {
		return descriptor.getId();
	}

	@Override
	public INavigatorContentDescriptor getDescriptor() {
		return descriptor;
	}

	@Override
	public ITreeContentProvider getContentProvider() {
		return internalGetContentProvider().getDelegateContentProvider();
	}

	/**
	 *
	 * @return The internal content provider that is wrapped by this extension.
	 */
	public SafeDelegateTreeContentProvider internalGetContentProvider() {
		if (contentProvider != null || contentProviderInitializationFailed) {
			return contentProvider;
		}
		synchronized (this) {
			SafeRunner.run(new NavigatorSafeRunnable() {
				@Override
				public void run() throws Exception {
					if (contentProvider == null) {
						ITreeContentProvider treeContentProvider = descriptor
								.createContentProvider();
						if (treeContentProvider != null) {
							contentProvider = new SafeDelegateTreeContentProvider(
									treeContentProvider);
							contentProvider.init(new CommonContentExtensionSite(getId(),
									contentService, appliedMemento));
							viewerManager.initialize(contentProvider);
						} else {
							contentProvider = new SafeDelegateTreeContentProvider(
									SkeletonTreeContentProvider.INSTANCE);
						}
					}
				}

				@Override
				public void handleException(Throwable e) {
					super.handleException(e);
					contentProviderInitializationFailed = true;
				}
			});

			if (contentProviderInitializationFailed) {
				contentProvider = new SafeDelegateTreeContentProvider(
						SkeletonTreeContentProvider.INSTANCE);
			}
		}
		return contentProvider;
	}

	@Override
	public ICommonLabelProvider getLabelProvider() {
		if (labelProvider != null || labelProviderInitializationFailed) {
			return labelProvider;
		}
		synchronized (this) {
			SafeRunner.run(new NavigatorSafeRunnable() {
				@Override
				public void run() throws Exception {
					if (labelProvider == null) {
						ILabelProvider tempLabelProvider = descriptor.createLabelProvider();

						if (tempLabelProvider instanceof ICommonLabelProvider) {
							labelProvider = (ICommonLabelProvider) tempLabelProvider;
							labelProvider.init(new CommonContentExtensionSite(getId(),
									contentService, appliedMemento));
						} else {
							labelProvider = new SafeDelegateCommonLabelProvider(tempLabelProvider);
						}

						labelProvider.addListener((ILabelProviderListener) contentService
								.createCommonLabelProvider());
					}
				}

				@Override
				public void handleException(Throwable e) {
					super.handleException(e);
					labelProviderInitializationFailed = true;
				}
			});
			if (labelProviderInitializationFailed) {
				labelProvider = SkeletonLabelProvider.INSTANCE;
			}
		}
		return labelProvider;
	}

	/**
	 * Dispose of any resources acquired during the lifecycle of the extension.
	 */
	public void dispose() {
		try {
			synchronized (this) {

				SafeRunner.run(new NavigatorSafeRunnable() {
					@Override
					public void run() throws Exception {
						if (contentProvider != null) {
							contentProvider.dispose();
						}

					}
				});

				SafeRunner.run(new NavigatorSafeRunnable() {
					@Override
					public void run() throws Exception {
						if (labelProvider != null) {
							labelProvider
									.removeListener((ILabelProviderListener) contentService
											.createCommonLabelProvider());
							labelProvider.dispose();
						}
					}
				});

			}
		} finally {
			isDisposed = true;
		}
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
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

	@Override
	public boolean isLoaded() {
		return contentProvider != null;
	}

	@Override
	public void restoreState(IMemento aMemento) {
		synchronized (this) {
			appliedMemento = aMemento;
			applyMemento(contentProvider);
			applyMemento(labelProvider);

		}
	}

	@Override
	public void saveState(IMemento aMemento) {
		synchronized (this) {
			if (contentProvider != null) {
				contentProvider.saveState(aMemento);
			}
			if (labelProvider != null) {
				labelProvider.saveState(aMemento);
			}
		}
	}

	private void applyMemento(IMementoAware target) {
		if (target != null) {
			target.restoreState(appliedMemento);
		}

	}

	protected final void complainDisposedIfNecessary() {
		if (isDisposed) {
			throw new IllegalStateException("INavigatorContentExtension " //$NON-NLS-1$
					+ descriptor.getId() + " is disposed!"); //$NON-NLS-1$
		}
	}

	@Override
	public IExtensionStateModel getStateModel() {
		return contentService.getExtensionStateService()
				.getExtensionStateModel(getDescriptor());
	}

	/**
	 * @param anElement
	 *            The element for the query.
	 * @return Returns the overridingExtensions.
	 */
	public NavigatorContentExtension[] getOverridingExtensionsForTriggerPoint(
			Object anElement) {
		return getOverridingExtensions(anElement, TRIGGER_POINT);
	}

	/**
	 *
	 * @param anElement
	 *            The element for the query.
	 * @return Returns the overridingExtensions.
	 */
	public NavigatorContentExtension[] getOverridingExtensionsForPossibleChild(
			Object anElement) {
		return getOverridingExtensions(anElement, !TRIGGER_POINT);
	}

	/**
	 *
	 * @return Returns the overridingExtensions.
	 */
	public NavigatorContentExtension[] getOverridingExtensions() {
		return getOverridingExtensions(null, !TRIGGER_POINT);
	}

	private static final boolean TRIGGER_POINT = true;

	/**
	 * @param anElement
	 *            The element for the query.
	 * @return Returns the overridingExtensions.
	 */
	private NavigatorContentExtension[] getOverridingExtensions(Object anElement,
			boolean triggerPoint) {
		if (!descriptor.hasOverridingExtensions()) {
			return NO_EXTENSIONS;
		}

		NavigatorContentDescriptor overridingDescriptor;
		Set overridingExtensions = new LinkedHashSet();
		for (Iterator contentDescriptorsItr = descriptor.getOverriddingExtensions().iterator(); contentDescriptorsItr
				.hasNext();) {
			overridingDescriptor = (NavigatorContentDescriptor) contentDescriptorsItr.next();

			if (contentService.isActive(overridingDescriptor.getId())
					&& contentService.isVisible(overridingDescriptor.getId())
					&& (anElement == null || (triggerPoint ? overridingDescriptor
							.isTriggerPoint(anElement) : overridingDescriptor
							.isPossibleChild(anElement)))) {
				overridingExtensions.add(contentService.getExtension(overridingDescriptor));
			}
		}
		if (overridingExtensions.isEmpty()) {
			return NO_EXTENSIONS;
		}
		if (Policy.DEBUG_EXTENSION_SETUP) {
			System.out
					.println(this
							+ " overriding: " + //$NON-NLS-1$
							(triggerPoint ? "(trigger pt: " : "(poss child: ") + anElement + "): " + overridingExtensions); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		}
		return (NavigatorContentExtension[]) overridingExtensions
				.toArray(new NavigatorContentExtension[overridingExtensions.size()]);
	}

	@Override
	public String toString() {
		return descriptor + " Instance"; //$NON-NLS-1$
	}
}
