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
import org.eclipse.team.ui.synchronize.ISynchronizePage;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * This interface defines the set of constants used in conjunction with the
 * display of models in a team operation.
 * <p>
 * This interface is not intended to be implemented by clients.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public interface ISynchronizationConstants {

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
	 * Property constant used to store and retrieve the synchronization page
	 * configuration from the
	 * {@link org.eclipse.ui.navigator.IExtensionStateModel} used by the Common
	 * Navigator framework.
	 */
	public static final String P_SYNCHRONIZATION_PAGE_CONFIGURATION = "org.eclipse.team.ui.synchronizationPageConfiguration"; //$NON-NLS-1$
	
	/**
	 * Property constant used to store and retrieve the id of the active
	 * {@link ModelProvider} from an {@link ISynchronizePageConfiguration}. The
	 * active model provider will be the only one visible in the page. If
	 * <code>null</code> or <code>ALL_MODEL_PROVIDERS_ACTIVE</code> is
	 * returned, all model providers are considered active and are visible.
	 */
	public static final String P_ACTIVE_MODEL_PROVIDER = "org.eclipse.team.ui.activeModelProvider"; //$NON-NLS-1$
	
	/**
	 * Constant used with the <code>P_ACTIVE_MODEL_PROVIDER</code> property to indicate
	 * that all model providers are active.
	 */
	public static final String ALL_MODEL_PROVIDERS_ACTIVE = "org.eclipse.team.ui.activeModelProvider"; //$NON-NLS-1$

}
