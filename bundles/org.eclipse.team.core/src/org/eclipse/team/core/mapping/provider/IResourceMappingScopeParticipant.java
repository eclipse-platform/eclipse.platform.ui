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
package org.eclipse.team.core.mapping.provider;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.team.core.mapping.IResourceMappingScope;
import org.eclipse.team.core.mapping.IResourceMappingScopeManager;

/**
 * A scope participant is responsible for ensuring that the resources
 * contained within an {@link IResourceMappingScope} that overlap with the
 * participant's model provider stay up-to-date
 * with the model elements (represented as {@link ResourceMapping} instances)
 * contained in the scope. 
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see org.eclipse.core.resources.mapping.ResourceMapping
 * @see IResourceMappingScopeManager
 * 
 * @since 3.2
 */
public interface IResourceMappingScopeParticipant {

}
