/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.mapping;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizePage;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * The team content provider manager provides access to the content
 * extenstions registered with the <code>org.eclipse.team.ui.teamContentProviders</code>
 * extension point. A team content provider defines a mapping between
 * a {@link ModelProvider} and a content extension registered with
 * the <code>org.eclipse.ui.navigator.navigatorContent</code> extension point.
 * 
 * @see TeamUI#getTeamContentProviderManager()
 * @see ModelProvider
 * @since 3.2
 */
public interface ITeamContentProviderManager {

	/**
	 * Property constant used to store and retrieve the synchronization page
	 * configuration from the
	 * {@link org.eclipse.ui.navigator.IExtensionStateModel} used by the Common
	 * Navigator framework.
	 */
	public static final String P_SYNCHRONIZATION_PAGE_CONFIGURATION = "org.eclipse.team.ui.synchronizationPageConfiguration"; //$NON-NLS-1$
	/**
	 * Property constant used to store and retrieve the synchronization context
	 * from the {@link org.eclipse.ui.navigator.IExtensionStateModel} used by
	 * the Common Navigator framework. It is also used to associate a context
	 * with an {@link ISynchronizePageConfiguration} when models are being
	 * shown in an {@link ISynchronizePage}.
	 */
	public static final String P_SYNCHRONIZATION_CONTEXT = "org.eclipse.team.ui.synchronizationContext"; //$NON-NLS-1$
	/**
	 * Property constant used to store and retrieve the resource mapping scope
	 * from the {@link org.eclipse.ui.navigator.IExtensionStateModel} used by
	 * the Common Navigator framework. It is also used to associate a scope
	 * with an {@link ISynchronizePageConfiguration} when models are being
	 * shown in an {@link ISynchronizePage}.
	 */
	public static final String P_RESOURCE_MAPPING_SCOPE = "org.eclipse.team.ui.resourceMappingScope"; //$NON-NLS-1$

	/**
	 * Return descriptors for all the registered content extensions.
	 * @return descriptors for all the registered content extensions
	 */
	public abstract ITeamContentProviderDescriptor[] getDescriptors();

	/**
	 * Return the ids for all the content extensions registered
	 * with the team content providers extenstion point. Each of the
	 * returned ids is the id of an extension registered with the
	 * <code>org.eclipse.ui.navigator.navigatorContent</code> extension point..
	 * 
	 * @return the ids for all the content extensions registered
	 * with the team content providers extenstion point
	 */
	public abstract String[] getContentProviderIds();

	/**
	 * Return the team content provider descriptor for the
	 * given model provider id. A <code>null</code> is
	 * returned if no extension is registered.
	 * @param modelProviderId the model provider id
	 * @return the team content provider descriptor for the
	 * given model provider id or <code>null</code>
	 */
	public abstract ITeamContentProviderDescriptor getDescriptor(
			String modelProviderId);

}