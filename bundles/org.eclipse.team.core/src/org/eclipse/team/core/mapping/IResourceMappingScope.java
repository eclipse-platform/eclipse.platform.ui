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

import org.eclipse.core.resources.IResource;

/**
 * @deprecated use {@link ISynchronizationScope} instead
 * @since 3.2
 */
public interface IResourceMappingScope {

	IResource[] getRoots();

	boolean contains(IResource resource);

}
