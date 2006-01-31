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
package org.eclipse.team.core.mapping;

import org.eclipse.core.resources.mapping.RemoteResourceMappingContext;


/**
 * A litener that will be notified whenever the list of projects that apply to
 * this scope change. Events will also be issued if the resource mapping context
 * of this manager is a {@link RemoteResourceMappingContext} and the remot state
 * of a resource that is a child of the projects.
 * <p>
 * This interface may be implemented by clients.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see IResourceMappingScopeManager#addListener(IScopeContextChangeListener)
 * 
 * @since 3.2
 */
public interface IScopeContextChangeListener {
	
	

}
