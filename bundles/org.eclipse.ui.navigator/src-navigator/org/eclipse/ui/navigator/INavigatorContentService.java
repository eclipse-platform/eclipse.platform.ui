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
package org.eclipse.ui.navigator;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.internal.extensions.INavigatorContentServiceListener;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentExtension;
import org.eclipse.ui.navigator.internal.extensions.NavigatorViewerDescriptor;

/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2
 *
 */
public interface INavigatorContentService {

	public abstract ITreeContentProvider createCommonContentProvider();

	/**
	 * @return
	 */
	public abstract ILabelProvider createCommonLabelProvider();

	/**
	 *  
	 */
	public abstract void dispose();

	public abstract IExtensionStateModel findStateModel(String anExtensionId);

	/**
	 * @param element
	 * @return
	 */
	public abstract ITreeContentProvider[] findParentContentProviders(
			Object anElement);

	/**
	 * <p>
	 * Return all of the content providers that are relevant for the viewer. The viewer is
	 * determined by the ID used to create the NavigatorContentService.
	 * </p>
	 * 
	 * @return
	 */
	public abstract ITreeContentProvider[] findRootContentProviders(
			Object anElement);

	/**
	 * <p>
	 * Return all of the content providers that are enabled for the given parameter 'element'.
	 * 
	 * @param anElement
	 * @return
	 */
	public abstract ITreeContentProvider[] findRelevantContentProviders(
			Object anElement);

	/**
	 * <p>
	 * Return all of the label providers that are enabled for the given parameter 'element'.
	 * 
	 * @param anElement
	 * @return
	 */

	public abstract ILabelProvider[] findRelevantLabelProviders(Object anElement);

	public abstract NavigatorContentExtension[] findRelevantContentExtensions(
			Object anElement);

	public abstract NavigatorContentExtension[] findRelevantContentExtensions(
			Object anElement, boolean toLoadIfNecessary);

	public abstract NavigatorContentExtension[] findRelevantContentExtensions(
			IStructuredSelection aSelection);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.internal.views.extensions.IInitializationManager#initialize(org.eclipse.jface.viewers.IStructuredContentProvider)
	 */
	public abstract boolean initialize(
			IStructuredContentProvider aContentProvider);

	/**
	 * @param viewerId
	 * @param navigatorExtensionId
	 * @param toEnable
	 */
	public abstract void onExtensionActivation(String aViewerId,
			String aNavigatorExtensionId, boolean toEnable);

	public abstract void update();

	/**
	 * @return Returns the viewerId.
	 */
	public abstract String getViewerId();

	/**
	 * @param object
	 * @return
	 */
	public abstract NavigatorContentExtension getExtension(
			NavigatorContentDescriptor aDescriptorKey);

	/**
	 * @param object
	 * @return
	 */
	public abstract NavigatorContentExtension getExtension(
			NavigatorContentDescriptor aDescriptorKey, boolean toLoadIfNecessary);

	/**
	 * 
	 * @return The ViewerDescriptor for tihs Content Service instance. 
	 */
	public abstract NavigatorViewerDescriptor getViewerDescriptor();

	public abstract void addExclusion(String anExtensionId);

	public abstract void removeExclusion(String anExtensionId);

	public abstract void restoreState(final IMemento aMemento);

	public abstract void saveState(IMemento aMemento);

	public abstract void addListener(INavigatorContentServiceListener aListener);

	public abstract void removeListener(
			INavigatorContentServiceListener aListener);

}
