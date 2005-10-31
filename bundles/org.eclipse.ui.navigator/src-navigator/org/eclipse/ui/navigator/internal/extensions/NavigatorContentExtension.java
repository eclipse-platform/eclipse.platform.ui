/***************************************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.ui.navigator.internal.extensions;

import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonActionProvider;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.IMementoAware;
import org.eclipse.ui.navigator.NavigatorActionService;
import org.eclipse.ui.navigator.NavigatorContentService;
import org.eclipse.ui.navigator.internal.CommonNavigatorMessages;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class NavigatorContentExtension implements IMementoAware {


	private NavigatorContentService contentService; 
	private NavigatorContentDescriptor descriptor;
	private final String viewerId;
	
	private ICommonContentProvider contentProvider;
	private ICommonLabelProvider labelProvider;
	private ICommonActionProvider actionProvider;
	private IExtensionStateModel stateModel;
	private Comparator comparator;
	
	private boolean labelProviderInitializationFailed = false;
	private boolean contentProviderInitializationFailed = false;
	private boolean actionProviderInitializationFailed = false; 
	private boolean comparatorInitializationFailed = false;
	private boolean isDisposed = false;
	private IMemento appliedMemento;
	private StructuredViewerManager viewerManager;


	public NavigatorContentExtension(
			NavigatorContentDescriptor aDescriptor,
			NavigatorContentService aContentService, 
			StructuredViewerManager aViewerManager) {
		super();

		if (aDescriptor == null)
			throw new IllegalArgumentException(
					CommonNavigatorMessages.NavigatorContentExtension_0
							+ NavigatorContentDescriptor.class.getName()
							+ CommonNavigatorMessages.NavigatorContentExtension_1);

		descriptor = aDescriptor;
		contentService = aContentService;
		viewerId = contentService.getViewerId();
		viewerManager = aViewerManager;
	}
	
	public String getId() {
		return descriptor.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.views.INavigatorContentExtension#getContentProvider()
	 */
	public ITreeContentProvider getContentProvider() {
		if (contentProvider != null || contentProviderInitializationFailed)
			return contentProvider;
		synchronized (this) {
			try {
				if (contentProvider == null) {
					ITreeContentProvider treeContentProvider = (ITreeContentProvider) descriptor.getConfigurationElement().createExecutableExtension(NavigatorContentDescriptor.ATT_CONTENT_PROVIDER);
					if (treeContentProvider != null) {
						contentProvider = new NavigatorContentProvider(treeContentProvider);
						contentProvider.init(getStateModel(), appliedMemento); 
						viewerManager.initialize(contentProvider);
					}
					else
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
	 * @see org.eclipse.wst.common.navigator.views.INavigatorContentExtension#getLabelProvider()
	 */
	public ICommonLabelProvider getLabelProvider() {
		if (labelProvider != null || labelProviderInitializationFailed)
			return labelProvider;
		synchronized (this) {
			try {

				if (labelProvider == null) {
					ILabelProvider tempLabelProvider = (ILabelProvider) descriptor.getConfigurationElement().createExecutableExtension(NavigatorContentDescriptor.ATT_LABEL_PROVIDER);

					if (tempLabelProvider instanceof ICommonLabelProvider) {
						labelProvider = (ICommonLabelProvider) tempLabelProvider;
						NavigatorContentProvider navigatorContentProvider = (NavigatorContentProvider) getContentProvider();
						labelProvider.init(getStateModel(), navigatorContentProvider.getDelegateContentProvider());
					} else {
						labelProvider = new DelegateCommonLabelProvider(tempLabelProvider);
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
	 * @return
	 */
	public ICommonActionProvider getActionProvider(NavigatorActionService theActionService) {
		if (actionProvider != null || actionProviderInitializationFailed)
			return actionProvider;
		if (descriptor.getConfigurationElement().getAttribute(NavigatorContentDescriptor.ATT_ACTION_PROVIDER) == null) {
			actionProvider = SkeletonActionProvider.INSTANCE;
			return actionProvider;
		}
		synchronized (this) {
			try {
				if (actionProvider == null) {
					actionProvider = (ICommonActionProvider) descriptor.getConfigurationElement().createExecutableExtension(NavigatorContentDescriptor.ATT_ACTION_PROVIDER);
					if(actionProvider == null) 
						actionProvider = SkeletonActionProvider.INSTANCE;
					else if(theActionService != null)
						theActionService.initialize(descriptor.getId(), actionProvider);
				}
			} catch (CoreException e) {
				actionProviderInitializationFailed = true;
				e.printStackTrace();
			} catch (RuntimeException e) {
				actionProviderInitializationFailed = true;
				e.printStackTrace();
			}
			if (actionProviderInitializationFailed)
				actionProvider = SkeletonActionProvider.INSTANCE;
		}
		return actionProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.views.INavigatorContentExtension#dispose()
	 */
	public void dispose() {
		try {
			synchronized (this) {

				if (contentProvider != null)
					contentProvider.dispose();
				if (labelProvider != null)
					labelProvider.dispose();
				if (actionProvider != null)
					actionProvider.dispose();
			}
		} finally {
			isDisposed = true;
		}
	}


	// M4 Revisit the sorting strategy [CommonNavigator:SORTING]
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.views.INavigatorContentExtension#getComparator()
	 */
	public Comparator getComparator() {
		if (comparator != null || comparatorInitializationFailed)
			return comparator;
		synchronized (this) {
			try {
				if (comparator == null) {
					String sorterClassName = descriptor.getConfigurationElement().getAttribute(NavigatorContentDescriptor.ATT_SORTER);
					if (sorterClassName != null && sorterClassName.length() > 0)
						comparator = (Comparator) descriptor.getConfigurationElement().createExecutableExtension(NavigatorContentDescriptor.ATT_SORTER);
				}

			} catch (CoreException e) {
				comparatorInitializationFailed = true;
				e.printStackTrace();
			} catch (RuntimeException e) {
				comparatorInitializationFailed = true;
				e.printStackTrace();
			}
			if (comparatorInitializationFailed || comparator == null)
				comparator = IdentityComparator.INSTANCE;
		}
		return comparator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
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

	public boolean hasLoadingFailed() { 
		return contentProviderInitializationFailed;
	}

	public boolean isLoaded() {
		return contentProvider != null;
	}

	public void restoreState(IMemento aMemento) {
		synchronized (this) {
			appliedMemento = aMemento;
			applyMemento(contentProvider);
			applyMemento(labelProvider);
			applyMemento(actionProvider); 

		}
	}
	
	public void saveState(IMemento aMemento) { 
		synchronized (this) {  
		}
	}
 

	private void applyMemento(IMementoAware target) { 
		if(target != null)
			target.restoreState(appliedMemento);
		
	}


	protected final void complainDisposedIfNecessary() {
		if (isDisposed)
			throw new IllegalStateException(
					CommonNavigatorMessages.NavigatorContentExtension_2
							+ descriptor.getId() + CommonNavigatorMessages.NavigatorContentExtension_3);
	}

	public IExtensionStateModel getStateModel() {
		if(stateModel == null)
			stateModel = new ExtensionStateModel(descriptor.getId(), viewerId);
		return stateModel;
	}
}