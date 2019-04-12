/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 ******************************************************************************/
package org.eclipse.ui.internal.quickaccess;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.quickaccess.QuickAccessProvider;

/**
 * @since 3.114
 *
 */
public class QuickAccessExtensionManager {

	private static final String EXTENSION_POINT_ID = PlatformUI.PLUGIN_ID + '.'
			+ IWorkbenchRegistryConstants.PL_QUICK_ACCESS;
	private static final String PROVIDER_TAG = "provider"; //$NON-NLS-1$

	public static Collection<QuickAccessProvider> getProviders() {
		return Arrays.stream(Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID))
				.filter(element -> PROVIDER_TAG.equals(element.getName())).map(element -> {
					try {
						return element.createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
					} catch (CoreException e) {
						WorkbenchPlugin.log(e);
						return null;
					}
				}).filter(QuickAccessProvider.class::isInstance).map(QuickAccessProvider.class::cast)
				.collect(Collectors.toList());
	}
}
