/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class represents provisional API. A provider is not required to implement this API.
 * Implementers, and those who reference it, do so with the awareness that this class may be
 * removed or substantially changed at future times without warning.
 * <p>
 * The intention is that this class will eventually replace <code>IProjectSetSerializer</code>.
 * At the current time it only complements this API by providing a notification mechanism
 * for informing repository providers when a project set has been created.
 * 
 * @see IProjectSetSerializer
 * @see RepositoryProviderType
 * 
 * @since 2.1
 */

public abstract class ProjectSetCapability {
	/**
	 * Notify the provider that a project set has been created at path.
	 * Only providers identified as having projects in the project set will be
	 * notified.  The project set may or may not be created in a workspace
	 * project (thus may not be a resource).
	 * 
	 * @param File the project set file that was created
	 */	
	public void projectSetCreated(File file, Object context, IProgressMonitor monitor) {
		//default is to do nothing
	}
	
}
