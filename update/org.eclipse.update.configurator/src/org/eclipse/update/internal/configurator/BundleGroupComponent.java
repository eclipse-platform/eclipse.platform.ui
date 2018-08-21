/*******************************************************************************
 * Copyright (c) 2009, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public IBundleGroup[] getBundleGroups() {
		ConfigurationActivator activator = ConfigurationActivator.getConfigurator();
		if (activator.bundleGroupProviderSR != null)
			// we manually registered the group in the activator; return no groups
			// the manually registered service will handle the groups we know about
			return new IBundleGroup[0];
		return activator.getBundleGroups();
	}

	@Override
	public String getName() {
		return ConfigurationActivator.getConfigurator().getName();
	}

}
