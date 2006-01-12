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
package org.eclipse.team.internal.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.team.core.RepositoryProvider;

/**
 * Interface for listening to repository provider changes
 */
public interface IRepositoryProviderListener {
	void providerMapped(RepositoryProvider provider);
	void providerUnmapped(IProject project);
}
