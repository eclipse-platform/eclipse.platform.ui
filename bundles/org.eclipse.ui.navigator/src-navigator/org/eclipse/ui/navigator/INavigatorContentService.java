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
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.internal.INavigatorContentServiceListener;
import org.eclipse.ui.navigator.internal.extensions.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.internal.extensions.INavigatorViewerDescriptor;

/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 *<p>
 * This interface is not intended to be implemented by clients.
 *</p> 
 * @since 3.2
 * 
 */
public interface INavigatorContentService {

	ITreeContentProvider createCommonContentProvider();

	ILabelProvider createCommonLabelProvider();

	void dispose();

	IExtensionStateModel findStateModel(String anExtensionId);

	ITreeContentProvider[] findParentContentProviders(Object anElement);

	/**
	 * <p>
	 * Return all of the content providers that are relevant for the viewer. The
	 * viewer is determined by the ID used to create the
	 * NavigatorContentService.
	 * </p>
	 * 
	 * @return
	 */
	ITreeContentProvider[] findRootContentProviders(Object anElement);

	/**
	 * <p>
	 * Return all of the content providers that are enabled for the given
	 * parameter 'element'.
	 * 
	 * @param anElement
	 * @return
	 */
	ITreeContentProvider[] findRelevantContentProviders(Object anElement);

	/**
	 * <p>
	 * Return all of the label providers that are enabled for the given
	 * parameter 'element'.
	 * 
	 * @param anElement
	 * @return
	 */

	ILabelProvider[] findRelevantLabelProviders(Object anElement);
   

	/**
	 * @return Returns the viewerId.
	 */
	String getViewerId();
	
	
	INavigatorViewerDescriptor getViewerDescriptor();
 
	/**
	 * Enable the given extensions. Optionally disable all other extensions.
	 * 
	 * @param extensionIds
	 *            The list of extensions to enable
	 * @param toDisableAllOthers
	 *            True will disable all other extensions; False will leave the
	 *            other enablements as-is
	 * @return A list of all INavigatorContentDescriptors that were enabled as a
	 *         result of this call.
	 */
	INavigatorContentDescriptor[] enableExtensions(String[] extensionIds,
			boolean toDisableAllOthers);

	/**
	 * Disable the specified extensions. Optionally enable all other extensions.
	 * 
	 * @param extensionIds
	 *            The list of extensions to enable
	 * @param toEnableAllOthers
	 *            True will enable all other extensions; False will leave the
	 *            other enablements as-is
	 * @return A list of all INavigatorContentDescriptors that were enabled as a
	 *         result of this call.
	 */
	INavigatorContentDescriptor[] disableExtensions(String[] extensionIds,
			boolean toEnableAllOthers);

	void addExclusion(String anExtensionId);

	void removeExclusion(String anExtensionId);

	void restoreState(IMemento aMemento);

	void saveState(IMemento aMemento);

	void addListener(INavigatorContentServiceListener aListener);

	void removeListener(INavigatorContentServiceListener aListener);
	
	void update();

}
