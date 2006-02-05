/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.mapping;

import java.util.EventListener;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceTraversal;

/**
 * Listener for synchronization scope changes.
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 * 
 * @see ISynchronizationScope
 *      <p>
 *      <strong>EXPERIMENTAL</strong>. This class or interface has been added
 *      as part of a work in progress. There is a guarantee neither that this
 *      API will work nor that it will remain the same. Please do not use this
 *      API without consulting with the Platform/Team team.
 *      </p>
 * 
 * @since 3.2
 */
public interface ISynchronizationScopeChangeListener extends EventListener {

	/**
	 * Notification that the scope has changed. The change may be the inclusion
	 * of new resource mappings, new resource traversals or both. This can be
	 * due to changes to the resource mappings used to calculate the traversals
	 * of the scope or because new resources have come under the control of the
	 * repository provider (or other entity) that generated the scope in the
	 * first place.
	 * 
	 * @param scope
	 *            the scope that has changed
	 * @param newTraversals
	 *            the new traversals (may be empty)
	 * @param newMappings
	 *            the new mappings (may be empty)
	 */
	void scopeChanged(ISynchronizationScope scope,
			ResourceMapping[] newMappings, ResourceTraversal[] newTraversals);
}
