/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.configurator;

import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;

/**
 * Declarative services component that provides an implementation of
 * {@link IBundleGroupProvider}. This allows the bundle group provider to be
 * made available in the service registry before this bundle has started.
 */
public class BundleGroupComponent implements IBundleGroupProvider {

	public IBundleGroup[] getBundleGroups() {
		return ConfigurationActivator.getConfigurator().getBundleGroups();
	}

	public String getName() {
		return ConfigurationActivator.getConfigurator().getName();
	}

}
